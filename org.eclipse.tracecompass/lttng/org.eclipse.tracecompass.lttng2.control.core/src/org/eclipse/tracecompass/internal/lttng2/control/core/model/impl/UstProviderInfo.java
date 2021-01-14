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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.tracecompass.internal.lttng2.control.core.model.IBaseEventInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.ILoggerInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IUstProviderInfo;

/**
 * <p>
 * Implementation of the Ust Provider interface (IUstProviderInfo) to store UST
 * provider related data.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class UstProviderInfo extends TraceInfo implements IUstProviderInfo {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The process ID of the UST provider.
     */
    private int fPid = 0;

    /**
     * List of event information.
     */
    private final List<IBaseEventInfo> fEvents = new ArrayList<>();

    /**
     * List of logger information.
     */
    private final List<ILoggerInfo> fLoggers = new ArrayList<>();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param name - name of UST provider
     */
    public UstProviderInfo(String name) {
        super(name);
    }

    /**
     * Copy constructor
     * @param other - the instance to copy
     */
    public UstProviderInfo(UstProviderInfo other) {
        super(other);
        fPid = other.fPid;
        for (Iterator<IBaseEventInfo> iterator = other.fEvents.iterator(); iterator.hasNext();) {
            IBaseEventInfo event = iterator.next();
            if (event instanceof BaseEventInfo) {
                fEvents.add(new BaseEventInfo((BaseEventInfo)event));
            } else {
                fEvents.add(event);
            }
        }
        for (Iterator<ILoggerInfo> iterator = other.fLoggers.iterator(); iterator.hasNext();) {
            ILoggerInfo logger = iterator.next();
            if (logger instanceof LoggerInfo) {
                fLoggers.add(new LoggerInfo((LoggerInfo)logger));
            } else {
                fLoggers.add(logger);
            }
        }
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public int getPid() {
        return fPid;
    }

    @Override
    public void setPid(int pid) {
        fPid = pid;
    }

    @Override
    public IBaseEventInfo[] getEvents() {
        return fEvents.toArray(new IBaseEventInfo[fEvents.size()]);
    }

    @Override
    public void setEvents(List<IBaseEventInfo> events) {
        fEvents.clear();
        for (Iterator<IBaseEventInfo> iterator = events.iterator(); iterator.hasNext();) {
            IBaseEventInfo eventInfo = iterator.next();
            fEvents.add(eventInfo);
        }
    }

    @Override
    public void addEvent(IBaseEventInfo event) {
        fEvents.add(event);
    }

    @Override
    public List<ILoggerInfo> getLoggers() {
        return new ArrayList<>(fLoggers);
    }

    @Override
    public void setLoggers(List<ILoggerInfo> loggers) {
        fLoggers.clear();
        for (ILoggerInfo logger : loggers) {
            fLoggers.add(logger);
        }
    }

    @Override
    public void addLogger(ILoggerInfo logger) {
        fLoggers.add(logger);
    }

    @Override
    public void addLoggers(List<ILoggerInfo> loggers) {
        fLoggers.addAll(loggers);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + fEvents.hashCode();
        result = prime * result + fLoggers.hashCode();
        result = prime * result + fPid;
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
        UstProviderInfo other = (UstProviderInfo) obj;
        if (!fEvents.equals(other.fEvents)) {
            return false;
        }
        if (!fLoggers.equals(other.fLoggers)) {
            return false;
        }
        return (fPid == other.fPid);
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        StringBuffer output = new StringBuffer();
            output.append("[UstProviderInfo(");
            output.append(super.toString());
            output.append(",PID=");
            output.append(fPid);
            output.append(",Events=");
            if (fEvents.isEmpty()) {
                output.append("None");
            } else {
                for (Iterator<IBaseEventInfo> iterator = fEvents.iterator(); iterator.hasNext();) {
                    IBaseEventInfo event = iterator.next();
                    output.append(event.toString());
                }
            }
            output.append(",Loggers=");
            if (fLoggers.isEmpty()) {
                output.append("None");
            } else {
                for (ILoggerInfo logger : fLoggers) {
                    output.append(logger.toString());
                }
            }
            output.append(")]");
            return output.toString();
    }

}
