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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TargetNodeState;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceChannelOutputType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceEnablement;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceEventType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceLogLevel;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.tracecompass.internal.lttng2.control.stubs.dialogs.CreateSessionDialogStub;
import org.eclipse.tracecompass.internal.lttng2.control.stubs.dialogs.DestroyConfirmDialogStub;
import org.eclipse.tracecompass.internal.lttng2.control.stubs.dialogs.EnableChannelDialogStub;
import org.eclipse.tracecompass.internal.lttng2.control.stubs.dialogs.GetEventInfoDialogStub;
import org.eclipse.tracecompass.internal.lttng2.control.stubs.service.TestRemoteSystemProxy;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.TraceControlDialogFactory;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.BaseEventComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.KernelProviderComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TargetNodeComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceChannelComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceEventComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.UstProviderComponent;
import org.eclipse.tracecompass.tmf.remote.core.proxy.TmfRemoteConnectionFactory;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;

/**
 * The class {@link TraceControlUstProviderTest} contains UST provider
 * handling test cases.
 */
public class TraceControlUstProviderTest {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final String TEST_STREAM = "CreateTreeTest.cfg";
    private static final String SCEN_SCENARIO2_TEST = "Scenario2";

    // ------------------------------------------------------------------------
    // Test data
    // ------------------------------------------------------------------------
    private IRemoteConnection fHost = TmfRemoteConnectionFactory.getLocalConnection();
    private TraceControlTestFacility fFacility;
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
        fProxy = new TestRemoteSystemProxy(fHost);
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
     *
     * @throws Exception
     *             This will fail the test
     */
    @Test
    public void testUstProviderTree() throws Exception {

        fProxy.setTestFile(fTestFile);
        fProxy.setScenario(TraceControlTestFacility.SCEN_INIT_TEST);

        ITraceControlComponent root = fFacility.getControlView().getTraceControlRoot();

        TargetNodeComponent node = new TargetNodeComponent("myNode", root, fProxy);
        root.addChild(node);

        fFacility.waitForJobs();

        fFacility.executeCommand(node, "connect");
        WaitUtils.waitUntil(new TargetNodeConnectedCondition(node));

        // Verify that node is connected
        assertEquals(TargetNodeState.CONNECTED, node.getTargetNodeState());

        // Get provider groups
        ITraceControlComponent[] groups = node.getChildren();
        assertNotNull(groups);
        assertEquals(2, groups.length);

        // Get kernel provider
        ITraceControlComponent[] providers = groups[0].getChildren();
        KernelProviderComponent kernelProvider = (KernelProviderComponent) providers[0];

        // Get kernel provider events and select 2 events
        ITraceControlComponent[] events = kernelProvider.getChildren();
        assertNotNull(events);
        assertEquals(3, events.length);

        BaseEventComponent baseEventInfo0 = (BaseEventComponent) events[0];
        BaseEventComponent baseEventInfo1  = (BaseEventComponent) events[1];

        // Initialize dialog implementations for command execution
        TraceControlDialogFactory.getInstance().setCreateSessionDialog(new CreateSessionDialogStub());
        TraceControlDialogFactory.getInstance().setGetEventInfoDialog(new GetEventInfoDialogStub());
        TraceControlDialogFactory.getInstance().setConfirmDialog(new DestroyConfirmDialogStub());

        // ------------------------------------------------------------------------
        // Create session
        // ------------------------------------------------------------------------
        // Initialize session handling scenario
        fProxy.setScenario(TraceControlTestFacility.SCEN_SCENARIO_SESSION_HANDLING_WITH_PATH);

        CreateSessionDialogStub sessionDialogStub = new CreateSessionDialogStub();
        sessionDialogStub.setSessionPath("/home/user/temp");
        TraceControlDialogFactory.getInstance().setCreateSessionDialog(sessionDialogStub);

        TraceSessionComponent session = fFacility.createSession(groups[1]);

        // Verify that session was created
        assertNotNull(session);
        assertEquals("mysession", session.getName());
        assertEquals("/home/user/temp", session.getSessionPath());
        assertEquals(TraceSessionState.INACTIVE, session.getSessionState());

        // ------------------------------------------------------------------------
        // Enable Channel on UST global domain
        // ------------------------------------------------------------------------
        fProxy.setScenario(SCEN_SCENARIO2_TEST);
        EnableChannelDialogStub channelDialogStub = new EnableChannelDialogStub();
        channelDialogStub.setDomain(TraceDomainType.UST);
        channelDialogStub.getChannelInfo().setOverwriteMode(false);
        channelDialogStub.getChannelInfo().setSwitchTimer(200);
        channelDialogStub.getChannelInfo().setReadTimer(100);
        channelDialogStub.getChannelInfo().setNumberOfSubBuffers(2);
        TraceControlDialogFactory.getInstance().setEnableChannelDialog(channelDialogStub);

        fFacility.executeCommand(session, "enableChannelOnSession");

        // Verify that UST domain was created
        ITraceControlComponent[] domains = session.getChildren();
        assertNotNull(domains);
        assertEquals(1, domains.length);

        assertEquals("UST global", domains[0].getName());

        // Verify that channel was created with correct data
        ITraceControlComponent[]channels =  domains[0].getChildren();
        assertNotNull(channels);
        assertEquals(1, channels.length);

        assertTrue(channels[0] instanceof TraceChannelComponent);
        TraceChannelComponent channel = (TraceChannelComponent) channels[0];
        assertEquals("mychannel", channel.getName());
        assertEquals(2, channel.getNumberOfSubBuffers());
        assertEquals("mmap()", channel.getOutputType().getInName());
        assertEquals(TraceChannelOutputType.MMAP, channel.getOutputType());
        assertEquals(false, channel.isOverwriteMode());
        assertEquals(100, channel.getReadTimer());
        assertEquals(TraceEnablement.ENABLED, channel.getState());
        assertEquals(16384, channel.getSubBufferSize());
        assertEquals(200, channel.getSwitchTimer());

        // ------------------------------------------------------------------------
        // Enable event on default channel on created session above
        // ------------------------------------------------------------------------
        // Get first UST provider
        UstProviderComponent ustProvider = (UstProviderComponent) providers[1];
        assertEquals("/home/user/git/lttng-ust/tests/hello.cxx/.libs/lt-hello [PID=9379]", ustProvider.getName());
        assertEquals(9379, ustProvider.getPid());

        // Get events
        events = ustProvider.getChildren();
        assertNotNull(events);
        assertEquals(2, events.length);

        baseEventInfo0 = (BaseEventComponent) events[0];
        baseEventInfo1 = (BaseEventComponent) events[1];

        ITraceControlComponent[] ustSelection =  { baseEventInfo0, baseEventInfo1 };

        fFacility.executeCommand(ustSelection, "assign.event");

        // verify that events were created under the channel
        // Note that domain and channel has to be re-read because the tree is re-created

        domains = session.getChildren();

        // Verify that channel was created with correct data
        channels =  domains[0].getChildren();

        ITraceControlComponent[] ustEvents = channels[0].getChildren();
        assertEquals(2, ustEvents.length);

        TraceEventComponent event = (TraceEventComponent) ustEvents[0];
        assertEquals("ust_tests_hello:tptest_sighandler", event.getName());
        assertEquals(TraceLogLevel.LEVEL_UNKNOWN, event.getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
        assertEquals(TraceEnablement.ENABLED, event.getState());

        event = (TraceEventComponent) ustEvents[1];
        assertEquals("ust_tests_hello:tptest", ustEvents[1].getName());
        assertEquals(TraceLogLevel.LEVEL_UNKNOWN, event.getLogLevel());
        assertEquals(TraceEventType.TRACEPOINT, event.getEventType());
        assertEquals(TraceEnablement.ENABLED, event.getState());

        // ------------------------------------------------------------------------
        // Disable event components
        // ------------------------------------------------------------------------
        fFacility.executeCommand(event, "disableEvent");

        assertEquals(TraceEnablement.DISABLED, event.getState());

        // ------------------------------------------------------------------------
        // Enable event component
        // ------------------------------------------------------------------------
        fFacility.executeCommand(event, "enableEvent");

        // Verify event state
        assertEquals(TraceEnablement.ENABLED, event.getState());

        // ------------------------------------------------------------------------
        // Destroy session
        // ------------------------------------------------------------------------

        // Initialize session handling scenario
        fProxy.setScenario(TraceControlTestFacility.SCEN_SCENARIO_SESSION_HANDLING);

        fFacility.destroySession(session);

        // Verify that no more session components exist
        assertEquals(0, groups[1].getChildren().length);

        //-------------------------------------------------------------------------
        // Disconnect node
        //-------------------------------------------------------------------------
        fFacility.executeCommand(node, "disconnect");
        assertEquals(TargetNodeState.DISCONNECTED, node.getTargetNodeState());

        //-------------------------------------------------------------------------
        // Delete node
        //-------------------------------------------------------------------------
        fFacility.executeCommand(node, "delete");
        assertEquals(0,fFacility.getControlView().getTraceControlRoot().getChildren().length);
   }
}