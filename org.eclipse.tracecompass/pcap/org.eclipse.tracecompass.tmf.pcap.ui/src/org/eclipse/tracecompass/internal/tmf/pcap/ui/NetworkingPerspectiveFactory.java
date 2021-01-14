/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.pcap.ui;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.pcap.ui.stream.StreamListView;
import org.eclipse.tracecompass.tmf.ui.project.wizards.NewTmfProjectWizard;
import org.eclipse.tracecompass.tmf.ui.views.colors.ColorsView;
import org.eclipse.tracecompass.tmf.ui.views.filter.FilterView;
import org.eclipse.tracecompass.tmf.ui.views.histogram.HistogramView;
import org.eclipse.tracecompass.tmf.ui.views.statistics.TmfStatisticsView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * The networking perspective definition.
 *
 * @author Vincent Perot
 */
public class NetworkingPerspectiveFactory implements IPerspectiveFactory {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /** The Perspective ID */
    public static final String ID = "org.eclipse.linuxtools.tmf.pcap.ui.perspective.network"; //$NON-NLS-1$

    // Views
    private static final String PROJECT_VIEW_ID = checkNotNull(IPageLayout.ID_PROJECT_EXPLORER);
    private static final String PROPERTIES_VIEW_ID = checkNotNull(IPageLayout.ID_PROP_SHEET);
    private static final String BOOKMARKS_VIEW_ID = checkNotNull(IPageLayout.ID_BOOKMARKS);
    private static final String FILTER_VIEW_ID = FilterView.ID;
    private static final String HISTOGRAM_VIEW_ID = HistogramView.ID;
    private static final String STATISTICS_VIEW_ID = TmfStatisticsView.ID;
    private static final String COLOR_VIEW_ID = ColorsView.ID;
    private static final String STREAM_LIST_VIEW_ID = StreamListView.ID;

    // ------------------------------------------------------------------------
    // IPerspectiveFactory
    // ------------------------------------------------------------------------

    @Override
    public void createInitialLayout(@Nullable IPageLayout layout) {

        if (layout == null) {
            return;
        }

        // Editor area
        layout.setEditorAreaVisible(true);

        // Create the top left folder
        IFolderLayout topLeftFolder = layout.createFolder("topLeftFolder", IPageLayout.LEFT, 0.15f, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
        topLeftFolder.addView(PROJECT_VIEW_ID);

        // Create the middle right folder
        IFolderLayout middleRightFolder = layout.createFolder("middleRightFolder", IPageLayout.BOTTOM, 0.40f, IPageLayout.ID_EDITOR_AREA); //$NON-NLS-1$
        middleRightFolder.addView(PROPERTIES_VIEW_ID);
        middleRightFolder.addView(HISTOGRAM_VIEW_ID);
        middleRightFolder.addView(STATISTICS_VIEW_ID);
        middleRightFolder.addView(COLOR_VIEW_ID);

        // Create the bottom right folder
        IFolderLayout bottomRightFolder = layout.createFolder("bottomRightFolder", IPageLayout.BOTTOM, 0.65f, "middleRightFolder"); //$NON-NLS-1$ //$NON-NLS-2$
        bottomRightFolder.addView(FILTER_VIEW_ID);
        bottomRightFolder.addView(BOOKMARKS_VIEW_ID);
        bottomRightFolder.addView(STREAM_LIST_VIEW_ID);

        // Populate menus, etc
        layout.addPerspectiveShortcut(ID);
        layout.addNewWizardShortcut(NewTmfProjectWizard.ID);
    }

}
