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

package org.eclipse.tracecompass.incubator.internal.virtual.machine.analysis.core.virtual.resources;

import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;

/**
 * State values that are used in the kernel event handler. It's much better to
 * use integer values whenever possible, since those take much less space in the
 * history file.
 *
 * @author Alexandre Montplaisir
 */
@SuppressWarnings("javadoc")
public interface StateValues {

    /* Machine Status*/
    int MACHINE_HOST = (1 << 0);
    int MACHINE_GUEST = (1 << 1);
    int MACHINE_CONTAINER = (1 << 2);
    int MACHINE_UNKNOWN = (1 << 3);

    /* CPU Status */
    int CPU_STATUS_IDLE = 0;
    /**
     * Soft IRQ raised, could happen in the CPU attribute but should not since
     * this means that the CPU went idle when a softirq was raised.
     */
    int CPU_STATUS_SOFT_IRQ_RAISED = (1 << 0);
    int CPU_STATUS_RUN_USERMODE = (1 << 1);
    int CPU_STATUS_RUN_SYSCALL = (1 << 2);
    int CPU_STATUS_SOFTIRQ = (1 << 3);
    int CPU_STATUS_IRQ = (1 << 4);
    int CPU_STATUS_IN_VM = (1 << 5);

    ITmfStateValue CPU_STATUS_IDLE_VALUE = TmfStateValue.newValueInt(CPU_STATUS_IDLE);
    ITmfStateValue CPU_STATUS_RUN_USERMODE_VALUE = TmfStateValue.newValueInt(CPU_STATUS_RUN_USERMODE);
    ITmfStateValue CPU_STATUS_RUN_SYSCALL_VALUE = TmfStateValue.newValueInt(CPU_STATUS_RUN_SYSCALL);
    ITmfStateValue CPU_STATUS_IRQ_VALUE = TmfStateValue.newValueInt(CPU_STATUS_IRQ);
    ITmfStateValue CPU_STATUS_SOFTIRQ_VALUE = TmfStateValue.newValueInt(CPU_STATUS_SOFTIRQ);
    ITmfStateValue CPU_STATUS_IN_VM_VALUE = TmfStateValue.newValueInt(CPU_STATUS_IN_VM);

    /* CPU condition*/
    int CONDITION_IN_VM = 0;
    int CONDITION_OUT_VM = 1;
    int CONDITION_UNKNOWN = 3;

//    ITmfStateValue CONDITION_IN_VM_VALUE = TmfStateValue.newValueInt(CONDITION_IN_VM);
//    ITmfStateValue CONDITION_OUT_VM_VALUE = TmfStateValue.newValueInt(CONDITION_OUT_VM);
//    ITmfStateValue CONDITION_UNKNOWN_VALUE = TmfStateValue.newValueInt(CONDITION_UNKNOWN);

    /* Process status */
    int PROCESS_STATUS_UNKNOWN = 0;
    int PROCESS_STATUS_WAIT_BLOCKED = 1;
    int PROCESS_STATUS_RUN_USERMODE = 2;
    int PROCESS_STATUS_RUN_SYSCALL = 3;
    int PROCESS_STATUS_INTERRUPTED = 4;
    int PROCESS_STATUS_WAIT_FOR_CPU = 5;
    int PROCESS_STATUS_WAIT_UNKNOWN = 6;
//
//    ITmfStateValue PROCESS_STATUS_UNKNOWN_VALUE = TmfStateValue.newValueInt(PROCESS_STATUS_UNKNOWN);
//    /**
//     * @since 1.0
//     */
//    ITmfStateValue PROCESS_STATUS_WAIT_UNKNOWN_VALUE = TmfStateValue.newValueInt(PROCESS_STATUS_WAIT_UNKNOWN);
//    ITmfStateValue PROCESS_STATUS_WAIT_BLOCKED_VALUE = TmfStateValue.newValueInt(PROCESS_STATUS_WAIT_BLOCKED);
//    ITmfStateValue PROCESS_STATUS_RUN_USERMODE_VALUE = TmfStateValue.newValueInt(PROCESS_STATUS_RUN_USERMODE);
//    ITmfStateValue PROCESS_STATUS_RUN_SYSCALL_VALUE = TmfStateValue.newValueInt(PROCESS_STATUS_RUN_SYSCALL);
//    ITmfStateValue PROCESS_STATUS_INTERRUPTED_VALUE = TmfStateValue.newValueInt(PROCESS_STATUS_INTERRUPTED);
//    ITmfStateValue PROCESS_STATUS_WAIT_FOR_CPU_VALUE = TmfStateValue.newValueInt(PROCESS_STATUS_WAIT_FOR_CPU);
//
//    /** Soft IRQ is raised, CPU is in user mode */
//    ITmfStateValue SOFT_IRQ_RAISED_VALUE = TmfStateValue.newValueInt(CPU_STATUS_SOFT_IRQ_RAISED);

    /** If the softirq is running and another is raised at the same time. */
    int CPU_STATUS_SOFT_IRQ_RAISED_RUNNING = CPU_STATUS_SOFT_IRQ_RAISED | CPU_STATUS_SOFTIRQ;
}
