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
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.signal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Signal indicating a trace is being closed.
 *
 * Receivers should cancel any jobs, threads or requests for the specified trace
 * and clear any user interface component related to it as soon as possible.
 * The trace will be disposed after the signal has been processed.
 *
 * @author Patrick Tasse
 */
@NonNullByDefault
public class TmfTraceClosedSignal extends TmfSignal {

    private final ITmfTrace fTrace;

    /**
     * Constructor for a new signal
     *
     * @param source
     *            The object sending this signal
     * @param trace
     *            The trace being closed
     */
    public TmfTraceClosedSignal(Object source, ITmfTrace trace) {
        super(source);
        fTrace = trace;
    }

    /**
     * Get a reference to the trace being closed
     *
     * @return The trace object
     */
    public ITmfTrace getTrace() {
        return fTrace;
    }

    @Override
    public String toString() {
        return "[TmfTraceClosedSignal (" + fTrace.getName() + ")]"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
