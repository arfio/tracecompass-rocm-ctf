/*******************************************************************************
 * Copyright (c) 2017, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.viewers.xycharts;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.ITmfChartTimeProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.ITimeDataProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.Utils.TimeFormat;

/**
 * Tmf Chart data provider wrapper to comply with Time data provider API
 *
 * @author Matthew Khouzam
 * @deprecated use {@link org.eclipse.tracecompass.internal.tmf.ui.viewers.xychart.TmfXYChartTimeAdapter}
 */
@Deprecated
public final class TmfXYChartTimeAdapter implements ITimeDataProvider {

    private final TmfXYChartViewer fTimeProvider;
    private TimeFormat fTimeFormat;

    /**
     * Constructor, requires a {@link ITmfChartTimeProvider}
     *
     * @param provider
     *            the provider to wrap
     */
    public TmfXYChartTimeAdapter(@NonNull TmfXYChartViewer provider) {
        fTimeProvider = provider;
    }

    @Override
    public long getBeginTime() {
        return fTimeProvider.getStartTime();
    }

    @Override
    public long getEndTime() {
        return fTimeProvider.getEndTime();
    }

    @Override
    public long getMinTimeInterval() {
        return 1L;
    }

    @Override
    public int getNameSpace() {
        // charts have no namespace
        return 0;
    }

    @Override
    public long getSelectionBegin() {
        return fTimeProvider.getSelectionBeginTime();
    }

    @Override
    public long getSelectionEnd() {
        return fTimeProvider.getSelectionEndTime();
    }

    @Override
    public long getTime0() {
        return fTimeProvider.getWindowStartTime();
    }

    @Override
    public long getTime1() {
        return fTimeProvider.getWindowEndTime();
    }

    @Override
    public void setSelectionRangeNotify(long beginTime, long endTime, boolean ensureVisible) {
        // Do nothing
    }

    @Override
    public void setSelectionRange(long beginTime, long endTime, boolean ensureVisible) {
        // Do nothing
    }

    @Override
    public long getMinTime() {
        return fTimeProvider.getStartTime();
    }

    @Override
    public long getMaxTime() {
        return fTimeProvider.getEndTime();
    }

    @Override
    public TimeFormat getTimeFormat() {
        return fTimeFormat;
    }

    @Override
    public int getTimeSpace() {
        return getAxisWidth();
    }

    /**
     * Get the width of the axis
     *
     * @return the width of the axis
     */
    public int getAxisWidth() {
        return fTimeProvider.getSwtChart().getPlotArea().getBounds().width;
    }

    // -------------------------------------------------------------------------
    // Override rest if need be
    // -------------------------------------------------------------------------

    @Override
    public void setStartFinishTimeNotify(long time0, long time1) {
        fTimeProvider.updateWindow(time0, time1);
    }

    @Override
    public void setStartFinishTime(long time0, long time1) {
        // Do nothing
    }

    @Override
    public void notifyStartFinishTime() {
        // Do nothing
    }

    @Override
    public void setSelectedTimeNotify(long time, boolean ensureVisible) {
        // Do nothing
    }

    @Override
    public void setSelectedTime(long time, boolean ensureVisible) {
        // Do nothing
    }

    @Override
    public void setNameSpace(int width) {
        // Do nothing
    }

    /**
     * Set the time format.
     *
     * @param timeFormat
     *            the time format
     */
    public void setTimeFormat(org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils.TimeFormat timeFormat) {
        fTimeFormat = TimeFormat.convert(timeFormat);
    }
}
