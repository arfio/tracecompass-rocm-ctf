/*******************************************************************************
 * Copyright (c) 2009, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   William Bourque <wbourque@gmail.com> - Initial API and implementation
 *   Matthew Khouzam - Update to CtfTmf trace and events
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests.temp.headless;

import java.util.Vector;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;

/**
 * Benchmark the event request subsystem of TMF.
 */
public class RequestBenchmark extends TmfEventRequest {

    private RequestBenchmark(final Class<? extends ITmfEvent> dataType,
            final TmfTimeRange range, final int nbRequested) {
        super(dataType, range, 0, nbRequested, ExecutionType.FOREGROUND);
    }

    // Path of the trace
    private static final String TRACE_PATH = "../org.eclipse.tracecompass.ctf.core.tests/traces/kernel";

    // Change this to run several time over the same trace
    private static final int NB_OF_PASS = 100;

    // Work variables
    private static int nbEvent = 0;
    private static TmfExperiment fExperiment = null;
    private static Vector<Double> benchs = new Vector<>();

    /**
     * Run the benchmark
     *
     * @param args
     *            The command-line arguments
     */
    public static void main(final String[] args) {

        try {
            /* Our experiment will contains ONE trace */
            final ITmfTrace[] traces = new ITmfTrace[1];
            traces[0] = new CtfTmfTrace();
            traces[0].initTrace(null, TRACE_PATH, CtfTmfEvent.class);
            /* Create our new experiment */
            fExperiment = new TmfExperiment(CtfTmfEvent.class, "Headless", traces,
                    TmfExperiment.DEFAULT_INDEX_PAGE_SIZE, null);

            /*
             * We will issue a request for each "pass". TMF will then process
             * them synchronously.
             */
            RequestBenchmark request = null;
            for (int x = 0; x < NB_OF_PASS; x++) {
                request = new RequestBenchmark(CtfTmfEvent.class,
                        TmfTimeRange.ETERNITY, Integer.MAX_VALUE);
                fExperiment.sendRequest(request);
            }
            prev = System.nanoTime();
        } catch (final NullPointerException e) {
            /*
             * Silently dismiss Null pointer exception The only way to "finish"
             * the threads in TMF is by crashing them with null.
             */
        } catch (final Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void handleData(final ITmfEvent event) {
        super.handleData(event);
        nbEvent++;

    }

    static long prev;
    static long done = 0;
    @Override
    public void handleCompleted() {
        final long next = System.nanoTime();
        double val = next - prev;
        final int nbEvent2 = nbEvent;
        val /= nbEvent2;

        nbEvent = 0;
        prev = next;
        benchs.add(val);
        if (benchs.size() == NB_OF_PASS) {
            try {
                System.out.println("Nb events : " + nbEvent2);

                for (final double value : benchs) {
                    System.out.print(value + ", ");
                }
                fExperiment.sendRequest(null);

            } catch (final Exception e) {
            }
        }
    }

    @Override
    public void handleSuccess() {
    }

    @Override
    public void handleFailure() {
    }

    @Override
    public void handleCancel() {
    }

}
