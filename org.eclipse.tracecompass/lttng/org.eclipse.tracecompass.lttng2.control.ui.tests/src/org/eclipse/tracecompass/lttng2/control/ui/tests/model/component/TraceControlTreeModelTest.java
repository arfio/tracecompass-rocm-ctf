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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.LogLevelType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TargetNodeState;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceChannelOutputType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceEnablement;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceEventType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.tracecompass.internal.lttng2.control.stubs.service.TestRemoteSystemProxy;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.BaseEventComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.KernelProviderComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TargetNodeComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceChannelComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceDomainComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceEventComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceProbeEventComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceProviderGroup;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionGroup;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.UstProviderComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.service.ILttngControlService;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.service.LTTngControlService;
import org.eclipse.tracecompass.tmf.remote.core.proxy.TmfRemoteConnectionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;

/**
 * The class <code>TraceControlTreeModelTest</code> contains tests for the tree
 * component classes.
 */
public class TraceControlTreeModelTest {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private TraceControlTestFacility fFacility;

    private static final String TEST_STREAM = "ListInfoTest.cfg";
    private static final String SCEN_LIST_INFO_TEST = "ListInfoTest";
    private static final String TARGET_NODE_NAME = "myNode";

    // ------------------------------------------------------------------------
    // Test data
    // ------------------------------------------------------------------------

