/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Matthew Khouzam - Improved validation
 ******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.trace;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelUtils;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelTrace;
import org.eclipse.tracecompass.internal.lttng2.common.core.trace.ILttngTrace;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.Activator;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.ContextPidAspect;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.ContextTidAspect;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout.Lttng26EventLayout;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout.Lttng27EventLayout;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout.Lttng28EventLayout;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout.Lttng29EventLayout;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout.LttngEventLayout;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout.PerfEventLayout;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.trace.TraceValidationStatus;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEventType;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTraceValidationStatus;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;

/**
 * This is the specification of CtfTmfTrace for use with LTTng 2.x kernel
 * traces.
 *
 * @author Alexandre Montplaisir
 */
public class LttngKernelTrace extends CtfTmfTrace implements IKernelTrace, ILttngTrace {

    /**
     * Supported Linux kernel tracers
     */
    private enum OriginTracer {
        LTTNG(LttngEventLayout.getInstance()),
        LTTNG26(Lttng26EventLayout.getInstance()),
        LTTNG27(Lttng27EventLayout.getInstance()),
        LTTNG28(Lttng28EventLayout.getInstance()),
        LTTNG29(Lttng29EventLayout.getInstance()),
        PERF(PerfEventLayout.getInstance());

        private final @NonNull IKernelAnalysisEventLayout fLayout;

        private OriginTracer(@NonNull IKernelAnalysisEventLayout layout) {
            fLayout = layout;
        }
    }

    /**
     * Event aspects available for all Lttng Kernel traces
     */
    private static final @NonNull Collection<ITmfEventAspect<?>> LTTNG_KERNEL_ASPECTS;

    static {
        ImmutableSet.Builder<ITmfEventAspect<?>> builder = ImmutableSet.builder();
        builder.addAll(CtfTmfTrace.CTF_ASPECTS);
        builder.addAll(KernelUtils.getKernelAspects());
        LTTNG_KERNEL_ASPECTS = builder.build();
    }

    /**
     * CTF metadata identifies trace type and tracer version pretty well, we are
     * quite confident in the inferred trace type.
     */
    private static final int CONFIDENCE = 100;

    /** The tracer which originated this trace */
    private OriginTracer fOriginTracer = null;

    /** Collection of aspects, default values */
    private @NonNull Collection<ITmfEventAspect<?>> fAspects = ImmutableSet.copyOf(LTTNG_KERNEL_ASPECTS);

    /**
     * Default constructor
     */
    public LttngKernelTrace() {
        super();
    }

    @Override
    public @NonNull IKernelAnalysisEventLayout getKernelEventLayout() {
        OriginTracer tracer = fOriginTracer;
        if (tracer == null) {
            throw new IllegalStateException("Cannot get the layout of a non-initialized trace!"); //$NON-NLS-1$
        }
        return tracer.fLayout;
    }

    @Override
    public void initTrace(IResource resource, String path,
            Class<? extends ITmfEvent> eventType) throws TmfTraceException {
        super.initTrace(resource, path, eventType);
        /*
         * Add aspects
         */
        fOriginTracer = getTracerFromEnv();
        ImmutableList.Builder<ITmfEventAspect<?>> builder = new Builder<>();
        builder.addAll(LTTNG_KERNEL_ASPECTS);
        builder.addAll(createCounterAspects(this));
        ContextPidAspect pidAspect = ContextPidAspect.getAspect(this);
        if (pidAspect != null) {
            builder.add(pidAspect);
        }
        ContextTidAspect tidAspect = ContextTidAspect.getAspect(this);
        if (tidAspect != null) {
            builder.add(tidAspect);
        }
        fAspects = builder.build();
    }

    /**
     * Identify which tracer generated a trace from its metadata.
     */
    private OriginTracer getTracerFromEnv() {
        String tracerName = CtfUtils.getTracerName(this);
        int tracerMajor = CtfUtils.getTracerMajorVersion(this);
        int tracerMinor = CtfUtils.getTracerMinorVersion(this);

        if ("perf".equals(tracerName)) { //$NON-NLS-1$
            return OriginTracer.PERF;

        } else if ("lttng-modules".equals(tracerName)) { //$NON-NLS-1$
            /* Look for specific versions of LTTng */
            if (tracerMajor >= 2) {
                if (tracerMinor >= 9) {
                    return OriginTracer.LTTNG29;
                } else if (tracerMinor >= 8) {
                    return OriginTracer.LTTNG28;
                } else if (tracerMinor >= 7) {
                    return OriginTracer.LTTNG27;
                } else if (tracerMinor >= 6) {
                    return OriginTracer.LTTNG26;
                }
            }
        }

        /* Use base LTTng layout as default */
        return OriginTracer.LTTNG;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation sets the confidence to 100 if the trace is a valid
     * CTF trace in the "kernel" domain.
     */
    @Override
    public IStatus validate(final IProject project, final String path) {
        IStatus status = super.validate(project, path);
        if (status instanceof CtfTraceValidationStatus) {
            Map<String, String> environment = ((CtfTraceValidationStatus) status).getEnvironment();
            /* Make sure the domain is "kernel" in the trace's env vars */
            String domain = environment.get("domain"); //$NON-NLS-1$
            if (domain == null || !domain.equals("\"kernel\"")) { //$NON-NLS-1$
                return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.LttngKernelTrace_DomainError);
            }
            return new TraceValidationStatus(CONFIDENCE, Activator.PLUGIN_ID);
        }
        return status;
    }

    @Override
    public Iterable<ITmfEventAspect<?>> getEventAspects() {
        return fAspects;
    }

    /*
     * Needs explicit @NonNull generic type annotation. Can be removed once this
     * class becomes @NonNullByDefault.
     */
    @Override
    public @NonNull Set<@NonNull CtfTmfEventType> getContainedEventTypes() {
        return super.getContainedEventTypes();
    }

}
