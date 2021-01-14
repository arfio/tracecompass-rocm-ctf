/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.AnalysisCompilationData;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlStateSystemPathCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenStateSystemPath;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlTimeGraphEntryModel.Builder;
import org.eclipse.tracecompass.internal.tmf.core.model.AbstractTmfTraceDataProvider;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderParameterUtils;
import org.eclipse.tracecompass.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphArrow;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphRowModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphState;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphRowModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphState;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse.Status;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfAnalysisModuleWithStateSystems;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.core.util.Pair;
import org.w3c.dom.Element;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

/**
 * This data provider will return time graph models (wrapped in a response)
 * based on a query filter. The models can be used afterwards by any viewer to
 * draw time graphs. Model returned is for XML analysis.
 *
 * @author Loic Prieur-Drevon
 * @since 3.0
 */
public class XmlTimeGraphDataProvider extends AbstractTmfTraceDataProvider implements ITimeGraphDataProvider<@NonNull XmlTimeGraphEntryModel> {

    /**
     * Provider unique ID.
     */
    public static final @NonNull String ID = "org.eclipse.tracecompass.tmf.analysis.xml.core.module.XmlTimeGraphDataProvider"; //$NON-NLS-1$
    private static final AtomicLong sfAtomicId = new AtomicLong();
    private static final @NonNull String SPLIT_STRING = "/"; //$NON-NLS-1$

    private final List<ITmfStateSystem> fSs;
    private final List<Element> fEntries;

    /**
     * Remember the unique mappings of state system and quark to entry ID.
     */
    private final Table<ITmfStateSystem, Integer, Long> fBaseQuarkToId = HashBasedTable.create();
    private final Map<Long, Pair<ITmfStateSystem, Integer>> fIDToDisplayQuark = new HashMap<>();
    private final @NonNull AnalysisCompilationData fCompilationData;


    /**
     * {@link XmlTimeGraphDataProvider} create method
     *
     * @param trace
     *            the trace for which the provider will return data
     * @param viewElement
     *            the XML view {@link Element}.
     * @return the relevant {@link XmlTimeGraphDataProvider}
     */
    public static XmlTimeGraphDataProvider create(@NonNull ITmfTrace trace, Element viewElement) {
        Set<@NonNull String> analysisIds = TmfXmlUtils.getViewAnalysisIds(viewElement);
        List<Element> entries = TmfXmlUtils.getChildElements(viewElement, TmfXmlStrings.ENTRY_ELEMENT);

        Set<@NonNull ITmfAnalysisModuleWithStateSystems> stateSystemModules = new HashSet<>();
        if (analysisIds.isEmpty()) {
            /*
             * No analysis specified, take all state system analysis modules
             */
            Iterables.addAll(stateSystemModules, TmfTraceUtils.getAnalysisModulesOfClass(trace, ITmfAnalysisModuleWithStateSystems.class));
        } else {
            for (String moduleId : analysisIds) {
                // Get the module for the current trace only. The caller will take care of
                // generating composite providers with experiments
                IAnalysisModule module = trace.getAnalysisModule(moduleId);
                if (module instanceof ITmfAnalysisModuleWithStateSystems) {
                    stateSystemModules.add((ITmfAnalysisModuleWithStateSystems) module);
                }
            }
        }

        List<ITmfStateSystem> sss = new ArrayList<>();
        for (ITmfAnalysisModuleWithStateSystems module : stateSystemModules) {
            if (module.schedule().isOK() && module.waitForInitialization()) {
                module.getStateSystems().forEach(sss::add);
            }
        }
        return (sss.isEmpty() ? null : new XmlTimeGraphDataProvider(trace, sss, entries));
    }

    private XmlTimeGraphDataProvider(@NonNull ITmfTrace trace, List<ITmfStateSystem> stateSystems, List<Element> entries) {
        super(trace);
        fSs = stateSystems;
        fEntries = entries;
        fCompilationData = new AnalysisCompilationData();
    }

