/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.latency;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.scatter.AbstractSegmentStoreScatterChartTreeViewer2;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.XmlLatencyViewInfo;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.views.xychart.TmfChartView;

/**
 * The scatter graph view for pattern latency
 *
 * @author Jean-Christian Kouame
 */
public class PatternScatterGraphView extends TmfChartView {

    /** The view's ID */
    public static final @NonNull String ID = "org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.scattergraph"; //$NON-NLS-1$

    private final XmlLatencyViewInfo fViewInfo = new XmlLatencyViewInfo(ID);

    private PatternScatterGraphViewer fViewer;

    /**
     * Constructor
     */
    public PatternScatterGraphView() {
        super(ID);
        this.addPartPropertyListener(event -> {
            if (event.getProperty().equals(TmfXmlStrings.XML_LATENCY_OUTPUT_DATA)) {
                Object newValue = event.getNewValue();
                if (newValue instanceof String) {
                    String data = (String) newValue;
                    fViewInfo.setViewData(data);
                    setPartName(fViewInfo.getLabel());
                    loadLatencyView();
                }
            }
        });
    }

    @Override
    public void createPartControl(@Nullable Composite parent) {
        String name = getViewSite().getSecondaryId();
        if (name != null) {
            /* must initialize view info before calling super */
            fViewInfo.setName(name);
        }
        super.createPartControl(parent);
        Display.getDefault().asyncExec(() -> setPartName(fViewInfo.getLabel()));
    }

    private void loadLatencyView() {
        if (fViewer != null) {
            fViewer.updateViewer(fViewInfo.getViewAnalysisId());
        }
    }

    @Override
    protected TmfXYChartViewer createChartViewer(Composite parent) {
        PatternScatterGraphViewer viewer = new PatternScatterGraphViewer(checkNotNull(parent), checkNotNull(Messages.PatternLatencyViews_ScatterGraphTitle), checkNotNull(Messages.PatternLatencyViews_ScatterGraphXLabel), checkNotNull(Messages.PatternLatencyViews_ScatterGraphYLabel));
        fViewer = viewer;
        loadLatencyView();
        return viewer;
    }

    @Override
    protected @NonNull TmfViewer createLeftChildViewer(@Nullable Composite parent) {
        String analysisId = fViewInfo.getViewAnalysisId();
        return new AbstractSegmentStoreScatterChartTreeViewer2(Objects.requireNonNull(parent), String.valueOf(analysisId)) {

            @Override
            protected @NonNull String getAnalysisId() {
                String viewAnalysisId = fViewInfo.getViewAnalysisId();
                return viewAnalysisId == null ? super.getAnalysisId() : viewAnalysisId;
            }

        };
    }

}
