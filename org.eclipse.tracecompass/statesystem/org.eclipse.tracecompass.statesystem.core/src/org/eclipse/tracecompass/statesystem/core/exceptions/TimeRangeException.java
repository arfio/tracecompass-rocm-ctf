/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
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
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Patrick Tasse - Add message to exceptions
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.exceptions;

/**
 * Generic exception for when the user specifies an invalid time stamp. Usually
 * timestamps must be within the range of the trace or state history being
 * queried.
 *
 * For insertions, it's forbidden to insert new states "in the past" (before where
 * the cursor is), so this exception is also thrown in that case.
 *
 * @author Alexandre Montplaisir
 */
public class TimeRangeException extends RuntimeException {

    private static final long serialVersionUID = -4067685227260254532L;

    /**
     * Default constructor
     */
    public TimeRangeException() {
    }

    /**
     * Constructor with a message
     *
     * @param message
     *            Message to attach to this exception
     * @since 1.0
     */
    public TimeRangeException(String message) {
        super(message);
    }

    /**
     * Constructor with both a message and a cause.
     *
     * @param message
     *            Message to attach to this exception
     * @param e
     *            Cause of this exception
     * @since 1.0
     */
    public TimeRangeException(String message, Throwable e) {
        super(message, e);
    }
}
