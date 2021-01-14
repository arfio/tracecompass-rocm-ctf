/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.synchronization;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSynchronizedSignal;
import org.eclipse.tracecompass.tmf.core.synchronization.SynchronizationAlgorithm;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;

/**
 * Small view to display statistics about a synchronization
 *
 * @author Geneviève Bastien
 */
public class TmfSynchronizationView extends TmfView {

    /**
     * The ID corresponds to the package in which this class is embedded.
     */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.synchronization"; //$NON-NLS-1$

    /**
     * The view name.
     */
    public static final String TMF_SYNCHRONIZATION_VIEW = "SynchronizationView"; //$NON-NLS-1$

    /**
     * The synchronization algorithm to display stats for
     */
    private SynchronizationAlgorithm fAlgoSync;

    private Tree fTree;

    /**
     * Default constructor
     */
    public TmfSynchronizationView() {
        super(TMF_SYNCHRONIZATION_VIEW);
    }

    @Override
    public void createPartControl(Composite parent) {
        fTree = new Tree(parent, SWT.NONE);
        TreeColumn nameCol = new TreeColumn(fTree, SWT.NONE, 0);
        TreeColumn valueCol = new TreeColumn(fTree, SWT.NONE, 1);
        nameCol.setText(Messages.TmfSynchronizationView_NameColumn);
        valueCol.setText(Messages.TmfSynchronizationView_ValueColumn);

        fTree.setItemCount(0);

        fTree.setHeaderVisible(true);
        nameCol.pack();
        valueCol.pack();

        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            traceSelected(new TmfTraceSelectedSignal(this, trace));
        }
    }

    private void updateTable() {
        fTree.setItemCount(0);
        if (fAlgoSync == null) {
            return;
        }

        for (Map.Entry<String, Map<String, Object>> entry : fAlgoSync.getStats().entrySet()) {
            TreeItem item = new TreeItem(fTree, SWT.NONE);
            item.setText(0, entry.getKey().toString());
            item.setText(1, entry.getValue().toString());

            for (Map.Entry<String, Object> subentry : entry.getValue().entrySet()) {
                TreeItem subitem = new TreeItem(item, SWT.NONE);
                subitem.setText(0, subentry.getKey().toString());
                subitem.setText(1, subentry.getValue().toString());
            }
        }

        /* Expand the tree items */
        for (int i = 0; i < fTree.getItemCount(); i++) {
            fTree.getItem(i).setExpanded(true);
        }

        for (TreeColumn column : fTree.getColumns()) {
            column.pack();
        }
    }

    @Override
    public void setFocus() {
        fTree.setFocus();
    }

    /**
     * Handler called when a trace is selected
     *
     * @param signal
     *            Contains information about the selected trace
     */
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        fAlgoSync = null;
        if (signal.getTrace() instanceof TmfExperiment) {
            fAlgoSync = ((TmfExperiment) signal.getTrace()).synchronizeTraces();
        }
        Display.getDefault().asyncExec(this::updateTable);
    }

    /**
     * Handler called when traces are synchronized
     *
     * @param signal
     *            Contains the information about the selection.
     */
    @TmfSignalHandler
    public void traceSynchronized(TmfTraceSynchronizedSignal signal) {
        if (signal.getSyncAlgo() != fAlgoSync) {
            fAlgoSync = signal.getSyncAlgo();
            Display.getDefault().asyncExec(this::updateTable);
        }
    }
}
