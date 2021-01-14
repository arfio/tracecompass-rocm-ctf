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

package org.eclipse.tracecompass.ctf.core.tests.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.types.FloatDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.FloatDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDefinition;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>IntegerDefinitionTest</code> contains tests for the class
 * <code>{@link IntegerDefinition}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
@SuppressWarnings("javadoc")
public class FloatDefinitionTest {

    private FloatDefinition fixture;
    private FloatDefinition singleFixture;
    private FloatDefinition doubleFixture; // all the way.
    private FloatDeclaration parent;
    @NonNull
    private static final String fieldName = "float";

    /**
     * Perform pre-test initialization.
     *
     * @throws CTFException
     *             error creating floats
     */
    @Before
    public void setUp() throws CTFException {
        testFloat248();
        testFloat5311();
    }

    @Test
    public void testFloat248() throws CTFException {
        parent = new FloatDeclaration(8, 24, ByteOrder.nativeOrder(), 0);
        BitBuffer bb = create32BitFloatByteBuffer();
        singleFixture = parent.createDefinition(null, fieldName, bb);
        assertNotNull(singleFixture);
    }

    @Test
    public void testFloat5311() throws CTFException {
        parent = new FloatDeclaration(11, 53, ByteOrder.nativeOrder(), 0);
        BitBuffer bb = create64BitFloatByteBuffer();
        doubleFixture = parent.createDefinition(null, fieldName, bb);
        assertNotNull(doubleFixture);
    }

    @Test
    public void testFloat32Bit() throws CTFException {
        for (int i = 1; i < 31; i++) {
            parent = new FloatDeclaration(i, 32 - i, ByteOrder.nativeOrder(), 0);

            fixture = parent.createDefinition(null, fieldName, create32BitFloatByteBuffer());
            assertNotNull(fixture);
            assertEquals("test" + i, "2.0", fixture.toString());
        }
    }

    @Test
    public void testFloat64Bit() throws CTFException {
        for (int i = 1; i < 63; i++) {
            parent = new FloatDeclaration(i, 64 - i, ByteOrder.nativeOrder(), 0);
            fixture = parent.createDefinition(null, fieldName, create64BitFloatByteBuffer());
            assertNotNull(fixture);
            if (i <= 32) {
                assertEquals("test" + i, "2.0", fixture.toString());
            } else if (i == 33) {
                assertEquals("test" + i, "1.0", fixture.toString());
            } else {
                assertNotNull(fixture.getValue());
            }

        }
    }

    @Test
    public void testFloat32BitNeg() throws CTFException {
        for (int i = 1; i < 31; i++) {
            parent = new FloatDeclaration(i, 32 - i, ByteOrder.nativeOrder(), 0);

            BitBuffer create32BitNegativeFloatByteBuffer = create32BitNegativeFloatByteBuffer();
            fixture = parent.createDefinition(null, fieldName, create32BitNegativeFloatByteBuffer);
            assertNotNull(fixture);
            assertEquals("test" + i, "-2.0", fixture.toString());
        }
    }

    @Test
    public void testFloat64BitNeg() throws CTFException {
        for (int i = 1; i < 63; i++) {
            parent = new FloatDeclaration(i, 64 - i, ByteOrder.nativeOrder(), 0);
            BitBuffer floatBuffer = create64BitNegativeFloatByteBuffer();
            fixture = parent.createDefinition(null, fieldName, floatBuffer);
            assertNotNull(fixture);
            if (i <= 32) {
                assertEquals("test" + i, "-2.0", fixture.toString());
            } else if (i == 33) {
                assertEquals("test" + i, "-1.0", fixture.toString());
            } else {
                assertNotNull(fixture.getValue());
            }
        }
    }

    @Test
    public void testFloat48Bit() throws CTFException {
        parent = new FloatDeclaration(12, 32, ByteOrder.nativeOrder(), 0);
        fixture = parent.createDefinition(null, fieldName, create64BitFloatByteBuffer());
        assertNotNull(fixture);
        assertEquals(Double.NaN, fixture.getValue(), 0.1);
    }

    /**
     * Run the IntegerDeclaration getDeclaration() method test.
     */
    @Test
    public void testGetDeclaration() {
        FloatDeclaration result = singleFixture.getDeclaration();
        assertNotNull(result);
    }

    /**
     * Run the long getValue() method test.
     */
    @Test
    public void testGetValue() {
        double result = singleFixture.getValue();
        assertEquals(2.0, result, 0.1);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        String result = singleFixture.toString();
        assertNotNull(result);
        assertEquals("2.0", result);
    }

    @NonNull
    private static BitBuffer create32BitFloatByteBuffer() {
        float[] data = new float[2];
        data[0] = 2.0f;
        data[1] = 3.14f;
        ByteBuffer byb = ByteBuffer.allocate(128);
        byb.order(ByteOrder.nativeOrder());
        byb.mark();
        byb.putFloat(data[0]);
        byb.putFloat(data[1]);
        byb.reset();
        BitBuffer bb = new BitBuffer(byb);
        return bb;
    }

    @NonNull
    private static BitBuffer create64BitFloatByteBuffer() {
        double[] data = new double[2];
        data[0] = 2.0f;
        data[1] = 3.14f;
        ByteBuffer byb = ByteBuffer.allocate(128);
        byb.order(ByteOrder.nativeOrder());
        byb.mark();
        byb.putDouble(data[0]);
        byb.putDouble(data[1]);
        byb.reset();
        BitBuffer bb = new BitBuffer(byb);
        return bb;
    }

    @NonNull
    private static BitBuffer create32BitNegativeFloatByteBuffer() {
        float[] data = new float[2];
        data[0] = -2.0f;
        data[1] = -3.14f;
        ByteBuffer byb = ByteBuffer.allocate(128);
        byb.order(ByteOrder.nativeOrder());
        byb.mark();
        byb.putFloat(data[0]);
        byb.putFloat(data[1]);
        byb.reset();
        BitBuffer bb = new BitBuffer(byb);
        return bb;
    }

    @NonNull
    private static BitBuffer create64BitNegativeFloatByteBuffer() {
        double[] data = new double[2];
        data[0] = -2.0f;
        data[1] = -3.14f;
        ByteBuffer byb = ByteBuffer.allocate(128);
        byb.order(ByteOrder.nativeOrder());
        byb.mark();
        byb.putDouble(data[0]);
        byb.putDouble(data[1]);
        byb.reset();
        BitBuffer bb = new BitBuffer(byb);
        return bb;
    }
}
