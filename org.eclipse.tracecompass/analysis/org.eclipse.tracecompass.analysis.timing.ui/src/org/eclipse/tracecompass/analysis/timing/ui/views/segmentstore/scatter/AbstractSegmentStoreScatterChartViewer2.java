/******************************************************************************
 * Copyright (c) 2017, 2020 Ericsson and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.scatter;

import java.text.Format;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.common.core.format.SubSecondTimeWithUnitFormat;
import org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore.SegmentStoreScatterDataProvider;
import org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.xyscatter.SegmentStoreScatterGraphTooltipProvider;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfXYDataProvider;
import org.eclipse.tracecompass.tmf.core.presentation.IYAppearance;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.linechart.TmfFilteredXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.linechart.TmfXYChartSettings;

/**
 * Displays the segment store provider data in a SWT scatter chart
 *
 * @author Yonni Chen
 * @since 4.1
 */
public class AbstractSegmentStoreScatterChartViewer2 extends TmfFilteredXYChartViewer {

    private static final Format FORMAT =SubSecondTimeWithUnitFormat.getInstance();
    private static final int DEFAULT_SERIES_WIDTH = 1;
    private String fAnalysisId;

    /**
     * Constructor
     *
     * @param parent
     *            parent composite
     * @param settings
     *            See {@link TmfXYChartSettings} to know what it contains
     */
    public AbstractSegmentStoreScatterChartViewer2(Composite parent, TmfXYChartSettings settings) {
        this(parent, settings, ""); //$NON-NLS-1$
    }

    /**
     * Constructor
     *
     * @param parent
     *            parent composite
     * @param settings
     *            See {@link TmfXYChartSettings} to know what it contains
     * @param analysisId
     *            The ID of the analysis to show in this viewer
     */
    public AbstractSegmentStoreScatterChartViewer2(Composite parent, TmfXYChartSettings settings, String analysisId) {
        super(parent, settings, SegmentStoreScatterDataProvider.ID);
        fAnalysisId = analysisId;
        setTooltipProvider(new SegmentStoreScatterGraphTooltipProvider(this));
        getSwtChart().getLegend().setVisible(false);
        getSwtChart().getAxisSet().getYAxis(0).getTick().setFormat(FORMAT);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Get the analysis ID to show in the viewer
     *
     * @return The analysis ID
     */
    protected String getAnalysisId() {
        return fAnalysisId;
    }

    @Override
    protected @Nullable ITmfXYDataProvider initializeDataProvider(ITmfTrace trace) {
        String analysisId = getAnalysisId();

        if (analysisId.isEmpty()) {
            /* Should not happen anymore since legacy code,
             * that get the analysis ID of the segment store, is removed */
            return null;
        }
        return DataProviderManager.getInstance().getDataProvider(trace, SegmentStoreScatterDataProvider.ID + ':' + analysisId, ITmfTreeXYDataProvider.class);
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * @param signal
     *            Signal received when a different trace is selected
     */
    @Override
    @TmfSignalHandler
    public void traceSelected(@Nullable TmfTraceSelectedSignal signal) {
        super.traceSelected(signal);
        if (getTrace() != null) {
            final TmfTimeRange timeRange = TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange();
            setWindowRange(timeRange.getStartTime().toNanos(), timeRange.getEndTime().toNanos());
        }
    }

    /**
     * @param signal
     *            Signal received when trace is opened
     */
    @Override
    @TmfSignalHandler
    public void traceOpened(@Nullable TmfTraceOpenedSignal signal) {
        super.traceOpened(signal);
        if (getTrace() != null) {
            final TmfTimeRange timeRange = TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange();
            setWindowRange(timeRange.getStartTime().toNanos(), timeRange.getEndTime().toNanos());
        }
    }

    @Override
    public OutputElementStyle getSeriesStyle(@NonNull Long seriesId) {
        return getPresentationProvider().getSeriesStyle(seriesId, IYAppearance.Type.SCATTER, DEFAULT_SERIES_WIDTH);
    }

    /**
     * @param signal
     *            Signal received when last opened trace is closed
     */
    @Override
    @TmfSignalHandler
    public void traceClosed(@Nullable TmfTraceClosedSignal signal) {
        super.traceClosed(signal);

        // Check if there is no more opened trace
        if (signal != null && TmfTraceManager.getInstance().getActiveTrace() == null) {
            clearContent();
        }
        refresh();
    }
}
