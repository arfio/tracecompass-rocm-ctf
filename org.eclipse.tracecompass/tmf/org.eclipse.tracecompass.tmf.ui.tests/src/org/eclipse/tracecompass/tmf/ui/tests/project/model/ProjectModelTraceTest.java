/*******************************************************************************
 * Copyright (c) 2013, 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.tests.project.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfOpenTraceHelper;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.tests.shared.ProjectModelTestData;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitTimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for the TmfTraceElement class.
 *
 * @author Geneviève Bastien
 */
public class ProjectModelTraceTest {

    private TmfProjectElement fixture;

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        try {
            fixture = ProjectModelTestData.getFilledProject();
        } catch (CoreException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Cleans up the project after tests have been executed
     */
    @After
    public void cleanUp() {
        ProjectModelTestData.deleteProject(fixture);
    }

    /**
     * Test the getTrace() and trace opening
     */
    @Test
    public void testOpenTrace() {
        assertNotNull(fixture);

        final TmfTraceFolder tracesFolder = fixture.getTracesFolder();
        assertNotNull(tracesFolder);

        final TmfTraceElement traceElement = tracesFolder.getTraces().get(0);

        /*
         * Get the trace from the element, it is not opened yet, should be null
         */
        ITmfTrace trace = traceElement.getTrace();
        assertNull(trace);

        TmfOpenTraceHelper.openFromElement(traceElement);

        /* Give the trace a chance to open */
        try {
            ProjectModelTestData.delayUntilTraceOpened(traceElement);
        } catch (WaitTimeoutException e) {
            fail("The trace did not open in a reasonable delay");
        }

        trace = traceElement.getTrace();
        assertNotNull(trace);

        /*
         * Open the trace from project, then get from element, both should be
         * the exact same element as the active trace
         */
        TmfOpenTraceHelper.openFromElement(traceElement);
        try {
            ProjectModelTestData.delayUntilTraceOpened(traceElement);
        } catch (WaitTimeoutException e) {
            fail("The trace did not open in a reasonable delay");
        }

        ITmfTrace trace2 = TmfTraceManager.getInstance().getActiveTrace();

        /* The trace was reopened, it should be the same as before */
        assertTrue(trace2 == trace);

        /* Here, the getTrace() should return the same as active trace */
        trace = traceElement.getTrace();
        assertTrue(trace2 == trace);

        traceElement.closeEditors();
    }

}
