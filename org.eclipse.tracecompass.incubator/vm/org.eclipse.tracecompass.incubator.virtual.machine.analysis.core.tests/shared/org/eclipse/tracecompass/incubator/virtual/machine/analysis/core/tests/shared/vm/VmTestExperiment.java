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

package org.eclipse.tracecompass.incubator.virtual.machine.analysis.core.tests.shared.vm;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.incubator.internal.virtual.machine.analysis.core.trace.VirtualMachineExperiment;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;

import com.google.common.collect.ImmutableSet;

/**
 * List virtual machine experiments that can be used in unit tests
 *
 * @author Geneviève Bastien
 */
public enum VmTestExperiment {

    /**
     * Virtual machine experiment: 1 guest, 1 host, using QEMU/KVM model
     */
    ONE_QEMUKVM(VmTraces.HOST_ONE_QEMUKVM, VmTraces.GUEST_ONE_QEMUKVM),
    /**
     * Virtual machine experiment: 1 trace, with multiple container data
     */
    ONE_CONTAINER(VmTraces.ONE_CONTAINER),
    /**
     * Virtual machine experiment: 1 guest, 1 host, guest has a container
     */
    QEMU_CONTAINER(VmTraces.HOST_QEMUKVM_CONTAINER, VmTraces.GUEST_QEMUKVM_CONTAINER),
    /**
     * Virtual machine experiment: 1 guest, 1 host, guest has a container
     */
    TWO_HOSTS(VmTraces.HOST_ONE_QEMUKVM, VmTraces.GUEST_ONE_QEMUKVM, VmTraces.ONE_CONTAINER);

    private @NonNull Set<VmTraces> fTraces = new HashSet<>();

    private VmTestExperiment(VmTraces... traces) {
        for (VmTraces trace : traces) {
            fTraces.add(trace);
        }
    }

    /**
     * Return a VirtualMachineExperiment object for this experiment with all its
     * traces. It will be already initTrace()'ed.
     *
     * Make sure you call {@link #exists()} before calling this! This will make
     * sure all traces in the experiment are available
     *
     * After being used by unit tests, the experiment must be properly disposed
     * of by calling the {@link VirtualMachineExperiment#dispose()} method on
     * the object returned by this method.
     *
     * @param deleteSuppFiles
     *            Indicate whether to make sure supplementary files are deleted
     * @return A VirtualMachineExperiment object corresponding to this
     *         experiment
     */
    public synchronized @NonNull TmfExperiment getExperiment(boolean deleteSuppFiles) {
        Set<@NonNull ITmfTrace> traces = new HashSet<>();
        for (VmTraces trace : fTraces) {
            ITmfTrace tmfTrace = trace.getTrace();
            if (tmfTrace != null) {
                traces.add(tmfTrace);
            }
        }
        String expName = name();
        VirtualMachineExperiment experiment = new VirtualMachineExperiment(expName, traces);
        if (deleteSuppFiles) {
            /*
             * Delete the supplementary files, so that the next iteration
             * rebuilds the state system.
             */
            File suppDir = new File(TmfTraceManager.getSupplementaryFileDir(experiment));
            for (File file : suppDir.listFiles()) {
                file.delete();
            }
        }
        return experiment;
    }

    /**
     * Check if all the traces actually exist on disk or not.
     *
     * @return If all traces for this experiment are present
     */
    public boolean exists() {
        boolean exists = true;
        for (VmTraces trace : fTraces) {
            exists &= trace.exists();
        }
        return exists;
    }

    /**
     * Get the traces for this experiment
     *
     * @return The traces of this experiment
     */
    public Set<VmTraces> getTraces() {
        return ImmutableSet.copyOf(fTraces);
    }

}
