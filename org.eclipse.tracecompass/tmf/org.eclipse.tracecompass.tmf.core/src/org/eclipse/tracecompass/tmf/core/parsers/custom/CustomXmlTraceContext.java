/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.parsers.custom;

import org.eclipse.tracecompass.tmf.core.trace.TmfContext;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;

/**
 * Trace context for custom XML traces.
 *
 * @author Patrick Tassé
 */
public class CustomXmlTraceContext extends TmfContext {

    /**
     * Constructor
     *
     * @param location
     *            The location (in the file) of this context
     * @param rank
     *            The rank of the event pointed by this context
     */
    public CustomXmlTraceContext(ITmfLocation location, long rank) {
        super(location, rank);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        return ((obj instanceof CustomXmlTraceContext));
    }

}