/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.IDataDrivenRuntimeObject;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfAnalysisModuleWithStateSystems;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.Iterables;

/**
 * Data provider factory for XY views
 *
 * @author Geneviève Bastien
 */
public class DataDrivenXYProviderFactory implements IDataDrivenRuntimeObject {

    private final List<DataDrivenOutputEntry> fEntries;
    private final Set<String> fAnalysisIds;

    /**
     * Constructor
     *
     * @param entries
     *            The list of entries
     * @param analysisIds
     *            The IDs of the analysis this view applies to
     */
    public DataDrivenXYProviderFactory(List<DataDrivenOutputEntry> entries, Set<String> analysisIds) {
        fEntries = entries;
        fAnalysisIds = analysisIds;
    }

    /**
     * Create an XY data provider for a trace
     *
     * @param trace
     *            The trace for which to create the data provider
     * @return The XY data provider or <code>null</code> if no such provider is
     *         available for that trace
     */
    public @Nullable ITmfTreeXYDataProvider<ITmfTreeDataModel> create(ITmfTrace trace) {

        Set<@NonNull ITmfAnalysisModuleWithStateSystems> stateSystemModules = new HashSet<>();
        List<ITmfStateSystem> sss = new ArrayList<>();
        if (fAnalysisIds.isEmpty()) {
            /*
             * No analysis specified, take all state system analysis modules
             */
            Iterables.addAll(stateSystemModules, TmfTraceUtils.getAnalysisModulesOfClass(trace, ITmfAnalysisModuleWithStateSystems.class));
        } else {
            for (String moduleId : fAnalysisIds) {
                // Get the module for the current trace only. The caller will
                // take care of
                // generating composite providers with experiments
                IAnalysisModule module = trace.getAnalysisModule(moduleId);
                if (module instanceof ITmfAnalysisModuleWithStateSystems) {
                    stateSystemModules.add((ITmfAnalysisModuleWithStateSystems) module);
                }
            }
        }

        for (ITmfAnalysisModuleWithStateSystems module : stateSystemModules) {
            if (module.schedule().isOK() && module.waitForInitialization()) {
                module.getStateSystems().forEach(sss::add);
            }
        }
        return (sss.isEmpty() ? null : create(trace, sss, fEntries, null));
    }

    /**
     * Create an XY data provider with state systems already available
     *
     * @param trace
     *            The trace for which to create the data provider
     * @param stateSystems
     *            The state systems to use
     * @param entries
     *            The list of entries
     * @param id
     *            The ID of the data provider to create
     * @return The XY data provider
     */
    public static ITmfTreeXYDataProvider<ITmfTreeDataModel> create(ITmfTrace trace, List<ITmfStateSystem> stateSystems, List<DataDrivenOutputEntry> entries, @Nullable String id) {
        return new DataDrivenXYDataProvider(trace, stateSystems, entries, id);
    }

}
