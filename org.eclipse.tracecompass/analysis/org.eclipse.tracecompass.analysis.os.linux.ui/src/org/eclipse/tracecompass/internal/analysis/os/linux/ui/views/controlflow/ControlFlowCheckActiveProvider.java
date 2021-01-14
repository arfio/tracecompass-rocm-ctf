/*******************************************************************************
 * Copyright (c) 2015, 2018 Keba AG
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Christian Mansky - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.threadstatus.ThreadEntryModel;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.threadstatus.ThreadStatusDataProvider;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.BaseDataProviderTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.ITimeGraphEntryActiveProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

/**
 * Provides Functionality for check Active / uncheck inactive
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @since 1.0
 */
public final class ControlFlowCheckActiveProvider implements ITimeGraphEntryActiveProvider {

    String fLabel;
    String fTooltip;
    private TmfTimeRange fRange = null;
    private ThreadStatusDataProvider fProvider = null;
    private @NonNull Set<Long> fActive = Collections.emptySet();

    /**
     * @param label
     *            Button label
     * @param tooltip
     *            Button tooltip
     */
    public ControlFlowCheckActiveProvider(String label, String tooltip) {
        fLabel = label;
        fTooltip = tooltip;
    }

    @Override
    public String getLabel() {
        return fLabel;
    }

    @Override
    public String getTooltip() {
        return fTooltip;
    }

    @Override
    public boolean isActive(ITimeGraphEntry element) {
        ThreadEntryModel model = ControlFlowView.getThreadEntryModel(element);
        if (model != null) {
            TmfTraceManager traceManager = TmfTraceManager.getInstance();
            TmfTraceContext traceContext = traceManager.getCurrentTraceContext();
            TmfTimeRange range = traceContext.getSelectionRange();

            /* Take precedence of selection over window range. */
            if (Objects.equals(range.getStartTime(), range.getEndTime())) {
                range = traceContext.getWindowRange();
            }

            Set<Long> ids = getActiveIds((TimeGraphEntry) element, range);
            return ids.contains(model.getId());
        }

        return false;
    }

    private Set<Long> getActiveIds(TimeGraphEntry cfe, TmfTimeRange range) {
        ITimeGraphDataProvider<? extends TimeGraphEntryModel> dataProvider = BaseDataProviderTimeGraphView.getProvider(cfe);
        if (range.equals(fRange) && dataProvider.equals(fProvider)
                || !(dataProvider instanceof ThreadStatusDataProvider)) {
            return fActive;
        }
        TimeQueryFilter filter = new TimeQueryFilter(range.getStartTime().toNanos(), range.getEndTime().toNanos(), 2);
        Map<@NonNull String, @NonNull Object> parameters = FetchParametersUtils.timeQueryToMap(filter);
        parameters.put(ThreadStatusDataProvider.ACTIVE_THREAD_FILTER_KEY, true);
        TmfModelResponse<TmfTreeModel<@NonNull TimeGraphEntryModel>> response = ((ThreadStatusDataProvider) dataProvider).fetchTree(parameters, null);
        TmfTreeModel<@NonNull TimeGraphEntryModel> model = response.getModel();
        if (model == null) {
            // query must have failed, return empty and don't invalidate the cache.
            return Collections.emptySet();
        }
        fRange = range;
        fActive = model.getEntries().stream().map(TimeGraphEntryModel::getId).collect(Collectors.toSet());
        return fActive;

    }

}
