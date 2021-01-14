/*******************************************************************************
 * Copyright (c) 2009, 2017 Ericsson
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
 *   Francois Chouinard - Adjusted for new Trace Model
 *   Alexandre Montplaisir - Port to JUnit4
 *   Patrick Tasse - Fix for concurrency
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Vector;

import org.eclipse.tracecompass.internal.tmf.core.trace.experiment.TmfExperimentContext;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest.ExecutionType;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfExperimentStub;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test suite for the TmfExperiment class (multiple traces).
 */
@SuppressWarnings("javadoc")
public class TmfMultiTraceExperimentTest {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private static final long   DEFAULT_INITIAL_OFFSET_VALUE = (1L * 100 * 1000 * 1000); // .1sec
    private static final String EXPERIMENT   = "MyExperiment";
    private static int          NB_EVENTS    = 20000;
    private static int          BLOCK_SIZE   = 1000;

    private static TmfExperimentStub fExperiment;

    private static byte SCALE = (byte) -3;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    @BeforeClass
    public static void setUp() {
        ITmfTrace[] traces = setupTraces();
        fExperiment = new TmfExperimentStub(EXPERIMENT, traces, BLOCK_SIZE);
        fExperiment.getIndexer().buildIndex(0, TmfTimeRange.ETERNITY, true);
    }

    @AfterClass
    public static void tearDown() {
        fExperiment.dispose();
    }

    private static ITmfTrace[] setupTraces() {
        try {
            ITmfTrace[] traces = new ITmfTrace[2];

            final TmfTraceStub trace1 = new TmfTraceStub(TmfTestTrace.O_TEST_10K.getFullPath(), 0, true, null);
            traces[0] = trace1;

            final TmfTraceStub trace2 = new TmfTraceStub(TmfTestTrace.E_TEST_10K.getFullPath(), 0, true, null);
            traces[1] = trace2;

            return traces;
        } catch (final TmfTraceException e) {
            e.printStackTrace();
        }
        return new ITmfTrace[0];
    }

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    @Test
    public void testBasicTmfExperimentConstructor() {
        assertEquals("GetId", EXPERIMENT, fExperiment.getName());
        assertEquals("GetNbEvents", NB_EVENTS, fExperiment.getNbEvents());

        final TmfTimeRange timeRange = fExperiment.getTimeRange();
        assertEquals("getStartTime", 1, timeRange.getStartTime().getValue());
        assertEquals("getEndTime", NB_EVENTS, timeRange.getEndTime().getValue());

        ITmfTimestamp initRange = TmfTimestamp.fromNanos(DEFAULT_INITIAL_OFFSET_VALUE);
        assertEquals("getInitialRangeOffset", initRange, fExperiment.getInitialRangeOffset());
    }

    // ------------------------------------------------------------------------
    // seekEvent on rank
    // ------------------------------------------------------------------------