    private IRemoteConnection fHost = TmfRemoteConnectionFactory.getLocalConnection();
    private @NonNull TestRemoteSystemProxy fProxy = new TestRemoteSystemProxy(fHost);
    private String fTestFile;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * Perform pre-test initialization.
     *
     * @throws Exception
     *         if the initialization fails for some reason
     */
    @Before
    public void setUp() throws Exception {
        fFacility = TraceControlTestFacility.getInstance();
        fFacility.init();
        URL location = FileLocator.find(FrameworkUtil.getBundle(this.getClass()), new Path(TraceControlTestFacility.DIRECTORY + File.separator + TEST_STREAM), null);
        File testfile = new File(FileLocator.toFileURL(location).toURI());
        fTestFile = testfile.getAbsolutePath();
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
     */
    @Test
    public void testTraceControlComponents() {

        fProxy.setTestFile(fTestFile);
        fProxy.setScenario(SCEN_LIST_INFO_TEST);

        ITraceControlComponent root = fFacility.getControlView().getTraceControlRoot();

        TargetNodeComponent node = new TargetNodeComponent(TARGET_NODE_NAME, root, fProxy);

        root.addChild(node);
        node.connect();

        fFacility.waitForConnect(node);
        fFacility.waitForJobs();

        // ------------------------------------------------------------------------
        // Verify Parameters of TargetNodeComponent
        // ------------------------------------------------------------------------
        assertEquals("Local", node.getToolTip()); // assigned in createLocalHost() above
        Image connectedImage = node.getImage();
        assertNotNull(connectedImage);
        assertEquals(TargetNodeState.CONNECTED, node.getTargetNodeState());
        assertNotNull(node.getControlService());
        ILttngControlService service = node.getControlService();
        assertTrue(service instanceof LTTngControlService);
        node.setControlService(service);
        assertTrue(node.getControlService() instanceof LTTngControlService);


        // ------------------------------------------------------------------------
        // Verify Children of TargetNodeComponent
        // ------------------------------------------------------------------------
        ITraceControlComponent[] groups = node.getChildren();
        assertNotNull(groups);
        assertEquals(2, groups.length);

        assertTrue(groups[0] instanceof TraceProviderGroup);
        assertTrue(groups[1] instanceof TraceSessionGroup);

        assertEquals("Provider", groups[0].getName());
        assertEquals("Sessions", groups[1].getName());

        // ------------------------------------------------------------------------
        // Verify TraceProviderGroup
        // ------------------------------------------------------------------------
        ITraceControlComponent[] providers = groups[0].getChildren();

        assertNotNull(providers);
        assertEquals(3, providers.length);

        // ------------------------------------------------------------------------
        // Verify KernelProviderComponent
        // ------------------------------------------------------------------------
        KernelProviderComponent kernelProvider = (KernelProviderComponent) providers[0];

        // ------------------------------------------------------------------------
        // Verify event info (kernel provider)
        // ------------------------------------------------------------------------
        ITraceControlComponent[] events = kernelProvider.getChildren();
        assertNotNull(events);
        assertEquals(3, events.length);

        BaseEventComponent baseEventInfo = (BaseEventComponent) events[0];
        assertNotNull(baseEventInfo);
        assertEquals("sched_kthread_stop", baseEventInfo.getName());
        assertEquals(TraceLogLevel.TRACE_EMERG, baseEventInfo.getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());

        baseEventInfo = (BaseEventComponent) events[1];
        assertEquals("sched_kthread_stop_ret", baseEventInfo.getName());
        assertEquals(TraceLogLevel.TRACE_EMERG, baseEventInfo.getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());

        baseEventInfo = (BaseEventComponent) events[2];
        assertEquals("sched_wakeup_new", baseEventInfo.getName());
        assertEquals(TraceLogLevel.TRACE_EMERG, baseEventInfo.getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());

        // ------------------------------------------------------------------------
        // Verify UstProviderComponent
        // ------------------------------------------------------------------------
        UstProviderComponent ustProvider = (UstProviderComponent) providers[1];
        assertEquals("/home/user/git/lttng-ust/tests/hello.cxx/.libs/lt-hello [PID=9379]", ustProvider.getName());
        assertEquals(9379, ustProvider.getPid());

        // ------------------------------------------------------------------------
        // Verify event info (UST provider)
        // ------------------------------------------------------------------------
        events = ustProvider.getChildren();
        assertNotNull(events);
        assertEquals(2, events.length);

        baseEventInfo = (BaseEventComponent) events[0];
        assertNotNull(baseEventInfo);
        assertEquals("ust_tests_hello:tptest_sighandler", baseEventInfo.getName());
        assertEquals(TraceLogLevel.TRACE_DEBUG_MODULE, baseEventInfo.getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());

        baseEventInfo = (BaseEventComponent) events[1];
        assertEquals("ust_tests_hello:tptest", baseEventInfo.getName());
        assertEquals(TraceLogLevel.TRACE_INFO, baseEventInfo.getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());

        // ------------------------------------------------------------------------
        // Verify UstProviderComponent
        // ------------------------------------------------------------------------
        ustProvider = (UstProviderComponent) providers[2];
        assertEquals("/home/user/git/lttng-ust/tests/hello.cxx/.libs/lt-hello [PID=4852]", ustProvider.getName());
        assertEquals(4852, ustProvider.getPid());

        // verify getters and setter
        verifyUstProviderGettersSetters(ustProvider);

        // ------------------------------------------------------------------------
        // Verify event info (UST provider)
        // ------------------------------------------------------------------------
        events = ustProvider.getChildren();
        assertNotNull(events);
        assertEquals(2, events.length);

        baseEventInfo = (BaseEventComponent) events[0];
        assertNotNull(baseEventInfo);
        assertEquals("ust_tests_hello:tptest_sighandler", baseEventInfo.getName());
        assertEquals(TraceLogLevel.TRACE_WARNING, baseEventInfo.getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());

        baseEventInfo = (BaseEventComponent) events[1];
        assertEquals("ust_tests_hello:tptest", baseEventInfo.getName());
        assertEquals(TraceLogLevel.TRACE_DEBUG_FUNCTION, baseEventInfo.getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, baseEventInfo.getEventType());

        // verify getters and setters
        verifyBaseEventGettersSetters(baseEventInfo);

        // ------------------------------------------------------------------------
        // Verify TraceSessionGroup
        // ------------------------------------------------------------------------
        ITraceControlComponent[] sessions = groups[1].getChildren();
        assertNotNull(sessions);
        assertEquals(2, sessions.length);
        for (int i = 0; i < sessions.length; i++) {
            assertTrue(sessions[i] instanceof TraceSessionComponent);
        }
        assertEquals("mysession1", sessions[0].getName());
        assertEquals("mysession", sessions[1].getName());

        // ------------------------------------------------------------------------
        // Verify TraceSessionComponent
        // ------------------------------------------------------------------------
        TraceSessionComponent session = (TraceSessionComponent)sessions[1];
        assertEquals("mysession", session.getName());
        assertEquals("/home/user/lttng-traces/mysession-20120129-084256", session.getSessionPath());
        assertEquals(TraceSessionState.ACTIVE, session.getSessionState());

        // Verify setters and setters
        verifySessionGetterSetters(session);

        ITraceControlComponent[] domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(2, domains.length);

        // ------------------------------------------------------------------------
        // Verify Kernel domain
        // ------------------------------------------------------------------------
        assertEquals("Kernel", domains[0].getName());
        ITraceControlComponent[] channels =  domains[0].getChildren();
        assertNotNull(channels);
        assertEquals(2, channels.length);

        // Verify setters and setters
        verifyDomainGettersSetters((TraceDomainComponent) domains[0]);

        // ------------------------------------------------------------------------
        // Verify Kernel's channel0
        // ------------------------------------------------------------------------
        assertTrue(channels[0] instanceof TraceChannelComponent);
        TraceChannelComponent channel = (TraceChannelComponent) channels[0];
        assertEquals("channel0", channel.getName());
        assertEquals(4, channel.getNumberOfSubBuffers());
        assertEquals("splice()", channel.getOutputType().getInName());
        assertEquals(TraceChannelOutputType.SPLICE, channel.getOutputType());
        assertEquals(false, channel.isOverwriteMode());
        assertEquals(200, channel.getReadTimer());
        assertEquals(TraceEnablement.ENABLED, channel.getState());
        assertEquals(262144, channel.getSubBufferSize());
        assertEquals(0, channel.getSwitchTimer());

        // ------------------------------------------------------------------------
        // Verify event info (kernel, channel0)
        // ------------------------------------------------------------------------
        ITraceControlComponent[] channel0Events = channel.getChildren();
        assertNotNull(channel0Events);
        assertEquals(5, channel0Events.length);
        assertTrue(channel0Events[0] instanceof TraceEventComponent);
        assertTrue(channel0Events[1] instanceof TraceEventComponent);
        assertTrue(channel0Events[2] instanceof TraceProbeEventComponent);
        assertTrue(channel0Events[3] instanceof TraceProbeEventComponent);
        assertTrue(channel0Events[4] instanceof TraceEventComponent);

        TraceEventComponent event = (TraceEventComponent) channel0Events[0];
        assertEquals("block_rq_remap", event.getName());
        assertEquals(TraceLogLevel.TRACE_EMERG, event.getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
        assertEquals(TraceEnablement.ENABLED, event.getState());

        event = (TraceEventComponent) channel0Events[1];
        assertEquals("block_bio_remap", event.getName());
        assertEquals(TraceLogLevel.TRACE_EMERG, event.getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
        assertEquals(TraceEnablement.DISABLED, event.getState());

        TraceProbeEventComponent probeEvent = (TraceProbeEventComponent) channel0Events[2];
        assertEquals("myevent2", probeEvent.getName());
        assertEquals(TraceLogLevel.LEVEL_UNKNOWN, probeEvent.getLogLevel());
        assertEquals(TraceEventType.PROBE, probeEvent.getEventType());
        assertEquals(TraceEnablement.ENABLED, probeEvent.getState());
        assertEquals("0xc0101340", probeEvent.getAddress());
        assertNull(probeEvent.getOffset());
        assertNull(probeEvent.getSymbol());

        // verify getters and setter
        verifyProbeEventGettersSetters(probeEvent);

        probeEvent = (TraceProbeEventComponent) channel0Events[3];
        assertEquals("myevent0", probeEvent.getName());
        assertEquals(TraceLogLevel.LEVEL_UNKNOWN, probeEvent.getLogLevel());
        assertEquals(TraceEventType.PROBE, probeEvent.getEventType());
        assertEquals(TraceEnablement.ENABLED, probeEvent.getState());
        assertNull(probeEvent.getAddress());
        assertEquals("0x0", probeEvent.getOffset());
        assertEquals("init_post", probeEvent.getSymbol());

        event = (TraceEventComponent) channel0Events[4];
        assertEquals("syscalls", event.getName());
        assertEquals(TraceLogLevel.LEVEL_UNKNOWN, event.getLogLevel());
        assertEquals(TraceEventType.SYSCALL, event.getEventType());
        assertEquals(TraceEnablement.ENABLED, event.getState());

        // ------------------------------------------------------------------------
        // Verify Kernel's channel1
        // ------------------------------------------------------------------------
        assertEquals("channel1", channels[1].getName());
        channel = (TraceChannelComponent) channels[1];
        assertEquals(4, channel.getNumberOfSubBuffers());
        assertEquals("splice()", channel.getOutputType().getInName());
        assertEquals(TraceChannelOutputType.SPLICE, channel.getOutputType());
        assertEquals(true, channel.isOverwriteMode());
        assertEquals(400, channel.getReadTimer());
        assertEquals(TraceEnablement.DISABLED, channel.getState());
        assertEquals(524288, channel.getSubBufferSize());
        assertEquals(100, channel.getSwitchTimer());

        // ------------------------------------------------------------------------
        // Verify event info (kernel, channel1)
        // ------------------------------------------------------------------------
        ITraceControlComponent[] channel1Events = channels[1].getChildren();
        assertEquals(0, channel1Events.length);

        // ------------------------------------------------------------------------
        // Verify domain UST global
        // ------------------------------------------------------------------------
        assertEquals("UST global", domains[1].getName());

        ITraceControlComponent[] ustChannels = domains[1].getChildren();

        for (int i = 0; i < ustChannels.length; i++) {
            assertTrue(ustChannels[i] instanceof TraceChannelComponent);
        }

        // ------------------------------------------------------------------------
        // Verify UST global's mychannel1
        // ------------------------------------------------------------------------
        channel = (TraceChannelComponent) ustChannels[0];
        assertEquals("mychannel1", channel.getName());
        assertEquals(8, channel.getNumberOfSubBuffers());
        assertEquals("mmap()", channel.getOutputType().getInName());
        assertEquals(TraceChannelOutputType.MMAP, channel.getOutputType());
        assertEquals(true, channel.isOverwriteMode());
        assertEquals(100, channel.getReadTimer());
        assertEquals(TraceEnablement.DISABLED, channel.getState());
        assertEquals(8192, channel.getSubBufferSize());
        assertEquals(200, channel.getSwitchTimer());

        // verify getters and setters
        verifyChannelGettersSetters(channel);

        // ------------------------------------------------------------------------
        // Verify event info (UST global, mychannel1)
        // ------------------------------------------------------------------------
        ITraceControlComponent[] ustEvents = channel.getChildren();
        assertEquals(0, ustEvents.length);

        // ------------------------------------------------------------------------
        // Verify UST global's channel0
        // ------------------------------------------------------------------------
        channel = (TraceChannelComponent) ustChannels[1];
        assertEquals("channel0", channel.getName());
        assertEquals(4, channel.getNumberOfSubBuffers());
        assertEquals("mmap()", channel.getOutputType().getInName());
        assertEquals(TraceChannelOutputType.MMAP, channel.getOutputType());
        assertEquals(false, channel.isOverwriteMode());
        assertEquals(200, channel.getReadTimer());
        assertEquals(TraceEnablement.ENABLED, channel.getState());
        assertEquals(4096, channel.getSubBufferSize());
        assertEquals(0, channel.getSwitchTimer());

        // ------------------------------------------------------------------------
        // Verify event info (UST global, channel0)
        // ------------------------------------------------------------------------
        ustEvents = channel.getChildren();
        assertEquals(4, ustEvents.length);

        event = (TraceEventComponent) ustEvents[0];
        assertEquals("ust_tests_hello:tptest_sighandler", event.getName());
        assertEquals(TraceLogLevel.TRACE_DEBUG_LINE, event.getLogLevel());
        assertEquals(LogLevelType.LOGLEVEL_ONLY, event.getLogLevelType());
        assertEquals(LogLevelType.LOGLEVEL_ONLY.getShortName(), event.getLogLevelType().getShortName());
        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
        assertEquals(TraceEnablement.DISABLED, event.getState());

        event = (TraceEventComponent) ustEvents[1];
        assertEquals("ust_tests_hello:tptest_sighandler1", event.getName());
        assertEquals(TraceLogLevel.TRACE_INFO, event.getLogLevel());
        assertEquals(LogLevelType.LOGLEVEL, event.getLogLevelType());
        assertEquals(LogLevelType.LOGLEVEL.getShortName(), event.getLogLevelType().getShortName());
        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
        assertEquals(TraceEnablement.DISABLED, event.getState());

        event = (TraceEventComponent) ustEvents[2];
        assertEquals("ust_tests_hello:tptest_sighandler2", event.getName());
        assertEquals(TraceLogLevel.TRACE_DEBUG_SYSTEM, event.getLogLevel());
        assertEquals(LogLevelType.LOGLEVEL_NONE, event.getLogLevelType());
        assertEquals(LogLevelType.LOGLEVEL_NONE.getShortName(), event.getLogLevelType().getShortName());
        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
        assertEquals(TraceEnablement.DISABLED, event.getState());

        event = (TraceEventComponent) ustEvents[3];
        assertEquals("*", event.getName());
        assertEquals(TraceLogLevel.LEVEL_UNKNOWN, event.getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
        assertEquals(TraceEnablement.ENABLED, event.getState());

        // verify getters and setters
        verifyEventGettersSetters(event);

        // disconnect
        node.disconnect();
        assertEquals(TargetNodeState.DISCONNECTED, node.getTargetNodeState());
        assertNotNull(node.getImage());
        assertNotSame(connectedImage, node.getImage());

        node.getParent().removeChild(node);
    }

    private static void verifySessionGetterSetters(TraceSessionComponent session) {
        // save original values
        String name = session.getName();
        String origPath = session.getSessionPath();
        TraceSessionState origState = session.getSessionState();

        // test cases
        session.setName("newName");
        assertEquals("newName", session.getName());

        session.setSessionPath("/home/user/tmp");
        assertEquals("/home/user/tmp", session.getSessionPath());

        session.setSessionState(TraceSessionState.INACTIVE);
        assertEquals(TraceSessionState.INACTIVE, session.getSessionState());
        Image inactiveImage = session.getImage();
        assertNotNull(inactiveImage);

        session.setSessionState("active");
        assertEquals(TraceSessionState.ACTIVE, session.getSessionState());

        Image activeImage = session.getImage();
        assertNotNull(activeImage);
        assertNotSame(activeImage, inactiveImage);

        ITraceControlComponent[] children = session.getChildren();
        TraceDomainComponent[] domains = session.getDomains();

        assertEquals(children.length, domains.length);

        for (int i = 0; i < domains.length; i++) {
            assertEquals(domains[i].getName(), children[i].getName());
        }

        // restore original values
        session.setName(name);
        session.setSessionPath(origPath);
        session.setSessionState(origState);
    }

    private static void verifyDomainGettersSetters(TraceDomainComponent domain) {
        // save original values
        TraceDomainType tmpDomain = domain.getDomain();

        domain.setDomain(TraceDomainType.KERNEL);
        assertEquals(domain.getDomain(), TraceDomainType.KERNEL);
        domain.setDomain(TraceDomainType.UST);
        assertEquals(domain.getDomain(), TraceDomainType.UST);
        domain.setDomain(TraceDomainType.JUL);
        assertEquals(domain.getDomain(), TraceDomainType.JUL);
        domain.setDomain(TraceDomainType.LOG4J);
        assertEquals(domain.getDomain(), TraceDomainType.LOG4J);
        domain.setDomain(TraceDomainType.PYTHON);
        assertEquals(domain.getDomain(), TraceDomainType.PYTHON);
        domain.setDomain(TraceDomainType.UNKNOWN);
        assertEquals(domain.getDomain(), TraceDomainType.UNKNOWN);

        ITraceControlComponent[] children = domain.getChildren();
        TraceChannelComponent[] channels = domain.getChannels();

        assertEquals(children.length, channels.length);

        for (int i = 0; i < channels.length; i++) {
            assertEquals(channels[i].getName(), children[i].getName());
        }

        String nodeName = domain.getTargetNode().getName();
        assertEquals(TARGET_NODE_NAME, nodeName);

        // restore original values
        domain.setDomain(tmpDomain);
    }

    private static void verifyBaseEventGettersSetters(BaseEventComponent event) {
        // save original values
        String name =  event.getName();
        TraceLogLevel level = event.getLogLevel();
        TraceEventType type = event.getEventType();

        // test cases
        event.setName("newName");
        assertEquals("newName", event.getName());

        event.setLogLevel(TraceLogLevel.TRACE_INFO);
        assertEquals(TraceLogLevel.TRACE_INFO, event.getLogLevel());
        event.setLogLevel("TRACE_ALERT");
        assertEquals(TraceLogLevel.TRACE_ALERT, event.getLogLevel());

        event.setEventType(TraceEventType.UNKNOWN);
        assertEquals(TraceEventType.UNKNOWN, event.getEventType());
        event.setEventType("tracepoint");
        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());

        // restore original values
        event.setName(name);
        event.setLogLevel(level);
        event.setEventType(type);
    }

    private static void verifyEventGettersSetters(TraceEventComponent event) {
        // save original values
        String name =  event.getName();
        TraceLogLevel level = event.getLogLevel();
        TraceEventType type = event.getEventType();
        TraceEnablement state = event.getState();

        // test cases
        event.setName("newName");
        assertEquals("newName", event.getName());

        event.setLogLevel(TraceLogLevel.TRACE_INFO);
        assertEquals(TraceLogLevel.TRACE_INFO, event.getLogLevel());
        event.setLogLevel("TRACE_ALERT");
        assertEquals(TraceLogLevel.TRACE_ALERT, event.getLogLevel());

        event.setEventType(TraceEventType.UNKNOWN);
        assertEquals(TraceEventType.UNKNOWN, event.getEventType());
        event.setEventType("tracepoint");
        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());

        event.setState("disabled");
        assertEquals(TraceEnablement.DISABLED, event.getState());

        Image disabledImage = event.getImage();
        assertNotNull(disabledImage);

        event.setState(TraceEnablement.ENABLED);
        assertEquals(TraceEnablement.ENABLED, event.getState());

        Image enabledImage = event.getImage();
        assertNotNull(enabledImage);
        assertNotSame(enabledImage, disabledImage);

        // restore original values
        event.setName(name);
        event.setLogLevel(level);
        event.setEventType(type);
        event.setState(state);
    }

    private static void verifyProbeEventGettersSetters(TraceProbeEventComponent event) {
        // save original values
        String address = event.getAddress();
        String offset = event.getOffset();
        String symbol = event.getSymbol();

        // test cases
        event.setAddress("0xffff1234");
        assertEquals("0xffff1234", event.getAddress());

        event.setOffset("0x1234");
        assertEquals("0x1234", event.getOffset());

        event.setSymbol("init");
        assertEquals("init", event.getSymbol());

        // restore original values
        event.setAddress(address);
        event.setOffset(offset);
        event.setSymbol(symbol);
    }

    private static void verifyChannelGettersSetters(TraceChannelComponent channel) {
        // save original values
        String name = channel.getName();
        int nbSubBuffers = channel.getNumberOfSubBuffers();
        TraceChannelOutputType type = channel.getOutputType();
        boolean mode = channel.isOverwriteMode();
        long readTimer = channel.getReadTimer();
        TraceEnablement state =  channel.getState();
        long subBufferSize = channel.getSubBufferSize();
        long switchTimer = channel.getSwitchTimer();

        // test cases
        channel.setName("newName");
        assertEquals("newName", channel.getName());

        channel.setNumberOfSubBuffers(2);
        assertEquals(2, channel.getNumberOfSubBuffers());

        channel.setOutputType("splice()");
        assertEquals("splice()", channel.getOutputType().getInName());
        assertEquals(TraceChannelOutputType.SPLICE, channel.getOutputType());

        channel.setOverwriteMode(false);
        assertEquals(false, channel.isOverwriteMode());

        channel.setReadTimer(250);
        assertEquals(250, channel.getReadTimer());

        channel.setState("enabled");
        assertEquals(TraceEnablement.ENABLED, channel.getState());

        Image enabledImage = channel.getImage();
        assertNotNull(enabledImage);
        channel.setState(TraceEnablement.DISABLED);
        assertEquals(TraceEnablement.DISABLED, channel.getState());

        Image disabledImage = channel.getImage();
        assertNotNull(disabledImage);
        assertNotSame(enabledImage, disabledImage);

        channel.setSubBufferSize(1024);
        assertEquals(1024, channel.getSubBufferSize());

        channel.setSwitchTimer(1000);
        assertEquals(1000, channel.getSwitchTimer());

        // restore original values
        channel.setName(name);
        channel.setNumberOfSubBuffers(nbSubBuffers);
        channel.setOutputType(type);
        channel.setOverwriteMode(mode);
        channel.setReadTimer(readTimer);
        channel.setState(state);
        channel.setSubBufferSize(subBufferSize);
        channel.setSwitchTimer(switchTimer);
    }

    private static void verifyUstProviderGettersSetters(UstProviderComponent ustProvider) {
        // save original values
        String name = ustProvider.getName();
        int pid = ustProvider.getPid();

        // test cases
        ustProvider.setName("newName");
        assertEquals("newName", ustProvider.getName());

        ustProvider.setPid(9876);
        assertEquals(9876, ustProvider.getPid());

        // restore original values
        ustProvider.setName(name);
        ustProvider.setPid(pid);
    }

}
