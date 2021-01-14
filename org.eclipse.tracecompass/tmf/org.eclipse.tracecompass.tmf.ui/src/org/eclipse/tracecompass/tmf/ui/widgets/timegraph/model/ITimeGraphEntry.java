/*******************************************************************************
 * Copyright (c) 2009, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alvaro Sanchez-Leon - Initial API and implementation
 *   Patrick Tasse - Refactoring
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.ISelection;

/**
 * Interface for an entry (row) in the time graph view
 *
 * @author Alvaro Sanchez-Leon
 * @author Patrick Tasse
 */
public interface ITimeGraphEntry extends ISelection {

    /**
     * An enumeration of the display style of the time graph entries
     *
     * @author Geneviève Bastien
     * @since 5.0
     */
    public enum DisplayStyle {
        /**
         * Display states, ie rectangle representing a discrete state that has a
         * beginning and an end
         */
        STATE,
        /**
         * Display XY lines for this entry, ie one or more continuous lines that change
         * over time
         */
        LINE
    }

    /**
     * Returns the parent of this entry, or <code>null</code> if it has none.
     *
     * @return the parent element, or <code>null</code> if it has none
     */
    ITimeGraphEntry getParent();

    /**
     * Returns whether this entry has children.
     *
     * @return <code>true</code> if the given element has children,
     *  and <code>false</code> if it has no children
     */
    boolean hasChildren();

    /**
     * Returns the child elements of this entry.
     *
     * @return an array of child elements
     */
    List<@NonNull ? extends ITimeGraphEntry> getChildren();

    /**
     * Returns the name of this entry.
     *
     * @return the entry name
     */
    String getName();

    /**
     * Returns the start time of this entry in nanoseconds.
     *
     * @return the start time
     */
    long getStartTime();

    /**
     * Returns the end time of this entry in nanoseconds.
     *
     * @return the end time
     */
    long getEndTime();

    /**
     * Returns whether this entry has time events.
     * If true, the time events iterator should not be null.
     *
     * @return true if the entry has time events
     *
     * @see #getTimeEventsIterator
     * @see #getTimeEventsIterator(long, long, long)
     */
    boolean hasTimeEvents();

    /**
     * Get an iterator which returns all time events.
     *
     * @return the iterator
     */
    Iterator<@NonNull ? extends ITimeEvent> getTimeEventsIterator();

    /**
     * Get an iterator which only returns events that fall within the start time
     * and the stop time. The visible duration is the event duration below which
     * further detail is not discernible. If no such iterator is implemented,
     * provide a basic iterator which returns all events.
     *
     * @param startTime
     *            start time in nanoseconds
     * @param stopTime
     *            stop time in nanoseconds
     * @param visibleDuration
     *            duration of one pixel in nanoseconds
     *
     * @return the iterator
     */
    <T extends ITimeEvent> Iterator<@NonNull T> getTimeEventsIterator(long startTime, long stopTime, long visibleDuration);

    /**
     * Test if this time graph entry matches with this pattern
     *
     * @param pattern
     *            The pattern to match
     * @return True if it matches, false otherwise.
     * @since 2.0
     */
    boolean matches(@NonNull Pattern pattern);

    @Override
    default boolean isEmpty() {
        return false;
    }

    /**
     * Get the style of entry this represents
     *
     * @return The style of the entry
     * @since 5.0
     */
    default DisplayStyle getStyle() {
        return DisplayStyle.STATE;
    }
}
