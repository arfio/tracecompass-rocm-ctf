/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Alexandre Montplaisir - Port to JUnit4
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.tracecompass.internal.tmf.core.component.TmfProviderManager;
import org.eclipse.tracecompass.tmf.core.component.TmfEventProvider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.junit.Test;

/**
 * Test suite for the TmfProviderManager class.
 */
public class TmfProviderManagerTest {

    // ------------------------------------------------------------------------
    // Dummy Providers
    // ------------------------------------------------------------------------

    private class TestProvider1 extends TmfEventProvider {
        public TestProvider1(Class<ITmfEvent> type) {
            super("TestProvider1", type);
        }

        @Override
        public ITmfContext armRequest(ITmfEventRequest request) {
            return null;
        }

        @Override
        public ITmfEvent getNext(ITmfContext context) {
            return null;
        }

        @Override
        public boolean isCompleted(ITmfEventRequest request, ITmfEvent data, int nbRead) {
            return false;
        }
    }

    private class TestProvider2 extends TmfEventProvider {
        public TestProvider2(Class<ITmfEvent> type) {
            super("TestProvider2", type);
        }

        @Override
        public ITmfContext armRequest(ITmfEventRequest request) {
            return null;
        }

        @Override
        public ITmfEvent getNext(ITmfContext context) {
            return null;
        }

        @Override
        public boolean isCompleted(ITmfEventRequest request, ITmfEvent data, int nbRead) {
            return false;
        }
    }

    private class TmfEvent3 extends TmfEvent {
        private TmfEvent3() {
            super(null, ITmfContext.UNKNOWN_RANK, null, null, null);
        }
    }

    private class TestProvider3 extends TmfEventProvider {
        public TestProvider3(Class<TmfEvent3> type) {
            super("TestProvider3", type);
        }

        @Override
        public ITmfContext armRequest(ITmfEventRequest request) {
            return null;
        }

        @Override
        public TmfEvent3 getNext(ITmfContext context) {
            return null;
        }

        @Override
        public boolean isCompleted(ITmfEventRequest request, ITmfEvent data, int nbRead) {
            return false;
        }
    }

    // ------------------------------------------------------------------------
    // register/dispose
    // ------------------------------------------------------------------------

    /**
     * Test registering
     */
    @Test
    public void testRegister_0() {
        TmfEventProvider[] providers = TmfProviderManager.getProviders(ITmfEvent.class);
        assertEquals("getProviders", 0, providers.length);

        providers = TmfProviderManager.getProviders(ITmfEvent.class, TestProvider1.class);
        assertEquals("getProviders", 0, providers.length);
    }

    /**
     * Test unregistering
     */
    @Test
    public void testRegister_Unregister_1() {

        // Register a single provider
        TestProvider1 testProvider1 = new TestProvider1(ITmfEvent.class);

        TmfEventProvider[] providers = TmfProviderManager.getProviders(ITmfEvent.class);
        assertEquals("getProviders", 1, providers.length);
        assertEquals("getProviders", testProvider1, providers[0]);

        providers = TmfProviderManager.getProviders(ITmfEvent.class, TestProvider1.class);
        assertEquals("getProviders", 1, providers.length);
        assertEquals("getProviders", testProvider1, providers[0]);

        // Unregister it
        testProvider1.dispose();

        providers = TmfProviderManager.getProviders(ITmfEvent.class);
        assertEquals("getProviders", 0, providers.length);

        providers = TmfProviderManager.getProviders(ITmfEvent.class, TestProvider1.class);
        assertEquals("getProviders", 0, providers.length);
    }

    /**
     * Test unregistering
     */
    @Test
    public void testRegister_Unregister_2() {

        // Register 2 providers, same data type
        TestProvider1 testProvider1 = new TestProvider1(ITmfEvent.class);
        TestProvider2 testProvider2 = new TestProvider2(ITmfEvent.class);

        TmfEventProvider[] providers = TmfProviderManager.getProviders(ITmfEvent.class);
        assertEquals("getProviders", 2, providers.length);
        assertTrue(providers.length == 2);
        if (providers[0] == testProvider1) {
            assertEquals("getProviders", testProvider2, providers[1]);
        }
        else {
            assertEquals("getProviders", testProvider2, providers[0]);
            assertEquals("getProviders", testProvider1, providers[1]);
        }

        providers = TmfProviderManager.getProviders(ITmfEvent.class, TestProvider1.class);
        assertEquals("getProviders", 1, providers.length);
        assertEquals("getProviders", testProvider1, providers[0]);

        providers = TmfProviderManager.getProviders(ITmfEvent.class, TestProvider2.class);
        assertEquals("getProviders", 1, providers.length);
        assertEquals("getProviders", testProvider2, providers[0]);

        // Remove one
        testProvider1.dispose();

        providers = TmfProviderManager.getProviders(ITmfEvent.class);
        assertEquals("getProviders", 1, providers.length);
        assertEquals("getProviders", testProvider2, providers[0]);

        providers = TmfProviderManager.getProviders(ITmfEvent.class, TestProvider1.class);
        assertEquals("getProviders", 0, providers.length);

        providers = TmfProviderManager.getProviders(ITmfEvent.class, TestProvider2.class);
        assertEquals("getProviders", 1, providers.length);
        assertEquals("getProviders", testProvider2, providers[0]);

        // Remove the other
        testProvider2.dispose();

        providers = TmfProviderManager.getProviders(ITmfEvent.class);
        assertEquals("getProviders", 0, providers.length);

        providers = TmfProviderManager.getProviders(ITmfEvent.class, TestProvider1.class);
        assertEquals("getProviders", 0, providers.length);

        providers = TmfProviderManager.getProviders(ITmfEvent.class, TestProvider2.class);
        assertEquals("getProviders", 0, providers.length);
    }

