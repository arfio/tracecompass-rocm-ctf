/*******************************************************************************
 * Copyright (c) 2016, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.profiling.ui.flamegraph;

import org.eclipse.tracecompass.internal.analysis.profiling.core.callgraph.AggregatedCalledFunction;
import org.eclipse.tracecompass.internal.analysis.profiling.core.callgraph.AggregatedCalledFunctionStatistics;
import org.eclipse.tracecompass.internal.analysis.profiling.core.callgraph.ICalledFunction;
import org.eclipse.tracecompass.internal.analysis.profiling.core.callstack.SymbolAspect;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;

/**
 * Time Event implementation specific to the FlameGraph View (it represents a
 * function in a certain depth)
 *
 * @author Sonia Farrah
 *
 */
public class FlamegraphEvent extends TimeEvent {

    private final Object fSymbol;
    private final long fSelfTime;
    private final int fProcessId;
    private final AggregatedCalledFunctionStatistics fStatistics;

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
    public FlamegraphEvent(ITimeGraphEntry source, long beginTime, AggregatedCalledFunction aggregatedFunction) {
        super(source, beginTime, aggregatedFunction.getDuration(), aggregatedFunction.getSymbol().hashCode());
        fSymbol = aggregatedFunction.getSymbol();
        fStatistics = aggregatedFunction.getFunctionStatistics();
        fProcessId = aggregatedFunction.getProcessId();
        fSelfTime = aggregatedFunction.getSelfTime();
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
     * The event's statistics
     *
     * @return The event's statistics
     */
    public AggregatedCalledFunctionStatistics getStatistics() {
        return fStatistics;
    }

    /**
     * The self time of an event
     *
     * @return The self time
     */
    public long getSelfTime() {
        return fSelfTime;
    }

    /**
     * The process ID of the traced application
     *
     * @return process id
     */
    public int getProcessId() {
        return fProcessId;
    }

    @Override
    public String getLabel() {
        return getFunctionSymbol();
    }

    private String getFunctionSymbol() {
        String funcSymbol = ""; //$NON-NLS-1$
        if (getSymbol() instanceof Long || getSymbol() instanceof Integer) {

            ICalledFunction segment = getStatistics().getDurationStatistics().getMinObject();
            if (segment == null) {
                long longAddress = ((Long) getSymbol()).longValue();
                return "0x" + Long.toHexString(longAddress); //$NON-NLS-1$
            }
            Object symbol = SymbolAspect.SYMBOL_ASPECT.resolve(segment);
            if (symbol != null) {
                return symbol.toString();
            }
        } else {
            return getSymbol().toString();
        }
        return funcSymbol;
    }
}
