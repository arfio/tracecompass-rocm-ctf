/*******************************************************************************
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
 *   Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.pcap.core.protocol.tcp;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.pcap.core.endpoint.ProtocolEndpoint;

/**
 * Class that extends the {@link ProtocolEndpoint} class. It represents the
 * endpoint at a TCP level.
 *
 * @author Vincent Perot
 */
public class TCPEndpoint extends ProtocolEndpoint {

    private final int fPort;

    /**
     * Constructor of the {@link TCPEndpoint} class. It takes a packet to get
     * its endpoint. Since every packet has two endpoints (source and
     * destination), the isSourceEndpoint parameter is used to specify which
     * endpoint to take.
     *
     * @param packet
     *            The packet that contains the endpoints.
     * @param isSourceEndpoint
     *            Whether to take the source or the destination endpoint of the
     *            packet.
     */
    public TCPEndpoint(TCPPacket packet, boolean isSourceEndpoint) {
        super(packet, isSourceEndpoint);
        fPort = isSourceEndpoint ? packet.getSourcePort() : packet.getDestinationPort();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        ProtocolEndpoint endpoint = getParentEndpoint();
        if (endpoint == null) {
            result = 0;
        } else {
            result = endpoint.hashCode();
        }
        result = prime * result + fPort;
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TCPEndpoint)) {
            return false;
        }

        TCPEndpoint other = (TCPEndpoint) obj;

        // Check on layer
        boolean localEquals = (fPort == other.fPort);
        if (!localEquals) {
            return false;
        }

        // Check above layers.
        ProtocolEndpoint endpoint = getParentEndpoint();
        if (endpoint != null) {
            return endpoint.equals(other.getParentEndpoint());
        }
        return true;
    }

    @Override
    public String toString() {
        ProtocolEndpoint endpoint = getParentEndpoint();
        if (endpoint == null) {
            return String.valueOf(fPort);
        }
        return endpoint.toString() + '/' + fPort;
    }

}
