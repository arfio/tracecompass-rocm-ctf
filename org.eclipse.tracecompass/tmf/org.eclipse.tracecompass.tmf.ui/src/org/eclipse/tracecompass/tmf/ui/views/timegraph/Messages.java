/*******************************************************************************
 * Copyright (c) 2013, 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.timegraph;

import org.eclipse.osgi.util.NLS;

/**
 * Generic messages for the bar charts
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.tmf.ui.views.timegraph.messages"; //$NON-NLS-1$

    public static String AbstractTimeGraphtView_NextText;
    /**
     * Build Job title
     * @since 2.1
     */
    public static String AbstractTimeGraphView_BuildJob;

    /** @since 3.0*/
    public static String AbstractTimeGraphView_MarkerSetEditActionText;
    /** @since 3.0*/
    public static String AbstractTimeGraphView_MarkerSetMenuText;
    /** @since 3.0*/
    public static String AbstractTimeGraphView_MarkerSetNoneActionText;
    public static String AbstractTimeGraphView_NextTooltip;
    public static String AbstractTimeGraphView_PreviousText;
    public static String AbstractTimeGraphView_PreviousTooltip;
    /** @since 3.3*/
    public static String AbstractTimeGraphView_ExportImageActionText;
    /** @since 3.3*/
    public static String AbstractTimeGraphView_ExportImageToolTipText;
    /** @since 5.2*/
    public static String AbstractTimeGraphView_ShowLabelsActionText;
    /**
     * @since 4.0
     */
    public static String AbstractTimeGraphView_TimeEventFilterDialogTitle;

    public static String TimeGraphPresentationProvider_multipleStates;

    /**
     * @since 4.1
     */
    public static String TimeEventFilterDialog_CloseButton;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
