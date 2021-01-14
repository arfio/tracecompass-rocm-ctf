/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect;

import java.util.Comparator;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiTableEntry;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types.LamiData;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types.LamiTimeRange;

/**
 * Aspect for a time range duration
 *
 * @author Alexandre Montplaisir
 */
public class LamiTimeRangeDurationAspect extends LamiGenericAspect {

    private final int fColIndex;

    /**
     * Constructor
     *
     * @param timeRangeName
     *            Name of the time range
     * @param colIndex
     *            Column index
     */
    public LamiTimeRangeDurationAspect(String timeRangeName, int colIndex) {
        super(timeRangeName + " (" + Messages.LamiAspect_TimeRangeDuration + ')', "ns", colIndex, true, false); //$NON-NLS-1$ //$NON-NLS-2$
        fColIndex = colIndex;
    }

    @Override
    public boolean isTimeDuration() {
        return true;
    }

    @Override
    public @Nullable String resolveString(LamiTableEntry entry) {
        LamiData data = entry.getValue(fColIndex);
        if (data instanceof LamiTimeRange) {
            LamiTimeRange range = (LamiTimeRange) data;

            // TODO: Consider low and high limits here.
            Number duration = range.getDuration();

            if (duration != null) {
                return String.valueOf(duration.longValue());
            }
        }
        return data.toString();
    }

    @Override
    public @Nullable Number resolveNumber(@NonNull LamiTableEntry entry) {
        LamiData data = entry.getValue(fColIndex);
        if (data instanceof LamiTimeRange) {
            LamiTimeRange range = (LamiTimeRange) data;

            // TODO: Consider low and high limits here.
            return range.getDuration();
        }
        return null;
    }

    @Override
    public Comparator<LamiTableEntry> getComparator() {
        return LamiComparators.getLongComparator(this::resolveNumber);
    }

}
