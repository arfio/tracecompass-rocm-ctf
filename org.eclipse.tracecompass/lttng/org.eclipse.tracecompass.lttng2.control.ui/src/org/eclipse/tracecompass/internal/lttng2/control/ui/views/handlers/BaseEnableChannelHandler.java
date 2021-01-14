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
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.window.Window;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IChannelInfo;
import org.eclipse.tracecompass.internal.lttng2.control.ui.Activator;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.IEnableChannelDialog;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.TraceControlDialogFactory;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceDomainComponent;

/**
 * <p>
 * Base implementation of a command handler to enable a trace channel.
 * </p>
 *
 * @author Bernd Hufmann
 */
abstract class BaseEnableChannelHandler extends BaseControlViewHandler {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    protected CommandParameter fParam;

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Enables channels with given names which are part of this domain. If a
     * given channel doesn't exists it creates a new channel with the given
     * parameters (or default values if given parameter is null).
     *
     * @param param
     *            - a parameter instance with data for the command execution
     * @param channelNames
     *            - a list of channel names to enable on this domain
     * @param info
     *            - channel information to set for the channel (use null for
     *            default)
     * @param domain
     *            - indicate the domain type ({@link TraceDomainType})
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If something goes wrong when enabling the channel
     */
    public abstract void enableChannel(CommandParameter param,
            List<String> channelNames, IChannelInfo info, TraceDomainType domain,
            IProgressMonitor monitor) throws ExecutionException;

    /**
     * @param param - a parameter instance with data for the command execution
     * @return returns the relevant domain (null if domain is not known)
     */
    public abstract TraceDomainComponent getDomain(CommandParameter param);

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        CommandParameter tmpParam = null;

        fLock.lock();
        try {
            tmpParam = fParam;
            if (tmpParam == null) {
                return null;
            }
            tmpParam = tmpParam.clone();
        } finally {
            fLock.unlock();
        }
        final CommandParameter param = tmpParam;

        final IEnableChannelDialog dialog =  TraceControlDialogFactory.getInstance().getEnableChannelDialog();
        dialog.setTargetNodeComponent(param.getSession().getTargetNode());
        dialog.setDomainComponent(getDomain(param));
        dialog.setHasKernel(param.getSession().hasKernelProvider());

        if (dialog.open() != Window.OK) {
            return null;
        }

        Job job = new Job(Messages.TraceControl_CreateChannelStateJob) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                Exception error = null;

                List<String> channelNames = new ArrayList<>();
                channelNames.add(dialog.getChannelInfo().getName());

                try {
                    enableChannel(param, channelNames, dialog.getChannelInfo(), dialog.getDomain(), monitor);
                } catch (ExecutionException e) {
                    error = e;
                }

                // refresh in all cases
                refresh(param);

                if (error != null) {
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TraceControl_CreateChannelStateFailure, error);
                }
                return Status.OK_STATUS;
            }
        };
        job.setUser(true);
        job.schedule();

        return null;
    }

}
