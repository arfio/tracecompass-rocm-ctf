/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.internal.callstack.ui.flamegraph;

import java.text.Format;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.timing.core.statistics.IStatistics;
import org.eclipse.tracecompass.incubator.analysis.core.concepts.AggregatedCallSite;
import org.eclipse.tracecompass.incubator.analysis.core.concepts.ICallStackSymbol;
import org.eclipse.tracecompass.incubator.analysis.core.model.IHostModel;
import org.eclipse.tracecompass.incubator.callstack.core.instrumented.ICalledFunction;
import org.eclipse.tracecompass.incubator.internal.callstack.core.instrumented.callgraph.AggregatedCalledFunction;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;

import com.google.common.collect.ImmutableMap;

/**
 * Time Event implementation specific to the FlameGraph View (it represents a
 * function in a certain depth)
 *
 * @author Sonia Farrah
 */
public class FlamegraphEvent extends TimeEvent {

    private final ICallStackSymbol fSymbol;

    private final AggregatedCallSite fCallSite;

    /**
     * Constructor
     *
     * @param source
     *            The Entry
     * @param beginTime
     *            The event's begin time
     * @param aggregatedFunction
     *            The function the event's presenting
     */
    public FlamegraphEvent(ITimeGraphEntry source, long beginTime, AggregatedCallSite aggregatedFunction) {
        super(source, beginTime, aggregatedFunction.getWeight(), aggregatedFunction.getObject().hashCode());
        fSymbol = aggregatedFunction.getObject();
        fCallSite = aggregatedFunction;
    }

    /**
     * The event's name or address
     *
     * @return The event's name or address
     */
    public Object getSymbol() {
        return fSymbol;
    }

    /**
     * Return a map containing tooltip key, value strings for this event
     *
     * @param formatter
     *            The number formatter to use
     * @return A map of key, value tooltip pairs
     */
    public Map<String, String> getTooltip(Format formatter) {
        ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<>();
        for (Entry<String, IStatistics<?>> entry : fCallSite.getStatistics().entrySet()) {
            String statType = String.valueOf(entry.getKey());
            IStatistics<?> stats = entry.getValue();
            if (stats.getMax() != IHostModel.TIME_UNKNOWN) {
                builder.put(statType, ""); //$NON-NLS-1$
                String lowerType = statType.toLowerCase();
                builder.put("\t" + Messages.FlameGraph_Total + ' ' + lowerType, formatter.format(stats.getTotal())); //$NON-NLS-1$
                builder.put("\t" + Messages.FlameGraph_Average + ' ' + lowerType, formatter.format(stats.getMean())); //$NON-NLS-1$
                builder.put("\t" + Messages.FlameGraph_Max + ' ' + lowerType, formatter.format(stats.getMax())); //$NON-NLS-1$
                builder.put("\t" + Messages.FlameGraph_Min + ' ' + lowerType, formatter.format(stats.getMin())); //$NON-NLS-1$
                builder.put("\t" + Messages.FlameGraph_Deviation + ' ' + lowerType, formatter.format(stats.getStdDev())); //$NON-NLS-1$

            }
        }
        return builder.build();
    }

    /**
     * Get the number of calls for this flamegraph event's callsite. If the callsite
     * was instrumented, the result will be the exact number of calls of this call
     * site, otherwise, it will be the number of samples.
     *
     * @return The number of calls of the call site
     */
    public long getNumberOfCalls() {
        AggregatedCallSite callSite = fCallSite;
        // Return the number of calls if this is an instrumented function, otherwise,
        // the length will be the number of samples
        if (callSite instanceof AggregatedCalledFunction) {
            return ((AggregatedCalledFunction) callSite).getNbCalls();
        }
        return callSite.getWeight();
    }

    /**
     * Get the object with minimal duration in this aggregated call site.
     *
     * @return The minimal duration object, or <code>null</code> if no such object
     *         exists
     */
    public @Nullable ICalledFunction getMinObject() {
        AggregatedCallSite callSite = fCallSite;
        // Return the number of calls if this is an instrumented function, otherwise,
        // the length will be the number of samples
        if (callSite instanceof AggregatedCalledFunction) {
            return ((AggregatedCalledFunction) callSite).getFunctionStatistics().getDurationStatistics().getMinObject();
        }
        return null;
    }

    /**
     * Get the object with maximum duration in this aggregated call site.
     *
     * @return The maximum duration object, or <code>null</code> if no such object
     *         exists
     */
    public @Nullable ICalledFunction getMaxObject() {
        AggregatedCallSite callSite = fCallSite;
        // Return the number of calls if this is an instrumented function, otherwise,
        // the length will be the number of samples
        if (callSite instanceof AggregatedCalledFunction) {
            return ((AggregatedCalledFunction) callSite).getFunctionStatistics().getDurationStatistics().getMaxObject();
        }
        return null;
    }

}
