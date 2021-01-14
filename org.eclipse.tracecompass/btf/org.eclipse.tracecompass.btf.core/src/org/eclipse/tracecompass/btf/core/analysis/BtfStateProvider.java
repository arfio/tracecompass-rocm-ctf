/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Matthew Khouzam - Add the task state system from figure 3 from the spec
 *******************************************************************************/

package org.eclipse.tracecompass.btf.core.analysis;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.btf.core.event.BtfEvent;
import org.eclipse.tracecompass.btf.core.trace.BtfColumnNames;
import org.eclipse.tracecompass.btf.core.trace.BtfTrace;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;

/**
 * State provider for the default BTF analysis.
 *
 * The generated attribute tree will look like this:
 *
 * <pre>
 * {root}
 *   +- Cores
 *   |   +- Core 1            (not running/Name of the running Task)
 *   |   +- Core 2            (not running/Name of the running Task)
 *   |
 *   +- Tasks
 *       +- Task A
 *       |   +- Core 1             (not running/running/suspended)
 *       |   |    +- Runnable A1   (not running/running/suspended)
 *       |   |    +- Runnable A2   (not running/running/suspended)
 *       |   +- Core 2
 *       |   |    +- Runnable A1
 *       |   |    +- Runnable A2
 *       |   +- ActiveCore
 *       |
 *       +- Task B
 *           +- Core 1
 *           |    +- Runnable B1
 *           |    +- Runnable B2
 *           +- Core 2
 *           |    +- Runnable B1
 *           |    +- Runnable B2
 *           +- ActiveCore
 * </pre>
 *
 * @author Alexandre Montplaisir
 */
public class BtfStateProvider extends AbstractTmfStateProvider {

    private static final int PROVIDER_VERSION = 3;

    private static class TmfNamedStateValue {
        private final String fName;
        private final @NonNull TmfStateValue fValue;

        public TmfNamedStateValue(@NonNull TmfStateValue value, String name) {
            fValue = value;
            fName = name;
        }

        public @NonNull TmfStateValue getValue() {
            return fValue;
        }

        @Override
        public String toString() {
            return fName;
        }
    }

    private static final TmfNamedStateValue STATE_CORE_IDLE = new TmfNamedStateValue(TmfStateValue.newValueInt(0), "Idle"); //$NON-NLS-1$

    private static final TmfNamedStateValue STATE_NOT_RUNNING = new TmfNamedStateValue(TmfStateValue.nullValue(), "Not Running"); //$NON-NLS-1$
    private static final TmfNamedStateValue STATE_RUNNING = new TmfNamedStateValue(TmfStateValue.newValueInt((1)), "RUNNING"); //$NON-NLS-1$
    private static final TmfNamedStateValue STATE_SUSPENDED = new TmfNamedStateValue(TmfStateValue.newValueInt((2)), "SUSPENDED"); //$NON-NLS-1$

    private static final TmfNamedStateValue STATE_TASK_ACTIVE = new TmfNamedStateValue(TmfStateValue.newValueInt((4)), "Active"); //$NON-NLS-1$
    private static final TmfNamedStateValue STATE_TASK_READY = new TmfNamedStateValue(TmfStateValue.newValueInt((5)), "Ready"); //$NON-NLS-1$
    private static final TmfNamedStateValue STATE_TASK_RUNNING = new TmfNamedStateValue(TmfStateValue.newValueInt((6)), "Task Running"); //$NON-NLS-1$
    private static final TmfNamedStateValue STATE_TASK_WAITING = new TmfNamedStateValue(TmfStateValue.newValueInt((7)), "Waiting"); //$NON-NLS-1$
    private static final TmfNamedStateValue STATE_TASK_PARKING = new TmfNamedStateValue(TmfStateValue.newValueInt((8)), "Parking"); //$NON-NLS-1$
    private static final TmfNamedStateValue STATE_TASK_POLLING = new TmfNamedStateValue(TmfStateValue.newValueInt((9)), "Polling"); //$NON-NLS-1$
    private static final TmfNamedStateValue STATE_TASK_TERMINATED = new TmfNamedStateValue(TmfStateValue.nullValue(), "Terminated"); //$NON-NLS-1$

    private static final String ENTITY_CORE = "Core"; //$NON-NLS-1$
    private static final String ENTITY_TASK = "TASK"; //$NON-NLS-1$
    // private static final String ENTITY_RUNNABLE = "RUNNABLE";

    private static final String ATTRIBUTE_TASKS = "Tasks"; //$NON-NLS-1$
    private static final String ATTRIBUTE_CORES = "Cores"; //$NON-NLS-1$
    private static final String ATTRIBUTE_ACTIVE_CORE = "ActiveCore"; //$NON-NLS-1$

    /**
     * Constructor
     *
     * @param trace
     *            The trace for which we will be building this state system
     */
    public BtfStateProvider(BtfTrace trace) {
        super(trace, "Btf State Provider"); //$NON-NLS-1$
    }

    @Override
    public BtfTrace getTrace() {
        return (BtfTrace) super.getTrace();
    }

    @Override
    public int getVersion() {
        return PROVIDER_VERSION;
    }

    @Override
    public ITmfStateProvider getNewInstance() {
        return new BtfStateProvider(getTrace());
    }