    @Override
    public TmfModelResponse<TmfTreeModel<@NonNull XmlTimeGraphEntryModel>> fetchTree(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        @NonNull List<@NonNull XmlTimeGraphEntryModel> entryList = new ArrayList<>();
        boolean isComplete = true;

        String traceName = String.valueOf(getTrace().getName());
        for (ITmfStateSystem ss : fSs) {
            isComplete &= ss.waitUntilBuilt(0);
            /* Don't query empty state system */
            if (ss.getNbAttributes() > 0 && ss.getStartTime() != Long.MIN_VALUE) {
                long start = ss.getStartTime();
                long end = ss.getCurrentEndTime();
                long id = fBaseQuarkToId.row(ss).computeIfAbsent(ITmfStateSystem.ROOT_ATTRIBUTE, s -> sfAtomicId.getAndIncrement());
                Builder ssEntry = new Builder(id, -1, Collections.singletonList(traceName), start, end, null, ss, ITmfStateSystem.ROOT_ATTRIBUTE, fCompilationData);
                entryList.add(ssEntry.build());

                for (Element entry : fEntries) {
                    buildEntry(ss, entry, ssEntry, -1, StringUtils.EMPTY, end, entryList);
                }
            }
        }
        Status status = isComplete ? Status.COMPLETED : Status.RUNNING;
        String msg = isComplete ? CommonStatusMessage.COMPLETED : CommonStatusMessage.RUNNING;
        return new TmfModelResponse<>(new TmfTreeModel<>(Collections.emptyList(), entryList), status, msg);
    }

    private void buildEntry(ITmfStateSystem ssq, Element entryElement, @NonNull Builder parentEntry,
            int prevBaseQuark, @NonNull String prevRegex, long currentEnd, List<XmlTimeGraphEntryModel> entryList) {
        /* Get the attribute string to display */
        String path = entryElement.getAttribute(TmfXmlStrings.PATH);
        if (path.isEmpty()) {
            path = TmfXmlStrings.WILDCARD;
        }

        /*
         * Make sure the XML element has either a display attribute or entries,
         * otherwise issue a warning
         */

        List<Element> displayElements = TmfXmlUtils.getChildElements(entryElement, TmfXmlStrings.DISPLAY_ELEMENT);
        List<Element> entryElements = TmfXmlUtils.getChildElements(entryElement, TmfXmlStrings.ENTRY_ELEMENT);

        if (displayElements.isEmpty() && entryElements.isEmpty()) {
            Activator.logWarning(String.format("XML view: entry for %s should have either a display element or entry elements", path)); //$NON-NLS-1$
            return;
        }

        // Get the state system to use to populate those entries, by default, it
        // is the same as the parent
        String analysisId = entryElement.getAttribute(TmfXmlStrings.ANALYSIS_ID);
        ITmfStateSystem parentSs = ssq;
        ITmfStateSystem ss = parentSs;
        int baseQuark = prevBaseQuark;
        if (!analysisId.isEmpty()) {
            ss = TmfStateSystemAnalysisModule.getStateSystem(getTrace(), analysisId);
            baseQuark = ITmfStateSystem.ROOT_ATTRIBUTE;
            if (ss == null) {
                return;
            }
        }

        // Replace any place holders in the path
        Pattern pattern = Pattern.compile(prevRegex);
        String attributePath = prevBaseQuark > 0 ? parentSs.getFullAttributePath(prevBaseQuark) : StringUtils.EMPTY;
        Matcher matcher = pattern.matcher(attributePath);
        if (matcher.find()) {
            path = matcher.replaceFirst(path);
        }
        String regexName = path.replaceAll("\\*", "(.*)"); //$NON-NLS-1$//$NON-NLS-2$

        /* Get the list of quarks to process with this path */
        String[] paths = regexName.split(SPLIT_STRING);
        int i = 0;
        List<Integer> quarks = Collections.singletonList(baseQuark);

        while (i < paths.length) {
            List<Integer> subQuarks = new LinkedList<>();
            /* Replace * by .* to have a regex string */
            String name = paths[i];
            for (int relativeQuark : quarks) {
                subQuarks.addAll(ss.getSubAttributes(relativeQuark, false, name));
            }
            quarks = subQuarks;
            i++;
        }

        /* Process each quark */
        DataDrivenStateSystemPath displayPath = null;
        Map<String, Builder> entryMap = new HashMap<>();
        if (!displayElements.isEmpty()) {
            Element displayElement = displayElements.get(0);
            TmfXmlStateSystemPathCu displayCu = TmfXmlStateSystemPathCu.compile(parentEntry.getAnalysisCompilationData(), Collections.singletonList(displayElement));
            if (displayCu != null) {
                displayPath = displayCu.generate();
            }
        }
        for (int quark : quarks) {
            Builder currentEntry = parentEntry;

            /* Process the current entry, if specified */
            if (displayPath != null) {
                currentEntry = processEntry(entryElement, displayPath, parentEntry, quark, ss, currentEnd);
                entryMap.put(currentEntry.getXmlId(), currentEntry);
            } else {
                long id = fBaseQuarkToId.row(ss).computeIfAbsent(quark, s -> sfAtomicId.getAndIncrement());
                currentEntry =  new Builder(id, parentEntry.getId(),
                        Collections.singletonList(ss.getAttributeName(quark)), ss.getStartTime(), ss.getCurrentEndTime(), null, ss, quark, fCompilationData);
                entryMap.put(currentEntry.getXmlId(), currentEntry);
            }
            /* Process the children entry of this entry */
            for (Element subEntryEl : entryElements) {
                String regex = prevRegex.isEmpty() ? regexName : prevRegex + '/' + regexName;
                buildEntry(ss, subEntryEl, currentEntry, quark, regex, currentEnd, entryList);
            }
        }
        // At this point, the parent has been set, so we can build the entries
        buildTree(entryMap, parentEntry.getId());
        for (Builder b : entryMap.values()) {
            entryList.add(b.build());
        }
    }

