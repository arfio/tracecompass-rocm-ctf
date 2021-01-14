/**********************************************************************
 * Copyright (c) 2012, 2015 Ericsson
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
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IChannelInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IEventInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.ITraceLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.LogLevelType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceChannelOutputType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceEnablement;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.ChannelInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.ProbeEventInfo;
import org.eclipse.tracecompass.internal.lttng2.control.ui.Activator;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.property.TraceChannelPropertySource;
import org.eclipse.ui.views.properties.IPropertySource;


/**
 * <p>
 * Implementation of the trace channel component.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class TraceChannelComponent extends TraceControlComponent {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * Path to icon file for this component (state enabled).
     */
    public static final String TRACE_CHANNEL_ICON_FILE_ENABLED = "icons/obj16/channel.gif"; //$NON-NLS-1$
    /**
     * Path to icon file for this component (state disabled).
     */
    public static final String TRACE_CHANNEL_ICON_FILE_DISABLED = "icons/obj16/channel_disabled.gif"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The channel information.
     */
    private IChannelInfo fChannelInfo = null;
    /**
     * The image to be displayed in disabled state.
     */
    private Image fDisabledImage = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param name - the name of the component.
     * @param parent - the parent of this component.
     */
    public TraceChannelComponent(String name, ITraceControlComponent parent) {
        super(name, parent);
        setImage(TRACE_CHANNEL_ICON_FILE_ENABLED);
        setToolTip(Messages.TraceControl_ChannelDisplayName);
        fChannelInfo = new ChannelInfo(name);
        fDisabledImage = Activator.getDefault().loadIcon(TRACE_CHANNEL_ICON_FILE_DISABLED);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public Image getImage() {
        if (fChannelInfo.getState() == TraceEnablement.DISABLED) {
            return fDisabledImage;
        }
        return super.getImage();
    }

    /**
     * Sets the channel information.
     *
     * @param channelInfo
     *            The channel info to assign to this component
     */
    public void setChannelInfo(IChannelInfo channelInfo) {
        fChannelInfo = channelInfo;
        IEventInfo[] events = fChannelInfo.getEvents();
        List<ITraceControlComponent> eventComponents = new ArrayList<>();
        for (int i = 0; i < events.length; i++) {
            TraceEventComponent event = null;
            if (events[i].getClass() == ProbeEventInfo.class) {
                event = new TraceProbeEventComponent(events[i].getName(), this);
            } else {
                event = new TraceEventComponent(events[i].getName(), this);
            }

            eventComponents.add(event);
            event.setEventInfo(events[i]);
        }
        if (!eventComponents.isEmpty()) {
            setChildren(eventComponents);
        }
    }

    /**
     * @return the overwrite mode value.
     */
    public boolean isOverwriteMode() {
        return fChannelInfo.isOverwriteMode();
    }
    /**
     * Sets the overwrite mode value to the given mode.
     * @param mode - mode to set.
     */
    public void setOverwriteMode(boolean mode){
        fChannelInfo.setOverwriteMode(mode);
    }
    /**
     * @return the sub-buffer size.
     */
    public long getSubBufferSize() {
        return fChannelInfo.getSubBufferSize();
    }
    /**
     * Sets the sub-buffer size to the given value.
     * @param bufferSize - size to set to set.
     */
    public void setSubBufferSize(long bufferSize) {
        fChannelInfo.setSubBufferSize(bufferSize);
    }
    /**
     * @return the number of sub-buffers.
     */
    public int getNumberOfSubBuffers() {
        return fChannelInfo.getNumberOfSubBuffers();
    }
    /**
     * Sets the number of sub-buffers to the given value.
     * @param numberOfSubBuffers - value to set.
     */
    public void setNumberOfSubBuffers(int numberOfSubBuffers) {
        fChannelInfo.setNumberOfSubBuffers(numberOfSubBuffers);
    }
    /**
     * @return the switch timer interval.
     */
    public long getSwitchTimer() {
        return fChannelInfo.getSwitchTimer();
    }
    /**
     * Sets the switch timer interval to the given value.
     * @param timer - timer value to set.
     */
    public void setSwitchTimer(long timer) {
        fChannelInfo.setSwitchTimer(timer);
    }
    /**
     * @return the read timer interval.
     */
    public long getReadTimer() {
        return fChannelInfo.getReadTimer();
    }
    /**
     * Sets the read timer interval to the given value.
     * @param timer - timer value to set..
     */
    public void setReadTimer(long timer) {
        fChannelInfo.setReadTimer(timer);
    }
    /**
     * @return the output type.
     */
    public TraceChannelOutputType getOutputType() {
        return fChannelInfo.getOutputType();
    }
    /**
     * Sets the output type to the given value.
     * @param type - type to set.
     */
    public void setOutputType(TraceChannelOutputType type) {
        fChannelInfo.setOutputType(type);
    }
    /**
     * Sets the output type to the given value.
     * @param type - type to set.
     */
    public void setOutputType(String type) {
        fChannelInfo.setOutputType(type);
    }

    /**
     * @return the channel state (enabled or disabled).
     */
    public TraceEnablement getState() {
        return fChannelInfo.getState();
    }
    /**
     * Sets the channel state (enablement) to the given value.
     * @param state - state to set.
     */
    public void setState(TraceEnablement state) {
        fChannelInfo.setState(state);
    }
    /**
     * Sets the channel state (enablement) to the value specified by the given name.
     * @param stateName - state to set.
     */
    public void setState(String stateName) {
        fChannelInfo.setState(stateName);
    }

    /**
     * @return number of discarded events
     */
    public long getNumberOfDiscardedEvents() {
        return fChannelInfo.getNumberOfDiscardedEvents();
    }
    /**
     * @return number of lost packets
     */
    public long getNumberOfLostPackets() {
        return fChannelInfo.getNumberOfLostPackets();
    }

    /**
     * @return maximum size of trace files
     */
    public long getMaxSizeTraceFiles() {
        return fChannelInfo.getMaxSizeTraceFiles();
    }
    /**
     * @return maximum number of trace files
     */
    public int getMaxNumberTraceFiles() {
        return fChannelInfo.getMaxNumberTraceFiles();
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == IPropertySource.class) {
            return adapter.cast(new TraceChannelPropertySource(this));
        }
        return null;
    }

    /**
     * @return session name from parent
     */
    public String getSessionName() {
       return ((TraceDomainComponent)getParent()).getSessionName();
    }

    /**
     * @return session from parent
     */
    public TraceSessionComponent getSession() {
       return ((TraceDomainComponent)getParent()).getSession();
    }

    /**
     * @return the domain type ({@link TraceDomainType})
     */
    public TraceDomainType getDomain() {
        return ((TraceDomainComponent)getParent()).getDomain();
    }

    /**
     * @return the parent target node
     */
    public TargetNodeComponent getTargetNode() {
        return ((TraceDomainComponent)getParent()).getTargetNode();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Enables a list of events with no additional parameters.
     *
     * @param eventNames
     *            - a list of event names to enabled.
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void enableEvents(List<String> eventNames, IProgressMonitor monitor) throws ExecutionException {
        enableEvents(eventNames, null, null, monitor);
    }

    /**
     * Enables a list of events with no additional parameters.
     *
     * @param eventNames
     *            - a list of event names to enabled.
     * @param filterExpression
     *            - a filter expression
     * @param excludedEvents
     *            - a list of events to exclude.
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void enableEvents(List<String> eventNames, String filterExpression, List<String> excludedEvents, IProgressMonitor monitor) throws ExecutionException {
        getControlService().enableEvents(getSessionName(), getName(), eventNames, getDomain(), filterExpression, excludedEvents, monitor);
    }

    /**
     * Enables all syscalls (for kernel domain)
     *
     * @param syscallNames
     *            - a list of syscall to enable
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void enableSyscalls(List<String> syscallNames, IProgressMonitor monitor) throws ExecutionException {
        getControlService().enableSyscalls(getSessionName(), getName(), syscallNames, monitor);
    }

    /**
     * Enables a dynamic probe (for kernel domain)
     *
     * @param eventName
     *            - event name for probe
     * @param isFunction
     *            - true for dynamic function entry/return probe else false
     * @param probe
     *            - the actual probe
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void enableProbe(String eventName, boolean isFunction, String probe,
            IProgressMonitor monitor) throws ExecutionException {
        getControlService().enableProbe(getSessionName(), getName(), eventName, isFunction, probe, monitor);
    }

    /**
     * Enables events using log level.
     *
     * @param eventNames
     *            - a list of event names
     * @param logLevelType
     *            - a log level type
     * @param level
     *            - a log level
     * @param filterExpression
     *            - a filter expression
     * @param domain
     *            - the domain type ({@link TraceDomainType})
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void enableLogLevel(List<String> eventNames, LogLevelType logLevelType,
            ITraceLogLevel level, String filterExpression, TraceDomainType domain, IProgressMonitor monitor)
            throws ExecutionException {
        getControlService().enableLogLevel(getSessionName(), getName(), eventNames, logLevelType, level, filterExpression, domain, monitor);
    }

    /**
     * Enables a list of events with no additional parameters.
     *
     * @param eventNames
     *            - a list of event names to enabled.
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void disableEvent(List<String> eventNames, IProgressMonitor monitor)
            throws ExecutionException {
        getControlService().disableEvent(getParent().getParent().getName(),
                getName(), eventNames, getDomain(), monitor);
    }

    /**
     * Add contexts to given channels and or events
     *
     * @param contexts
     *            - a list of contexts to add
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void addContexts(List<String> contexts, IProgressMonitor monitor)
            throws ExecutionException {
        getControlService().addContexts(getSessionName(), getName(), null,
                getDomain(), contexts, monitor);
    }
}