    @Override
    protected void eventHandle(ITmfEvent ev) {
        if (!(ev instanceof BtfEvent)) {
            return;
        }

        BtfEvent event = (BtfEvent) ev;
        final ITmfStateSystemBuilder ssb = checkNotNull(getStateSystemBuilder());

        final long ts = event.getTimestamp().getValue();
        final String eventType = (String) event.getContent().getField(BtfColumnNames.EVENT.toString()).getValue();
        final String source = event.getSource();
        final String target = event.getTarget();
        String task;
        int quark;
        try {
            switch (eventType) {

            case "activate": //$NON-NLS-1$
                updateTaskStateSystem(ssb, ts, event, STATE_TASK_ACTIVE);
                break;

            case "start": //$NON-NLS-1$
            case "resume": //$NON-NLS-1$
                updateTaskStateSystem(ssb, ts, event, STATE_TASK_RUNNING);

                if (source.startsWith(ENTITY_CORE)) {
                    String core = source;
                    task = target;

                    quark = ssb.getQuarkAbsoluteAndAdd(ATTRIBUTE_TASKS, task, core);
                    ssb.modifyAttribute(ts, STATE_RUNNING.getValue().unboxValue(), quark);

                    /* Mark this task as active in the ActiveCore attribute */
                    quark = ssb.getQuarkAbsoluteAndAdd(ATTRIBUTE_TASKS, task, ATTRIBUTE_ACTIVE_CORE);
                    ssb.modifyAttribute(ts, core, quark);

                    /* Mark this task as active in the Cores/* attribute */
                    quark = ssb.getQuarkAbsoluteAndAdd(ATTRIBUTE_CORES, core);
                    /* Until the view can display the string */
                    ssb.modifyAttribute(ts, STATE_RUNNING.getValue().unboxValue(), quark);

                } else if (source.startsWith(ENTITY_TASK)) {
                    task = source;
                    String runnable = target;
                    String core = getCoreOfTask(ssb, task);

                    quark = ssb.getQuarkAbsoluteAndAdd(ATTRIBUTE_TASKS, task, core, runnable);
                    ssb.modifyAttribute(ts, STATE_RUNNING.getValue().unboxValue(), quark);
                }
                break;

            case "suspend": //$NON-NLS-1$
                /* "suspend" events only happen on Tasks */
                if (source.startsWith(ENTITY_TASK)) {
                    task = source;
                    String runnable = target;
                    String core = getCoreOfTask(ssb, task);

                    /* We'll update both the Core and Runnable attributes */
                    quark = ssb.getQuarkAbsoluteAndAdd(ATTRIBUTE_TASKS, task, core);
                    ssb.modifyAttribute(ts, STATE_SUSPENDED.getValue().unboxValue(), quark);
                    quark = ssb.getQuarkRelativeAndAdd(quark, runnable);
                    ssb.modifyAttribute(ts, STATE_SUSPENDED.getValue().unboxValue(), quark);
                }
                break;

            case "terminate": //$NON-NLS-1$
                updateTaskStateSystem(ssb, ts, event, STATE_TASK_TERMINATED);

                if (source.startsWith(ENTITY_CORE)) {
                    String core = source;
                    task = target;

                    quark = ssb.getQuarkAbsoluteAndAdd(ATTRIBUTE_TASKS, task, core);
                    ssb.modifyAttribute(ts, STATE_NOT_RUNNING.getValue().unboxValue(), quark);

                    /* Remove our "active task on core" bookmark */
                    quark = ssb.getQuarkAbsoluteAndAdd(ATTRIBUTE_TASKS, task, ATTRIBUTE_ACTIVE_CORE);
                    ITmfStateValue value = TmfStateValue.nullValue();
                    ssb.modifyAttribute(ts, value.unboxValue(), quark);

                    /* Mark the Cores/* attribute as not running */
                    quark = ssb.getQuarkAbsoluteAndAdd(ATTRIBUTE_CORES, core);
                    ssb.modifyAttribute(ts, STATE_CORE_IDLE.getValue().unboxValue(), quark);

                } else if (source.startsWith(ENTITY_TASK)) {
                    task = source;
                    String runnable = target;
                    String core = getCoreOfTask(ssb, task);

                    quark = ssb.getQuarkAbsoluteAndAdd(ATTRIBUTE_TASKS, task, core, runnable);
                    ssb.modifyAttribute(ts, STATE_NOT_RUNNING.getValue().unboxValue(), quark);
                }
                break;

            case "preempt": //$NON-NLS-1$
            case "release": //$NON-NLS-1$
            case "release_parking": //$NON-NLS-1$
                updateTaskStateSystem(ssb, ts, event, STATE_TASK_READY);
                break;
            case "wait": //$NON-NLS-1$
                updateTaskStateSystem(ssb, ts, event, STATE_TASK_WAITING);
                break;
            case "park": //$NON-NLS-1$
                updateTaskStateSystem(ssb, ts, event, STATE_TASK_PARKING);
                break;
            case "poll": //$NON-NLS-1$
                //$FALL-THROUGH$
            case "poll_parking": //$NON-NLS-1$
                updateTaskStateSystem(ssb, ts, event, STATE_TASK_POLLING);
                break;
            case "run": //$NON-NLS-1$
                updateTaskStateSystem(ssb, ts, event, STATE_TASK_RUNNING);
                break;
            default:
                break;

            }
        } catch (AttributeNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void updateTaskStateSystem(
            final ITmfStateSystemBuilder ssb,
            final long ts, BtfEvent event,
            TmfNamedStateValue stateValue)
            throws AttributeNotFoundException {
        String name = event.getName();
        if (name.equals("Task")) { //$NON-NLS-1$
            String task = event.getTarget();
            int quark = ssb.getQuarkAbsoluteAndAdd(ATTRIBUTE_TASKS, task);
            ssb.modifyAttribute(ts, stateValue.getValue().unboxValue(), quark);
        }
    }

    private static String getCoreOfTask(ITmfStateSystemBuilder ssb, String task) {
        try {
            int quark = ssb.getQuarkAbsolute(ATTRIBUTE_TASKS, task, ATTRIBUTE_ACTIVE_CORE);
            ITmfStateValue value = ssb.queryOngoingState(quark);
            return value.unboxStr();
        } catch (AttributeNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}
