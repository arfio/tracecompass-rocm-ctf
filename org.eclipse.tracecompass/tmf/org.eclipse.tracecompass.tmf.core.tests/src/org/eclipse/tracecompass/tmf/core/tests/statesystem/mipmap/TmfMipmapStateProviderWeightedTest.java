/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Patrick Tasse - Updates to mipmap feature
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.statesystem.mipmap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.core.statesystem.mipmap.TmfStateSystemOperations;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.StateSystemFactory;
import org.eclipse.tracecompass.statesystem.core.backend.IStateHistoryBackend;
import org.eclipse.tracecompass.statesystem.core.backend.StateHistoryBackendFactory;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue.Type;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Patrick Tasse
 *
 */
public class TmfMipmapStateProviderWeightedTest {

    @NonNull private static final String SSID = "mipmap-test";
    private static final String TEST_ATTRIBUTE_NAME = TmfMipmapStateProviderStub.TEST_ATTRIBUTE_NAME;
    private static final long END_TIME = 250000L;
    private static final long INTERVAL = 1000L;
    private static final int RESOLUTION = 2;
    private static final double DELTA = 0.0001;
    private static ITmfStateSystemBuilder ssqi;
    private static ITmfStateSystemBuilder ssqd;

    /**
     * Startup code, build a state system with uneven state durations
     */
    @BeforeClass
    public static void init() {
        /* setup for INTEGER test */
        TmfMipmapStateProviderStub mmpi = new TmfMipmapStateProviderStub(RESOLUTION, Type.INTEGER);
        IStateHistoryBackend bei = StateHistoryBackendFactory.createInMemoryBackend(SSID, 0);
        ITmfStateSystemBuilder ssbi = StateSystemFactory.newStateSystem(bei);
        mmpi.assignTargetStateSystem(ssbi);
        ssqi = ssbi;
        /* setup for DOUBLE test */
        TmfMipmapStateProviderStub mmpd = new TmfMipmapStateProviderStub(RESOLUTION, Type.DOUBLE);
        IStateHistoryBackend bed = StateHistoryBackendFactory.createInMemoryBackend(SSID, 0);
        ITmfStateSystemBuilder ssbd = StateSystemFactory.newStateSystem(bed);
        mmpd.assignTargetStateSystem(ssbd);
        ssqd = ssbd;
        /*
         * Every 10,000 ns chunk contains the following states:
         *
         * | null |  10  | null |      20     | null |        30          | null |
         * 0     1000   2000   3000   4000   5000   6000   7000   8000   9000  10,000
         *
         * The weighted average for a chunk is (1 x 10 + 2 x 20 + 3 x 30) / 10 = 14.
         */
        for (int i = 0; i < END_TIME / INTERVAL / 10; i++) {
            long time = i * 10 * INTERVAL;
            /* update for INTEGER test */
            mmpi.processEvent(mmpi.createEvent(time, null));
            mmpi.processEvent(mmpi.createEvent(time + 1000, 10L));
            mmpi.processEvent(mmpi.createEvent(time + 2000, null));
            mmpi.processEvent(mmpi.createEvent(time + 3000, 20L));
            mmpi.processEvent(mmpi.createEvent(time + 5000, null));
            mmpi.processEvent(mmpi.createEvent(time + 6000, 30L));
            mmpi.processEvent(mmpi.createEvent(time + 9000, null));
            /* update for DOUBLE test */
            mmpd.processEvent(mmpd.createEvent(time, null));
            mmpd.processEvent(mmpd.createEvent(time + 1000, 10L));
            mmpd.processEvent(mmpd.createEvent(time + 2000, null));
            mmpd.processEvent(mmpd.createEvent(time + 3000, 20L));
            mmpd.processEvent(mmpd.createEvent(time + 5000, null));
            mmpd.processEvent(mmpd.createEvent(time + 6000, 30L));
            mmpd.processEvent(mmpd.createEvent(time + 9000, null));
        }
        /* cleanup for INTEGER test */
        mmpi.processEvent(mmpi.createEvent(END_TIME, 0L));
        mmpi.dispose();
        ssqi.waitUntilBuilt();
        /* cleanup for DOUBLE test */
        mmpd.processEvent(mmpd.createEvent(END_TIME, 0L));
        mmpd.dispose();
        ssqd.waitUntilBuilt();
    }

