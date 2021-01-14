/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.internal.tmf.core.model.xy.AbstractTreeCommonXDataProvider;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataType;
import org.eclipse.tracecompass.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.tmf.core.model.IOutputStyleProvider;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.OutputStyleModel;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;
import org.eclipse.tracecompass.tmf.core.model.YModel;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.tmf.core.model.xy.TmfXYAxisDescription;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.core.util.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

/**
 * This data provider will return a XY model (model is wrapped in a response)
 * based on a query filter. The model is used afterwards by any viewer to draw
 * charts. Model returned is for Disks I/O views
 *
 * @author Yonni Chen
 */
public class DisksIODataProvider extends AbstractTreeCommonXDataProvider<InputOutputAnalysisModule, TmfTreeDataModel>
        implements IOutputStyleProvider {

    /**
     * Extension point ID.
     */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.DisksIODataProvider"; //$NON-NLS-1$

    /**
     * Title used to create XY models for the {@link DisksIODataProvider}.
     */
    protected static final String PROVIDER_TITLE = Objects.requireNonNull(Messages.DisksIODataProvider_title);

    private static final String BASE_STYLE = "base"; //$NON-NLS-1$
    private static final Map<String, OutputElementStyle> STATE_MAP;
    private static final String BINARY_SPEED_UNIT = "B/s"; //$NON-NLS-1$
    private static final List<Pair<String, String>> COLOR_LIST = IODataPalette.getColors();
    private static final TmfXYAxisDescription Y_AXIS_DESCRIPTION = new TmfXYAxisDescription(Objects.requireNonNull(Messages.DiskIODataProvider_YAxis), BINARY_SPEED_UNIT, DataType.BINARY_NUMBER);
    private static final List<String> SUPPORTED_STYLES = ImmutableList.of(
            StyleProperties.SeriesStyle.SOLID,
            StyleProperties.SeriesStyle.DASH,
            StyleProperties.SeriesStyle.DOT,
            StyleProperties.SeriesStyle.DASHDOT,
            StyleProperties.SeriesStyle.DASHDOTDOT);

    static {
        // Create the base style
        ImmutableMap.Builder<@NonNull String, @NonNull OutputElementStyle> builder = new ImmutableMap.Builder<>();
        builder.put(BASE_STYLE, new OutputElementStyle(null, ImmutableMap.of(StyleProperties.SERIES_TYPE, StyleProperties.SeriesType.AREA, StyleProperties.WIDTH, 1.0f)));
        STATE_MAP = builder.build();
    }

    /**
     * Inline class to encapsulate all the values required to build a series.
     * Allows for reuse of full query results to be faster than {@link Disk}.
     */
    private static final class DiskBuilder {

        private static final int BYTES_PER_SECTOR = 512;
        private static final double SECONDS_PER_NANOSECOND = 1E-9;
        private static final double RATIO = BYTES_PER_SECTOR / SECONDS_PER_NANOSECOND;

        private final long fId;
        /** This series' sector quark. public because final */
        public final int fSectorQuark;
        private final String fName;
        private final double[] fValues;
        private double fPrevCount;

        /**
         * Constructor
         *
         * @param name
         *            the series name
         * @param sectorQuark
         *            sector quark
         * @param length
         *            desired length of the series
         */
        private DiskBuilder(long id, int sectorQuark, String name, int length) {
            fId = id;
            fSectorQuark = sectorQuark;
            fName = name;
            fValues = new double[length];
        }

        private void setPrevCount(double prevCount) {
            fPrevCount = prevCount;
        }

        /**
         * Update the value for the counter at the desired index. Use in
         * increasing order of position
         *
         * @param pos
         *            index to update
         * @param newCount
         *            new number of read / written sectors
         * @param deltaT
         *            time difference to the previous value for interpolation
         */
        private void updateValue(int pos, double newCount, long deltaT) {
            /**
             * Linear interpolation to compute the disk throughput between time
             * and the previous time, from the number of sectors at each time.
             */
            fValues[pos] = (newCount - fPrevCount) * RATIO / deltaT;
            fPrevCount = newCount;
        }

        private IYModel build() {
            return new YModel(fId, fName, fValues, Y_AXIS_DESCRIPTION);
        }
    }

    /**
     * Create an instance of {@link DisksIODataProvider}. Returns a null
     * instance if the analysis module is not found.
     *
     * @param trace
     *            A trace on which we are interested to fetch a model
     * @return A {@link DisksIODataProvider} instance. If analysis module is not
     *         found, it returns null
     */
    public static @Nullable DisksIODataProvider create(ITmfTrace trace) {
        InputOutputAnalysisModule module = TmfTraceUtils.getAnalysisModuleOfClass(trace, InputOutputAnalysisModule.class, InputOutputAnalysisModule.ID);
        if (module != null) {
            module.schedule();
            return new DisksIODataProvider(trace, module);
        }
        return null;
    }

    /**
     * Constructor
     */
    private DisksIODataProvider(ITmfTrace trace, InputOutputAnalysisModule module) {
        super(trace, module);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    protected TmfTreeModel<TmfTreeDataModel> getTree(ITmfStateSystem ss, Map<String, Object> parameters, @Nullable IProgressMonitor monitor) {
        List<TmfTreeDataModel> nodes = new ArrayList<>();
        long rootId = getId(ITmfStateSystem.ROOT_ATTRIBUTE);
        nodes.add(new TmfTreeDataModel(rootId, -1, Collections.singletonList(getTrace().getName()), false, null));

        String readName = Objects.requireNonNull(Messages.DisksIODataProvider_read);
        String writeName = Objects.requireNonNull(Messages.DisksIODataProvider_write);

        int i = 0;
        for (Integer diskQuark : ss.getQuarks(Attributes.DISKS, "*")) { //$NON-NLS-1$
            String diskName = DiskUtils.getDiskName(ss, diskQuark);
            long diskId = getId(diskQuark);
            nodes.add(new TmfTreeDataModel(diskId, rootId, Collections.singletonList(diskName), false, null));

            // Do not add the read/write entries if there was no read/write
            int readQuark = ss.optQuarkRelative(diskQuark, Attributes.SECTORS_READ);
            int writeQuark = ss.optQuarkRelative(diskQuark, Attributes.SECTORS_WRITTEN);
            if (readQuark == ITmfStateSystem.INVALID_ATTRIBUTE && writeQuark == ITmfStateSystem.INVALID_ATTRIBUTE) {
                continue;
            }

            // Get read and write color for this disk
            Pair<String, String> pair = COLOR_LIST.get(i % COLOR_LIST.size());
            String seriesStyle = SUPPORTED_STYLES.get((i / COLOR_LIST.size()) % SUPPORTED_STYLES.size());

            if (readQuark != ITmfStateSystem.INVALID_ATTRIBUTE) {
                nodes.add(new TmfTreeDataModel(getId(readQuark), diskId, Collections.singletonList(readName), true,
                        new OutputElementStyle(BASE_STYLE, ImmutableMap.of(
                                StyleProperties.COLOR, pair.getFirst(),
                                StyleProperties.SERIES_STYLE, seriesStyle,
                                StyleProperties.STYLE_NAME, diskName + '/' + readName))));
            }

            if (writeQuark != ITmfStateSystem.INVALID_ATTRIBUTE) {
                nodes.add(new TmfTreeDataModel(getId(writeQuark), diskId, Collections.singletonList(writeName), true,
                        new OutputElementStyle(BASE_STYLE, ImmutableMap.of(
                        StyleProperties.COLOR, pair.getSecond(),
                        StyleProperties.SERIES_STYLE, seriesStyle,
                        StyleProperties.STYLE_NAME, diskName + '/' + writeName))));
            }
            i++;
        }
        return new TmfTreeModel<>(Collections.emptyList(), nodes);
    }

    @Deprecated
    @Override
    protected @Nullable Map<String, IYModel> getYModels(ITmfStateSystem ss, Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) throws StateSystemDisposedException {
        return Maps.uniqueIndex(getYSeriesModels(ss, fetchParameters, monitor), IYModel::getName);
    }

    @Override
    protected @Nullable Collection<IYModel> getYSeriesModels(ITmfStateSystem ss, Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) throws StateSystemDisposedException {
        SelectionTimeQueryFilter filter = FetchParametersUtils.createSelectionTimeQuery(fetchParameters);
        if (filter == null) {
            return null;
        }
        long[] xValues = filter.getTimesRequested();
        List<DiskBuilder> builders = initBuilders(ss, filter);
        if (builders.isEmpty()) {
            // this would return an empty map even if we did the queries.
            return Collections.emptyList();
        }

        long currentEnd = ss.getCurrentEndTime();
        long prevTime = filter.getStart();
        if (prevTime >= ss.getStartTime() && prevTime <= currentEnd) {
            // reuse the results from the full query
            List<ITmfStateInterval> states = ss.queryFullState(prevTime);

            for (DiskBuilder entry : builders) {
                entry.setPrevCount(Disk.extractCount(entry.fSectorQuark, ss, states, prevTime));
            }
        }

        for (int i = 1; i < xValues.length; i++) {
            if (monitor != null && monitor.isCanceled()) {
                return null;
            }
            long time = xValues[i];
            if (time > currentEnd) {
                break;
            } else if (time >= ss.getStartTime()) {
                // reuse the results from the full query
                List<ITmfStateInterval> states = ss.queryFullState(time);

                for (DiskBuilder entry : builders) {
                    double count = Disk.extractCount(entry.fSectorQuark, ss, states, time);
                    entry.updateValue(i, count, time - prevTime);
                }
            }
            prevTime = time;
        }
        return ImmutableList.copyOf(Iterables.transform(builders, DiskBuilder::build));
    }

    private List<DiskBuilder> initBuilders(ITmfStateSystem ss, SelectionTimeQueryFilter filter) {
        int length = filter.getTimesRequested().length;
        List<DiskBuilder> builders = new ArrayList<>();
        for (Entry<Long, Integer> entry : getSelectedEntries(filter).entrySet()) {
            long id = entry.getKey();
            int quark = entry.getValue();

            if (ss.getAttributeName(quark).equals(Attributes.SECTORS_READ)) {
                String name = getTrace().getName() + '/' + DiskUtils.getDiskName(ss, ss.getParentAttributeQuark(quark)) + "/read"; //$NON-NLS-1$
                builders.add(new DiskBuilder(id, quark, name, length));
            } else if (ss.getAttributeName(quark).equals(Attributes.SECTORS_WRITTEN)) {
                String name = getTrace().getName() + '/' + DiskUtils.getDiskName(ss, ss.getParentAttributeQuark(quark)) + "/write"; //$NON-NLS-1$
                builders.add(new DiskBuilder(id, quark, name, length));
            }
        }
        return builders;
    }

    @Override
    protected boolean isCacheable() {
        return true;
    }

    @Override
    protected String getTitle() {
        return PROVIDER_TITLE;
    }

    @Override
    public TmfModelResponse<OutputStyleModel> fetchStyle(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        return new TmfModelResponse<>(new OutputStyleModel(STATE_MAP), ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }
}
