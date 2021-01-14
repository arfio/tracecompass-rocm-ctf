/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Patrick Tasse - Initial API and implementation
 *     Bernd Hufmann - Updated for new parent class
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.event.lookup;

import java.util.Objects;

import org.eclipse.tracecompass.ctf.core.event.CTFCallsite;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfCallsite;
import org.eclipse.tracecompass.tmf.core.event.lookup.TmfCallsite;

/**
 * CTF TMF call site information for source code lookup.
 *
 * @author Patrick Tasse
 * @since 3.0
 */
public class CtfTmfCallsite extends TmfCallsite {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /** The event name. */
    private final String fEventName;

    /** The instruction pointer. */
    private final long fInstructionPointer;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Standard Constructor.
     *
     * @param callsite
     *            - a CTF call site
     */
    public CtfTmfCallsite(CTFCallsite callsite) {
        super(callsite.getFileName(), callsite.getLineNumber());
        fEventName = callsite.getEventName();
        fInstructionPointer = callsite.getIp();
    }

    /**
     * Constructor to wrap a ITmfCallsite
     *
     * @param callsite
     *            the callsite
     * @param eventName
     *            the event name
     * @since 4.2
     */
    public CtfTmfCallsite(ITmfCallsite callsite, String eventName) {
        super(callsite.getFileName(), callsite.getLineNo());
        fEventName = eventName;
        fInstructionPointer = 0;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Returns the event name of the call site.
     *
     * @return the event name
     */
    public String getEventName() {
        return fEventName;
    }

    /**
     * Returns the instruction pointer of the call site.
     *
     * @return the instruction pointer
     */
    public long getIntructionPointer() {
        return fInstructionPointer;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fEventName == null) ? 0 : fEventName.hashCode());
        result = prime * result + (int) (fInstructionPointer ^ (fInstructionPointer >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CtfTmfCallsite other = (CtfTmfCallsite) obj;
        if (!Objects.equals(fEventName, other.fEventName)) {
            return false;
        }
        return (fInstructionPointer == other.fInstructionPointer);
    }

    @Override
    public String toString() {
        return getEventName() + "@0x" + Long.toHexString(fInstructionPointer) + ": " + //$NON-NLS-1$ //$NON-NLS-2$
                getFileName() + ':' + String.valueOf(getLineNo()) + ' ' + getFileName() + "()"; //$NON-NLS-1$
    }
}
