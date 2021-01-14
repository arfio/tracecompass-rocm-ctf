/**********************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Alexandre Montplaisir - Port to JUnit4
 *   Markus Schorn - Bug 448058: Use org.eclipse.remote in favor of RSE
 **********************************************************************/

package org.eclipse.tracecompass.lttng2.control.ui.tests.model.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.IChannelInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TargetNodeState;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceEnablement;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceEventType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.BufferType;
import org.eclipse.tracecompass.internal.lttng2.control.stubs.service.TestRemoteSystemProxy;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.BaseEventComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.KernelProviderComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TargetNodeComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceChannelComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceDomainComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceEventComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceProbeEventComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.UstProviderComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.property.BaseEventPropertySource;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.property.KernelProviderPropertySource;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.property.TargetNodePropertySource;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.property.TraceChannelPropertySource;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.property.TraceDomainPropertySource;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.property.TraceEventPropertySource;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.property.TraceProbeEventPropertySource;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.property.TraceSessionPropertySource;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.property.UstProviderPropertySource;
import org.eclipse.tracecompass.tmf.remote.core.proxy.TmfRemoteConnectionFactory;
import org.eclipse.ui.views.properties.IPropertySource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;

/**
 * The class <code>TraceControlPropertiesTest</code> contains tests for the all
 * property class</code>.
 */
