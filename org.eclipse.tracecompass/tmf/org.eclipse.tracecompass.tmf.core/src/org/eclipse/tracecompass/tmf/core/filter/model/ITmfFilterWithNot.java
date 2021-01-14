/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.core.filter.model;

/**
 * Interface to leverage commonalities with filters that can be negated.
 *
 * @author Matthew Khouzam
 * @since 5.1
 */
public interface ITmfFilterWithNot {

    /**
     * not attribute name, for serialization
     */
    String NOT_ATTRIBUTE = "not"; //$NON-NLS-1$

    /**
     * Gets the not state
     *
     * @return the equals not state
     */
    boolean isNot();

    /**
     * Sets the current not state
     *
     * @param not
     *            the not state
     */
    void setNot(boolean not);
}
