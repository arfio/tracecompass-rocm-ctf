/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.internal.virtual.machine.analysis.core.model;

import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.model.HostThread;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Interface that represents the model of an hypervisor. Each hypervisor (or
 * tracing method for an hypervisor) should implement this.
 *
 * @TODO: Deprecate me in favor of {@link IVirtualEnvironmentModel}
 *
 * @author Geneviève Bastien
 */
public interface IVirtualMachineModel {

    /**
     * This value corresponds to the root namespace ID, as defined in the linux source code linux/include/linux/proc_ns.h
     *
     * <pre>
     * enum {
     *    [...]
     *    PROC_PID_INIT_INO   = 0xEFFFFFFCU,
     *    [...]
     * };
     * </pre>
     */
    long ROOT_NAMESPACE = 4026531836L;

    /**
     * Get the machine that ran this event
     *
     * @param event
     *            The trace event
     * @return The machine this event was run on or {@code null} if the machine
     *         is not one belonging to this model.
     */
    @Nullable VirtualMachine getCurrentMachine(ITmfEvent event);

    /**
     * Get a the set of events required for this model to apply.
     *
     * TODO: This should be updated to something else to fit the event layout
     * generic linux model
     *
     * @param layout
     *            The event layout
     *
     * @return The set of required events for this model
     */
    Set<String> getRequiredEvents(IKernelAnalysisEventLayout layout);

    /**
     * Get the virtual CPU that is entering hypervisor mode with this event.
     *
     * "Hypervisor mode" means the virtual CPU of the guest is running on the
     * host, but it is not running code from the guest, but rather other tasks
     * from the hypervisor. When hypervisor mode is entered, the process on the
     * host stops running guest code, so from the guest point of view, the
     * thread running on this CPU is preempted.
     *
     * @param event
     *            The event being handled
     * @param ht
     *            The current thread this event belongs to
     * @param layout
     *            The event layout
     * @return The virtual CPU entering hypervisor mode or {@code null} if the
     *         hypervisor is not being entered with this event.
     */
    @Nullable VirtualCPU getVCpuEnteringHypervisorMode(ITmfEvent event, HostThread ht, IKernelAnalysisEventLayout layout);

    /**
     * Get the virtual CPU that is exiting hypervisor mode with this event.
     *
     * "Hypervisor mode" means the virtual CPU of the guest is running on the
     * host, but it is not running code from the guest, but rather other tasks
     * from the hypervisor. When hypervisor mode is exited, the process on the
     * host runs guest code, so from the guest point of view, the thread running
     * on this CPU is actively running.
     *
     * @param event
     *            The event being handled
     * @param ht
     *            The current thread this event belongs to
     * @param layout
     *            The event layout
     * @return The virutal CPU exiting hypervisor mode or {@code null} if the
     *         hypervisor is not exiting with this event.
     */
    @Nullable VirtualCPU getVCpuExitingHypervisorMode(ITmfEvent event, HostThread ht, IKernelAnalysisEventLayout layout);

    /**
     * Get the virtual CPU from a guest that corresponds to a specific thread
     * from the host
     *
     * @param event
     *            The event being handled
     * @param ht
     *            The current thread this event belongs to. This thread should
     *            be running on the host.
     * @return The virtual CPU corresponding to this thread or {@code null} if
     *         no virtual CPU corresponds to the thread
     */
    @Nullable VirtualCPU getVirtualCpu(HostThread ht);

    /**
     * Get the host thread corresponding to a virtual CPU
     *
     * @param vcpu
     *            The vcpu for which to get the thread
     * @return The HostThread corresponding to this CPU, or <code>null</code> if no
     *         such thread is found
     */
    @Nullable HostThread getVirtualCpuTid(VirtualCPU vcpu);

    /**
     * Handles the event. This method will be called for each event required or
     * optional by the analysis, before any other handling is done on this
     * event.
     *
     * This is where each implementation of the model will build itself,
     * determine which guests are running on which hosts and get the necessary
     * information to be able to return the virtual CPUs requested by the other
     * methods of this interface.
     *
     * @param event
     *            The event being handled. It can come from any trace in the
     *            experiment
     * @param layout
     *            The layout associated with the trace of the event
     */
    void handleEvent(ITmfEvent event, IKernelAnalysisEventLayout layout);

}
