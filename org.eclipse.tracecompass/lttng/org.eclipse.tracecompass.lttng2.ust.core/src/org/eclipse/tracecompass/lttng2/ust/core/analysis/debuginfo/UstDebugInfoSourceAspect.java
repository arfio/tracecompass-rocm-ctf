/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.ust.core.analysis.debuginfo;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.io.File;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.debuginfo.FileOffsetMapper;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.lookup.TmfCallsite;

/**
 * Event aspect of UST traces to generate a {@link TmfCallsite} using the debug
 * info analysis and the IP (instruction pointer) context.
 *
 * @author Alexandre Montplaisir
 * @since 3.0
 */
public class UstDebugInfoSourceAspect implements ITmfEventAspect<TmfCallsite> {

    /** Singleton instance */
    public static final UstDebugInfoSourceAspect INSTANCE = new UstDebugInfoSourceAspect();

    private UstDebugInfoSourceAspect() {}

    @Override
    public String getName() {
        return nullToEmptyString(Messages.UstDebugInfoAnalysis_SourceAspectName);
    }

    @Override
    public String getHelpText() {
        return nullToEmptyString(Messages.UstDebugInfoAnalysis_SourceAspectHelpText);
    }

    /**
     * @since 2.1
     */
    @Override
    public @Nullable TmfCallsite resolve(ITmfEvent event) {
        /* This aspect only supports UST traces */
        if (!(event.getTrace() instanceof LttngUstTrace)) {
            return null;
        }
        LttngUstTrace trace = (LttngUstTrace) event.getTrace();

        /*
         * Resolve the binary callsite first, from there we can use the file's
         * debug information if it is present.
         */
        BinaryCallsite bc = UstDebugInfoBinaryAspect.INSTANCE.resolve(event);
        if (bc == null) {
            return null;
        }

        TmfCallsite callsite = FileOffsetMapper.getCallsiteFromOffset(
                new File(bc.getBinaryFilePath()),
                bc.getBuildId(),
                bc.getOffset());
        if (callsite == null) {
            return null;
        }

        /*
         * Apply the path prefix again, this time on the path given from
         * addr2line. If applicable.
         */
        String pathPrefix = trace.getSymbolProviderConfig().getActualRootDirPath();
        if (pathPrefix.isEmpty()) {
            return callsite;
        }

        String fullFileName = (pathPrefix + callsite.getFileName());
        return new TmfCallsite(fullFileName, callsite.getLineNo());
    }
}
