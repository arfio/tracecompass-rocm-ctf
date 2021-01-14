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

import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.incubator.internal.virtual.machine.analysis.core.fused.FusedAttributes;
import org.eclipse.tracecompass.incubator.internal.virtual.machine.analysis.core.model.VirtualCPU;
import org.eclipse.tracecompass.incubator.internal.virtual.machine.analysis.core.model.VirtualMachine;
import org.eclipse.tracecompass.incubator.internal.virtual.machine.analysis.core.virtual.resources.StateValues;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * @author Cédric Biancheri
 */
public class SysEntryHandler extends VMKernelEventHandler {

    /**
     * Constructor
     *
     * @param layout
     *            the layout
     * @param sp
     *            the state provider
     */
    public SysEntryHandler(IKernelAnalysisEventLayout layout, FusedVirtualMachineStateProvider sp) {
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
        /* Assign the new system call to the process */
        int currentThreadNode = FusedVMEventHandlerUtils.getCurrentThreadNode(cpu, ss);
        int quark = ss.getQuarkRelativeAndAdd(currentThreadNode, FusedAttributes.SYSTEM_CALL);
        long timestamp = FusedVMEventHandlerUtils.getTimestamp(event);
        ss.modifyAttribute(timestamp, event.getName(), quark);

        /* Put the process in system call mode */
        quark = ss.getQuarkRelativeAndAdd(currentThreadNode, FusedAttributes.STATUS);
        ss.modifyAttribute(timestamp, StateValues.PROCESS_STATUS_RUN_SYSCALL, quark);

        /* Put the CPU in system call (kernel) mode */
        int currentCPUNode = FusedVMEventHandlerUtils.getCurrentCPUNode(cpu, ss);

        /*
         * If the trace that generates the event doesn't match the currently
         * running machine on this pcpu then we do not modify the state system.
         */
        boolean modify = true;
        if (host != null) {
            int machineNameQuark = ss.getQuarkRelativeAndAdd(currentCPUNode, FusedAttributes.MACHINE_NAME);
            try {
                modify = ss.querySingleState(timestamp, machineNameQuark).getStateValue().unboxStr().equals(host.getHostId());
            } catch (StateSystemDisposedException e) {
                e.printStackTrace();
            }
        }

        quark = ss.getQuarkRelativeAndAdd(currentCPUNode, FusedAttributes.STATUS);
        if (modify) {
            ss.modifyAttribute(timestamp, StateValues.CPU_STATUS_RUN_SYSCALL, quark);
        }
        cpuObject.setCurrentState(StateValues.CPU_STATUS_RUN_SYSCALL);
    }

}
