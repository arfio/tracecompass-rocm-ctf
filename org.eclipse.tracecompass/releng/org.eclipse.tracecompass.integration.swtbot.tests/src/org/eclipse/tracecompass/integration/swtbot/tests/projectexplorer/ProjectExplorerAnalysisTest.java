/******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.integration.swtbot.tests.projectexplorer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

/**
 * SWTBot test for testing Project Explorer Views folder
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class ProjectExplorerAnalysisTest {

    @NonNull private static final String EXPERIMENT_NAME = "TestExperiment";
    @NonNull private static final String IRQ_XML_ANALYSIS_NAME = "IRQ Analysis";
    @NonNull private static final String IRQ_XML_ANALYSIS_HT_FILE_NAME = "lttng.analysis.irq.ht";
    @NonNull private static final String FUTEX_XML_ANALYSIS_NAME = "Futex Contention Analysis";
    @NonNull private static final String FUTEX_XML_ANALYSIS_HT_FILE_NAME = "lttng.analysis.futex.ht";

    private static SWTWorkbenchBot fBot;
    private static String fUstTraceFile;
    private static String fKernelTraceFile;

    private static final class AnalysisNode {

        private final String fTitle;
        private final boolean fEnabled;
        private final boolean fVisible;

        public AnalysisNode(String title, boolean enabled, boolean visible) {
            fTitle = title;
            fEnabled = enabled;
            fVisible = visible;
        }

        @Override
        public int hashCode() {
            return Objects.hash(fTitle, fEnabled, fVisible);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof AnalysisNode) {
                AnalysisNode analysisNode = (AnalysisNode) obj;
                return analysisNode.fEnabled == fEnabled &&
                        analysisNode.fVisible == fVisible &&
                        Objects.equals(analysisNode.fTitle, fTitle);
            }
            return false;
        }

        @Override
        public String toString() {
            return "new AnalysisNode( \"" + fTitle + "\", " + fEnabled + " , " + fVisible + ")";
        }
    }

    private static final String TRACE_PROJECT_NAME = "test";
    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();

    private static final Set<AnalysisNode> KERNEL_ANALYSIS_NODES = ImmutableSet.of(
            new AnalysisNode("Active Thread", true, true),
            new AnalysisNode("Context switch", true, true),
            new AnalysisNode("CPU usage", true, true),
            new AnalysisNode("Counters", true, true),
            new AnalysisNode("Futex Contention Analysis", true, true),
            new AnalysisNode("Input/Output", true, true),
            new AnalysisNode("IRQ Analysis", true, true),
            new AnalysisNode("Kernel memory usage", true, true),
            new AnalysisNode("Linux Kernel", true, true),
            new AnalysisNode("OS Execution Graph", true, true),
            new AnalysisNode("Statistics", true, true),
            new AnalysisNode("System Call Latency", true, true));

    private static final Set<AnalysisNode> UST_ANALYSIS_NODES = ImmutableSet.of(
            new AnalysisNode("Counters", true, true),
            new AnalysisNode("Debug Info", true, true),
            new AnalysisNode("LTTng-UST CallStack", true, true),
            new AnalysisNode("Statistics", true, true),
            new AnalysisNode("Ust Memory", true, true));

    private static final Set<AnalysisNode> EXPERIMENT_NODES;

    static {
         ImmutableSet.Builder<AnalysisNode> builder = new ImmutableSet.Builder<>();
         builder.addAll(UST_ANALYSIS_NODES);
         builder.addAll(KERNEL_ANALYSIS_NODES);
         EXPERIMENT_NODES = builder.build();
    }

    /**
     * Test Class setup
     */
    @BeforeClass
    public static void init() {
        SWTBotUtils.initialize();

        /* Set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
        fBot = new SWTWorkbenchBot();

        /* Close welcome view */
        SWTBotUtils.closeView("Welcome", fBot);

        /* Switch perspectives */
        SWTBotUtils.switchToTracingPerspective();

        /* Finish waiting for eclipse to load */
        WaitUtils.waitForJobs();
        SWTBotUtils.createProject(TRACE_PROJECT_NAME);

        File kernelTraceFile = new File(CtfTmfTestTraceUtils.getTrace(CtfTestTrace.ARM_64_BIT_HEADER).getPath());
        CtfTmfTestTraceUtils.dispose(CtfTestTrace.ARM_64_BIT_HEADER);

        File ustTraceFile = new File(CtfTmfTestTraceUtils.getTrace(CtfTestTrace.DEBUG_INFO_SYNTH_EXEC).getPath());
        CtfTmfTestTraceUtils.dispose(CtfTestTrace.DEBUG_INFO_SYNTH_EXEC);

        fKernelTraceFile = kernelTraceFile.getName();
        fUstTraceFile = ustTraceFile.getName();

        /* Open Traces*/
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, kernelTraceFile.getAbsolutePath(), "org.eclipse.linuxtools.lttng2.kernel.tracetype");
        fBot.waitUntil(ConditionHelpers.isEditorOpened(fBot, fKernelTraceFile));

        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, ustTraceFile.getAbsolutePath(), "org.eclipse.linuxtools.lttng2.ust.tracetype");
        fBot.waitUntil(ConditionHelpers.isEditorOpened(fBot, fUstTraceFile));

        /* Create experiment and open experiment */
        SWTBotUtils.createExperiment(fBot, TRACE_PROJECT_NAME, EXPERIMENT_NAME);

        SWTBotTreeItem project = SWTBotUtils.selectProject(fBot, TRACE_PROJECT_NAME);

        SWTBotTreeItem tracesFolder = SWTBotUtils.getTraceProjectItem(fBot, project, "Traces");
        tracesFolder.expand();

        SWTBotTreeItem ustTrace = tracesFolder.getNode(fUstTraceFile);
        SWTBotTreeItem kernelTrace = tracesFolder.getNode(fKernelTraceFile);
        SWTBotTreeItem experiment = SWTBotUtils.getTraceProjectItem(fBot, project, "Experiments", EXPERIMENT_NAME);

        ustTrace.dragAndDrop(experiment);
        kernelTrace.dragAndDrop(experiment);
        experiment.doubleClick();
    }

    /**
     * Test class tear down method.
     */
    @AfterClass
    public static void tearDown() {
        fBot.closeAllEditors();
        SWTBotUtils.deleteProject(TRACE_PROJECT_NAME, new SWTWorkbenchBot());
        fLogger.removeAllAppenders();
    }

    /**
     * Test tear down method.
     */
    @After
    public void afterTest() {
        SWTBotUtils.closeSecondaryShells(new SWTWorkbenchBot());
    }

    /**
     * Test which analyses are present for a kernel trace
     */
    @Test
    public void testKernelAnalyses() {
        Set<AnalysisNode> actualNodes = getAnalysisNodes(fBot, fKernelTraceFile, false);
        StringJoiner sj = new StringJoiner(", ", "{", "}");
        actualNodes.forEach(node -> sj.add(node.fTitle));
        assertTrue(sj.toString(), actualNodes.containsAll(KERNEL_ANALYSIS_NODES));
        if (!KERNEL_ANALYSIS_NODES.containsAll(actualNodes)) {
            SetView<AnalysisNode> diff = Sets.difference(KERNEL_ANALYSIS_NODES, actualNodes);
            diff.forEach(elem -> System.err.println("New untested analysis : " + elem));
        }
    }

    /**
     * Test which analyses are present for a user space trace
     */
    @Test
    public void testUstAnalyses() {
        Set<AnalysisNode> actualNodes = getAnalysisNodes(fBot, fUstTraceFile, false);
        StringJoiner sj = new StringJoiner(", ", "{", "}");
        actualNodes.forEach(node -> sj.add(node.fTitle));
        assertTrue(sj.toString(), actualNodes.containsAll(UST_ANALYSIS_NODES));

        if (!UST_ANALYSIS_NODES.containsAll(actualNodes)) {
            SetView<AnalysisNode> diff = Sets.difference(UST_ANALYSIS_NODES, actualNodes);
            diff.forEach(elem -> System.err.println("New untested analysis : " + elem));
        }
    }

    /**
     * Test which analyses are present for a experiment with kernel and ust trace
     */
    @Test
    public void testExperimentAnalyses() {
        Set<AnalysisNode> actualNodes = getAnalysisNodes(fBot, EXPERIMENT_NAME, true);
        StringJoiner sj = new StringJoiner(", ", "{", "}");
        actualNodes.forEach(node -> sj.add(node.fTitle));

        assertTrue(sj.toString(), actualNodes.containsAll(EXPERIMENT_NODES));
        if (!EXPERIMENT_NODES.containsAll(actualNodes)) {
            SetView<AnalysisNode> diff = Sets.difference(EXPERIMENT_NODES, actualNodes);
            diff.forEach(elem -> System.err.println("New untested analysis : " + elem));
        }
    }
    /**
     * Test execution using double-click or context sensitive menu
     */
    @Test
    public void testAnalysisRun() {
        SWTBotTreeItem traceNode = getExpandedTraceNode(fBot, fKernelTraceFile, false);
        SWTBotTreeItem viewNode = traceNode.getNode("Views");
        viewNode.expand();

        boolean supplExists = supplementaryFileExists(traceNode, IRQ_XML_ANALYSIS_HT_FILE_NAME);
        assertFalse(supplExists);
        SWTBotTreeItem irqAnalysisNode = viewNode.getNode(IRQ_XML_ANALYSIS_NAME);
        irqAnalysisNode.contextMenu().menu("Open").click();
        supplExists = supplementaryFileExists(traceNode, IRQ_XML_ANALYSIS_HT_FILE_NAME);
        assertTrue(supplExists);

        supplExists = supplementaryFileExists(traceNode, FUTEX_XML_ANALYSIS_HT_FILE_NAME);
        assertFalse(supplExists);
        SWTBotTreeItem futexAnalysisNode = viewNode.getNode(FUTEX_XML_ANALYSIS_NAME);
        futexAnalysisNode.doubleClick();
        supplExists = supplementaryFileExists(traceNode, FUTEX_XML_ANALYSIS_HT_FILE_NAME);
        assertTrue(supplExists);
    }

    private static Set<AnalysisNode> getAnalysisNodes(SWTWorkbenchBot bot, String name, boolean isExperiment) {
        SWTBotTreeItem traceNode = getExpandedTraceNode(bot, name, isExperiment);
        SWTBotTreeItem viewNode = traceNode.getNode("Views");
        viewNode.expand();

        SWTBotTreeItem[] analysisNodes = viewNode.getItems();
        assertNotNull(analysisNodes);
        int length = analysisNodes.length;
        Set<AnalysisNode> actualNodes = new HashSet<>();
        for (int i = 0; i < length; i++) {
            SWTBotTreeItem analysisNode = analysisNodes[i];
            actualNodes.add(new AnalysisNode(analysisNode.getText(), analysisNode.isEnabled(), analysisNode.isVisible()));
        }
        return actualNodes;
    }

    private static SWTBotTreeItem getExpandedTraceNode(SWTWorkbenchBot bot, String name, boolean isExperiment) {
        SWTBotTreeItem project = SWTBotUtils.selectProject(fBot, TRACE_PROJECT_NAME);
        SWTBotTreeItem traceNode;
        if (isExperiment) {
            SWTBotTreeItem tracesFolderItem = SWTBotUtils.getTraceProjectItem(fBot, project, "Experiments");
            tracesFolderItem.select();
            tracesFolderItem.expand();
            traceNode = SWTBotUtils.getTraceProjectItem(fBot, tracesFolderItem, name);
        } else {
            SWTBotTreeItem tracesFolderItem = SWTBotUtils.selectTracesFolder(bot, TRACE_PROJECT_NAME);
            tracesFolderItem.expand();
            traceNode = SWTBotUtils.getTraceProjectItem(fBot, tracesFolderItem, name);
        }
        traceNode.expand();

        return traceNode;
    }

    private static boolean supplementaryFileExists(SWTBotTreeItem traceNode, String filename) {
        // make sure that analysis are finished
        WaitUtils.waitForJobs();
        try {
            ResourcesPlugin.getWorkspace().getRoot().getProject(TRACE_PROJECT_NAME).refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (CoreException e) {
        }
        // Make sure that all jobs are finished after refresh
        WaitUtils.waitForJobs();

        traceNode.contextMenu().menu("Delete Supplementary Files...").click();

        SWTBotShell shell = fBot.shell("Delete Resources");
        SWTBot bot = shell.bot();
        SWTBotTree tree = bot.tree();
        SWTBotTreeItem traceSupplNode = SWTBotUtils.getTreeItem(bot, tree, traceNode.getText());

        SWTBotTreeItem[] supplFileNodes = traceSupplNode.getItems();

        boolean supplFound = false;
        for (SWTBotTreeItem swtBotTreeItem : supplFileNodes) {
            if (swtBotTreeItem.getText().equals(filename)) {
                supplFound = true;
                break;
            }
        }

        bot.button("Cancel").click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        return supplFound;
    }
}
