/*******************************************************************************
 * Copyright (c) 2012, 2019 Ericsson, École Polytechnique de Montréal and others.
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
 *   Christian Mansky - Add check active / uncheck inactive buttons
 *   Mahdi Zolnouri & Samuel Gagnon - Add flat / hierarchical button
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelTidAspect;
import org.eclipse.tracecompass.analysis.os.linux.core.model.OsStrings;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.threadstatus.ThreadEntryModel;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.threadstatus.ThreadStatusDataProvider;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Activator;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Messages;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.actions.FollowThreadAction;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow.filters.ActiveThreadsFilter;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow.filters.DynamicFilterDialog;
import org.eclipse.tracecompass.internal.provisional.tmf.ui.widgets.timegraph.BaseDataProviderTimeGraphPresentationProvider;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphRowModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphState;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils;
import org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils.Resolution;
import org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils.TimeFormat;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.BaseDataProviderTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ILinkEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.Utils;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

/**
 * The Control Flow view main object
 */
public class ControlFlowView extends BaseDataProviderTimeGraphView {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * View ID.
     */
    public static final @NonNull String ID = "org.eclipse.tracecompass.analysis.os.linux.views.controlflow"; //$NON-NLS-1$

    private static final String ICONS_PATH = "icons/"; //$NON-NLS-1$
    private static final String OPTIMIZE_ICON = ICONS_PATH + "elcl16/Optimization.png"; //$NON-NLS-1$

    private static final String PROCESS_COLUMN = Messages.ControlFlowView_processColumn;
    private static final String TID_COLUMN = OsStrings.tid();
    private static final String PTID_COLUMN = OsStrings.ptid();
    private static final String PID_COLUMN = OsStrings.pid();
    private static final String BIRTH_TIME_COLUMN = Messages.ControlFlowView_birthTimeColumn;
    private Action fOptimizationAction;

    private static final String NEXT_EVENT_ICON_PATH = "icons/elcl16/shift_r_edit.gif"; //$NON-NLS-1$
    private static final String PREV_EVENT_ICON_PATH = "icons/elcl16/shift_l_edit.gif"; //$NON-NLS-1$

    private static final String[] COLUMN_NAMES = new String[] {
            PROCESS_COLUMN,
            TID_COLUMN,
            PID_COLUMN,
            PTID_COLUMN,
            BIRTH_TIME_COLUMN
    };

    private static final String[] FILTER_COLUMN_NAMES = new String[] {
            PROCESS_COLUMN,
            TID_COLUMN,
            PID_COLUMN
    };

    private static final Comparator<ITimeGraphEntry>[] COLUMN_COMPARATORS;

    private static final Function<Collection<ILinkEvent>, Map<Integer, Long>> UPDATE_SCHEDULING_COLUMN_ALGO = new NaiveOptimizationAlgorithm();

    private static final int INITIAL_SORT_COLUMN_INDEX = 3;

    static {
        ImmutableList.Builder<Comparator<ITimeGraphEntry>> builder = ImmutableList.builder();
        builder.add(ControlFlowColumnComparators.PROCESS_NAME_COLUMN_COMPARATOR)
            .add(ControlFlowColumnComparators.TID_COLUMN_COMPARATOR)
            .add(ControlFlowColumnComparators.PID_COLUMN_COMPARATOR)
            .add(ControlFlowColumnComparators.PTID_COLUMN_COMPARATOR)
            .add(ControlFlowColumnComparators.BIRTH_TIME_COLUMN_COMPARATOR);
        List<Comparator<ITimeGraphEntry>> l = builder.build();
        COLUMN_COMPARATORS = l.toArray(new Comparator[l.size()]);
    }

    /**
     * Mutex rule for search action jobs, making sure they execute sequentially
     */
    private final ISchedulingRule fSearchActionMutexRule = new ISchedulingRule() {
        @Override
        public boolean isConflicting(ISchedulingRule rule) {
            return (rule == this);
        }

        @Override
        public boolean contains(ISchedulingRule rule) {
            return (rule == this);
        }
    };

    private final Set<ITmfTrace> fFlatTraces = new HashSet<>();

    private IAction fFlatAction;

    private IAction fHierarchicalAction;

    private @NonNull ActiveThreadsFilter fActiveThreadsFilter = new ActiveThreadsFilter(null, false, null);

