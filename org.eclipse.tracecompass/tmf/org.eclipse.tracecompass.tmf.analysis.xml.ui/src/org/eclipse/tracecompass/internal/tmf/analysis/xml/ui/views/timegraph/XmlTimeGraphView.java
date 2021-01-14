/*******************************************************************************
 * Copyright (c) 2014, 2019 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Florian Wininger - Initial API and implementation
 *   Geneviève Bastien - Review of the initial implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.timegraph;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.DataDrivenOutputEntryModel;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.DataDrivenTimeGraphDataProvider;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.XmlDataProviderManager;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.XmlViewInfo;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphState;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.views.TmfViewFactory;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.BaseDataProviderTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphViewer;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.w3c.dom.Element;

/**
 * This view displays state system data in a time graph view. It uses an XML
 * {@link TmfXmlStrings#TIME_GRAPH_VIEW} element from an XML file. This
 * element defines which entries from the state system will be shown and also
 * gives additional information on the presentation of the view (states, colors,
 * etc)
 *
 * @author Florian Wininger
 * @author Mikael Ferland
 */
public class XmlTimeGraphView extends BaseDataProviderTimeGraphView {


    /** View ID. */
    public static final @NonNull String ID = "org.eclipse.linuxtools.tmf.analysis.xml.ui.views.timegraph"; //$NON-NLS-1$

    private static final String[] DEFAULT_COLUMN_NAMES = new String[] {
            Messages.XmlTimeGraphView_ColumnName,
            Messages.XmlTimeGraphView_ColumnId,
            Messages.XmlTimeGraphView_ColumnParentId,
    };

    private static final String[] DEFAULT_FILTER_COLUMN_NAMES = new String[] {
            Messages.XmlTimeGraphView_ColumnName,
            Messages.XmlTimeGraphView_ColumnId
    };

    /** The relative weight of the sash */
    private static final int[] fWeight = { 1, 2 };

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    private static final Comparator<TimeGraphEntryModel> XML_ENTRY_COMPARATOR = ((TimeGraphEntryModel obj1, TimeGraphEntryModel obj2) -> {
        try {
            return Comparator
                    .comparing((TimeGraphEntryModel obj) -> Long.decode(obj.getName()))
                    .thenComparingLong(TimeGraphEntryModel::getStartTime)
                    .compare(obj1, obj2);
        } catch (NumberFormatException nfe) {
            return Comparator
                    .comparing(TimeGraphEntryModel::getName)
                    .thenComparingLong(TimeGraphEntryModel::getStartTime)
                    .compare(obj1, obj2);
        }
    });
    private static final Comparator<ITimeGraphEntry> ENTRY_COMPARATOR = Comparator.comparing(x -> (TimeGraphEntryModel) ((TimeGraphEntry) x).getEntryModel(), XML_ENTRY_COMPARATOR);

    private final @NonNull XmlViewInfo fViewInfo = new XmlViewInfo(ID);
    private final Map<String, Integer> fStringValueMap = new HashMap<>();

    private XmlPresentationProvider fPresentationProvider;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public XmlTimeGraphView() {
        super(ID, new XmlPresentationProvider(ID), DataDrivenTimeGraphDataProvider.ID);
        setWeight(fWeight);
        setTreeColumns(DEFAULT_COLUMN_NAMES);
        setTreeLabelProvider(new XmlTreeLabelProvider());
        setFilterColumns(DEFAULT_FILTER_COLUMN_NAMES);
        setFilterLabelProvider(new XmlTreeLabelProvider());
        setEntryComparator(ENTRY_COMPARATOR);
        addPartPropertyListener(event -> {
            Object newValue = event.getNewValue();
            if (event.getProperty().equals(TmfXmlStrings.XML_OUTPUT_DATA) && newValue instanceof String) {
                String data = (String) newValue;
                fViewInfo.setViewData(data);
                TimeGraphViewer timeGraphViewer = getTimeGraphViewer();
                timeGraphViewer.getTimeGraphControl().colorSettingsChanged(timeGraphViewer.getTimeGraphProvider().getStateTable());
                rebuild();
            }
        });
    }

