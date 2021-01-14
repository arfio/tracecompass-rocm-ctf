/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.signals;

import org.eclipse.tracecompass.tmf.core.signal.TmfTraceModelSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * A signal to say a computer core was selected
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public class TmfCpuSelectedSignal extends TmfTraceModelSignal {

    private final int fCore;
    private final ITmfTrace fTrace;

    /**
     * Constructor
     *
     * @param source
     *            the source
     * @param core
     *            the core number, -1 for all
     * @param trace
     *            the current trace that the cpu belongs to
     */
    public TmfCpuSelectedSignal(Object source, int core, ITmfTrace trace) {
        super(source, 0, trace.getHostId());
        fTrace = trace;
        fCore = core;
    }

    /**
     * Get the core
     *
     * @return the core number
     */
    public int getCore() {
        return fCore;
    }

    /**
     * Get the trace for which the CPUs were changed.
     *
     * @return the impacted trace
     * @since 2.4
     */
    public ITmfTrace getTrace() {
        return fTrace;
    }
}
