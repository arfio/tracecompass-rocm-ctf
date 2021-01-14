/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.internal.virtual.machine.analysis.core.fused.handlers;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.List;

import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.incubator.internal.virtual.machine.analysis.core.fused.FusedAttributes;
import org.eclipse.tracecompass.incubator.internal.virtual.machine.analysis.core.model.VirtualCPU;
import org.eclipse.tracecompass.incubator.internal.virtual.machine.analysis.core.model.VirtualMachine;
import org.eclipse.tracecompass.incubator.internal.virtual.machine.analysis.core.virtual.resources.LinuxValues;
import org.eclipse.tracecompass.incubator.internal.virtual.machine.analysis.core.virtual.resources.StateValues;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * @author Cédric Biancheri
 */
public class SchedSwitchHandler extends VMKernelEventHandler {

    /**
     * Constructor
     *
     * @param layout
     *            The event layout of the trace being analysed by this handler
     * @param sp
     *            The state provider
     */
    public SchedSwitchHandler(IKernelAnalysisEventLayout layout, FusedVirtualMachineStateProvider sp) {
        super(layout, sp);
    }

    @Override
    public void handleEvent(ITmfStateSystemBuilder ss, ITmfEvent event) {
        Integer cpu = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), TmfCpuAspect.class, event);
        if (cpu == null) {
            return;
        }
        FusedVirtualMachineStateProvider sp = getStateProvider();
        VirtualMachine host = sp.getCurrentMachine(event);
        VirtualCPU cpuObject = VirtualCPU.getVirtualCPU(host, cpu.longValue());
        if (host != null && host.isGuest()) {
            Integer physicalCPU = sp.getPhysicalCPU(host, cpu);
            if (physicalCPU != null) {
                cpu = physicalCPU;
            } else {
                return;
            }
        }

        ITmfEventField content = event.getContent();
        Integer prevTid = ((Long) content.getField(getLayout().fieldPrevTid()).getValue()).intValue();
        Long prevState = checkNotNull((Long) content.getField(getLayout().fieldPrevState()).getValue());
        String nextProcessName = checkNotNull((String) content.getField(getLayout().fieldNextComm()).getValue());
        Integer nextTid = ((Long) content.getField(getLayout().fieldNextTid()).getValue()).intValue();
        Integer nextPrio = ((Long) content.getField(getLayout().fieldNextPrio()).getValue()).intValue();
        String machineHost = event.getTrace().getHostId();

        /* Will never return null since "cpu" is null checked */
        String formerThreadAttributeName = FusedVMEventHandlerUtils.buildThreadAttributeName(prevTid, cpu);
        String currenThreadAttributeName = FusedVMEventHandlerUtils.buildThreadAttributeName(nextTid, cpu);

        int nodeThreads = FusedVMEventHandlerUtils.getNodeThreads(ss);
        int formerThreadNode = ss.getQuarkRelativeAndAdd(nodeThreads, machineHost, formerThreadAttributeName);
        int newCurrentThreadNode = ss.getQuarkRelativeAndAdd(nodeThreads, machineHost, currenThreadAttributeName);
        int currentMachineQuark = ss.getQuarkAbsoluteAndAdd(FusedAttributes.HOSTS, machineHost);
        int machineContainerQuark = ss.getQuarkRelativeAndAdd(currentMachineQuark, FusedAttributes.CONTAINERS);

        long timestamp = FusedVMEventHandlerUtils.getTimestamp(event);
        /* Set the status of the process that got scheduled out. */
        setOldProcessStatus(ss, prevState, formerThreadNode, timestamp);

        /* Set the status of the new scheduled process */
        FusedVMEventHandlerUtils.setProcessToRunning(timestamp, newCurrentThreadNode, ss);

        /* Set the exec name of the new process */
        setNewProcessExecName(ss, nextProcessName, newCurrentThreadNode, timestamp);

        /* Set the current prio for the new process */
        setNewProcessPio(ss, nextPrio, newCurrentThreadNode, timestamp);

        /* Make sure the PPID and system_call sub-attributes exist */
        ss.getQuarkRelativeAndAdd(newCurrentThreadNode, FusedAttributes.SYSTEM_CALL);
        ss.getQuarkRelativeAndAdd(newCurrentThreadNode, FusedAttributes.PPID);

        /* Set the current scheduled process on the relevant CPU */
        int currentCPUNode = FusedVMEventHandlerUtils.getCurrentCPUNode(cpu, ss);

        /*
         * If the trace that generates the event doesn't match the currently
         * running machine on this pcpu then we do not modify the state system.
         */
        boolean modify = true;
        int machineNameQuark = ss.getQuarkRelativeAndAdd(currentCPUNode, FusedAttributes.MACHINE_NAME);
        try {
            modify = ss.querySingleState(timestamp, machineNameQuark).getStateValue().unboxStr().equals(machineHost);
        } catch (StateSystemDisposedException e) {
            e.printStackTrace();
        }

        setCpuProcess(ss, nextTid, timestamp, currentCPUNode, modify);

        /* Set the status of the CPU itself */
        int stateCpu = setCpuStatus(ss, nextTid, newCurrentThreadNode, timestamp, currentCPUNode, modify);

        /* Remember the cpu used by the namespaces containing the next thread */
        if (nextTid != 0) {
            List<Long> namespaces = FusedVMEventHandlerUtils.getProcessNSIDs(ss, newCurrentThreadNode);

            for (Long namespace : namespaces) {
                ss.getQuarkRelativeAndAdd(machineContainerQuark, namespace.toString(), FusedAttributes.PCPUS, cpu.toString());
            }
        }

        cpuObject.setCurrentState(stateCpu);
        cpuObject.setCurrentThread(nextTid);
    }

    private static void setOldProcessStatus(ITmfStateSystemBuilder ss, Long prevState, Integer formerThreadNode, long timestamp) {
        Integer status;
        /*
         * Empirical observations and look into the linux code have shown that
         * the TASK_STATE_MAX flag is used internally and |'ed with other
         * states, most often the running state, so it is ignored from the
         * prevState value.
         *
         * Since Linux 4.1, the TASK_NOLOAD state was created and TASK_STATE_MAX
         * is now 2048. We use TASK_NOLOAD as the new max because it does not
         * modify the displayed state value.
         */
        int state = (int) (prevState & (LinuxValues.TASK_NOLOAD - 1));

        if (isRunning(state)) {
            status = StateValues.PROCESS_STATUS_WAIT_FOR_CPU;
        } else if (isWaiting(state)) {
            status = StateValues.PROCESS_STATUS_WAIT_BLOCKED;
        } else if (isDead(state)) {
            status = null;
        } else {
            status = StateValues.PROCESS_STATUS_WAIT_UNKNOWN;
        }
        int quark = ss.getQuarkRelativeAndAdd(formerThreadNode, FusedAttributes.STATUS);
        ss.modifyAttribute(timestamp, status, quark);

    }

    private static boolean isDead(int state) {
        return (state & LinuxValues.TASK_DEAD) != 0;
    }

    private static boolean isWaiting(int state) {
        return (state & (LinuxValues.TASK_INTERRUPTIBLE | LinuxValues.TASK_UNINTERRUPTIBLE)) != 0;
    }

    private static boolean isRunning(int state) {
        // special case, this means ALL STATES ARE 0
        // this is effectively an anti-state
        return state == 0;
    }

    private static int setCpuStatus(ITmfStateSystemBuilder ss, Integer nextTid, Integer newCurrentThreadNode, long timestamp, int currentCPUNode, boolean modify) {
        int quark;
        int status;
        if (nextTid > 0) {
            /* Check if the entering process is in kernel or user mode */
            quark = ss.getQuarkRelativeAndAdd(newCurrentThreadNode, FusedAttributes.SYSTEM_CALL);
            Object syscall = ss.queryOngoing(quark);
            if (syscall == null) {
                status = StateValues.CPU_STATUS_RUN_USERMODE;
            } else {
                status = StateValues.CPU_STATUS_RUN_SYSCALL;
            }
            if (modify) {
                quark = ss.getQuarkRelativeAndAdd(currentCPUNode, FusedAttributes.STATUS);
                ss.modifyAttribute(timestamp, status, quark);
            }
        } else {
            status = StateValues.CPU_STATUS_IDLE;
            if (modify) {
                quark = ss.getQuarkRelativeAndAdd(currentCPUNode, FusedAttributes.STATUS);
                ss.modifyAttribute(timestamp, status, quark);
            }
        }
        return status;

    }

    private static void setCpuProcess(ITmfStateSystemBuilder ss, Integer nextTid, long timestamp, int currentCPUNode, boolean modify) {
        int quark;
        quark = ss.getQuarkRelativeAndAdd(currentCPUNode, FusedAttributes.CURRENT_THREAD);
        if (modify) {
            ss.modifyAttribute(timestamp, nextTid, quark);
        }
    }

    private static void setNewProcessPio(ITmfStateSystemBuilder ss, Integer nextPrio, Integer newCurrentThreadNode, long timestamp) {
        int quark;
        quark = ss.getQuarkRelativeAndAdd(newCurrentThreadNode, FusedAttributes.PRIO);
        ss.modifyAttribute(timestamp, nextPrio, quark);
    }

    private static void setNewProcessExecName(ITmfStateSystemBuilder ss, String nextProcessName, Integer newCurrentThreadNode, long timestamp) {
        int quark;
        quark = ss.getQuarkRelativeAndAdd(newCurrentThreadNode, FusedAttributes.EXEC_NAME);
        ss.modifyAttribute(timestamp, nextProcessName, quark);
    }

}
