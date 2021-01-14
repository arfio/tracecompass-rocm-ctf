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
 *   Simon Delisle - Updated for support of LTTng Tools 2.2
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.core.model;

import java.util.List;

import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.BufferType;

/**
 * <p>
 * Interface for retrieval of trace channel information.
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface IChannelInfo extends ITraceInfo {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * Default value for overwrite mode.
     */
    boolean DEFAULT_OVERWRITE_MODE = false;

    /**
     * @return the overwrite mode value.
     */
    boolean isOverwriteMode();
    /**
     * Sets the overwrite mode value to the given mode.
     * @param mode - mode to set.
     */
    void setOverwriteMode(boolean mode);

    /**
     * @return the sub-buffer size.
     */
    long getSubBufferSize();
    /**
     * Sets the sub-buffer size to the given value.
     * @param bufferSize - size to set to set.
     */
    void setSubBufferSize(long bufferSize);

    /**
     * @return the number of sub-buffers.
     */
    int getNumberOfSubBuffers();
    /**
     * Sets the number of sub-buffers to the given value.
     * @param numberOfSubBuffers - value to set.
     */
    void setNumberOfSubBuffers(int numberOfSubBuffers);

    /**
     * @return the switch timer interval.
     */
    long getSwitchTimer();
    /**
     * Sets the switch timer interval to the given value.
     * @param timer - timer value to set.
     */
    void setSwitchTimer(long timer);

    /**
     * @return the read timer interval.
     */
    long getReadTimer();
    /**
     * Sets the read timer interval to the given value.
     * @param timer - timer value to set..
     */
    void setReadTimer(long timer);

    /**
     * @return the output type.
     */
    TraceChannelOutputType getOutputType();
    /**
     * Sets the output type to the given value.
     * @param type - type to set.
     */
    void setOutputType(String type);
    /**
     * Sets the output type to the given value.
     * @param type - type to set.
     */
    void setOutputType(TraceChannelOutputType type);

    /**
     * @return the channel state (enabled or disabled).
     */
    TraceEnablement getState();
    /**
     * Sets the channel state (enablement) to the given value.
     * @param state - state to set.
     */
    void setState(TraceEnablement state);
    /**
     * Sets the channel state (enablement) to the value specified by the given name.
     * @param stateName - state to set.
     */
    void setState(String stateName);

    /**
     * @return all event information as array.
     */
    IEventInfo[] getEvents();
    /**
     * Sets the event information specified by given list.
     * @param events - all event information to set.
     */
    void setEvents(List<IEventInfo> events);
    /**
     * Adds a single event information.
     * @param event - event information to add.
     */
    void addEvent(IEventInfo event);
    /**
     * Sets the maximum size of trace files
     * @param maxSizeTraceFiles - maximum size
     */
    void setMaxSizeTraceFiles(long maxSizeTraceFiles);
    /**
     * Sets the maximum number of trace files
     * @param maxNumberTraceFiles - maximum number
     */
    void setMaxNumberTraceFiles(int maxNumberTraceFiles);
    /**
     * @return maximum size of trace files
     */
    long getMaxSizeTraceFiles();
    /**
     * @return maximum number of trace files
     */
    int getMaxNumberTraceFiles();
    /**
     * Sets per UID buffers
     * @param buffersUID - enable or not
     */
    void setBufferType(BufferType buffersUID);
    /**
     * @return the value of buffersUID (enable or not)
     */
    BufferType getBufferType();
    /**
     * Sets number of discarded events
     * @param numberOfDiscardedEvents - number of discarded events
     */
    void setNumberOfDiscardedEvents(long numberOfDiscardedEvents);
    /**
     * @return the number of discarded events
     */
    long getNumberOfDiscardedEvents();
    /**
     * Sets number of lost packets
     * @param numberOfLostPackets - number of lost packets
     */
    void setNumberOfLostPackets(long numberOfLostPackets);
    /**
     * @return the number of lost packets
     */
    long getNumberOfLostPackets();
}
