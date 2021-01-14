/*******************************************************************************
 * Copyright (c) 2020 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.incubator.internal.rocm.ctf.core.analysis;

import java.util.List;
import java.util.LinkedList;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * @author Arnaud Fiorini
 *
 */
public class RocmCtfCallStackStateProvider extends AbstractTmfStateProvider {

    private static final String ID = "org.eclipse.tracecompass.incubator.rocm.ctf.callstackstateprovider"; //$NON-NLS-1$
    static final @NonNull String EDGES_LANE = "EDGES"; //$NON-NLS-1$
    static final @NonNull String PROCESSES = "Processes"; //$NON-NLS-1$

    // Event types
    static final @NonNull String GPU_KERNEL = "compute_kernels_hsa"; //$NON-NLS-1$
    static final @NonNull String HSA_API = "hsa_api"; //$NON-NLS-1$
    static final @NonNull String HIP_API = "hip_api"; //$NON-NLS-1$
    static final @NonNull String KFD_API = "kfd_api"; //$NON-NLS-1$
    static final @NonNull String HCC_OPS = "hcc_ops"; //$NON-NLS-1$
    static final @NonNull String ROCTX = "roctx"; //$NON-NLS-1$
    static final @NonNull String ASYNC_COPY = "async_copy"; //$NON-NLS-1$

    final List<Long> currentKernelDispatched = new LinkedList<>();

    /**
     * @param trace Trace to follow
     */
    public RocmCtfCallStackStateProvider(@NonNull ITmfTrace trace) {
        super(trace, ID);
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public ITmfStateProvider getNewInstance() {
        return new RocmCtfCallStackStateProvider(getTrace());
    }

    @Override
    protected void eventHandle(ITmfEvent event) {
        ITmfEventField content = event.getContent();
        if (content == null) {
            return;
        }
        ITmfStateSystemBuilder ssb = getStateSystemBuilder();
        if (ssb == null) {
            return;
        }
        int callStackQuark = getCorrectQuark(ssb, event);
        if (callStackQuark == -1) {
            return;
        }
        long timestamp = event.getTimestamp().toNanos();
        String eventName;
        if (event.getName().equals(GPU_KERNEL)) {
            eventName = (String) content.getField("kernel_name").getValue(); //$NON-NLS-1$
            long eventDispatchId = (long) content.getField("kernel_dispatch_id").getValue(); //$NON-NLS-1$
            if (currentKernelDispatched.contains(eventDispatchId)) {
                ssb.popAttribute(timestamp, callStackQuark);
            } else {
                currentKernelDispatched.add(eventDispatchId);
                ssb.pushAttribute(timestamp, eventName, callStackQuark);
            }
        } else {
            eventName = (String) content.getField("name").getValue(); //$NON-NLS-1$
            if (eventName.endsWith("_exit")) { //$NON-NLS-1$
                ssb.popAttribute(timestamp, callStackQuark);
            } else {
                ssb.pushAttribute(timestamp, eventName.substring(0, eventName.length()-6), callStackQuark);
            }
        }

    }

    private static int getCorrectQuark(ITmfStateSystemBuilder ssb, @NonNull ITmfEvent event) {
        switch (event.getName()) {
        case HSA_API:
        case HIP_API:
        case KFD_API:
            return getApiCallStackQuark(ssb, event);
        case ASYNC_COPY:
        case GPU_KERNEL:
        case HCC_OPS:
        case ROCTX:
            return getGpuActivityCallStackQuark(ssb, event);
        default:
            return -1;
        }
    }

    private static int getGpuActivityCallStackQuark(ITmfStateSystemBuilder ssb, @NonNull ITmfEvent event) {
        int gpuActivity = ssb.getQuarkAbsoluteAndAdd(PROCESSES, "GPU Activity"); //$NON-NLS-1$
        if (event.getName().equals(GPU_KERNEL)) {
            long queueId = (long) event.getContent().getField("queue_id").getValue(); //$NON-NLS-1$
            long gpuId = (long) event.getContent().getField("gpu_id").getValue(); //$NON-NLS-1$
            int queueQuark = ssb.getQuarkRelativeAndAdd(gpuActivity, "GPU " + Long.toString(gpuId)  //$NON-NLS-1$
                + " Queue " + Long.toString(queueId)); //$NON-NLS-1$
            int callStackQuark = ssb.getQuarkRelativeAndAdd(queueQuark, "CallStack"); //$NON-NLS-1$
            return callStackQuark;
        } else if (event.getName().equals(HCC_OPS)) {
            int gpuQuark = ssb.getQuarkRelativeAndAdd(gpuActivity, "GPU Kernels"); //$NON-NLS-1$
            int callStackQuark = ssb.getQuarkRelativeAndAdd(gpuQuark, "CallStack"); //$NON-NLS-1$
            return callStackQuark;
        }
        int copyQuark = ssb.getQuarkRelativeAndAdd(gpuActivity, "Memory Transfers"); //$NON-NLS-1$
        int callStackQuark = ssb.getQuarkRelativeAndAdd(copyQuark, "CallStack"); //$NON-NLS-1$
        return callStackQuark;
    }

    private static int getApiCallStackQuark(ITmfStateSystemBuilder ssb, @NonNull ITmfEvent event) {
        int systemQuark = ssb.getQuarkAbsoluteAndAdd(PROCESSES, "System"); //$NON-NLS-1$
        int apiQuark = ssb.getQuarkRelativeAndAdd(systemQuark, event.getName().toUpperCase());
        int callStackQuark = ssb.getQuarkRelativeAndAdd(apiQuark, "CallStack"); //$NON-NLS-1$
        return callStackQuark;
    }

}
