/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event.types;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;

/**
 * A CTF float definition.
 *
 * The definition of a floating point basic data type. It will take the data
 * from a trace and store it (and make it fit) as a double.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public final class FloatDefinition extends Definition {
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final double fValue;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param declaration
     *            the parent declaration
     * @param definitionScope
     *            the parent scope
     * @param fieldName
     *            the field name
     * @param value
     *            field value
     */
    public FloatDefinition(@NonNull FloatDeclaration declaration,
            IDefinitionScope definitionScope, @NonNull String fieldName, double value) {
        super(declaration, definitionScope, fieldName);
        fValue = value;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * The value of a float stored, fit into a double. This should be extended
     * for exotic floats if this is necessary.
     *
     * @return the value of the float field fit into a double.
     */
    public double getValue() {
        return fValue;
    }

    @Override
    public FloatDeclaration getDeclaration() {
        return (FloatDeclaration) super.getDeclaration();
    }

    @Override
    public long size() {
        return getDeclaration().getMaximumSize();
    }
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        return String.valueOf(fValue);
    }
}
