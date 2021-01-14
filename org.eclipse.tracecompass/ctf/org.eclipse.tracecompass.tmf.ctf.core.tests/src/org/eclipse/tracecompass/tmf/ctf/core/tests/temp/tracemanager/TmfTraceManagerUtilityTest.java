/*******************************************************************************
 * Copyright (c) 2016, 2019 EfficiOS Inc., Alexandre Montplaisir and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests.temp.tracemanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.runtime.URIUtil;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the utility methods in {@link TmfTraceManager}.
 *
 * @author Alexandre Montplaisir
 */
public class TmfTraceManagerUtilityTest {

    private ITmfTrace fTrace;

    private static final String TEMP_DIR_NAME = ".tracecompass-temp";

    /**
     * Test initialization
     */
    @Before
    public void setup() {
        fTrace = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.TRACE2);
        fTrace.indexTrace(true);
    }

    /**
     * Test clean-up
     */
    @AfterClass
    public static void teardown() {
        CtfTmfTestTraceUtils.dispose(CtfTestTrace.TRACE2);
    }

    /**
     * Test the {@link TmfTraceManager#getTemporaryDirPath} method.
     * @throws URISyntaxException
     *              in case of URI syntax error
     */
    @Test
    public void testTemporaryDirPath() throws URISyntaxException {
        String tempDirPath = TmfTraceManager.getTemporaryDirPath();
        assertTrue(tempDirPath.endsWith(TEMP_DIR_NAME));

        String property = System.getProperty("osgi.instance.area"); //$NON-NLS-1$
        File dir = URIUtil.toFile(URIUtil.fromString(property));
        String basePath = dir.getAbsolutePath();
        assertTrue(tempDirPath.startsWith(basePath));
    }

    /**
     * Test the {@link TmfTraceManager#getSupplementaryFileDir} method.
     */
    @Test
    public void testSupplementaryFileDir() {
        final ITmfTrace trace = fTrace;
        assertNotNull(trace);
        String tracePath = trace.getPath();
        String tempDirPath = TmfTraceManager.getTemporaryDirPath();

        String expected = Paths.get(tempDirPath, tracePath).toString() + File.separator;
        assertEquals(expected, TmfTraceManager.getSupplementaryFileDir(trace));
    }

    /**
     * Test the {@link TmfTraceManager#deleteSupplementaryFiles} method.
     */
    @Test
    public void testDeleteSupplementaryFiles() {
        ITmfTrace trace = fTrace;
        assertNotNull(trace);

        String suppFilesPath = TmfTraceManager.getSupplementaryFileDir(trace);
        try {
            /*
             * Initializing/indexing the trace should have produced some
             * supplementary files already.
             */
            assertFalse(isDirectoryEmpty(suppFilesPath));
            trace.dispose();
            TmfTraceManager.deleteSupplementaryFiles(trace);
            assertTrue(isDirectoryEmpty(suppFilesPath));

        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    private static boolean isDirectoryEmpty(String dirPath) throws IOException {
        Path path = Paths.get(dirPath);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            return !stream.iterator().hasNext();
        }
    }

    /**
     * Test the {@link TmfTraceManager#deleteSupplementaryFolder(ITmfTrace)} method.
     *
     * @throws IOException
     *             if IOException happens
     */
    @Test
    public void testDeleteSupplementaryFolder() throws IOException {
        ITmfTrace trace = fTrace;
        assertNotNull(trace);
        String suppFilesPath = TmfTraceManager.getSupplementaryFileDir(trace);
        /*
         * Initializing/indexing the trace should have produced some
         * supplementary files already.
         */
        assertFalse(isDirectoryEmpty(suppFilesPath));
        trace.dispose();
        TmfTraceManager.deleteSupplementaryFolder(trace);
        File parent = new File(suppFilesPath);
        assertFalse(parent.exists());
    }
}