    @Override
    public void createPartControl(Composite parent) {
        String name = getViewSite().getSecondaryId();
        if (name != null) {
            name = TmfViewFactory.getBaseSecId(name);
        }
        if (name != null) {
            /* must initialize view info before calling super */
            fViewInfo.setName(name);
        }
        fPresentationProvider = new XmlPresentationProvider(name);
        fPresentationProvider.addColorListener(stateItems -> super.getPresentationProvider().refresh());
        super.createPartControl(parent);
    }

    private void setViewTitle(final String title) {
        Display.getDefault().asyncExec(() -> setPartName(title));
    }

    @Override
    protected String getNextText() {
        return Messages.XmlTimeGraphView_NextText;
    }

    @Override
    protected String getNextTooltip() {
        return Messages.XmlTimeGraphView_NextTooltip;
    }

    @Override
    protected String getPrevText() {
        return Messages.XmlTimeGraphView_PreviousText;
    }

    @Override
    protected String getPrevTooltip() {
        return Messages.XmlTimeGraphView_PreviousInterval;
    }

    /**
     * Getter for the presentation provider
     *
     * @return The time graph presentation provider
     */
    @Override
    protected XmlPresentationProvider getPresentationProvider() {
        /*
         * masking a field.
         */
        return fPresentationProvider;
    }

