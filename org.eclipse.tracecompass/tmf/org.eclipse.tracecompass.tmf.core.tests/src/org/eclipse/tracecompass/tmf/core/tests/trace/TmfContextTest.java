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
 *   Francois Chouinard - Adapted for TMF Trace Model 1.0
 *   Alexandre Montplaisir - Port to JUnit4
 *   Patrick Tasse - Updated for removal of context clone
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfContext;
import org.eclipse.tracecompass.tmf.core.trace.location.TmfLongLocation;
import org.eclipse.tracecompass.tmf.core.trace.location.TmfTimestampLocation;
import org.junit.Test;

/**
 * Test suite for the TmfContext class.
 */
@SuppressWarnings("javadoc")
public class TmfContextTest {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private final Long aLong = 12345L;
    private final ITmfTimestamp aTimestamp = TmfTimestamp.fromSeconds(0);

    private final TmfLongLocation fLocation1 = new TmfLongLocation(aLong);
    private final TmfTimestampLocation fLocation2 = new TmfTimestampLocation(aTimestamp);

    private final long fRank1 = 1;
    private final long fRank2 = 2;

    private final TmfContext fContext1 = new TmfContext(fLocation1, fRank1);
    private final TmfContext fContext2 = new TmfContext(fLocation2, fRank2);

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    @Test
    public void testTmfContextDefault() {
        final TmfContext context = new TmfContext();
        assertEquals("getLocation", null, context.getLocation());
        assertEquals("getRank", ITmfContext.UNKNOWN_RANK, context.getRank());
    }

    @Test
    public void testTmfContextNoRank() {
        final TmfContext context1 = new TmfContext(fLocation1);
        final TmfContext context2 = new TmfContext(fLocation2);

        assertEquals("getLocation", fLocation1, context1.getLocation());
        assertEquals("getLocation", fLocation2, context2.getLocation());

        assertEquals("getRank", ITmfContext.UNKNOWN_RANK, context1.getRank());
        assertEquals("getRank", ITmfContext.UNKNOWN_RANK, context2.getRank());
    }

    @Test
    public void testTmfContext() {
        assertEquals("getLocation", fLocation1, fContext1.getLocation());
        assertEquals("getLocation", fLocation2, fContext2.getLocation());

        assertEquals("getRank", fRank1, fContext1.getRank());
        assertEquals("getRank", fRank2, fContext2.getRank());
    }

    @Test
    public void testTmfContextCopy() {
        final TmfContext context1 = new TmfContext(fContext1);
        final TmfContext context2 = new TmfContext(fContext2);

        assertEquals("getLocation", fLocation1, context1.getLocation());
        assertEquals("getLocation", fLocation2, context2.getLocation());

        assertEquals("getRank", fRank1, context1.getRank());
        assertEquals("getRank", fRank2, context2.getRank());
    }

