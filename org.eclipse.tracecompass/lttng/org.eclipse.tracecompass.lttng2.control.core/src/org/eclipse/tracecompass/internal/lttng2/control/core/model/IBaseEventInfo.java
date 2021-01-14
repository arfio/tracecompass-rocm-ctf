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
 *   Bernd Hufmann - Updated for support of LTTng Tools 2.1
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.core.model;

import java.util.List;

/**
 * <p>
 * Interface for retrieval of basic trace event information.
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface IBaseEventInfo extends ITraceInfo {

    /**
     * @return the trace event type
     */
    TraceEventType getEventType();

    /**
     * Sets the trace event type to the given type
     * @param type - type to set
     */
    void setEventType(TraceEventType type);

    /**
     * Sets the trace event type to the type specified by the given name.
     * @param typeName - event type name
     */
    void setEventType(String typeName);

    /**
     * @return the trace event log level
     */
    TraceLogLevel getLogLevel();

    /**
     * Sets the trace event log level to the given level
     * @param level - event log level to set
     */
    void setLogLevel(TraceLogLevel level);

    /**
     * Sets the trace event log level to the level specified by the given name.
     * @param levelName - event log level name
     */
    void setLogLevel(String levelName);

    /**
     * Returns the field information (if exists)
     * @return the field information or null
     */
    IFieldInfo[] getFields();

    /**
     * @param field The field to add
     */
    void addField(IFieldInfo field);

    /**
     * Sets the fields
     * @param fields The fields
     */
    void setFields(List<IFieldInfo> fields);

    /**
     * Returns filter expression.
     * @return filter expression
     */
    String getFilterExpression();

    /**
     * Sets the filter expression.
     * @param filter The filter expression to set
     */
    void setFilterExpression(String filter);

    /**
     * Returns the excluded events.
     * @return excluded events
     */
    public String getExcludedEvents();

    /**
     * Sets the excluded events.
     * @param events The excluded events to set
     */
    public void setExcludedEvents(String events);

}
