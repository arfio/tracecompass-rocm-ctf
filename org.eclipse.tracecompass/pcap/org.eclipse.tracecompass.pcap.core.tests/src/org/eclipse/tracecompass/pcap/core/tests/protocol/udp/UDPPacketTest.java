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
 *   Viet-Hung Phan - Support pcapNg
 *******************************************************************************/

package org.eclipse.tracecompass.pcap.core.tests.protocol.udp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

import org.eclipse.tracecompass.internal.pcap.core.packet.BadPacketException;
import org.eclipse.tracecompass.internal.pcap.core.protocol.PcapProtocol;
import org.eclipse.tracecompass.internal.pcap.core.protocol.udp.UDPEndpoint;
import org.eclipse.tracecompass.internal.pcap.core.protocol.udp.UDPPacket;
import org.eclipse.tracecompass.internal.pcap.core.trace.BadPcapFileException;
import org.eclipse.tracecompass.internal.pcap.core.trace.PcapFile;
import org.eclipse.tracecompass.pcap.core.tests.shared.PcapTestTrace;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

/**
 * JUnit Class that tests the UDPPacket class and its method.
 *
 * @author Vincent Perot
 */
public class UDPPacketTest {

    private static final Map<String, String> EXPECTED_FIELDS = ImmutableMap.of(
            "Source Port", "18057",
            "Destination Port", "39611",
            "Length", "41452 bytes",
            "Checksum", "0xfaaf"
            );

    private static final String EXPTECTED_TOSTRING;
    static {
        StringBuilder sb = new StringBuilder();
        sb.append("User Datagram Protocol, Source Port: 18057, Destination Port: 39611, Length: 41452, Checksum: 64175\n");
        sb.append("Payload: 99 88 77 66");

        EXPTECTED_TOSTRING = sb.toString();
    }

    private ByteBuffer fPacket;

    /**
     * Initialize the packet.
     */
    @Before
    public void initialize() {
        fPacket = ByteBuffer.allocate(12);
        fPacket.order(ByteOrder.BIG_ENDIAN);

        // Source Port
        fPacket.put((byte) 0x46);
        fPacket.put((byte) 0x89);

        // Destination Port
        fPacket.put((byte) 0x9A);
        fPacket.put((byte) 0xBB);

        // Total length - this is randomly chosen so that we verify that the
        // packet handles wrong total length.
        fPacket.put((byte) 0xA1);
        fPacket.put((byte) 0xEC);

        // Checksum
        fPacket.put((byte) 0xFA);
        fPacket.put((byte) 0xAF);

        // Payload - 4 bytes
        fPacket.put((byte) 0x99);
        fPacket.put((byte) 0x88);
        fPacket.put((byte) 0x77);
        fPacket.put((byte) 0x66);

        fPacket.flip();
    }

    /**
     * Test that verify the correctness of the UDPPacket's methods.
     * @throws BadPcapFileException
     *             Thrown when the file is erroneous. Fails the test.
     * @throws IOException
     *             Thrown when an IO error occurs. Fails the test.
     * @throws BadPacketException
     *             Thrown when a packet is erroneous. Fails the test.
     */
    @Test
    public void CompleteUDPPacketTest() throws IOException, BadPcapFileException, BadPacketException {
        PcapTestTrace trace = PcapTestTrace.MOSTLY_TCP;
        assumeTrue(trace.exists());
        try (PcapFile file = trace.getTrace()) {
            ByteBuffer byteBuffer = fPacket;
            if (byteBuffer == null) {
                fail("CompleteUDPPacketTest has failed!");
                return;
            }
            UDPPacket packet = new UDPPacket(file, null, byteBuffer);

            // Protocol Testing
            assertEquals(PcapProtocol.UDP, packet.getProtocol());
            assertTrue(packet.hasProtocol(PcapProtocol.UDP));
            assertTrue(packet.hasProtocol(PcapProtocol.UNKNOWN));
            assertFalse(packet.hasProtocol(PcapProtocol.ETHERNET_II));

            // Abstract methods Testing
            assertTrue(packet.validate());
            UDPPacket expected = new UDPPacket(file, null, byteBuffer);
            assertEquals(expected.hashCode(), packet.hashCode());
            assertEquals(expected, packet);

            assertEquals(EXPECTED_FIELDS, packet.getFields());
            assertEquals(EXPTECTED_TOSTRING, packet.toString());
            assertEquals("Src Port: 18057, Dst Port: 39611", packet.getLocalSummaryString());
            assertEquals("Source Port: 18057, Destination Port: 39611", packet.getGlobalSummaryString());

            assertEquals(new UDPEndpoint(packet, true), packet.getSourceEndpoint());
            assertEquals(new UDPEndpoint(packet, false), packet.getDestinationEndpoint());

            fPacket.position(8);
            byte[] payload = new byte[4];
            fPacket.get(payload);
            assertEquals(ByteBuffer.wrap(payload), packet.getPayload());

            // Packet-specific methods Testing
            assertEquals(0x4689, packet.getSourcePort());
            assertEquals(0x9ABB, packet.getDestinationPort());
            assertEquals(0xA1EC, packet.getTotalLength());
            assertEquals(0xFAAF, packet.getChecksum());
        }
    }
}
