/**********************************************************************
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
 *   Bernd Hufmann - Initial API and implementation
 *   Bernd Hufmann - Updated for support of LTTng Tools 2.1
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.ISessionInfo;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.ITraceControlComponent;

/**
 * <p>
 * Implementation of the trace session group.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class TraceSessionGroup extends TraceControlComponent {
    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * Path to icon file for this component.
     */
    public static final String TRACE_SESSIONS_ICON_FILE = "icons/obj16/sessions.gif"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param name - the name of the component.
     * @param parent - the parent of this component.
     */
    public TraceSessionGroup(String name, ITraceControlComponent parent) {
        super(name, parent);
        setImage(TRACE_SESSIONS_ICON_FILE);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return the parent target node
     */
    public TargetNodeComponent getTargetNode() {
        return (TargetNodeComponent)getParent();
    }

    /**
     * Returns if node supports networks streaming or not
     * @return <code>true</code> if node supports filtering else <code>false</code>
     */
    public boolean isNetworkStreamingSupported() {
        return getTargetNode().isNetworkStreamingSupported();
    }
    /**
     * Returns if node supports snapshots or not
     * @return <code>true</code> if it supports snapshots else <code>false</code>
     *
     */    public boolean isSnapshotSupported() {
        return getTargetNode().isSnapshotSupported();
    }

    /**
     * Returns if node supports live or not
     *
     * @return <code>true</code> if it supports live else <code>false</code>
     */
    public boolean isLiveSupported() {
        return getTargetNode().isLiveSupported();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Retrieves the sessions information from the node.
     *
     * @throws ExecutionException
     *             If the command fails
     */
    public void getSessionsFromNode() throws ExecutionException {
        getSessionsFromNode(new NullProgressMonitor());
    }

    /**
     * Retrieves the sessions information from the node.
     *
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void getSessionsFromNode(IProgressMonitor monitor)
            throws ExecutionException {
        List<String> sessionNames = getControlService().getSessionNames(monitor);
        for (String sessionName : sessionNames) {
            TraceSessionComponent session =
                    new TraceSessionComponent(sessionName, this);
            addChild(session);
            session.getConfigurationFromNode(monitor);
        }
    }

    /**
     * Creates a session with given session name and location.
     *
     * @param sessionInf
     *            the session information used to create the session
     *
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void createSession(ISessionInfo sessionInf, IProgressMonitor monitor) throws ExecutionException {
        ISessionInfo sessionInfo = getControlService().createSession(sessionInf, monitor);

        if (sessionInfo != null) {
            TraceSessionComponent session = new TraceSessionComponent(sessionInfo, TraceSessionGroup.this);
            addChild(session);
            session.getConfigurationFromNode(monitor);
        }
    }

    /**
     * Command to execute a list of commands
     * @param monitor
     *            - a progress monitor
     * @param commands
     *            - a list of commands to execute
     * @throws ExecutionException
     *            If the command fails
     */
    public void executeCommands(IProgressMonitor monitor, List<String> commands) throws ExecutionException {
        getControlService().runCommands(monitor, commands);
        getTargetNode().refresh();
    }

    /**
     * Destroys a session with given session name.
     *
     * @param session
     *            - a session component to destroy
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void destroySession(TraceSessionComponent session,
            IProgressMonitor monitor) throws ExecutionException {
        getControlService().destroySession(session.getName(), monitor);
        session.removeAllChildren();
        removeChild(session);
    }

    /**
     * Load all or a given session.
     *
     * @param inputPath
     *            a input path to load session from or null for load all from default
     * @param isForce
     *            flag whether to overwrite existing or not
     * @param monitor
     *            a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void loadSession(@Nullable String inputPath, boolean isForce, IProgressMonitor monitor)
            throws ExecutionException {
        getControlService().loadSession(inputPath, isForce, monitor);
        getTargetNode().refresh();
    }
}