    /**
     * Test a query range to the state system to get the maximum value in the
     * range. The test values are INTEGER.
     */
    @Test
    public void testQueryMipmapRangeMaxInteger() {
        assertNotNull(ssqi);
        try {
            int quark = ssqi.getQuarkAbsolute(TEST_ATTRIBUTE_NAME);
            assertEquals(TmfStateValue.nullValue(), TmfStateSystemOperations.queryRangeMax(ssqi, 0, 0, quark));
            assertEquals(10, TmfStateSystemOperations.queryRangeMax(ssqi, 500, 1500, quark).unboxInt());
            assertEquals(20, TmfStateSystemOperations.queryRangeMax(ssqi, 1500, 5000, quark).unboxInt());
            assertEquals(30, TmfStateSystemOperations.queryRangeMax(ssqi, 5000, 10000, quark).unboxInt());
            assertEquals(30, TmfStateSystemOperations.queryRangeMax(ssqi, 0, 10000, quark).unboxInt());
            assertEquals(TmfStateValue.nullValue(), TmfStateSystemOperations.queryRangeMax(ssqi, 120000, 120000, quark));
            assertEquals(10, TmfStateSystemOperations.queryRangeMax(ssqi, 120500, 121500, quark).unboxInt());
            assertEquals(20, TmfStateSystemOperations.queryRangeMax(ssqi, 121500, 125000, quark).unboxInt());
            assertEquals(30, TmfStateSystemOperations.queryRangeMax(ssqi, 125000, 130000, quark).unboxInt());
            assertEquals(30, TmfStateSystemOperations.queryRangeMax(ssqi, 120000, 130000, quark).unboxInt());
            assertEquals(30, TmfStateSystemOperations.queryRangeMax(ssqi, 100000, 150000, quark).unboxInt());
            assertEquals(30, TmfStateSystemOperations.queryRangeMax(ssqi, 240000, 250000, quark).unboxInt());
            assertEquals(30, TmfStateSystemOperations.queryRangeMax(ssqi, 0, 250000, quark).unboxInt());
            assertEquals(00, TmfStateSystemOperations.queryRangeMax(ssqi, 250000, 250000, quark).unboxInt());

        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        } catch (StateValueTypeException e) {
            fail(e.getMessage());
        } catch (TimeRangeException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test a query range to the state system to get the minimum value in the
     * range. The test values are INTEGER.
     */
    @Test
    public void testQueryMipmapRangeMinInteger() {
        assertNotNull(ssqi);
        try {
            int quark = ssqi.getQuarkAbsolute(TEST_ATTRIBUTE_NAME);
            assertEquals(TmfStateValue.nullValue(), TmfStateSystemOperations.queryRangeMin(ssqi, 0, 0, quark));
            assertEquals(10, TmfStateSystemOperations.queryRangeMin(ssqi, 500, 1500, quark).unboxInt());
            assertEquals(10, TmfStateSystemOperations.queryRangeMin(ssqi, 1500, 5000, quark).unboxInt());
            assertEquals(30, TmfStateSystemOperations.queryRangeMin(ssqi, 5000, 10000, quark).unboxInt());
            assertEquals(10, TmfStateSystemOperations.queryRangeMin(ssqi, 0, 10000, quark).unboxInt());
            assertEquals(TmfStateValue.nullValue(), TmfStateSystemOperations.queryRangeMin(ssqi, 120000, 120000, quark));
            assertEquals(10, TmfStateSystemOperations.queryRangeMin(ssqi, 120500, 121500, quark).unboxInt());
            assertEquals(10, TmfStateSystemOperations.queryRangeMin(ssqi, 121500, 125000, quark).unboxInt());
            assertEquals(30, TmfStateSystemOperations.queryRangeMin(ssqi, 125000, 130000, quark).unboxInt());
            assertEquals(10, TmfStateSystemOperations.queryRangeMin(ssqi, 120000, 130000, quark).unboxInt());
            assertEquals(10, TmfStateSystemOperations.queryRangeMin(ssqi, 100000, 150000, quark).unboxInt());
            assertEquals(00, TmfStateSystemOperations.queryRangeMin(ssqi, 240000, 250000, quark).unboxInt());
            assertEquals(00, TmfStateSystemOperations.queryRangeMin(ssqi, 0, 250000, quark).unboxInt());
            assertEquals(00, TmfStateSystemOperations.queryRangeMin(ssqi, 250000, 250000, quark).unboxInt());

        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        } catch (StateValueTypeException e) {
            fail(e.getMessage());
        } catch (TimeRangeException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test a query range to the state system to get the average value in the
     * range. The test values are INTEGER.
     */
    @Test
    public void testQueryMipmapRangeAvgInteger() {
        assertNotNull(ssqi);
        try {
            int quark = ssqi.getQuarkAbsolute(TEST_ATTRIBUTE_NAME);
            assertEquals(0.0, TmfStateSystemOperations.queryRangeAverage(ssqi, 0, 0, quark), DELTA);
            assertEquals(5.0, TmfStateSystemOperations.queryRangeAverage(ssqi, 500, 1500, quark), DELTA);
            assertEquals(90.0 / 7, TmfStateSystemOperations.queryRangeAverage(ssqi, 1500, 5000, quark), DELTA);
            assertEquals(90.0 / 5, TmfStateSystemOperations.queryRangeAverage(ssqi, 5000, 10000, quark), DELTA);
            assertEquals(14.0, TmfStateSystemOperations.queryRangeAverage(ssqi, 0, 10000, quark), DELTA);
            assertEquals(14.0, TmfStateSystemOperations.queryRangeAverage(ssqi, 0, 20000, quark), DELTA);
            assertEquals(14.0, TmfStateSystemOperations.queryRangeAverage(ssqi, 500, 20500, quark), DELTA);
            assertEquals(14.0, TmfStateSystemOperations.queryRangeAverage(ssqi, 1000, 21000, quark), DELTA);
            assertEquals(14.0, TmfStateSystemOperations.queryRangeAverage(ssqi, 2000, 22000, quark), DELTA);
            assertEquals(14.0, TmfStateSystemOperations.queryRangeAverage(ssqi, 3000, 23000, quark), DELTA);
            assertEquals(14.0, TmfStateSystemOperations.queryRangeAverage(ssqi, 4000, 24000, quark), DELTA);
            assertEquals(14.0, TmfStateSystemOperations.queryRangeAverage(ssqi, 5000, 25000, quark), DELTA);
            assertEquals(0.0, TmfStateSystemOperations.queryRangeAverage(ssqi, 120000, 120000, quark), DELTA);
            assertEquals(5.0, TmfStateSystemOperations.queryRangeAverage(ssqi, 120500, 121500, quark), DELTA);
            assertEquals(90.0 / 7, TmfStateSystemOperations.queryRangeAverage(ssqi, 121500, 125000, quark), DELTA);
            assertEquals(90.0 / 5, TmfStateSystemOperations.queryRangeAverage(ssqi, 125000, 130000, quark), DELTA);
            assertEquals(14.0, TmfStateSystemOperations.queryRangeAverage(ssqi, 120000, 130000, quark), DELTA);
            assertEquals(14.0, TmfStateSystemOperations.queryRangeAverage(ssqi, 100000, 150000, quark), DELTA);
            assertEquals(14.0, TmfStateSystemOperations.queryRangeAverage(ssqi, 240000, 250000, quark), DELTA);
            assertEquals(14.0, TmfStateSystemOperations.queryRangeAverage(ssqi, 0, 250000, quark), DELTA);
            assertEquals(0.0, TmfStateSystemOperations.queryRangeAverage(ssqi, 250000, 250000, quark), DELTA);

        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        } catch (TimeRangeException e) {
            fail(e.getMessage());
        } catch (StateValueTypeException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test a query range to the state system to get the maximum value in the
     * range. The test values are DOUBLE.
     */
    @Test
    public void testQueryMipmapRangeMaxDouble() {
        assertNotNull(ssqd);
        try {
            int quark = ssqd.getQuarkAbsolute(TEST_ATTRIBUTE_NAME);
            assertEquals(TmfStateValue.nullValue(), TmfStateSystemOperations.queryRangeMax(ssqd, 0, 0, quark));
            assertEquals(10.0, TmfStateSystemOperations.queryRangeMax(ssqd, 500, 1500, quark).unboxDouble(), DELTA);
            assertEquals(20.0, TmfStateSystemOperations.queryRangeMax(ssqd, 1500, 5000, quark).unboxDouble(), DELTA);
            assertEquals(30.0, TmfStateSystemOperations.queryRangeMax(ssqd, 5000, 10000, quark).unboxDouble(), DELTA);
            assertEquals(30.0, TmfStateSystemOperations.queryRangeMax(ssqd, 0, 10000, quark).unboxDouble(), DELTA);
            assertEquals(TmfStateValue.nullValue(), TmfStateSystemOperations.queryRangeMax(ssqd, 120000, 120000, quark));
            assertEquals(10.0, TmfStateSystemOperations.queryRangeMax(ssqd, 120500, 121500, quark).unboxDouble(), DELTA);
            assertEquals(20.0, TmfStateSystemOperations.queryRangeMax(ssqd, 121500, 125000, quark).unboxDouble(), DELTA);
            assertEquals(30.0, TmfStateSystemOperations.queryRangeMax(ssqd, 125000, 130000, quark).unboxDouble(), DELTA);
            assertEquals(30.0, TmfStateSystemOperations.queryRangeMax(ssqd, 120000, 130000, quark).unboxDouble(), DELTA);
            assertEquals(30.0, TmfStateSystemOperations.queryRangeMax(ssqd, 100000, 150000, quark).unboxDouble(), DELTA);
            assertEquals(30.0, TmfStateSystemOperations.queryRangeMax(ssqd, 240000, 250000, quark).unboxDouble(), DELTA);
            assertEquals(30.0, TmfStateSystemOperations.queryRangeMax(ssqd, 0, 250000, quark).unboxDouble(), DELTA);
            assertEquals(00.0, TmfStateSystemOperations.queryRangeMax(ssqd, 250000, 250000, quark).unboxDouble(), DELTA);

        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        } catch (StateValueTypeException e) {
            fail(e.getMessage());
        } catch (TimeRangeException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test a query range to the state system to get the minimum value in the
     * range. The test values are DOUBLE.
     */
    @Test
    public void testQueryMipmapRangeMinDouble() {
        assertNotNull(ssqd);
        try {
            int quark = ssqd.getQuarkAbsolute(TEST_ATTRIBUTE_NAME);
            assertEquals(TmfStateValue.nullValue(), TmfStateSystemOperations.queryRangeMin(ssqd, 0, 0, quark));
            assertEquals(10.0, TmfStateSystemOperations.queryRangeMin(ssqd, 500, 1500, quark).unboxDouble(), DELTA);
            assertEquals(10.0, TmfStateSystemOperations.queryRangeMin(ssqd, 1500, 5000, quark).unboxDouble(), DELTA);
            assertEquals(30.0, TmfStateSystemOperations.queryRangeMin(ssqd, 5000, 10000, quark).unboxDouble(), DELTA);
            assertEquals(10.0, TmfStateSystemOperations.queryRangeMin(ssqd, 0, 10000, quark).unboxDouble(), DELTA);
            assertEquals(TmfStateValue.nullValue(), TmfStateSystemOperations.queryRangeMin(ssqd, 120000, 120000, quark));
            assertEquals(10.0, TmfStateSystemOperations.queryRangeMin(ssqd, 120500, 121500, quark).unboxDouble(), DELTA);
            assertEquals(10.0, TmfStateSystemOperations.queryRangeMin(ssqd, 121500, 125000, quark).unboxDouble(), DELTA);
            assertEquals(30.0, TmfStateSystemOperations.queryRangeMin(ssqd, 125000, 130000, quark).unboxDouble(), DELTA);
            assertEquals(10.0, TmfStateSystemOperations.queryRangeMin(ssqd, 120000, 130000, quark).unboxDouble(), DELTA);
            assertEquals(10.0, TmfStateSystemOperations.queryRangeMin(ssqd, 100000, 150000, quark).unboxDouble(), DELTA);
            assertEquals(00.0, TmfStateSystemOperations.queryRangeMin(ssqd, 240000, 250000, quark).unboxDouble(), DELTA);
            assertEquals(00.0, TmfStateSystemOperations.queryRangeMin(ssqd, 0, 250000, quark).unboxDouble(), DELTA);
            assertEquals(00.0, TmfStateSystemOperations.queryRangeMin(ssqd, 250000, 250000, quark).unboxDouble(), DELTA);

        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        } catch (StateValueTypeException e) {
            fail(e.getMessage());
        } catch (TimeRangeException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test a query range to the state system to get the average value in the
     * range. The test values are DOUBLE.
     */
    @Test
    public void testQueryMipmapRangeAvgDouble() {
        assertNotNull(ssqd);
        try {
            int quark = ssqd.getQuarkAbsolute(TEST_ATTRIBUTE_NAME);
            assertEquals(0.0, TmfStateSystemOperations.queryRangeAverage(ssqd, 0, 0, quark), DELTA);
            assertEquals(5.0, TmfStateSystemOperations.queryRangeAverage(ssqd, 500, 1500, quark), DELTA);
            assertEquals(90.0 / 7, TmfStateSystemOperations.queryRangeAverage(ssqd, 1500, 5000, quark), DELTA);
            assertEquals(90.0 / 5, TmfStateSystemOperations.queryRangeAverage(ssqd, 5000, 10000, quark), DELTA);
            assertEquals(14.0, TmfStateSystemOperations.queryRangeAverage(ssqd, 0, 10000, quark), DELTA);
            assertEquals(14.0, TmfStateSystemOperations.queryRangeAverage(ssqd, 0, 20000, quark), DELTA);
            assertEquals(14.0, TmfStateSystemOperations.queryRangeAverage(ssqd, 500, 20500, quark), DELTA);
            assertEquals(14.0, TmfStateSystemOperations.queryRangeAverage(ssqd, 1000, 21000, quark), DELTA);
            assertEquals(14.0, TmfStateSystemOperations.queryRangeAverage(ssqd, 2000, 22000, quark), DELTA);
            assertEquals(14.0, TmfStateSystemOperations.queryRangeAverage(ssqd, 3000, 23000, quark), DELTA);
            assertEquals(14.0, TmfStateSystemOperations.queryRangeAverage(ssqd, 4000, 24000, quark), DELTA);
            assertEquals(14.0, TmfStateSystemOperations.queryRangeAverage(ssqd, 5000, 25000, quark), DELTA);
            assertEquals(0.0, TmfStateSystemOperations.queryRangeAverage(ssqd, 120000, 120000, quark), DELTA);
            assertEquals(5.0, TmfStateSystemOperations.queryRangeAverage(ssqd, 120500, 121500, quark), DELTA);
            assertEquals(90.0 / 7, TmfStateSystemOperations.queryRangeAverage(ssqd, 121500, 125000, quark), DELTA);
            assertEquals(90.0 / 5, TmfStateSystemOperations.queryRangeAverage(ssqd, 125000, 130000, quark), DELTA);
            assertEquals(14.0, TmfStateSystemOperations.queryRangeAverage(ssqd, 120000, 130000, quark), DELTA);
            assertEquals(14.0, TmfStateSystemOperations.queryRangeAverage(ssqd, 100000, 150000, quark), DELTA);
            assertEquals(14.0, TmfStateSystemOperations.queryRangeAverage(ssqd, 240000, 250000, quark), DELTA);
            assertEquals(14.0, TmfStateSystemOperations.queryRangeAverage(ssqd, 0, 250000, quark), DELTA);
            assertEquals(0.0, TmfStateSystemOperations.queryRangeAverage(ssqd, 250000, 250000, quark), DELTA);

        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        } catch (TimeRangeException e) {
            fail(e.getMessage());
        } catch (StateValueTypeException e) {
            fail(e.getMessage());
        }
    }
}
