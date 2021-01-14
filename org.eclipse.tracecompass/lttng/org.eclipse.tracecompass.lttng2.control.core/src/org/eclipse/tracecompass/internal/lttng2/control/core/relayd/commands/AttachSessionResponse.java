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
 * Attach session response
 *
 * @author Matthew Khouzam
 */
public class AttachSessionResponse implements IRelayResponse {

    /**
     * Response size
     *
     * fStatus + fStreamsCount (first half of a packet) */
    private static final int SIZE = (Integer.SIZE + Integer.SIZE) / 8;
    /** enum lttng_viewer_attach_return_code */
    private final AttachReturnCode fStatus;
    /** how many streams are there */
    private final int fStreamsCount;
    /** public class lttng_viewer_stream */
    private final List<StreamResponse> fStreamList;

    /**
     * Attach session response network constructor
     *
     * @param inNet
     *            network input stream
     * @throws IOException
     *             network error
     */
    public AttachSessionResponse(DataInputStream inNet) throws IOException {
        byte[] data = new byte[SIZE];
        inNet.readFully(data, 0, SIZE);
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.BIG_ENDIAN);
        fStatus = AttachReturnCode.values()[bb.getInt() - 1];
        fStreamsCount = bb.getInt();
        Builder<StreamResponse> streamResponses = ImmutableList.builder();
        for (int i = 0; i < getNbStreams(); i++) {
            streamResponses.add(new StreamResponse(inNet));
        }
        fStreamList = streamResponses.build();

    }

    /**
     * Gets the Status
     *
     * @return the Status
     */
    public AttachReturnCode getStatus() {
        return fStatus;
    }

    /**
     * Gets the StreamsCount
     *
     * @return the StreamsCount
     */
    public int getNbStreams() {
        return fStreamsCount;
    }

    /**
     * Gets the StreamList
     *
     * @return the StreamList
     */
    public List<StreamResponse> getStreamList() {
        return fStreamList;
    }

}