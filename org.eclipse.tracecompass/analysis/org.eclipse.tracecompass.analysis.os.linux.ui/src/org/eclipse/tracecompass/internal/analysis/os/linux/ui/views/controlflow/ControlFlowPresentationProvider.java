/*******************************************************************************
 * Copyright (c) 2012, 2018 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Geneviève Bastien - Move code to provide base classes for time graph view
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.model.ProcessStatus;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.registry.LinuxStyle;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.threadstatus.ThreadEntryModel;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Messages;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.BaseDataProviderTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ILinkEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEventStyleStrings;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NamedTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Presentation provider for the control flow view
 */
public class ControlFlowPresentationProvider extends TimeGraphPresentationProvider {

    private static final Map<Integer, StateItem> STATE_MAP;
    private static final List<StateItem> STATE_LIST;
    private static final StateItem[] STATE_TABLE;
    private static final int LINK_VALUE = 8;

    private static StateItem createState(LinuxStyle style) {
        return new StateItem(style.toMap());
    }

    static {
        ImmutableMap.Builder<Integer, StateItem> builder = new ImmutableMap.Builder<>();
        /*
         * ADD STATE MAPPING HERE
         */
        builder.put(ProcessStatus.UNKNOWN.getStateValue().unboxInt(), createState(LinuxStyle.UNKNOWN));
        builder.put(ProcessStatus.RUN.getStateValue().unboxInt(), createState(LinuxStyle.USERMODE));
        builder.put(ProcessStatus.RUN_SYTEMCALL.getStateValue().unboxInt(), createState(LinuxStyle.SYSCALL));
        builder.put(ProcessStatus.INTERRUPTED.getStateValue().unboxInt(), createState(LinuxStyle.INTERRUPTED));
        builder.put(ProcessStatus.WAIT_BLOCKED.getStateValue().unboxInt(), createState(LinuxStyle.WAIT_BLOCKED));
        builder.put(ProcessStatus.WAIT_CPU.getStateValue().unboxInt(), createState(LinuxStyle.WAIT_FOR_CPU));
        builder.put(ProcessStatus.WAIT_UNKNOWN.getStateValue().unboxInt(), createState(LinuxStyle.WAIT_UNKNOWN));

        LinuxStyle link = LinuxStyle.LINK;
        ImmutableMap.Builder<String, Object> linkyBuilder = new ImmutableMap.Builder<>();
        linkyBuilder.putAll(link.toMap());
        linkyBuilder.put(ITimeEventStyleStrings.itemTypeProperty(), ITimeEventStyleStrings.linkType());
        StateItem linkItem = new StateItem(linkyBuilder.build());
        builder.put(LINK_VALUE, linkItem);
        /*
         * DO NOT MODIFY AFTER
         */
        STATE_MAP = builder.build();
        STATE_LIST = ImmutableList.copyOf(STATE_MAP.values());
        STATE_TABLE = STATE_LIST.toArray(new StateItem[STATE_LIST.size()]);
    }

    /**
     * Default constructor
     */
    public ControlFlowPresentationProvider() {
        super(Messages.ControlFlowView_stateTypeName);
    }

    @Override
    public StateItem[] getStateTable() {
        return STATE_TABLE;
    }

    @Override
    public int getStateTableIndex(ITimeEvent event) {
        if (event instanceof TimeEvent) {
            if (event instanceof ILinkEvent) {
                return STATE_LIST.indexOf(STATE_MAP.getOrDefault(LINK_VALUE, STATE_MAP.get(ProcessStatus.UNKNOWN.getStateValue().unboxInt())));
            }
            if (((TimeEvent) event).hasValue()) {
                int status = ((TimeEvent) event).getValue();
                return STATE_LIST.indexOf(getMatchingState(status));
            }
        }
        if (event instanceof NullTimeEvent) {
            return INVISIBLE;
        }
        return TRANSPARENT;
    }

    @Override
    public String getEventName(ITimeEvent event) {
        if (event instanceof TimeEvent) {
            TimeEvent ev = (TimeEvent) event;
            if (ev.hasValue()) {
                return getMatchingState(ev.getValue()).getStateString();
            }
        }
        return Messages.ControlFlowView_multipleStates;
    }

    private static StateItem getMatchingState(int status) {
        return STATE_MAP.getOrDefault(status, STATE_MAP.get(ProcessStatus.WAIT_UNKNOWN.getStateValue().unboxInt()));
    }

    @Override
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event) {
        Map<String, String> retMap = new LinkedHashMap<>(1);

        if (event instanceof NamedTimeEvent) {
            retMap.put(Messages.ControlFlowView_attributeSyscallName, ((NamedTimeEvent) event).getLabel());
        }

        return retMap;
    }

    @Override
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event, long hoverTime) {
        Map<String, String> retMap = super.getEventHoverToolTipInfo(event, hoverTime);
        if (retMap == null) {
            retMap = new LinkedHashMap<>(1);
        }

        ThreadEntryModel entryModel = ControlFlowView.getThreadEntryModel(event.getEntry());
        if (!(event instanceof TimeEvent) || !((TimeEvent) event).hasValue() ||
                entryModel == null) {
            return retMap;
        }

        ITimeGraphDataProvider<? extends TimeGraphEntryModel> dataProvider = BaseDataProviderTimeGraphView.getProvider((TimeGraphEntry) event.getEntry());
        TmfModelResponse<@NonNull Map<@NonNull String, @NonNull String>> response = dataProvider.fetchTooltip(
        FetchParametersUtils.selectionTimeQueryToMap(new SelectionTimeQueryFilter(hoverTime, hoverTime, 1, Collections.singletonList(entryModel.getId()))), null);
        Map<@NonNull String, @NonNull String> tooltipModel = response.getModel();
        if (tooltipModel != null) {
            retMap.putAll(tooltipModel);
        }

        return retMap;
    }
}
