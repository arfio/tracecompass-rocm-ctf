/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.callstack.core.tests.flamechart.statesystem;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.tracecompass.incubator.callstack.core.base.ICallStackGroupDescriptor;
import org.eclipse.tracecompass.incubator.callstack.core.instrumented.statesystem.CallStackSeries;
import org.eclipse.tracecompass.incubator.callstack.core.instrumented.statesystem.InstrumentedCallStackAnalysis;
import org.eclipse.tracecompass.incubator.callstack.core.tests.flamechart.CallStackTestBase;
import org.eclipse.tracecompass.incubator.callstack.core.tests.stubs.CallStackAnalysisStub;
import org.eclipse.tracecompass.incubator.internal.callstack.core.instrumented.InstrumentedGroupDescriptor;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.statesystem.core.tests.shared.utils.StateIntervalStub;
import org.eclipse.tracecompass.statesystem.core.tests.shared.utils.StateSystemTestUtils;
import org.junit.Test;

/**
 * Test the call stack provider and callstack analysis for state system
 * callstacks. This class specifically tests the content of the state system
 * built from the {@link InstrumentedCallStackAnalysis} and the
 * {@link CallStackStateProvider}
 *
 * @author Geneviève Bastien
 */
public class CallStackStateSystemTest extends CallStackTestBase {

    /**
     * Test that the module as a callstack provider produces the same results as
     * the callstack analysis module under it
     */
    @Test
    public void testCallStackProvider() {
        CallStackAnalysisStub module = getModule();
        assertNotNull(module);

        // There should be only 1 callstack series
        CallStackSeries callstack = module.getCallStackSeries();
        assertNotNull(callstack);

        // Get the patterns for each level of the callstack
        List<String[]> patterns = module.getPatterns();
        assertEquals(2, patterns.size());

        // Check each level of the callstack. Make sure the path in the series
        // correspond to the expected path
        ICallStackGroupDescriptor nextLevel = callstack.getRootGroup();
        assertTrue(nextLevel instanceof InstrumentedGroupDescriptor);
        String[] subPattern = ((InstrumentedGroupDescriptor) nextLevel).getSubPattern();
        assertArrayEquals(patterns.get(0), subPattern);

        nextLevel = nextLevel.getNextGroup();
        assertTrue(nextLevel instanceof InstrumentedGroupDescriptor);
        subPattern = ((InstrumentedGroupDescriptor) nextLevel).getSubPattern();
        assertArrayEquals(patterns.get(1), subPattern);

        nextLevel = nextLevel.getNextGroup();
        assertNull(nextLevel);
    }

    /**
     * Test the content of the callstack state system built from the state
     * provider
     *
     * @throws AttributeNotFoundException
     *             Exception thrown by test
     */
    @Test
    public void testCallStackContent() throws AttributeNotFoundException {
        // The javadoc of getModule() contains the expected structure of the
        // callstack
        CallStackAnalysisStub module = getModule();
        assertNotNull(module);

        // There should be 1 callstack series
        CallStackSeries callstack = module.getCallStackSeries();
        assertNotNull(callstack);

        ITmfStateSystem ss = module.getStateSystem();
        assertNotNull(ss);

        List<String[]> patterns = module.getPatterns();

        // Get the process quarks (first level)
        List<Integer> quarks = ss.getQuarks(patterns.get(0));
        assertEquals("Number of processes", 2, quarks.size());

        for (Integer processQuark : quarks) {
            // Get the threads under this process (second level)
            List<Integer> threadQuarks = ss.getQuarks(processQuark, patterns.get(1));
            switch (ss.getAttributeName(processQuark)) {
            case "1":
                verifyProcess1(ss, callstack, threadQuarks);
                break;
            case "5":
                verifyProcess5(ss, callstack, threadQuarks);
                break;
            default:
                fail("Unknown process in callstack");
            }
        }
    }

