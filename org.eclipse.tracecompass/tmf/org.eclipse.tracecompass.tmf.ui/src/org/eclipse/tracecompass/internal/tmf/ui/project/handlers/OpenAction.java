/*******************************************************************************
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
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.handlers;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfAnalysisElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfAnalysisOutputElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfOnDemandAnalysisElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectModelElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfReportElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * <b><u>OpenAction</u></b>
 */
public class OpenAction extends Action {

    private static final String OPEN_COMMAND_ID = "org.eclipse.ui.navigate.openResource"; //$NON-NLS-1$

    private final IWorkbenchPage page;
    private final ISelectionProvider selectionProvider;
    private TmfProjectModelElement element;

    /**
     * Default constructor
     * @param page the workbench page
     * @param selectionProvider the selection provider
     */
    public OpenAction(IWorkbenchPage page, ISelectionProvider selectionProvider) {
        this.page = page;
        this.selectionProvider = selectionProvider;
    }

    @Override
    public boolean isEnabled() {
        ISelection selection = selectionProvider.getSelection();
        if (!selection.isEmpty()) {
            IStructuredSelection sSelection = (IStructuredSelection) selection;
            Object firstElement = sSelection.getFirstElement();
            if ((sSelection.size() == 1) && (firstElement instanceof TmfTraceElement ||
                    firstElement instanceof TmfExperimentElement ||
                    firstElement instanceof TmfOnDemandAnalysisElement ||
                    firstElement instanceof TmfAnalysisOutputElement ||
                    firstElement instanceof TmfReportElement ||
                    firstElement instanceof TmfAnalysisElement)) {
                element = (TmfProjectModelElement) firstElement;
                return true;
            }
        }
        return false;
    }

    @Override
    public void run() {
        try {
            Object service = page.getActivePart().getSite().getService(IHandlerService.class);
            IHandlerService handlerService = (IHandlerService) service;
            boolean executeCommand = (element instanceof TmfTraceElement ||
                    element instanceof TmfOnDemandAnalysisElement ||
                    element instanceof TmfAnalysisOutputElement ||
                    element instanceof TmfReportElement ||
                    element instanceof TmfAnalysisElement);

            if (!executeCommand && element instanceof TmfExperimentElement) {
                TmfExperimentElement experiment = (TmfExperimentElement) element;
                executeCommand = (!experiment.getTraces().isEmpty());
            }

            if (executeCommand) {
                handlerService.executeCommand(OPEN_COMMAND_ID, null);
            }
        } catch (ExecutionException e) {
            Activator.getDefault().logError("Error opening resource " + element.getName(), e); //$NON-NLS-1$
        } catch (NotDefinedException e) {
            Activator.getDefault().logError("Error opening resource " + element.getName(), e); //$NON-NLS-1$
        } catch (NotEnabledException e) {
            Activator.getDefault().logError("Error opening resource " + element.getName(), e); //$NON-NLS-1$
        } catch (NotHandledException e) {
            Activator.getDefault().logError("Error opening resource " + element.getName(), e); //$NON-NLS-1$
        }
    }

}
