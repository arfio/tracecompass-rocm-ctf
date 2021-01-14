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
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;

/**
 * A state value containing a variable-sized string
 *
 * @version 1.0
 * @author Alexandre Montplaisir
 */
final class StringStateValue extends TmfStateValue {

    private final String fValue;

    /**
     * String state value constructor
     *
     * @param valueAsString
     *            string
     */
    public StringStateValue(String valueAsString) {
        fValue = valueAsString;
    }

    @Override
    public Type getType() {
        return Type.STRING;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (!(object instanceof StringStateValue)) {
            return false;
        }
        StringStateValue other = (StringStateValue) object;
        return fValue.equals(other.fValue);
    }

    @Override
    public int hashCode() {
        return fValue.hashCode();
    }

    @Override
    public String toString() {
        return fValue;
    }

    // ------------------------------------------------------------------------
    // Unboxing methods
    // ------------------------------------------------------------------------

    @Override
    public String unboxStr() {
        return fValue;
    }

    @Override
    public int compareTo(@Nullable ITmfStateValue other) {
        if (other == null) {
            throw new IllegalArgumentException();
        }

        switch (other.getType()) {
        case DOUBLE:
            throw new StateValueTypeException("A String state value cannot be compared to a Double state value."); //$NON-NLS-1$
        case INTEGER:
            throw new StateValueTypeException("A String state value cannot be compared to an Integer state value."); //$NON-NLS-1$
        case LONG:
            throw new StateValueTypeException("A String state value cannot be compared to a Long state value."); //$NON-NLS-1$
        case NULL:
            /*
             * We assume that every string state value is greater than a null state value.
             */
            return 1;
        case STRING:
            StringStateValue otherStringValue = (StringStateValue) other;
            return fValue.compareTo(otherStringValue.fValue);
        case CUSTOM:
        default:
            throw new StateValueTypeException("A String state value cannot be compared to the type " + other.getType()); //$NON-NLS-1$
        }

    }

    @Override
    public @Nullable Object unboxValue() {
        return fValue;
    }

}
