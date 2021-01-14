/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core;

/**
 * This file defines all the known event and field names used to trace socket
 * connection for both the addons module method (
 * {@link org.eclipse.tracecompass.internal.lttng2.kernel.core.event.matching.TcpEventMatching}
 * ) and the net_data_experimental branch (
 * {@link org.eclipse.tracecompass.internal.lttng2.kernel.core.event.matching.TcpLttngEventMatching}
 * ).
 *
 * These events should be eventually mainlined and when this happens, this class
 * won't be necessary anymore and they should be moved to {@link LttngStrings}
 * class
 *
 *
 * @author Geneviève Bastien
 */
@SuppressWarnings({ "javadoc", "nls" })
public interface TcpEventStrings {

    /* Event names */
    public static final String INET_CONNECT = "inet_connect";
    public static final String INET_SOCK_CREATE = "inet_sock_create";
    public static final String INET_SOCK_LOCAL_OUT = "inet_sock_local_out";
    public static final String INET_SOCK_LOCAL_IN = "inet_sock_local_in";
    public static final String INET_SOCK_CLONE = "inet_sock_clone";
    public static final String INET_ACCEPT = "inet_accept";
    public static final String INET_SOCK_DELETE = "inet_sock_delete";

    /* Field names */
    public static final String SEQ = "seq";
    public static final String SK = "sk";
    public static final String OSK = "osk";
    public static final String NSK = "nsk";
    public static final String SPORT = "sport";
    public static final String DPORT = "dport";
    public static final String SADDR = "saddr";
    public static final String DADDR = "daddr";
    public static final String ACKSEQ = "ack_seq";
    public static final String CHECK = "check";
    public static final String WINDOW = "window";
    public static final String FLAGS = "flags";

}
