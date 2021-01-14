/*******************************************************************************
 * Copyright (c) 2015, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.handlers;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.StateValues;
import org.eclipse.tracecompass.analysis.os.linux.core.model.ProcessStatus;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.Attributes;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * Kernel Event Handler Utils is a collection of static methods to be used in
 * subclasses of {@link KernelEventHandler}.
 *
 * @author Matthew Khouzam
 * @author Francis Giraldeau
 */
public final class KernelEventHandlerUtils {

    private KernelEventHandlerUtils() {
    }

    /**
     * Get CPU
     *
     * @param event
     *            The event containing the cpu
     *
     * @return the CPU number (null for not set)
     */
    public static @Nullable Integer getCpu(ITmfEvent event) {
        return TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), TmfCpuAspect.class, event);
    }

    /**
     * Gets the current CPU quark
     *
     * @param cpuNumber
     *            The cpu number
     * @param ss
     *            the state system
     *
     * @return the current CPU quark -1 for not set
     */
    public static int getCurrentCPUNode(Integer cpuNumber, ITmfStateSystemBuilder ss) {
        return ss.getQuarkRelativeAndAdd(getNodeCPUs(ss), cpuNumber.toString());
    }

    /**
     * Get the timestamp of the event
     *
     * @param event
     *            the event containing the timestamp
     *
     * @return the timestamp in long format
     */
    public static long getTimestamp(ITmfEvent event) {
        return event.getTimestamp().toNanos();
    }

    /**
     * Get the current thread node
     *
     * @param cpuNumber
     *            The cpu number
     * @param ss
     *            the state system
     *
     * @return the current thread node quark
     */
    public static int getCurrentThreadNode(Integer cpuNumber, ITmfStateSystemBuilder ss) {
        /*
         * Shortcut for the "current thread" attribute node. It requires
         * querying the current CPU's current thread.
         */
        int quark = ss.getQuarkRelativeAndAdd(getCurrentCPUNode(cpuNumber, ss), Attributes.CURRENT_THREAD);
        ITmfStateValue value = ss.queryOngoingState(quark);
        int thread = value.isNull() ? -1 : value.unboxInt();
        return ss.getQuarkRelativeAndAdd(getNodeThreads(ss), Attributes.buildThreadAttributeName(thread, cpuNumber));
    }

    /**
     * When we want to set a process back to a "running" state, first check its
     * current System_call attribute. If there is a system call active, we put
     * the process back in the syscall state. If not, we put it back in user
     * mode state.
     *
     * @param timestamp
     *            the time in the state system of the change
     * @param currentThreadNode
     *            The current thread node
     * @param ssb
     *            the state system
     * @throws TimeRangeException
     *             the time is out of range
     * @throws StateValueTypeException
     *             the attribute was not set with int values
     */
    public static void setProcessToRunning(long timestamp, int currentThreadNode, ITmfStateSystemBuilder ssb)
            throws TimeRangeException, StateValueTypeException {
        int quark = ssb.getQuarkRelativeAndAdd(currentThreadNode, Attributes.SYSTEM_CALL);

        ITmfStateValue value;
        if (ssb.queryOngoingState(quark).isNull()) {
            /* We were in user mode before the interruption */
            value = ProcessStatus.RUN.getStateValue();
        } else {
            /* We were previously in kernel mode */
            value = ProcessStatus.RUN_SYTEMCALL.getStateValue();
        }
        ssb.modifyAttribute(timestamp, value.unboxValue(), currentThreadNode);
    }

    /**
     * Get the IRQs node
     *
     * @param cpuNumber
     *            the cpu core
     * @param ss
     *            the state system
     * @return the IRQ node quark
     */
    public static int getNodeIRQs(int cpuNumber, ITmfStateSystemBuilder ss) {
        return ss.getQuarkAbsoluteAndAdd(Attributes.CPUS, Integer.toString(cpuNumber), Attributes.IRQS);
    }

    /**
     * Get the CPUs node
     *
     * @param ss
     *            the state system
     * @return the CPU node quark
     */
    public static int getNodeCPUs(ITmfStateSystemBuilder ss) {
        return ss.getQuarkAbsoluteAndAdd(Attributes.CPUS);
    }

    /**
     * Get the Soft IRQs node
     *
     * @param cpuNumber
     *            the cpu core
     * @param ss
     *            the state system
     * @return the Soft IRQ node quark
     */
    public static int getNodeSoftIRQs(int cpuNumber, ITmfStateSystemBuilder ss) {
        return ss.getQuarkAbsoluteAndAdd(Attributes.CPUS, Integer.toString(cpuNumber), Attributes.SOFT_IRQS);
    }

    /**
     * Get the threads node
     *
     * @param ss
     *            the state system
     * @return the threads quark
     */
    public static int getNodeThreads(ITmfStateSystemBuilder ss) {
        return ss.getQuarkAbsoluteAndAdd(Attributes.THREADS);
    }

    /**
     * Update the CPU's status when it has been modified by an interruption.
     *
     * @param timestamp
     *            the time when the status of the cpu is "leaving irq"
     * @param cpuNumber
     *            the cpu returning to its previous state
     *
     * @param ssb
     *            State system
     * @throws StateValueTypeException
     *             the attribute is not set as an int
     * @throws TimeRangeException
     *             the time is out of range
     */
    public static void updateCpuStatus(long timestamp, Integer cpuNumber, ITmfStateSystemBuilder ssb)
            throws StateValueTypeException, TimeRangeException {
        int currentCPUNode = getCurrentCPUNode(cpuNumber, ssb);

        ITmfStateValue value = getCpuStatus(ssb, currentCPUNode);
        ssb.modifyAttribute(timestamp, value.unboxValue(), currentCPUNode);
    }

    /**
     * Get the ongoing Status state of a CPU.
     *
     * This will look through the states of the
     *
     * <ul>
     * <li>IRQ</li>
     * <li>Soft IRQ</li>
     * <li>Process</li>
     * </ul>
     *
     * under the CPU, giving priority to states higher in the list. If the state
     * is a null value, we continue looking down the list.
     *
     * @param ssb
     *            The state system
     * @param cpuQuark
     *            The *quark* of the CPU we are looking for. Careful, this is
     *            NOT the CPU number (or attribute name)!
     * @return The state value that represents the status of the given CPU
     */
    private static ITmfStateValue getCpuStatus(ITmfStateSystemBuilder ssb, int cpuQuark) {

        /* Check if there is a IRQ running */
        int irqQuarks = ssb.getQuarkRelativeAndAdd(cpuQuark, Attributes.IRQS);
        List<Integer> irqs = ssb.getSubAttributes(irqQuarks, false);
        for (Integer quark : irqs) {
            ITmfStateValue irqState = ssb.queryOngoingState(quark);
            if (!irqState.isNull()) {
                return StateValues.CPU_STATUS_IRQ_VALUE;
            }
        }

        /* Check if there is a soft IRQ running */
        int softIrqQuarks = ssb.getQuarkRelativeAndAdd(cpuQuark, Attributes.SOFT_IRQS);
        List<Integer> softIrqs = ssb.getSubAttributes(softIrqQuarks, false);
        for (Integer quark : softIrqs) {
            if (isInSoftIrq(ssb, quark)) {
                return StateValues.CPU_STATUS_SOFTIRQ_VALUE;
            }
        }

        /*
         * Check if there is a thread running. If not, report IDLE. If there is,
         * report the running state of the thread (usermode or system call).
         */
        int currentThreadQuark = ssb.getQuarkRelativeAndAdd(cpuQuark, Attributes.CURRENT_THREAD);
        ITmfStateValue currentThreadState = ssb.queryOngoingState(currentThreadQuark);
        if (currentThreadState.isNull()) {
            return TmfStateValue.nullValue();
        }
        int tid = currentThreadState.unboxInt();
        if (tid == 0) {
            return StateValues.CPU_STATUS_IDLE_VALUE;
        }
        int threadSystemCallQuark = ssb.getQuarkAbsoluteAndAdd(Attributes.THREADS, Integer.toString(tid), Attributes.SYSTEM_CALL);
        return (ssb.queryOngoingState(threadSystemCallQuark).isNull() ? StateValues.CPU_STATUS_RUN_USERMODE_VALUE : StateValues.CPU_STATUS_RUN_SYSCALL_VALUE);
    }

    /**
     * Get the highest active IRQ for all CPUs for the IRQ type (SOFT or not) and ID
     *
     * @param ss
     *            state system
     * @param irqType
     *            IRQ type : {@link Attributes#IRQS} or
     *            {@link Attributes#SOFT_IRQS}.
     * @param irqId
     *            the id of the IRQ for which we want aggregate status
     * @return the highest IRQ (IRQ, SOFT_IRQ or raised SOFT_IRQ.
     */
    public static ITmfStateValue getAggregate(ITmfStateSystemBuilder ss, String irqType, Integer irqId) {
        ITmfStateValue value = TmfStateValue.nullValue();
        for (Integer irqQuark : ss.getQuarks(Attributes.CPUS, "*", irqType, irqId.toString())) { //$NON-NLS-1$
            ITmfStateValue ongoing = ss.queryOngoingState(irqQuark);
            if (ongoing.unboxInt() > value.unboxInt()) {
                value = ongoing;
            }
        }
        return value;
    }

    /**
     * Find a CPU which has this IRQ raised.
     *
     * @param ss
     *            state system
     * @param irqId
     *            IRQ id
     * @return the CPU number if a CPU has this IRQ raised else null
     */
    public static @Nullable Integer getCpuForIrq(ITmfStateSystemBuilder ss, Integer irqId) {
        for (Integer irqs : ss.getQuarks(Attributes.CPUS, "*", Attributes.IRQS, irqId.toString())) { //$NON-NLS-1$
            if (!ss.queryOngoingState(irqs).isNull()) {
                return Integer.parseInt(ss.getAttributeName(ss.getParentAttributeQuark(ss.getParentAttributeQuark(irqs))));
            }
        }
        return null;
    }

    /**
     * Returns true if the Soft IRQ is in running state
     *
     * @param ss
     *            state system
     * @param softIrqQuark
     *            soft IRQ quark
     * @return true if the Soft IRQ is in running state
     */
    public static boolean isInSoftIrq(ITmfStateSystemBuilder ss, int softIrqQuark) {
        ITmfStateValue state = ss.queryOngoingState(softIrqQuark);
        return (!state.isNull() &&
                (state.unboxInt() & StateValues.CPU_STATUS_SOFTIRQ) == StateValues.CPU_STATUS_SOFTIRQ);
    }
}
