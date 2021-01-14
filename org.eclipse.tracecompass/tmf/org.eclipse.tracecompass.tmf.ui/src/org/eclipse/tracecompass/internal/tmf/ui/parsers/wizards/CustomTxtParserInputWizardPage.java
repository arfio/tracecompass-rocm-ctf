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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition.Tag;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtTraceDefinition;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtTraceDefinition.Cardinality;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtTraceDefinition.InputData;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtTraceDefinition.InputLine;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;
import org.osgi.framework.Bundle;

/**
 * Input wizard page for custom text parsers.
 *
 * @author Patrick Tasse
 */
public class CustomTxtParserInputWizardPage extends WizardPage {

    private static final String DEFAULT_REGEX = "\\s*(.*\\S)"; //$NON-NLS-1$
    private static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"; //$NON-NLS-1$
    private static final String TIMESTAMP_FORMAT_BUNDLE = "org.eclipse.tracecompass.doc.user"; //$NON-NLS-1$
    private static final String TIMESTAMP_FORMAT_PATH = "reference/api/org/eclipse/tracecompass/tmf/core/timestamp/TmfTimestampFormat.html"; //$NON-NLS-1$
    private static final String PATTERN_URL = "http://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#sum"; //$NON-NLS-1$
    private static final Image LINE_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/line_icon.gif"); //$NON-NLS-1$
    private static final Image ADD_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/add_button.gif"); //$NON-NLS-1$
    private static final Image ADD_NEXT_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/addnext_button.gif"); //$NON-NLS-1$
    private static final Image ADD_CHILD_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/addchild_button.gif"); //$NON-NLS-1$
    private static final Image DELETE_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/delete_button.gif"); //$NON-NLS-1$
    private static final Image MOVE_UP_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/moveup_button.gif"); //$NON-NLS-1$
    private static final Image MOVE_DOWN_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/movedown_button.gif"); //$NON-NLS-1$
    private static final Image HELP_IMAGE = Activator.getDefault().getImageFromPath("/icons/elcl16/help_button.gif"); //$NON-NLS-1$
    private static final Color COLOR_BLACK = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
    private static final Color COLOR_LIGHT_GREEN = new Color(Display.getDefault(), 192, 255, 192);
    private static final Color COLOR_GREEN = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
    private static final Color COLOR_LIGHT_YELLOW = new Color(Display.getDefault(), 255, 255, 192);
    private static final Color COLOR_YELLOW = Display.getDefault().getSystemColor(SWT.COLOR_YELLOW);
    private static final Color COLOR_LIGHT_MAGENTA = new Color(Display.getDefault(), 255, 192, 255);
    private static final Color COLOR_MAGENTA = Display.getDefault().getSystemColor(SWT.COLOR_MAGENTA);
    private static final Color COLOR_LIGHT_RED = new Color(Display.getDefault(), 255, 192, 192);
    private static final Color COLOR_TEXT_BACKGROUND = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
    private static final Color COLOR_WIDGET_BACKGROUND = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
    private static final Color COLOR_GRAY = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);

    private final ISelection selection;
    private CustomTxtTraceDefinition definition;
    private String editCategoryName;
    private String editDefinitionName;
    private String defaultDescription;
    private Line selectedLine;
    private Composite container;
    private Text categoryText;
    private Text logtypeText;
    private Text timestampOutputFormatText;
    private Text timestampPreviewText;
    private ScrolledComposite lineScrolledComposite;
    private TreeViewer treeViewer;
    private Composite lineContainer;
    private StyledText inputText;
    private Font fixedFont;
    private UpdateListener updateListener;
    private Browser helpBrowser;

    // variables used recursively through line traversal
    private String timeStampFormat;
    private boolean timestampFound;

    /**
     * Constructor
     *
     * @param selection
     *            The Selection object
     * @param definition
     *            The trace definition
     */
    protected CustomTxtParserInputWizardPage(ISelection selection,
            CustomTxtTraceDefinition definition) {
        super("CustomParserWizardPage"); //$NON-NLS-1$
        if (definition == null) {
            setTitle(Messages.CustomTxtParserInputWizardPage_titleNew);
            defaultDescription = Messages.CustomTxtParserInputWizardPage_descriptionNew;
        } else {
            setTitle(Messages.CustomTxtParserInputWizardPage_titleEdit);
            defaultDescription = Messages.CustomTxtParserInputWizardPage_desccriptionEdit;
        }
        setDescription(defaultDescription);
        this.selection = selection;
        this.definition = definition;
        if (definition != null) {
            this.editCategoryName = definition.categoryName;
            this.editDefinitionName = definition.definitionName;
        }
    }

    @Override
    public void createControl(Composite parent) {
        container = new Composite(parent, SWT.NULL);
        container.setLayout(new GridLayout());

        updateListener = new UpdateListener();

        Composite headerComposite = new Composite(container, SWT.FILL);
        GridLayout headerLayout = new GridLayout(5, false);
        headerLayout.marginHeight = 0;
        headerLayout.marginWidth = 0;
        headerComposite.setLayout(headerLayout);
        headerComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Label categoryLabel = new Label(headerComposite, SWT.NULL);
        categoryLabel.setText(Messages.CustomTxtParserInputWizardPage_category);

        categoryText = new Text(headerComposite, SWT.BORDER | SWT.SINGLE);
        categoryText.setLayoutData(new GridData(120, SWT.DEFAULT));

        Label timestampFormatLabel = new Label(headerComposite, SWT.NULL);
        timestampFormatLabel.setText(Messages.CustomTxtParserInputWizardPage_timestampFormat);

        timestampOutputFormatText = new Text(headerComposite, SWT.BORDER | SWT.SINGLE);
        timestampOutputFormatText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        timestampOutputFormatText.setText(DEFAULT_TIMESTAMP_FORMAT);
        timestampOutputFormatText.addPaintListener(e -> {
            if (!timestampOutputFormatText.isFocusControl() && timestampOutputFormatText.getText().trim().isEmpty()) {
                e.gc.setForeground(COLOR_GRAY);
                int borderWidth = timestampOutputFormatText.getBorderWidth();
                e.gc.drawText(Messages.CustomTxtParserInputWizardPage_default, borderWidth, borderWidth);
            }
        });

        Button timeStampFormatHelpButton = new Button(headerComposite, SWT.PUSH);
        timeStampFormatHelpButton.setImage(HELP_IMAGE);
        timeStampFormatHelpButton.setToolTipText(Messages.CustomTxtParserInputWizardPage_timestampFormatHelp);
        timeStampFormatHelpButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Bundle plugin = Platform.getBundle(TIMESTAMP_FORMAT_BUNDLE);
                IPath path = new Path(TIMESTAMP_FORMAT_PATH);
                URL fileURL = FileLocator.find(plugin, path, null);
                try {
                    URL pageURL = FileLocator.toFileURL(fileURL);
                    openHelpShell(pageURL.toString());
                } catch (IOException e1) {
                }
            }
        });

        Label logtypeLabel = new Label(headerComposite, SWT.NULL);
        logtypeLabel.setText(Messages.CustomTxtParserInputWizardPage_logType);

        logtypeText = new Text(headerComposite, SWT.BORDER | SWT.SINGLE);
        logtypeText.setLayoutData(new GridData(120, SWT.DEFAULT));
        logtypeText.setFocus();

        Label timestampPreviewLabel = new Label(headerComposite, SWT.NULL);
        timestampPreviewLabel.setText(Messages.CustomTxtParserInputWizardPage_preview);

        timestampPreviewText = new Text(headerComposite, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
        timestampPreviewText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        timestampPreviewText.setText(Messages.CustomTxtParserInputWizardPage_noMatchingTimestamp);

        Composite buttonBar = new Composite(container, SWT.NONE);
        GridLayout buttonBarLayout = new GridLayout(5, false);
        buttonBarLayout.marginHeight = 0;
        buttonBarLayout.marginWidth = 0;
        buttonBar.setLayout(buttonBarLayout);

        Button removeButton = new Button(buttonBar, SWT.PUSH);
        removeButton.setImage(DELETE_IMAGE);
        removeButton.setToolTipText(Messages.CustomTxtParserInputWizardPage_removeLine);
        removeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (treeViewer.getSelection().isEmpty() || selectedLine == null) {
                    return;
                }
                removeLine();
                InputLine inputLine = (InputLine) ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
                if (inputLine.parentInput == null) {
                    definition.inputs.remove(inputLine);
                } else {
                    int index = inputLine.parentInput.childrenInputs.indexOf(inputLine);
                    if (index > 0) {
                        inputLine.parentInput.childrenInputs.get(index - 1).nextInput = inputLine.nextInput;
                    }
                    inputLine.parentInput.childrenInputs.remove(inputLine);
                }
                treeViewer.refresh();
                validate();
                updatePreviews();
            }
        });
        Button addNextButton = new Button(buttonBar, SWT.PUSH);
        addNextButton.setImage(ADD_NEXT_IMAGE);
        addNextButton.setToolTipText(Messages.CustomTxtParserInputWizardPage_addNextLine);
        addNextButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                InputLine inputLine = new InputLine(Cardinality.ZERO_OR_MORE, "", null); //$NON-NLS-1$
                if (((List<?>) treeViewer.getInput()).isEmpty()) {
                    definition.inputs.add(inputLine);
                } else if (treeViewer.getSelection().isEmpty()) {
                    return;
                } else {
                    InputLine previousInputLine = (InputLine) ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
                    if (previousInputLine.parentInput == null) {
                        for (int i = 0; i < definition.inputs.size(); i++) {
                            if (definition.inputs.get(i).equals(previousInputLine)) {
                                definition.inputs.add(i + 1, inputLine);
                            }
                        }
                    } else {
                        previousInputLine.addNext(inputLine);
                    }
                }
                treeViewer.refresh();
                treeViewer.setSelection(new StructuredSelection(inputLine), true);
            }
        });
        Button addChildButton = new Button(buttonBar, SWT.PUSH);
        addChildButton.setImage(ADD_CHILD_IMAGE);
        addChildButton.setToolTipText(Messages.CustomTxtParserInputWizardPage_addChildLine);
        addChildButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                InputLine inputLine = new InputLine(Cardinality.ZERO_OR_MORE, "", null); //$NON-NLS-1$
                if (((List<?>) treeViewer.getInput()).isEmpty()) {
                    definition.inputs.add(inputLine);
                } else if (treeViewer.getSelection().isEmpty()) {
                    return;
                } else {
                    InputLine parentInputLine = (InputLine) ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
                    parentInputLine.addChild(inputLine);
                }
                treeViewer.refresh();
                treeViewer.setSelection(new StructuredSelection(inputLine), true);
            }
        });
        Button moveUpButton = new Button(buttonBar, SWT.PUSH);
        moveUpButton.setImage(MOVE_UP_IMAGE);
        moveUpButton.setToolTipText(Messages.CustomTxtParserInputWizardPage_moveUp);
        moveUpButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (treeViewer.getSelection().isEmpty()) {
                    return;
                }
                InputLine inputLine = (InputLine) ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
                if (inputLine.parentInput == null) {
                    for (int i = 1; i < definition.inputs.size(); i++) {
                        if (definition.inputs.get(i).equals(inputLine)) {
                            definition.inputs.add(i - 1, definition.inputs.remove(i));
                            break;
                        }
                    }
                } else {
                    inputLine.moveUp();
                }
                treeViewer.refresh();
                validate();
                updatePreviews();
            }
        });
        Button moveDownButton = new Button(buttonBar, SWT.PUSH);
        moveDownButton.setImage(MOVE_DOWN_IMAGE);
        moveDownButton.setToolTipText(Messages.CustomTxtParserInputWizardPage_moveDown);
        moveDownButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (treeViewer.getSelection().isEmpty()) {
                    return;
                }
                InputLine inputLine = (InputLine) ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
                if (inputLine.parentInput == null) {
                    for (int i = 0; i < definition.inputs.size() - 1; i++) {
                        if (definition.inputs.get(i).equals(inputLine)) {
                            definition.inputs.add(i + 1, definition.inputs.remove(i));
                            break;
                        }
                    }
                } else {
                    inputLine.moveDown();
                }
                treeViewer.refresh();
                validate();
                updatePreviews();
            }
        });

        SashForm vSash = new SashForm(container, SWT.VERTICAL);
        vSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        vSash.setBackground(COLOR_GRAY);

        SashForm hSash = new SashForm(vSash, SWT.HORIZONTAL);
        hSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        ScrolledComposite treeScrolledComposite = new ScrolledComposite(hSash, SWT.V_SCROLL | SWT.H_SCROLL);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = 200;
        gd.widthHint = 200;
        treeScrolledComposite.setLayoutData(gd);
        Composite treeContainer = new Composite(treeScrolledComposite, SWT.NONE);
        treeContainer.setLayout(new FillLayout());
        treeScrolledComposite.setContent(treeContainer);
        treeScrolledComposite.setExpandHorizontal(true);
        treeScrolledComposite.setExpandVertical(true);

        treeViewer = new TreeViewer(treeContainer, SWT.SINGLE | SWT.BORDER);
        treeViewer.setContentProvider(new InputLineTreeNodeContentProvider());
        treeViewer.setLabelProvider(new InputLineTreeLabelProvider());
        treeViewer.addSelectionChangedListener(new InputLineTreeSelectionChangedListener());
        treeContainer.layout();

        treeScrolledComposite.setMinSize(treeContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, treeContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);

        lineScrolledComposite = new ScrolledComposite(hSash, SWT.V_SCROLL);
        lineScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        lineContainer = new Composite(lineScrolledComposite, SWT.NONE);
        GridLayout linesLayout = new GridLayout();
        linesLayout.marginHeight = 1;
        linesLayout.marginWidth = 0;
        lineContainer.setLayout(linesLayout);
        lineScrolledComposite.setContent(lineContainer);
        lineScrolledComposite.setExpandHorizontal(true);
        lineScrolledComposite.setExpandVertical(true);

        if (definition == null) {
            definition = new CustomTxtTraceDefinition();
            definition.inputs.add(new InputLine(Cardinality.ZERO_OR_MORE, DEFAULT_REGEX,
                    Arrays.asList(new InputData(Tag.MESSAGE, CustomTraceDefinition.ACTION_SET))));
        }
        loadDefinition(definition);
        treeViewer.expandAll();
        lineContainer.layout();

        categoryText.addModifyListener(updateListener);
        logtypeText.addModifyListener(updateListener);
        timestampOutputFormatText.addModifyListener(updateListener);

        lineScrolledComposite.setMinSize(lineContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, lineContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y - 1);

        hSash.setWeights(new int[] { 1, 2 });

        Composite sashBottom = new Composite(vSash, SWT.NONE);
        GridLayout sashBottomLayout = new GridLayout(3, false);
        sashBottomLayout.marginHeight = 0;
        sashBottomLayout.marginWidth = 0;
        sashBottom.setLayout(sashBottomLayout);

        Label previewLabel = new Label(sashBottom, SWT.NULL);
        previewLabel.setText(Messages.CustomTxtParserInputWizardPage_previewInput);
        previewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Button highlightAllButton = new Button(sashBottom, SWT.PUSH);
        highlightAllButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        highlightAllButton.setText(Messages.CustomTxtParserInputWizardPage_highlightAll);
        highlightAllButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updatePreviews(true);
            }
        });

        Button legendButton = new Button(sashBottom, SWT.PUSH);
        legendButton.setImage(HELP_IMAGE);
        legendButton.setToolTipText(Messages.CustomTxtParserInputWizardPage_previewLegend);
        legendButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        legendButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openLegend();
            }
        });

        inputText = new StyledText(sashBottom, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        if (fixedFont == null) {
            if (System.getProperty("os.name").contains("Windows")) { //$NON-NLS-1$  //$NON-NLS-2$
                fixedFont = new Font(Display.getCurrent(), new FontData("Courier New", 10, SWT.NORMAL)); //$NON-NLS-1$
            } else {
                fixedFont = new Font(Display.getCurrent(), new FontData("Monospace", 10, SWT.NORMAL)); //$NON-NLS-1$
            }
        }
        inputText.setFont(fixedFont);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
        gd.heightHint = inputText.computeSize(SWT.DEFAULT, inputText.getLineHeight() * 4).y;
        gd.widthHint = 800;
        inputText.setLayoutData(gd);
        inputText.setText(getSelectionText());
        inputText.addModifyListener(updateListener);

        vSash.setWeights(new int[] { hSash.computeSize(SWT.DEFAULT, SWT.DEFAULT).y, sashBottom.computeSize(SWT.DEFAULT, SWT.DEFAULT).y });

        setControl(container);

        validate();
        updatePreviews();
    }

    private static class InputLineTreeNodeContentProvider implements ITreeContentProvider {

        @Override
        public Object[] getElements(Object inputElement) {
            return ((List<?>) inputElement).toArray();
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            InputLine inputLine = (InputLine) parentElement;
            if (inputLine.childrenInputs == null) {
                return new InputLine[0];
            }
            return inputLine.childrenInputs.toArray();
        }

        @Override
        public boolean hasChildren(Object element) {
            InputLine inputLine = (InputLine) element;
            return (inputLine.childrenInputs != null && !inputLine.childrenInputs.isEmpty());
        }

        @Override
        public void dispose() {
            // Do nothing
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // Do nothing
        }

        @Override
        public Object getParent(Object element) {
            InputLine inputLine = (InputLine) element;
            return inputLine.parentInput;
        }
    }

    private class InputLineTreeLabelProvider extends ColumnLabelProvider {

        @Override
        public Image getImage(Object element) {
            return LINE_IMAGE;
        }

        @Override
        public String getText(Object element) {
            InputLine inputLine = (InputLine) element;
            if (inputLine.parentInput == null) {
                return "Root Line " + getName(inputLine) + " " + inputLine.cardinality.toString() + " : " + inputLine.getRegex(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            return "Line " + getName(inputLine) + " " + inputLine.cardinality.toString() + " : " + inputLine.getRegex(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    private class InputLineTreeSelectionChangedListener implements ISelectionChangedListener {
        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            if (selectedLine != null) {
                selectedLine.dispose();
            }
            if (!(event.getSelection().isEmpty()) && event.getSelection() instanceof IStructuredSelection) {
                IStructuredSelection sel = (IStructuredSelection) event.getSelection();
                InputLine inputLine = (InputLine) sel.getFirstElement();
                selectedLine = new Line(lineContainer, getName(inputLine), inputLine);
                lineContainer.layout();
                lineScrolledComposite.setMinSize(lineContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, lineContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y - 1);
                container.layout();
                validate();
                updatePreviews();
            }
        }
    }

    @Override
    public void dispose() {
        if (fixedFont != null) {
            fixedFont.dispose();
            fixedFont = null;
        }
        super.dispose();
    }

    private void loadDefinition(CustomTxtTraceDefinition def) {
        categoryText.setText(def.categoryName);
        logtypeText.setText(def.definitionName);
        if (def.timeStampOutputFormat != null) {
            timestampOutputFormatText.setText(def.timeStampOutputFormat);
        } else {
            timestampOutputFormatText.setText(""); //$NON-NLS-1$
        }
        treeViewer.setInput(def.inputs);
        if (!def.inputs.isEmpty()) {
            InputLine inputLine = def.inputs.get(0);
            treeViewer.setSelection(new StructuredSelection(inputLine));
        }
    }

    private String getName(InputLine inputLine) {
        if (inputLine.parentInput == null) {
            return Integer.toString(definition.inputs.indexOf(inputLine) + 1);
        }
        return getName(inputLine.parentInput) + "." + Integer.toString(inputLine.parentInput.childrenInputs.indexOf(inputLine) + 1); //$NON-NLS-1$
    }

    /**
     * Get the global list of inputs.
     *
     * @return The list of inputs
     */
    public List<Entry<Tag, String>> getInputs() {
        List<Entry<Tag, String>> inputs = new ArrayList<>();
        for (InputLine inputLine : definition.inputs) {
            for (Entry<Tag, String> input : getInputs(inputLine)) {
                if (!inputs.contains(input)) {
                    inputs.add(input);
                }
            }
        }
        return inputs;
    }

    /**
     * Get the list of inputs for the given input line, recursively.
     *
     * @param inputLine
     *            The input line
     * @return The list of inputs
     */
    private List<Entry<Tag, String>> getInputs(InputLine inputLine) {
        List<Entry<Tag, String>> inputs = new ArrayList<>();
        if (inputLine.columns != null) {
            for (InputData inputData : inputLine.columns) {
                Entry<Tag, String> input = new SimpleEntry<>(inputData.tag, inputData.name);
                if (!inputs.contains(input)) {
                    inputs.add(input);
                }
            }
        }
        if (inputLine.childrenInputs != null) {
            for (InputLine childInputLine : inputLine.childrenInputs) {
                for (Entry<Tag, String> input : getInputs(childInputLine)) {
                    if (!inputs.contains(input)) {
                        inputs.add(input);
                    }
                }
            }
        }
        return inputs;
    }

    private void removeLine() {
        selectedLine.dispose();
        selectedLine = null;
        lineContainer.layout();
        lineScrolledComposite.setMinSize(lineContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, lineContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y - 1);
        container.layout();
    }

    private String getSelectionText() {
        if (this.selection instanceof IStructuredSelection) {
            Object sel = ((IStructuredSelection) this.selection).getFirstElement();
            if (sel instanceof IFile) {
                IFile file = (IFile) sel;
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(file.getContents()));
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                        sb.append('\n');
                    }
                    return sb.toString();
                } catch (CoreException e) {
                    return ""; //$NON-NLS-1$
                } catch (IOException e) {
                    return ""; //$NON-NLS-1$
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
        }
        return ""; //$NON-NLS-1$
    }

    private void updatePreviews() {
        updatePreviews(false);
    }

    private void updatePreviews(boolean updateAll) {
        if (inputText == null) {
            // early update during construction
            return;
        }
        inputText.setStyleRanges(new StyleRange[] {});

        try (Scanner scanner = new Scanner(inputText.getText());) {
            scanner.useDelimiter("\n"); //$NON-NLS-1$

            int rawPos = 0;
            // skip starting delimiters
            String skip = scanner.findWithinHorizon("\\A\n+", 0); //$NON-NLS-1$
            if (skip != null) {
                rawPos += skip.length();
            }

            timeStampFormat = null;
            if (selectedLine != null) {
                for (InputGroup input : selectedLine.inputs) {
                    input.previewText.setText(Messages.CustomTxtParserInputWizardPage_noMathcingLine);
                }
            }

            Map<Object, String> data = new HashMap<>();
            int rootLineMatches = 0;
            String firstEntryTimeStamp = null;
            String firstEntryTimeStampInputFormat = null;
            String line = null;
            boolean lineIsNull = true; // needed because of JDT bug with continue at label
            event: while (scanner.hasNext() || !lineIsNull) {
                if (rootLineMatches > 0 && !updateAll) {
                    break;
                }
                if (line == null) {
                    line = scanner.next();
                    lineIsNull = false;
                }
                int length = line.length();
                String log = line.replaceAll("\r", ""); //$NON-NLS-1$ //$NON-NLS-2$
                for (InputLine rootInputLine : definition.inputs) {
                    Pattern pattern;
                    try {
                        pattern = rootInputLine.getPattern();
                    } catch (PatternSyntaxException e) {
                        continue;
                    }
                    Matcher matcher = pattern.matcher(log);
                    if (matcher.matches()) {
                        rootLineMatches++;
                        inputText.setStyleRange(new StyleRange(rawPos, length,
                                COLOR_BLACK, COLOR_YELLOW, SWT.ITALIC));
                        data = new HashMap<>();
                        timeStampFormat = null;
                        updatePreviewLine(rootInputLine, matcher, data, rawPos, rootLineMatches);
                        if (rootLineMatches == 1) {
                            firstEntryTimeStamp = data.get(Tag.TIMESTAMP);
                            firstEntryTimeStampInputFormat = timeStampFormat;
                        }
                        HashMap<InputLine, Integer> countMap = new HashMap<>();
                        InputLine currentInput = null;
                        if (rootInputLine.childrenInputs != null && !rootInputLine.childrenInputs.isEmpty()) {
                            currentInput = rootInputLine.childrenInputs.get(0);
                            countMap.put(currentInput, 0);
                        }
                        rawPos += length + 1; // +1 for \n
                        while (scanner.hasNext()) {
                            line = scanner.next();
                            length = line.length();
                            log = line.replaceAll("\r", ""); //$NON-NLS-1$ //$NON-NLS-2$
                            boolean processed = false;
                            if (currentInput == null) {
                                for (InputLine input : definition.inputs) {
                                    try {
                                        matcher = input.getPattern().matcher(log);
                                    } catch (PatternSyntaxException e) {
                                        continue;
                                    }
                                    if (matcher.matches()) {
                                        continue event;
                                    }
                                }
                            } else {
                                if (checkNotNull(countMap.get(currentInput)) >= currentInput.getMinCount()) {
                                    List<InputLine> nextInputs = currentInput.getNextInputs(countMap);
                                    if (nextInputs.isEmpty() || nextInputs.get(nextInputs.size() - 1).getMinCount() == 0) {
                                        for (InputLine input : definition.inputs) {
                                            try {
                                                matcher = input.getPattern().matcher(log);
                                            } catch (PatternSyntaxException e) {
                                                continue;
                                            }
                                            if (matcher.matches()) {
                                                continue event;
                                            }
                                        }
                                    }
                                    for (InputLine input : nextInputs) {
                                        try {
                                            matcher = input.getPattern().matcher(log);
                                        } catch (PatternSyntaxException e) {
                                            continue;
                                        }
                                        if (matcher.matches()) {
                                            inputText.setStyleRange(new StyleRange(rawPos, length,
                                                    COLOR_BLACK, COLOR_LIGHT_YELLOW, SWT.ITALIC));
                                            currentInput = input;
                                            updatePreviewLine(currentInput, matcher, data, rawPos, rootLineMatches);
                                            if (countMap.get(currentInput) == null) {
                                                countMap.put(currentInput, 1);
                                            } else {
                                                countMap.put(currentInput, checkNotNull(countMap.get(currentInput)) + 1);
                                            }
                                            Iterator<InputLine> iter = countMap.keySet().iterator();
                                            while (iter.hasNext()) {
                                                InputLine inputLine = iter.next();
                                                if (inputLine.level > currentInput.level) {
                                                    iter.remove();
                                                }
                                            }
                                            if (currentInput.childrenInputs != null && !currentInput.childrenInputs.isEmpty()) {
                                                currentInput = currentInput.childrenInputs.get(0);
                                                countMap.put(currentInput, 0);
                                            } else {
                                                if (checkNotNull(countMap.get(currentInput)) >= currentInput.getMaxCount()) {
                                                    if (!currentInput.getNextInputs(countMap).isEmpty()) {
                                                        currentInput = currentInput.getNextInputs(countMap).get(0);
                                                        countMap.putIfAbsent(currentInput, 0);
                                                        iter = countMap.keySet().iterator();
                                                        while (iter.hasNext()) {
                                                            InputLine inputLine = iter.next();
                                                            if (inputLine.level > currentInput.level) {
                                                                iter.remove();
                                                            }
                                                        }
                                                    } else {
                                                        currentInput = null;
                                                    }
                                                }
                                            }
                                            processed = true;
                                            break;
                                        }
                                    }
                                }
                                if (!processed && currentInput != null) {
                                    matcher = null;
                                    try {
                                        matcher = currentInput.getPattern().matcher(log);
                                    } catch (PatternSyntaxException e) {
                                    }
                                    if (matcher != null && matcher.matches()) {
                                        inputText.setStyleRange(new StyleRange(rawPos, length,
                                                COLOR_BLACK, COLOR_LIGHT_YELLOW, SWT.ITALIC));
                                        updatePreviewLine(currentInput, matcher, data, rawPos, rootLineMatches);
                                        countMap.put(currentInput, checkNotNull(countMap.get(currentInput)) + 1);
                                        if (currentInput.childrenInputs != null && !currentInput.childrenInputs.isEmpty()) {
                                            currentInput = currentInput.childrenInputs.get(0);
                                            countMap.put(currentInput, 0);
                                        } else {
                                            if (checkNotNull(countMap.get(currentInput)) >= currentInput.getMaxCount()) {
                                                if (!currentInput.getNextInputs(countMap).isEmpty()) {
                                                    currentInput = currentInput.getNextInputs(countMap).get(0);
                                                    if (countMap.get(currentInput) == null) {
                                                        countMap.put(currentInput, 0);
                                                    }
                                                    Iterator<InputLine> iter = countMap.keySet().iterator();
                                                    while (iter.hasNext()) {
                                                        InputLine inputLine = iter.next();
                                                        if (inputLine.level > currentInput.level) {
                                                            iter.remove();
                                                        }
                                                    }
                                                } else {
                                                    currentInput = null;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            rawPos += length + 1; // +1 for \n
                        }

                        break;
                    }
                }
                rawPos += length + 1; // +1 for \n
                line = null;
                lineIsNull = true;
            }

            if (rootLineMatches == 1) {
                firstEntryTimeStamp = data.get(Tag.TIMESTAMP);
                firstEntryTimeStampInputFormat = timeStampFormat;
            }
            if (firstEntryTimeStamp == null) {
                timestampPreviewText.setText(Messages.CustomTxtParserInputWizardPage_noTimestampGroup);
                if (selectedLine != null) {
                    for (InputGroup group : selectedLine.inputs) {
                        if (group.tagCombo.getText().equals(Tag.TIMESTAMP.toString())) {
                            timestampPreviewText.setText(Messages.CustomTxtParserInputWizardPage_noMatchingTimestamp);
                            break;
                        }
                    }
                }
            } else {
                try {
                    TmfTimestampFormat timestampFormat = new TmfTimestampFormat(firstEntryTimeStampInputFormat);
                    long timestamp = timestampFormat.parseValue(firstEntryTimeStamp);
                    if (timestampOutputFormatText.getText().trim().isEmpty()) {
                        timestampFormat = new TmfTimestampFormat();
                    } else {
                        timestampFormat = new TmfTimestampFormat(timestampOutputFormatText.getText().trim());
                    }
                    timestampPreviewText.setText(timestampFormat.format(timestamp));
                } catch (ParseException e) {
                    timestampPreviewText.setText("*parse exception* [" + firstEntryTimeStamp + "] <> [" + firstEntryTimeStampInputFormat + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                } catch (IllegalArgumentException e) {
                    timestampPreviewText.setText("*parse exception* [Illegal Argument: " + e.getMessage() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
                }

            }
        }
    }

    private void updatePreviewLine(InputLine line, Matcher matcher, Map<Object, String> data, int rawPos, int rootLineMatches) {
        for (int i = 0; i < line.columns.size(); i++) {
            InputData input = line.columns.get(i);
            if (i < matcher.groupCount() && matcher.group(i + 1) != null) {
                if (line.parentInput == null) {
                    inputText.setStyleRange(new StyleRange(rawPos + matcher.start(i + 1), matcher.end(i + 1) - matcher.start(i + 1),
                            COLOR_BLACK, COLOR_GREEN, SWT.BOLD));
                } else {
                    inputText.setStyleRange(new StyleRange(rawPos + matcher.start(i + 1), matcher.end(i + 1) - matcher.start(i + 1),
                            COLOR_BLACK, COLOR_LIGHT_GREEN, SWT.BOLD));
                }
                String value = matcher.group(i + 1).trim();
                if (selectedLine != null && selectedLine.inputLine.equals(line) && rootLineMatches == 1 &&
                        selectedLine.inputs.get(i).previewText.getText().equals(Messages.CustomTxtParserInputWizardPage_noMatchingLine)) {
                    selectedLine.inputs.get(i).previewText.setText(value);
                }
                if (value.length() == 0) {
                    continue;
                }
                Object key = (input.tag.equals(Tag.OTHER) ? input.name : input.tag);
                if (input.action == CustomTraceDefinition.ACTION_SET) {
                    data.put(key, value);
                    if (input.tag.equals(Tag.TIMESTAMP)) {
                        timeStampFormat = input.format;
                    }
                } else if (input.action == CustomTraceDefinition.ACTION_APPEND) {
                    String s = data.get(key);
                    if (s != null) {
                        data.put(key, s + value);
                    } else {
                        data.put(key, value);
                    }
                    if (input.tag.equals(Tag.TIMESTAMP)) {
                        if (timeStampFormat != null) {
                            timeStampFormat += input.format;
                        } else {
                            timeStampFormat = input.format;
                        }
                    }
                } else if (input.action == CustomTraceDefinition.ACTION_APPEND_WITH_SEPARATOR) {
                    String s = data.get(key);
                    if (s != null) {
                        data.put(key, s + CustomTraceDefinition.SEPARATOR + value);
                    } else {
                        data.put(key, value);
                    }
                    if (input.tag.equals(Tag.TIMESTAMP)) {
                        if (timeStampFormat != null) {
                            timeStampFormat += CustomTraceDefinition.SEPARATOR + input.format;
                        } else {
                            timeStampFormat = input.format;
                        }
                    }
                }
            } else {
                if (selectedLine != null && selectedLine.inputLine.equals(line) && rootLineMatches == 1) {
                    if (selectedLine.inputs.get(i).previewText.getText().equals(Messages.CustomTxtParserInputWizardPage_noMatchingLine)) {
                        selectedLine.inputs.get(i).previewText.setText(Messages.CustomTxtParserInputWizardPage_noMatchingGroup);
                    }
                }
            }
        }
        // highlight the matching groups that have no corresponponding input
        for (int i = line.columns.size(); i < matcher.groupCount(); i++) {
            if (matcher.group(i + 1) != null) {
                if (line.parentInput == null) {
                    inputText.setStyleRange(new StyleRange(rawPos + matcher.start(i + 1), matcher.end(i + 1) - matcher.start(i + 1),
                            COLOR_BLACK, COLOR_MAGENTA));
                } else {
                    inputText.setStyleRange(new StyleRange(rawPos + matcher.start(i + 1), matcher.end(i + 1) - matcher.start(i + 1),
                            COLOR_BLACK, COLOR_LIGHT_MAGENTA));
                }
            }
        }
    }

    private void openHelpShell(String url) {
        if (helpBrowser != null && !helpBrowser.isDisposed()) {
            helpBrowser.getShell().setActive();
            if (!helpBrowser.getUrl().equals(url)) {
                helpBrowser.setUrl(url);
            }
            return;
        }
        final Shell helpShell = new Shell(getShell(), SWT.SHELL_TRIM);
        helpShell.setLayout(new FillLayout());
        helpBrowser = new Browser(helpShell, SWT.NONE);
        helpBrowser.addTitleListener(event -> helpShell.setText(event.title));
        Rectangle r = container.getBounds();
        Point p = container.toDisplay(r.x, r.y);
        Rectangle trim = helpShell.computeTrim(p.x + (r.width - 750) / 2, p.y + (r.height - 400) / 2, 750, 400);
        helpShell.setBounds(trim);
        helpShell.open();
        helpBrowser.setUrl(url);
    }

    private void openLegend() {
        final String cg = Messages.CustomTxtParserInputWizardPage_capturedGroup;
        final String ucg = Messages.CustomTxtParserInputWizardPage_unidentifiedCaptureGroup;
        final String ut = Messages.CustomTxtParserInputWizardPage_uncapturedText;
        int line1start = 0;
        String line1 = Messages.CustomTxtParserInputWizardPage_nonMatchingLine;
        int line2start = line1start + line1.length();
        String line2 = Messages.CustomTxtParserInputWizardPage_matchingRootLine + ' ' + cg + ' ' + ucg + ' ' + ut + " \n"; //$NON-NLS-1$
        int line3start = line2start + line2.length();
        String line3 = Messages.CustomTxtParserInputWizardPage_matchingOtherLine + ' '  + cg + ' ' + ucg + ' ' + ut + " \n"; //$NON-NLS-1$
        int line4start = line3start + line3.length();
        String line4 = Messages.CustomTxtParserInputWizardPage_matchingOtherLine + ' ' + cg + ' ' + ucg + ' ' + ut + " \n"; //$NON-NLS-1$
        int line5start = line4start + line4.length();
        String line5 = Messages.CustomTxtParserInputWizardPage_nonMatchingLine;
        int line6start = line5start + line5.length();
        String line6 = Messages.CustomTxtParserInputWizardPage_matchingRootLine + cg + ' ' + ucg + ' ' + ut + " \n"; //$NON-NLS-1$

        final Shell legendShell = new Shell(getShell(), SWT.DIALOG_TRIM);
        legendShell.setLayout(new FillLayout());
        StyledText legendText = new StyledText(legendShell, SWT.MULTI);
        legendText.setFont(fixedFont);
        legendText.setText(line1 + line2 + line3 + line4 + line5 + line6);
        legendText.setStyleRange(new StyleRange(line2start, line2.length(), COLOR_BLACK, COLOR_YELLOW, SWT.ITALIC));
        legendText.setStyleRange(new StyleRange(line3start, line3.length(), COLOR_BLACK, COLOR_LIGHT_YELLOW, SWT.ITALIC));
        legendText.setStyleRange(new StyleRange(line4start, line4.length(), COLOR_BLACK, COLOR_LIGHT_YELLOW, SWT.ITALIC));
        legendText.setStyleRange(new StyleRange(line6start, line6.length(), COLOR_BLACK, COLOR_YELLOW, SWT.ITALIC));
        legendText.setStyleRange(new StyleRange(line2start + line2.indexOf(cg), cg.length(), COLOR_BLACK, COLOR_GREEN, SWT.BOLD));
        legendText.setStyleRange(new StyleRange(line2start + line2.indexOf(ucg), ucg.length(), COLOR_BLACK, COLOR_MAGENTA));
        legendText.setStyleRange(new StyleRange(line3start + line3.indexOf(cg), cg.length(), COLOR_BLACK, COLOR_LIGHT_GREEN, SWT.BOLD));
        legendText.setStyleRange(new StyleRange(line3start + line3.indexOf(ucg), ucg.length(), COLOR_BLACK, COLOR_LIGHT_MAGENTA));
        legendText.setStyleRange(new StyleRange(line4start + line4.indexOf(cg), cg.length(), COLOR_BLACK, COLOR_LIGHT_GREEN, SWT.BOLD));
        legendText.setStyleRange(new StyleRange(line4start + line4.indexOf(ucg), ucg.length(), COLOR_BLACK, COLOR_LIGHT_MAGENTA));
        legendText.setStyleRange(new StyleRange(line6start + line6.indexOf(cg), cg.length(), COLOR_BLACK, COLOR_GREEN, SWT.BOLD));
        legendText.setStyleRange(new StyleRange(line6start + line6.indexOf(ucg), ucg.length(), COLOR_BLACK, COLOR_MAGENTA));
        legendShell.setText(Messages.CustomTxtParserInputWizardPage_previewLegend);
        legendShell.pack();
        legendShell.open();
    }

    private class UpdateListener implements ModifyListener, SelectionListener {

        @Override
        public void modifyText(ModifyEvent e) {
            validate();
            updatePreviews();
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            validate();
            updatePreviews();
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            validate();
            updatePreviews();
        }

    }

    private class Line {
        private static final String INFINITY_STRING = "\u221E"; //$NON-NLS-1$
        private InputLine inputLine;
        private Group group;
        private Composite labelComposite;
        private Text regexText;
        private Composite cardinalityContainer;
        private Combo cardinalityCombo;
        private Label cardinalityMinLabel;
        private Text cardinalityMinText;
        private Label cardinalityMaxLabel;
        private Text cardinalityMaxText;
        private Button infiniteButton;
        private Button eventTypeButton;
        private Text eventTypeText;
        private List<InputGroup> inputs = new ArrayList<>();
        private Button addGroupButton;
        private Label addGroupLabel;

        public Line(Composite parent, String name, InputLine inputLine) {
            this.inputLine = inputLine;

            group = new Group(parent, SWT.NONE);
            group.setText(name);
            group.setLayout(new GridLayout(2, false));
            group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

            labelComposite = new Composite(group, SWT.FILL);
            GridLayout labelLayout = new GridLayout(1, false);
            labelLayout.marginWidth = 0;
            labelLayout.marginHeight = 0;
            labelComposite.setLayout(labelLayout);
            labelComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

            Label label = new Label(labelComposite, SWT.NULL);
            label.setText(Messages.CustomTxtParserInputWizardPage_regularExpression);

            Composite regexContainer = new Composite(group, SWT.NONE);
            GridLayout regexLayout = new GridLayout(2, false);
            regexLayout.marginHeight = 0;
            regexLayout.marginWidth = 0;
            regexContainer.setLayout(regexLayout);
            regexContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

            regexText = new Text(regexContainer, SWT.BORDER | SWT.SINGLE);
            GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gd.widthHint = 0;
            regexText.setLayoutData(gd);
            regexText.setText(inputLine.getRegex());
            regexText.addModifyListener(updateListener);

            Button regexHelpButton = new Button(regexContainer, SWT.PUSH);
            regexHelpButton.setImage(HELP_IMAGE);
            regexHelpButton.setToolTipText(Messages.CustomTxtParserInputWizardPage_regularExpressionHelp);
            regexHelpButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    openHelpShell(PATTERN_URL);
                }
            });

            label = new Label(group, SWT.NONE);
            label.setText(Messages.CustomTxtParserInputWizardPage_cardinality);
            label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

            cardinalityContainer = new Composite(group, SWT.NONE);
            GridLayout cardinalityLayout = new GridLayout(6, false);
            cardinalityLayout.marginHeight = 0;
            cardinalityLayout.marginWidth = 0;
            cardinalityContainer.setLayout(cardinalityLayout);
            cardinalityContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

            cardinalityCombo = new Combo(cardinalityContainer, SWT.DROP_DOWN | SWT.READ_ONLY);
            cardinalityCombo.setItems(new String[] {
                    Cardinality.ZERO_OR_MORE.toString(),
                    Cardinality.ONE_OR_MORE.toString(),
                    Cardinality.ZERO_OR_ONE.toString(),
                    Cardinality.ONE.toString(), "(?,?)" }); //$NON-NLS-1$
            cardinalityCombo.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    // Do nothing
                }

                @Override
                public void widgetSelected(SelectionEvent e) {
                    switch (cardinalityCombo.getSelectionIndex()) {
                    case 4: // (?,?)
                        cardinalityMinLabel.setVisible(true);
                        cardinalityMinText.setVisible(true);
                        cardinalityMaxLabel.setVisible(true);
                        cardinalityMaxText.setVisible(true);
                        infiniteButton.setVisible(true);
                        break;
                    default:
                        cardinalityMinLabel.setVisible(false);
                        cardinalityMinText.setVisible(false);
                        cardinalityMaxLabel.setVisible(false);
                        cardinalityMaxText.setVisible(false);
                        infiniteButton.setVisible(false);
                        break;
                    }
                    cardinalityContainer.layout();
                    validate();
                    updatePreviews();
                }
            });

            cardinalityMinLabel = new Label(cardinalityContainer, SWT.NONE);
            cardinalityMinLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
            cardinalityMinLabel.setText(Messages.CustomTxtParserInputWizardPage_min);
            cardinalityMinLabel.setVisible(false);

            cardinalityMinText = new Text(cardinalityContainer, SWT.BORDER | SWT.SINGLE);
            gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
            gd.widthHint = 20;
            cardinalityMinText.setLayoutData(gd);
            cardinalityMinText.setVisible(false);

            cardinalityMaxLabel = new Label(cardinalityContainer, SWT.NONE);
            cardinalityMaxLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
            cardinalityMaxLabel.setText(Messages.CustomTxtParserInputWizardPage_max);
            cardinalityMaxLabel.setVisible(false);

            cardinalityMaxText = new Text(cardinalityContainer, SWT.BORDER | SWT.SINGLE);
            gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
            gd.widthHint = 20;
            cardinalityMaxText.setLayoutData(gd);
            cardinalityMaxText.setVisible(false);

            infiniteButton = new Button(cardinalityContainer, SWT.PUSH);
            infiniteButton.setText(INFINITY_STRING);
            infiniteButton.setVisible(false);
            infiniteButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    cardinalityMaxText.setText(INFINITY_STRING);
                }
            });

            if (inputLine.cardinality.equals(Cardinality.ZERO_OR_MORE)) {
                cardinalityCombo.select(0);
            } else if (inputLine.cardinality.equals(Cardinality.ONE_OR_MORE)) {
                cardinalityCombo.select(1);
            } else if (inputLine.cardinality.equals(Cardinality.ZERO_OR_ONE)) {
                cardinalityCombo.select(2);
            } else if (inputLine.cardinality.equals(Cardinality.ONE)) {
                cardinalityCombo.select(3);
            } else {
                cardinalityCombo.select(4);
                cardinalityMinLabel.setVisible(true);
                cardinalityMinText.setVisible(true);
                if (inputLine.getMinCount() >= 0) {
                    cardinalityMinText.setText(Integer.toString(inputLine.getMinCount()));
                }
                cardinalityMaxLabel.setVisible(true);
                cardinalityMaxText.setVisible(true);
                if (inputLine.getMaxCount() == Cardinality.INF) {
                    cardinalityMaxText.setText(INFINITY_STRING);
                } else if (inputLine.getMaxCount() >= 0) {
                    cardinalityMaxText.setText(Integer.toString(inputLine.getMaxCount()));
                }
                infiniteButton.setVisible(true);
            }

            VerifyListener digitsListener = e -> {
                if (e.text.equals(INFINITY_STRING)) {
                    e.doit = e.widget == cardinalityMaxText && e.start == 0 && e.end == ((Text) e.widget).getText().length();
                } else {
                    if (((Text) e.widget).getText().equals(INFINITY_STRING)) {
                        e.doit = e.start == 0 && e.end == ((Text) e.widget).getText().length();
                    }
                    for (int i = 0; i < e.text.length(); i++) {
                        if (!Character.isDigit(e.text.charAt(i))) {
                            e.doit = false;
                            break;
                        }
                    }
                }
            };

            cardinalityMinText.addModifyListener(updateListener);
            cardinalityMaxText.addModifyListener(updateListener);
            cardinalityMinText.addVerifyListener(digitsListener);
            cardinalityMaxText.addVerifyListener(digitsListener);

            eventTypeButton = new Button(group, SWT.CHECK);
            eventTypeButton.setText(Messages.CustomTxtParserInputWizardPage_eventType);
            eventTypeButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
            eventTypeButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (eventTypeButton.getSelection()) {
                        eventTypeText.setEnabled(true);
                    } else {
                        eventTypeText.setEnabled(false);
                    }
                }
            });
            eventTypeButton.addSelectionListener(updateListener);

            eventTypeText = new Text(group, SWT.BORDER | SWT.SINGLE);
            gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gd.widthHint = 0;
            eventTypeText.setLayoutData(gd);
            if (inputLine.eventType != null) {
                eventTypeText.setText(inputLine.eventType);
                eventTypeButton.setSelection(true);
            } else {
                eventTypeText.setEnabled(false);
                eventTypeButton.setSelection(false);
            }
            eventTypeText.addModifyListener(updateListener);

            if (inputLine.columns != null) {
                for (InputData inputData : inputLine.columns) {
                    InputGroup inputGroup = new InputGroup(group, this, inputs.size() + 1);
                    if (inputData.tag.equals(Tag.TIMESTAMP)) {
                        inputGroup.tagCombo.select(0);
                        inputGroup.tagText.setText(inputData.format);
                        inputGroup.tagLabel.setText(Messages.CustomTxtParserInputWizardPage_format);
                        inputGroup.tagLabel.setVisible(true);
                        inputGroup.tagText.setVisible(true);
                        inputGroup.tagText.addModifyListener(updateListener);
                        inputGroup.actionCombo.setVisible(true);
                    } else if (inputData.tag.equals(Tag.EVENT_TYPE)) {
                        inputGroup.tagCombo.select(1);
                        inputGroup.actionCombo.setVisible(true);
                    } else if (inputData.tag.equals(Tag.MESSAGE)) {
                        inputGroup.tagCombo.select(2);
                        inputGroup.actionCombo.setVisible(true);
                    } else if (inputData.tag.equals(Tag.EXTRA_FIELD_NAME)) {
                        inputGroup.tagCombo.select(3);
                        inputGroup.actionCombo.setVisible(false);
                    } else if (inputData.tag.equals(Tag.EXTRA_FIELD_VALUE)) {
                        inputGroup.tagCombo.select(4);
                        inputGroup.actionCombo.setVisible(true);
                    } else {
                        inputGroup.tagCombo.select(5);
                        inputGroup.tagText.setText(inputData.name);
                        inputGroup.tagLabel.setText(Messages.CustomTxtParserInputWizardPage_name);
                        inputGroup.tagLabel.setVisible(true);
                        inputGroup.tagText.setVisible(true);
                        inputGroup.tagText.addModifyListener(updateListener);
                        inputGroup.actionCombo.setVisible(true);
                    }
                    inputGroup.actionCombo.select(inputData.action);
                    inputs.add(inputGroup);
                }
            }

            createAddGroupButton();
        }

        private void createAddGroupButton() {
            addGroupButton = new Button(group, SWT.PUSH);
            addGroupButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
            addGroupButton.setImage(ADD_IMAGE);
            addGroupButton.setToolTipText(Messages.CustomTxtParserInputWizardPage_addGroup);
            addGroupButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    removeAddGroupButton();
                    inputs.add(new InputGroup(group, Line.this, inputs.size() + 1));
                    createAddGroupButton();
                    lineContainer.layout();
                    lineScrolledComposite.setMinSize(lineContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, lineContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y - 1);
                    group.getParent().layout();
                    validate();
                    updatePreviews();
                }
            });

            addGroupLabel = new Label(group, SWT.NULL);
            addGroupLabel.setText(Messages.CustomTxtParserInputWizardPage_newGroup);
        }

        private void removeAddGroupButton() {
            addGroupButton.dispose();
            addGroupLabel.dispose();
        }

        private void removeInput(int inputNumber) {
            int nb = inputNumber;
            if (--nb < inputs.size()) {
                inputs.remove(nb).dispose();
                for (int i = nb; i < inputs.size(); i++) {
                    inputs.get(i).setInputNumber(i + 1);
                }
                lineContainer.layout();
                lineScrolledComposite.setMinSize(lineContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, lineContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT).y - 1);
                group.getParent().layout();
            }
        }

        private void dispose() {
            group.dispose();
        }

        private void extractInputs() {
            inputLine.setRegex(selectedLine.regexText.getText());
            inputLine.eventType = selectedLine.eventTypeButton.getSelection() ? selectedLine.eventTypeText.getText().trim() : null;
            switch (cardinalityCombo.getSelectionIndex()) {
            case 0:
                inputLine.cardinality = Cardinality.ZERO_OR_MORE;
                break;
            case 1:
                inputLine.cardinality = Cardinality.ONE_OR_MORE;
                break;
            case 2:
                inputLine.cardinality = Cardinality.ZERO_OR_ONE;
                break;
            case 3:
                inputLine.cardinality = Cardinality.ONE;
                break;
            case 4: // (?,?)
                int min,
                max;
                try {
                    min = Integer.parseInt(cardinalityMinText.getText());
                } catch (NumberFormatException e) {
                    min = -1;
                }
                try {
                    if (cardinalityMaxText.getText().equals(INFINITY_STRING)) {
                        max = Cardinality.INF;
                    } else {
                        max = Integer.parseInt(cardinalityMaxText.getText());
                    }
                } catch (NumberFormatException e) {
                    max = -1;
                }
                inputLine.cardinality = new Cardinality(min, max);
                break;
            default:
                inputLine.cardinality = Cardinality.ZERO_OR_MORE;
                break;
            }
            inputLine.columns = new ArrayList<>(inputs.size());
            for (int i = 0; i < inputs.size(); i++) {
                InputGroup grp = inputs.get(i);
                InputData inputData = new InputData();
                inputData.tag = Tag.fromLabel(grp.tagCombo.getText());
                if (inputData.tag.equals(Tag.OTHER)) {
                    inputData.name = grp.tagText.getText().trim();
                } else {
                    inputData.name = inputData.tag.toString();
                    if (inputData.tag.equals(Tag.TIMESTAMP)) {
                        inputData.format = grp.tagText.getText().trim();
                    }
                }
                inputData.action = grp.actionCombo.getSelectionIndex();
                inputLine.columns.add(inputData);
            }
        }
    }

    private class InputGroup {
        private Line line;
        private int inputNumber;

        // children of parent (must be disposed)
        private Composite labelComposite;
        private Composite tagComposite;
        private Label previewLabel;
        private Text previewText;

        // children of labelComposite
        private Label inputLabel;

        // children of tagComposite
        private Combo tagCombo;
        private Label tagLabel;
        private Text tagText;
        private Combo actionCombo;

        public InputGroup(Composite parent, Line line, int inputNumber) {
            this.line = line;
            this.inputNumber = inputNumber;

            labelComposite = new Composite(parent, SWT.FILL);
            GridLayout labelLayout = new GridLayout(2, false);
            labelLayout.marginWidth = 0;
            labelLayout.marginHeight = 0;
            labelComposite.setLayout(labelLayout);
            labelComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

            Button deleteButton = new Button(labelComposite, SWT.PUSH);
            deleteButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
            deleteButton.setImage(DELETE_IMAGE);
            deleteButton.setToolTipText(Messages.CustomTxtParserInputWizardPage_removeGroup);
            deleteButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    InputGroup.this.line.removeInput(InputGroup.this.inputNumber);
                    validate();
                    updatePreviews();
                }
            });

            inputLabel = new Label(labelComposite, SWT.NULL);
            inputLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
            inputLabel.setText(NLS.bind(Messages.CustomTxtParserInputWizardPage_group, inputNumber));

            tagComposite = new Composite(parent, SWT.FILL);
            GridLayout tagLayout = new GridLayout(4, false);
            tagLayout.marginWidth = 0;
            tagLayout.marginHeight = 0;
            tagComposite.setLayout(tagLayout);
            tagComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

            tagCombo = new Combo(tagComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
            tagCombo.setItems(new String[] {
                    Tag.TIMESTAMP.toString(),
                    Tag.EVENT_TYPE.toString(),
                    Tag.MESSAGE.toString(),
                    Tag.EXTRA_FIELD_NAME.toString(),
                    Tag.EXTRA_FIELD_VALUE.toString(),
                    Tag.OTHER.toString()});
            tagCombo.select(2);
            tagCombo.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    // Do nothing
                }

                @Override
                public void widgetSelected(SelectionEvent e) {
                    tagText.removeModifyListener(updateListener);
                    switch (tagCombo.getSelectionIndex()) {
                    case 0: // Time Stamp
                        tagLabel.setText(Messages.CustomTxtParserInputWizardPage_format);
                        tagLabel.setVisible(true);
                        tagText.setVisible(true);
                        tagText.addModifyListener(updateListener);
                        actionCombo.setVisible(true);
                        break;
                    case 1: // Event type
                        tagLabel.setVisible(false);
                        tagText.setVisible(false);
                        actionCombo.setVisible(true);
                        break;
                    case 2: // Message
                        tagLabel.setVisible(false);
                        tagText.setVisible(false);
                        actionCombo.setVisible(true);
                        break;
                    case 3: // Field name
                        tagLabel.setVisible(false);
                        tagText.setVisible(false);
                        actionCombo.setVisible(false);
                        break;
                    case 4: // Field type
                        tagLabel.setVisible(false);
                        tagText.setVisible(false);
                        actionCombo.setVisible(true);
                        break;
                    case 5: // Other
                        tagLabel.setText(Messages.CustomTxtParserInputWizardPage_name);
                        tagLabel.setVisible(true);
                        tagText.setVisible(true);
                        tagText.addModifyListener(updateListener);
                        actionCombo.setVisible(true);
                        break;
                    default:
                        break;
                    }
                    tagComposite.layout();
                    validate();
                    updatePreviews();
                }
            });

            tagLabel = new Label(tagComposite, SWT.NULL);
            tagLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
            tagLabel.setVisible(false);

            tagText = new Text(tagComposite, SWT.BORDER | SWT.SINGLE);
            GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gd.widthHint = 0;
            tagText.setLayoutData(gd);
            tagText.setVisible(false);

            actionCombo = new Combo(tagComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
            actionCombo.setItems(new String[] { Messages.CustomTxtParserInputWizardPage_set, Messages.CustomTxtParserInputWizardPage_append, Messages.CustomTxtParserInputWizardPage_appendWith });
            actionCombo.select(0);
            actionCombo.addSelectionListener(updateListener);

            previewLabel = new Label(parent, SWT.NULL);
            previewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
            previewLabel.setText(Messages.CustomTxtParserInputWizardPage_preview);

            previewText = new Text(parent, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
            gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gd.widthHint = 0;
            previewText.setLayoutData(gd);
            previewText.setText(Messages.CustomTxtParserInputWizardPage_noMatch);
            previewText.setBackground(COLOR_WIDGET_BACKGROUND);
        }

        private void dispose() {
            labelComposite.dispose();
            tagComposite.dispose();
            previewLabel.dispose();
            previewText.dispose();
        }

        private void setInputNumber(int inputNumber) {
            this.inputNumber = inputNumber;
            inputLabel.setText(NLS.bind(Messages.CustomTxtParserInputWizardPage_group, inputNumber));
            labelComposite.layout();
        }
    }

    private void validate() {

        definition.categoryName = categoryText.getText().trim();
        definition.definitionName = logtypeText.getText().trim();
        definition.timeStampOutputFormat = timestampOutputFormatText.getText().trim();

        if (selectedLine != null) {
            selectedLine.extractInputs();
            treeViewer.refresh();
        }

        StringBuffer errors = new StringBuffer();

        if (definition.categoryName.length() == 0) {
            errors.append("Enter a category for the new trace type. "); //$NON-NLS-1$
            categoryText.setBackground(COLOR_LIGHT_RED);
        } else if (definition.definitionName.length() == 0) {
            errors.append("Enter a name for the new trace type. "); //$NON-NLS-1$
            logtypeText.setBackground(COLOR_LIGHT_RED);
        } else {
            categoryText.setBackground(COLOR_TEXT_BACKGROUND);
            logtypeText.setBackground(COLOR_TEXT_BACKGROUND);
            if (definition.categoryName.indexOf(':') != -1) {
                errors.append("Invalid character ':' in category. "); //$NON-NLS-1$
                categoryText.setBackground(COLOR_LIGHT_RED);
            }
            if (definition.definitionName.indexOf(':') != -1) {
                errors.append("Invalid character ':' in trace type. "); //$NON-NLS-1$
                logtypeText.setBackground(COLOR_LIGHT_RED);
            }
            for (TraceTypeHelper helper : TmfTraceType.getTraceTypeHelpers()) {
                if (definition.categoryName.equals(helper.getCategoryName()) &&
                        definition.definitionName.equals(helper.getName()) &&
                        (editDefinitionName == null || !editDefinitionName.equals(definition.definitionName)) &&
                        (editCategoryName == null || !editCategoryName.equals(definition.categoryName))) {
                    errors.append("The trace type name already exists. "); //$NON-NLS-1$
                    logtypeText.setBackground(COLOR_LIGHT_RED);
                    break;
                }
            }
        }

        timestampFound = false;
        for (int i = 0; i < definition.inputs.size(); i++) {

            InputLine inputLine = definition.inputs.get(i);
            String name = Integer.toString(i + 1);
            errors.append(validateLine(inputLine, name));
        }
        if (timestampFound && !definition.timeStampOutputFormat.isEmpty()) {
            try {
                new TmfTimestampFormat(definition.timeStampOutputFormat);
                timestampOutputFormatText.setBackground(COLOR_TEXT_BACKGROUND);
            } catch (IllegalArgumentException e) {
                errors.append("Enter a valid output format for the Time Stamp field [" + e.getMessage() + "]."); //$NON-NLS-1$ //$NON-NLS-2$
                timestampOutputFormatText.setBackground(COLOR_LIGHT_RED);
            }
        } else {
            timestampOutputFormatText.setBackground(COLOR_TEXT_BACKGROUND);
        }

        if (errors.length() == 0) {
            setDescription(defaultDescription);
            setPageComplete(true);
        } else {
            setDescription(errors.toString());
            setPageComplete(false);
        }
    }

    /**
     * Validate an input line.
     *
     * @param inputLine
     *            The line to clean up
     * @param name
     *            The name of the line
     * @return The cleaned up line
     */
    public StringBuffer validateLine(InputLine inputLine, String name) {
        StringBuffer errors = new StringBuffer();
        Line line = null;
        if (selectedLine != null && selectedLine.inputLine.equals(inputLine)) {
            line = selectedLine;
        }
        try {
            Pattern.compile(inputLine.getRegex());
            if (line != null) {
                line.regexText.setBackground(COLOR_TEXT_BACKGROUND);
            }
        } catch (PatternSyntaxException e) {
            errors.append("Enter a valid regular expression (Line " + name + "). "); //$NON-NLS-1$ //$NON-NLS-2$
            if (line != null) {
                line.regexText.setBackground(COLOR_LIGHT_RED);
            }
        }
        if (inputLine.getMinCount() == -1) {
            errors.append("Enter a minimum value for cardinality (Line " + name + "). "); //$NON-NLS-1$ //$NON-NLS-2$
            if (line != null) {
                line.cardinalityMinText.setBackground(COLOR_LIGHT_RED);
            }
        } else {
            if (line != null) {
                line.cardinalityMinText.setBackground(COLOR_TEXT_BACKGROUND);
            }
        }
        if (inputLine.getMaxCount() == -1) {
            errors.append("Enter a maximum value for cardinality (Line " + name + "). "); //$NON-NLS-1$ //$NON-NLS-2$
            if (line != null) {
                line.cardinalityMaxText.setBackground(COLOR_LIGHT_RED);
            }
        } else if (inputLine.getMinCount() > inputLine.getMaxCount()) {
            errors.append("Enter correct (min <= max) values for cardinality (Line " + name + "). "); //$NON-NLS-1$ //$NON-NLS-2$
            if (line != null) {
                line.cardinalityMinText.setBackground(COLOR_LIGHT_RED);
            }
            if (line != null) {
                line.cardinalityMaxText.setBackground(COLOR_LIGHT_RED);
            }
        } else {
            if (line != null) {
                line.cardinalityMaxText.setBackground(COLOR_TEXT_BACKGROUND);
            }
        }
        if (inputLine.eventType != null && inputLine.eventType.trim().isEmpty()) {
            errors.append("Enter the event type (Line " + name + "). "); //$NON-NLS-1$ //$NON-NLS-2$
            if (line != null) {
                line.eventTypeText.setBackground(COLOR_LIGHT_RED);
            }
        } else {
            if (line != null) {
                line.eventTypeText.setBackground(COLOR_TEXT_BACKGROUND);
            }
        }
        for (int i = 0; inputLine.columns != null && i < inputLine.columns.size(); i++) {
            InputData inputData = inputLine.columns.get(i);
            InputGroup group = null;
            if (line != null) {
                group = line.inputs.get(i);
            }
            if (inputData.tag.equals(Tag.TIMESTAMP)) {
                timestampFound = true;
                if (inputData.format.length() == 0) {
                    errors.append("Enter the input format for the Time Stamp (Line " + name + " Group " + (i + 1) + "). "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    if (group != null) {
                        group.tagText.setBackground(COLOR_LIGHT_RED);
                    }
                } else {
                    try {
                        new TmfTimestampFormat(inputData.format);
                        if (group != null) {
                            group.tagText.setBackground(COLOR_TEXT_BACKGROUND);
                        }
                    } catch (IllegalArgumentException e) {
                        errors.append("Enter a valid input format for the Time Stamp (Line " + name + " Group " + (i + 1) + ") [" + e.getMessage() + "]. "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        if (group != null) {
                            group.tagText.setBackground(COLOR_LIGHT_RED);
                        }
                    }
                }
            } else if (inputData.tag.equals(Tag.OTHER)) {
                if (inputData.name.isEmpty()) {
                    errors.append("Enter a name for the data group (Line " + name + " Group " + (i + 1) + "). "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    if (group != null) {
                        group.tagText.setBackground(COLOR_LIGHT_RED);
                    }
                } else if (Tag.fromLabel(inputData.name) != null) {
                    errors.append("Cannot use reserved name for the data group (Line " + name + " Group " + (i + 1) + "). "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    if (group != null) {
                        group.tagText.setBackground(COLOR_LIGHT_RED);
                    }
                } else {
                    if (group != null) {
                        group.tagText.setBackground(COLOR_TEXT_BACKGROUND);
                    }
                }
            } else {
                if (group != null) {
                    group.tagText.setBackground(COLOR_TEXT_BACKGROUND);
                }
            }
        }
        for (int i = 0; inputLine.childrenInputs != null && i < inputLine.childrenInputs.size(); i++) {
            errors.append(validateLine(inputLine.childrenInputs.get(i), name + "." + (i + 1))); //$NON-NLS-1$
        }
        return errors;
    }

    /**
     * Get the trace definition.
     *
     * @return The trace definition
     */
    public CustomTxtTraceDefinition getDefinition() {
        return definition;
    }

    /**
     * Get the raw text of the input.
     *
     * @return The raw input text
     */
    public char[] getInputText() {
        return inputText.getText().toCharArray();
    }
}
