/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal and others
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
 *   Marc-Andre Laperle - Add method to get opened tmf projects
 *   Patrick Tasse - Add support for folder elements
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.project.model.TmfProjectModelHelper;
import org.eclipse.tracecompass.tmf.core.TmfProjectNature;
import org.eclipse.ui.PlatformUI;

/**
 * Utility class for common tmf.ui functionalities
 */
public class TraceUtils {

    private TraceUtils() {
    }

    /**
     * Displays an error message in a box
     *
     * @param boxTitle
     *            The message box title
     * @param errorMsg
     *            The error message to display
     */
    public static void displayErrorMsg(final String boxTitle, final String errorMsg) {
        displayErrorMsg(boxTitle, errorMsg, null);
    }

    /**
     * Displays an error message in a box
     *
     * @param exception
     *            the exception or null if the error does not originate from an
     *            exception
     * @since 5.1
     */
    public static void displayErrorMsg(Throwable exception) {
        displayErrorMsg(exception.getClass().getSimpleName(), exception.getMessage(), exception);
    }

    /**
     * Displays a warning message in a box
     *
     * @param exception
     *            the exception or null if the error does not originate from an
     *            exception
     * @since 5.1
     */
    public static void displayWarningMsg(Throwable exception) {
        final String warningMsg = exception.getMessage();
        Display.getDefault().asyncExec(() -> {
            final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
            Activator.getDefault().logWarning(warningMsg, exception);
            MessageDialog.openWarning(shell, exception.getClass().getSimpleName(), warningMsg);
        });
    }

    /**
     * Displays an error message in a box
     *
     * @param boxTitle
     *            The message box title
     * @param errorMsg
     *            The error message to display
     * @param exception
     *            the exception or null if the error does not originate from an
     *            exception
     * @since 2.2
     */
    public static void displayErrorMsg(final String boxTitle, final String errorMsg, Throwable exception) {
        Display.getDefault().asyncExec(() -> {
            final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
            Activator.getDefault().logError(errorMsg, exception);
            MessageDialog.openError(shell, boxTitle, errorMsg);
        });
    }

    /**
     * Get the opened (accessible) projects with Tmf nature
     *
     * @return the Tmf projects
     */
    public static List<IProject> getOpenedTmfProjects() {
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        List<IProject> tmfProjects = new ArrayList<>();
        for (IProject project : projects) {
            try {
                if (project.isAccessible() && project.getNature(TmfProjectNature.ID) != null && !TmfProjectModelHelper.isShadowProject(project)) {
                    tmfProjects.add(project);
                }
            } catch (CoreException e) {
                Activator.getDefault().logError("Error getting opened tmf projects", e); //$NON-NLS-1$
            }
        }
        return tmfProjects;
    }

    /**
     * Create a folder, ensuring all parent folders are also created.
     *
     * @param folder
     *            the folder to create
     * @param monitor
     *            the progress monitor
     * @throws CoreException
     *            if the folder cannot be created
     */
    public static void createFolder(IFolder folder, IProgressMonitor monitor) throws CoreException {
        if (!folder.exists()) {
            if (folder.getParent() instanceof IFolder) {
                createFolder((IFolder) folder.getParent(), monitor);
            }
            folder.create(true, true, monitor);
        }
    }

}
