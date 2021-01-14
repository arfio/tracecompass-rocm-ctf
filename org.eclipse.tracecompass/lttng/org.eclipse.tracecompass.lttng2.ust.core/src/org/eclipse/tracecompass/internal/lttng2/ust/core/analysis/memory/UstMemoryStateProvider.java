/**********************************************************************
 * Copyright (c) 2014, 2016 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Geneviève Bastien - Memory is per thread and only total is kept
 **********************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.memory;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.event.aspect.LinuxTidAspect;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.lttng2.ust.core.trace.layout.ILttngUstEventLayout;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.ImmutableMap;

/**
 * State provider to track the memory of the threads using the UST libc wrapper
 * memory events.
 *
 * Attribute tree:
 *
 * <pre>
 * |- <TID number>
 * |  |- UST_MEMORY_MEMORY_ATTRIBUTE -> Memory Usage
 * |  |- UST_MEMORY_PROCNAME_ATTRIBUTE -> Process name
 * </pre>
 *
 * @author Matthew Khouzam
 * @author Geneviève Bastien
 */
public class UstMemoryStateProvider extends AbstractTmfStateProvider {

    /* Version of this state provider */
    private static final int VERSION = 2;

    private static final Long MINUS_ONE = Long.valueOf(-1);
    private static final Long ZERO = Long.valueOf(0);

    private static final int MALLOC_INDEX = 1;
    private static final int FREE_INDEX = 2;
    private static final int CALLOC_INDEX = 3;
    private static final int REALLOC_INDEX = 4;
    private static final int MEMALIGN_INDEX = 5;
    private static final int POSIX_MEMALIGN_INDEX = 6;

    /** Map of a pointer to a memory zone to the size of the memory */
    private final Map<Long, MemoryAllocation> fMemory = new HashMap<>();

    private final @NonNull ILttngUstEventLayout fLayout;
    private final @NonNull Map<String, Integer> fEventNames;
    private final @Nullable UstMemoryAnalysisModule fAnalysis;

    /**
     * Describe a memory allocation in details: timestamp, tid, and size
     */
    public static class MemoryAllocation {

        private final long fTs;
        private final Long fTid;
        private final Long fSize;

        /**
         * Constructor
         *
         * @param ts
         *            The timestamp
         * @param tid
         *            The thread ID
         * @param size
         *            The size of the allocation
         */
        public MemoryAllocation(long ts, Long tid, Long size) {
            fTs = ts;
            fTid = tid;
            fSize = size;
        }

        /**
         * Get the timestamp of this memory allocation
         *
         * @return The timestamp at which the allocation happened
         */
        public long getTs() {
            return fTs;
        }

        /**
         * Get the ID of the thread doing the allocation
         *
         * @return The thread ID
         */
        public Long getTid() {
            return fTid;
        }

        /**
         * Get the size of this memory allocation
         *
         * @return The size of allocation
         */
        public Long getSize() {
            return fSize;
        }
    }

    /**
     * Constructor
     *
     * @param trace
     *            trace
     * @param baseAnalysis
     *            The analysis that created the state provider
     */
    public UstMemoryStateProvider(@NonNull ITmfTrace trace, @Nullable UstMemoryAnalysisModule baseAnalysis) {
        super(trace, "Ust:Memory"); //$NON-NLS-1$
        if (!(trace instanceof LttngUstTrace)) {
            fLayout = ILttngUstEventLayout.DEFAULT_LAYOUT;
        } else {
            fLayout = ((LttngUstTrace) trace).getEventLayout();
        }
        fEventNames = buildEventNames(fLayout);
        fAnalysis = baseAnalysis;
    }

    private static @NonNull Map<String, Integer> buildEventNames(ILttngUstEventLayout layout) {
        ImmutableMap.Builder<String, Integer> builder = ImmutableMap.builder();
        builder.put(layout.eventLibcMalloc(), MALLOC_INDEX);
        builder.put(layout.eventLibcFree(), FREE_INDEX);
        builder.put(layout.eventLibcCalloc(), CALLOC_INDEX);
        builder.put(layout.eventLibcRealloc(), REALLOC_INDEX);
        builder.put(layout.eventLibcMemalign(), MEMALIGN_INDEX);
        builder.put(layout.eventLibcPosixMemalign(), POSIX_MEMALIGN_INDEX);
        return builder.build();
    }

