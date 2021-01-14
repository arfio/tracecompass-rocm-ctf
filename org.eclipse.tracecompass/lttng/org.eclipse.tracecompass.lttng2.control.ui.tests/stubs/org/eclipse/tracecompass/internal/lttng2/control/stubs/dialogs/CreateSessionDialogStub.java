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
 *   Marc-Andre Laperle - Support for creating a live session
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.stubs.dialogs;

import org.eclipse.tracecompass.internal.lttng2.control.core.model.ISessionInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.SessionInfo;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.ICreateSessionDialog;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionGroup;

/**
 * Create session dialog stub implementation.
 */
@SuppressWarnings("javadoc")
public class CreateSessionDialogStub implements ICreateSessionDialog {

    public String fName = "mysession";
    public String fPath = null;
    private boolean fIsStreamedTrace = false;
    private String fNetworkUrl = null;
    private String fControlUrl = null;
    private String fDataUrl = null;
    private boolean fIsSnapshot;
    private boolean fIsLive = false;
    private long fLiveDelay = 0;

    @Override
    public void initialize(TraceSessionGroup group) {
    }

    @Override
    public int open() {
        return 0;
    }

    public void setSessionPath(String path) {
        fPath = path;
    }

    public void setSessionName(String name) {
        fName = name;
    }

    public void setStreamedTrace(boolean isStreamedTrace) {
        fIsStreamedTrace = isStreamedTrace;
    }

    public void setNetworkUrl(String fNetworkUrl) {
        this.fNetworkUrl = fNetworkUrl;
    }

    public void setControlUrl(String fControlUrl) {
        this.fControlUrl = fControlUrl;
    }

    public void setDataUrl(String fDataUrl) {
        this.fDataUrl = fDataUrl;
    }

    public void setSnapshot(boolean isSnapshot) {
        fIsSnapshot = isSnapshot;
    }

    public void setLive(boolean isLive) {
        fIsLive = isLive;
    }

    public void setLiveDelay(int liveDelay) {
        fLiveDelay = liveDelay;
    }

    @Override
    public ISessionInfo getParameters() {
        ISessionInfo sessionInfo = new SessionInfo(fName);

        if (fIsStreamedTrace) {
            sessionInfo.setNetworkUrl(fNetworkUrl);
            sessionInfo.setControlUrl(fControlUrl);
            sessionInfo.setDataUrl(fDataUrl);
            sessionInfo.setStreamedTrace(true);
        } else if (fPath != null) {
            sessionInfo.setSessionPath(fPath);
        }

        sessionInfo.setLive(fIsLive);
        sessionInfo.setLiveDelay(fLiveDelay);
        sessionInfo.setSnapshot(fIsSnapshot);

        return sessionInfo;
    }
}