    private final ActiveThreadsFilterAction fActiveThreadsRapidToggle = new ActiveThreadsFilterAction();

    class ActiveThreadsFilterAction extends Action {
        public ActiveThreadsFilterAction() {
            super(PackageMessages.ControlFlowView_DynamicFiltersActiveThreadToggleLabel, IAction.AS_CHECK_BOX);
            setToolTipText(PackageMessages.ControlFlowView_DynamicFiltersActiveThreadToggleToolTip);
            addPropertyChangeListener(event -> {
                if (!(event.getNewValue() instanceof Boolean)) {
                    return;
                }

                Boolean enabled = (Boolean) event.getNewValue();

                /* Always remove the previous Active Threads filter */
                getTimeGraphViewer().removeFilter(fActiveThreadsFilter);

                if (enabled) {
                    fActiveThreadsFilter.setEnabled(true);
                    getTimeGraphViewer().addFilter(fActiveThreadsFilter);

                    /* Use flat representation */
                    if (fFlatAction != null) {
                        applyFlatPresentation();
                        fFlatAction.setChecked(true);
                        fHierarchicalAction.setChecked(false);
                    }
                } else {
                    fActiveThreadsFilter.setEnabled(false);
                }
                startZoomThread(getTimeGraphViewer().getTime0(), getTimeGraphViewer().getTime1());
            });
        }
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     */
    public ControlFlowView() {
        super(ID, new BaseDataProviderTimeGraphPresentationProvider(), ThreadStatusDataProvider.ID);
        setTreeColumns(COLUMN_NAMES, COLUMN_COMPARATORS, INITIAL_SORT_COLUMN_INDEX);
        setTreeLabelProvider(new ControlFlowTreeLabelProvider());
        setFilterColumns(FILTER_COLUMN_NAMES);
        setFilterLabelProvider(new ControlFlowFilterLabelProvider());
        setEntryComparator(ControlFlowColumnComparators.BIRTH_TIME_COLUMN_COMPARATOR);
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        // add "Check active" Button to TimeGraphFilterDialog
        getTimeGraphViewer().getShowFilterDialogAction().getFilterDialog().addTimeGraphFilterCheckActiveButton(
                new ControlFlowCheckActiveProvider(Messages.ControlFlowView_checkActiveLabel, Messages.ControlFlowView_checkActiveToolTip));
        // add "Uncheck inactive" Button to TimeGraphFilterDialog
        getTimeGraphViewer().getShowFilterDialogAction().getFilterDialog().addTimeGraphFilterUncheckInactiveButton(
                new ControlFlowCheckActiveProvider(Messages.ControlFlowView_uncheckInactiveLabel, Messages.ControlFlowView_uncheckInactiveToolTip));
    }

    /**
     * @since 2.0
     */
    @Override
    protected void fillTimeGraphEntryContextMenu(@NonNull IMenuManager menuManager) {
        ISelection selection = getSite().getSelectionProvider().getSelection();
        if (selection instanceof StructuredSelection) {
            StructuredSelection sSel = (StructuredSelection) selection;
            if (sSel.getFirstElement() instanceof TimeGraphEntry) {
                TimeGraphEntry entry = (TimeGraphEntry) sSel.getFirstElement();
                ITmfTreeDataModel entryModel = entry.getEntryModel();
                if (entryModel instanceof ThreadEntryModel) {
                    menuManager.add(new FollowThreadAction(ControlFlowView.this, entry.getName(), ((ThreadEntryModel) entryModel).getThreadId(), getTrace(entry)));
                }
            }
        }
    }

    @Override
    protected void fillLocalToolBar(IToolBarManager manager) {
        // add "Optimization" Button to local tool bar of Controlflow
        IAction optimizationAction = getOptimizationAction();
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, optimizationAction);

        // add a separator to local tool bar
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, new Separator());

        super.fillLocalToolBar(manager);
        IDialogSettings settings = Activator.getDefault().getDialogSettings();
        IDialogSettings section = settings.getSection(getClass().getName());
        if (section == null) {
            section = settings.addNewSection(getClass().getName());
        }