    @Test
    public void testSeekRankOnCacheBoundary() {
        long cacheSize = fExperiment.getCacheSize();

        // On lower bound, returns the first event (TS = 1)
        ITmfContext context = fExperiment.seekEvent(0);
        assertEquals("Context rank", 0, context.getRank());

        ITmfEvent event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Context rank", 1, context.getRank());

        // Position trace at event rank [cacheSize]
        context = fExperiment.seekEvent(cacheSize);
        assertEquals("Context rank", cacheSize, context.getRank());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", cacheSize + 1, event.getTimestamp().getValue());
        assertEquals("Context rank", cacheSize + 1, context.getRank());

        // Position trace at event rank [4 * cacheSize]
        context = fExperiment.seekEvent(4 * cacheSize);
        assertEquals("Context rank", 4 * cacheSize, context.getRank());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 4 * cacheSize + 1, event.getTimestamp().getValue());
        assertEquals("Context rank", 4 * cacheSize + 1, context.getRank());
    }

    @Test
    public void testSeekRankNotOnCacheBoundary() {
        long cacheSize = fExperiment.getCacheSize();

        // Position trace at event rank 9
        ITmfContext context = fExperiment.seekEvent(9);
        assertEquals("Context rank", 9, context.getRank());

        ITmfEvent event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
        assertEquals("Context rank", 10, context.getRank());

        // Position trace at event rank [cacheSize - 1]
        context = fExperiment.seekEvent(cacheSize - 1);
        assertEquals("Context rank", cacheSize - 1, context.getRank());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", cacheSize, event.getTimestamp().getValue());
        assertEquals("Context rank", cacheSize, context.getRank());

        // Position trace at event rank [cacheSize + 1]
        context = fExperiment.seekEvent(cacheSize + 1);
        assertEquals("Context rank", cacheSize + 1, context.getRank());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", cacheSize + 2, event.getTimestamp().getValue());
        assertEquals("Context rank", cacheSize + 2, context.getRank());

        // Position trace at event rank 4500
        context = fExperiment.seekEvent(4500);
        assertEquals("Context rank", 4500, context.getRank());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
        assertEquals("Context rank", 4501, context.getRank());
    }

    @Test
    public void testSeekRankOutOfScope() {
        // Position trace at beginning
        ITmfContext context = fExperiment.seekEvent(-1);
        assertEquals("Event rank", 0, context.getRank());

        ITmfEvent event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Context rank", 1, context.getRank());

        // Position trace at event passed the end
        context = fExperiment.seekEvent(NB_EVENTS);
        assertEquals("Context rank", NB_EVENTS, context.getRank());

        event = fExperiment.getNext(context);
        assertNull("Event", event);
        assertEquals("Context rank", NB_EVENTS, context.getRank());
    }

    // ------------------------------------------------------------------------
    // seekEvent on timestamp
    // ------------------------------------------------------------------------

    @Test
    public void testSeekTimestampOnCacheBoundary() {
        long cacheSize = fExperiment.getCacheSize();

        // Position trace at event rank 0
        ITmfContext context = fExperiment.seekEvent(TmfTimestamp.create(1, SCALE));
        assertEquals("Context rank", 0, context.getRank());

        ITmfEvent event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Context rank", 1, context.getRank());

        // Position trace at event rank [cacheSize]
        context = fExperiment.seekEvent(TmfTimestamp.create(cacheSize + 1, SCALE));
        assertEquals("Event rank", cacheSize, context.getRank());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", cacheSize + 1, event.getTimestamp().getValue());
        assertEquals("Context rank", cacheSize + 1, context.getRank());

        // Position trace at event rank [4 * cacheSize]
        context = fExperiment.seekEvent(TmfTimestamp.create(4 * cacheSize + 1, SCALE));
        assertEquals("Context rank", 4 * cacheSize, context.getRank());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 4 * cacheSize + 1, event.getTimestamp().getValue());
        assertEquals("Context rank", 4 * cacheSize + 1, context.getRank());
    }

    @Test
    public void testSeekTimestampNotOnCacheBoundary() {
        // Position trace at event rank 1 (TS = 2)
        ITmfContext context = fExperiment.seekEvent(TmfTimestamp.create(2, SCALE));
        assertEquals("Context rank", 1, context.getRank());

        ITmfEvent event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 2, event.getTimestamp().getValue());
        assertEquals("Context rank", 2, context.getRank());

        // Position trace at event rank 9 (TS = 10)
        context = fExperiment.seekEvent(TmfTimestamp.create(10, SCALE));
        assertEquals("Context rank", 9, context.getRank());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
        assertEquals("Context rank", 10, context.getRank());

        // Position trace at event rank 999 (TS = 1000)
        context = fExperiment.seekEvent(TmfTimestamp.create(1000, SCALE));
        assertEquals("Context rank", 999, context.getRank());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
        assertEquals("Context rank", 1000, context.getRank());

        // Position trace at event rank 1001 (TS = 1002)
        context = fExperiment.seekEvent(TmfTimestamp.create(1002, SCALE));
        assertEquals("Context rank", 1001, context.getRank());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
        assertEquals("Context rank", 1002, context.getRank());

        // Position trace at event rank 4500 (TS = 4501)
        context = fExperiment.seekEvent(TmfTimestamp.create(4501, SCALE));
        assertEquals("Context rank", 4500, context.getRank());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
        assertEquals("Context rank", 4501, context.getRank());
    }

    @Test
    public void testSeekTimestampOutOfScope() {
        // Position trace at beginning
        ITmfContext context = fExperiment.seekEvent(TmfTimestamp.create(-1, SCALE));
        assertEquals("Event rank", 0, context.getRank());

        ITmfEvent event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 1, context.getRank());

        // Position trace at event passed the end
        context = fExperiment.seekEvent(TmfTimestamp.create(NB_EVENTS + 1, SCALE));
        event = fExperiment.getNext(context);
        assertNull("Event location", event);
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
    }

    // ------------------------------------------------------------------------
    // seekEvent by location (context rank is undefined)
    // ------------------------------------------------------------------------

    @Test
    public void testSeekLocationOnCacheBoundary() {
        long cacheSize = fExperiment.getCacheSize();

        // Position trace at event rank 0
        ITmfContext tmpContext = fExperiment.seekEvent(0);
        ITmfContext context = fExperiment.seekEvent(tmpContext.getLocation());

        ITmfEvent event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 2, event.getTimestamp().getValue());

        // Position trace at event rank 'cacheSize'
        tmpContext = fExperiment.seekEvent(cacheSize);
        context = fExperiment.seekEvent(tmpContext.getLocation());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", cacheSize + 1, event.getTimestamp().getValue());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", cacheSize + 2, event.getTimestamp().getValue());

        // Position trace at event rank 4 * 'cacheSize'
        tmpContext = fExperiment.seekEvent(4 * cacheSize);
        context = fExperiment.seekEvent(tmpContext.getLocation());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 4 * cacheSize + 1, event.getTimestamp().getValue());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 4 * cacheSize + 2, event.getTimestamp().getValue());
    }

    @Test
    public void testSeekLocationNotOnCacheBoundary() {
        long cacheSize = fExperiment.getCacheSize();

        // Position trace at event 'cacheSize' - 1
        ITmfContext tmpContext = fExperiment.seekEvent(cacheSize - 1);
        ITmfContext context = fExperiment.seekEvent(tmpContext.getLocation());

        ITmfEvent event = fExperiment.getNext(context);
        assertEquals("Event timestamp", cacheSize, event.getTimestamp().getValue());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", cacheSize + 1, event.getTimestamp().getValue());

        // Position trace at event rank 2 * 'cacheSize' - 1
        tmpContext = fExperiment.seekEvent(2 * cacheSize - 1);
        context = fExperiment.seekEvent(tmpContext.getLocation());
        context = fExperiment.seekEvent(2 * cacheSize - 1);

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 2 * cacheSize, event.getTimestamp().getValue());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 2 * cacheSize + 1, event.getTimestamp().getValue());

        // Position trace at event rank 4500
        tmpContext = fExperiment.seekEvent(4500);
        context = fExperiment.seekEvent(tmpContext.getLocation());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());

        event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 4502, event.getTimestamp().getValue());
    }

    @Test
    public void testSeekLocationOutOfScope() {
        // Position trace at beginning
        ITmfContext context = fExperiment.seekEvent((ITmfLocation) null);

        ITmfEvent event = fExperiment.getNext(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
    }

    // ------------------------------------------------------------------------
    // getNext - updates the context
    // ------------------------------------------------------------------------

    private static void validateContextRanks(ITmfContext context) {
        assertTrue("Experiment context type", context instanceof TmfExperimentContext);
        TmfExperimentContext ctx = (TmfExperimentContext) context;

        ITmfContext[] subContexts = ctx.getContexts();

        long expRank = 0;
        for (ITmfContext subContext : subContexts) {
            assertNotNull(subContext);
            long rank = subContext.getRank();
            if (rank == -1) {
                expRank = -1;
                break;
            }
            expRank += rank - 1;
        }
        assertEquals("Experiment context rank", expRank, ctx.getRank());
    }

    @Test
    public void testGetNextAfteSeekingOnTS_1() {
        final long INITIAL_TS = 1;
        final int NB_READS = 20;

        // On lower bound, returns the first event (ts = 1)
        final ITmfContext context = fExperiment.seekEvent(TmfTimestamp.create(INITIAL_TS, SCALE));

        validateContextRanks(context);

        // Read NB_EVENTS
        ITmfEvent event;
        for (int i = 0; i < NB_READS; i++) {
            event = fExperiment.getNext(context);
            assertEquals("Event timestamp", INITIAL_TS + i, event.getTimestamp().getValue());
            assertEquals("Event rank", INITIAL_TS + i, context.getRank());
        }

        // Make sure we stay positioned
        event = fExperiment.parseEvent(context);
        assertEquals("Event timestamp", INITIAL_TS + NB_READS, event.getTimestamp().getValue());
        assertEquals("Event rank", INITIAL_TS + NB_READS - 1, context.getRank());

        validateContextRanks(context);
    }

    @Test
    public void testGetNextAfteSeekingOnTS_2() {
        final long INITIAL_TS = 2;
        final int NB_READS = 20;

        // On lower bound, returns the first event (ts = 2)
        final ITmfContext context = fExperiment.seekEvent(TmfTimestamp.create(INITIAL_TS, SCALE));

        validateContextRanks(context);

        // Read NB_EVENTS
        ITmfEvent event;
        for (int i = 0; i < NB_READS; i++) {
            event = fExperiment.getNext(context);
            assertEquals("Event timestamp", INITIAL_TS + i, event.getTimestamp().getValue());
            assertEquals("Event rank", INITIAL_TS + i, context.getRank());
        }

        // Make sure we stay positioned
        event = fExperiment.parseEvent(context);
        assertEquals("Event timestamp", INITIAL_TS + NB_READS, event.getTimestamp().getValue());
        assertEquals("Event rank", INITIAL_TS + NB_READS - 1, context.getRank());

        validateContextRanks(context);
    }

    @Test
    public void testGetNextAfteSeekingOnTS_3() {
        final long INITIAL_TS = 500;
        final int NB_READS = 20;

        // On lower bound, returns the first event (ts = 500)
        final ITmfContext context = fExperiment.seekEvent(TmfTimestamp.create(INITIAL_TS, SCALE));

        validateContextRanks(context);

        // Read NB_EVENTS
        ITmfEvent event;
        for (int i = 0; i < NB_READS; i++) {
            event = fExperiment.getNext(context);
            assertEquals("Event timestamp", INITIAL_TS + i, event.getTimestamp().getValue());
            assertEquals("Event rank", INITIAL_TS + i, context.getRank());
        }

        // Make sure we stay positioned
        event = fExperiment.parseEvent(context);
        assertEquals("Event timestamp", INITIAL_TS + NB_READS, event.getTimestamp().getValue());
        assertEquals("Event rank", INITIAL_TS + NB_READS - 1, context.getRank());

        validateContextRanks(context);
    }

    @Test
    public void testGetNextAfterSeekingOnRank_1() {
        final long INITIAL_RANK = 0L;
        final int NB_READS = 20;

        // On lower bound, returns the first event (rank = 0)
        final ITmfContext context = fExperiment.seekEvent(INITIAL_RANK);

        validateContextRanks(context);

        // Read NB_EVENTS
        ITmfEvent event;
        for (int i = 0; i < NB_READS; i++) {
            event = fExperiment.getNext(context);
            assertEquals("Event timestamp", INITIAL_RANK + i + 1, event.getTimestamp().getValue());
            assertEquals("Event rank", INITIAL_RANK + i + 1, context.getRank());
        }

        // Make sure we stay positioned
        event = fExperiment.parseEvent(context);
        assertEquals("Event timestamp", INITIAL_RANK + NB_READS + 1, event.getTimestamp().getValue());
        assertEquals("Event rank", INITIAL_RANK + NB_READS, context.getRank());

        validateContextRanks(context);
    }

    @Test
    public void testGetNextAfterSeekingOnRank_2() {
        final long INITIAL_RANK = 1L;
        final int NB_READS = 20;

        // On lower bound, returns the first event (rank = 0)
        final ITmfContext context = fExperiment.seekEvent(INITIAL_RANK);

        validateContextRanks(context);

        // Read NB_EVENTS
        ITmfEvent event;
        for (int i = 0; i < NB_READS; i++) {
            event = fExperiment.getNext(context);
            assertEquals("Event timestamp", INITIAL_RANK + i + 1, event.getTimestamp().getValue());
            assertEquals("Event rank", INITIAL_RANK + i + 1, context.getRank());
        }

        // Make sure we stay positioned
        event = fExperiment.parseEvent(context);
        assertEquals("Event timestamp", INITIAL_RANK + NB_READS + 1, event.getTimestamp().getValue());
        assertEquals("Event rank", INITIAL_RANK + NB_READS, context.getRank());

        validateContextRanks(context);
    }

    @Test
    public void testGetNextAfterSeekingOnRank_3() {
        final long INITIAL_RANK = 500L;
        final int NB_READS = 20;

        // On lower bound, returns the first event (rank = 0)
        final ITmfContext context = fExperiment.seekEvent(INITIAL_RANK);

        validateContextRanks(context);

        // Read NB_EVENTS
        ITmfEvent event;
        for (int i = 0; i < NB_READS; i++) {
            event = fExperiment.getNext(context);
            assertEquals("Event timestamp", INITIAL_RANK + i + 1, event.getTimestamp().getValue());
            assertEquals("Event rank", INITIAL_RANK + i + 1, context.getRank());
        }

        // Make sure we stay positioned
        event = fExperiment.parseEvent(context);
        assertEquals("Event timestamp", INITIAL_RANK + NB_READS + 1, event.getTimestamp().getValue());
        assertEquals("Event rank", INITIAL_RANK + NB_READS, context.getRank());

        validateContextRanks(context);
    }

    @Test
    public void testGetNextAfterSeekingOnLocation_1() {
        final ITmfLocation INITIAL_LOC = null;
        final long INITIAL_TS = 1;
        final int NB_READS = 20;

        // On lower bound, returns the first event (ts = 1)
        final ITmfContext context = fExperiment.seekEvent(INITIAL_LOC);

        validateContextRanks(context);

        // Read NB_EVENTS
        ITmfEvent event;
        for (int i = 0; i < NB_READS; i++) {
            event = fExperiment.getNext(context);
            assertEquals("Event timestamp", INITIAL_TS + i, event.getTimestamp().getValue());
            assertEquals("Event rank", INITIAL_TS + i, context.getRank());
        }

        // Make sure we stay positioned
        event = fExperiment.parseEvent(context);
        assertEquals("Event timestamp", INITIAL_TS + NB_READS, event.getTimestamp().getValue());
        assertEquals("Event rank", INITIAL_TS + NB_READS - 1, context.getRank());

        validateContextRanks(context);
    }

    @Test
    public void testGetNextAfterSeekingOnLocation_2() {
        final ITmfLocation INITIAL_LOC = fExperiment.seekEvent(1L).getLocation();
        final long INITIAL_TS = 2;
        final int NB_READS = 20;

        // On lower bound, returns the first event (ts = 2)
        final ITmfContext context = fExperiment.seekEvent(INITIAL_LOC);

        validateContextRanks(context);

        // Read NB_EVENTS
        ITmfEvent event;
        for (int i = 0; i < NB_READS; i++) {
            event = fExperiment.getNext(context);
            assertEquals("Event timestamp", INITIAL_TS + i, event.getTimestamp().getValue());
        }

        // Make sure we stay positioned
        event = fExperiment.parseEvent(context);
        assertEquals("Event timestamp", INITIAL_TS + NB_READS, event.getTimestamp().getValue());

        validateContextRanks(context);
    }

    @Test
    public void testGetNextAfterSeekingOnLocation_3() {
        final ITmfLocation INITIAL_LOC = fExperiment.seekEvent(500L).getLocation();
        final long INITIAL_TS = 501;
        final int NB_READS = 20;

        // On lower bound, returns the first event (ts = 501)
        final ITmfContext context = fExperiment.seekEvent(INITIAL_LOC);

        validateContextRanks(context);

        // Read NB_EVENTS
        ITmfEvent event;
        for (int i = 0; i < NB_READS; i++) {
            event = fExperiment.getNext(context);
            assertEquals("Event timestamp", INITIAL_TS + i, event.getTimestamp().getValue());
        }

        // Make sure we stay positioned
        event = fExperiment.parseEvent(context);
        assertEquals("Event timestamp", INITIAL_TS + NB_READS, event.getTimestamp().getValue());

        validateContextRanks(context);
    }

    @Test
    public void testGetNextLocation() {
        ITmfContext context1 = fExperiment.seekEvent(0);
        fExperiment.getNext(context1);
        ITmfLocation location = context1.getLocation();
        ITmfEvent event1 = fExperiment.getNext(context1);
        ITmfContext context2 = fExperiment.seekEvent(location);
        ITmfEvent event2 = fExperiment.getNext(context2);
        assertEquals("Event timestamp", event1.getTimestamp().getValue(), event2.getTimestamp().getValue());
    }

    @Test
    public void testGetNextEndLocation() {
        ITmfContext context1 = fExperiment.seekEvent(fExperiment.getNbEvents() - 1);
        fExperiment.getNext(context1);
        ITmfLocation location = context1.getLocation();
        ITmfContext context2 = fExperiment.seekEvent(location);
        ITmfEvent event = fExperiment.getNext(context2);
        assertNull("Event", event);
    }

    // ------------------------------------------------------------------------
    // processRequest
    // ------------------------------------------------------------------------

    @Test
    public void testProcessRequestForNbEvents() throws InterruptedException {
        final int nbEvents  = 1000;
        final Vector<ITmfEvent> requestedEvents = new Vector<>();

        final TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);
        final TmfEventRequest request = new TmfEventRequest(ITmfEvent.class,
                range, 0, nbEvents, ExecutionType.FOREGROUND) {
            @Override
            public void handleData(final ITmfEvent event) {
                super.handleData(event);
                requestedEvents.add(event);
            }
        };
        fExperiment.sendRequest(request);
        request.waitForCompletion();

        assertEquals("nbEvents", nbEvents, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub!
        for (int i = 0; i < nbEvents; i++) {
            assertEquals("Distinct events", i+1, requestedEvents.get(i).getTimestamp().getValue());
        }
    }

    @Test
    public void testProcessRequestForAllEvents() throws InterruptedException {
        final int nbEvents  = ITmfEventRequest.ALL_DATA;
        final Vector<ITmfEvent> requestedEvents = new Vector<>();
        final long nbExpectedEvents = NB_EVENTS;

        final TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);
        final TmfEventRequest request = new TmfEventRequest(ITmfEvent.class,
                range, 0, nbEvents, ExecutionType.FOREGROUND) {
            @Override
            public void handleData(final ITmfEvent event) {
                super.handleData(event);
                requestedEvents.add(event);
            }
        };
        fExperiment.sendRequest(request);
        request.waitForCompletion();

        assertEquals("nbEvents", nbExpectedEvents, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub!
        for (int i = 0; i < nbExpectedEvents; i++) {
            assertEquals("Distinct events", i+1, requestedEvents.get(i).getTimestamp().getValue());
        }
    }

    // ------------------------------------------------------------------------
    // cancel
    // ------------------------------------------------------------------------

    @Test
    public void testCancel() throws InterruptedException {
        final int nbEvents  = NB_EVENTS;
        final int limit = BLOCK_SIZE;
        final Vector<ITmfEvent> requestedEvents = new Vector<>();

        final TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);
        final TmfEventRequest request = new TmfEventRequest(ITmfEvent.class,
                range, 0, nbEvents, ExecutionType.FOREGROUND) {
            int nbRead = 0;

            @Override
            public void handleData(final ITmfEvent event) {
                super.handleData(event);
                requestedEvents.add(event);
                if (++nbRead == limit) {
                    cancel();
                }
            }
        };
        fExperiment.sendRequest(request);
        request.waitForCompletion();

        assertEquals("nbEvents",  limit, requestedEvents.size());
        assertTrue("isCompleted", request.isCompleted());
        assertTrue("isCancelled", request.isCancelled());
    }

    // ------------------------------------------------------------------------
    // getTimestamp
    // ------------------------------------------------------------------------

    @Test
    public void testGetTimestamp() {
        assertEquals("getTimestamp", TmfTimestamp.create(    1, (byte) -3), fExperiment.getTimestamp(    0));
        assertEquals("getTimestamp", TmfTimestamp.create(    2, (byte) -3), fExperiment.getTimestamp(    1));
        assertEquals("getTimestamp", TmfTimestamp.create(   11, (byte) -3), fExperiment.getTimestamp(   10));
        assertEquals("getTimestamp", TmfTimestamp.create(  101, (byte) -3), fExperiment.getTimestamp(  100));
        assertEquals("getTimestamp", TmfTimestamp.create( 1001, (byte) -3), fExperiment.getTimestamp( 1000));
        assertEquals("getTimestamp", TmfTimestamp.create( 2001, (byte) -3), fExperiment.getTimestamp( 2000));
        assertEquals("getTimestamp", TmfTimestamp.create( 2501, (byte) -3), fExperiment.getTimestamp( 2500));
        assertEquals("getTimestamp", TmfTimestamp.create(10000, (byte) -3), fExperiment.getTimestamp( 9999));
        assertEquals("getTimestamp", TmfTimestamp.create(20000, (byte) -3), fExperiment.getTimestamp(19999));
        assertNull("getTimestamp", fExperiment.getTimestamp(20000));
    }

    // ------------------------------------------------------------------------
    // getInitialRangeOffset, getCurrentRange, getCurrentTime
    // ------------------------------------------------------------------------

    @Test
    public void testDefaultCurrentTimeValues() {
        ITmfTrace[] traces = setupTraces();
        TmfExperimentStub exp = new TmfExperimentStub(EXPERIMENT, traces, BLOCK_SIZE);

        // verify initial values
        ITmfTimestamp initRange = TmfTimestamp.fromNanos(DEFAULT_INITIAL_OFFSET_VALUE);
        assertEquals("getInitialRangeOffset", initRange, exp.getInitialRangeOffset());

        exp.dispose();
    }

    @Test
    public void testInitialRangeOffset() {
        ITmfTrace[] traces = setupTraces();
        ((TmfTraceStub) traces[0]).setInitialRangeOffset(TmfTimestamp.fromMillis(5));
        ((TmfTraceStub) traces[1]).setInitialRangeOffset(TmfTimestamp.fromMillis(2));
        TmfExperimentStub exp = new TmfExperimentStub(EXPERIMENT, traces, BLOCK_SIZE);

        ITmfTimestamp initRange = TmfTimestamp.fromMillis(2);
        assertEquals("getInitialRangeOffset", initRange, exp.getInitialRangeOffset());

        exp.dispose();
    }

}
