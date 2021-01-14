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
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.tracecompass.tmf.ctf.core.context.CtfLocationInfo;
import org.junit.Before;
import org.junit.Test;

/**
 * Collection of tests for the {@link CtfLocationInfo}
 *
 * @author alexmont
 */
public class CtfLocationDataTest {

    private CtfLocationInfo fixture;

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new CtfLocationInfo(1, 0);
    }

    /**
     * Test for the .getTimestamp() and .getIndex() methods
     */
    @Test
    public void testGetters() {
        long timestamp = fixture.getTimestamp();
        long index = fixture.getIndex();

        assertEquals(1, timestamp);
        assertEquals(0, index);
    }

    /**
     * Test for the .hashCode() method
     */
    @Test
    public void testHashCode() {
        int code = fixture.hashCode();
        assertEquals(962, code);
    }

    /**
     * Test for the .equals() method
     */
    @Test
    public void testEquals() {
        CtfLocationInfo same = new CtfLocationInfo(1, 0);
        CtfLocationInfo diff1 = new CtfLocationInfo(100, 0);
        CtfLocationInfo diff2 = new CtfLocationInfo(1, 10);

        assertTrue(fixture.equals(same));
        assertFalse(fixture.equals(diff1));
        assertFalse(fixture.equals(diff2));
    }

    /**
     * Test for the .compareTo() method
     */
    @Test
    public void testCompareTo() {
        CtfLocationInfo same = new CtfLocationInfo(1, 0);
        CtfLocationInfo smaller = new CtfLocationInfo(0, 0);
        CtfLocationInfo bigger1 = new CtfLocationInfo(1000, 500);
        CtfLocationInfo bigger2 = new CtfLocationInfo(1, 1);

        assertEquals(0, same.compareTo(fixture));
        assertEquals(-1, smaller.compareTo(fixture));
        assertEquals(1, bigger1.compareTo(fixture));
        assertEquals(1, bigger2.compareTo(fixture));
    }

    /**
     * Test for the .toString() method
     */
    @Test
    public void testToString() {
        String expected = "Element [1/0]";
        assertEquals(expected, fixture.toString());
    }
}
