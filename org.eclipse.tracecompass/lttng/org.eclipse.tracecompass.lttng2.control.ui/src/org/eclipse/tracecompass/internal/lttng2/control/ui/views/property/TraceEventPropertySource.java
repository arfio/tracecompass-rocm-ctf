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
 *     Bernd Hufmann - Initial API and implementation
 *     Bernd Hufmann - Updated for support of LTTng Tools 2.1
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.property;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tracecompass.internal.lttng2.control.core.model.LogLevelType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceEventComponent;
import org.eclipse.tracecompass.tmf.ui.properties.ReadOnlyTextPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

/**
 * <p>
 * Property source implementation for the trace event component.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class TraceEventPropertySource extends BasePropertySource {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The trace event 'name' property ID.
     */
    public static final String TRACE_EVENT_NAME_PROPERTY_ID = "trace.event.name"; //$NON-NLS-1$
    /**
     * The trace event 'type' property ID.
     */
    public static final String TRACE_EVENT_TYPE_PROPERTY_ID = "trace.event.type"; //$NON-NLS-1$
    /**
     * The trace event 'log level' property ID.
     */
    public static final String TRACE_EVENT_LOGLEVEL_PROPERTY_ID = "trace.event.loglevel"; //$NON-NLS-1$
    /**
     * The trace event 'state' property ID.
     */
    public static final String TRACE_EVENT_STATE_PROPERTY_ID = "trace.event.state"; //$NON-NLS-1$
    /**
     * The trace event 'filter' property ID.
     */
    public static final String TRACE_EVENT_FILTER_PROPERTY_ID = "trace.event.filter"; //$NON-NLS-1$
    /**
     * The trace event 'exclude' property ID.
     */
    public static final String TRACE_EVENT_EXCLUDE_PROPERTY_ID = "trace.event.exclude"; //$NON-NLS-1$

    /**
     *  The trace event 'name' property name.
     */
    public static final String TRACE_EVENT_NAME_PROPERTY_NAME = Messages.TraceControl_EventNamePropertyName;
    /**
     * The trace event 'type' property name.
     */
    public static final String TRACE_EVENT_TYPE_PROPERTY_NAME = Messages.TraceControl_EventTypePropertyName;
    /**
     * The trace event 'log level' property name.
     */
    public static final String TRACE_EVENT_LOGLEVEL_PROPERTY_NAME = Messages.TraceControl_LogLevelPropertyName;
    /**
     * The trace event 'state' property name.
     */
    public static final String TRACE_EVENT_STATE_PROPERTY_NAME = Messages.TraceControl_StatePropertyName;
    /**
     * The trace event 'filter' property name.
     */
    public static final String TRACE_EVENT_FILTER_PROPERTY_NAME = Messages.TraceControl_FilterPropertyName;
    /**
     * The trace event 'exclude' property name.
     */
    public static final String TRACE_EVENT_EXCLUDE_PROPERTY_NAME = Messages.TraceControl_ExcludePropertyName;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The event component which this property source is for.
     */
    protected final TraceEventComponent fEvent;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param component - the base event component
     */
    public TraceEventPropertySource(TraceEventComponent component) {
        fEvent = component;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        List<IPropertyDescriptor> list = new ArrayList<> ();
        list.add(new ReadOnlyTextPropertyDescriptor(TRACE_EVENT_NAME_PROPERTY_ID, TRACE_EVENT_NAME_PROPERTY_NAME));
        list.add(new ReadOnlyTextPropertyDescriptor(TRACE_EVENT_TYPE_PROPERTY_ID, TRACE_EVENT_TYPE_PROPERTY_NAME));
        list.add( new ReadOnlyTextPropertyDescriptor(TRACE_EVENT_STATE_PROPERTY_ID, TRACE_EVENT_STATE_PROPERTY_NAME));
        if (fEvent.getLogLevel() != TraceLogLevel.LEVEL_UNKNOWN) {
            list.add(new ReadOnlyTextPropertyDescriptor(TRACE_EVENT_LOGLEVEL_PROPERTY_ID, TRACE_EVENT_LOGLEVEL_PROPERTY_NAME));
        }
        if (fEvent.getFilterExpression() != null) {
            list.add(new ReadOnlyTextPropertyDescriptor(TRACE_EVENT_FILTER_PROPERTY_ID, TRACE_EVENT_FILTER_PROPERTY_NAME));
        }
        if (fEvent.getExcludedEvents() != null && !fEvent.getExcludedEvents().isEmpty()) {
            list.add(new ReadOnlyTextPropertyDescriptor(TRACE_EVENT_EXCLUDE_PROPERTY_ID, TRACE_EVENT_EXCLUDE_PROPERTY_NAME));
        }
        return list.toArray(new IPropertyDescriptor[list.size()]);
    }

    @Override
    public Object getPropertyValue(Object id) {
        if(TRACE_EVENT_NAME_PROPERTY_ID.equals(id)) {
            return fEvent.getName();
        }
        if (TRACE_EVENT_TYPE_PROPERTY_ID.equals(id)) {
            return fEvent.getEventType().name();
        }
        if (TRACE_EVENT_LOGLEVEL_PROPERTY_ID.equals(id)) {
            StringBuffer buffer = new StringBuffer();
            if (fEvent.getLogLevelType() != LogLevelType.LOGLEVEL_NONE) {
                buffer.append(fEvent.getLogLevelType().getShortName()).append(' ');
            }
            buffer.append(fEvent.getLogLevel().name());
            return buffer.toString();
        }
        if (TRACE_EVENT_STATE_PROPERTY_ID.equals(id)) {
            return fEvent.getState().name();
        }
        if (TRACE_EVENT_FILTER_PROPERTY_ID.equals(id)) {
            return fEvent.getFilterExpression();
        }
        if (TRACE_EVENT_EXCLUDE_PROPERTY_ID.equals(id)) {
            return fEvent.getExcludedEvents();
        }

        return null;
    }

}