        IAction hideArrowsAction = getTimeGraphViewer().getHideArrowsAction(section);
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, hideArrowsAction);

        IAction followArrowBwdAction = getTimeGraphViewer().getFollowArrowBwdAction();
        followArrowBwdAction.setText(Messages.ControlFlowView_followCPUBwdText);
        followArrowBwdAction.setToolTipText(Messages.ControlFlowView_followCPUBwdText);
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, followArrowBwdAction);

        IAction followArrowFwdAction = getTimeGraphViewer().getFollowArrowFwdAction();
        followArrowFwdAction.setText(Messages.ControlFlowView_followCPUFwdText);
        followArrowFwdAction.setToolTipText(Messages.ControlFlowView_followCPUFwdText);
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, followArrowFwdAction);

        IAction previousEventAction = new SearchEventAction(false, PackageMessages.ControlFlowView_PreviousEventJobName);
        previousEventAction.setText(PackageMessages.ControlFlowView_PreviousEventActionName);
        previousEventAction.setToolTipText(PackageMessages.ControlFlowView_PreviousEventActionTooltip);
        previousEventAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(PREV_EVENT_ICON_PATH));
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, previousEventAction);

        IAction nextEventAction = new SearchEventAction(true, PackageMessages.ControlFlowView_NextEventJobName);
        nextEventAction.setText(PackageMessages.ControlFlowView_NextEventActionName);
        nextEventAction.setToolTipText(PackageMessages.ControlFlowView_NextEventActionTooltip);
        nextEventAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(NEXT_EVENT_ICON_PATH));
        manager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, nextEventAction);
    }

    private IAction getOptimizationAction() {
        if (fOptimizationAction == null) {
            fOptimizationAction = new OptimizationAction();
            fOptimizationAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(OPTIMIZE_ICON));
            fOptimizationAction.setText(Messages.ControlFlowView_optimizeLabel);
            fOptimizationAction.setToolTipText(Messages.ControlFlowView_optimizeToolTip);
        }
        return fOptimizationAction;
    }

    @Override
    protected void fillLocalMenu(IMenuManager manager) {
        super.fillLocalMenu(manager);
        MenuManager item = new MenuManager(Messages.ControlFlowView_threadPresentation);
        fFlatAction = createFlatAction();
        item.add(fFlatAction);

        fHierarchicalAction = createHierarchicalAction();
        item.add(fHierarchicalAction);
        manager.add(item);

        item = new MenuManager(PackageMessages.ControlFlowView_DynamicFiltersMenuLabel);
        item.add(fActiveThreadsRapidToggle);
        item.add(new Separator());

        IAction dynamicFiltersConfigureAction = createDynamicFilterConfigureAction();
        item.add(dynamicFiltersConfigureAction);

        manager.add(item);
    }

    /**
     * Base Action for the "Go to Next/Previous Event for thread" actions
     */
    private class SearchEventAction extends Action {

        private final boolean ifDirection;
        private final String ifJobName;

        /**
         * Constructor
         *
         * @param direction
         *            The direction of the search, "true" for forwards and
         *            "false" for backwards.
         * @param jobName
         *            The name of the job that will be spawned
         */
        public SearchEventAction(boolean direction, String jobName) {
            ifDirection = direction;
            ifJobName = jobName;
        }

        @Override
        public void run() {
            Job job = new Job(ifJobName) {
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    TimeGraphControl ctrl = getTimeGraphViewer().getTimeGraphControl();
                    ITimeGraphEntry traceEntry = ctrl.getSelectedTrace();

                    long ts = getTimeGraphViewer().getSelectionBegin();
                    ITimeEvent selectedState = Utils.findEvent(traceEntry, ts, 0);

                    if (selectedState == null) {
                        /* No selection currently in the view, do nothing */
                        return Status.OK_STATUS;
                    }
                    ITimeGraphEntry entry = selectedState.getEntry();
                    ThreadEntryModel entryModel = ControlFlowView.getThreadEntryModel(entry);
                    if (entryModel == null) {
                        return Status.OK_STATUS;
                    }
                    int tid = entryModel.getThreadId();

                    ITmfTrace trace = getTrace((TimeGraphEntry) entry);
                    ITmfContext ctx = trace.seekEvent(TmfTimestamp.fromNanos(ts));
                    long rank = ctx.getRank();
                    ctx.dispose();

                    /*
                     * TODO Specific to the Control Flow View and kernel traces for now. Could be
                     * eventually generalized to anything represented by the time graph row.
                     */
                    Predicate<@NonNull ITmfEvent> predicate = event -> Objects.equals(tid, KernelTidAspect.INSTANCE.resolve(event));

                    ITmfEvent event = (ifDirection ?
                            TmfTraceUtils.getNextEventMatching(trace, rank, predicate, monitor) :
                            TmfTraceUtils.getPreviousEventMatching(trace, rank, predicate, monitor));
                    if (event != null) {
                        TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(this, event.getTimestamp(), event.getTimestamp(), getTrace()));
                    }
                    return Status.OK_STATUS;

                }
            };
            /*
             * Make subsequent jobs not run concurrently, but wait after one
             * another.
             */
            job.setRule(fSearchActionMutexRule);
            job.schedule();
        }
    }

    private IAction createDynamicFilterConfigureAction() {
        return new Action(PackageMessages.ControlFlowView_DynamicFiltersConfigureLabel, IAction.AS_PUSH_BUTTON) {
            @Override
            public void run() {
                DynamicFilterDialog dialog = new DynamicFilterDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), fActiveThreadsFilter, getTrace());
                if (dialog.open() == Window.OK) {
                    /* Remove the previous Active Threads filter */
                    checkNotNull(getTimeGraphViewer()).removeFilter(fActiveThreadsFilter);

                    ActiveThreadsFilter newFilter = dialog.getActiveThreadsResult();
                    ActiveThreadsFilter previousFilter = fActiveThreadsFilter;

                    /* Set the filter to the view */
                    fActiveThreadsFilter = newFilter;

                    boolean enabled = fActiveThreadsFilter.isEnabled();
                    if (enabled) {
                        checkNotNull(getTimeGraphViewer()).addFilter(newFilter);
                    }

                    /*
                     * Prevent double refresh from change state of setChecked
                     * and ensure that a refresh is done if the mode of the
                     * filter is changed or options are changed
                     */
                    if (previousFilter.isEnabled() && newFilter.isEnabled()) {
                        boolean changed = !Objects.equals(previousFilter.getCpuRanges(), newFilter.getCpuRanges()) || previousFilter.isCpuRangesBased() != newFilter.isCpuRangesBased();
                        if (changed) {
                            refresh();
                        }
                    } else {
                        fActiveThreadsRapidToggle.setChecked(enabled);
                    }
                }
            }
        };
    }

    private IAction createHierarchicalAction() {
        IAction action = new Action(Messages.ControlFlowView_hierarchicalViewLabel, IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                ITmfTrace parentTrace = getTrace();
                synchronized (fFlatTraces) {
                    fFlatTraces.remove(parentTrace);
                    List<@NonNull TimeGraphEntry> entryList = getEntryList(parentTrace);
                    if (entryList != null) {
                        for (TimeGraphEntry traceEntry : entryList) {
                            Collection<TimeGraphEntry> controlFlowEntries = fEntries.row(getProvider(traceEntry)).values();
                            controlFlowEntries.forEach(e -> e.setParent(null));
                            addEntriesToHierarchicalTree(controlFlowEntries, traceEntry);
                        }
                    }
                }
                refresh();
            }
        };
        action.setChecked(true);
        action.setToolTipText(Messages.ControlFlowView_hierarchicalViewToolTip);
        return action;
    }

    private IAction createFlatAction() {
        IAction action = new Action(Messages.ControlFlowView_flatViewLabel, IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                applyFlatPresentation();
                refresh();
            }
        };
        action.setChecked(true);
        action.setToolTipText(Messages.ControlFlowView_flatViewToolTip);
        return action;
    }

    private void applyFlatPresentation() {
        ITmfTrace parentTrace = getTrace();
        synchronized (fFlatTraces) {
            fFlatTraces.add(parentTrace);
            List<@NonNull TimeGraphEntry> entryList = getEntryList(parentTrace);
            if (entryList != null) {
                for (TimeGraphEntry traceEntry : entryList) {
                    Collection<TimeGraphEntry> entries = fEntries.row(getProvider(traceEntry)).values();
                    addEntriesToFlatTree(entries, traceEntry);
                }
            }
        }
    }

    @Override
    protected String getNextText() {
        return Messages.ControlFlowView_nextProcessActionNameText;
    }

    @Override
    protected String getNextTooltip() {
        return Messages.ControlFlowView_nextProcessActionToolTipText;
    }

    @Override
    protected String getPrevText() {
        return Messages.ControlFlowView_previousProcessActionNameText;
    }

    @Override
    protected String getPrevTooltip() {
        return Messages.ControlFlowView_previousProcessActionToolTipText;
    }

    /**
     * Get the optimization function for the scheduling column. In the base
     * implementation, this optimizes by Line arrows, but can be overidden.
     * <p>
     * It takes a collection of link events, looking at the entries being
     * linked, and returns a list of the proposed order. The list of indexes
     * should be in ascending order. There can be duplicates, but the values and
     * order should always be the same for the same input.
     *
     * @return the returned column order, where the integer is the tid of the
     *         entry, and the return value is the position, there can be
     *         duplicates.
     */
    public Function<Collection<ILinkEvent>, Map<Integer, Long>> getUpdatedSchedulingColumn() {
        return UPDATE_SCHEDULING_COLUMN_ALGO;
    }

    /**
     * This is an optimization action used to find cliques of entries due to
     * links and put them closer together
     *
     * @author Samuel Gagnon
     */
    private final class OptimizationAction extends Action {

        @Override
        public void runWithEvent(Event event) {
            ITmfTrace parentTrace = getTrace();
            if (parentTrace == null) {
                return;
            }

            createFlatAction().run();

            /*
             * This method only returns the arrows in the current time interval
             * [a,b] of ControlFlowView. Thus, we only optimize for that time
             * interval
             */
            List<ILinkEvent> arrows = getTimeGraphViewer().getTimeGraphControl().getArrows();
            List<TimeGraphEntry> currentList = getEntryList(parentTrace);
            if (currentList == null) {
                return;
            }

            Map<Integer, Long> orderedTidMap = getUpdatedSchedulingColumn().apply(arrows);

            /*
             * Now that we have our list of ordered tid, it's time to assign a position for
             * each threads in the view. For this, we assign a value to an invisible column
             * and sort according to the values in this column.
             */
            Map<ITimeGraphEntry, Long> schedulingPositions = new HashMap<>();
            synchronized (fEntries) {
                for (TimeGraphEntry entry : currentList) {
                    Collection<TimeGraphEntry> controlFlowEntries = fEntries.row(getProvider(entry)).values();
                    for (TimeGraphEntry child : controlFlowEntries) {
                        /*
                         * If the thread is in our list, we give it a position. Otherwise, it means
                         * there's no activity in the current interval for that thread. We set its
                         * position to Long.MAX_VALUE so it goes to the bottom.
                         */
                        ITmfTreeDataModel entryModel = child.getEntryModel();
                        if (entryModel instanceof ThreadEntryModel) {
                            schedulingPositions.put(child, orderedTidMap.getOrDefault(((ThreadEntryModel) entryModel).getThreadId(), Long.MAX_VALUE));
                        }
                    }
                }
            }

            setEntryComparator(ControlFlowColumnComparators.newSchedulingComparator(schedulingPositions));
            refresh();
        }

    }

    /**
     * @author gbastien
     *
     */
    protected static class ControlFlowTreeLabelProvider extends TreeLabelProvider {

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (!(element instanceof TimeGraphEntry)) {
                return ""; //$NON-NLS-1$
            }
            TimeGraphEntry entry = (TimeGraphEntry) element;

            if (columnIndex == 0) {
                return entry.getName();
            }

            ITmfTreeDataModel entryModel = entry.getEntryModel();
            if (entryModel instanceof ThreadEntryModel) {
                ThreadEntryModel model = (ThreadEntryModel) entryModel;

                if (COLUMN_NAMES[columnIndex].equals(OsStrings.tid())) {
                    return Integer.toString(model.getThreadId());
                } else if (COLUMN_NAMES[columnIndex].equals(OsStrings.ptid())) {
                    if (model.getParentThreadId() > 0) {
                        return Integer.toString(model.getParentThreadId());
                    }
                } else if (COLUMN_NAMES[columnIndex].equals(Messages.ControlFlowView_birthTimeColumn)) {
                    return FormatTimeUtils.formatTime(entry.getStartTime(), TimeFormat.CALENDAR, Resolution.NANOSEC);
                } else if (COLUMN_NAMES[columnIndex].equals(Messages.ControlFlowView_traceColumn)) {
                    return getTrace(entry).getName();
                } else if (COLUMN_NAMES[columnIndex].equals(OsStrings.pid())) {
                    return Integer.toString(model.getProcessId());
                }
            }
            return ""; //$NON-NLS-1$
        }

    }

    private static class ControlFlowFilterLabelProvider extends TreeLabelProvider {

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (!(element instanceof TimeGraphEntry)) {
                return ""; //$NON-NLS-1$
            }
            TimeGraphEntry entry = (TimeGraphEntry) element;
            if (columnIndex == 0) {
                return entry.getName();
            } else if (columnIndex == 1) {
                ITmfTreeDataModel entryModel = entry.getEntryModel();
                if (entryModel instanceof ThreadEntryModel) {
                    return Integer.toString(((ThreadEntryModel) entryModel).getThreadId());
                }
            } else if (columnIndex == 2) {
                ITmfTreeDataModel entryModel = entry.getEntryModel();
                if (entryModel instanceof ThreadEntryModel) {
                    return Integer.toString(((ThreadEntryModel) entryModel).getProcessId());
                }
            }
            return ""; //$NON-NLS-1$
        }

        @Override
        public String getText(Object element) {
            if (!(element instanceof TimeGraphEntry)) {
                return String.valueOf(element);
            }
            TimeGraphEntry entry = (TimeGraphEntry) element;
            ITmfTreeDataModel entryModel = entry.getEntryModel();
            if (entryModel instanceof ThreadEntryModel) {
                return entry.getName() + ' ' + Integer.toString(((ThreadEntryModel) entryModel).getThreadId()) + ' ' + Integer.toString(((ThreadEntryModel) entryModel).getProcessId());
            }
            return entry.getName();
        }

    }

    @TmfSignalHandler
    @Override
    public void traceClosed(TmfTraceClosedSignal signal) {
        ITmfTrace parentTrace = signal.getTrace();
        super.traceClosed(signal);
        synchronized (fFlatTraces) {
            fFlatTraces.remove(parentTrace);
        }
    }

    @TmfSignalHandler
    @Override
    public void traceSelected(TmfTraceSelectedSignal signal) {
        super.traceSelected(signal);

        /* Update the Flat and Hierarchical actions */
        synchronized (fFlatTraces) {
            if (fFlatTraces.contains(signal.getTrace())) {
                fHierarchicalAction.setChecked(false);
                fFlatAction.setChecked(true);
            } else {
                fFlatAction.setChecked(false);
                fHierarchicalAction.setChecked(true);
            }
        }

        /* Update the Dynamic Filters related actions */
        ViewerFilter activeThreadFilter = null;
        ViewerFilter[] traceFilters = getFiltersMap().get(signal.getTrace());
        if (traceFilters != null) {
            activeThreadFilter = getActiveThreadsFilter(traceFilters);
        }

        if (activeThreadFilter instanceof ActiveThreadsFilter) {
            fActiveThreadsFilter = (ActiveThreadsFilter) activeThreadFilter;
        } else {
            fActiveThreadsFilter = new ActiveThreadsFilter(null, false, getTrace());
        }

        fActiveThreadsRapidToggle.setChecked(fActiveThreadsFilter.isEnabled());
    }

    // ------------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------------

    @Override
    protected void buildEntryList(final ITmfTrace trace, final ITmfTrace parentTrace, final IProgressMonitor monitor) {
        super.buildEntryList(trace, parentTrace, monitor);

        // By default is a hierarchical presentation, if flat is requested, update
        if (fFlatTraces.contains(parentTrace)) {
            applyFlatPresentation();
        }
    }

    /**
     * Add entries to the traces's child list in a flat fashion (no hierarchy).
     */
    private static void addEntriesToFlatTree(Collection<@NonNull TimeGraphEntry> entries, TimeGraphEntry traceEntry) {
        traceEntry.clearChildren();
        for (TimeGraphEntry e : entries) {
            // the main entry is in the list, ignore it
            if (traceEntry == e) {
                continue;
            }
            // reset the entries
            e.setParent(null);
            e.clearChildren();
            traceEntry.addChild(e);
        }
    }

    /**
     * Add entries to the trace's child list in a hierarchical fashion.
     */
    private static void addEntriesToHierarchicalTree(Iterable<TimeGraphEntry> entryList, TimeGraphEntry traceEntry) {
        traceEntry.clearChildren();
        Map<Long, TimeGraphEntry> map = Maps.uniqueIndex(entryList, entry -> entry.getEntryModel().getId());
        for (TimeGraphEntry e : entryList) {
            // reset children tree prior to rebuild
            e.clearChildren();
            e.setParent(null);
        }
        for (TimeGraphEntry entry : entryList) {
            // the main entry is in the list, ignore it
            if (entry == traceEntry) {
                continue;
            }
            TimeGraphEntry parent = map.get(entry.getEntryModel().getParentId());
            /*
             * Associate the parent entry only if their time overlap. A child entry may
             * start before its parent, for example at the beginning of the trace if a
             * parent has not yet appeared in the state system. We just want to make sure
             * that the entry didn't start after the parent ended or ended before the parent
             * started.
             */
            if (parent != null) {
                parent.addChild(entry);
            } else {
                traceEntry.addChild(entry);
            }
        }
    }

    @Override
    protected void synchingToTime(long time) {
        List<TimeGraphEntry> traceEntries = getEntryList(getTrace());
        if (traceEntries == null) {
            return;
        }
        for (TraceEntry traceEntry : Iterables.filter(traceEntries, TraceEntry.class)) {
            Iterable<TimeGraphEntry> unfiltered = Utils.flatten(traceEntry);
            Map<Long, TimeGraphEntry> map = Maps.uniqueIndex(unfiltered, e -> e.getEntryModel().getId());
            // use time -1 as a lower bound for the end of Time events to be included.
            SelectionTimeQueryFilter filter = new SelectionTimeQueryFilter(time - 1, time, 2, map.keySet());
            TmfModelResponse<@NonNull TimeGraphModel> response = traceEntry.getProvider().fetchRowModel(FetchParametersUtils.selectionTimeQueryToMap(filter), null);
            TimeGraphModel model = response.getModel();
            if (model == null) {
                continue;
            }
            for (ITimeGraphRowModel row : model.getRows()) {
                if (syncToRow(row, time, map)) {
                    return;
                }
            }
        }
    }

    @Override
    protected void zoomEntries(@NonNull Iterable<@NonNull TimeGraphEntry> entries, long zoomStartTime, long zoomEndTime, long resolution, @NonNull IProgressMonitor monitor) {
        super.zoomEntries(entries, zoomStartTime, zoomEndTime, resolution, monitor);
        if (monitor.isCanceled()) {
            return;
        }
        Map<ITmfTrace, Set<Long>> data = fActiveThreadsFilter.computeData(zoomStartTime, zoomEndTime);
        if (data != null) {
            applyResults(() -> fActiveThreadsFilter.updateData(zoomStartTime, zoomEndTime, data));
        }
    }

    private boolean syncToRow(ITimeGraphRowModel rowModel, long time, Map<Long, TimeGraphEntry> entryMap) {
        long id = rowModel.getEntryID();
        List<@NonNull ITimeGraphState> list = rowModel.getStates();
        if (list.isEmpty()) {
            return false;
        }
        ITimeGraphState event = list.get(0);
        if (event.getStartTime() + event.getDuration() <= time && list.size() > 1) {
            /*
             * get the second time graph state as passing time - 1 as a first argument to
             * the filter will get the previous state, if time is the beginning of an event
             */
            event = list.get(1);
        }

        if (time == event.getStartTime()) {
            TimeGraphEntry entry = entryMap.get(id);
            if (entry != null) {
                Display.getDefault().asyncExec(() -> getTimeGraphViewer().setSelection(entry, true));
                return true;
            }
        }
        return false;
    }

    private static ActiveThreadsFilter getActiveThreadsFilter(ViewerFilter[] filters) {
        for (ViewerFilter viewerFilter : filters) {
            if ((viewerFilter instanceof ActiveThreadsFilter)) {
                return (ActiveThreadsFilter) viewerFilter;
            }
        }
        return null;
    }

    /**
     * Get the thread entry model for an entry
     *
     * @param entry
     *            The entry
     * @return The entry model for a thread or <code>null</code> if the entry
     *         does not have a thread entry model
     */
    public static @Nullable ThreadEntryModel getThreadEntryModel(ITimeGraphEntry entry) {
        if (!(entry instanceof TimeGraphEntry)) {
            return null;
        }
        ITmfTreeDataModel model = ((TimeGraphEntry) entry).getEntryModel();
        if (!(model instanceof ThreadEntryModel)) {
            return null;
        }
        return (ThreadEntryModel) model;
    }
}
