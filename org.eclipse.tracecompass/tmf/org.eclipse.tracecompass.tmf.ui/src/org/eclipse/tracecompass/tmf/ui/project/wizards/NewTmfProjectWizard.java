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
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann -  Moved project creation utility method to TmfProjectRegistry
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.wizards;

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

/**
 * Wizard implementation for creating a TMF tracing project.
 *
 * @author Francois Chouinard
 */
public class NewTmfProjectWizard extends Wizard implements INewWizard, IExecutableExtension {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The wizard id
     */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.ui.wizards.newProject"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final String fTtitle;
    private final String fDescription;

    /**
     * Wizard main page
     */
    protected NewTmfProjectMainWizardPage fMainPage;

    /**
     * The Project name
     */
    protected String fProjectName;

    /**
     * The project location
     */

    protected URI fProjectLocation;

    /**
     * The configuration element.
     */
    protected IConfigurationElement fConfigElement;

    /**
     * The project reference
     */
    protected IProject fProject;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public NewTmfProjectWizard() {
        this(Messages.NewProjectWizard_DialogHeader, Messages.NewProjectWizard_DialogMessage);
    }

    /**
     * Constructor
     * @param title The tile string
     * @param desc The description string
     */
    public NewTmfProjectWizard(String title, String desc) {
        super();
        setDialogSettings(Activator.getDefault().getDialogSettings());
        setNeedsProgressMonitor(true);
        setForcePreviousAndNextButtons(true);
        setWindowTitle(title);
        fTtitle = title;
        fDescription = desc;
    }

    // ------------------------------------------------------------------------
    // Wizard
    // ------------------------------------------------------------------------

    @Override
    public void addPages() {
        fMainPage = new NewTmfProjectMainWizardPage(Messages.NewProjectWizard_DialogHeader);
        fMainPage.setTitle(fTtitle);
        fMainPage.setDescription(fDescription);
        addPage(fMainPage);
    }

    @Override
    public boolean performCancel() {
        return true;
    }

    @Override
    public boolean performFinish() {
        fProjectName = fMainPage.getProjectName();
        fProjectLocation = fMainPage.useDefaults() ? null : fMainPage.getLocationURI();
        fProject = TmfProjectRegistry.createProject(fProjectName, fProjectLocation, new NullProgressMonitor());
        BasicNewProjectResourceWizard.updatePerspective(fConfigElement);
        return true;
    }

    // ------------------------------------------------------------------------
    // INewWizard
    // ------------------------------------------------------------------------

    @Override
    public void init(IWorkbench iworkbench, IStructuredSelection istructuredselection) {
        // Do nothing
    }

    // ------------------------------------------------------------------------
    // IExecutableExtension
    // ------------------------------------------------------------------------

    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
        fConfigElement = config;
    }

}