    private static void verifyProcess1(ITmfStateSystem ss, CallStackSeries callstack, List<Integer> threadQuarks) throws AttributeNotFoundException {
        for (Integer threadQuark : threadQuarks) {
            int csQuark = ss.getQuarkRelative(threadQuark, InstrumentedCallStackAnalysis.CALL_STACK);
            String[] csPathArray = ss.getFullAttributePathArray(csQuark);
            List<String> pathList = new ArrayList<>();
            pathList.addAll(Arrays.asList(csPathArray));
            switch (ss.getAttributeName(threadQuark)) {
            case "2": {
                List<Integer> subAttributes = ss.getSubAttributes(csQuark, false);
                assertEquals(3, subAttributes.size());
                List<ITmfStateInterval> expected = new ArrayList<>();
                // Check the first depth level of thread 2
                pathList.add("1");
                expected.add(new StateIntervalStub(1, 9, TmfStateValue.newValueString("op1")));
                expected.add(new StateIntervalStub(10, 11, TmfStateValue.nullValue()));
                expected.add(new StateIntervalStub(12, 19, TmfStateValue.newValueString("op4")));
                expected.add(new StateIntervalStub(20, 20, TmfStateValue.nullValue()));
                StateSystemTestUtils.testIntervalForAttributes(ss, expected, pathList.toArray(new String[pathList.size()]));

                // Check the second depth level
                pathList.remove(pathList.size() - 1);
                pathList.add("2");
                expected.clear();
                expected.add(new StateIntervalStub(1, 2, TmfStateValue.nullValue()));
                expected.add(new StateIntervalStub(3, 6, TmfStateValue.newValueString("op2")));
                expected.add(new StateIntervalStub(7, 20, TmfStateValue.nullValue()));
                StateSystemTestUtils.testIntervalForAttributes(ss, expected, pathList.toArray(new String[pathList.size()]));

                // Check the third depth level
                pathList.remove(pathList.size() - 1);
                pathList.add("3");
                expected.clear();
                expected.add(new StateIntervalStub(1, 3, TmfStateValue.nullValue()));
                expected.add(new StateIntervalStub(4, 4, TmfStateValue.newValueString("op3")));
                expected.add(new StateIntervalStub(5, 20, TmfStateValue.nullValue()));
                StateSystemTestUtils.testIntervalForAttributes(ss, expected, pathList.toArray(new String[pathList.size()]));

            }
                break;
            case "3": {
                List<Integer> subAttributes = ss.getSubAttributes(csQuark, false);
                assertEquals(2, subAttributes.size());
                List<ITmfStateInterval> expected = new ArrayList<>();
                // Check the first depth level of thread 3
                pathList.add("1");
                expected.add(new StateIntervalStub(1, 2, TmfStateValue.nullValue()));
                expected.add(new StateIntervalStub(3, 19, TmfStateValue.newValueString("op2")));
                expected.add(new StateIntervalStub(20, 20, TmfStateValue.nullValue()));
                StateSystemTestUtils.testIntervalForAttributes(ss, expected, pathList.toArray(new String[pathList.size()]));

                // Check the second depth level
                pathList.remove(pathList.size() - 1);
                pathList.add("2");
                expected.clear();
                expected.add(new StateIntervalStub(1, 4, TmfStateValue.nullValue()));
                expected.add(new StateIntervalStub(5, 5, TmfStateValue.newValueString("op3")));
                expected.add(new StateIntervalStub(6, 6, TmfStateValue.nullValue()));
                expected.add(new StateIntervalStub(7, 12, TmfStateValue.newValueString("op2")));
                expected.add(new StateIntervalStub(13, 20, TmfStateValue.nullValue()));
                StateSystemTestUtils.testIntervalForAttributes(ss, expected, pathList.toArray(new String[pathList.size()]));
            }
                break;
            default:
                fail("Unknown thread child of process 5");
            }
        }
    }

