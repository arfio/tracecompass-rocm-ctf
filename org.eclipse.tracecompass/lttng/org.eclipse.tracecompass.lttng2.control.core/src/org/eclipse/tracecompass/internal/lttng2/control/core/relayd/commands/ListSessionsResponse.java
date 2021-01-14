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

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

/**
 * VIEWER_LIST_SESSIONS payload.
 *
 * @author Matthew Khouzam
 */
public class ListSessionsResponse implements IRelayResponse {

    /**
     * Response size (nbSessions)
     */
    public static final int PACKET_FIXED_SIZE = Integer.SIZE / 8;

    /** the list of sessions */
    private final List<SessionResponse> fSessionList;

    /**
     * List Sessions response from network
     *
     * @param inNet
     *            the network stream
     * @throws IOException
     *             network error
     */
    public ListSessionsResponse(DataInputStream inNet) throws IOException {
        byte[] data = new byte[PACKET_FIXED_SIZE];
        inNet.readFully(data);
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.BIG_ENDIAN);
        int nbSessions = bb.getInt();
        Builder<SessionResponse> sl = new ImmutableList.Builder<>();
        for (int session = 0; session < nbSessions; session++) {
            sl.add(new SessionResponse(inNet));
        }
        fSessionList = sl.build();
    }

    /**
     * Gets the session list
     *
     * @return the sessions list
     */
    public List<SessionResponse> getSessionsList() {
        return fSessionList;
    }


}