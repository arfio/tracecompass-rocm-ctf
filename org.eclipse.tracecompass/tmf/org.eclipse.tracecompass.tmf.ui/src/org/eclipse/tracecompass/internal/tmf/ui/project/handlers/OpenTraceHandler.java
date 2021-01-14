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
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfOpenTraceHelper;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>OpenTraceHandler</u></b>
 * <p>
 * TODO: Add support for multiple trace selection
 */
public class OpenTraceHandler extends AbstractHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private TmfTraceElement fTrace = null;

    // ------------------------------------------------------------------------
    // Validation
    // ------------------------------------------------------------------------

    @Override
    public boolean isEnabled() {

        // Check if we are closing down
        final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return false;
        }

        // Get the selection
        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        final IWorkbenchPart part = page.getActivePart();
        if (part == null) {
            return false;
        }
        final ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();
        if (selectionProvider == null) {
            return false;
        }
        final ISelection selection = selectionProvider.getSelection();

        // Make sure there is only one selection and that it is a trace
        fTrace = null;
        if (selection instanceof TreeSelection) {
            final TreeSelection sel = (TreeSelection) selection;
            // There should be only one item selected as per the plugin.xml
            final Object element = sel.getFirstElement();
            if (element instanceof TmfTraceElement) {
                fTrace = (TmfTraceElement) element;
            }
        }

        // We only enable opening from the Traces folder for now
        return (fTrace != null);
    }

    // ------------------------------------------------------------------------
    // Execution
    // ------------------------------------------------------------------------

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {

        // Check if we are closing down
        final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }

        // Check that the trace is valid
        if (fTrace == null) {
            return null;
        }

        // If trace is under an experiment, use the original trace from the traces folder
        IStatus status = TmfOpenTraceHelper.openFromElement(fTrace.getElementUnderTraceFolder());
        if (!status.isOK()) {
            Activator.getDefault().logError("Error opening trace: " + status.getMessage()); //$NON-NLS-1$
        }
        return null;
    }

}
