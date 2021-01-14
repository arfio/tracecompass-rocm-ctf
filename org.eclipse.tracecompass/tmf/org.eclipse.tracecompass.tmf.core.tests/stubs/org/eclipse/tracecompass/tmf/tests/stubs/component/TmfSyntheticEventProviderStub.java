/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.tests.stubs.component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.core.component.TmfProviderManager;
import org.eclipse.tracecompass.tmf.core.component.ITmfEventProvider;
import org.eclipse.tracecompass.tmf.core.component.TmfEventProvider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest.ExecutionType;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfContext;
import org.eclipse.tracecompass.tmf.tests.stubs.event.TmfSyntheticEventStub;

/**
 * <b><u>TmfSyntheticEventProviderStub</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
@SuppressWarnings("javadoc")
public class TmfSyntheticEventProviderStub extends TmfEventProvider {

    public static final int NB_EVENTS  = 1000;

    private final BlockingQueue<ITmfEvent> fDataQueue = new LinkedBlockingQueue<>(1000);

    public TmfSyntheticEventProviderStub() {
        super("TmfSyntheticEventProviderStub", TmfSyntheticEventStub.class);
    }

    @Override
    public ITmfContext armRequest(final ITmfEventRequest request) {

        // Get the TmfSyntheticEventStub provider
        final ITmfEventProvider[] eventProviders = TmfProviderManager.getProviders(ITmfEvent.class, TmfEventProviderStub.class);
        final ITmfEventProvider provider = eventProviders[0];

        final TmfTimeRange range = request.getRange();
        final TmfEventRequest subRequest =
                new TmfEventRequest(ITmfEvent.class, range, 0, NB_EVENTS, ExecutionType.FOREGROUND) {
                    @Override
                    public void handleData(final ITmfEvent event) {
                        super.handleData(event);
                        handleIncomingData(event);
                    }
                };
        provider.sendRequest(subRequest);

        // Return a dummy context
        return new TmfContext();
    }

    // Queue 2 synthetic events per base event
    private void handleIncomingData(final @NonNull ITmfEvent e) {
        queueResult(new TmfSyntheticEventStub(e));
        queueResult(new TmfSyntheticEventStub(e));
    }

    private static final int TIMEOUT = 10000;

    @Override
    public TmfSyntheticEventStub getNext(final ITmfContext context) {
        TmfSyntheticEventStub data = null;
        try {
            data = (TmfSyntheticEventStub) fDataQueue.poll(TIMEOUT, TimeUnit.MILLISECONDS);
            if (data == null) {
                throw new InterruptedException();
            }
        }
        catch (final InterruptedException e) {
        }
        return data;
    }

    public void queueResult(final TmfSyntheticEventStub data) {
        boolean ok = false;
        try {
            ok = fDataQueue.offer(data, TIMEOUT, TimeUnit.MILLISECONDS);
            if (!ok) {
                throw new InterruptedException();
            }
        }
        catch (final InterruptedException e) {
        }
    }

}
