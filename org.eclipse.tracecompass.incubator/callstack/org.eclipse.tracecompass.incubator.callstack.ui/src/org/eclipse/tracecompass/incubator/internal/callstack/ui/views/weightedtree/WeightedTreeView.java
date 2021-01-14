/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.incubator.internal.callstack.ui.views.weightedtree;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.incubator.analysis.core.weighted.tree.IWeightedTreeProvider;
import org.eclipse.tracecompass.incubator.analysis.core.weighted.tree.WeightedTree;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.AbstractTmfTreeViewer;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;

/**
 * Base view that shows a weighted tree
 *
 * @author Geneviève Bastien
 */
public class WeightedTreeView extends TmfView {

    /**
     * The ID of this view
     */
    public static final String ID = "org.eclipse.tracecompass.incubator.callstack.ui.views.weightedtree"; //$NON-NLS-1$

    private @Nullable AbstractTmfTreeViewer fWeightedTreeViewer = null;
    private @Nullable WeightedTreePieChartViewer fPieChartViewer = null;
    private @Nullable String fAnalysisId = null;

    private @Nullable SashForm fSash = null;
    private @Nullable Set<WeightedTree<?>> fSelection = null;
    private @Nullable IWeightedTreeProvider<?, ?, WeightedTree<?>> fSelectedTreeProvider = null;

    /**
     * Constructor
     */
    public WeightedTreeView() {
        super("WeightedTreeView"); //$NON-NLS-1$
    }

    @Override
    public void createPartControl(@Nullable Composite parent) {
        super.createPartControl(parent);
        String analysisId = NonNullUtils.nullToEmptyString(getViewSite().getSecondaryId());
        fAnalysisId = analysisId;
        fSash  = new SashForm(parent, SWT.HORIZONTAL);
        // Build the tree viewer
        AbstractTmfTreeViewer weightedTreeViewer = new WeightedTreeViewer(fSash, this);
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            weightedTreeViewer.loadTrace(trace);
        }
        fWeightedTreeViewer = weightedTreeViewer;

        // Build the pie chart viewer
        WeightedTreePieChartViewer pieChartViewer = new WeightedTreePieChartViewer(fSash, this);
        if (trace != null) {
            pieChartViewer.loadTrace(trace);
        }
        pieChartViewer.addSelectionListener(new Listener() {

            @Override
            public void handleEvent(@Nullable Event event) {
                if (event == null) {
                    return;
                }
                Set<WeightedTree<?>> selection = fSelection;
                IWeightedTreeProvider<?, ?, WeightedTree<?>> provider = fSelectedTreeProvider;
                if (selection == null || provider == null) {
                    return;
                }
                String selectedText = event.text;
                WeightedTree<?> selectedTree = null;
                for (WeightedTree<?> tree : selection) {
                    if (provider.toDisplayString(tree).equals(selectedText)) {
                        selectedTree = tree;
                        break;
                    }
                }
                if (selectedTree == null) {
                    return;
                }
                if (selectedTree.getChildren().isEmpty()) {
                    return;
                }
//                pieChartViewer.secondarySelection(selectedTree.getChildren(), provider);
            }

        });
        fPieChartViewer = pieChartViewer;
    }

    Set<IWeightedTreeProvider<?, ?, WeightedTree<?>>> getWeightedTrees(ITmfTrace trace) {
        String analysisId = fAnalysisId;
        if (analysisId == null) {
            return Collections.emptySet();
        }
        Iterable<IWeightedTreeProvider> callgraphModules = TmfTraceUtils.getAnalysisModulesOfClass(trace, IWeightedTreeProvider.class);

        Set<IWeightedTreeProvider<?, ?, WeightedTree<?>>> set = new HashSet<>();
        for (IWeightedTreeProvider<?, ?, WeightedTree<?>> treeProvider : callgraphModules) {
            if (treeProvider instanceof IAnalysisModule) {
                if (((IAnalysisModule) treeProvider).getId().equals(analysisId)) {
                    set.add(treeProvider);
                }
            }
        }
        return set;
    }

    @Override
    public void setFocus() {
        AbstractTmfTreeViewer treeViewer = fWeightedTreeViewer;
        if (treeViewer != null) {
            treeViewer.getControl().setFocus();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        AbstractTmfTreeViewer treeViewer = fWeightedTreeViewer;
        if (treeViewer != null) {
            treeViewer.dispose();
        }
        WeightedTreePieChartViewer pieChartViewer = fPieChartViewer;
        if (pieChartViewer != null) {
            pieChartViewer.dispose();
        }
    }

    /**
     * Dispatches the selection to the viewers
     *
     * @param trees The selected trees
     * @param treeProvider The tree provider for the selected trees
     */
    public void elementSelected(Set<WeightedTree<?>> trees, IWeightedTreeProvider<?, ?, WeightedTree<?>> treeProvider) {
        WeightedTreePieChartViewer pieChartViewer = fPieChartViewer;
        fSelection  = trees;
        fSelectedTreeProvider  = treeProvider;
        if (pieChartViewer != null) {
            pieChartViewer.elementSelected(trees, treeProvider);
        }
    }

}