    @Override
    protected void eventHandle(ITmfEvent event) {
        String name = event.getName();
        Integer index = fEventNames.get(name);
        int intIndex = (index == null ? -1 : index.intValue());

        switch (intIndex) {
        case MALLOC_INDEX: {
            Long ptr = (Long) event.getContent().getField(fLayout.fieldPtr()).getValue();
            if (ZERO.equals(ptr)) {
                return;
            }
            Long size = (Long) event.getContent().getField(fLayout.fieldSize()).getValue();
            setMem(event, ptr, size);
        }
            break;
        case FREE_INDEX: {
            Long ptr = (Long) event.getContent().getField(fLayout.fieldPtr()).getValue();
            if (ZERO.equals(ptr)) {
                return;
            }
            setMem(event, ptr, ZERO);
        }
            break;
        case CALLOC_INDEX: {
            Long ptr = (Long) event.getContent().getField(fLayout.fieldPtr()).getValue();
            if (ZERO.equals(ptr)) {
                return;
            }
            Long nmemb = (Long) event.getContent().getField(fLayout.fieldNmemb()).getValue();
            Long size = (Long) event.getContent().getField(fLayout.fieldSize()).getValue();
            setMem(event, ptr, size * nmemb);
        }
            break;
        case REALLOC_INDEX: {
            Long ptr = (Long) event.getContent().getField(fLayout.fieldPtr()).getValue();
            if (ZERO.equals(ptr)) {
                return;
            }
            Long newPtr = (Long) event.getContent().getField(fLayout.fieldInPtr()).getValue();
            Long size = (Long) event.getContent().getField(fLayout.fieldSize()).getValue();
            setMem(event, ptr, ZERO);
            setMem(event, newPtr, size);
        }
            break;
        case MEMALIGN_INDEX: {
            Long ptr = (Long) event.getContent().getField(fLayout.fieldPtr()).getValue();
            if (ZERO.equals(ptr)) {
                return;
            }
            Long size = (Long) event.getContent().getField(fLayout.fieldSize()).getValue();
            setMem(event, ptr, size);
        }
            break;
        case POSIX_MEMALIGN_INDEX: {
            Long ptr = (Long) event.getContent().getField(fLayout.fieldOutPtr()).getValue();
            if (ZERO.equals(ptr)) {
                return;
            }
            Long size = (Long) event.getContent().getField(fLayout.fieldSize()).getValue();
            setMem(event, ptr, size);
        }
            break;
        default:
            /* Ignore other event types */
            break;
        }

    }

    @Override
    public ITmfStateProvider getNewInstance() {
        return new UstMemoryStateProvider(getTrace(), fAnalysis);
    }

    @Override
    public LttngUstTrace getTrace() {
        return (LttngUstTrace) super.getTrace();
    }

    @Override
    public int getVersion() {
        return VERSION;
    }

    private static Long getVtid(ITmfEvent event) {
        /* We checked earlier that the "vtid" context is present */
        Integer tid = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), LinuxTidAspect.class, event);
        if (tid == null) {
            return MINUS_ONE;
        }
        return tid.longValue();
    }

    private @Nullable String getProcname(ITmfEvent event) {
        ITmfEventField field = event.getContent().getField(fLayout.contextProcname());
        if (field == null) {
            return null;
        }
        return (String) field.getValue();
    }

    private void setMem(ITmfEvent event, Long ptr, Long size) {
        ITmfStateSystemBuilder ss = checkNotNull(getStateSystemBuilder());
        long ts = event.getTimestamp().toNanos();
        Long tid = getVtid(event);

        Long memoryDiff = size;
        /* Size is 0, it means it was deleted */
        if (ZERO.equals(size)) {
            MemoryAllocation memAlloc = fMemory.remove(ptr);
            if (memAlloc == null) {
                return;
            }
            memoryDiff = -(memAlloc.getSize());
        } else {
            fMemory.put(ptr, new MemoryAllocation(ts, tid, size));
        }
        try {
            int tidQuark = ss.getQuarkAbsoluteAndAdd(tid.toString());
            int tidMemQuark = ss.getQuarkRelativeAndAdd(tidQuark, UstMemoryStrings.UST_MEMORY_MEMORY_ATTRIBUTE);

            ITmfStateValue prevMem = ss.queryOngoingState(tidMemQuark);
            /* First time we set this value */
            if (prevMem.isNull()) {
                String procName = getProcname(event);
                /*
                 * No tid/procname for the event for the event, added to a
                 * 'others' thread
                 */
                if (tid.equals(MINUS_ONE)) {
                    procName = UstMemoryStrings.OTHERS;
                }
                if (procName != null) {
                    int procNameQuark = ss.getQuarkRelativeAndAdd(tidQuark, UstMemoryStrings.UST_MEMORY_PROCNAME_ATTRIBUTE);
                    ss.modifyAttribute(ts, procName, procNameQuark);
                }
                prevMem = TmfStateValue.newValueLong(0);
            }

            long prevMemValue = prevMem.unboxLong();
            prevMemValue += memoryDiff.longValue();
            ss.modifyAttribute(ts, prevMemValue, tidMemQuark);
        } catch (TimeRangeException | StateValueTypeException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void done() {
        UstMemoryAnalysisModule analysis = fAnalysis;
        if (analysis != null) {
            analysis.setPotentialLeaks(fMemory);
        }
    }

}
