/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.jdt.annotation.NonNull;


/**
 * An iterator for time events. Events from the zoomed event list override any
 * events from the underlying event list. Gaps in the zoomed event list will not
 * allow the underlying events to be included in the iterator, e.g. no
 * underlying event will be included between the start of the first zoomed event
 * and the end of the last zoomed event.
 *
 * <pre>
 * example:
 * zoomed list    [D][E][F]       [G]
 * event list  [      A      ] [   B   ][C]
 * iterator    [A][D][E][F]       [G][B][C]
 * </pre>
 */
public class EventIterator implements Iterator<@NonNull ITimeEvent> {

    private final long fStartTime;
    private final long fEndTime;
    private List<ITimeEvent> fEventList;
    private List<ITimeEvent> fZoomedEventList;
    private long fZoomedStartTime;
    private long fZoomedEndTime;
    private int fIndex = 0;
    private int fZoomedIndex= 0;
    private ITimeEvent fNext = null;
    private ITimeEvent fSplitNext = null;
    private ITimeEvent fZoomedNext = null;

    /**
     * Basic constructor, with start time and end times equal to the lowest and
     * highest values possible, respectively.
     *
     * @param eventList
     *            The list on which this iterator will iterate
     * @param zoomedEventList
     *            The "zoomed" list
     */
    public EventIterator(List<ITimeEvent> eventList, List<ITimeEvent> zoomedEventList) {
        this(eventList, zoomedEventList, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    /**
     * Complete constructor, where we specify start and end times.
     *
     * @param eventList
     *            The list on which this iterator will iterate
     * @param zoomedEventList
     *            The "zoomed" list
     * @param startTime
     *            The start time
     * @param endTime
     *            The end time
     */
    public EventIterator(List<ITimeEvent> eventList,
            List<ITimeEvent> zoomedEventList, long startTime, long endTime) {
        fEventList = eventList;
        fZoomedEventList = zoomedEventList;
        if (zoomedEventList != null && !zoomedEventList.isEmpty()) {
            fZoomedStartTime = zoomedEventList.get(0).getTime();
            ITimeEvent lastEvent = zoomedEventList.get(zoomedEventList.size() - 1);
            fZoomedEndTime = lastEvent.getTime() + lastEvent.getDuration();
        } else {
            fZoomedStartTime = Long.MAX_VALUE;
            fZoomedEndTime = Long.MIN_VALUE;
        }
        fStartTime = startTime;
        fEndTime = endTime;
    }

    @Override
    public boolean hasNext() {
        if (fNext == null && fEventList != null) {
            while (fIndex < fEventList.size()) {
                ITimeEvent event = fEventList.get(fIndex++);
                if (event.getTime() + event.getDuration() >= fStartTime && event.getTime() <= fEndTime &&
                        (event.getTime() < fZoomedStartTime || event.getTime() + event.getDuration() > fZoomedEndTime)) {
                    // the event is visible and is not completely hidden by the zoomed events
                    fNext = event;
                    if (event.getTime() < fZoomedEndTime && event.getTime() + event.getDuration() > fZoomedStartTime) {
                        // the event is partially hidden by the zoomed events and must be split
                        fNext = null;
                        if (event.getTime() + event.getDuration() > fZoomedEndTime && fZoomedEndTime < fEndTime) {
                            // the end of the event is partially hidden by the zoomed events and is visible
                            fNext = event.splitAfter(fZoomedEndTime);
                        }
                        if (event.getTime() < fZoomedStartTime && fZoomedStartTime > fStartTime) {
                            // the start of the event is partially hidden by the zoomed events and is visible
                            fSplitNext = fNext;
                            fNext = event.splitBefore(fZoomedStartTime);
                        }
                    }
                    if (fNext != null) {
                        break;
                    }
                }
            }
            if (fNext == null) {
                fEventList = null;
            }
        }

        if (fZoomedNext == null && fZoomedEventList != null) {
            while (fZoomedIndex < fZoomedEventList.size()) {
                ITimeEvent event = fZoomedEventList.get(fZoomedIndex++);
                if (event.getTime() + event.getDuration() >= fStartTime && event.getTime() <= fEndTime) {
                    // the zoomed event is visible
                    fZoomedNext = event;
                    break;
                }
            }
            if (fZoomedNext == null) {
                fZoomedEventList = null;
            }
        }

        return fNext != null || fZoomedNext != null;
    }

    @Override
    public ITimeEvent next() {
        if (hasNext()) {
            if (fZoomedNext != null && (fNext == null || fZoomedNext.getTime() <= fNext.getTime())) {
                ITimeEvent event = checkNotNull(fZoomedNext);
                fZoomedNext = null;
                return event;
            }
            ITimeEvent event = checkNotNull(fNext);
            fNext = fSplitNext;
            fSplitNext = null;
            return event;
        }
        throw new NoSuchElementException();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
