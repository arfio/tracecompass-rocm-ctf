/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.tests.module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.AnalysisCompilationData;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlOutputEntryCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlXYViewCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlAnalysisModuleSource;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.DataDrivenOutputEntry;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.DataDrivenXYDataProvider;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.DataDrivenXYProviderFactory;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.XmlDataProviderManager;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestFiles;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ISeriesModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfXyModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfAnalysisModuleWithStateSystems;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Test the XML XY data provider
 *
 * @author Geneviève Bastien
 */
public class XmlXyDataProviderTest {

    private static final @NonNull String TEST_TRACE = "test_traces/testTrace4.xml";

    private static final @NonNull String ANALYSIS_ID = "xml.core.tests.simple.pattern";
    private static final @NonNull String XY_VIEW_ID = "xml.core.tests.simple.pattern.xy";
    private static final @NonNull String XY_VIEW_ID_DELTA = "xml.core.tests.simple.pattern.xy.delta";
    private static final @NonNull IProgressMonitor MONITOR = new NullProgressMonitor();

    /**
     * Load the XML files for the current test
     */
    @Before
    public void setUp() {
        XmlUtils.addXmlFile(TmfXmlTestFiles.VALID_PATTERN_SIMPLE_FILE.getFile());
        XmlUtils.addXmlFile(TmfXmlTestFiles.DATA_PROVIDER_SIMPLE_FILE.getFile());

        XmlAnalysisModuleSource.notifyModuleChange();
    }

    /**
     * Clean
     */
    public void cleanUp() {
        XmlUtils.deleteFiles(ImmutableList.of(
                TmfXmlTestFiles.VALID_PATTERN_SIMPLE_FILE.getFile().getName(),
                TmfXmlTestFiles.DATA_PROVIDER_SIMPLE_FILE.getFile().getName()));
        XmlAnalysisModuleSource.notifyModuleChange();
    }

    private ITmfTrace getTrace() {
        // Initialize the trace and module
        ITmfTrace trace = XmlUtilsTest.initializeTrace(TEST_TRACE);
        TmfTraceOpenedSignal signal = new TmfTraceOpenedSignal(this, trace, null);
        ((TmfTrace) trace).traceOpened(signal);
        // The data provider manager uses opened traces from the manager
        TmfTraceManager.getInstance().traceOpened(signal);
        return trace;
    }

    private static void runModule(ITmfTrace trace) {
        IAnalysisModule module = trace.getAnalysisModule(ANALYSIS_ID);
        assertNotNull(module);
        module.schedule();
        assertTrue(module.waitForCompletion());
    }

    /**
     * Test getting the XML data provider for one trace, with an analysis that
     * applies to a trace
     *
     * @throws IOException
     *             Exception thrown by analyses
     */
    @Test
    public void testXYDataProvider() throws IOException {
        ITmfTrace trace = getTrace();
        assertNotNull(trace);
        try {
            runModule(trace);
            // Get the view element from the file
            Element viewElement = TmfXmlUtils.getElementInFile(TmfXmlTestFiles.DATA_PROVIDER_SIMPLE_FILE.getPath().toOSString(), TmfXmlStrings.XY_VIEW, XY_VIEW_ID);
            assertNotNull(viewElement);
            ITmfTreeXYDataProvider<@NonNull ITmfTreeDataModel> xyProvider = XmlDataProviderManager.getInstance().getXyProvider(trace, viewElement);
            assertNotNull(xyProvider);

            List<String> expectedStrings = Files.readAllLines(Paths.get("test_traces/simple_dataprovider/expectedXYTree"));
            Map<Long, String> tree = assertAndGetTree(xyProvider, trace, expectedStrings);

            expectedStrings = Files.readAllLines(Paths.get("test_traces/simple_dataprovider/expectedXYData"));
            assertRows(xyProvider, tree, expectedStrings);

        } finally {
            trace.dispose();
            TmfTraceManager.getInstance().traceClosed(new TmfTraceClosedSignal(this, trace));
        }

    }

