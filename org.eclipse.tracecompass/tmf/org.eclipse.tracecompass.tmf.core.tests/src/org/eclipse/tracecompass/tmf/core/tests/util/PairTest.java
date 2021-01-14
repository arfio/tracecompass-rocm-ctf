/*******************************************************************************
 * Copyright (c) 2011, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Bernd Hufmann - Initial design and implementation
 *  Alexandre Montplaisir - Port to JUnit4
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.tracecompass.tmf.core.util.Pair;
import org.junit.Test;

/**
 * Test case for Pair class.
 *
 * @author Bernd Hufmann
 */
@SuppressWarnings("javadoc")
public class PairTest {

    // ------------------------------------------------------------------------
    // Field(s)
    // ------------------------------------------------------------------------

    Pair<String, Long> fPair1 = new Pair<>("String 1", 1L);
    Pair<String, Long> fPair2 = new Pair<>("String 2", 2L);

    // ------------------------------------------------------------------------
    // to String
    // ------------------------------------------------------------------------

    @Test
    public void testToString() {
        String result = fPair1.toString();
        assertEquals("(String 1, 1)", result);
    }

    // ------------------------------------------------------------------------
    // Setters/Getters
    // ------------------------------------------------------------------------

    @Test
    public void testAccessors() {
        Pair<String, Long> myPair = new Pair<>("String 1", 1L);
        assertEquals("String 1", myPair.getFirst());
        assertEquals(Long.valueOf(1L), myPair.getSecond());
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    @Test
    public void testEqualsReflexivity() {
        assertTrue("equals", fPair1.equals(fPair1));
        assertTrue("equals", fPair2.equals(fPair2));

        assertTrue("equals", !fPair1.equals(fPair2));
        assertTrue("equals", !fPair2.equals(fPair1));
    }

    @Test
    public void testEqualsSymmetry() {
        Pair<String, Long> info1 = new Pair<>(fPair1.getFirst(), fPair1.getSecond());
        Pair<String, Long> info2 = new Pair<>(fPair2.getFirst(), fPair2.getSecond());

        assertTrue("equals", info1.equals(fPair1));
        assertTrue("equals", fPair1.equals(info1));

        assertTrue("equals", info2.equals(fPair2));
        assertTrue("equals", fPair2.equals(info2));
    }

    @Test
    public void testEqualsTransivity() {
        Pair<String, Long> info1 = new Pair<>(fPair1.getFirst(), fPair1.getSecond());
        Pair<String, Long> info2 = new Pair<>(fPair1.getFirst(), fPair1.getSecond());
        Pair<String, Long> info3 = new Pair<>(fPair1.getFirst(), fPair1.getSecond());

        assertTrue("equals", info1.equals(info2));
        assertTrue("equals", info2.equals(info3));
        assertTrue("equals", info1.equals(info3));
    }

    @Test
    public void testEqualsNull() {
        assertTrue("equals", !fPair1.equals(null));
        assertTrue("equals", !fPair2.equals(null));
    }

    @Test
    public void testEqualsDifferentObj() {
        Pair<Long, String> info = new Pair<>(1L, "String1");
        assertTrue("equals", !fPair1.equals(info));
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    @Test
    public void testHashCode() {
        Pair<String, Long> info1 = new Pair<>(fPair1.getFirst(), fPair1.getSecond());
        Pair<String, Long> info2 = new Pair<>(fPair2.getFirst(), fPair2.getSecond());

        assertTrue("hashCode", fPair1.hashCode() == info1.hashCode());
        assertTrue("hashCode", fPair2.hashCode() == info2.hashCode());

        assertTrue("hashCode", fPair1.hashCode() != info2.hashCode());
        assertTrue("hashCode", fPair2.hashCode() != info1.hashCode());
    }
}
