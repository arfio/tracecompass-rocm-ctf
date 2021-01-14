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
package org.eclipse.tracecompass.tmf.ui.tracetype.preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypePreferences;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.FilteredCheckboxTree;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.TreePatternFilter;
import org.eclipse.ui.dialogs.PatternFilter;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * This class implements a preference page viewer for the trace type
 *
 * @author Jean-Christian Kouame
 * @since 3.0
 */
public class TraceTypePreferencePageViewer {

    private static final int BUTTON_CHECK_SELECTED_ID = IDialogConstants.CLIENT_ID;
    private static final int BUTTON_UNCHECK_SELECTED_ID = IDialogConstants.CLIENT_ID + 1;
    private static final String[] FILTER_COLUMN_NAMES = new String[] {
            "Trace Types", //$NON-NLS-1$
            "Initial Range Duration" //$NON-NLS-1$
    };
    private static final String[] COLUMN_PROPERTIES = new String[] {
            "NAME", //$NON-NLS-1$
            "DURATION" //$NON-NLS-1$
    };

    private static final Set<String> EDITABLE = ImmutableSet.of(Objects.requireNonNull(FILTER_COLUMN_NAMES[1]));

    private boolean fIsEmpty;
    private FilteredCheckboxTree fTree;
    private TraceTypeTreeContentProvider fContentProvider;
    private TraceTypeLabelProvider fLabelProvider;
    private ViewerComparator fComparator;
    private List<ViewerFilter> fFilters;
    private Iterable<@NonNull TraceTypeHelper> fEntries;
    private TextCellEditor fCellEditor;

    /**
     * Constructor
     *
     * @param entries
     *            The viewer entries
     */
    public TraceTypePreferencePageViewer(Iterable<@NonNull TraceTypeHelper> entries) {
        fEntries = entries;
        fContentProvider = new TraceTypeTreeContentProvider();
        fLabelProvider = new TraceTypeLabelProvider();
    }

    /**
     * Create the filter viewer area and initialize the values
     *
     * @param parent
     *            The parent
     * @return The created area
     */
    public Composite create(Composite parent) {
        Composite composite = createFilterArea(parent);
        fIsEmpty = fEntries.iterator().hasNext();
        BusyIndicator.showWhile(null, () -> {
            Iterable<@NonNull TraceTypeHelper> toCheck = Iterables.filter(fEntries, helper -> helper.isEnabled());
            toCheck.forEach(handler -> checkElement(handler));
            fTree.getViewer().expandAll();
            for (TreeColumn column : fTree.getViewer().getTree().getColumns()) {
                column.pack();
            }
            fTree.getViewer().collapseAll();
        });
        return composite;
    }

