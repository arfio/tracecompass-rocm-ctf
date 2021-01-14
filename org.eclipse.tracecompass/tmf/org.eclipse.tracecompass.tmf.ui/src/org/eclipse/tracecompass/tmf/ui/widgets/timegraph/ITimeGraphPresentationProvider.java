/*******************************************************************************
 * Copyright (c) 2012, 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEventStyleStrings;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.ITmfTimeGraphDrawingHelper;

/**
 * Interface for the time graph widget provider
 *
 * @author Patrick Tasse
 */
public interface ITimeGraphPresentationProvider {

    /**
     * State table index for an invisible event
     */
    final int INVISIBLE = -1;

    /**
     * State table index for a transparent event (only borders drawn)
     */
    final int TRANSPARENT = -2;

    /**
     * Returns the name of state types.
     *
     * @return the name of state types
     */
    String getStateTypeName();

    /**
     * Returns the name of state type depending on the given entry. Note that
     * this overwrites the name which is return by getStateTypeName().
     *
     * @param entry
     *            the entry
     * @return the name of state type depending on the given entry or null.
     */
    String getStateTypeName(ITimeGraphEntry entry);

    /**
     * Returns table of states with state name to state color relationship.
     *
     * @return table of states with color and name
     *
     * @see #getStateTableIndex
     */
    StateItem[] getStateTable();

    /**
     * Returns the index in the state table corresponding to this time event.
     * The index should correspond to a state in the state table, otherwise the
     * color SWT.COLOR_BLACK will be used. If the index returned is TRANSPARENT,
     * only the event borders will be drawn. If the index returned is INVISIBLE
     * or another negative, the event will not be drawn.
     *
     * @param event
     *            the time event
     * @return the corresponding state table index
     *
     * @see #getStateTable
     * @see #TRANSPARENT
     * @see #INVISIBLE
     */
    int getStateTableIndex(ITimeEvent event);

    /**
     * Called after drawing the control
     *
     * @param bounds
     *            The drawing rectangle
     * @param gc
     *            The graphics context
     */
    void postDrawControl(Rectangle bounds, GC gc);

    /**
     * Called after drawing an entry
     *
     * @param entry
     *            the entry that was drawn
     * @param bounds
     *            the drawing rectangle
     * @param gc
     *            the graphics context
     */
    void postDrawEntry(ITimeGraphEntry entry, Rectangle bounds, GC gc);

    /**
     * Called after drawing an event
     *
     * @param event
     *            the event that was drawn
     * @param bounds
     *            the drawing rectangle
     * @param gc
     *            the graphics context
     */
    void postDrawEvent(ITimeEvent event, Rectangle bounds, GC gc);

    /**
     * Returns the height of this item. This value is ignored if the time graph
     * has a fixed item height.
     *
     * @param entry
     *            the entry
     * @return the item height
     *
     * @see TimeGraphViewer#setItemHeight
     */
    int getItemHeight(ITimeGraphEntry entry);

    /**
     * Provides the image icon for a given entry.
     *
     * @param entry
     *            the entry
     * @return the image icon
     */
    Image getItemImage(ITimeGraphEntry entry);

    /**
     * Returns the name of this event.
     *
     * @param event
     *            The event
     * @return The event name
     */
    String getEventName(ITimeEvent event);

    /**
     * Returns a map of name and value providing additional information to
     * display in the tool tip for this event.
     *
     * @param event
     *            the time event
     * @return a map of tool tip information
     */
    Map<String, String> getEventHoverToolTipInfo(ITimeEvent event);

    /**
     * Returns a map of name and value providing additional information to
     * display in the tool tip for this event.
     *
     * @param event
     *            the time event
     * @param hoverTime
     *            the time corresponding to the mouse hover position
     * @return a map of tool tip information
     */
    Map<String, String> getEventHoverToolTipInfo(ITimeEvent event, long hoverTime);

