/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mohamad Gebai - Initial API and implementation
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.internal.virtual.machine.analysis.core.virtual.resources;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelThreadInformationProvider;
import org.eclipse.tracecompass.incubator.internal.virtual.machine.analysis.core.data.VcpuStateValues;
import org.eclipse.tracecompass.incubator.internal.virtual.machine.analysis.core.model.analysis.VirtualMachineModelAnalysis;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.interval.TmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue.Type;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperimentUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

/**
 * Module for the virtual machine CPU analysis. It tracks the status of the
 * virtual CPUs for each guest of the experiment.
 *
 * @author Mohamad Gebai
 * @author Geneviève Bastien
 */
public class VirtualResourcesAnalysis extends TmfStateSystemAnalysisModule {

    /** The ID of this analysis module */
    public static final String ID = "org.eclipse.tracecompass.incubator.virtual.machine.analysis.core.VirtualResourcesAnalysis"; //$NON-NLS-1$

    // TODO: Update with event layout when requirements are back */
    static final Set<String> REQUIRED_EVENTS = ImmutableSet.of(
            // LttngStrings.SCHED_SWITCH
            );

    /* State value for a preempted virtual CPU */
    private static final ITmfStateValue VCPU_PREEMPT_VALUE = TmfStateValue.newValueInt(VcpuStateValues.VCPU_PREEMPT);

    /**
     * Constructor
     */
    public VirtualResourcesAnalysis() {
        super();
    }

    @Override
    protected ITmfStateProvider createStateProvider() {
        ITmfTrace trace = getTrace();
        if (!(trace instanceof TmfExperiment)) {
            throw new IllegalStateException("The trace should be an experiment"); //$NON-NLS-1$
        }
        VirtualMachineModelAnalysis model = TmfTraceUtils.getAnalysisModuleOfClass(trace, VirtualMachineModelAnalysis.class, VirtualMachineModelAnalysis.ID);
        if (model == null) {
            throw new IllegalStateException("There should be a model analysis for this class"); //$NON-NLS-1$
        }
        model.schedule();
        if (!model.waitForInitialization()) {
            throw new IllegalStateException("Problem initializing the model analysis"); //$NON-NLS-1$
        }

        return new VirtualResourcesStateProvider((TmfExperiment) trace, model.getVirtualEnvironmentModel());
    }

    @Override
    protected @NonNull StateSystemBackendType getBackendType() {
        return StateSystemBackendType.FULL;
    }

    @Override
    public String getHelpText() {
        return Messages.getMessage(Messages.VirtualMachineCPUAnalysis_Help);
    }

    @Override
    protected Iterable<IAnalysisModule> getDependentAnalyses() {
        ITmfTrace exp = getTrace();
        if (exp instanceof TmfExperiment) {
            VirtualMachineModelAnalysis model = TmfTraceUtils.getAnalysisModuleOfClass(exp, VirtualMachineModelAnalysis.class, VirtualMachineModelAnalysis.ID);
            if (model == null) {
                throw new IllegalStateException("There should be a model analysis for this class"); //$NON-NLS-1$
            }
            return Collections.singleton(model);
        }

        return Collections.emptySet();
    }

    private static Multimap<Integer, ITmfStateInterval> createThreadMultimap() {

        /*
         * Create the multimap for threads with the appropriate comparator
         * objects for keys and values
         */
        final Multimap<Integer, ITmfStateInterval> map = TreeMultimap.create(
                /* Key comparator. Keys do not have to be sorted, just use natural sorting*/
                Comparator.naturalOrder(),

                /* Value comparator */
                (arg0, arg1) -> {
                    if (arg1.getStateValue() == VCPU_PREEMPT_VALUE && arg0.getStateValue() != VCPU_PREEMPT_VALUE) {
                        /*
                         * For VCPU_PREEMPT state values, the state has to be
                         * after any other state that it overlaps, because those
                         * intervals usually decorate the other intervals.
                         */
                        if (((Long) arg0.getEndTime()).compareTo(arg1.getStartTime()) < 0) {
                            return -1;
                        }
                        return ((Long) arg0.getStartTime()).compareTo(arg1.getEndTime());
                    }
                    /* Otherwise, we use ordering by start time */
                    return (((Long) arg0.getStartTime()).compareTo(arg1.getStartTime()));
                });
        return map;
    }

    /**
     * Get the status intervals for the threads from a virtual machine. Those
     * intervals are correlated with the data from the virtual CPU's preemption
     * status.
     *
     * This method uses the Linux Kernel Analysis data for the thread's status
     * intervals.
     *
     * @param vmQuark
     *            The quark of the virtual machine
     * @param start
     *            The start time of the period to get the intervals from
     * @param end
     *            The end time of the period to get the intervals from
     * @param resolution
     *            The resolution
     * @param monitor
     *            A progress monitor for this task
     * @return A map of status intervals for the machine's threads, including
     *         preempted intervals. Intervals from the thread status and the CPU
     *         preemption status overlap and are ordered such that CPU
     *         preemption intervals are after any interval they overlap with
     */
    public Multimap<Integer, ITmfStateInterval> getUpdatedThreadIntervals(int vmQuark, long start, long end, long resolution, IProgressMonitor monitor) {

        final Multimap<Integer, ITmfStateInterval> map = createThreadMultimap();

        ITmfStateSystem ss = getStateSystem();
        if (ss == null) {
            return map;
        }
        ITmfTrace trace = getTrace();
        if (!(trace instanceof TmfExperiment)) {
            return map;
        }

        String vmHostId = ss.getAttributeName(vmQuark);
        KernelAnalysisModule kernelModule = TmfExperimentUtils.getAnalysisModuleOfClassForHost((TmfExperiment) trace, vmHostId, KernelAnalysisModule.class);
        if (kernelModule == null) {
            return map;
        }

        /*
         * Initialize the map with the original status intervals from the kernel
         * module
         */
        for (Integer tid : KernelThreadInformationProvider.getThreadIds(kernelModule)) {
            map.putAll(tid, KernelThreadInformationProvider.getStatusIntervalsForThread(kernelModule, tid, start, end, resolution, monitor));
            if (monitor.isCanceled()) {
                return map;
            }
        }

        try {
            /* Correlate thread information with virtual CPU information */
            for (Integer vcpuQuark : ss.getSubAttributes(vmQuark, false)) {
                Long virtualCPU = Long.parseLong(ss.getAttributeName(vcpuQuark));
                Integer statusQuark = ss.getQuarkRelative(vcpuQuark, VmAttributes.STATUS);

                for (ITmfStateInterval cpuInterval : StateSystemUtils.queryHistoryRange(ss, statusQuark, start, end - 1, resolution, monitor)) {
                    ITmfStateValue stateValue = cpuInterval.getStateValue();
                    if (stateValue.getType() == Type.INTEGER) {
                        int value = stateValue.unboxInt();
                        /*
                         * If the current CPU is either preempted or in
                         * hypervisor mode, add preempted intervals to running
                         * processes
                         */
                        if ((value & (VcpuStateValues.VCPU_PREEMPT | VcpuStateValues.VCPU_VMM)) == 0) {
                            continue;
                        }
                        Integer threadOnCpu = KernelThreadInformationProvider.getThreadOnCpu(kernelModule, virtualCPU, cpuInterval.getStartTime());
                        if (threadOnCpu != null) {
                            map.put(threadOnCpu, new TmfStateInterval(cpuInterval.getStartTime(), cpuInterval.getEndTime(), threadOnCpu, VcpuStateValues.VCPU_PREEMPT));
                        }
                    }
                }
            }
        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
        }
        return map;
    }

}
