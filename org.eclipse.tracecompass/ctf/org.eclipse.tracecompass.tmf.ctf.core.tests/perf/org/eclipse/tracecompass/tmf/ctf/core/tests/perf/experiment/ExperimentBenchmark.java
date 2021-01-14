/*******************************************************************************
 * Copyright (c) 2014, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *     Geneviève Bastien - Convert to JUnit performance test
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests.perf.experiment;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest.ExecutionType;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfExperimentStub;
import org.junit.Test;

/**
 * Coalescing benchmark
 *
 * @author Matthew Khouzam
 */
public class ExperimentBenchmark {

    private static final String TEST_ID = "org.eclipse.linuxtools#Experiment benchmark#";
    private static final int MAX_TRACES = 160;
    private static final int BLOCK_SIZE = 100;
    private static final String TRACES_ROOT_PATH;
    static {
        try {
            TRACES_ROOT_PATH = FileUtils.toFile(FileLocator.toFileURL(CtfTestTrace.TRACE_EXPERIMENT.getTraceURL())).getAbsolutePath();
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }
    private static final int SAMPLE_SIZE_SLOW = 20;
    private static final int SAMPLE_SIZE = 100;

    private TmfExperimentStub fExperiment;

    /**
     * Run the benchmark
     */
    @Test
    public void benchmarkExperimentSizeRequest() {
        Performance perf = Performance.getDefault();

        for (int numTraces = 1; numTraces < MAX_TRACES; numTraces = (int) (1.6 * (numTraces + 1))) {
            PerformanceMeter pm = perf.createPerformanceMeter(TEST_ID + numTraces + " traces");
            perf.tagAsSummary(pm, "Experiment Benchmark:" + numTraces + " traces", Dimension.CPU_TIME);
            if ((int) (1.6 * (numTraces + 1)) > MAX_TRACES) {
                perf.tagAsGlobalSummary(pm, "Experiment Benchmark:" + numTraces + " traces", Dimension.CPU_TIME);
            }

            int sampleSize = SAMPLE_SIZE;
            if (numTraces > 20) {
                sampleSize = SAMPLE_SIZE_SLOW;
            }

            for (int s = 0; s < sampleSize; s++) {

                InnerEventRequest expReq = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
                InnerEventRequest traceReq[] = new InnerEventRequest[numTraces];

                init(numTraces);
                fExperiment.sendRequest(expReq);
                List<ITmfTrace> traces = fExperiment.getTraces();
                for (int i = 0; i < numTraces; i++) {
                    traceReq[i] = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
                    traces.get(i).sendRequest(traceReq[i]);
                }

                pm.start();
                waitForRequest(expReq, traceReq);
                pm.stop();

                for (int i = 0; i < traces.size(); i++) {
                    if (!expReq.isTraceHandled(traces.get(i))) {
                        System.err.println("Trace " + i + " not handled!");
                    }
                }

                fExperiment.dispose();
            }
            pm.commit();
        }
    }

    /**
     * Initialization
     *
     * @param maxTraces
     *            maximum number of traces to open
     */
    private void init(int maxTraces) {
        try {
            File parentDir = new File(TRACES_ROOT_PATH);
            File[] traceFiles = parentDir.listFiles();
            ITmfTrace[] traces = new CtfTmfTrace[Math.min(maxTraces, traceFiles.length)];
            for (int i = 0; i < traces.length; i++) {
                traces[i] = new CtfTmfTrace();
            }
            int j = 0;
            for (int i = 0; i < (traces.length) && (j < traces.length); i++) {
                String absolutePath = traceFiles[j].getAbsolutePath();
                if (traces[i].validate(null, absolutePath).isOK()) {
                    traces[i].initTrace(null, absolutePath, ITmfEvent.class);
                } else {
                    i--;
                }
                j++;
            }
            fExperiment = new TmfExperimentStub("MegaExperiment", traces, BLOCK_SIZE);
            if (traces[traces.length - 1].getPath() == null) {
                throw new TmfTraceException("Insufficient valid traces in directory");
            }
        } catch (TmfTraceException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void waitForRequest(InnerEventRequest expReq, InnerEventRequest[] traceReqs) {
        try {
            expReq.waitForCompletion();
            List<InnerEventRequest> reqs = Arrays.asList(traceReqs);
            for (InnerEventRequest traceReq : reqs) {
                traceReq.waitForCompletion();
            }
        } catch (InterruptedException e) {
        }
    }

    private static class InnerEventRequest extends TmfEventRequest {
        private Set<String> fTraces = new HashSet<>();

        public InnerEventRequest(Class<? extends ITmfEvent> dataType, long index, int nbRequested, ExecutionType priority) {
            super(dataType, index, nbRequested, priority);
        }

        @Override
        public void handleData(ITmfEvent event) {
            super.handleData(event);
            if (!fTraces.contains(event.getTrace().getName())) {
                fTraces.add(event.getTrace().getName());
            }
        }

        public boolean isTraceHandled(ITmfTrace trace) {
            return fTraces.contains(trace.getName());
        }
    }
}
