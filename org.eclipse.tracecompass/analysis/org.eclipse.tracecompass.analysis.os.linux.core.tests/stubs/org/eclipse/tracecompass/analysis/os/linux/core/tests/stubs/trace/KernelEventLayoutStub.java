/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.trace;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.tracecompass.analysis.os.linux.core.trace.DefaultEventLayout;

import com.google.common.collect.ImmutableList;

/**
 * Class to extend to be able to set the event names for the os unit tests.
 *
 * @author Geneviève Bastien
 */
public class KernelEventLayoutStub extends DefaultEventLayout {

    /**
     * Protected constructor
     */
    protected KernelEventLayoutStub() {
        super();
    }

    private static final KernelEventLayoutStub INSTANCE = new KernelEventLayoutStub();

    /**
     * Get an instance of this event layout
     *
     * This object is completely immutable, so no need to create additional
     * instances via the constructor.
     *
     * @return The instance
     */
    public static synchronized KernelEventLayoutStub getInstance() {
        return INSTANCE;
    }

    @Override
    public Collection<String> eventsNetworkSend() {
        return ImmutableList.of("packet_sent");
    }

    @Override
    public Collection<String> eventsNetworkReceive() {
        return ImmutableList.of("packet_received");
    }

    @Override
    public String eventIrqEntry() {
        return "do_IRQ_entry";
    }

    @Override
    public String eventIrqExit() {
        return "do_IRQ_return";
    }

    @Override
    public Collection<String> eventsNetworkReceiveEntry() {
        return Collections.singleton("netif_receive_skb_internal_entry");
    }

    @Override
    public Collection<String> eventsNetworkReceiveExit() {
        return Collections.singleton("netif_receive_skb_internal_return");
    }



}