    /**
     * Test unregistering
     */
    @Test
    public void testRegister_Unregister_3() {

        // Register 3 providers, mixed data types
        TestProvider1 testProvider1 = new TestProvider1(ITmfEvent.class);
        TestProvider2 testProvider2 = new TestProvider2(ITmfEvent.class);
        TestProvider3 testProvider3 = new TestProvider3(TmfEvent3.class);

        TmfEventProvider[] providers = TmfProviderManager.getProviders(ITmfEvent.class);
        assertEquals("getProviders", 2, providers.length);
        if (providers[0] == testProvider1) {
            assertEquals("getProviders", testProvider2, providers[1]);
        }
        else {
            assertEquals("getProviders", testProvider2, providers[0]);
            assertEquals("getProviders", testProvider1, providers[1]);
        }

        TmfEventProvider[] providers3 = TmfProviderManager.getProviders(TmfEvent3.class);
        assertEquals("getProviders", 1, providers3.length);
        assertEquals("getProviders", testProvider3, providers3[0]);

        providers = TmfProviderManager.getProviders(ITmfEvent.class, TestProvider1.class);
        assertEquals("getProviders", 1, providers.length);
        assertEquals("getProviders", testProvider1, providers[0]);

        providers = TmfProviderManager.getProviders(ITmfEvent.class, TestProvider2.class);
        assertEquals("getProviders", 1, providers.length);
        assertEquals("getProviders", testProvider2, providers[0]);

        providers3 = TmfProviderManager.getProviders(TmfEvent3.class, TestProvider3.class);
        assertEquals("getProviders", 1, providers3.length);
        assertEquals("getProviders", testProvider3, providers3[0]);

        // Remove one
        testProvider1.dispose();

        providers = TmfProviderManager.getProviders(ITmfEvent.class);
        assertEquals("getProviders", 1, providers.length);
        assertEquals("getProviders", testProvider2, providers[0]);

        providers = TmfProviderManager.getProviders(ITmfEvent.class, TestProvider1.class);
        assertEquals("getProviders", 0, providers.length);

        providers = TmfProviderManager.getProviders(ITmfEvent.class, TestProvider2.class);
        assertEquals("getProviders", 1, providers.length);
        assertEquals("getProviders", testProvider2, providers[0]);

        providers3 = TmfProviderManager.getProviders(TmfEvent3.class);
        assertEquals("getProviders", 1, providers3.length);
        assertEquals("getProviders", testProvider3, providers3[0]);

        providers3 = TmfProviderManager.getProviders(TmfEvent3.class, TestProvider3.class);
        assertEquals("getProviders", 1, providers3.length);
        assertEquals("getProviders", testProvider3, providers3[0]);

        // Remove another one
        testProvider2.dispose();

        providers = TmfProviderManager.getProviders(ITmfEvent.class);
        assertEquals("getProviders", 0, providers.length);

        providers = TmfProviderManager.getProviders(ITmfEvent.class, TestProvider1.class);
        assertEquals("getProviders", 0, providers.length);

        providers = TmfProviderManager.getProviders(ITmfEvent.class, TestProvider2.class);
        assertEquals("getProviders", 0, providers.length);

        providers3 = TmfProviderManager.getProviders(TmfEvent3.class);
        assertTrue(providers3.length == 1);
        assertTrue(providers3[0] == testProvider3);

        providers3 = TmfProviderManager.getProviders(TmfEvent3.class, TestProvider3.class);
        assertEquals("getProviders", 1, providers3.length);
        assertEquals("getProviders", testProvider3, providers3[0]);

        // Remove the last one
        testProvider3.dispose();

        providers = TmfProviderManager.getProviders(ITmfEvent.class);
        assertEquals("getProviders", 0, providers.length);

        providers = TmfProviderManager.getProviders(ITmfEvent.class, TestProvider1.class);
        assertEquals("getProviders", 0, providers.length);

        providers = TmfProviderManager.getProviders(ITmfEvent.class, TestProvider2.class);
        assertEquals("getProviders", 0, providers.length);

        providers3 = TmfProviderManager.getProviders(TmfEvent3.class);
        assertEquals("getProviders", 0, providers.length);

        providers3 = TmfProviderManager.getProviders(TmfEvent3.class, TestProvider3.class);
        assertEquals("getProviders", 0, providers.length);
    }

    /**
     * Test getProviders method
     */
    @Test
    public void testGetProvider() {

        // Register 3 providers, mixed data types
        TestProvider1 testProvider1 = new TestProvider1(ITmfEvent.class);
        TestProvider2 testProvider2 = new TestProvider2(ITmfEvent.class);
        TestProvider3 testProvider3 = new TestProvider3(TmfEvent3.class);

        TmfEventProvider[] providers = TmfProviderManager.getProviders(ITmfEvent.class, null);
        assertEquals("getProviders", 2, providers.length);
        if (providers[0] == testProvider1) {
            assertEquals("getProviders", testProvider2, providers[1]);
        }
        else {
            assertEquals("getProviders", testProvider2, providers[0]);
            assertEquals("getProviders", testProvider1, providers[1]);
        }

        providers = TmfProviderManager.getProviders(TmfEvent3.class, null);
        assertEquals("getProviders", 1, providers.length);
        assertEquals("getProviders", testProvider3, providers[0]);

        // Remove the providers
        testProvider1.dispose();
        testProvider2.dispose();
        testProvider3.dispose();
    }

}
