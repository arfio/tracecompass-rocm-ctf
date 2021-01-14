/*******************************************************************************
 * Copyright (c) 2020 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.incubator.internal.rocm.ctf.core.analysis;


import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.incubator.callstack.core.instrumented.statesystem.InstrumentedCallStackAnalysis;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAbstractAnalysisRequirement;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;

/**
 * ROCm API call stack analysis
 *
 * @author Arnaud Fiorini
 */
public class RocmCtfCallStackAnalysis extends InstrumentedCallStackAnalysis {
    /**
     * Call stack analysis ID
     */
    public static final @NonNull String ID = "org.eclipse.tracecompass.incubator.rocm.ctf.core.analysis.callstack"; //$NON-NLS-1$

//    private static final @NonNull List<String[]> PATTERNS = ImmutableList.of(
//            new String[] { RocmCtfCallStackStateProvider.PROCESSES, "*" }, //$NON-NLS-1$
//            new String[] { "*" }, //$NON-NLS-1$
//            new String[] { "*" }, //$NON-NLS-1$
//            new String[] { "*" } //$NON-NLS-1$
//    );
//
//    @Override
//    protected List<String[]> getPatterns() {
//        return PATTERNS;
//    }

    @Override
    protected ITmfStateProvider createStateProvider() {
        return new RocmCtfCallStackStateProvider(Objects.requireNonNull(getTrace()));
    }

    @Override
    public Iterable<TmfAbstractAnalysisRequirement> getAnalysisRequirements() {
        return Collections.emptyList();
    }

    @Override
    protected @NonNull Collection<@NonNull Integer> getEdgeQuarks() {
        ITmfStateSystem ss = getStateSystem();
        if (ss == null) {
            return Collections.emptyList();
        }
        int edgeQuark = ss.optQuarkAbsolute(RocmCtfCallStackStateProvider.EDGES_LANE);
        if (edgeQuark == ITmfStateSystem.INVALID_ATTRIBUTE) {
            return Collections.emptyList();
        }
        return ss.getSubAttributes(edgeQuark, false);
    }

}
