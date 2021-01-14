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
 *   Bernd Hufmann - Updated for support of LTTng Tools 2.1
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceEventType;
import org.eclipse.tracecompass.internal.lttng2.control.ui.Activator;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.IGetEventInfoDialog;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.TraceControlDialogFactory;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.BaseEventComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.KernelProviderComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TargetNodeComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceChannelComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.UstProviderComponent;
import org.eclipse.ui.IWorkbenchPage;

/**
 * <p>
 * Command handler implementation to assign events to a session and channel and enable/configure them.
 * This is done on the trace provider level.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class AssignEventHandler extends BaseControlViewHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The command execution parameter.
     */
    private Parameter fParam;

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        // Make a copy for thread safety
        Parameter tmpParam = null;
        fLock.lock();
        try {
            tmpParam = fParam;
            if (tmpParam == null)  {
                return null;
            }
            tmpParam = new Parameter(tmpParam);
        } finally {
            fLock.unlock();
        }
        final Parameter param = tmpParam;

        // Open dialog box to retrieve the session and channel where the events should be enabled in.
        final IGetEventInfoDialog dialog = TraceControlDialogFactory.getInstance().getGetEventInfoDialog();
        dialog.setDomain(param.getDomain());
        dialog.setSessions(param.getSessions());

        if (dialog.open() != Window.OK) {
            return null;
        }

        Job job = new Job(Messages.TraceControl_EnableEventsJob) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {

                Exception error = null;
                TraceSessionComponent session = dialog.getSession();
                try {
                    List<String> eventNames = new ArrayList<>();
                    List<BaseEventComponent> events = param.getEvents();
                    // Find the type of the events (all the events in the list are the same type)
                    TraceEventType  eventType = !events.isEmpty() ? events.get(0).getEventType() : null;
                    // Create list of event names
                    for (Iterator<BaseEventComponent> iterator = events.iterator(); iterator.hasNext();) {
                        BaseEventComponent baseEvent = iterator.next();
                        eventNames.add(baseEvent.getName());
                    }

                    TraceChannelComponent channel = dialog.getChannel();
                    if (TraceEventType.TRACEPOINT.equals(eventType)) {
                        if (channel == null) {
                            // enable events on default channel (which will be created by lttng-tools)
                            session.enableEvents(eventNames, param.getDomain(), dialog.getFilterExpression(), null, monitor);
                        } else {
                            channel.enableEvents(eventNames, dialog.getFilterExpression(), null, monitor);
                        }
                    } else if (TraceEventType.SYSCALL.equals(eventType)) {
                        if (channel == null) {
                            session.enableSyscalls(eventNames, monitor);
                        } else {
                            channel.enableSyscalls(eventNames, monitor);
                        }
                    }

                } catch (ExecutionException e) {
                    error = e;
                }

                // refresh in all cases
                if (session != null) {
                    refresh(new CommandParameter(session));
                }

                if (error != null) {
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TraceControl_EnableEventsFailure, error);
                }
                return Status.OK_STATUS;
            }
        };
        job.setUser(true);
        job.schedule();

        return null;
    }

    @Override
    public boolean isEnabled() {
        @NonNull ArrayList<@NonNull BaseEventComponent> events = new ArrayList<>();
        @NonNull TraceSessionComponent[] sessions = null;
        TraceDomainType domain = null;
        TraceEventType eventType = null;

        // Get workbench page for the Control View
        IWorkbenchPage page = getWorkbenchPage();
        if (page == null) {
            return false;
        }

        // Check if one or more session are selected
        ISelection selection = page.getSelection(ControlView.ID);
        if (selection instanceof StructuredSelection) {

            StructuredSelection structered = ((StructuredSelection) selection);
            for (Iterator<?> iterator = structered.iterator(); iterator.hasNext();) {
                Object element = iterator.next();
                if (element instanceof BaseEventComponent) {
                    BaseEventComponent event = (BaseEventComponent) element;
                    ITraceControlComponent provider = event.getParent();

                    // check for the domain provider
                    TraceDomainType temp = null;
                    if (provider instanceof KernelProviderComponent) {
                        temp = TraceDomainType.KERNEL;
                    } else if (provider instanceof UstProviderComponent) {
                        temp = TraceDomainType.UST; // Loggers are under the UST domain
                    } else {
                        return false;
                    }

                    if (domain == null) {
                        domain = temp;
                    } else {
                        // don't mix events from Kernel and UST provider
                        if (!domain.equals(temp)) {
                            return false;
                        }
                    }
                    // The events have to be the same type
                    if (eventType == null) {
                        eventType = event.getEventType();
                    } else if (!eventType.equals(event.getEventType())) {
                        events.clear();
                        break;
                    }

                    // Add BaseEventComponents
                    events.add(event);

                    if (sessions == null) {
                        TargetNodeComponent  root = (TargetNodeComponent)event.getParent().getParent().getParent();
                        sessions = root.getSessions();
                    }
                }
            }
        }

        boolean isEnabled = ((!events.isEmpty()) && (sessions != null) && (sessions.length > 0));

        // To avoid compiler warnings check for null even if isKernel is always not null when used below
        if (domain == null) {
            return false;
        }

        fLock.lock();
        try {
            fParam = null;
            if(isEnabled) {
                fParam = new Parameter(NonNullUtils.checkNotNull(sessions), events, domain);
            }
        } finally {
            fLock.unlock();
        }
        return isEnabled;
    }

    /**
     *  Class containing parameter for the command execution.
     */
    @NonNullByDefault
    private static final class Parameter {

        /**
         * The list of event components the command is to be executed on.
         */
        private final List<BaseEventComponent> fEvents;

        /**
         * The list of available sessions.
         */
        private final @NonNull TraceSessionComponent[] fSessions;

        /**
         * The domain type ({@link TraceDomainType})
         */
        private final TraceDomainType fDomain;

        /**
         * Constructor
         *
         * @param sessions - a array of trace sessions
         * @param events - a lists of events to enable
         * @param domain - domain type ({@link TraceDomainType})
         */
        public Parameter(@NonNull TraceSessionComponent[] sessions, List<BaseEventComponent> events, TraceDomainType domain) {
            fSessions = NonNullUtils.checkNotNull(Arrays.copyOf(sessions, sessions.length));
            fEvents = new ArrayList<>();
            fEvents.addAll(events);
            fDomain = domain;
        }

        /**
         * Copy constructor
         * @param other - a parameter to copy
         */
        public Parameter(Parameter other) {
            this(other.fSessions, other.fEvents, other.fDomain);
        }

        public TraceSessionComponent[] getSessions() {
            return fSessions;
        }

        public List<BaseEventComponent> getEvents() {
            return fEvents;
        }

        public TraceDomainType getDomain() {
            return fDomain;
        }
    }
}
