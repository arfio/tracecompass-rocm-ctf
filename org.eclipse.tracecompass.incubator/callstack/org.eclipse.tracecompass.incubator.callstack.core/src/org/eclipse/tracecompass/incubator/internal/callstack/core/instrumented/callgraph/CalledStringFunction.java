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

package org.eclipse.tracecompass.incubator.internal.callstack.core.instrumented.callgraph;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.incubator.analysis.core.model.IHostModel;
import org.eclipse.tracecompass.incubator.callstack.core.instrumented.ICalledFunction;

/**
 * Called Function with the symbol being a string, useful for name resolved
 * ICalledFunction
 *
 * @author Matthew Khouzam
 *
 */
public class CalledStringFunction extends AbstractCalledFunction {

    /**
     * Default Serial ID
     */
    private static final long serialVersionUID = 2996033841349563006L;

    private final String fSymbol;

    /**
     * Create a new segment.
     *
     * The end position should be equal to or greater than the start position.
     *
     * @param start
     *            Start position of the segment
     * @param end
     *            End position of the segment
     * @param symbol
     *            The symbol of the call stack function
     * @param processId
     *            The process ID of the traced application
     * @param threadId
     *            the thread ID of the traced thread
     * @param parent
     *            The caller, can be null for root elements
     * @param model
     *            The model for the host the traced application is running on
     */
    protected CalledStringFunction(long start, long end, String symbol, int processId, int threadId, @Nullable ICalledFunction parent, IHostModel model) {
        super(start, end, processId, threadId, parent, model);
        fSymbol = symbol;
    }

    @Override
    public @NonNull String getSymbol() {
        return fSymbol;
    }

}
