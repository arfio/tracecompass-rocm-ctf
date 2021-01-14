/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Add support for folder elements
 *   Marc-Andre Laperle - Preserve folder structure on import
 *   Bernd Hufmann - Extract ImportTraceWizard messages
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.wizards;

import org.eclipse.osgi.util.NLS;

/**
 * Message strings for TMF model handling.
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.tmf.ui.project.wizards.messages"; //$NON-NLS-1$

    /**
     * The dialog header of the new project wizard
     */
    public static String NewProjectWizard_DialogHeader;
    /**
     * The dialog message of the new project wizard
     */
    public static String NewProjectWizard_DialogMessage;
    /**
     * The title of the select traces wizard.
     */
    public static String SelectTracesWizard_WindowTitle;
    /**
     * The column header for the traces (select traces wizard page).
     */
    public static String SelectTracesWizardPage_TraceColumnHeader;
    /**
     * The title of select traces wizard page.
     */
    public static String SelectTracesWizardPage_WindowTitle;
    /**
     * The description of the select traces wizard page.
     */
    public static String SelectTracesWizardPage_Description;
    /**
     * The error message when selecting of traces for an experiment fails.
     */
    public static String SelectTracesWizardPage_SelectionError;
    /**
     * The cancel message for the trace selection operation.
     */
    public static String SelectTracesWizardPage_SelectionOperationCancelled;
    /**
     * The error message title.
     */
    public static String SelectTracesWizardPage_InternalErrorTitle;
    /**
     * The Label of the field to enter start time.
     * @since 3.1
     */
    public static String SelectTracesWizardPage_StartTime;
    /**
     * The Label of the field to enter end time.
     * @since 3.1
     */
    public static String SelectTracesWizardPage_EndTime;
    /**
     * The name of the button to enable time range filtering.
     * @since 3.1
     */
    public static String SelectTracesWizardPage_TimeRangeOptionButton;
    /**
     * The error message when the time range is not valid.
     * @since 3.1
     */
    public static String SelectTracesWizardPage_TimeRangeErrorMessage;
    /**
     * The error message when no name was entered in a dialog box (new trace or
     * experiment dialog)
     */
    public static String Dialog_EmptyNameError;
    /**
     * The error message when name of trace or experiment already exists
     */
    public static String Dialog_ExistingNameError;
    /**
     * The title of the new experiment dialog.
     */
    public static String NewExperimentDialog_DialogTitle;
    /**
     * The label of the new experiment name field.
     */
    public static String NewExperimentDialog_ExperimentName;
    /**
     * The title of the rename experiment dialog.
     */
    public static String RenameExperimentDialog_DialogTitle;
    /**
     * The label of the field of the current experiment name.
     */
    public static String RenameExperimentDialog_ExperimentName;
    /**
     * The label of the field for entering the new experiment name.
     */
    public static String RenameExperimentDialog_ExperimentNewName;
    /**
     * The title of the copy experiment dialog.
     */
    public static String CopyExperimentDialog_DialogTitle;
    /**
     * The label of the field of the current experiment name.
     */
    public static String CopyExperimentDialog_ExperimentName;
    /**
     * The label of the field for entering the new experiment name.
     */
    public static String CopyExperimentDialog_ExperimentNewName;
    /**
     * Text for deep copy of an experiment button
     * @since 3.3
     */
    public static String CopyExperimentDialog_DeepCopyButton;
    /**
     * Error message when the destination folder for traces already exist.
     * @since 3.3
     */
    public static String CopyExperimentDialog_DeepCopyError;
    /**
     * The title of the rename trace dialog.
     */
    public static String RenameTraceDialog_DialogTitle;
    /**
     * The label of the field of the current trace name.
     */
    public static String RenameTraceDialog_TraceName;
    /**
     * The label of the field for entering the new trace name.
     */
    public static String RenameTraceDialog_TraceNewName;
    /**
     * The title of the copy trace dialog.
     */
    public static String CopyTraceDialog_DialogTitle;
    /**
     * The label of the field of the current trace name.
     */
    public static String CopyTraceDialog_TraceName;
    /**
     * The label of the field for entering the new trace name.
     */
    public static String CopyTraceDialog_TraceNewName;
    /**
     * The description for how to copy a trace.
     * @since 3.3
     */
    public static String CopyTraceDialog_Description;
    /**
     * Text for copy as link button
     * @since 3.3
     */
    public static String CopyTraceDialog_CopyLinkButton;
    /**
     * Text for copy the trace button
     * @since 3.3
     */
    public static String CopyTraceDialog_CopyTraceButton;
    /**
     * The title of the new folder dialog.
     */
    public static String NewFolderDialog_DialogTitle;
    /**
     * The label of the new folder name field.
     */
    public static String NewFolderDialog_FolderName;
    /**
     * The title of the rename folder dialog.
     */
    public static String RenameFolderDialog_DialogTitle;
    /**
     * The label of the field of the current folder name.
     */
    public static String RenameFolderDialog_FolderName;
    /**
     * The label of the field for entering the new folder name.
     */
    public static String RenameFolderDialog_FolderNewName;
    /**
     * The title of the select root node wizard.
     *
     * The title of the select root node wizard.
     *
     * @since 2.0
     *
     */
    public static String SelectRootNodeWizard_WindowTitle;

    /**
     * The title of the select root node wizard page.
     *
     * @since 2.0
     *
     */
    public static String SelectRootNodeWizardPage_WindowTitle;

    /**
     * The description of the select root node wizard page.
     *
     * @since 2.0
     *
     */
    public static String SelectRootNodeWizardPage_Description;
    /**
     * The column header for the traces (select root node wizard page).
     *
     * @since 2.0
     */
    public static String SelectRootNodeWizardPage_TraceColumnHeader;

    /**
     * Trim trace title bar
     *
     * @since 4.1
     */
    public static String TrimTraceDialog_ExportTrimmedTrace;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
