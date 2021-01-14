/*******************************************************************************
 * Copyright (c) 2010, 2016 Ericsson
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
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.parsers.wizards;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tracecompass.internal.tmf.core.parsers.custom.CustomEventAspects;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition.OutputColumn;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition.Tag;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlTrace;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.indexer.ITmfTraceIndexer;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.TmfCheckpointIndexer;
import org.eclipse.tracecompass.tmf.ui.viewers.events.TmfEventsTable;

/**
 * Output wizard page for custom XML trace parsers.
 *
 * @author Patrick Tasse
 */
public class CustomXmlParserOutputWizardPage extends WizardPage {

    private static final Image UP_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/up_button.gif"); //$NON-NLS-1$
    private static final Image DOWN_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/down_button.gif"); //$NON-NLS-1$
    private final CustomXmlParserWizard wizard;
    private CustomXmlTraceDefinition definition;
    private List<Output> outputs = new ArrayList<>();
    private Composite container;
    private SashForm sash;
    private ScrolledComposite outputsScrolledComposite;
    private Composite outputsContainer;
    private Composite tableContainer;
    private TmfEventsTable previewTable;
    private File tmpFile;

    /**
     * Constructor
     *
     * @param wizard
     *            The wizard to which this page belongs
     */
    protected CustomXmlParserOutputWizardPage(final CustomXmlParserWizard wizard) {
        super("CustomParserOutputWizardPage"); //$NON-NLS-1$
        setTitle(wizard.inputPage.getTitle());
        setDescription(Messages.CustomXmlParserOutputWizardPage_description);
        this.wizard = wizard;
        setPageComplete(false);
    }

    @Override
    public void createControl(final Composite parent) {
        container = new Composite(parent, SWT.NULL);
        container.setLayout(new GridLayout());

        sash = new SashForm(container, SWT.VERTICAL);
        sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        sash.setBackground(sash.getDisplay().getSystemColor(SWT.COLOR_GRAY));

        outputsScrolledComposite = new ScrolledComposite(sash, SWT.V_SCROLL);
        outputsScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        outputsContainer = new Composite(outputsScrolledComposite, SWT.NONE);
        final GridLayout outputsLayout = new GridLayout(4, false);
        outputsLayout.marginHeight = 10;
        outputsLayout.marginWidth = 0;
        outputsContainer.setLayout(outputsLayout);
        outputsScrolledComposite.setContent(outputsContainer);
        outputsScrolledComposite.setExpandHorizontal(true);
        outputsScrolledComposite.setExpandVertical(true);

        outputsContainer.layout();

        outputsScrolledComposite.setMinSize(outputsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, outputsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y-5);

        tableContainer = new Composite(sash, SWT.NONE);
        final GridLayout tableLayout = new GridLayout();
        tableLayout.marginHeight = 0;
        tableLayout.marginWidth = 0;
        tableContainer.setLayout(tableLayout);
        previewTable = new TmfEventsTable(tableContainer, 0, CustomEventAspects.generateAspects(new CustomXmlTraceDefinition()));
        previewTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        if (wizard.definition != null) {
            loadDefinition(wizard.definition);
        }
        setControl(container);

    }

    @Override
    public void dispose() {
        previewTable.dispose();
        super.dispose();
    }

    private void loadDefinition(final CustomTraceDefinition def) {
        for (final OutputColumn outputColumn : def.outputs) {
            final Output output = new Output(outputsContainer, outputColumn.tag, outputColumn.name);
            outputs.add(output);
        }
    }