    private Builder processEntry(@NonNull Element entryElement, DataDrivenStateSystemPath displayPath,
            @NonNull Builder parentEntry, int quark, ITmfStateSystem ss, long currentEnd) {
        /*
         * Get the start time and end time of this entry from the display attribute
         */
        int displayQuark = displayPath.getQuark(quark, parentEntry);
        long id = fBaseQuarkToId.row(ss).computeIfAbsent(quark, s -> sfAtomicId.getAndIncrement());
        if (displayQuark < 0) {
            return new Builder(id, parentEntry.getId(),
                    Collections.singletonList(String.format("Unknown display quark for %s", ss.getAttributeName(quark))), ss.getStartTime(), ss.getCurrentEndTime(), null, ss, quark, fCompilationData); //$NON-NLS-1$
        }
        fIDToDisplayQuark.put(id, new Pair<>(ss, displayQuark));

        long entryStart = ss.getStartTime();
        long entryEnd = currentEnd;

        try {

            ITmfStateInterval oneInterval = ss.querySingleState(entryStart, displayQuark);
            /* The entry start is the first non-null interval */
            while (oneInterval.getStateValue().isNull()) {
                long ts = oneInterval.getEndTime() + 1;
                if (ts > currentEnd) {
                    break;
                }
                oneInterval = ss.querySingleState(ts, displayQuark);
            }
            entryStart = oneInterval.getStartTime();

            /* The entry end is the last non-null interval */
            oneInterval = ss.querySingleState(entryEnd - 1, displayQuark);
            while (oneInterval.getStateValue().isNull()) {
                long ts = oneInterval.getStartTime() - 1;
                if (ts < ss.getStartTime()) {
                    break;
                }
                oneInterval = ss.querySingleState(ts, displayQuark);
            }
            entryEnd = Math.min(oneInterval.getEndTime() + 1, currentEnd);

        } catch (StateSystemDisposedException e) {
        }

        return new Builder(id, parentEntry.getId(), Collections.singletonList(ss.getAttributeName(quark)), entryStart, entryEnd, entryElement, ss, quark, fCompilationData);
    }

    /** Build a tree using getParentId() and getId() */
    private static void buildTree(Map<String, Builder> entryMap, long parentId) {
        for (Builder entry : entryMap.values()) {
            boolean root = true;
            if (!entry.getXmlParentId().isEmpty()) {
                Builder parent = entryMap.get(entry.getXmlParentId());
                /*
                 * Associate the parent entry only if their time overlap. A child entry may
                 * start before its parent, for example at the beginning of the trace if a
                 * parent has not yet appeared in the state system. We just want to make sure
                 * that the entry didn't start after the parent ended or ended before the parent
                 * started.
                 */
                if (parent != null &&
                        !(entry.getStartTime() > parent.getEndTime() ||
                                entry.getEndTime() < parent.getStartTime())) {
                    entry.setParentId(parent.getId());
                    root = false;
                }
            }
            if (root) {
                entry.setParentId(parentId);
            }
        }
    }

    @Override
    public @NonNull String getId() {
        return ID;
    }

