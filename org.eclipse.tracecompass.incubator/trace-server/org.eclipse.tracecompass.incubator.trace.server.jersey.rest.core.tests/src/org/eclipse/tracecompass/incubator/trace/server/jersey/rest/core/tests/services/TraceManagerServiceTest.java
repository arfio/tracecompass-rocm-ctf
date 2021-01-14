/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.TraceManagerService;
import org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.stubs.TraceModelStub;
import org.eclipse.tracecompass.incubator.trace.server.jersey.rest.core.tests.utils.RestServerTest;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

/**
 * Test the {@link TraceManagerService}
 *
 * @author Loic Prieur-Drevon
 */
public class TraceManagerServiceTest extends RestServerTest {

    /**
     * Test basic operations on the {@link TraceManagerService}.
     */
    @Test
    public void testWithOneTrace() {
        WebTarget traces = getApplicationEndpoint().path(TRACES);

        assertTrue("Expected empty set of traces", getTraces(traces).isEmpty());

        assertPost(traces, CONTEXT_SWITCHES_KERNEL_STUB);

        assertEquals(CONTEXT_SWITCHES_KERNEL_STUB, traces.path(CONTEXT_SWITCHES_KERNEL_UUID.toString()).request().get(TraceModelStub.class));

        assertEquals("Expected set of traces to contain trace2 stub",
                Collections.singleton(CONTEXT_SWITCHES_KERNEL_STUB), getTraces(traces));

        Response deleteResponse = traces.path(CONTEXT_SWITCHES_KERNEL_UUID.toString()).request().delete();
        int deleteCode = deleteResponse.getStatus();
        assertEquals("Failed to DELETE trace2, error code=" + deleteCode, 200, deleteCode);
        assertEquals(CONTEXT_SWITCHES_KERNEL_STUB, deleteResponse.readEntity(TraceModelStub.class));

        assertEquals("Trace should have been deleted", Collections.emptySet(), getTraces(traces));
    }

    /**
     * Test the server with two traces, to eliminate the server trace manager bug
     */
    @Test
    public void testWithTwoTraces() {
        WebTarget traces = getApplicationEndpoint().path(TRACES);

        assertPost(traces, CONTEXT_SWITCHES_KERNEL_STUB);
        assertPost(traces, CONTEXT_SWITCHES_UST_STUB);

        assertEquals(ImmutableSet.of(CONTEXT_SWITCHES_KERNEL_STUB, CONTEXT_SWITCHES_UST_STUB), getTraces(traces));
    }

    /**
     * Test conflicting traces
     * @throws IOException Exception thrown by getting trace path
     */
    @Test
    public void testConflictingTraces() throws IOException {
        WebTarget traces = getApplicationEndpoint().path(TRACES);

        assertPost(traces, CONTEXT_SWITCHES_KERNEL_STUB);

        // Post the trace a second time
        assertPost(traces, CONTEXT_SWITCHES_KERNEL_STUB);
        assertEquals(ImmutableSet.of(CONTEXT_SWITCHES_KERNEL_STUB), getTraces(traces));

        // Post a trace with the same name but another path, the name does not
        // matter if the path is different, the trace will be added
        assertPost(traces, ARM_64_KERNEL_STUB);
        assertEquals(ImmutableSet.of(CONTEXT_SWITCHES_KERNEL_STUB, ARM_64_KERNEL_STUB), getTraces(traces));
    }
}
