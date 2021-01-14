/*******************************************************************************
 * Copyright (c) 2010, 2016 Ericsson, EfficiOS Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.net.URL;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.osgi.framework.Bundle;

/**
 * Facilities to load icons used in the Project View.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
final class TmfProjectModelIcons {

    private TmfProjectModelIcons() {}

    public static final @NonNull Image DEFAULT_TRACE_ICON;
    public static final @NonNull Image DEFAULT_EXPERIMENT_ICON;
    public static final @NonNull Image DEFAULT_ANALYSIS_ICON;
    public static final @NonNull Image DEFAULT_VIEW_ICON;
    public static final @NonNull Image DEFAULT_REPORT_ICON;

    public static final @NonNull Image FOLDER_ICON;
    public static final @NonNull Image VIEWS_ICON;
    public static final @NonNull Image ONDEMAND_ANALYSES_ICON;
    public static final @NonNull Image BUILT_IN_ONDEMAND_ICON;
    public static final @NonNull Image USER_DEFINED_ONDEMAND_ICON;
    public static final @NonNull Image REPORTS_ICON;
    public static final @NonNull Image TRACECOMPASS_ICON;

    public static final WorkbenchLabelProvider WORKSPACE_LABEL_PROVIDER = new WorkbenchLabelProvider();

    private static final String TRACE_ICON_FILE = "icons/elcl16/trace.gif"; //$NON-NLS-1$
    private static final String EXPERIMENT_ICON_FILE = "icons/elcl16/experiment.gif"; //$NON-NLS-1$
    private static final String ANALYSIS_ICON_FILE = "icons/ovr16/experiment_folder_ovr.png"; //$NON-NLS-1$
    private static final String BUILT_IN_ONDEMAND_FILE = "icons/ovr16/built_in_ondemand_ovr.gif"; //$NON-NLS-1$
    private static final String USER_DEFINED_ONDEMAND_FILE = "icons/ovr16/user_defined_ondemand_ovr.gif"; //$NON-NLS-1$
    private static final String VIEW_ICON_FILE = "icons/obj16/node_obj.gif"; //$NON-NLS-1$
    private static final String ONDEMAND_ANALYSES_ICON_FILE = "icons/obj16/debugt_obj.gif"; //$NON-NLS-1$
    private static final String REPORTS_ICON_FILE = "icons/obj16/arraypartition_obj.gif"; //$NON-NLS-1$
    private static final String DEFAULT_REPORT_ICON_FILE = "icons/etool16/copy_edit.gif"; //$NON-NLS-1$
    private static final String VIEWS_ICON_FILE = "icons/obj16/analysisprovider_obj.gif"; //$NON-NLS-1$
    private static final String TRACECOMPASS_ICON_FILE = "icons/obj16/tc_icon.png"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Initialization
    // ------------------------------------------------------------------------

    static {
        ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
        Bundle bundle = Activator.getDefault().getBundle();

        FOLDER_ICON = checkNotNull(sharedImages.getImage(ISharedImages.IMG_OBJ_FOLDER));
        ONDEMAND_ANALYSES_ICON = checkNotNull(loadIcon(bundle, ONDEMAND_ANALYSES_ICON_FILE));
        REPORTS_ICON = checkNotNull(loadIcon(bundle, REPORTS_ICON_FILE));
        BUILT_IN_ONDEMAND_ICON = checkNotNull(loadIcon(bundle, BUILT_IN_ONDEMAND_FILE));
        USER_DEFINED_ONDEMAND_ICON = checkNotNull(loadIcon(bundle, USER_DEFINED_ONDEMAND_FILE));

        DEFAULT_TRACE_ICON = checkNotNull(loadIcon(bundle, TRACE_ICON_FILE));
        DEFAULT_EXPERIMENT_ICON = checkNotNull(loadIcon(bundle, EXPERIMENT_ICON_FILE));
        DEFAULT_ANALYSIS_ICON = checkNotNull(loadIcon(bundle, ANALYSIS_ICON_FILE));
        DEFAULT_VIEW_ICON = checkNotNull(loadIcon(bundle, VIEW_ICON_FILE));
        DEFAULT_REPORT_ICON = checkNotNull(loadIcon(bundle, DEFAULT_REPORT_ICON_FILE));
        VIEWS_ICON = checkNotNull(loadIcon(bundle, VIEWS_ICON_FILE));
        TRACECOMPASS_ICON = checkNotNull(loadIcon(bundle, TRACECOMPASS_ICON_FILE));
    }

    public static @Nullable Image loadIcon(Bundle bundle, String url) {
        if (bundle == null) {
            return null;
        }
        Activator plugin = Activator.getDefault();
        String key = bundle.getSymbolicName() + "/" + url; //$NON-NLS-1$
        Image icon = plugin.getImageRegistry().get(key);
        if (icon == null) {
            URL imageURL = bundle.getResource(url);
            ImageDescriptor descriptor = ImageDescriptor.createFromURL(imageURL);
            if (descriptor != null) {
                icon = descriptor.createImage();
                plugin.getImageRegistry().put(key, icon);
            }
        }
        return icon;
    }
}