    /**
     * Test getting the XML XY data provider for one trace, with an analysis that
     * applies to a trace, and requesting relative values (delta) for the y axis.
     *
     * @throws IOException
     *             Exception thrown by analyses
     */
    @Test
    public void testXYDataProviderDelta() throws IOException {
        ITmfTrace trace = getTrace();
        assertNotNull(trace);
        try {
            runModule(trace);
            // Get the view element from the file
            Element viewElement = TmfXmlUtils.getElementInFile(TmfXmlTestFiles.DATA_PROVIDER_SIMPLE_FILE.getPath().toOSString(), TmfXmlStrings.XY_VIEW, XY_VIEW_ID_DELTA);
            assertNotNull(viewElement);
            ITmfTreeXYDataProvider<@NonNull ITmfTreeDataModel> xyProvider = XmlDataProviderManager.getInstance().getXyProvider(trace, viewElement);
            assertNotNull(xyProvider);

            List<String> expectedStrings = Files.readAllLines(Paths.get("test_traces/simple_dataprovider/expectedXYTree"));
            Map<Long, String> tree = assertAndGetTree(xyProvider, trace, expectedStrings);

            expectedStrings = Files.readAllLines(Paths.get("test_traces/simple_dataprovider/expectedXYDataDelta"));
            assertRows(xyProvider, tree, expectedStrings);

        } finally {
            trace.dispose();
            TmfTraceManager.getInstance().traceClosed(new TmfTraceClosedSignal(this, trace));
        }

    }

    /**
     * Test the {@link DataDrivenXYProviderFactory} class
     */
    @Test
    public void testXYFactory() {
        ITmfTrace trace = getTrace();
        assertNotNull(trace);
        try {
            runModule(trace);
            // Get the view element from the file
            Element viewElement = TmfXmlUtils.getElementInFile(TmfXmlTestFiles.DATA_PROVIDER_SIMPLE_FILE.getPath().toOSString(), TmfXmlStrings.XY_VIEW, XY_VIEW_ID_DELTA);
            assertNotNull(viewElement);

            TmfXmlXYViewCu tgViewCu = TmfXmlXYViewCu.compile(new AnalysisCompilationData(), viewElement);
            assertNotNull(tgViewCu);
            DataDrivenXYProviderFactory XYFactory = tgViewCu.generate();

            // Test the factory with a simple trace
            ITmfTreeXYDataProvider<@NonNull ITmfTreeDataModel> provider = XYFactory.create(trace);
            assertNotNull(provider);
            assertEquals(DataDrivenXYDataProvider.ID, provider.getId());

            // Test the factory with an ID and state system
            ITmfAnalysisModuleWithStateSystems module = TmfTraceUtils.getAnalysisModuleOfClass(trace, ITmfAnalysisModuleWithStateSystems.class, ANALYSIS_ID);
            assertNotNull(module);
            Iterable<@NonNull ITmfStateSystem> stateSystems = module.getStateSystems();
            assertNotNull(stateSystems);


            provider = DataDrivenXYProviderFactory.create(trace, Objects.requireNonNull(Lists.newArrayList(stateSystems)), getEntries(new AnalysisCompilationData(), viewElement), ANALYSIS_ID);
            assertNotNull(provider);
            assertEquals(ANALYSIS_ID, provider.getId());

        } finally {
            trace.dispose();
            TmfTraceManager.getInstance().traceClosed(new TmfTraceClosedSignal(this, trace));
        }

    }

    private static List<DataDrivenOutputEntry> getEntries(AnalysisCompilationData compilationData, Element viewElement) {
        List<Element> entries = TmfXmlUtils.getChildElements(viewElement, TmfXmlStrings.ENTRY_ELEMENT);

        List<TmfXmlOutputEntryCu> entriesCu = new ArrayList<>();
        for (Element entry : entries) {
            TmfXmlOutputEntryCu entryCu = TmfXmlOutputEntryCu.compile(compilationData, entry);
            if (entryCu != null) {
                entriesCu.add(entryCu);
            }
        }

        return entriesCu.stream()
                .map(TmfXmlOutputEntryCu::generate)
                .collect(Collectors.toList());
    }

