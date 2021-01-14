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
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.stubs.dialogs;

import java.util.Arrays;

import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.IGetEventInfoDialog;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceChannelComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;

/**
 * Get event information dialog stub implementation.
 */
public class GetEventInfoDialogStub implements IGetEventInfoDialog {

    private TraceSessionComponent[] fSessions;
    private String fFilterExpression;

    @Override
    public TraceSessionComponent getSession() {
        return fSessions[0];
    }

    @Override
    public TraceChannelComponent getChannel() {
        return null;
    }

    @Override
    public void setDomain(TraceDomainType domain) {
    }

    @Override
    public void setSessions(TraceSessionComponent[] sessions) {
        if (sessions != null) {
            fSessions = Arrays.copyOf(sessions, sessions.length);
            return;
        }
        fSessions = null;
    }

    @Override
    public String getFilterExpression() {
        return fFilterExpression;
    }

    @Override
    public int open() {
        return 0;
    }

    /**
     * @param filter the filter to set
     */
    public void setFilterExpression(String filter) {
        fFilterExpression = filter;
    }
}