    /**
     * Default label provider, it shows name, id and parent columns
     *
     * TODO: There should be a way to define columns in the XML
     */
    private static class XmlTreeLabelProvider extends TreeLabelProvider {

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof TimeGraphEntry) {
                TimeGraphEntry entry = (TimeGraphEntry) element;

                if (DEFAULT_COLUMN_NAMES[columnIndex].equals(Messages.XmlTimeGraphView_ColumnName)) {
                    return entry.getName();
                }

                ITmfTreeDataModel model = entry.getEntryModel();
                if (model instanceof DataDrivenOutputEntryModel) {
                    DataDrivenOutputEntryModel xmlModel = (DataDrivenOutputEntryModel) model;
                    if (DEFAULT_COLUMN_NAMES[columnIndex].equals(Messages.XmlTimeGraphView_ColumnId)) {
                        return xmlModel.getXmlId();
                    } else if (DEFAULT_COLUMN_NAMES[columnIndex].equals(Messages.XmlTimeGraphView_ColumnParentId)) {
                        return xmlModel.getXmlParentId();
                    }
                }
            }
            return EMPTY_STRING;
        }
    }

    // ------------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------------

    @Override
    protected void buildEntryList(ITmfTrace trace, ITmfTrace parentTrace, IProgressMonitor monitor) {
        /*
         * Get the view element from the XML file. If the element can't be found,
         * return.
         */
        Element viewElement = fViewInfo.getViewElement(TmfXmlStrings.TIME_GRAPH_VIEW);
        if (viewElement == null) {
            return;
        }
        // Empty the additional state values
        fStringValueMap.clear();
        ITimeGraphPresentationProvider pres = this.getPresentationProvider();
        if (pres instanceof XmlPresentationProvider) {
            /*
             * TODO: Each entry of a line could have their own states/color. That will
             * require an update to the presentation provider
             */
            ((XmlPresentationProvider) pres).loadNewStates(viewElement);
            Display.getDefault().asyncExec(() -> {
                TimeGraphViewer timeGraphViewer = getTimeGraphViewer();
                if (timeGraphViewer.getTimeGraphControl().isDisposed()) {
                    return;
                }
                timeGraphViewer.getTimeGraphControl().colorSettingsChanged(timeGraphViewer.getTimeGraphProvider().getStateTable());
            });
        }

        String title = fViewInfo.getViewTitle(viewElement);
        setViewTitle(title != null ? title : Messages.XmlTimeGraphView_DefaultTitle);

        SubMonitor subMonitor = SubMonitor.convert(monitor);
        boolean complete = false;
        ITimeGraphDataProvider<@NonNull TimeGraphEntryModel> provider = XmlDataProviderManager.getInstance().getTimeGraphProvider(trace, viewElement);
        if (provider == null) {
            return;
        }
        while (!complete && !subMonitor.isCanceled()) {
            TmfModelResponse<TmfTreeModel<@NonNull TimeGraphEntryModel>> response = provider.fetchTree(FetchParametersUtils.timeQueryToMap(new TimeQueryFilter(0, Long.MAX_VALUE, 2)), subMonitor);
            if (response.getStatus() == ITmfResponse.Status.FAILED) {
                Activator.logError("XML Time Graph Data Provider failed: " + response.getStatusMessage()); //$NON-NLS-1$
                return;
            } else if (response.getStatus() == ITmfResponse.Status.CANCELLED) {
                return;
            }
            complete = response.getStatus() == ITmfResponse.Status.COMPLETED;

            TmfTreeModel<@NonNull TimeGraphEntryModel> model = response.getModel();
            if (model != null) {
                synchronized (fEntries) {
                    /*
                     * Ensure that all the entries exist and are up to date.
                     */
                    for (TimeGraphEntryModel entry : model.getEntries()) {
                        TimeGraphEntry tgEntry = fEntries.get(provider, entry.getId());
                        if (tgEntry == null) {
                            if (entry.getParentId() == -1) {
                                tgEntry = new TraceEntry(entry, trace, provider);
                                addToEntryList(parentTrace, Collections.singletonList(tgEntry));
                            } else {
                                tgEntry = new TimeGraphEntry(entry);
                            }
                            fEntries.put(provider, entry.getId(), tgEntry);
                        } else {
                            tgEntry.updateModel(entry);
                        }
                        if (entry.getParentId() == -1) {
                            setStartTime(Long.min(getStartTime(), entry.getStartTime()));
                            setEndTime(Long.max(getEndTime(), entry.getEndTime()));
                        }
                    }
                }
                /*
                 * set the correct child / parent relation
                 */
                for (TimeGraphEntry child : fEntries.row(provider).values()) {
                    TimeGraphEntry parent = fEntries.get(provider, child.getEntryModel().getParentId());
                    if (parent != null) {
                        parent.addChild(child);
                    }
                }
                long start = getStartTime();
                long end = getEndTime();
                final long resolution = Long.max(1, (end - start) / getDisplayWidth());
                zoomEntries(fEntries.row(provider).values(), start, end, resolution, subMonitor);
            }
            if (parentTrace.equals(getTrace())) {
                refresh();
            }
            subMonitor.worked(1);

            if (!complete) {
                try {
                    Thread.sleep(BUILD_UPDATE_TIMEOUT);
                } catch (InterruptedException e) {
                    Activator.logError("Failed to wait for data provider", e); //$NON-NLS-1$
                }
            }
        }
    }

    @Override
    protected TimeEvent createTimeEvent(TimeGraphEntry entry, ITimeGraphState state) {
        int status = state.getValue();
        String label = state.getLabel();
        if (status == Integer.MIN_VALUE) {
            if (label == null) {
                return new NullTimeEvent(entry, state.getStartTime(), state.getDuration());
            }
            // String interval
            int value = getStringIndex(label);
            return new TimeEvent(entry, state.getStartTime(), state.getDuration(), value, state.getActiveProperties());
        }

        XmlPresentationProvider pres = getPresentationProvider();
        if (label != null && !pres.hasIndex(status)) {
            status = getStringIndex(label);
        }

        return new TimeEvent(entry, state.getStartTime(), state.getDuration(), status, state.getActiveProperties()) {
            @Override
            public String getLabel() {
                if (entry.getEntryModel() instanceof DataDrivenOutputEntryModel) {
                    // Is there text to show
                    if (((DataDrivenOutputEntryModel) entry.getEntryModel()).showText()) {
                        return pres.getEventName(this);
                    }
                }
                return null;
            }
        };
    }

    private int getStringIndex(String state) {
        return fStringValueMap.computeIfAbsent(state, s -> getPresentationProvider().addState(s));
    }

    @Override
    protected @NonNull Iterable<ITmfTrace> getTracesToBuild(@Nullable ITmfTrace trace) {
        // The data provider factory will build the proper data provider
        return Collections.singleton(trace);
    }

}