    private static void assertRows(ITmfTreeXYDataProvider<@NonNull ITmfTreeDataModel> xyProvider, Map<Long, String> tree, List<String> expectedStrings) {
        TmfModelResponse<@NonNull ITmfXyModel> rowResponse = xyProvider.fetchXY(FetchParametersUtils.selectionTimeQueryToMap(new SelectionTimeQueryFilter(1, 20, 20, tree.keySet())), null);
        assertNotNull(rowResponse);
        assertEquals(ITmfResponse.Status.COMPLETED, rowResponse.getStatus());
        ITmfXyModel rowModel = rowResponse.getModel();
        assertNotNull(rowModel);
        Collection<@NonNull ISeriesModel> series = rowModel.getSeriesData();
        ImmutableMap<Long, @NonNull ISeriesModel> data = Maps.uniqueIndex(series, ISeriesModel::getId);

        for (int i = 0; i < expectedStrings.size(); i++) {
            String expectedString = expectedStrings.get(i);
            String[] split = expectedString.split(":");
            String rowName = split[0];
            Long rowId = null;
            for (Entry<Long, String> entry : tree.entrySet()) {
                if (entry.getValue().equals(rowName)) {
                    rowId = entry.getKey();
                    break;
                }
            }
            assertNotNull(rowId);
            ISeriesModel row = data.get(rowId);
            assertNotNull(row);

            String[] expectedData = split[1].split(",");
            double[] actualData = row.getData();
            for (int j = 0; j < expectedData.length; j++) {
                assertTrue("Presence of data at position " + j + " for row " + rowName, actualData.length > j);
                double expectedValue = Double.parseDouble(expectedData[j]);
                assertEquals("Data at position " + j + " for row " + rowName, expectedValue, actualData[j], 0.001);
            }

        }
        assertEquals("Same number of data", expectedStrings.size(), data.size());

    }

    private static Map<Long, String> assertAndGetTree(ITmfTreeXYDataProvider<@NonNull ITmfTreeDataModel> xyProvider, ITmfTrace trace, List<String> expectedStrings) {
        TmfModelResponse<@NonNull TmfTreeModel<@NonNull ITmfTreeDataModel>> treeResponse = xyProvider.fetchTree(FetchParametersUtils.timeQueryToMap(new TimeQueryFilter(0, Long.MAX_VALUE, 2)), MONITOR);
        assertNotNull(treeResponse);
        assertEquals(ITmfResponse.Status.COMPLETED, treeResponse.getStatus());
        TmfTreeModel<@NonNull ITmfTreeDataModel> treeModel = treeResponse.getModel();
        assertNotNull(treeModel);
        List<@NonNull ITmfTreeDataModel> treeEntries = treeModel.getEntries();

        Map<Long, String> map = new HashMap<>();
        for (int i = 0; i < expectedStrings.size(); i++) {
            String expectedString = expectedStrings.get(i);
            assertTrue("actual entry absent at " + i + ": " + expectedString, treeEntries.size() > i);
            String[] split = expectedString.split(",");
            ITmfTreeDataModel xmlXyEntry = treeEntries.get(i);

            assertEquals("Checking entry name at " + i, split[0], xmlXyEntry.getName());
            // Check the parent
            long parentId = xmlXyEntry.getParentId();
            if (parentId < 0) {
                assertEquals("Checking empty parent at " + i, split[1], "null");
            } else {
                String parentName = map.get(parentId);
                assertEquals("Checking parent at " + i, split[1], parentName);
            }
            map.put(xmlXyEntry.getId(), xmlXyEntry.getName());
        }
        assertEquals("Extra actual entries", expectedStrings.size(), treeEntries.size());

        return map;
    }

}
