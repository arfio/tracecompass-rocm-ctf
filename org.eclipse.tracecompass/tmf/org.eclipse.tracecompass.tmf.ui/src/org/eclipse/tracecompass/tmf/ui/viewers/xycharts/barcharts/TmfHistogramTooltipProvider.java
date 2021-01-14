/**********************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.tmf.ui.viewers.xycharts.barcharts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.ITmfChartTimeProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfBaseProvider;
import org.swtchart.IAxis;
import org.swtchart.ISeries;

/**
 * Tool tip provider for TMF bar chart viewer. It displays the y value of
 * position x as well as it highlights the bar of the x position.
 * It only considers the first series of the chart.
 *
 * @author Bernd Hufmann
 * @deprecated use {@link org.eclipse.tracecompass.tmf.ui.viewers.xychart.barchart.TmfHistogramTooltipProvider}
 */
@Deprecated
public class TmfHistogramTooltipProvider extends TmfBaseProvider implements MouseTrackListener, MouseMoveListener, PaintListener {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /** X coordinate for highlighting */
    private int fHighlightX;
    /** y coordinate for highlighting */
    private int fHighlightY;
    /** Flag to do highlighting or not */
    private boolean fIsHighlight;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor for a tool tip provider.
     *
     * @param tmfChartViewer
     *                  - the parent chart viewer
     */
    public TmfHistogramTooltipProvider(ITmfChartTimeProvider tmfChartViewer) {
        super(tmfChartViewer);
        register();
    }

    // ------------------------------------------------------------------------
    // TmfBaseProvider
    // ------------------------------------------------------------------------
    @Override
    public void register() {
        getChart().getPlotArea().addMouseTrackListener(this);
        getChart().getPlotArea().addMouseMoveListener(this);
        getChart().getPlotArea().addPaintListener(this);
    }

    @Override
    public void deregister() {
        if ((getChartViewer().getControl() != null) && !getChartViewer().getControl().isDisposed()) {
            getChart().getPlotArea().removeMouseTrackListener(this);
            getChart().getPlotArea().removeMouseMoveListener(this);
            getChart().getPlotArea().removePaintListener(this);
        }
    }

    @Override
    public void refresh() {
        // nothing to do
    }

    // ------------------------------------------------------------------------
    // MouseTrackListener
    // ------------------------------------------------------------------------
    @Override
    public void mouseEnter(MouseEvent e) {
        // Do nothing
    }

    @Override
    public void mouseExit(MouseEvent e) {
        // Do nothing
    }

    @Override
    public void mouseHover(MouseEvent e) {
        if (getChartViewer().getWindowDuration() != 0) {
            IAxis xAxis = getChart().getAxisSet().getXAxis(0);
            IAxis yAxis = getChart().getAxisSet().getYAxis(0);

            double xCoordinate = xAxis.getDataCoordinate(e.x);

            ISeries[] series = getChart().getSeriesSet().getSeries();

            if ((xCoordinate < 0) || (series.length == 0)) {
                return;
            }

            double y = 0.0;
            double rangeStart = 0.0;
            double rangeEnd = 0.0;

            // Consider first series only
            double[] xS = series[0].getXSeries();
            double[] yS = series[0].getYSeries();

            if ((xS == null) || (yS == null)) {
                return;
            }

            for (int i = 0; i < xS.length - 1; i++) {
                int pixel = xAxis.getPixelCoordinate(xS[i]);
                if (pixel <= e.x) {
                    rangeStart = xS[i];
                    rangeEnd = (long) xS[i + 1];
                    if (xCoordinate >= rangeStart) {
                        y = yS[i + 1];
                    } else {
                        y = yS[i];
                    }
                }
            }

            ITmfChartTimeProvider viewer = getChartViewer();

            /* set tooltip of closest data point */
            StringBuffer buffer = new StringBuffer();
            buffer.append("Range=["); //$NON-NLS-1$
            buffer.append(TmfTimestamp.fromNanos((long) rangeStart + viewer.getTimeOffset()).toString());
            buffer.append(',');
            buffer.append(TmfTimestamp.fromNanos((long) rangeEnd + viewer.getTimeOffset()).toString());
            buffer.append("]\n"); //$NON-NLS-1$
            buffer.append("y="); //$NON-NLS-1$
            buffer.append((long) y);
            getChart().getPlotArea().setToolTipText(buffer.toString());

            fHighlightX = e.x;
            fHighlightY = yAxis.getPixelCoordinate(y);
            fIsHighlight = true;
            getChart().redraw();
        }
    }

    // ------------------------------------------------------------------------
    // MouseMoveListener
    // ------------------------------------------------------------------------
    @Override
    public void mouseMove(MouseEvent e) {
        fIsHighlight = false;
        getChart().redraw();
    }

    // ------------------------------------------------------------------------
    // PaintListener
    // ------------------------------------------------------------------------
    @Override
    public void paintControl(PaintEvent e) {
        if (fIsHighlight) {
            e.gc.setBackground(Display.getDefault().getSystemColor(
                    SWT.COLOR_RED));
            e.gc.setAlpha(128);

            e.gc.fillOval(fHighlightX - 5, fHighlightY - 5, 10, 10);
        }
    }

}