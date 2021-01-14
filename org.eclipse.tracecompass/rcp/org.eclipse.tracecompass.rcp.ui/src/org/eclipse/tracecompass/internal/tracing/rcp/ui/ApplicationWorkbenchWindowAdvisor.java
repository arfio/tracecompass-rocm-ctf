/**********************************************************************
 * Copyright (c) 2013, 2020 Ericsson
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
 *   Marc-Andre Laperle - Bug 459835
 **********************************************************************/
package org.eclipse.tracecompass.internal.tracing.rcp.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

/**
 * The WorkbenchAdvisor implementation of the LTTng RCP.
 *
 * @author Bernd Hufmann
 */
public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    @SuppressWarnings("nls")
    private static final String[] UNWANTED_ACTION_SET = {
        "org.eclipse.search.searchActionSet",
        "org.eclipse.rse.core.search.searchActionSet",
        "org.eclipse.debug.ui.launchActionSet",
        "org.eclipse.debug.ui.debugActionSet",
        "org.eclipse.debug.ui.breakpointActionSet",
        "org.eclipse.team.ui",
        "org.eclipse.ui.externaltools.ExternalToolsSet",
//        "org.eclipse.update.ui.softwareUpdates",
//        "org.eclipse.ui.edit.text.actionSet.navigation",
//        "org.eclipse.ui.actionSet.keyBindings",
//        "org.eclipse.ui.edit.text.actionSet.navigation",
        "org.eclipse.ui.edit.text.actionSet.convertLineDelimitersTo",
//        "org.eclipse.ui.edit.text.actionSet.annotationNavigation",
//        "org.eclipse.ui.NavigateActionSet",
//        "org.eclipse.jdt.ui.JavaActionSet",
//        "org.eclipse.jdt.ui.A_OpenActionSet",
//        "org.eclipse.jdt.ui.text.java.actionSet.presentation",
//        "org.eclipse.jdt.ui.JavaElementCreationActionSet",
//        "org.eclipse.jdt.ui.CodingActionSet",
//        "org.eclipse.jdt.ui.SearchActionSet",
//        "org.eclipse.jdt.debug.ui.JDTDebugActionSet",
        "org.eclipse.ui.edit.text.actionSet.openExternalFile",
//        "org.eclipse.debug.ui.profileActionSet"
    };

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Standard constructor
     *
     * @param configurer
     *            - the workbench window configurer
     */
    public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    @Override
    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new ApplicationActionBarAdvisor(configurer);
    }

    @Override
    public void preWindowOpen() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setShowCoolBar(false);
        configurer.setShowStatusLine(true);
        configurer.setShowProgressIndicator(true);
    }

    @Override
    public void postWindowCreate() {
        super.postWindowOpen();
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(new PerspectiveListener());
        hideActionSets();
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    /**
     * Hides the unwanted action sets
     */
    private static void hideActionSets() {

        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

        for (int i = 0; i < UNWANTED_ACTION_SET.length; i++) {
            page.hideActionSet(UNWANTED_ACTION_SET[i]);
        }
    }

    private static IProject createDefaultProject() {
        return TmfProjectRegistry.createProject(TmfCommonConstants.DEFAULT_TRACE_PROJECT_NAME, null, new NullProgressMonitor());
    }

    /**
     * A perspective listener implementation
     *
     * @author Bernd Hufmann
     */
    public class PerspectiveListener implements IPerspectiveListener {

        /**
         * Default Constructor
         */
        public PerspectiveListener() {
            // Do nothing
        }

        @Override
        public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
            createDefaultProject();
            hideActionSets();
        }

        @Override
        public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
            // Do nothing
        }
    }

}
