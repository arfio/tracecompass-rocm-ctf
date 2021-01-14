/*******************************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.viewers.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarDropDownButton;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.views.TimeGraphViewStub;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.eclipse.ui.IWorkbenchPart;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test the pin and clone functionality
 *
 * @author Loic Prieur-Drevon
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class PinAndCloneTest {

    /** The workbench bot */
    private static SWTWorkbenchBot fBot;

    /** Default project name */
    private static final String TRACE_PROJECT_NAME = "test";
    private static final String TRACETYPE_ID = "org.eclipse.linuxtools.lttng2.ust.tracetype";

    private static final String TIME_GRAPH_VIEW_TITLE = "Time Graph Stub";
    private static final String PINNED_TO_UST_TIME_GRAPH_VIEW_TITLE = "Time Graph Stub <context-switches-ust>";
    private static final String PINNED_TO_KERNEL_TIME_GRAPH_VIEW_TITLE = "Time Graph Stub <context-switches-kernel>";
    private static final String PIN_VIEW_BUTTON_NAME = "Pin View";
    private static final String UNPIN_VIEW_BUTTON_NAME = "Unpin View";
    private static final String PIN_TO_PREFIX = "Pin to ";
    private static final String NEW_VIEW_MENU = "New Time Graph Stub view";
    private static final String PINNED_TO_PREFIX = "pinned to ";
    private static final String CLONED_TRACE_SUFFIX = " | 2";
    private static final String FOLLOW_TIME_UPDATES_FROM_OTHER_TRACES = "Follow time updates from other traces";

    private static final int SECOND = 1000000000;
    private static final long UST_START = 1450193697034689597L;
    private static final long UST_END =   1450193745774189602L;

    private static final @NonNull TmfTimeRange RANGE = new TmfTimeRange(TmfTimestamp.fromNanos(UST_START + SECOND), TmfTimestamp.fromNanos(UST_START + 2 * SECOND));
    private static final @NonNull TmfTimeRange INITIAL_UST_RANGE = new TmfTimeRange(TmfTimestamp.fromNanos(UST_START), TmfTimestamp.fromNanos(1450193697134689597L));

    private static final @NonNull String PINNED_TO_UST_TIME_GRAPH_VIEW_TITLE2 = "Time Graph Stub <context-switches-ust | 2>";

    private SWTBotView fOriginalViewBot;

    private CtfTmfTrace fUstTestTrace;

    /**
     * Before Class
     */
    @BeforeClass
    public static void beforeClass() {
        SWTBotUtils.initialize();

        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        fBot = new SWTWorkbenchBot();
        SWTBotUtils.closeView("welcome", fBot);
        /* Create the trace project */
        SWTBotUtils.createProject(TRACE_PROJECT_NAME);
        /* Finish waiting for eclipse to load */
        WaitUtils.waitForJobs();
    }

    /**
     * Close the editor
     */
    @AfterClass
    public static void tearDown() {
        SWTBotUtils.deleteProject(TRACE_PROJECT_NAME, fBot);
    }

    /**
     * Set up
     */
    @Before
    public void setup() {
        fUstTestTrace = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.CONTEXT_SWITCHES_UST);
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, fUstTestTrace.getPath(), TRACETYPE_ID);
        SWTBotUtils.activateEditor(fBot, fUstTestTrace.getName());

        SWTBotUtils.openView(TimeGraphViewStub.ID);
        fOriginalViewBot = fBot.viewById(TimeGraphViewStub.ID);
        fOriginalViewBot.show();
    }

    /**
     * After Test
     */
    @After
    public void after() {
        fBot.closeAllEditors();
        SWTBotUtils.closeSecondaryShells(fBot);
        fUstTestTrace.dispose();
    }

    /**
     * Test that the pin / unpin button and drop down are present / works
     */
    @Test
    public void testPinSingleTrace() {
        // ensure that the view name is correct before
        assertEquals(TIME_GRAPH_VIEW_TITLE, fOriginalViewBot.getTitle());

        // ensure that the pin drop down is present, pin the view.
        fBot.waitUntil(new DefaultCondition() {
            WidgetNotFoundException fException;

            @Override
            public boolean test() throws Exception {
                try {
                    SWTBotToolbarDropDownButton toolbarDropDownButton = fOriginalViewBot.toolbarDropDownButton(PIN_VIEW_BUTTON_NAME);
                    toolbarDropDownButton.menuItem(PIN_TO_PREFIX + fUstTestTrace.getName()).click();
                    return true;
                } catch (WidgetNotFoundException e) {
                    fException = e;
                    return false;
                }
            }

            @Override
            public String getFailureMessage() {
                return "Traces not available in toolbar drop down menu: " + fException;
            }
        });

        // ensure that the view has been renamed. Get the view by title and ensure it
        // has the same widget as there is a renaming bug.
        assertOriginalViewTitle(PINNED_TO_UST_TIME_GRAPH_VIEW_TITLE);

        fOriginalViewBot.toolbarButton(UNPIN_VIEW_BUTTON_NAME).click();

        // ensure that the view has been renamed. Get the view by title and ensure it
        // has the same widget as there is a renaming bug.
        assertOriginalViewTitle(TIME_GRAPH_VIEW_TITLE);

        // ensure that the pin button is present, pin the view.
        fOriginalViewBot.toolbarButton(PIN_VIEW_BUTTON_NAME).click();

        // ensure that the view has been renamed. Get the view by title and ensure it
        // has the same widget as there is a renaming bug.
        assertOriginalViewTitle(PINNED_TO_UST_TIME_GRAPH_VIEW_TITLE);
    }

    private void assertOriginalViewTitle(String newName) {
        Widget expectedWidget = fOriginalViewBot.getWidget();
        assertNotNull(expectedWidget);
        SWTBotView actualView = fBot.viewByTitle(newName);
        Widget actualWidget = actualView.getWidget();
        assertNotNull(actualWidget);
        assertEquals(expectedWidget, actualWidget);
    }

    /**
     * Test the behavior with two traces.
     */
    @Ignore
    @Test
    public void testPinTwoTraces() {
        ITmfTrace ust = TmfTraceManager.getInstance().getActiveTrace();
        assertNotNull(ust);
        ITmfTrace kernelTestTrace = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.CONTEXT_SWITCHES_KERNEL);
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, kernelTestTrace.getPath(), TRACETYPE_ID);
        /* Finish waiting for the trace to index */
        WaitUtils.waitForJobs();
        SWTBotEditor kernelEditor = SWTBotUtils.activateEditor(fBot, kernelTestTrace.getName());
        // wait for the editor to be ready.
        fBot.editorByTitle(kernelTestTrace.getName());

        // assert that the pin to drop down menuItems are present for both traces.
        fBot.waitUntil(new DefaultCondition() {
            WidgetNotFoundException fException;

            @Override
            public boolean test() throws Exception {
                try {
                    SWTBotToolbarDropDownButton toolbarDropDownButton = fOriginalViewBot.toolbarDropDownButton(PIN_VIEW_BUTTON_NAME);
                    toolbarDropDownButton.menuItem(PIN_TO_PREFIX + kernelTestTrace.getName());
                    toolbarDropDownButton.menuItem(PIN_TO_PREFIX + fUstTestTrace.getName()).click();
                    return true;
                } catch (WidgetNotFoundException e) {
                    fException = e;
                    return false;
                }
            }

            @Override
            public String getFailureMessage() {
                return "Traces not available in toolbar drop down menu: " + fException;
            }
        });

        /*
         * assert that the pinned view is the UST trace despite the active trace being
         * the kernel trace.
         */
        assertOriginalViewTitle(PINNED_TO_UST_TIME_GRAPH_VIEW_TITLE);
        ITmfTrace activeTrace = TmfTraceManager.getInstance().getActiveTrace();
        assertNotNull("There should be an active trace", activeTrace);
        assertEquals("context-switches-kernel should be the active trace", kernelTestTrace.getName(), activeTrace.getName());

        // Get the window range of the kernel trace
        TmfTraceManager traceManager = TmfTraceManager.getInstance();
        ITmfTrace kernelTrace = traceManager.getActiveTrace();
        assertNotNull(kernelTrace);

        // switch back and forth
        SWTBotUtils.activateEditor(fBot, fUstTestTrace.getName());
        assertOriginalViewTitle(PINNED_TO_UST_TIME_GRAPH_VIEW_TITLE);

        SWTBotUtils.activateEditor(fBot, kernelTestTrace.getName());
        assertOriginalViewTitle(PINNED_TO_UST_TIME_GRAPH_VIEW_TITLE);

        IWorkbenchPart part = fOriginalViewBot.getViewReference().getPart(false);
        assertTrue(part instanceof AbstractTimeGraphView);
        AbstractTimeGraphView abstractTimeGraphView = (AbstractTimeGraphView) part;
        TmfSignalManager.dispatchSignal(new TmfWindowRangeUpdatedSignal(this, RANGE, kernelTrace));

        // assert that the ust trace's window range did not change
        SWTBotUtils.activateEditor(fBot, fUstTestTrace.getName());
        fBot.waitUntil(ConditionHelpers.timeGraphRangeCondition(abstractTimeGraphView, ust, INITIAL_UST_RANGE));

        // unpin from another active trace
        SWTBotUtils.activateEditor(fBot, kernelTrace.getName());
        fOriginalViewBot.toolbarButton(UNPIN_VIEW_BUTTON_NAME).click();
        assertOriginalViewTitle(TIME_GRAPH_VIEW_TITLE);

        fOriginalViewBot.toolbarButton(PIN_VIEW_BUTTON_NAME).click();
        assertOriginalViewTitle(PINNED_TO_KERNEL_TIME_GRAPH_VIEW_TITLE);

        SWTBotTable kernelEventTable = kernelEditor.bot().table();
        SWTBotTableItem kernelEvent = kernelEventTable.getTableItem(5);
        kernelEvent.contextMenu(FOLLOW_TIME_UPDATES_FROM_OTHER_TRACES).click();

        TmfTimeRange expectedUstWindowRange = new TmfTimeRange(TmfTimestamp.fromNanos(UST_START + SECOND), TmfTimestamp.fromNanos(UST_END - SECOND));
        TmfSignalManager.dispatchSignal(new TmfWindowRangeUpdatedSignal(this, expectedUstWindowRange, ust));
        fBot.waitUntil(ConditionHelpers.timeGraphRangeCondition(abstractTimeGraphView, kernelTrace, expectedUstWindowRange));

        // close the pinned trace
        SWTBotEditor kernelTable = fBot.editorByTitle(kernelTestTrace.getName());
        kernelTable.close();
        assertOriginalViewTitle(TIME_GRAPH_VIEW_TITLE);

        kernelTestTrace.dispose();
    }

    /**
     * Test the cloning feature.
     */
    @Test
    public void testCloneSingleTrace() {
        // single trace.
        SWTBotMenu cloneMenu = fOriginalViewBot.viewMenu().menu(NEW_VIEW_MENU);

        /*
         * assert that the original editor was not renamed and that the cloned one
         * exists and is pinned to the UST trace.
         */
        cloneMenu.menu(PINNED_TO_PREFIX + fUstTestTrace.getName()).click();
        assertOriginalViewTitle(TIME_GRAPH_VIEW_TITLE);
        SWTBotView clonedView = fBot.viewByTitle(PINNED_TO_UST_TIME_GRAPH_VIEW_TITLE);
        assertEquals("Should not have created a new instance", 1, fBot.editors().size());
        clonedView.close();

        /*
         * Assert that a new instance is created.
         */
        cloneMenu.menu(PINNED_TO_PREFIX + fUstTestTrace.getName() + " | new instance").click();
        assertOriginalViewTitle(TIME_GRAPH_VIEW_TITLE);
        clonedView = fBot.viewByTitle(PINNED_TO_UST_TIME_GRAPH_VIEW_TITLE2);
        assertEquals("Should have created a new instance", 2, fBot.editors().size());
        SWTBotEditor cloneEditor = fBot.editorByTitle(fUstTestTrace.getName() + CLONED_TRACE_SUFFIX);

        // Get the window range of the cloned trace
        TmfTraceManager traceManager = TmfTraceManager.getInstance();
        ITmfTrace cloneTrace = traceManager.getActiveTrace();
        assertNotNull(cloneTrace);

        // go back to original trace, pin it
        SWTBotUtils.activateEditor(fBot, fUstTestTrace.getName());
        fOriginalViewBot.toolbarButton(PIN_VIEW_BUTTON_NAME).click();
        ITmfTrace ust = traceManager.getActiveTrace();
        TmfSignalManager.dispatchSignal(new TmfWindowRangeUpdatedSignal(this, RANGE, ust));

        // assert that the cloned trace's window range did not change
        SWTBotUtils.activateEditor(fBot, cloneTrace.getName() + CLONED_TRACE_SUFFIX);
        IWorkbenchPart part = clonedView.getViewReference().getPart(false);
        assertTrue(part instanceof AbstractTimeGraphView);
        AbstractTimeGraphView abstractTimeGraphView = (AbstractTimeGraphView) part;
        fBot.waitUntil(ConditionHelpers.timeGraphRangeCondition(abstractTimeGraphView, cloneTrace, INITIAL_UST_RANGE));
        cloneEditor.close();
    }

    /**
     * Test the follow time updates functionality
     */
    @Test
    public void testFollow() {
        TmfTraceManager traceManager = TmfTraceManager.getInstance();
        ITmfTrace ust = traceManager.getActiveTrace();
        assertNotNull(ust);
        ITmfTrace kernelTest = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.CONTEXT_SWITCHES_KERNEL);
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, kernelTest.getPath(), TRACETYPE_ID);
        /* Finish waiting for the trace to index */
        WaitUtils.waitForJobs();
        SWTBotEditor kernelEditor = SWTBotUtils.activateEditor(fBot, kernelTest.getName());
        // wait for the editor to be ready.
        fBot.editorByTitle(kernelTest.getName());
        ITmfTrace kernel = traceManager.getActiveTrace();
        assertNotNull(kernel);

        SWTBotTable kernelEventTable = kernelEditor.bot().table();
        SWTBotTableItem kernelEvent = kernelEventTable.getTableItem(5);
        kernelEvent.contextMenu(FOLLOW_TIME_UPDATES_FROM_OTHER_TRACES).click();
        SWTBotUtils.activateEditor(fBot, ust.getName());
        TmfSignalManager.dispatchSignal(new TmfWindowRangeUpdatedSignal(this, RANGE, ust));

        // assert that the kernel trace followed the ust trace's range
        IWorkbenchPart part = fOriginalViewBot.getViewReference().getPart(false);
        assertTrue(part instanceof AbstractTimeGraphView);
        AbstractTimeGraphView abstractTimeGraphView = (AbstractTimeGraphView) part;
        fBot.waitUntil(ConditionHelpers.timeGraphRangeCondition(abstractTimeGraphView, ust, RANGE));

        SWTBotUtils.activateEditor(fBot, kernel.getName());
        fBot.waitUntil(ConditionHelpers.timeGraphRangeCondition(abstractTimeGraphView, kernel, RANGE));

        // unfollow (don't use context menu on table item to avoid updating selection)
        kernelEventTable.contextMenu(FOLLOW_TIME_UPDATES_FROM_OTHER_TRACES).click();
        TmfSignalManager.dispatchSignal(new TmfWindowRangeUpdatedSignal(this, ust.getInitialTimeRange(), ust));
        fBot.waitUntil(ConditionHelpers.timeGraphRangeCondition(abstractTimeGraphView, kernel, RANGE));

        kernelTest.dispose();
    }

}
