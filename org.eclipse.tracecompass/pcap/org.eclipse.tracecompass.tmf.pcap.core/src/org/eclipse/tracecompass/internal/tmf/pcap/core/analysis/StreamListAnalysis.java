/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.pcap.core.analysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.pcap.core.event.PcapEvent;
import org.eclipse.tracecompass.internal.tmf.pcap.core.event.TmfPacketStreamBuilder;
import org.eclipse.tracecompass.internal.tmf.pcap.core.protocol.TmfPcapProtocol;
import org.eclipse.tracecompass.internal.tmf.pcap.core.trace.PcapTrace;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;

/**
 * A pcap-specific analysis that parse an entire trace to find all the streams.
 *
 * @author Vincent Perot
 */
public class StreamListAnalysis extends TmfAbstractAnalysisModule {

    /**
     * The Stream List analysis ID.
     */
    public static final String ID = "org.eclipse.linuxtools.tmf.pcap.core.analysis.stream"; //$NON-NLS-1$

    private @Nullable ITmfEventRequest fRequest;
    private final Map<TmfPcapProtocol, TmfPacketStreamBuilder> fBuilders;

    /**
     * The default constructor. It initializes all variables.
     */
    public StreamListAnalysis() {
        super();
        fBuilders = new HashMap<>();
        for (TmfPcapProtocol protocol : TmfPcapProtocol.values()) {
            if (protocol.supportsStream()) {
                fBuilders.put(protocol, new TmfPacketStreamBuilder(protocol));
            }
        }
    }

    @Override
    public boolean canExecute(ITmfTrace trace) {

        // Trace is Pcap
        if (trace instanceof PcapTrace) {
            return true;
        }

        // Trace is not a TmfExperiment
        if (!(trace instanceof TmfExperiment)) {
            return false;
        }

        // Trace is TmfExperiment. Check if it has a PcapTrace.
        TmfExperiment experiment = (TmfExperiment) trace;
        List<ITmfTrace> traces = experiment.getTraces();
        for (ITmfTrace expTrace : traces) {
            if (expTrace instanceof PcapTrace) {
                return true;
            }
        }

        // No Pcap :(
        return false;
    }

    @Override
    protected boolean executeAnalysis(@Nullable IProgressMonitor monitor) throws TmfAnalysisException {
        IProgressMonitor mon = (monitor == null ? new NullProgressMonitor() : monitor);
        ITmfTrace trace = getTrace();
        if (trace == null) {
            /* This analysis was cancelled in the meantime */
            return false;
        }

        ITmfEventRequest request = fRequest;
        if ((request != null) && (!request.isCompleted())) {
            request.cancel();
        }

        request = new TmfEventRequest(PcapEvent.class,
                TmfTimeRange.ETERNITY, 0L, ITmfEventRequest.ALL_DATA,
                ITmfEventRequest.ExecutionType.BACKGROUND) {

            @Override
            public void handleData(ITmfEvent data) {
                // Called for each event
                super.handleData(data);
                if (!(data instanceof PcapEvent)) {
                    return;
                }
                PcapEvent event = (PcapEvent) data;
                for (Map.Entry<TmfPcapProtocol, TmfPacketStreamBuilder> entry : fBuilders.entrySet()) {
                    entry.getValue().addEventToStream(event);
                }

            }
        };
        trace.sendRequest(request);
        fRequest = request;
        try {
            request.waitForCompletion();
        } catch (InterruptedException e) {
            // Request was canceled.
            return false;
        }

        return !mon.isCanceled() && !request.isCancelled() && !request.isFailed();

    }

    @Override
    protected void canceling() {
        ITmfEventRequest req = fRequest;
        if ((req != null) && (!req.isCompleted())) {
            req.cancel();
        }
    }

    /**
     * Getter method that returns the packet builder associated to a particular
     * protocol.
     *
     * @param protocol
     *            The specified protocol.
     * @return The builder.
     */
    public @Nullable TmfPacketStreamBuilder getBuilder(TmfPcapProtocol protocol) {
        return fBuilders.get(protocol);
    }

    /**
     * Method that indicates if the analysis is still running or has finished.
     *
     * @return Whether the analysis is finished or not.
     */
    public boolean isFinished() {
        ITmfEventRequest req = fRequest;
        if (req == null) {
            return false;
        }
        return req.isCompleted();
    }

}
