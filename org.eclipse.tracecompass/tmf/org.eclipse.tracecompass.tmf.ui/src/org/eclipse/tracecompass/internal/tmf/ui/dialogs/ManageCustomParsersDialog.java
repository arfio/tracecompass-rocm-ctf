/*******************************************************************************
 * Copyright (c) 2010, 2017 Ericsson
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

package org.eclipse.tracecompass.internal.tmf.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.internal.tmf.ui.parsers.CustomParserUtils;
import org.eclipse.tracecompass.internal.tmf.ui.parsers.wizards.CustomTxtParserWizard;
import org.eclipse.tracecompass.internal.tmf.ui.parsers.wizards.CustomXmlParserWizard;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtTrace;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtTraceDefinition;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlTrace;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.tracecompass.tmf.ui.dialog.TmfFileDialogFactory;

/**
 * Dialog for custom text parsers.
 *
 * @author Patrick Tassé
 */
public class ManageCustomParsersDialog extends Dialog {

    private static final String SEP = " : "; //$NON-NLS-1$
    private static final int SEP_LEN = SEP.length();

    private static final Image image = Activator.getDefault().getImageFromPath("/icons/etool16/customparser_wizard.gif"); //$NON-NLS-1$

    Button txtButton;
    Button xmlButton;
    List parserList;
    Button newButton;
    Button editButton;
    Button deleteButton;
    Button importButton;
    Button exportButton;

