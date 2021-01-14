/*******************************************************************************
 * Copyright (c) 2009, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Updated as per TMF Event Model 1.0
 *   Alexandre Montplaisir - Removed Cloneable, made immutable
 *   Patrick Tasse - Remove getSubField
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.ObjectUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

/**
 * A basic implementation of ITmfEventField.
 * <p>
 * Non-value fields are structural (i.e. used to represent the event structure
 * including optional fields) while the valued fields are actual event fields.
 *
 * @version 1.0
 * @author Francois Chouinard
 *
 * @see ITmfEvent
 * @see ITmfEventType
 */
public class TmfEventField implements ITmfEventField {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final @NonNull String fName;
    private final @Nullable Object fValue;
    private final @NonNull Map<String, ITmfEventField> fFields;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Full constructor
     *
     * @param name
     *            the event field id
     * @param value
     *            the event field value
     * @param fields
     *            the list of subfields
     * @throws IllegalArgumentException
     *             If 'name' is null, or if 'fields' has duplicate field names.
     */
    public TmfEventField(@NonNull String name, @Nullable Object value, ITmfEventField @Nullable [] fields) {
        fName = name;
        fValue = value;

        if (fields == null) {
            fFields = ImmutableMap.of();
        } else {
            ImmutableMap.Builder<String, ITmfEventField> mapBuilder = new ImmutableMap.Builder<>();
            Arrays.stream(fields).forEach(t -> mapBuilder.put(t.getName(), t));
            fFields = mapBuilder.build();
        }
    }

    /**
     * Copy constructor
     *
     * @param field the other event field
     */
    public TmfEventField(final TmfEventField field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        fName = field.fName;
        fValue = field.fValue;
        fFields = field.fFields;
    }

    // ------------------------------------------------------------------------
    // ITmfEventField
    // ------------------------------------------------------------------------

    @Override
    public String getName() {
        return fName;
    }

    @Override
    public Object getValue() {
        return fValue;
    }

    @Override
    public final Collection<String> getFieldNames() {
        return fFields.keySet();
    }

    @Override
    public final Collection<ITmfEventField> getFields() {
        return fFields.values();
    }

    @Override
    public ITmfEventField getField(final String... path) {
        if (path.length == 1) {
            return fFields.get(path[0]);
        }
        ITmfEventField field = this;
        for (String name : path) {
            field = field.getField(name);
            if (field == null) {
                return null;
            }
        }
        return field;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Create a root field from a list of labels.
     *
     * @param labels the list of labels
     * @return the (flat) root list
     */
    public static final @NonNull ITmfEventField makeRoot(final String[] labels) {
        final ITmfEventField[] fields = new ITmfEventField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            String label = checkNotNull(labels[i]);
            fields[i] = new TmfEventField(label, null, null);
        }
        // Return a new root field;
        return new TmfEventField(ITmfEventField.ROOT_FIELD_ID, null, fields);
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getName().hashCode();
        result = prime * result + ObjectUtils.deepHashCode(getValue());
        result = prime * result + fFields.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        /* We only consider equals fields of the exact same class. */
        if (!(this.getClass().equals(obj.getClass()))) {
            return false;
        }

        final TmfEventField other = (TmfEventField) obj;

        /* Check that the field names are the same. */
        if (!Objects.equals(getName(), other.getName())) {
            return false;
        }

        /*
         * Check that the field values are the same. We use ObjectUtils to
         * handle cases where the Object values may be primitive and/or nested
         * arrays.
         */
        if (!ObjectUtils.deepEquals(this.getValue(), other.getValue())) {
            return false;
        }

        /* Check that sub-fields are the same. */
        return (fFields.equals(other.fFields));
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        if (fName.equals(ITmfEventField.ROOT_FIELD_ID)) {
            /*
             * If this field is a top-level "field container", we will print its
             * sub-fields directly.
             */
            appendSubFields(ret);

        } else {
            /* The field has its own values */
            ret.append(fName);
            ret.append('=');
            ret.append(fValue);

            if (!fFields.isEmpty()) {
                /*
                 * In addition to its own name/value, this field also has
                 * sub-fields.
                 */
                ret.append(" ["); //$NON-NLS-1$
                appendSubFields(ret);
                ret.append(']');
            }
        }
        return ret.toString();
    }

    private void appendSubFields(StringBuilder sb) {
        Joiner joiner = Joiner.on(", ").skipNulls(); //$NON-NLS-1$
        sb.append(joiner.join(getFields()));
    }

    @Override
    public String getFormattedValue() {
        return getValue().toString();
    }

}