    @Override
    public void setVisible(final boolean visible) {
        if (visible) {
            this.definition = wizard.inputPage.getDefinition();
            final List<Entry<Tag, String>> inputs = wizard.inputPage.getInputs();

            // substitute extra field name/value with extra fields tag
            Iterator<Entry<Tag, String>> iterator = inputs.iterator();
            boolean addExtraFields = false;
            while (iterator.hasNext()) {
                Entry<Tag, String> entry = iterator.next();
                if (entry.getKey().equals(Tag.EXTRA_FIELD_NAME) ||
                        entry.getKey().equals(Tag.EXTRA_FIELD_VALUE)) {
                    iterator.remove();
                    addExtraFields = true;
                }
            }
            if (addExtraFields) {
                inputs.add(new SimpleEntry<>(Tag.EXTRA_FIELDS, Tag.EXTRA_FIELDS.toString()));
            }

            // dispose outputs that have been removed in the input page
            final Iterator<Output> iter = outputs.iterator();
            while (iter.hasNext()) {
                final Output output = iter.next();
                boolean found = false;
                for (final Entry<Tag, String> input : inputs) {
                    if (output.tag.equals(input.getKey()) &&
                            output.name.equals(input.getValue())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    output.dispose();
                    iter.remove();
                }
            }

            // create outputs that have been added in the input page
            for (final Entry<Tag, String> input : inputs) {
                boolean found = false;
                for (final Output output : outputs) {
                    if (output.tag.equals(input.getKey()) &&
                            output.name.equals(input.getValue())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    outputs.add(new Output(outputsContainer, input.getKey(), input.getValue()));
                }
            }

            outputsContainer.layout();
            outputsScrolledComposite.setMinSize(outputsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, outputsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y-5);
            updatePreviewTable();
            if (sash.getSize().y > outputsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y + previewTable.getTable().getItemHeight()) {
                sash.setWeights(new int[] {outputsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y, sash.getSize().y - outputsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y});
            } else {
                sash.setWeights(new int[] {outputsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y, previewTable.getTable().getItemHeight()});
            }
            setPageComplete(true);
        } else {
            setPageComplete(false);
        }
        super.setVisible(visible);
    }

    private void moveBefore(final Output moved) {
        final int i = outputs.indexOf(moved);
        if (i > 0) {
            final Output previous = outputs.get(i-1);
            moved.enabledButton.moveAbove(previous.enabledButton);
            moved.nameLabel.moveBelow(moved.enabledButton);
            moved.upButton.moveBelow(moved.nameLabel);
            moved.downButton.moveBelow(moved.upButton);
            outputs.add(i-1, outputs.remove(i));
            outputsContainer.layout();
            outputsScrolledComposite.setMinSize(outputsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, outputsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y-5);
            container.layout();
            updatePreviewTable();
        }
    }

    private void moveAfter(final Output moved) {
        final int i = outputs.indexOf(moved);
        if (i+1 < outputs.size()) {
            final Output next = outputs.get(i+1);
            moved.enabledButton.moveBelow(next.downButton);
            moved.nameLabel.moveBelow(moved.enabledButton);
            moved.upButton.moveBelow(moved.nameLabel);
            moved.downButton.moveBelow(moved.upButton);
            outputs.add(i+1, outputs.remove(i));
            outputsContainer.layout();
            outputsScrolledComposite.setMinSize(outputsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, outputsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y-5);
            container.layout();
            updatePreviewTable();
        }
    }

    private void updatePreviewTable() {
        final int CACHE_SIZE = 50;
        definition.outputs = extractOutputs();
        tmpFile = Activator.getDefault().getStateLocation().addTrailingSeparator().append("customwizard.tmp").toFile(); //$NON-NLS-1$

        try (final FileWriter writer = new FileWriter(tmpFile);) {
            writer.write(wizard.inputPage.getInputText());
        } catch (final IOException e) {
            Activator.getDefault().logError("Error creating CustomXmlTrace. File:" + tmpFile.getAbsolutePath(), e); //$NON-NLS-1$
        }

        try {
            final CustomXmlTrace trace = new CustomXmlTrace(null, definition, tmpFile.getAbsolutePath(), CACHE_SIZE) {
                @Override
                protected ITmfTraceIndexer createIndexer(int interval) {
                    return new TmfCheckpointIndexer(this, interval);
                }
            };
            trace.getIndexer().buildIndex(0, TmfTimeRange.ETERNITY, false);
            previewTable.dispose();
            previewTable = new TmfEventsTable(tableContainer, CACHE_SIZE, CustomEventAspects.generateAspects(definition));
            previewTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            previewTable.setTrace(trace, true);
        } catch (final TmfTraceException e) {
            Activator.getDefault().logError("Error creating CustomXmlTrace. File:" + tmpFile.getAbsolutePath(), e); //$NON-NLS-1$
        }

        tableContainer.layout();
        container.layout();
    }

    /**
     * Extract the output columns from the page's current contents.
     *
     * @return The list of output columns
     */
    public List<OutputColumn> extractOutputs() {
        final List<OutputColumn> outputColumns = new ArrayList<>();
        for (Output output : outputs) {
            if (output.enabledButton.getSelection()) {
                final OutputColumn column = new OutputColumn(checkNotNull(output.tag), checkNotNull(output.name));
                outputColumns.add(column);
            }
        }
        return outputColumns;
    }

    private class Output {
        Tag tag;
        String name;
        Button enabledButton;
        Text nameLabel;
        Button upButton;
        Button downButton;

        public Output(final Composite parent, final Tag tag, final String name) {
            this.tag = tag;
            this.name = name;

            enabledButton = new Button(parent, SWT.CHECK);
            enabledButton.setToolTipText(Messages.CustomXmlParserOutputWizardPage_visible);
            enabledButton.setSelection(true);
            enabledButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(final SelectionEvent e) {
                    updatePreviewTable();
                }
            });
            //            if (messageOutput != null) {
            //                enabledButton.moveAbove(messageOutput.enabledButton);
            //            }

            nameLabel = new Text(parent, SWT.BORDER | SWT.READ_ONLY | SWT.SINGLE);
            nameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
            nameLabel.setText(name);
            nameLabel.moveBelow(enabledButton);

            upButton = new Button(parent, SWT.PUSH);
            upButton.setImage(UP_IMAGE);
            upButton.setToolTipText(Messages.CustomXmlParserOutputWizardPage_moveBefore);
            upButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(final SelectionEvent e) {
                    moveBefore(Output.this);
                }
            });
            upButton.moveBelow(nameLabel);

            downButton = new Button(parent, SWT.PUSH);
            downButton.setImage(DOWN_IMAGE);
            downButton.setToolTipText(Messages.CustomXmlParserOutputWizardPage_moveAfter);
            downButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(final SelectionEvent e) {
                    moveAfter(Output.this);
                }
            });
            downButton.moveBelow(upButton);
        }

        private void dispose() {
            enabledButton.dispose();
            nameLabel.dispose();
            upButton.dispose();
            downButton.dispose();
        }
    }

    /**
     * Get the trace definition.
     *
     * @return The trace definition
     */
    public CustomXmlTraceDefinition getDefinition() {
        return definition;
    }

}
