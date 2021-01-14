/*******************************************************************************
 * Copyright (c) 2019, 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.analysis.callsite;

import java.util.NoSuchElementException;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils.QuarkIterator;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.analysis.callsite.ITmfCallsiteIterator;
import org.eclipse.tracecompass.tmf.core.analysis.callsite.TimeCallsite;
import org.eclipse.tracecompass.tmf.core.event.lookup.TmfCallsite;

import com.google.common.annotations.VisibleForTesting;

/**
 * Iterator implementation for iteration of callsites forwards or backwards for
 * a give device and initial time. The callsites return will give time
 * information when it was called.
 *
 * @author Bernd Hufmann
 */
class CallsiteIterator implements ITmfCallsiteIterator {

    private @Nullable ITmfStateSystem fSS = null;

    private @Nullable QuarkIterator fFileIterator = null;
    private @Nullable QuarkIterator fLineIterator = null;
    private int fSourceQuark = ITmfStateSystem.INVALID_ATTRIBUTE;

    private @Nullable TimeCallsite fPrevious = null;
    private @Nullable TimeCallsite fNext = null;
    private @Nullable ITmfStateInterval fFileInterval = null;
    private @Nullable ITmfStateInterval fLineInterval = null;
    private final long fInitialTime;

    private final StateSystemStringInterner fInterner;

    /**
     * Constructor (Use only for testing)
     *
     * @param ss
     *            the program flow state system
     * @param traceId
     *            the trace Id
     * @param deviceType
     *            the device type (CPU/GPU...) to iterate on
     * @param deviceId
     *            the device id to iterate on
     * @param initialTime
     *            the start time of the iteration
     * @param interner
     *            String interner
     */
    @VisibleForTesting
    public CallsiteIterator(@Nullable ITmfStateSystem ss, String traceId, String deviceType, String deviceId, long initialTime, StateSystemStringInterner interner) {
        fInterner = interner;
        fInitialTime = initialTime;
        if (ss == null) {
            return;
        }
        int deviceQuark = ss.optQuarkAbsolute(CallsiteStateProvider.DEVICES, traceId, deviceType, deviceId);
        if (deviceQuark == ITmfStateSystem.INVALID_ATTRIBUTE) {
            return;
        }

        int fileQuark = ss.optQuarkRelative(deviceQuark, CallsiteStateProvider.FILES);
        if (fileQuark == ITmfStateSystem.INVALID_ATTRIBUTE) {
            return;
        }

        int lineQuark = ss.optQuarkRelative(deviceQuark, CallsiteStateProvider.LINES);
        if (lineQuark == ITmfStateSystem.INVALID_ATTRIBUTE) {
            return;
        }
        fFileIterator = new QuarkIterator(ss, fileQuark, initialTime);
        fLineIterator = new QuarkIterator(ss, lineQuark, initialTime);
        fSourceQuark = ss.optQuarkAbsolute(CallsiteStateProvider.STRING_POOL);
        fSS = ss;
    }

    private static boolean notMatchNext(@Nullable ITmfStateInterval interval, long time) {
        return interval == null || interval.getValue() == null || interval.getStartTime() < time;
    }

    private static boolean notMatchPrevious(@Nullable ITmfStateInterval interval, long time) {
        return interval == null || interval.getValue() == null || interval.getStartTime() > time;
    }

    @Override
    public boolean hasNext() {
        if (fNext != null) {
            return true;
        }

        QuarkIterator fileIterator = fFileIterator;
        QuarkIterator lineIterator = fLineIterator;
        ITmfStateInterval lineInterval = fLineInterval;
        ITmfStateInterval fileInterval = fFileInterval;
        long nextTime = Long.MIN_VALUE;
        if ((fileIterator == null || lineIterator == null) ||
                (lineInterval == null && !lineIterator.hasNext()) ||
                (fileInterval == null && !fileIterator.hasNext())) {
            return false;
        }
        if (fileInterval != null && lineInterval != null) {
            nextTime = Math.min(lineInterval.getEndTime() + 1, fileInterval.getEndTime() + 1);
        } else {
            nextTime = fInitialTime;
        }
        fPrevious = null;
        if (fileInterval == null) {
            fileInterval = fileIterator.next();
        }
        if (lineInterval == null) {
            lineInterval = lineIterator.next();
        }

        while (notMatchNext(fileInterval, nextTime) && notMatchNext(lineInterval, nextTime)) {
            int compare = Long.compare(fileInterval.getEndTime(), lineInterval.getEndTime());
            if (compare <= 0) {
                if (fileIterator.hasNext()) {
                    fileInterval = fileIterator.next();
                } else {
                    return false;
                }
            }
            if (compare >= 0) {
                if (lineIterator.hasNext()) {
                    lineInterval = lineIterator.next();
                } else {
                    return false;
                }
            }
        }

        fFileInterval = fileInterval;
        fLineInterval = lineInterval;
        TimeCallsite next = getCallsite(fileInterval, lineInterval);
        fNext = next;
        return next != null;
    }