    /**
     * Get the name of the link type
     *
     * @return The name of the link type
     * @since 4.0
     */
    default String getLinkTypeName() {
        return Messages.TimeGraphLegend_Arrows;
    }

    /**
     * Check whether time and duration should be displayed in tooltip (after
     * items from {@link #getEventHoverToolTipInfo(ITimeEvent)}).
     *
     * @return <code>true</code> if times and duration should be displayed on
     *         tooltip, <code>false</code> otherwise.
     */
    public boolean displayTimesInTooltip();

    /**
     * Get the style map of a given ITimeEvent
     *
     * @param event
     *            the time event
     * @return the style map, as detailed in {@link ITimeEventStyleStrings}
     * @since 3.0
     */
    default Map<String, Object> getEventStyle(ITimeEvent event) {
        StateItem stateItem = null;
        int index = getStateTableIndex(event);
        StateItem[] stateTable = getStateTable();
        if (index >= 0 && index < stateTable.length) {
            stateItem = stateTable[index];
        }
        Map<String, Object> styleMap = stateItem == null ? Collections.emptyMap() : stateItem.getStyleMap();
        Map<String, Object> specificEventStyles = getSpecificEventStyle(event);
        if (specificEventStyles.isEmpty()) {
            return styleMap;
        }
        if (styleMap.isEmpty()) {
            return specificEventStyles;
        }
        styleMap = new HashMap<>(styleMap);
        styleMap.putAll(specificEventStyles);
        return styleMap;
    }

    /**
     * Get the id of the state provider
     *
     * @return The preference key, if there are many instances of a class, this
     *         method needs to be overridden
     * @since 4.1
     */
    default String getPreferenceKey() {
        return getClass().getName();
    }

    /**
     * Get the specific style map for a given event.
     *
     * @param event
     *            the time event
     * @return a style map containing the elements as detailed in
     *         {@link ITimeEventStyleStrings} to override
     * @since 3.0
     */
    default Map<String, Object> getSpecificEventStyle(ITimeEvent event) {
        return Collections.emptyMap();
    }

    /**
     * Signal the provider that its color settings have changed
     * @since 3.2
     */
    default void refresh(){
        // do nothing
    }


    /**
     * Returns the drawing helper for this presentation provider.
     *
     * @return The drawing helper
     * @since 4.0
     */
    ITmfTimeGraphDrawingHelper getDrawingHelper();

    /**
     * Sets this presentation provider's drawing helper.
     * This helper be needed to know where to draw items, get its coordinates
     * given a time, etc.
     *
     * @param helper
     *            The drawing helper
     * @since 4.0
     */
    void setDrawingHelper(ITmfTimeGraphDrawingHelper helper);

    /**
     * Adds a color settings listener, to be notified when the presentation
     * provider's state colors change.
     *
     * @param listener
     *            The new listener for color settings changes
     * @since 4.0
     */
    void addColorListener(ITimeGraphColorListener listener);

    /**
     * Removes a color settings listener.
     *
     * @param listener
     *            The color settings listener to remove
     * @since 4.0
     */
    void removeColorListener(ITimeGraphColorListener listener);

    /**
     * Get input data used for filtering
     *
     * @param event
     *            The ongoing time event
     * @return The map of input data
     * @since 4.0
     */
    default @NonNull Map<String, String> getFilterInput(ITimeEvent event) {
        Map<String, String> inputs = new HashMap<>();
        ITimeGraphEntry entry = event.getEntry();

        String stateTypeName = getStateTypeName(entry);
        String entryName = entry.getName();
        if (stateTypeName == null) {
            stateTypeName = getStateTypeName();
        }

        if (!entryName.isEmpty()) {
            inputs.put(stateTypeName, entry.getName());
        }

        String state = getEventName(event);
        if (state != null) {
            inputs.put(Messages.TmfTimeTipHandler_TRACE_STATE, state);
        }

        return inputs;
    }
}
