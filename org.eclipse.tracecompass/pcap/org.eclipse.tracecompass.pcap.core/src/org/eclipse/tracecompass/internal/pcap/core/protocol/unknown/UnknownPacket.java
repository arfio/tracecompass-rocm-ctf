/*******************************************************************************
 * Copyright (c) 2014, 2019 Ericsson
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

package org.eclipse.tracecompass.internal.pcap.core.protocol.unknown;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.pcap.core.packet.Packet;
import org.eclipse.tracecompass.internal.pcap.core.protocol.PcapProtocol;
import org.eclipse.tracecompass.internal.pcap.core.trace.PcapFile;
import org.eclipse.tracecompass.internal.pcap.core.util.ConversionHelper;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

/**
 * Class that represents an Unknown packet. It is possible to get such a packet
 * if the protocol has not been implemented in this library or if the parent
 * packet was invalid (in certain cases only). The header of such a packet is
 * inexistent.
 *
 * @author Vincent Perot
 */
public class UnknownPacket extends Packet {

    private final @Nullable Packet fChildPacket;
    private final ByteBuffer fPayload;

    private @Nullable UnknownEndpoint fSourceEndpoint;
    private @Nullable UnknownEndpoint fDestinationEndpoint;

    private @Nullable Map<String, String> fFields;

    /**
     * Constructor of an Unknown Packet object.
     *
     * @param file
     *            The file to which this packet belongs.
     * @param parent
     *            The parent packet of this packet.
     * @param packet
     *            The entire packet (header and payload).
     */
    public UnknownPacket(PcapFile file, @Nullable Packet parent, ByteBuffer packet) {
        super(file, parent, PcapProtocol.UNKNOWN);

        // The endpoints are lazy loaded. They are defined in the get*Endpoint()
        // methods.
        fSourceEndpoint = null;
        fDestinationEndpoint = null;

        fFields = null;

        // Header is not used. All data go into payload.
        fPayload = packet;

        fChildPacket = findChildPacket();
    }

    @Override
    public @Nullable Packet getChildPacket() {
        return fChildPacket;
    }

    @Override
    public @Nullable ByteBuffer getPayload() {
        return fPayload;
    }

    @Override
    protected @Nullable Packet findChildPacket() {
        return null;
    }

    @Override
    public String toString() {
        byte[] array = Arrays.copyOfRange(fPayload.array(), fPayload.arrayOffset(), fPayload.arrayOffset() + fPayload.limit());
        String string = "Payload: " + ConversionHelper.bytesToHex(array, true); //$NON-NLS-1$
        final Packet child = fChildPacket;
        if (child != null) {
            return string + child.toString();
        }
        return string;
    }

    @Override
    public boolean validate() {
        // Not yet implemented. ATM, we consider that all packets are valid.
        // This is the case for all packets.
        // TODO Implement it.
        return true;
    }

    @Override
    public UnknownEndpoint getSourceEndpoint() {
        @Nullable
        UnknownEndpoint endpoint = fSourceEndpoint;
        if (endpoint == null) {
            endpoint = new UnknownEndpoint(this, true);
        }
        fSourceEndpoint = endpoint;
        return fSourceEndpoint;
    }

    @Override
    public UnknownEndpoint getDestinationEndpoint() {
        @Nullable
        UnknownEndpoint endpoint = fDestinationEndpoint;
        if (endpoint == null) {
            endpoint = new UnknownEndpoint(this, false);
        }
        fDestinationEndpoint = endpoint;
        return fDestinationEndpoint;
    }

    @Override
    public Map<String, String> getFields() {
        Map<String, String> map = fFields;
        if (map == null) {
            byte[] array = Arrays.copyOfRange(fPayload.array(), fPayload.arrayOffset(), fPayload.arrayOffset() + fPayload.limit());

            Builder<String, String> builder = ImmutableMap.<@NonNull String, @NonNull String> builder()
                    .put("Binary", ConversionHelper.bytesToHex(array, true)); //$NON-NLS-1$
            try {
                String s = new String(array, "UTF-8"); //$NON-NLS-1$
                builder.put("Character", s); //$NON-NLS-1$
            } catch (UnsupportedEncodingException e) {
                // Do nothing. The string won't be added to the map anyway.
            }
            fFields = builder.build();
            return fFields;
        }
        return map;
    }

    @Override
    public String getLocalSummaryString() {
        return "Len: " + fPayload.limit() + " bytes"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected String getSignificationString() {
        return "Data: " + fPayload.limit() + " bytes"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public Packet getMostEcapsulatedPacket() {
        Packet packet = this.getParentPacket();
        if (packet == null) {
            return this;
        }
        return packet;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = prime;
        final Packet child = fChildPacket;
        if (child != null) {
            result += child.hashCode();
        }
        if (child == null) {
            result = prime * result + payloadHashCode(fPayload);
        }
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        UnknownPacket other = (UnknownPacket) obj;
        if (!Objects.equals(fChildPacket, other.fChildPacket)) {
            return false;
        }
        if (fChildPacket == null && !payloadEquals(fPayload, other.fPayload)) {
            return false;
        }
        return true;
    }

}