    @Test
    public void testTmfContextCopy2() {
        try {
            new TmfContext((TmfContext) null);
            fail("Copy constructor: no exception");
        }
        catch (final IllegalArgumentException e) {
            // pass
        }
        catch (final Exception e) {
            fail("Copy constructor: wrong exception");
        }
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    @Test
    public void testEqualsReflexivity() {
        assertTrue("equals", fContext1.equals(fContext1));
        assertTrue("equals", fContext2.equals(fContext2));

        assertFalse("equals", fContext1.equals(fContext2));
        assertFalse("equals", fContext2.equals(fContext1));
    }

    @Test
    public void testEqualsSymmetry() {
        final TmfContext context1 = new TmfContext(fContext1);
        final TmfContext context2 = new TmfContext(fContext2);

        assertTrue("equals", context1.equals(fContext1));
        assertTrue("equals", fContext1.equals(context1));

        assertTrue("equals", context2.equals(fContext2));
        assertTrue("equals", fContext2.equals(context2));
    }

    @Test
    public void testEqualsTransivity() {
        final TmfContext context1 = new TmfContext(fContext1);
        final TmfContext context2 = new TmfContext(context1);
        final TmfContext context3 = new TmfContext(context2);

        assertTrue("equals", context1.equals(context2));
        assertTrue("equals", context2.equals(context3));
        assertTrue("equals", context1.equals(context3));
    }

    @Test
    public void testEqualsNull() {
        assertFalse("equals", fContext1.equals(null));
        assertFalse("equals", fContext2.equals(null));
    }

    private static class MyContext extends TmfContext {
    }

    @Test
    public void testNonEquals() {

        // Different classes
        final MyContext myContext = new MyContext();
        assertFalse("equals", fContext1.equals(myContext));
        assertFalse("equals", myContext.equals(fContext1));

        // Different locations
        TmfContext context1 = new TmfContext(fContext1);
        TmfContext context2 = new TmfContext(fContext1);
        context1.setLocation(null);
        context2.setLocation(null);

        assertFalse("equals", fContext1.equals(context1));
        assertFalse("equals", context1.equals(fContext1));
        assertTrue("equals", context1.equals(context2));

        // Different ranks
        context1 = new TmfContext(fContext1);
        context2 = new TmfContext(fContext1);
        context1.setRank(fContext1.getRank() + 1);
        context2.setRank(fContext1.getRank() + 2);

        assertFalse("equals", fContext1.equals(context1));
        assertFalse("equals", context1.equals(fContext1));
        assertFalse("equals", context1.equals(context2));
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    @Test
    public void testHashCode() {
        final TmfContext context1 = new TmfContext(fContext1);
        final TmfContext context2 = new TmfContext(fContext2);

        assertEquals("hashCode", fContext1.hashCode(), context1.hashCode());
        assertEquals("hashCode", fContext2.hashCode(), context2.hashCode());

        assertFalse("hashCode", fContext1.hashCode() == context2.hashCode());
        assertFalse("hashCode", fContext2.hashCode() == context1.hashCode());

        final TmfContext nullContext1 = new TmfContext();
        final TmfContext nullContext2 = new TmfContext(nullContext1);
        assertEquals("hashCode", nullContext1.hashCode(), nullContext2.hashCode());
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    @Test
    public void testToString() {
        final String expected1 = "TmfContext [fLocation=" + fLocation1 + ", fRank=" + fRank1 + "]";
        final String expected2 = "TmfContext [fLocation=" + fLocation2 + ", fRank=" + fRank2 + "]";

        assertEquals("toString", expected1, fContext1.toString());
        assertEquals("toString", expected2, fContext2.toString());
    }

    // ------------------------------------------------------------------------
    // setLocation, setRank, updateRank
    // ------------------------------------------------------------------------

    @Test
    public void testSetLocation() {
        final TmfContext context1 = new TmfContext(fContext1);
        context1.setLocation(fContext2.getLocation());

        assertEquals("getLocation", fLocation2, context1.getLocation());
        assertEquals("getRank", fRank1, context1.getRank());
    }

    @Test
    public void testSetRank() {
        final TmfContext context1 = new TmfContext(fContext1);
        context1.setRank(fContext2.getRank());

        assertEquals("getLocation", fLocation1, context1.getLocation());
        assertEquals("getRank", fRank2, context1.getRank());
    }

    @Test
    public void testIncreaseRank() {
        final TmfContext context1 = new TmfContext(fContext1);

        context1.increaseRank();
        assertEquals("getRank", fRank1 + 1, context1.getRank());
        context1.increaseRank();
        assertEquals("getRank", fRank1 + 2, context1.getRank());

        context1.setRank(ITmfContext.UNKNOWN_RANK);
        context1.increaseRank();
        assertEquals("getRank", ITmfContext.UNKNOWN_RANK, context1.getRank());
        context1.increaseRank();
        assertEquals("getRank", ITmfContext.UNKNOWN_RANK, context1.getRank());
    }

}
