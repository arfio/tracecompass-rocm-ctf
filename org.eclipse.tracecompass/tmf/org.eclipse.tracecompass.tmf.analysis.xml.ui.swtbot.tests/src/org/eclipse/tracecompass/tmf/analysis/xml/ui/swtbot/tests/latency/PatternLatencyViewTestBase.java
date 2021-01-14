/*******************************************************************************
 * Copyright (c) 2016, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.ui.swtbot.tests.latency;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlAnalysisModuleSource;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestFiles;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * Base class for pattern latency view test
 *
 * @author Jean-Christian Kouame
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public abstract class PatternLatencyViewTestBase {

    private static final String PROJECT_NAME = "test";
    private static final String TRACE_TYPE = "org.eclipse.linuxtools.lttng2.kernel.tracetype";
    private static final String TRACE_NAME = "bug446190";

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();
    /**
     * The workbench bot
     */
    protected static SWTWorkbenchBot fBot;

    /**
     * Things to setup
     */
    @BeforeClass
    public static void beforeClass() {

        SWTBotUtils.initialize();
        Thread.currentThread().setName("SWTBotTest");
        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
        /* Finish waiting for eclipse to load */
        WaitUtils.waitForJobs();

        fBot = new SWTWorkbenchBot();

        loadXmlFile();
        openTrace();
    }

    /**
     * Clean up
     */
    @AfterClass
    public static void afterClass() {
        fLogger.removeAllAppenders();
    }

    /**
     * Create a tracing project and open the test trace
     */
    private static void openTrace() {
        try {
            String tracePath = FileUtils.toFile(FileLocator.toFileURL(CtfTestTrace.ARM_64_BIT_HEADER.getTraceURL())).getAbsolutePath();
            SWTBotUtils.createProject(PROJECT_NAME);
            SWTBotUtils.openTrace(PROJECT_NAME, tracePath, TRACE_TYPE);
            WaitUtils.waitForJobs();
        } catch (IOException e) {
            fail("Failed to get the trace.");
        }
    }

    /**
     * Bypassing the native import wizard and programmatically load the XML
     * analysis
     */
    private static void loadXmlFile() {
        XmlUtils.addXmlFile(TmfXmlTestFiles.VALID_PATTERN_FILE.getFile());
        XmlAnalysisModuleSource.notifyModuleChange();
    }

    /**
     * set up before the test and open the latency view
     */
    @Before
    public void before() {
        openView(getViewTitle());
    }

    /**
     * Closes the view
     */
    @After
    public void closeView() {
        fBot.closeAllEditors();
        SWTBotUtils.deleteProject(PROJECT_NAME, fBot);

        SWTBotUtils.closeViewById(getViewId(), fBot);
    }

    /**
     * Open a pattern latency view
     *
     * @param viewTitle
     *            The view title
     */
    private static void openView(final String viewTitle) {
        SWTBotTreeItem treeItem = SWTBotUtils.selectTracesFolder(fBot, PROJECT_NAME);
        treeItem = SWTBotUtils.getTreeItem(fBot, treeItem, TRACE_NAME, "Views", "XML system call analysis", viewTitle);
        treeItem.doubleClick();
        WaitUtils.waitForJobs();
    }

    /**
     * Get the id of the latency view tested
     *
     * @return The view id
     */
    protected abstract String getViewId();

    /**
     * Get the the latency view title
     *
     * @return The view title
     */
    protected abstract String getViewTitle();
}
