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
 *   Matthew Khouzam - Initial generation with CodePro tools
 *   Alexandre Montplaisir - Clean up, consolidate redundant tests
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.ctf.core.context.CtfLocation;
import org.eclipse.tracecompass.tmf.ctf.core.context.CtfLocationInfo;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>CtfLocationTest</code> contains tests for the class
 * <code>{@link CtfLocation}</code>.
 *
 * @author ematkho
 * @version 1.0
 */
public class CtfLocationTest {

    private CtfLocation fixture;

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new CtfLocation(new CtfLocationInfo(1, 0));
    }

    /**
     * Run the CtfLocation(Long) constructor test.
     */
    @Test
    public void testCtfLocation_long() {
        CtfLocationInfo location = new CtfLocationInfo(1, 0);
        CtfLocation result = new CtfLocation(location);

        assertNotNull(result);
        assertEquals(1L, result.getLocationInfo().getTimestamp());
    }

    /**
     * Run the CtfLocation(ITmfTimestamp) constructor test.
     */
    @Test
    public void testCtfLocation_timestamp() {
        ITmfTimestamp timestamp = TmfTimestamp.fromSeconds(0);
        CtfLocation result = new CtfLocation(timestamp);

        assertNotNull(result);
        assertEquals(0L, result.getLocationInfo().getTimestamp());
    }

    /**
     * Run the Long getLocation() method test.
     */
    @Test
    public void testGetLocation() {
        CtfLocationInfo location = fixture.getLocationInfo();
        long result = location.getTimestamp();
        assertEquals(1L, result);
    }

    /**
     * Run the void setLocation(Long) method test.
     */
    @Test
    public void testSetLocation() {
        CtfLocationInfo location = new CtfLocationInfo(1337, 7331);
        fixture = new CtfLocation(location);
    }

    /**
     * Test the toString() method with a valid location.
     */
    @Test
    public void testToString_valid(){
        CtfLocation fixture2 = new CtfLocation(new CtfLocationInfo(1337, 7331));
        assertEquals("CtfLocation [fLocationInfo=Element [1337/7331]]", fixture2.toString());
    }

    /**
     * Test the toString() method with an invalid location.
     */
    @Test
    public void testToString_invalid(){
        CtfLocation fixture2 = new CtfLocation(new CtfLocationInfo(-1, -1));
        assertEquals("CtfLocation [INVALID]", fixture2.toString());
    }
}
