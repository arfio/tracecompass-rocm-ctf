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

import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.BaseEventComponent;
import org.eclipse.tracecompass.tmf.ui.properties.ReadOnlyTextPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

/**
 * <p>
 * Property source implementation for the base event component.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class BaseEventPropertySource extends BasePropertySource {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The base event 'name' property ID.
     */
    public static final String BASE_EVENT_NAME_PROPERTY_ID = "base.event.name"; //$NON-NLS-1$
    /**
     * The base event 'type' property ID.
     */
    public static final String BASE_EVENT_TYPE_PROPERTY_ID = "base.event.type"; //$NON-NLS-1$
    /**
     * The base event 'log level' property ID.
     */
    public static final String BASE_EVENT_LOGLEVEL_PROPERTY_ID = "base.event.loglevel"; //$NON-NLS-1$
    /**
     * The base event 'fields' property ID.
     */
    public static final String BASE_EVENT_FIELDS_PROPERTY_ID = "base.event.fields"; //$NON-NLS-1$
    /**
     *  The base event 'name' property name.
     */
    public static final String BASE_EVENT_NAME_PROPERTY_NAME = Messages.TraceControl_EventNamePropertyName;
    /**
     * The base event 'type' property name.
     */
    public static final String BASE_EVENT_TYPE_PROPERTY_NAME = Messages.TraceControl_EventTypePropertyName;
    /**
     * The base event 'log level' property name.
     */
    public static final String BASE_EVENT_LOGLEVEL_PROPERTY_NAME = Messages.TraceControl_LogLevelPropertyName;
    /**
     * The base event 'fields' property name.
     */
    public static final String BASE_EVENT_FIELDS_PROPERTY_NAME = Messages.TraceControl_FieldsPropertyName;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The base event component which this property source is for.
     */
    private final BaseEventComponent fBaseEvent;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param component - the base event component
     */
    public BaseEventPropertySource(BaseEventComponent component) {
        fBaseEvent = component;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        List<IPropertyDescriptor> list = new ArrayList<> ();
        list.add(new ReadOnlyTextPropertyDescriptor(BASE_EVENT_NAME_PROPERTY_ID, BASE_EVENT_NAME_PROPERTY_NAME));
        list.add(new ReadOnlyTextPropertyDescriptor(BASE_EVENT_TYPE_PROPERTY_ID, BASE_EVENT_TYPE_PROPERTY_NAME));
        if (fBaseEvent.getLogLevel() != TraceLogLevel.LEVEL_UNKNOWN) {
            list.add(new ReadOnlyTextPropertyDescriptor(BASE_EVENT_LOGLEVEL_PROPERTY_ID, BASE_EVENT_LOGLEVEL_PROPERTY_NAME));
        }
        if (fBaseEvent.getFieldString() != null) {
            list.add(new ReadOnlyTextPropertyDescriptor(BASE_EVENT_FIELDS_PROPERTY_ID, BASE_EVENT_FIELDS_PROPERTY_NAME));
        }
        return list.toArray(new IPropertyDescriptor[list.size()]);
    }

    @Override
    public Object getPropertyValue(Object id) {
        if(BASE_EVENT_NAME_PROPERTY_ID.equals(id)) {
            return fBaseEvent.getName();
        }
        if (BASE_EVENT_TYPE_PROPERTY_ID.equals(id)) {
            return fBaseEvent.getEventType().name();
        }
        if (BASE_EVENT_LOGLEVEL_PROPERTY_ID.equals(id)) {
            return fBaseEvent.getLogLevel().name();
        }
        if (BASE_EVENT_FIELDS_PROPERTY_ID.equals(id)) {
            return fBaseEvent.getFieldString();
        }
        return null;
    }

}
