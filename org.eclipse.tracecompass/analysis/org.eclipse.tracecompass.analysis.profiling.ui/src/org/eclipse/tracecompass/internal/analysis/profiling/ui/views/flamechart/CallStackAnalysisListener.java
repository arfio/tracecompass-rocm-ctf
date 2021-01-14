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

package org.eclipse.tracecompass.internal.analysis.profiling.ui.views.flamechart;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackAnalysis;
import org.eclipse.tracecompass.analysis.profiling.ui.views.flamechart.FlameChartView;
import org.eclipse.tracecompass.internal.analysis.profiling.ui.callgraph.CallGraphDensityView;
import org.eclipse.tracecompass.internal.analysis.profiling.ui.callgraph.statistics.CallGraphStatisticsView;
import org.eclipse.tracecompass.internal.analysis.profiling.ui.flamegraph.FlameGraphView;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.ITmfNewAnalysisModuleListener;
import org.eclipse.tracecompass.tmf.ui.analysis.TmfAnalysisViewOutput;

/**
 * Registers the {@link FlameChartView} to {@link CallStackAnalysis}. The
 * analysis being an abstract class, it is not possible to use the output
 * extension to add the view, but the listener fixes the issue.
 *
 * @author Geneviève Bastien
 */
public class CallStackAnalysisListener implements ITmfNewAnalysisModuleListener {

    @Override
    public void moduleCreated(@Nullable IAnalysisModule module) {
        if (module instanceof CallStackAnalysis) {
            module.registerOutput(new TmfAnalysisViewOutput(FlameChartView.ID));
            module.registerOutput(new TmfAnalysisViewOutput(FlameGraphView.ID));
            module.registerOutput(new TmfAnalysisViewOutput(CallGraphDensityView.ID));
            module.registerOutput(new TmfAnalysisViewOutput(CallGraphStatisticsView.ID));
        }
    }

}
