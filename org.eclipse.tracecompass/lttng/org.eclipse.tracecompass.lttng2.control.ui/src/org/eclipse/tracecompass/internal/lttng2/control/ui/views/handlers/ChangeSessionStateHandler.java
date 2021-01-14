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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.tracecompass.internal.lttng2.control.ui.Activator;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <p>
 * Abstract command handler implementation to start or stop one or more trace sessions.
 * </p>
 *
 * @author Bernd Hufmann
 */
public abstract class ChangeSessionStateHandler extends BaseControlViewHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The list of session components the command is to be executed on.
     */
    protected List<TraceSessionComponent> fSessions = new ArrayList<>();

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return new required state.
     */
    public abstract TraceSessionState getNewState();

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Performs the state change on given session.
     *
     * @param session
     *            - a session which state is to be changed
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public abstract void changeState(TraceSessionComponent session, IProgressMonitor monitor) throws ExecutionException;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        if (window == null) {
            return false;
        }

        fLock.lock();
        try {

            final List<TraceSessionComponent> sessions = new ArrayList<>();
            sessions.addAll(fSessions);

            Job job = new Job(Messages.TraceControl_ChangeSessionStateJob) {
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    try {
                        for (TraceSessionComponent session : sessions) {

                            // Change state of selected sessions
                            changeState(session, monitor);

                            // Set Session state
                            session.setSessionState(getNewState());
                            session.fireComponentChanged(session);
                        }
                    } catch (ExecutionException e) {
                        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TraceControl_ChangeSessionStateFailure, e);
                    }
                    return Status.OK_STATUS;
                }
            };
            job.setUser(true);
            job.schedule();
        } finally {
            fLock.unlock();
        }
        return null;
    }

    @Override
    public boolean isEnabled() {
        // Get workbench page for the Control View
        IWorkbenchPage page = getWorkbenchPage();
        if (page == null) {
            return false;
        }

        List<TraceSessionComponent> sessions = new ArrayList<>(0);

        // Check if one or more session are selected
        ISelection selection = page.getSelection(ControlView.ID);
        if (selection instanceof StructuredSelection) {
            StructuredSelection structered = ((StructuredSelection) selection);
            for (Iterator<?> iterator = structered.iterator(); iterator.hasNext();) {
                Object element = iterator.next();
                if (element instanceof TraceSessionComponent) {
                    // Add only TraceSessionComponents that are inactive and not destroyed
                    TraceSessionComponent session = (TraceSessionComponent) element;
                    if ((session.getSessionState() != getNewState()) && (!session.isDestroyed())) {
                        sessions.add(session);
                    }
                }
            }
        }
        boolean isEnabled = !sessions.isEmpty();
        fLock.lock();
        try {
            fSessions.clear();
            if (isEnabled) {
                fSessions = sessions;
            }
        } finally {
            fLock.unlock();
        }
        return isEnabled;
    }
}
