/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.analysis;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Interface for all output types of analysis
 *
 * @author Geneviève Bastien
 */
public interface IAnalysisOutput {

    /**
     * Gets the name of the output
     *
     * @return Name of the output
     */
    String getName();

    /**
     * Requests the output for an analysis module. This function does not
     * necessarily output the analysis, it just specifies that the user wants
     * this output.
     */
    void requestOutput();

    /**
     * Sets an arbitrary property on the output. The key must not be null, a
     * <code>null</code> value removes the property.
     *
     * @param key
     *            The arbitrary property. Must not be null.
     * @param value
     *            The value of the property.
     * @param immediate
     *            If <code>true</code>, the property will be applied immediately
     *            if the output is active. Otherwise, it is only applied when the
     *            output is explicitly requested by the user.
     */
    void setOutputProperty(@NonNull String key, String value, boolean immediate);

    /**
     * Get the ID of the analysis output
     * @return the ID
     *
     * @since 6.1
     */
    @NonNull
    default String getId() {
        return ""; //$NON-NLS-1$
    }

}