    /**
     * Constructor
     *
     * @param parent
     *            Parent shell of this dialog
     */
    public ManageCustomParsersDialog(Shell parent) {
        super(parent);
        setShellStyle(SWT.RESIZE | SWT.MAX | getShellStyle());
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        getShell().setText(Messages.ManageCustomParsersDialog_DialogHeader);
        getShell().setImage(image);

        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite listContainer = new Composite(composite, SWT.NONE);
        listContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout lcgl = new GridLayout();
        lcgl.marginHeight = 0;
        lcgl.marginWidth = 0;
        listContainer.setLayout(lcgl);

        Composite radioContainer = new Composite(listContainer, SWT.NONE);
        GridLayout rcgl = new GridLayout(2, true);
        rcgl.marginHeight = 0;
        rcgl.marginWidth = 0;
        radioContainer.setLayout(rcgl);

        txtButton = new Button(radioContainer, SWT.RADIO);
        txtButton.setText(Messages.ManageCustomParsersDialog_TextButtonLabel);
        txtButton.setSelection(true);
        txtButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Do nothing
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                fillParserList();
            }
        });

        xmlButton = new Button(radioContainer, SWT.RADIO);
        xmlButton.setText("XML"); //$NON-NLS-1$
        xmlButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Do nothing
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                fillParserList();
            }
        });

        parserList = new List(listContainer, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        parserList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        parserList.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Do nothing
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (parserList.getSelectionCount() == 0) {
                    editButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                    exportButton.setEnabled(false);
                } else {
                    editButton.setEnabled(true);
                    deleteButton.setEnabled(true);
                    exportButton.setEnabled(true);
                }
            }
        });

        Composite buttonContainer = new Composite(composite, SWT.NULL);
        buttonContainer.setLayout(new GridLayout());
        buttonContainer.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));

        newButton = new Button(buttonContainer, SWT.PUSH);
        newButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        newButton.setText(Messages.ManageCustomParsersDialog_NewButtonLabel);
        newButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Do nothing
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                WizardDialog dialog = null;
                if (txtButton.getSelection()) {
                    dialog = new WizardDialog(getShell(), new CustomTxtParserWizard());
                } else if (xmlButton.getSelection()) {
                    dialog = new WizardDialog(getShell(), new CustomXmlParserWizard());
                }
                if (dialog != null) {
                    dialog.open();
                    if (dialog.getReturnCode() == Window.OK) {
                        fillParserList();
                    }
                }
            }
        });

        editButton = new Button(buttonContainer, SWT.PUSH);
        editButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        editButton.setText(Messages.ManageCustomParsersDialog_EditButtonLabel);
        editButton.setEnabled(false);
        editButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Do nothing
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                WizardDialog dialog = null;
                String selection = parserList.getSelection()[0];
                String category = selection.substring(0, selection.indexOf(SEP));
                String name = selection.substring(selection.indexOf(SEP) + SEP_LEN);
                if (txtButton.getSelection()) {
                    dialog = new WizardDialog(getShell(),
                            new CustomTxtParserWizard(CustomTxtTraceDefinition.load(category, name)));
                } else if (xmlButton.getSelection()) {
                    dialog = new WizardDialog(getShell(),
                            new CustomXmlParserWizard(CustomXmlTraceDefinition.load(category, name)));
                }
                if (dialog != null) {
                    dialog.open();
                    if (dialog.getReturnCode() == Window.OK) {
                        fillParserList();
                    }
                }
            }
        });

        deleteButton = new Button(buttonContainer, SWT.PUSH);
        deleteButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        deleteButton.setText(Messages.ManageCustomParsersDialog_DeleteButtonLabel);
        deleteButton.setEnabled(false);
        deleteButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Do nothing
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean confirm = MessageDialog.openQuestion(
                        getShell(),
                        Messages.ManageCustomParsersDialog_DeleteParserDialogHeader,
                        NLS.bind(Messages.ManageCustomParsersDialog_DeleteConfirmation, parserList.getSelection()[0]));
                if (confirm) {
                    String selection = parserList.getSelection()[0];
                    String category = selection.substring(0, selection.indexOf(SEP));
                    String name = selection.substring(selection.indexOf(SEP) + SEP_LEN);
                    if (txtButton.getSelection()) {
                        CustomTxtTraceDefinition.delete(category, name);
                        CustomParserUtils.cleanup(CustomTxtTrace.buildTraceTypeId(category, name));
                    } else if (xmlButton.getSelection()) {
                        CustomXmlTraceDefinition.delete(category, name);
                        CustomParserUtils.cleanup(CustomXmlTrace.buildTraceTypeId(category, name));
                    }
                    fillParserList();
                }
            }
        });

        new Label(buttonContainer, SWT.NONE); // filler

        importButton = new Button(buttonContainer, SWT.PUSH);
        importButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        importButton.setText(Messages.ManageCustomParsersDialog_ImportButtonLabel);
        importButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Do nothing
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = TmfFileDialogFactory.create(Display.getCurrent().getActiveShell(), SWT.OPEN);
                dialog.setText(Messages.ManageCustomParsersDialog_ImportParserSelection);
                dialog.setFilterExtensions(new String[] { "*.xml", "*" }); //$NON-NLS-1$ //$NON-NLS-2$
                String path = dialog.open();
                if (path != null) {
                    CustomTraceDefinition[] defs = null;
                    if (txtButton.getSelection()) {
                        defs = CustomTxtTraceDefinition.loadAll(path);
                    } else if (xmlButton.getSelection()) {
                        defs = CustomXmlTraceDefinition.loadAll(path);
                    }
                    if (defs != null && defs.length > 0) {
                        for (CustomTraceDefinition def : defs) {
                            boolean ok = checkNameConflict(def);
                            if (ok) {
                                def.save();
                                CustomParserUtils.cleanup(CustomTxtTrace.buildTraceTypeId(def.categoryName, def.definitionName));
                            }
                        }
                        fillParserList();
                    } else {
                        MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
                                Messages.ManageCustomParsersDialog_ImportFailureTitle,
                                Messages.ManageCustomParsersDialog_ImportFailureMessage);
                    }
                }
            }
        });

        exportButton = new Button(buttonContainer, SWT.PUSH);
        exportButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        exportButton.setText(Messages.ManageCustomParsersDialog_ExportButtonLabel);
        exportButton.setEnabled(false);
        exportButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // Do nothing
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = TmfFileDialogFactory.create(Display.getCurrent().getActiveShell(), SWT.SAVE);
                dialog.setText(NLS.bind(Messages.ManageCustomParsersDialog_ExportParserSelection, parserList.getSelection()[0]));
                dialog.setFilterExtensions(new String[] { "*.xml", "*" }); //$NON-NLS-1$ //$NON-NLS-2$
                String path = dialog.open();
                if (path != null) {
                    String selection = parserList.getSelection()[0];
                    String category = selection.substring(0, selection.indexOf(SEP));
                    String name = selection.substring(selection.indexOf(SEP) + SEP_LEN);
                    CustomTraceDefinition def = null;
                    if (txtButton.getSelection()) {
                        def = CustomTxtTraceDefinition.load(category, name);
                    } else if (xmlButton.getSelection()) {
                        def = CustomXmlTraceDefinition.load(category, name);
                    }
                    if (def != null) {
                        def.save(path);
                    }
                }
            }
        });

        fillParserList();

        getShell().setMinimumSize(300, 275);
        return composite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL, false);
    }

    private void fillParserList() {
        parserList.removeAll();
        if (txtButton.getSelection()) {
            for (CustomTxtTraceDefinition def : CustomTxtTraceDefinition.loadAll(false)) {
                parserList.add(def.categoryName + SEP + def.definitionName);
            }
        } else if (xmlButton.getSelection()) {
            for (CustomXmlTraceDefinition def : CustomXmlTraceDefinition.loadAll(false)) {
                parserList.add(def.categoryName + SEP + def.definitionName);
            }
        }
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        exportButton.setEnabled(false);
    }

    private boolean checkNameConflict(CustomTraceDefinition def) {
        for (TraceTypeHelper helper : TmfTraceType.getTraceTypeHelpers()) {
            if (def.categoryName.equals(helper.getCategoryName()) &&
                    def.definitionName.equals(helper.getName())) {
                String newName = findAvailableName(def);
                MessageDialog dialog = new MessageDialog(
                        getShell(),
                        null,
                        null,
                        NLS.bind(Messages.ManageCustomParsersDialog_ConflictMessage,
                                new Object[] { def.categoryName, def.definitionName, newName}),
                        MessageDialog.QUESTION,
                        new String[] { Messages.ManageCustomParsersDialog_ConflictRenameButtonLabel,
                            Messages.ManageCustomParsersDialog_ConflictSkipButtonLabel },
                        0);
                int result = dialog.open();
                if (result == 0) {
                    def.definitionName = newName;
                    return true;
                }
                return false;
            }
        }
        return true;
    }

    private static String findAvailableName(CustomTraceDefinition def) {
        int i = 2;
        Iterable<TraceTypeHelper> helpers = TmfTraceType.getTraceTypeHelpers();
        while (true) {
            String newName = def.definitionName + '(' + Integer.toString(i++) + ')';
            boolean available = true;
            for (TraceTypeHelper helper : helpers) {
                if (def.categoryName.equals(helper.getCategoryName()) &&
                        newName.equals(helper.getName())) {
                    available = false;
                    break;
                }
            }
            if (available) {
                return newName;
            }
        }
    }
}