    private static void verifyProcess5(ITmfStateSystem ss, CallStackSeries callstack, List<Integer> threadQuarks) throws AttributeNotFoundException {
        for (Integer threadQuark : threadQuarks) {
            int csQuark = ss.getQuarkRelative(threadQuark, InstrumentedCallStackAnalysis.CALL_STACK);
            String[] csPathArray = ss.getFullAttributePathArray(csQuark);
            List<String> pathList = new ArrayList<>();
            pathList.addAll(Arrays.asList(csPathArray));
            switch (ss.getAttributeName(threadQuark)) {
            case "6": {
                List<Integer> subAttributes = ss.getSubAttributes(csQuark, false);
                assertEquals(3, subAttributes.size());
                List<ITmfStateInterval> expected = new ArrayList<>();
                // Check the first depth level of thread 6
                pathList.add("1");
                expected.add(new StateIntervalStub(1, 19, TmfStateValue.newValueString("op1")));
                expected.add(new StateIntervalStub(20, 20, TmfStateValue.nullValue()));
                StateSystemTestUtils.testIntervalForAttributes(ss, expected, pathList.toArray(new String[pathList.size()]));

                // Check the second depth level
                pathList.remove(pathList.size() - 1);
                pathList.add("2");
                expected.clear();
                expected.add(new StateIntervalStub(1, 1, TmfStateValue.nullValue()));
                expected.add(new StateIntervalStub(2, 6, TmfStateValue.newValueString("op3")));
                expected.add(new StateIntervalStub(7, 7, TmfStateValue.nullValue()));
                expected.add(new StateIntervalStub(8, 10, TmfStateValue.newValueString("op2")));
                expected.add(new StateIntervalStub(11, 11, TmfStateValue.nullValue()));
                expected.add(new StateIntervalStub(12, 19, TmfStateValue.newValueString("op4")));
                expected.add(new StateIntervalStub(20, 20, TmfStateValue.nullValue()));
                StateSystemTestUtils.testIntervalForAttributes(ss, expected, pathList.toArray(new String[pathList.size()]));

                // Check the third depth level
                pathList.remove(pathList.size() - 1);
                pathList.add("3");
                expected.clear();
                expected.add(new StateIntervalStub(1, 3, TmfStateValue.nullValue()));
                expected.add(new StateIntervalStub(4, 5, TmfStateValue.newValueString("op1")));
                expected.add(new StateIntervalStub(6, 8, TmfStateValue.nullValue()));
                expected.add(new StateIntervalStub(9, 9, TmfStateValue.newValueString("op3")));
                expected.add(new StateIntervalStub(10, 20, TmfStateValue.nullValue()));
                StateSystemTestUtils.testIntervalForAttributes(ss, expected, pathList.toArray(new String[pathList.size()]));
            }
                break;
            case "7": {
                List<Integer> subAttributes = ss.getSubAttributes(csQuark, false);
                assertEquals(3, subAttributes.size());
                List<ITmfStateInterval> expected = new ArrayList<>();
                // Check the first depth level of thread 7
                pathList.add("1");
                expected.add(new StateIntervalStub(1, 19, TmfStateValue.newValueString("op5")));
                expected.add(new StateIntervalStub(20, 20, TmfStateValue.nullValue()));
                StateSystemTestUtils.testIntervalForAttributes(ss, expected, pathList.toArray(new String[pathList.size()]));

                // Check the second depth level
                pathList.remove(pathList.size() - 1);
                pathList.add("2");
                expected.clear();
                expected.add(new StateIntervalStub(1, 1, TmfStateValue.nullValue()));
                expected.add(new StateIntervalStub(2, 5, TmfStateValue.newValueString("op2")));
                expected.add(new StateIntervalStub(6, 8, TmfStateValue.nullValue()));
                expected.add(new StateIntervalStub(9, 12, TmfStateValue.newValueString("op2")));
                expected.add(new StateIntervalStub(13, 14, TmfStateValue.nullValue()));
                expected.add(new StateIntervalStub(15, 18, TmfStateValue.newValueString("op2")));
                expected.add(new StateIntervalStub(19, 20, TmfStateValue.nullValue()));
                StateSystemTestUtils.testIntervalForAttributes(ss, expected, pathList.toArray(new String[pathList.size()]));

                // Check the third depth level
                pathList.remove(pathList.size() - 1);
                pathList.add("3");
                expected.clear();
                expected.add(new StateIntervalStub(1, 9, TmfStateValue.nullValue()));
                expected.add(new StateIntervalStub(10, 10, TmfStateValue.newValueString("op3")));
                expected.add(new StateIntervalStub(11, 20, TmfStateValue.nullValue()));
                StateSystemTestUtils.testIntervalForAttributes(ss, expected, pathList.toArray(new String[pathList.size()]));
            }
                break;
            default:
                fail("Unknown thread child of process 5");
            }
        }
    }
}
