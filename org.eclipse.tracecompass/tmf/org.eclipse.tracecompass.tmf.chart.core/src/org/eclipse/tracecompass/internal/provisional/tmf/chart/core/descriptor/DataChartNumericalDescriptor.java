/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.INumericalResolver;

/**
 * Abstract class for describing numbers from a stream of objects it
 * understands.
 *
 * @param <T>
 *            The type of the input it understands
 * @param <R>
 *            The type of the output number it describes
 *
 * @see IDataChartDescriptor
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class DataChartNumericalDescriptor<T, R extends Number> implements IDataChartDescriptor<T, R> {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final String fName;
    private final INumericalResolver<T, R> fResolver;

    private final @Nullable String fUnit;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param name
     *            The name of the descriptor
     * @param resolver
     *            The resolver used for mapping numbers
     */
    public DataChartNumericalDescriptor(String name, INumericalResolver<T, R> resolver) {
        this(name, resolver, null);
    }

    /**
     * Constructor.
     *
     * @param name
     *            The name of the descriptor
     * @param resolver
     *            The resolver used for mapping numbers
     * @param unit
     *            The unit of this descriptor, eg. s, ms, ns
     */
    public DataChartNumericalDescriptor(String name, INumericalResolver<T, R> resolver, @Nullable String unit) {
        fName = name;
        fResolver = resolver;
        fUnit = unit;
    }

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    public void accept(IDescriptorVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public INumericalResolver<T, R> getResolver() {
        return fResolver;
    }

    @Override
    public String getName() {
        return fName;
    }

    @Override
    public @Nullable String getUnit() {
        return fUnit;
    }

    @Override
    public String toString() {
        return "Numerical Descriptor: " + getName(); //$NON-NLS-1$
    }

}
