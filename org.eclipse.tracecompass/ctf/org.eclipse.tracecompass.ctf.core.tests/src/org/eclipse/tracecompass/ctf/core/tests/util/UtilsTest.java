/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.internal.ctf.core.utils.Utils;
import org.junit.Test;

/**
 * The class <code>UtilsTest</code> contains tests for the class
 * {@link Utils}.
 *
 * @author Matthew Khouzam
 */
public class UtilsTest {

    /**
     * Run the UUID makeUUID(byte[]) method test.
     *  @throws CTFException
     *             the data is not the right size
     */
    @Test
    public void testMakeUUID() throws CTFException {
        int byteSize = 32;
        byte[] bytes = new byte[byteSize];
        for (int i = 0; i < byteSize; i++) {
            bytes[i] = (byte) (i);
        }

        UUID result = Utils.makeUUID(bytes);
        assertNotNull(result);
    }

    /**
     * Run the UUID makeUUID(byte[]) method test.
     *
     * @throws CTFException
     *             the data is not the right size
     */
    @Test
    public void testMakeUUID_2() throws CTFException {
        byte[] bytes = new byte[] { (byte) 1, (byte) 1, (byte) 0, (byte) 0,
                (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 1,
                (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0 };

        UUID result = Utils.makeUUID(bytes);

        assertNotNull(result);
        assertEquals(72339069014638592L, result.getLeastSignificantBits());
        assertEquals(72339069014638592L, result.getMostSignificantBits());
        assertEquals("01010000-0000-0000-0101-000000000000", result.toString());
        assertEquals(0, result.variant());
        assertEquals(0, result.version());
    }

    /**
     * Run the UUID makeUUID(byte[]) method test.
     *
     * @throws CTFException
     *             the data is not the right size
     */
    @Test
    public void testMakeUUID_3() throws CTFException {
        byte[] bytes = new byte[] { (byte) 0, (byte) 0, (byte) 0, (byte) 0,
                (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
                (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0 };

        UUID result = Utils.makeUUID(bytes);

        assertNotNull(result);
        assertEquals(0L, result.getLeastSignificantBits());
        assertEquals(0L, result.getMostSignificantBits());
        assertEquals("00000000-0000-0000-0000-000000000000", result.toString());
        assertEquals(0, result.variant());
        assertEquals(0, result.version());
    }

    /**
     * Run the int unsignedCompare(long,long) method test.
     */
    @Test
    public void testUnsignedCompare() {
        long a = 1L;
        long b = 1L;
        int result;

        result = Utils.unsignedCompare(a, b);
        assertEquals(0, result);

        result = Utils.unsignedCompare(0L, 1L);
        assertEquals(-1, result);
        result = Utils.unsignedCompare(0xFFFFFFFFL, 0x100000000L);
        assertEquals(-1, result);
        result = Utils.unsignedCompare(-4L, -1L);
        assertEquals(-1, result);
        result = Utils.unsignedCompare(-0x80000000L, -1L);
        assertEquals(-1, result);
        result = Utils.unsignedCompare(0x7FFFFFFFFFFFFFFEL, 0x7FFFFFFFFFFFFFFFL);
        assertEquals(-1, result);

        result = Utils.unsignedCompare(1L, 0L);
        assertEquals(1, result);
        result = Utils.unsignedCompare(0x100000000L, 0xFFFFFFFFL);
        assertEquals(1, result);
        result = Utils.unsignedCompare(-1L, -4L);
        assertEquals(1, result);
        result = Utils.unsignedCompare(-1L, -0x80000000L);
        assertEquals(1, result);
        result = Utils.unsignedCompare(0x7FFFFFFFFFFFFFFFL, 0x7FFFFFFFFFFFFFFEL);
        assertEquals(1, result);
    }
}