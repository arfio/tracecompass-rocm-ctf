/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.statevalue;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A state value that contains no particular value. It is sometimes needed over
 * a "null" reference, since we avoid NPE's this way.
 *
 * It can also be read either as a String ("nullValue") or an Integer (-1).
 *
 * @version 1.0
 * @author Alexandre Montplaisir
 */
final class NullStateValue extends TmfStateValue {

    /**
     * Instance
     */
    public static final TmfStateValue INSTANCE = new NullStateValue();

    /**
     * Private constructor
     */
    private NullStateValue() {
        // Do nothing
    }

    private static final String VALUE = "nullValue"; //$NON-NLS-1$

    @Override
    public Type getType() {
        return Type.NULL;
    }

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        return (object instanceof NullStateValue);
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return VALUE;
    }

    // ------------------------------------------------------------------------
    // Unboxing methods. Null values can be unboxed into any type.
    // ------------------------------------------------------------------------

    @Override
    public int unboxInt() {
        return -1;
    }

    @Override
    public long unboxLong() {
        return -1;
    }

    @Override
    public double unboxDouble() {
        return Double.NaN;
    }

    @Override
    public String unboxStr() {
        return VALUE;
    }

    @Override
    public int compareTo(@Nullable ITmfStateValue other) {
        if (other == null) {
            throw new IllegalArgumentException();
        }
        if (other instanceof NullStateValue) {
            return 0;
        }
        /*
         * For every other state value type, we defer to how that type wants to be
         * compared against null values.
         */
        int result = Math.max(-100, Math.min(100, other.compareTo(this)));
        /*
         * Result is clamped between 100 and -100 so it is safe to invert it
         */
        return -result;
    }

    @Override
    public @Nullable Object unboxValue() {
        return null;
    }

}