public class TraceControlPropertiesTest {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private TraceControlTestFacility fFacility;
    private static final String DIRECTORY   = "testfiles";
    private static final String TEST_STREAM = "ListInfoTest.cfg";
    private static final String SCEN_LIST_INFO_TEST = "ListInfoTest";

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fFacility = TraceControlTestFacility.getInstance();
        fFacility.init();
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        fFacility.dispose();
    }

    /**
     * Run the TraceControlComponent.
     *
     * @throws Exception
     *             This will fail the test
     */
    @Test
    public void testComponentProperties() throws Exception {
        IRemoteConnection host = TmfRemoteConnectionFactory.getLocalConnection();
        TestRemoteSystemProxy proxy = new TestRemoteSystemProxy(host);

        URL location = FileLocator.find(FrameworkUtil.getBundle(this.getClass()), new Path(DIRECTORY + File.separator + TEST_STREAM), null);
        File testfile = new File(FileLocator.toFileURL(location).toURI());
        proxy.setTestFile(testfile.getAbsolutePath());
        proxy.setScenario(SCEN_LIST_INFO_TEST);

        ITraceControlComponent root = fFacility.getControlView().getTraceControlRoot();

        TargetNodeComponent node = new TargetNodeComponent("myNode", root, proxy);

        root.addChild(node);
        node.connect();

        fFacility.waitForConnect(node);
        fFacility.waitForJobs();

        // ------------------------------------------------------------------------
        // Verify Node Properties (adapter)
        // ------------------------------------------------------------------------
        Object adapter = node.getAdapter(IPropertySource.class);
        assertNotNull(adapter);
        assertTrue(adapter instanceof TargetNodePropertySource);

        TargetNodePropertySource source = (TargetNodePropertySource)adapter;

        assertNull(source.getEditableValue());
        assertFalse(source.isPropertySet(TargetNodePropertySource.TARGET_NODE_NAME_PROPERTY_ID));
        assertNotNull(source.getPropertyDescriptors());

        assertEquals("myNode", source.getPropertyValue(TargetNodePropertySource.TARGET_NODE_NAME_PROPERTY_ID));
        // Don't check the address property because the string can vary on the machine the test is running
//        assertEquals("localhost",  source.getPropertyValue(TargetNodePropertySource.TARGET_NODE_ADDRESS_PROPERTY_ID));
        assertEquals(TargetNodeState.CONNECTED.name(), source.getPropertyValue(TargetNodePropertySource.TARGET_NODE_STATE_PROPERTY_ID));
        assertEquals("2.5.0", source.getPropertyValue(TargetNodePropertySource.TARGET_NODE_VERSION_PROPERTY_ID));
        assertNull(source.getPropertyValue("test"));

        adapter = node.getAdapter(IChannelInfo.class);
        assertNull(adapter);

        ITraceControlComponent[] groups = node.getChildren();
        assertNotNull(groups);
        assertEquals(2, groups.length);

        ITraceControlComponent[] providers = groups[0].getChildren();

        assertNotNull(providers);
        assertEquals(3, providers.length);

        // ------------------------------------------------------------------------
        // Verify Kernel Provider Properties (adapter)
        // ------------------------------------------------------------------------
        KernelProviderComponent kernelProvider = (KernelProviderComponent) providers[0];

        adapter = kernelProvider.getAdapter(IPropertySource.class);
        assertNotNull(adapter);
        assertTrue(adapter instanceof KernelProviderPropertySource);

        KernelProviderPropertySource kernelSource = (KernelProviderPropertySource)adapter;
        assertNotNull(kernelSource.getPropertyDescriptors());

        assertEquals("Kernel", kernelSource.getPropertyValue(KernelProviderPropertySource.KERNEL_PROVIDER_NAME_PROPERTY_ID));

        // ------------------------------------------------------------------------
        // Verify UST Provider Properties (adapter)
        // ------------------------------------------------------------------------
        UstProviderComponent ustProvider = (UstProviderComponent) providers[1];

        adapter = ustProvider.getAdapter(IPropertySource.class);
        assertNotNull(adapter);
        assertTrue(adapter instanceof UstProviderPropertySource);

        UstProviderPropertySource ustSource = (UstProviderPropertySource)adapter;
        assertNotNull(ustSource.getPropertyDescriptors());

        assertEquals("/home/user/git/lttng-ust/tests/hello.cxx/.libs/lt-hello [PID=9379]", ustSource.getPropertyValue(UstProviderPropertySource.UST_PROVIDER_NAME_PROPERTY_ID));
        assertEquals(String.valueOf(9379), ustSource.getPropertyValue(UstProviderPropertySource.UST_PROVIDER_PID_PROPERTY_ID));

        // ------------------------------------------------------------------------
        // Verify Base Event Properties (adapter)
        // ------------------------------------------------------------------------
        ITraceControlComponent[] events = ustProvider.getChildren();
        assertNotNull(events);
        assertEquals(2, events.length);

        BaseEventComponent baseEventInfo = (BaseEventComponent) events[0];
        assertNotNull(baseEventInfo);

        adapter = baseEventInfo.getAdapter(IPropertySource.class);
        assertNotNull(adapter);
        assertTrue(adapter instanceof BaseEventPropertySource);

        BaseEventPropertySource baseSource = (BaseEventPropertySource)adapter;
        assertNotNull(baseSource.getPropertyDescriptors());

        assertEquals("ust_tests_hello:tptest_sighandler", baseSource.getPropertyValue(BaseEventPropertySource.BASE_EVENT_NAME_PROPERTY_ID));
        assertEquals(TraceEventType.TRACEPOINT.name(), baseSource.getPropertyValue(BaseEventPropertySource.BASE_EVENT_TYPE_PROPERTY_ID));
        assertEquals(TraceLogLevel.TRACE_DEBUG_MODULE.name(), baseSource.getPropertyValue(BaseEventPropertySource.BASE_EVENT_LOGLEVEL_PROPERTY_ID));

        baseEventInfo = (BaseEventComponent) events[1];
        assertNotNull(baseEventInfo);

        adapter = baseEventInfo.getAdapter(IPropertySource.class);
        assertNotNull(adapter);
        assertTrue(adapter instanceof BaseEventPropertySource);
        baseSource = (BaseEventPropertySource)adapter;
        assertNotNull(baseSource.getPropertyDescriptors());
        assertEquals("doublefield=float;floatfield=float;stringfield=string", baseSource.getPropertyValue(BaseEventPropertySource.BASE_EVENT_FIELDS_PROPERTY_ID));

        // ------------------------------------------------------------------------
        // Verify Session Properties (adapter)
        // ------------------------------------------------------------------------
        ITraceControlComponent[] sessions = groups[1].getChildren();
        assertNotNull(sessions);
        assertEquals(2, sessions.length);

        TraceSessionComponent session = (TraceSessionComponent)sessions[1];

        adapter = session.getAdapter(IPropertySource.class);
        assertNotNull(adapter);
        assertTrue(adapter instanceof TraceSessionPropertySource);

        TraceSessionPropertySource sessionSource = (TraceSessionPropertySource)adapter;
        assertNotNull(sessionSource.getPropertyDescriptors());

        assertEquals("mysession", sessionSource.getPropertyValue(TraceSessionPropertySource.TRACE_SESSION_NAME_PROPERTY_ID));
        assertEquals("/home/user/lttng-traces/mysession-20120129-084256", sessionSource.getPropertyValue(TraceSessionPropertySource.TRACE_SESSION_PATH_PROPERTY_ID));
        assertEquals(TraceSessionState.ACTIVE.name(), sessionSource.getPropertyValue(TraceSessionPropertySource.TRACE_SESSION_STATE_PROPERTY_ID));

        // ------------------------------------------------------------------------
        // Verify Domain Provider Properties (adapter)
        // ------------------------------------------------------------------------
        ITraceControlComponent[] domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(2, domains.length);

        TraceDomainComponent domain = (TraceDomainComponent) domains[0];
        adapter = domain.getAdapter(IPropertySource.class);
        assertNotNull(adapter);
        assertTrue(adapter instanceof TraceDomainPropertySource);

        TraceDomainPropertySource domainSource = (TraceDomainPropertySource)adapter;
        assertNotNull(domainSource.getPropertyDescriptors());

        assertEquals("Kernel", domainSource.getPropertyValue(TraceDomainPropertySource.TRACE_DOMAIN_NAME_PROPERTY_ID));
        assertEquals(BufferType.BUFFER_SHARED.getInName(), domainSource.getPropertyValue(TraceDomainPropertySource.BUFFER_TYPE_PROPERTY_ID));

        ITraceControlComponent[] channels =  domains[0].getChildren();
        assertNotNull(channels);
        assertEquals(2, channels.length);

        // ------------------------------------------------------------------------
        // Verify Channel Properties (adapter)
        // ------------------------------------------------------------------------
        assertTrue(channels[0] instanceof TraceChannelComponent);
        TraceChannelComponent channel = (TraceChannelComponent) channels[0];

        adapter = channel.getAdapter(IPropertySource.class);
        assertNotNull(adapter);
        assertTrue(adapter instanceof TraceChannelPropertySource);

        TraceChannelPropertySource channelSource = (TraceChannelPropertySource)adapter;
        assertNotNull(channelSource.getPropertyDescriptors());

        assertEquals("channel0", channelSource.getPropertyValue(TraceChannelPropertySource.TRACE_CHANNEL_NAME_PROPERTY_ID));
        assertEquals(String.valueOf(4), channelSource.getPropertyValue(TraceChannelPropertySource.TRACE_CHANNEL_NO_SUBBUFFERS_PROPERTY_ID));
        assertEquals(TraceEnablement.ENABLED.name(), channelSource.getPropertyValue(TraceChannelPropertySource.TRACE_CHANNEL_STATE_PROPERTY_ID));
        assertEquals(String.valueOf(false), channelSource.getPropertyValue(TraceChannelPropertySource.TRACE_CHANNEL_OVERWRITE_MODE_PROPERTY_ID));
        assertEquals("splice()", channelSource.getPropertyValue(TraceChannelPropertySource.TRACE_CHANNEL_OUTPUT_TYPE_PROPERTY_ID));
        assertEquals(String.valueOf(200), channelSource.getPropertyValue(TraceChannelPropertySource.TRACE_CHANNEL_READ_TIMER_PROPERTY_ID));
        assertEquals(String.valueOf(262144), channelSource.getPropertyValue(TraceChannelPropertySource.TRACE_CHANNEL_SUBBUFFER_SIZE_PROPERTY_ID));
        assertEquals(String.valueOf(0), channelSource.getPropertyValue(TraceChannelPropertySource.TRACE_CHANNEL_SWITCH_TIMER_PROPERTY_ID));
        assertEquals(Integer.valueOf(2), channelSource.getPropertyValue(TraceChannelPropertySource.TRACE_CHANNEL_TRACE_FILE_COUNT_PROPERTY_ID));
        assertEquals(Long.valueOf(262144), channelSource.getPropertyValue(TraceChannelPropertySource.TRACE_CHANNEL_TRACE_FILE_SIZE_PROPERTY_ID));

        // ------------------------------------------------------------------------
        // Verify Event Properties (adapter)
        // ------------------------------------------------------------------------
        ITraceControlComponent[] channel0Events = channel.getChildren();
        assertNotNull(channel0Events);
        assertEquals(5, channel0Events.length);
        assertTrue(channel0Events[0] instanceof TraceEventComponent);

        TraceEventComponent event = (TraceEventComponent) channel0Events[0];

        adapter = event.getAdapter(IPropertySource.class);
        assertNotNull(adapter);
        assertTrue(adapter instanceof TraceEventPropertySource);

        TraceEventPropertySource eventSource = (TraceEventPropertySource)adapter;
        assertNotNull(eventSource.getPropertyDescriptors());

        assertEquals("block_rq_remap", eventSource.getPropertyValue(TraceEventPropertySource.TRACE_EVENT_NAME_PROPERTY_ID));
        assertEquals(TraceLogLevel.TRACE_EMERG.name(), eventSource.getPropertyValue(TraceEventPropertySource.TRACE_EVENT_LOGLEVEL_PROPERTY_ID));
        assertEquals(TraceEventType.TRACEPOINT.name(), eventSource.getPropertyValue(TraceEventPropertySource.TRACE_EVENT_TYPE_PROPERTY_ID));
        assertEquals(TraceEnablement.ENABLED.name(), eventSource.getPropertyValue(TraceEventPropertySource.TRACE_EVENT_STATE_PROPERTY_ID));

        // ------------------------------------------------------------------------
        // Verify Probe Event Properties (adapter)
        // ------------------------------------------------------------------------
        assertTrue(channel0Events[2] instanceof TraceProbeEventComponent);

        TraceProbeEventComponent probeEvent = (TraceProbeEventComponent) channel0Events[2];

        adapter = probeEvent.getAdapter(IPropertySource.class);
        assertNotNull(adapter);
        assertTrue(adapter instanceof TraceProbeEventPropertySource);

        TraceProbeEventPropertySource probeEventSource = (TraceProbeEventPropertySource)adapter;
        assertNotNull(probeEventSource.getPropertyDescriptors());
        assertEquals(4, probeEventSource.getPropertyDescriptors().length);

        assertEquals("myevent2", probeEventSource.getPropertyValue(TraceEventPropertySource.TRACE_EVENT_NAME_PROPERTY_ID));
        assertEquals(TraceEventType.PROBE.name(), probeEventSource.getPropertyValue(TraceEventPropertySource.TRACE_EVENT_TYPE_PROPERTY_ID));
        assertEquals(TraceEnablement.ENABLED.name(), probeEventSource.getPropertyValue(TraceEventPropertySource.TRACE_EVENT_STATE_PROPERTY_ID));
        assertEquals("0xc0101340", probeEventSource.getPropertyValue(TraceProbeEventPropertySource.TRACE_EVENT_PROBE_ADDRESS_PROPERTY_ID));

        assertTrue(channel0Events[3] instanceof TraceProbeEventComponent);

        probeEvent = (TraceProbeEventComponent) channel0Events[3];

        adapter = probeEvent.getAdapter(IPropertySource.class);
        assertNotNull(adapter);
        assertTrue(adapter instanceof TraceProbeEventPropertySource);

        probeEventSource = (TraceProbeEventPropertySource)adapter;
        assertNotNull(probeEventSource.getPropertyDescriptors());
        assertEquals(5, probeEventSource.getPropertyDescriptors().length);

        assertEquals("myevent0", probeEventSource.getPropertyValue(TraceEventPropertySource.TRACE_EVENT_NAME_PROPERTY_ID));
        assertEquals(TraceEventType.PROBE.name(), probeEventSource.getPropertyValue(TraceEventPropertySource.TRACE_EVENT_TYPE_PROPERTY_ID));
        assertEquals(TraceEnablement.ENABLED.name(), probeEventSource.getPropertyValue(TraceEventPropertySource.TRACE_EVENT_STATE_PROPERTY_ID));
        assertEquals("0x0", probeEventSource.getPropertyValue(TraceProbeEventPropertySource.TRACE_EVENT_PROBE_OFFSET_PROPERTY_ID));
        assertEquals("init_post", probeEventSource.getPropertyValue(TraceProbeEventPropertySource.TRACE_EVENT_PROBE_SYMBOL_PROPERTY_ID));

        //-------------------------------------------------------------------------
        // Verify Filter of UST event
        //-------------------------------------------------------------------------
        event = (TraceEventComponent) domains[1].getChildren()[1].getChildren()[0];
        adapter = event.getAdapter(IPropertySource.class);
        assertEquals("with filter", event.getFilterExpression());

        //-------------------------------------------------------------------------
        // Verify Log Level Type of UST events (> LTTng 2.4)
        //-------------------------------------------------------------------------
        event = (TraceEventComponent) domains[1].getChildren()[1].getChildren()[0];
        adapter = event.getAdapter(IPropertySource.class);
        assertNotNull(adapter);
        eventSource = (TraceEventPropertySource) adapter;
        assertEquals("== TRACE_DEBUG_LINE", eventSource.getPropertyValue(TraceEventPropertySource.TRACE_EVENT_LOGLEVEL_PROPERTY_ID));

        event = (TraceEventComponent) domains[1].getChildren()[1].getChildren()[1];
        adapter = event.getAdapter(IPropertySource.class);
        assertNotNull(adapter);
        eventSource = (TraceEventPropertySource) adapter;
        assertEquals("<= TRACE_INFO", eventSource.getPropertyValue(TraceEventPropertySource.TRACE_EVENT_LOGLEVEL_PROPERTY_ID));

        event = (TraceEventComponent) domains[1].getChildren()[1].getChildren()[2];
        adapter = event.getAdapter(IPropertySource.class);
        assertNotNull(adapter);
        eventSource = (TraceEventPropertySource) adapter;
        assertEquals("TRACE_DEBUG_SYSTEM", eventSource.getPropertyValue(TraceEventPropertySource.TRACE_EVENT_LOGLEVEL_PROPERTY_ID));

        //-------------------------------------------------------------------------
        // Delete node
        //-------------------------------------------------------------------------
        node.disconnect();
        node.getParent().removeChild(node);
    }
}
