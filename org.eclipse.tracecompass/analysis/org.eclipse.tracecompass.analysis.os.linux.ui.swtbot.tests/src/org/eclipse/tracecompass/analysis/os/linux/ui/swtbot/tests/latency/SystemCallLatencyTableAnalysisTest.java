/*******************************************************************************
 * Copyright (c) 2015, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.ui.swtbot.tests.latency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.analysis.timing.ui.swtbot.tests.table.SegmentTableTest;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.table.AbstractSegmentStoreTableView;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.table.AbstractSegmentStoreTableViewer;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.table.SegmentStoreTableView;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.latency.SystemCall;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.latency.SystemCallLatencyAnalysis;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * SystemCall Latency Table Test. This adds specific tests for the system call
 * name for the TSV export and adds a column test.
 *
 * @author Matthew Khouzam
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class SystemCallLatencyTableAnalysisTest extends SegmentTableTest {

    private static final String TRACE_TYPE = "org.eclipse.linuxtools.lttng2.kernel.tracetype";
    private static final String PROJECT_NAME = "test";

    private static final String PRIMARY_VIEW_ID = SegmentStoreTableView.ID;
    private static final String SECONDARY_VIEW_ID = SystemCallLatencyAnalysis.ID;
    private static final SystemCallLatencyAnalysis fSystemCallLatencyAnalysis = new SystemCallLatencyAnalysis();
    private static final int TID = 10;
    private static final int RETURN_VALUE = 1;

    @Override
    protected ISegmentStoreProvider getSegStoreProvider() {
        return fSystemCallLatencyAnalysis;
    }

    /**
     * Clean up
     */
    @AfterClass
    public static void afterClass() {
        fSystemCallLatencyAnalysis.dispose();
    }

    @Override
    protected AbstractSegmentStoreTableView openTable() {
        /*
         * Open latency view
         */
        SWTBotUtils.openView(PRIMARY_VIEW_ID, SECONDARY_VIEW_ID);
        SWTBotView viewBot = fBot.viewById(PRIMARY_VIEW_ID);
        final IViewReference viewReference = viewBot.getViewReference();
        IViewPart viewPart = UIThreadRunnable.syncExec(new Result<IViewPart>() {
            @Override
            public IViewPart run() {
                return viewReference.getView(true);
            }
        });
        assertTrue("Could not instanciate view", viewPart instanceof SegmentStoreTableView);
        return (SegmentStoreTableView) viewPart;
    }

    @Override
    protected @NonNull ISegment createSegment(long start, long end) {
        // Notice the string is interned, that saves a lot of ram.
        return new SystemCall(new SystemCall.InitialInfo(start, start % 3 == 0 ? "rightpad" : "leftpad", TID), end, RETURN_VALUE);
    }

    @Test
    @Override
    public void climbTest() {
        super.climbTest();
        SWTBotTable tableBot = new SWTBotTable(getTable().getTableViewer().getTable());
        tableBot.header("System Call").click();
        // this is an assert in the sense that it will timeout if it is not true
        // FIXME: The first one should be leftpad, but because of preceding
        // sorts, it first sort descending in this case
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "rightpad", 0, 3));
        tableBot.header("System Call").click();
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "leftpad", 0, 3));
        // Test that duration still works after having tested System Call
        tableBot.header("Duration").click();
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "0", 0, 2));
        tableBot.header("Duration").click();
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "99", 0, 2));
        tableBot.header("Start Time").click();
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "99", 0, 2));
    }

    /**
     * Test with an actual trace, this is more of an integration test than a
     * unit test. This test is a slow one too. If some analyses are not well
     * configured, this test will also generates null pointer exceptions. These
     * are will be logged.
     *
     * @throws IOException
     *             trace not found?
     */
    @Test
    public void testWithTrace() throws IOException {
        String tracePath = FileUtils.toFile(FileLocator.toFileURL(CtfTestTrace.ARM_64_BIT_HEADER.getTraceURL())).getAbsolutePath();
        SWTBotUtils.closeViewById(PRIMARY_VIEW_ID, fBot);
        SWTBotUtils.createProject(PROJECT_NAME);
        SWTBotUtils.openTrace(PROJECT_NAME, tracePath, TRACE_TYPE);
        WaitUtils.waitForJobs();
        AbstractSegmentStoreTableView tableView = openTable();
        setTableView(tableView);
        AbstractSegmentStoreTableViewer table = tableView.getSegmentStoreViewer();
        assertNotNull(table);
        setTable(table);
        WaitUtils.waitForJobs();
        SWTBotTable tableBot = new SWTBotTable(table.getTableViewer().getTable());
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "24,100", 0, 2));
        tableBot.header("Duration").click();
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "1,000", 0, 2));
        tableBot.header("Duration").click();
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "5,904,091,700", 0, 2));
        fBot.closeAllEditors();
        SWTBotUtils.deleteProject(PROJECT_NAME, fBot);
    }

    @Override
    protected void testTsv(String[] lines) {
        assertNotNull(lines);
        assertEquals("number of lines", 21, lines.length);
        assertEquals("header", "Start Time\tEnd Time\tDuration\tSystem Call\tTID\tReturn value\tComponent\tFile", lines[0]);
        // not a straight up string compare due to time zones. Kathmandu and
        // Eucla have 15 minute time zones.
        assertTrue("line 1 : " + lines[1], lines[1].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s001\\t\\d\\d:\\d\\d:00.000 000 002\\t1\\tleftpad\\t10\\t1\\t\\t"));
        assertTrue("line 2 : " + lines[2], lines[2].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s002\\t\\d\\d:\\d\\d:00.000 000 006\\t4\\tleftpad\\t10\\t1\\t\\t"));
        assertTrue("line 3 : " + lines[3], lines[3].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s003\\t\\d\\d:\\d\\d:00.000 000 012\\t9\\trightpad\\t10\\t1\\t\\t"));
        assertTrue("line 4 : " + lines[4], lines[4].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s004\\t\\d\\d:\\d\\d:00.000 000 020\\t16\\tleftpad\\t10\\t1\\t\\t"));
        assertTrue("line 5 : " + lines[5], lines[5].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s005\\t\\d\\d:\\d\\d:00.000 000 030\\t25\\tleftpad\\t10\\t1\\t\\t"));
        assertTrue("line 6 : " + lines[6], lines[6].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s006\\t\\d\\d:\\d\\d:00.000 000 042\\t36\\trightpad\\t10\\t1\\t\\t"));
        assertTrue("line 7 : " + lines[7], lines[7].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s007\\t\\d\\d:\\d\\d:00.000 000 056\\t49\\tleftpad\\t10\\t1\\t\\t"));
        assertTrue("line 8 : " + lines[8], lines[8].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s008\\t\\d\\d:\\d\\d:00.000 000 072\\t64\\tleftpad\\t10\\t1\\t\\t"));
        assertTrue("line 9 : " + lines[9], lines[9].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s009\\t\\d\\d:\\d\\d:00.000 000 090\\t81\\trightpad\\t10\\t1\\t\\t"));
        assertTrue("line 10 : " + lines[10], lines[10].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s010\\t\\d\\d:\\d\\d:00.000 000 110\\t100\\tleftpad\\t10\\t1\\t\\t"));
        assertTrue("line 11 : " + lines[11], lines[11].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s011\\t\\d\\d:\\d\\d:00.000 000 132\\t121\\tleftpad\\t10\\t1\\t\\t"));
        assertTrue("line 12 : " + lines[12], lines[12].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s012\\t\\d\\d:\\d\\d:00.000 000 156\\t144\\trightpad\\t10\\t1\\t\\t"));
        assertTrue("line 13 : " + lines[13], lines[13].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s013\\t\\d\\d:\\d\\d:00.000 000 182\\t169\\tleftpad\\t10\\t1\\t\\t"));
        assertTrue("line 14 : " + lines[14], lines[14].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s014\\t\\d\\d:\\d\\d:00.000 000 210\\t196\\tleftpad\\t10\\t1\\t\\t"));
        assertTrue("line 15 : " + lines[15], lines[15].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s015\\t\\d\\d:\\d\\d:00.000 000 240\\t225\\trightpad\\t10\\t1\\t\\t"));
        assertTrue("line 16 : " + lines[16], lines[16].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s016\\t\\d\\d:\\d\\d:00.000 000 272\\t256\\tleftpad\\t10\\t1\\t\\t"));
        assertTrue("line 17 : " + lines[17], lines[17].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s017\\t\\d\\d:\\d\\d:00.000 000 306\\t289\\tleftpad\\t10\\t1\\t\\t"));
        assertTrue("line 18 : " + lines[18], lines[18].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s018\\t\\d\\d:\\d\\d:00.000 000 342\\t324\\trightpad\\t10\\t1\\t\\t"));
        assertTrue("line 19 : " + lines[19], lines[19].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s019\\t\\d\\d:\\d\\d:00.000 000 380\\t361\\tleftpad\\t10\\t1\\t\\t"));
        assertTrue("line 20 : " + lines[20], lines[20].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s020\\t\\d\\d:\\d\\d:00.000 000 420\\t400\\tleftpad\\t10\\t1\\t\\t"));
    }
}
