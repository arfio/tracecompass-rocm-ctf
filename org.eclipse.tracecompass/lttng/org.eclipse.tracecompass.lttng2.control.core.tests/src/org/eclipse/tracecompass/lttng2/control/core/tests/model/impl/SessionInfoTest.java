/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
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
 **********************************************************************/

package org.eclipse.tracecompass.lttng2.control.core.tests.model.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.tracecompass.internal.lttng2.control.core.model.IDomainInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.ISessionInfo;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.impl.SessionInfo;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>ChannelInfoTest</code> contains tests for the class
 * <code>{@link SessionInfo}</code>.
 */
public class SessionInfoTest {

    // ------------------------------------------------------------------------
    // Test data
    // ------------------------------------------------------------------------

    private ISessionInfo fSessionInfo1 = null;
    private ISessionInfo fSessionInfo2 = null;

    private IDomainInfo fDomainInfo1 = null;
    private IDomainInfo fDomainInfo2 = null;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        ModelImplFactory factory = new ModelImplFactory();
        fSessionInfo1 = factory.getSessionInfo1();
        fDomainInfo1 = factory.getDomainInfo1();
        fSessionInfo2 = factory.getSessionInfo2();
        fDomainInfo2 = factory.getDomainInfo2();
    }

    // ------------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Run the ChannelInfo() constructor test.
     */
    @Test
    public void testSessionInfo() {
        ISessionInfo result = new SessionInfo("test");
        assertNotNull(result);

        assertEquals("test", result.getName());
        assertEquals("", result.getSessionPath());
        TraceSessionState state = result.getSessionState();
        assertEquals("inactive", state.getInName());
        assertEquals("INACTIVE", state.name());
        assertEquals("INACTIVE", state.toString());
        assertEquals(0, state.ordinal());
        assertEquals(0, result.getDomains().length);
        assertFalse(result.isSnapshotSession());
        assertNull(result.getNetworkUrl());
        assertNull(result.getControlUrl());
        assertNull(result.getDataUrl());
    }

    /**
     * Test copy constructor.
     */
    @Test
    public void testSessionInfoCopy() {
        SessionInfo sessionInfo = new SessionInfo((SessionInfo)fSessionInfo1);

        assertEquals(sessionInfo.getName(), fSessionInfo1.getName());
        assertEquals(sessionInfo.getSessionPath(), fSessionInfo1.getSessionPath());
        assertEquals(sessionInfo.getSessionState(), fSessionInfo1.getSessionState());

        IDomainInfo[] orignalDomains = fSessionInfo1.getDomains();
        IDomainInfo[] resultDomains = sessionInfo.getDomains();
        for (int i = 0; i < orignalDomains.length; i++) {
            assertEquals(orignalDomains[i], resultDomains[i]);
        }

        assertEquals(sessionInfo.getNetworkUrl(), fSessionInfo1.getNetworkUrl());
        assertEquals(sessionInfo.getControlUrl(), fSessionInfo1.getControlUrl());
        assertEquals(sessionInfo.getDataUrl(), fSessionInfo1.getDataUrl());
    }

    /**
     * Test copy constructor.
     */
    @Test
    public void testSessionCopy2() {
        try {
            SessionInfo session = null;
            new SessionInfo(session);
            fail("null copy");
        } catch (IllegalArgumentException e) {
            // Success
        }
    }

    /**
     * Run the long getNumberOfSubBuffers() method test.
     */
    @Test
    public void testGetAndSetters() {

        // Note that addDomain() has been executed in setUp()
        // check get method here
        assertEquals(1, fSessionInfo1.getDomains().length);
        assertNotNull(fSessionInfo1.getDomains()[0]);
        assertEquals(fDomainInfo1, fSessionInfo1.getDomains()[0]);

        ISessionInfo session = new SessionInfo("session");
        List<IDomainInfo> list = new LinkedList<>();
        list.add(fDomainInfo1);
        list.add(fDomainInfo2);
        session.setDomains(list);

        IDomainInfo[] result = session.getDomains();
        assertEquals(2, result.length);
        assertEquals(fDomainInfo1, result[0]);
        assertEquals(fDomainInfo2, result[1]);

        session.setSessionPath("/home/user");
        assertEquals("/home/user", session.getSessionPath());

        session.setSessionState("active");
        TraceSessionState state = session.getSessionState();
        state = session.getSessionState();
        assertEquals("active", state.getInName());
        assertEquals("ACTIVE", state.name());
        assertEquals("ACTIVE", state.toString());
        assertEquals(1, state.ordinal());

        session.setSessionState("inactive");
        state = session.getSessionState();
        assertEquals("inactive", state.getInName());
        assertEquals("INACTIVE", state.name());
        assertEquals("INACTIVE", state.toString());
        assertEquals(0, state.ordinal());

        session.setSessionState("test");
        state = session.getSessionState();
        assertEquals("inactive", state.getInName());
        assertEquals("INACTIVE", state.name());
        assertEquals("INACTIVE", state.toString());
        assertEquals(0, state.ordinal());

        session.setSessionState(TraceSessionState.ACTIVE);
        state = session.getSessionState();
        assertEquals("active", state.getInName());
        assertEquals("ACTIVE", state.name());
        assertEquals("ACTIVE", state.toString());
        assertEquals(1, state.ordinal());

        session.setSessionState(TraceSessionState.INACTIVE);
        state = session.getSessionState();
        assertEquals("inactive", state.getInName());
        assertEquals("INACTIVE", state.name());
        assertEquals("INACTIVE", state.toString());
        assertEquals(0, state.ordinal());
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString_1() {
        ISessionInfo fixture = new SessionInfo("sessionName");

        String result = fixture.toString();

        // add additional test code here
        assertEquals("[SessionInfo([TraceInfo(Name=sessionName)],Path=,State=INACTIVE,isStreamedTrace=false,isSnapshot=false,Domains=,NetworkUrl=null,ControlUrl=null,DataUrl=null)]", result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString_2() {
        String result = fSessionInfo1.toString();

        // add additional test code here
        assertEquals(
                "[SessionInfo([TraceInfo(Name=session1)],Path=/home/user/lttng-trace/mysession/,State=ACTIVE,isStreamedTrace=false,"
                + "isSnapshot=false,snapshotInfo=[SnapshotInfo([TraceInfo(Name=snapshot-1)],snapshotPath=/home/user/lttng-trace/mysession/,"
                + "ID=1,isStreamedSnapshot=false)],Domains=[DomainInfo([TraceInfo(Name=test1)],Channels=[ChannelInfo([TraceInfo(Name=channel1)],"
                + "State=DISABLED,OverwriteMode=true,SubBuffersSize=13,NumberOfSubBuffers=12,SwitchTimer=10,ReadTimer=11,output=splice(),"
                + "NumberOfDiscardedEvents=14,NumberOfLostPackets=15,Events=[EventInfo([BaseEventInfo([TraceInfo(Name=event1)],"
                + "type=TRACEPOINT,level=TRACE_DEBUG)],State=ENABLED,levelType=LOGLEVEL_ONLY)])],Loggers=None,isKernel=false)],NetworkUrl=null,"
                + "ControlUrl=null,DataUrl=null)]",
                result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString_3() {
        SessionInfo info = new SessionInfo((SessionInfo)fSessionInfo1);
        info.setSnapshot(false);
        info.setSnapshotInfo(null);
        info.setSessionPath("/home/user/lttng-trace/mysession/");

        String result = info.toString();

        // add additional test code here
        assertEquals("[SessionInfo([TraceInfo(Name=session1)],Path=/home/user/lttng-trace/mysession/,State=ACTIVE,isStreamedTrace=false,"
                + "isSnapshot=false,Domains=[DomainInfo([TraceInfo(Name=test1)],Channels=[ChannelInfo([TraceInfo(Name=channel1)],"
                + "State=DISABLED,OverwriteMode=true,SubBuffersSize=13,NumberOfSubBuffers=12,SwitchTimer=10,ReadTimer=11,output=splice(),"
                + "NumberOfDiscardedEvents=14,NumberOfLostPackets=15,Events=[EventInfo([BaseEventInfo([TraceInfo(Name=event1)],type=TRACEPOINT,"
                + "level=TRACE_DEBUG)],State=ENABLED,levelType=LOGLEVEL_ONLY)])],Loggers=None,isKernel=false)],"
                + "NetworkUrl=null,ControlUrl=null,DataUrl=null)]", result);
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    /**
     * Run the {@link SessionInfo#equals} method test.
     */
    @Test
    public void testEqualsReflexivity() {
        assertTrue("equals", fSessionInfo1.equals(fSessionInfo1));
        assertTrue("equals", fSessionInfo2.equals(fSessionInfo2));

        assertTrue("equals", !fSessionInfo1.equals(fSessionInfo2));
        assertTrue("equals", !fSessionInfo2.equals(fSessionInfo1));
    }

    /**
     * Run the {@link SessionInfo#equals} method test.
     */
    @Test
    public void testEqualsSymmetry() {
        SessionInfo event1 = new SessionInfo((SessionInfo)fSessionInfo1);
        SessionInfo event2 = new SessionInfo((SessionInfo)fSessionInfo2);

        assertTrue("equals", event1.equals(fSessionInfo1));
        assertTrue("equals", fSessionInfo1.equals(event1));

        assertTrue("equals", event2.equals(fSessionInfo2));
        assertTrue("equals", fSessionInfo2.equals(event2));
    }

    /**
     * Run the {@link SessionInfo#equals} method test.
     */
    @Test
    public void testEqualsTransivity() {
        SessionInfo channel1 = new SessionInfo((SessionInfo)fSessionInfo1);
        SessionInfo channel2 = new SessionInfo((SessionInfo)fSessionInfo1);
        SessionInfo channel3 = new SessionInfo((SessionInfo)fSessionInfo1);

        assertTrue("equals", channel1.equals(channel2));
        assertTrue("equals", channel2.equals(channel3));
        assertTrue("equals", channel1.equals(channel3));
    }

    /**
     * Run the {@link SessionInfo#equals} method test.
     */
    @Test
    public void testEqualsNull() {
        assertTrue("equals", !fSessionInfo1.equals(null));
        assertTrue("equals", !fSessionInfo2.equals(null));
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    /**
     * Run the {@link SessionInfo#hashCode} method test.
     */
    @Test
    public void testHashCode() {
        SessionInfo channel1 = new SessionInfo((SessionInfo)fSessionInfo1);
        SessionInfo channel2 = new SessionInfo((SessionInfo)fSessionInfo2);

        assertTrue("hashCode", fSessionInfo1.hashCode() == channel1.hashCode());
        assertTrue("hashCode", fSessionInfo2.hashCode() == channel2.hashCode());

        assertTrue("hashCode", fSessionInfo1.hashCode() != channel2.hashCode());
        assertTrue("hashCode", fSessionInfo2.hashCode() != channel1.hashCode());
    }
}