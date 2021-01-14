/**********************************************************************
 * Copyright (c) 2012, 2015 Ericsson
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
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.handlers;

import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.lttng2.control.ui.Activator;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <p>
 * Abstract Command handler implementation for all control view handlers.
 * </p>
 *
 * @author Bernd Hufmann
 */
public abstract class BaseControlViewHandler extends AbstractHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The synchronization lock.
     */
    protected final ReentrantLock fLock = new ReentrantLock();

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * @return returns the workbench page for the Control View
     */
    protected IWorkbenchPage getWorkbenchPage() {
        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }

        // Check if we are in the Project View
        IWorkbenchPage page = window.getActivePage();
        if (page == null) {
            return null;
        }

        IWorkbenchPart part = page.getActivePart();
        if (!(part instanceof ControlView)) {
            return null;
        }
        return page;
    }

    /**
     * Refreshes the session information based on given session (in CommandParameter)
     * @param param - command parameter containing the session to refresh
     */
    protected void refresh(final @NonNull CommandParameter param) {
        Job job = new Job(Messages.TraceControl_RetrieveNodeConfigurationJob) {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    param.getSession().getConfigurationFromNode(monitor);
                } catch (ExecutionException e) {
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TraceControl_ListSessionFailure, e);
                }
                return Status.OK_STATUS;
            }
        };
        job.setUser(true);
        job.schedule();
    }

}