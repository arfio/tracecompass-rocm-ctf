/**********************************************************************
 * Copyright (c) 2013, 2014, 2020 Ericsson
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
package org.eclipse.tracecompass.tmf.ui.viewers.xychart;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swtchart.ICustomPaintListener;

/**
 * Class for providing selection of ranges with the left mouse button. It also
 * notifies the viewer about a change of selection.
 *
 * @author Bernd Hufmann
 * @since 6.0
 */
public class TmfMouseSelectionProvider extends TmfBaseProvider implements MouseListener, MouseMoveListener, ICustomPaintListener {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /** Cached start time */
    private long fBeginTime;
    /** Cached end time */
    private long fEndTime;
    /** Cached cursor time*/
    private long fCursorTime;
    /** Flag indicating that an update is ongoing */
    private boolean fIsInternalUpdate;
    /** Flag indicating that the begin marker is dragged */
    private boolean fDragBeginMarker;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor
     *
     * @param tmfChartViewer
     *            The chart viewer reference.
     */
    public TmfMouseSelectionProvider(ITmfChartTimeProvider tmfChartViewer) {
        super(tmfChartViewer);
    }

    // ------------------------------------------------------------------------
    // MouseListener
    // ------------------------------------------------------------------------
    @Override
    public void mouseDoubleClick(MouseEvent e) {
        // Do nothing
    }

    @Override
    public void mouseDown(MouseEvent e) {
        if ((getChartViewer().getWindowDuration() != 0) && (e.button == 1)) {
            if ((e.stateMask & SWT.CTRL) != 0) {
                return;
            }
            fDragBeginMarker = false;
            IAxis xAxis = getXAxis();
            if ((e.stateMask & SWT.SHIFT) != SWT.SHIFT) {
                fBeginTime = limitXDataCoordinate(xAxis.getDataCoordinate(e.x));
                fEndTime = fBeginTime;
            } else {
                long selectionBegin = fBeginTime;
                long selectionEnd = fEndTime;
                long time = limitXDataCoordinate(xAxis.getDataCoordinate(e.x));
                if (Math.abs(time - selectionBegin) < Math.abs(time - selectionEnd)) {
                    fDragBeginMarker = true;
                    fBeginTime = time;
                    fEndTime = selectionEnd;
                } else {
                    fBeginTime = selectionBegin;
                    fEndTime = time;
                }
            }
            fIsInternalUpdate = true;
        }
    }

    @Override
    public void mouseUp(MouseEvent e) {
        if (fIsInternalUpdate) {
            ITmfChartTimeProvider viewer = getChartViewer();
            viewer.updateSelectionRange(fBeginTime + viewer.getTimeOffset(), fEndTime + viewer.getTimeOffset());

            if (viewer instanceof TmfXYChartViewer) {
                TmfXYChartViewer xyChartViewer = (TmfXYChartViewer) viewer;
                xyChartViewer.updateStatusLine(fBeginTime, fEndTime, fCursorTime);
            }

            fIsInternalUpdate = false;
            redraw();
        }
    }

    // ------------------------------------------------------------------------
    // MouseMoveListener
    // ------------------------------------------------------------------------
    @Override
    public void mouseMove(MouseEvent e) {
        IAxis xAxis = getXAxis();
        fCursorTime = limitXDataCoordinate(xAxis.getDataCoordinate(e.x));

        if (fIsInternalUpdate) {
            if (fDragBeginMarker) {
                fBeginTime = limitXDataCoordinate(xAxis.getDataCoordinate(e.x));
            } else {
                fEndTime = limitXDataCoordinate(xAxis.getDataCoordinate(e.x));
            }
            redraw();
        }

        ITmfChartTimeProvider viewer = getChartViewer();
        if (viewer instanceof TmfXYChartViewer) {
            TmfXYChartViewer xyChartViewer = (TmfXYChartViewer) viewer;
            xyChartViewer.updateStatusLine(fBeginTime, fEndTime, fCursorTime);
        }
    }

    // ------------------------------------------------------------------------
    // ICustomPaintListener
    // ------------------------------------------------------------------------
    @Override
    public void paintControl(PaintEvent e) {
        ITmfChartTimeProvider viewer = getChartViewer();

        if (!fIsInternalUpdate) {
            fBeginTime = viewer.getSelectionBeginTime() - viewer.getTimeOffset();
            fEndTime = viewer.getSelectionEndTime() - viewer.getTimeOffset();
        }
        long windowStartTime = viewer.getWindowStartTime() - viewer.getTimeOffset();
        long windowEndTime = viewer.getWindowEndTime() - viewer.getTimeOffset();

        IAxis xAxis = getXAxis();
        e.gc.setBackground(TmfXYChartViewer.getDisplay().getSystemColor(SWT.COLOR_BLUE));
        e.gc.setForeground(TmfXYChartViewer.getDisplay().getSystemColor(SWT.COLOR_BLUE));
        e.gc.setLineStyle(SWT.LINE_SOLID);
        if ((fBeginTime >= windowStartTime) && (fBeginTime <= windowEndTime)) {
            int beginX = xAxis.getPixelCoordinate(fBeginTime);
            e.gc.drawLine(beginX, 0, beginX, e.height);
        }

        if ((fEndTime >= windowStartTime) && (fEndTime <= windowEndTime) && (fBeginTime != fEndTime)) {
            int endX = xAxis.getPixelCoordinate(fEndTime);
            e.gc.drawLine(endX, 0, endX, e.height);
        }
        e.gc.setAlpha(64);
        if (Math.abs(fEndTime - fBeginTime) > 1) {
            e.gc.setBackground(TmfXYChartViewer.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
            int beginX = xAxis.getPixelCoordinate(fBeginTime);
            int endX = xAxis.getPixelCoordinate(fEndTime);
            if (fEndTime > fBeginTime) {
                e.gc.fillRectangle(beginX + 1, 0, endX - beginX - 1, e.height);
            } else {
                e.gc.fillRectangle(endX + 1, 0, beginX - endX - 1, e.height);
            }
        }
    }

    @Override
    public boolean drawBehindSeries() {
        return false;
    }
}
