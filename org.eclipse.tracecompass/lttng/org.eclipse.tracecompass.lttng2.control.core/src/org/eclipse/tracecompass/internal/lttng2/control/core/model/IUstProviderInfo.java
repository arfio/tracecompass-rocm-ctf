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
package org.eclipse.tracecompass.internal.lttng2.control.core.model;

import java.util.List;

/**
 * <p>
 * Interface for retrieval of UST provider information.
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface IUstProviderInfo extends ITraceInfo {

    /**
     * @return the process ID of the UST provider.
     */
    int getPid();

    /**
     * Sets the process ID of the UST provider to the given value.
     *
     * @param pid
     *            - process ID to set
     */
    void setPid(int pid);

    /**
     * @return all event information as array.
     */
    IBaseEventInfo[] getEvents();

    /**
     * Sets the event information specified by given list.
     *
     * @param events
     *            - all event information to set.
     */
    void setEvents(List<IBaseEventInfo> events);

    /**
     * Adds a single event information.
     *
     * @param event
     *            - event information to add.
     */
    void addEvent(IBaseEventInfo event);

    /**
     * @return all loggers information as array.
     */
    List<ILoggerInfo> getLoggers();

    /**
     * Sets the loggers information specified by given list.
     *
     * @param loggers
     *            - all loggers information to set.
     */
    void setLoggers(List<ILoggerInfo> loggers);

    /**
     * Adds a single logger information.
     *
     * @param logger
     *            - logger information to add.
     */
    void addLogger(ILoggerInfo logger);

    /**
     * Adds a list of logger information to the UST provider.
     *
     * @param loggers
     *            a list of logger information
     */
    void addLoggers(List<ILoggerInfo> loggers);
}
