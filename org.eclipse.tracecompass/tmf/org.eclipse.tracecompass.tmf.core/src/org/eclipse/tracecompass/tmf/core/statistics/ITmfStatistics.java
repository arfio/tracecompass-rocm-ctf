/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API
 ******************************************************************************/

package org.eclipse.tracecompass.tmf.core.statistics;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;

/**
 * Provider for statistics, which is assigned to a trace. This can be used to
 * populate views like the Statistics View or the Histogram.
 *
 * As a guideline, since any trace type can use this interface, all timestamps
 * should be normalized to nanoseconds when using these methods
 * ({@link ITmfTimestamp#NANOSECOND_SCALE}).
 *
 * @author Alexandre Montplaisir
 */
public interface ITmfStatistics {

    /**
     * Run a histogram query on the statistics back-end. This means, return the
     * total number of events in a series of 'timeRquested'. As its name implies,
     * this is typically used to fill the histogram data (where each range
     * represents one pixel on the histogram).
     *
     * This method will block the caller until the results are returned, so it
     * should not be called from a signal handler or from the UI thread.
     *
     * @param timeRequested
     *            Array of requested times
     * @return The array representing the number of events found in each sub-range.
     * @since 4.0
     */
    List<@NonNull Long> histogramQuery(long[] timeRequested);

    /**
     * Return the total number of events in the trace.
     *
     * @return The total number of events
     */
    long getEventsTotal();

    /**
     * Return a Map of the total events in the trace, per event type. The event
     * type should come from ITmfEvent.getType().getName().
     *
     * @return The map of <event_type, count>, for the whole trace
     */
    Map<@NonNull String, @NonNull Long> getEventTypesTotal();

    /**
     * Retrieve the number of events in the trace in a given time interval.
     *
     * @param start
     *            Start time of the time range
     * @param end
     *            End time of the time range
     * @return The number of events found
     */
    long getEventsInRange(long start, long end);

    /**
     * Retrieve the number of events in the trace, per event type, in a given
     * time interval.
     *
     * @param start
     *            Start time of the time range
     * @param end
     *            End time of the time range
     * @return The map of <event_type, count>, for the given time range
     */
    Map<String, Long> getEventTypesInRange(long start, long end);

    /**
     * Notify the statistics back-end that the trace is being closed, so it
     * should dispose itself as appropriate (release file descriptors, etc.)
     */
    void dispose();
}
