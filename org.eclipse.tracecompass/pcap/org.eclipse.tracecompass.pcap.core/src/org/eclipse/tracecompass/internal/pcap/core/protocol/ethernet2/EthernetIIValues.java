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

package org.eclipse.tracecompass.internal.pcap.core.protocol.ethernet2;

/**
 * Interface that lists constants related to Ethernet II.
 *
 * See http://en.wikipedia.org/wiki/Ethernet_frame#Ethernet_II.
 *
 * @author Vincent Perot
 */
public interface EthernetIIValues {

    /** Size in bytes of a MAC address */
    int MAC_ADDRESS_SIZE = 6;

    /** Size in bytes of the ethertype field */
    int ETHERTYPE_SIZE = 4;

    /** Size in bytes of the CRC checksum */
    int CRC_CHECKSUM_SIZE = 4;

    /** Maximum size in bytes of a entire Ethernet II Frame */
    int ETHERNET_II_MAX_SIZE = 1518;

    /** Minimum size in bytes of a entire Ethernet II Frame */
    int ETHERNET_II_MIN_SIZE = 14;

}