    /**
     * Create the filter area
     *
     * @param parent
     *            The parent composite
     * @return The filter area composite
     */
    public Composite createFilterArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, true));
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        CheckboxTreeViewer treeViewer = createTreeViewer(composite);
        Control buttonComposite = createSelectionButtons(composite);
        GridData data = new GridData(GridData.FILL_BOTH);
        Tree treeWidget = treeViewer.getTree();
        treeWidget.setLayoutData(data);
        treeWidget.setFont(parent.getFont());
        if (fIsEmpty) {
            treeWidget.setEnabled(false);
            buttonComposite.setEnabled(false);
        }
        return composite;
    }

    /**
     * Creates the tree viewer.
     *
     * @param parent
     *            the parent composite
     * @return the tree viewer
     */
    protected CheckboxTreeViewer createTreeViewer(Composite parent) {
        PatternFilter filter = new TreePatternFilter();
        filter.setIncludeLeadingWildcard(true);
        fTree = new FilteredCheckboxTree(parent, SWT.BORDER | SWT.MULTI, filter, true, false);
        fTree.setLayout(new GridLayout());
        fTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        TreeViewer viewer = fTree.getViewer();
        TextCellEditor[] editors = new TextCellEditor[FILTER_COLUMN_NAMES.length];

        TreeViewerEditor.create(viewer, new ColumnViewerEditorActivationStrategy(viewer) {
            @Override
            protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
                if (event.getSource() instanceof ViewerCell) {
                    ViewerCell viewerCell = (ViewerCell) event.getSource();
                    if (editors[viewerCell.getColumnIndex()] != null) {
                        return event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION;
                    }
                }
                return false;
            }
        }, ColumnViewerEditor.DEFAULT);
        Tree tree = viewer.getTree();
        fCellEditor = new TextCellEditor(tree, SWT.BORDER);
        tree.setHeaderVisible(true);
        for (String columnName : FILTER_COLUMN_NAMES) {
            TreeColumn column = new TreeColumn(tree, SWT.LEFT);
            column.setText(columnName);
        }
        for (int i = 0; i < FILTER_COLUMN_NAMES.length; i++) {
            if (EDITABLE.contains(FILTER_COLUMN_NAMES[i])) {
                editors[i] = fCellEditor;
            }
        }
        viewer.setContentProvider(fContentProvider);
        viewer.setLabelProvider(fLabelProvider);
        viewer.setColumnProperties(COLUMN_PROPERTIES);
        viewer.setCellEditors(editors);
        viewer.setCellModifier(new ICellModifier() {

            @Override
            public void modify(Object element, String property, Object value) {
                if (element instanceof TreeItem) {
                    TreeItem treeItem = (TreeItem) element;
                    Object data = treeItem.getData();
                    if (data instanceof TraceTypeHelper) {
                        TraceTypeHelper helper = (TraceTypeHelper) data;
                        if (property.equals(COLUMN_PROPERTIES[1])) {
                            try {
                                String text = value.toString().replaceAll("\\s", ""); //$NON-NLS-1$ //$NON-NLS-2$
                                double doubleval = Double.parseDouble(text) * 1e9;
                                TraceTypePreferences.setInitialTimeRange(helper.getTraceTypeId(), (long) doubleval);
                                viewer.refresh();
                            } catch (NumberFormatException e) {
                                // ignore me
                            }
                        }

                    }
                }

            }

            @Override
            public Object getValue(Object element, String property) {
                if (element instanceof TraceTypeHelper) {
                    TraceTypeHelper helper = (TraceTypeHelper) element;
                    if (property.equals(COLUMN_PROPERTIES[1])) {
                        String traceTypeId = helper.getTraceTypeId();
                        return TmfTimestamp.fromNanos(TraceTypePreferences.getInitialTimeRange(traceTypeId, helper.getTrace().getInitialRangeOffset().toNanos())).toString(TmfTimestampFormat.getDefaulIntervalFormat());
                    }
                }
                return element;
            }

            @Override
            public boolean canModify(Object element, String property) {
                if (element instanceof TraceTypeHelper) {
                    return property.equals(COLUMN_PROPERTIES[1]);
                }
                return false;
            }
        });

        fTree.addCheckStateListener(new CheckStateListener());
        viewer.setComparator(fComparator);
        if (fFilters != null) {
            for (int i = 0; i != fFilters.size(); i++) {
                viewer.addFilter(fFilters.get(i));
            }
        }
        viewer.setInput(fEntries);
        return (CheckboxTreeViewer) viewer;
    }

    /**
     * Adds the selection and deselection buttons to the dialog.
     *
     * @param composite
     *            the parent composite
     * @return Composite the composite the buttons were created in.
     */
    protected Composite createSelectionButtons(Composite composite) {
        Composite buttonComposite = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout(2, true);
        layout.marginWidth = 0;
        buttonComposite.setLayout(layout);
        buttonComposite.setFont(composite.getFont());
        GridData data = new GridData(SWT.RIGHT, SWT.TOP, false, false);
        buttonComposite.setLayoutData(data);

        /* Create the buttons in the good order to place them as we want */
        Button checkSelectedButton = createButton(buttonComposite,
                BUTTON_CHECK_SELECTED_ID, Messages.TmfTimeFilterDialog_CHECK_SELECTED);
        Button checkAllButton = createButton(buttonComposite,
                IDialogConstants.SELECT_ALL_ID, Messages.TmfTimeFilterDialog_CHECK_ALL);
        Button uncheckSelectedButton = createButton(buttonComposite,
                BUTTON_UNCHECK_SELECTED_ID, Messages.TmfTimeFilterDialog_UNCHECK_SELECTED);
        Button uncheckAllButton = createButton(buttonComposite,
                IDialogConstants.DESELECT_ALL_ID, Messages.TmfTimeFilterDialog_UNCHECK_ALL);

        /* Add a listener to each button */
        checkSelectedButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TreeSelection selection = (TreeSelection) fTree.getViewer().getSelection();

                for (Object element : selection.toArray()) {
                    checkElement(element);
                }
            }
        });

        checkAllButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Object[] viewerElements = fContentProvider.getElements(fEntries);

                for (int i = 0; i < viewerElements.length; i++) {
                    fTree.setSubtreeChecked(viewerElements[i], true);
                }
            }
        });

        uncheckSelectedButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TreeSelection selection = (TreeSelection) fTree.getViewer().getSelection();

                for (Object element : selection.toArray()) {
                    uncheckElement(element);
                }
            }
        });

        uncheckAllButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Object[] viewerElements = fContentProvider.getElements(fEntries);
                for (Object element : viewerElements) {
                    if (fTree.getViewer().testFindItem(element) != null) {
                        // uncheck only visible roots and their children
                        uncheckElement(element);
                    }
                }
            }
        });
        return buttonComposite;
    }

    private static Button createButton(Composite parent, int id, String label) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(label);
        button.setFont(JFaceResources.getDialogFont());
        button.setData(Integer.valueOf(id));
        GridData data = new GridData(SWT.FILL, SWT.CENTER, true, true);
        button.setLayoutData(data);
        return button;
    }

    /**
     * Perform the default behavior
     */
    public void performDefaults() {
        Object input = fTree.getViewer().getInput();
        if (input instanceof Iterable) {
            ((Iterable<?>) input).forEach(this::checkElementAndSubtree);
            ((Iterable<?>) input).forEach(element -> {
                if(element instanceof TraceTypeHelper) {
                    TraceTypeHelper helper = (TraceTypeHelper) element;
                    TraceTypePreferences.resetInitialTimeRange(helper.getTraceTypeId());
                }
            });
        }
        fTree.getViewer().refresh();
        fTree.getViewer().expandAll();
    }

    /**
     * Private classes
     */

    private class CheckStateListener implements ICheckStateListener {

        @Override
        public void checkStateChanged(CheckStateChangedEvent event) {
            try {
                boolean checked = event.getChecked();
                if (checked) {
                    checkElement(event.getElement());
                } else {
                    uncheckElement(event.getElement());
                }
            } catch (ClassCastException e) {
                Activator.getDefault().logError("Failed to enable trace types", e); //$NON-NLS-1$
                return;
            }
        }
    }

    /**
     * Check an element and all its parents.
     *
     * @param element
     *            The element to check.
     */
    private void checkElement(Object element) {
        fTree.setChecked(element, true);

        Object parent = fContentProvider.getParent(element);

        while (parent != null && !fTree.getChecked(parent)) {
            fTree.setChecked(parent, true);
            parent = fContentProvider.getParent(parent);
        }

        Object[] children = fContentProvider.getChildren(element);
        if (children != null) {
            for (Object child : children) {
                checkElement(child);
            }
        }
    }

    /**
     * Check an element, all its parents and all its children.
     *
     * @param element
     *            The element to check.
     */
    private void checkElementAndSubtree(Object element) {
        checkElement(element);

        for (Object child : fContentProvider.getChildren(element)) {
            checkElementAndSubtree(child);
        }
    }

    /**
     * Uncheck an element and all its children.
     *
     * @param element
     *            The element to uncheck.
     */
    private void uncheckElement(Object element) {
        fTree.setChecked(element, false);

        for (Object child : fContentProvider.getChildren(element)) {
            uncheckElement(child);
        }

        Object parent = fContentProvider.getParent(element);

        while (parent != null && !hasCheckedChild(parent)) {
            fTree.setChecked(parent, false);
            parent = fContentProvider.getParent(parent);
        }
    }

    private boolean hasCheckedChild(Object parent) {
        TraceTypeHelper[] children = fContentProvider.getChildren(parent);
        for (Object child : children) {
            if (fTree.getChecked(child)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the list of checked elements
     *
     * @return The checked elements
     */
    public List<TraceTypeHelper> getCheckedElements() {
        List<TraceTypeHelper> checked = new ArrayList<>();
        Object[] checkedElements = fTree.getCheckedElements();
        for (Object element : checkedElements) {
            if (element instanceof TraceTypeHelper) {
                checked.add((TraceTypeHelper) element);
            }
        }
        return checked;
    }

    /**
     * Get the list of unchecked elements
     *
     * @return The unchecked elements
     */
    public List<TraceTypeHelper> getUncheckedElements() {
        List<TraceTypeHelper> unchecked = getEntries();
        unchecked.removeAll(getCheckedElements());
        return unchecked;
    }

    /**
     * Return a copy of the entries
     *
     * @return The copy of the entries
     */
    private List<TraceTypeHelper> getEntries() {
        return Lists.newArrayList(fEntries);
    }

    /**
     * Sets the comparator used by the tree viewer.
     *
     * @param comparator
     *            The comparator
     */
    public void setComparator(ViewerComparator comparator) {
        fComparator = comparator;
    }
}
