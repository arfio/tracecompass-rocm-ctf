/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/

package org.eclipse.tracecompass.internal.lttng2.control.core.model.impl;

import org.eclipse.tracecompass.internal.lttng2.control.core.model.IEventInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IProbeEventInfo;

/**
 * Implementation of the trace event interface (IProbeEventInfo) to store probe
 * event related data.
 *
 * @author Bernd Hufmann
 */
public class ProbeEventInfo extends EventInfo implements IProbeEventInfo {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The dynamic probe address (null if symbol is used).
     */
    private String fAddress;
    /**
     * The dynamic probe offset (if symbol is used).
     */
    private String fOffset;

    /**
     * The symbol name (null if address is used)
     */
    private String fSymbol;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param name
     *            - name of event
     */
    public ProbeEventInfo(String name) {
        super(name);
    }

    /**
     * Copy constructor
     *
     * @param other
     *            - the instance to copy
     */
    public ProbeEventInfo(ProbeEventInfo other) {
        super(other);
        fAddress = other.fAddress;
        fOffset = other.fOffset;
        fSymbol = other.fSymbol;
    }

    /**
     * Constructor from a {@link IEventInfo}
     *
     * @param eventInfo
     *            - the instance to copy
     */
    public ProbeEventInfo(IEventInfo eventInfo) {
        super(eventInfo.getName());
        setState(eventInfo.getState());
        setLogLevelType(eventInfo.getLogLevelType());
        setLogLevel(eventInfo.getLogLevel());
        setFilterExpression(eventInfo.getFilterExpression());
        setExcludedEvents(eventInfo.getExcludedEvents());
        setEventType(eventInfo.getEventType());
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public String getAddress() {
        return fAddress;
    }

    @Override
    public void setAddress(String address) {
        fAddress = address;
    }

    @Override
    public String getOffset() {
        return fOffset;
    }

    @Override
    public void setOffset(String offset) {
        fOffset = offset;
    }

    @Override
    public String getSymbol() {
        return fSymbol;
    }

    @Override
    public void setSymbol(String symbol) {
        fSymbol = symbol;
    }

    @Override
    public String getProbeString() {
        if (fAddress != null) {
            return fAddress;
        } else if (fSymbol != null) {
            StringBuffer buffer = new StringBuffer(fSymbol);
            if (fOffset != null) {
                buffer.append('+').append(fOffset);
            }
            return buffer.toString();
        }
        return null;
    }

    // ------------------------------------------------------------------------
    // Operation
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fAddress == null) ? 0 : fAddress.hashCode());
        result = prime * result + ((fOffset == null) ? 0 : fOffset.hashCode());
        result = prime * result + ((fSymbol == null) ? 0 : fSymbol.hashCode());
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
        ProbeEventInfo other = (ProbeEventInfo) obj;
        if (fAddress == null) {
            if (other.fAddress != null) {
                return false;
            }
        } else if (!fAddress.equals(other.fAddress)) {
            return false;
        }
        if (fOffset == null) {
            if (other.fOffset != null) {
                return false;
            }
        } else if (!fOffset.equals(other.fOffset)) {
            return false;
        }
        if (fSymbol == null) {
            if (other.fSymbol != null) {
                return false;
            }
        } else if (!fSymbol.equals(other.fSymbol)) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        StringBuffer output = new StringBuffer();
        output.append("[ProbeEventInfo(");
        output.append(super.toString());
        if (fAddress != null) {
            output.append(",fAddress=");
            output.append(fAddress);
        } else {
            output.append(",fOffset=");
            output.append(fOffset);
            output.append(",fSymbol=");
            output.append(fSymbol);
        }
        output.append(")]");
        return output.toString();
    }

}
