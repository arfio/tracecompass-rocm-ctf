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
 *   William Bourque - Initial API and implementation
 *   Francois Chouinard - Cleanup and refactoring
 *   Francois Chouinard - Moved from LTTng to TMF
 *   Patrick Tasse - Update for histogram selection range and tool tip
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.histogram;

import org.eclipse.osgi.util.NLS;

/**
 * Messages file for the histogram widgets.
 * <p>
 *
 * @author Francois Chouinard
 */
public class Messages extends NLS {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.tmf.ui.views.histogram.messages"; //$NON-NLS-1$

    /**
     * The label for the Show Traces button
     */
    public static String HistogramView_showTraces;
    /**
     * The label for the Lost Events button
     */
    public static String HistogramView_hideLostEvents;
    /**
     * The label for the selection start time
     */
    public static String HistogramView_selectionStartLabel;
    /**
     * The label for the selection end time
     */
    public static String HistogramView_selectionEndLabel;
    /**
     * The label for the window span.
     */
    public static String HistogramView_windowSpanLabel;
    /**
     * The tool tip text for the selection span.
     */
    public static String Histogram_selectionSpanToolTip;
    /**
     * The tool tip text for the bucket range.
     */
    public static String Histogram_bucketRangeToolTip;
    /**
     * Tool tip formatter for time range
     * @since 5.0
     */
    public static String Histogram_timeRange;
    /**
     * The tool tip text for the event count.
     */
    public static String Histogram_eventCountToolTip;
    /**
     * The tool tip text for the lost event count.
     */
    public static String Histogram_lostEventCountToolTip;

    // ------------------------------------------------------------------------
    // Initializer
    // ------------------------------------------------------------------------

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    private Messages() {
    }

}
