/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.callstack.core.tests.lttng2.ust.callstack;

import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;

/**
 * Test suite for the UST callstack state provider, using the trace of a program
 * instrumented with lttng-ust-cyg-profile.so tracepoints.
 *
 * @author Alexandre Montplaisir
 */
public class LttngUstCallStackProviderTest extends AbstractProviderTest {

    private static final long[] timestamps = { 1378850463600000000L,
                                               1378850463770000000L,
                                               1378850463868753000L };

    @Override
    protected CtfTestTrace getTestTrace() {
        return CtfTestTrace.CYG_PROFILE;
    }

    @Override
    protected int getProcessId() {
        /* This particular trace does not have PID contexts. */
        return -1;
    }

    @Override
    protected String getThreadName() {
        return "glxgears-16073";
    }

    @Override
    protected long getTestTimestamp(int index) {
        return timestamps[index];
    }

}