    @Override
    public TimeCallsite next() {
        if (hasNext()) {
            TimeCallsite next = Objects.requireNonNull(fNext, "Inconsistent state, should be non null if hasNext returned true"); //$NON-NLS-1$
            fNext = null;
            return next;
        }
        throw new NoSuchElementException();
    }

    /**
     * Returns {@code true} if the backwards iteration has more elements. (In
     * other words, returns {@code true} if {@link #previous} would return an
     * element rather than throwing an exception.)
     *
     * @return {@code true} if the backwards iteration has more elements
     */
    @Override
    public boolean hasPrevious() {
        if (fPrevious != null) {
            return true;
        }

        QuarkIterator fileIterator = fFileIterator;
        QuarkIterator lineIterator = fLineIterator;
        ITmfStateInterval lineInterval = fLineInterval;
        ITmfStateInterval fileInterval = fFileInterval;
        long prevTime = Long.MAX_VALUE;
        if ((fileIterator == null || lineIterator == null) ||
                (lineInterval == null && !fileIterator.hasPrevious()) ||
                (fileInterval == null && !lineIterator.hasPrevious())) {
            return false;
        }
        if (fileInterval != null && lineInterval != null) {
            prevTime = Math.max(lineInterval.getStartTime() - 1, fileInterval.getStartTime() - 1);
        } else {
            prevTime = fInitialTime;
        }
        fNext = null;
        if (fileInterval == null) {
            fileInterval = fileIterator.previous();
        }
        if (lineInterval == null) {
            lineInterval = lineIterator.previous();
        }

        while (notMatchPrevious(fileInterval, prevTime) || notMatchPrevious(lineInterval, prevTime)) {
            int compare = Long.compare(fileInterval.getStartTime(), lineInterval.getStartTime());
            if (compare >= 0) {
                if (fileIterator.hasPrevious()) {
                    fileInterval = fileIterator.previous();
                } else {
                    return false;
                }
            }
            if (compare <= 0) {
                if (lineIterator.hasPrevious()) {
                    lineInterval = lineIterator.previous();
                } else {
                    return false;
                }
            }
        }

        fFileInterval = fileInterval;
        fLineInterval = lineInterval;
        TimeCallsite previous = getCallsite(fileInterval, lineInterval);
        fPrevious = previous;
        return previous != null;
    }

    /**
     * Returns the previous element in the iteration.
     *
     * @return the previous element in the iteration
     * @throws NoSuchElementException
     *             if the iteration has no more elements
     */
    @Override
    public TimeCallsite previous() {
        if (hasPrevious()) {
            TimeCallsite prev = Objects.requireNonNull(fPrevious, "Inconsistent state, should be non null if hasPrevious returned true"); //$NON-NLS-1$
            fPrevious = null;
            return prev;
        }
        throw new NoSuchElementException();
    }

    private @Nullable TimeCallsite getCallsite(@Nullable ITmfStateInterval fileInterval, @Nullable ITmfStateInterval lineInterval) {
        ITmfStateSystem ss = fSS;
        if (ss == null || fileInterval == null || lineInterval == null) {
            return null;
        }
        try {
            Object value = fileInterval.getValue();
            if (value instanceof Integer) {
                long fileId = (Integer) value + ss.getStartTime();
                // Query line number
                Object lineValue = lineInterval.getValue();
                if (lineValue instanceof Integer) {
                    long line = (Integer) lineValue;
                    String fileName = fInterner.resolve(ss, fileId, fSourceQuark);
                    if (fileName != null) {
                        long time = Math.max(fileInterval.getStartTime(), lineInterval.getStartTime());
                        return new TimeCallsite(new TmfCallsite(fileName, line == -1 ? null : line), time);
                    }
                }
            }
        } catch (StateSystemDisposedException e) {
            // Skip
        }
        return null;
    }
}
