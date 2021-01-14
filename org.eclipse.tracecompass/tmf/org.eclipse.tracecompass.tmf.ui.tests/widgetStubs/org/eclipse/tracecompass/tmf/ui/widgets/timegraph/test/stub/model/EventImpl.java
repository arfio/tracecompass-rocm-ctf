/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.test.stub.model;

import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;

/**
 * ITimeEvent implementation for test purposes.
 */
@SuppressWarnings("javadoc")
public class EventImpl implements ITimeEvent {
    // ========================================================================
    // Data
    // ========================================================================
    public static enum Type {ERROR, WARNING, TIMEADJUSTMENT, ALARM, EVENT, INFORMATION, UNKNOWN, INFO1, INFO2, INFO3, INFO4, INFO5, INFO6, INFO7, INFO8, INFO9}

    private long time = 0;
    private ITimeGraphEntry trace = null;
    private Type myType = Type.UNKNOWN;
    private long duration;

    // ========================================================================
    // Constructor
    // ========================================================================
    public EventImpl(long time, ITimeGraphEntry trace, Type type) {
        this.time = time;
        this.trace = trace;
        this.myType = type;
    }

    // ========================================================================
    // Methods
    // ========================================================================
    public Type getType() {
        return myType;
    }

    public void setType(Type myType) {
        this.myType = myType;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setTrace(ITimeGraphEntry trace) {
        this.trace = trace;
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public ITimeGraphEntry getEntry() {
        return trace;
    }

    /**
     * @param duration the duration to set
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * @return the duration
     */
    @Override
    public long getDuration() {
        return duration;
    }

    @Override
    public ITimeEvent splitBefore(long splitTime) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ITimeEvent splitAfter(long splitTime) {
        throw new UnsupportedOperationException();
    }
}
