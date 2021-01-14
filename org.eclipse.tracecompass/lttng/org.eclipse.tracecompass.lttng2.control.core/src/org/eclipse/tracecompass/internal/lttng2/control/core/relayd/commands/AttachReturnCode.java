/**********************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial implementation and API
 *   Marc-Andre Laperle - Initial implementation and API
 **********************************************************************/

package org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands;

/**
 * Return codes for "viewer attach" command
 *
 * @author Matthew Khouzam
 */
public enum AttachReturnCode implements IBaseCommand {

    /** If the attach command succeeded. */
    VIEWER_ATTACH_OK(1),
    /** If a viewer is already attached. */
    VIEWER_ATTACH_ALREADY(2),
    /** If the session ID is unknown. */
    VIEWER_ATTACH_UNK(3),
    /** If the session is not live. */
    VIEWER_ATTACH_NOT_LIVE(4),
    /** Seek error. */
    VIEWER_ATTACH_SEEK_ERR(5),
    /** No session */
    VIEWER_ATTACH_NO_SESSION(6);

    private final int fCode;

    private AttachReturnCode(int c) {
        fCode = c;
    }

    @Override
    public int getCommand() {
        return fCode;
    }
}