    @Override
    public @NonNull TmfModelResponse<@NonNull TimeGraphModel> fetchRowModel(@NonNull Map<@NonNull String, @NonNull Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        Table<ITmfStateSystem, Integer, Long> table = HashBasedTable.create();

        // TODO server: Parameters validation should be handle separately. It
        // can be either in the data provider itself or before calling it. It
        // will avoid the creation of filters and the content of the map can be
        // use directly.
        SelectionTimeQueryFilter filter = FetchParametersUtils.createSelectionTimeQuery(fetchParameters);
        if (filter == null) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.INCORRECT_QUERY_PARAMETERS);
        }
        for (Long id : filter.getSelectedItems()) {
            Pair<ITmfStateSystem, Integer> pair = fIDToDisplayQuark.get(id);
            if (pair != null) {
                table.put(pair.getFirst(), pair.getSecond(), id);
            }
        }
        List<@NonNull ITimeGraphRowModel> allRows = new ArrayList<>();
        try {
            for (Entry<ITmfStateSystem, Map<Integer, Long>> ssEntry : table.rowMap().entrySet()) {
                Collection<@NonNull ITimeGraphRowModel> rows = createRows(ssEntry.getKey(), ssEntry.getValue(), filter.getTimesRequested(), fetchParameters, monitor);
                allRows.addAll(rows);
            }
        } catch (IndexOutOfBoundsException | TimeRangeException | StateSystemDisposedException e) {
            return new TmfModelResponse<>(null, Status.FAILED, CommonStatusMessage.STATE_SYSTEM_FAILED);
        }
        return new TmfModelResponse<>(new TimeGraphModel(allRows), Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    private @NonNull Collection<@NonNull ITimeGraphRowModel> createRows(ITmfStateSystem ss, Map<Integer, Long> idToDisplayQuark,
            long[] timesRequested, @NonNull Map<@NonNull String, @NonNull Object> parameters, @Nullable IProgressMonitor monitor) throws StateSystemDisposedException {
        Map<@NonNull Integer, @NonNull Predicate<@NonNull Multimap<@NonNull String, @NonNull Object>>> predicates = new HashMap<>();
        Multimap<@NonNull Integer, @NonNull String> regexesMap = DataProviderParameterUtils.extractRegexFilter(parameters);
        if (regexesMap != null) {
            predicates.putAll(computeRegexPredicate(regexesMap));
        }

        long currentEndTime = ss.getCurrentEndTime();
        Map<Integer, ITimeGraphRowModel> quarkToRow = new HashMap<>(idToDisplayQuark.size());
        for (Entry<Integer, Long> entry : idToDisplayQuark.entrySet()) {
            quarkToRow.put(entry.getKey(), new TimeGraphRowModel(entry.getValue(), new ArrayList<>()));
        }
        for (ITmfStateInterval interval : ss.query2D(idToDisplayQuark.keySet(), getTimes(ss, timesRequested))) {
            if (monitor != null && monitor.isCanceled()) {
                return Collections.emptyList();
            }
            ITimeGraphRowModel row = quarkToRow.get(interval.getAttribute());
            if (row != null) {
                List<@NonNull ITimeGraphState> states = row.getStates();
                ITimeGraphState timeGraphState = getStateFromInterval(interval, currentEndTime);
                applyFilterAndAddState(states, timeGraphState, row.getEntryID(), predicates, monitor);
            }
        }
        for (ITimeGraphRowModel model : quarkToRow.values()) {
            model.getStates().sort(Comparator.comparingLong(ITimeGraphState::getStartTime));
        }
        return quarkToRow.values();
    }

    private static @NonNull TimeGraphState getStateFromInterval(ITmfStateInterval statusInterval, long currentEndTime) {
        long time = statusInterval.getStartTime();
        long duration = Math.min(currentEndTime, statusInterval.getEndTime() + 1) - time;
        Object o = statusInterval.getValue();
        if (o instanceof Integer) {
            return new TimeGraphState(time, duration, ((Integer) o).intValue(), String.valueOf(o));
        } else if (o instanceof Long) {
            long l = (long) o;
            return new TimeGraphState(time, duration, (int) l, "0x" + Long.toHexString(l)); //$NON-NLS-1$
        } else if (o instanceof String) {
            return new TimeGraphState(time, duration, Integer.MIN_VALUE, (String) o);
        } else if (o instanceof Double) {
            return new TimeGraphState(time, duration, ((Double) o).intValue());
        }
        return new TimeGraphState(time, duration, Integer.MIN_VALUE);
    }

    private static @NonNull Set<@NonNull Long> getTimes(ITmfStateSystem key, long[] timesRequested) {
        Set<@NonNull Long> times = new HashSet<>();
        for (long t : timesRequested) {
            if (key.getStartTime() <= t && t <= key.getCurrentEndTime()) {
                times.add(t);
            }
        }
        return times;
    }

    @Override
    public @NonNull TmfModelResponse<@NonNull List<@NonNull ITimeGraphArrow>> fetchArrows(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        return new TmfModelResponse<>(null, Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    @Override
    public @NonNull TmfModelResponse<@NonNull Map<@NonNull String, @NonNull String>> fetchTooltip(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        return new TmfModelResponse<>(null, Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

}
