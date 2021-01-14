/*******************************************************************************
 * Copyright (c) 2011, 2020 Ericsson
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
 *   Patrick Tasse - Added drag and drop messages
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.handlers;

import org.eclipse.osgi.util.NLS;

/**
 * Messages file
 *
 * @author Francois Chouinard
 * @version 1.0
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.tmf.ui.project.handlers.messages"; //$NON-NLS-1$

    public static String DeleteDialog_Title;
    public static String DeleteTraceHandler_Message;
    public static String DeleteTraceHandlerGeneric_Message;
    public static String DeleteTraceHandlerGeneric_Error;
    public static String DeleteTraceHandlerGeneric_TaskName;
    public static String DeleteExperimentHandler_Message;
    public static String DeleteExperimentHandler_Error;
    public static String DeleteExperimentHandler_TaskName;
    public static String DeleteFolderHandler_Message;
    public static String CloseTraces_TaskName;

    public static String RemoveDialog_Title;
    public static String RemoveTraceFromExperimentHandler_Message;
    public static String RemoveTraceFromExperimentHandler_TaskName;
    public static String RemoveTraceFromExperimentHandler_Error;

    public static String ClearDialog_Title;
    public static String DeleteFolderHandlerClear_Message;

    public static String SelectTraceTypeHandler_ErrorSelectingTrace;
    public static String SelectTraceTypeHandler_Title;
    public static String SelectTraceTypeHandler_TraceFailedValidation;
    public static String SelectTraceTypeHandler_TracesFailedValidation;
    public static String SelectTraceTypeHandler_InvalidTraceType;

    public static String DropAdapterAssistant_RenameTraceTitle;
    public static String DropAdapterAssistant_RenameTraceMessage;

    public static String SynchronizeTracesHandler_InitError;
    public static String SynchronizeTracesHandler_CopyProblem;
    public static String SynchronizeTracesHandler_WrongType;
    public static String SynchronizeTracesHandler_WrongTraceNumber;
    public static String SynchronizeTracesHandler_Title;
    public static String SynchronizeTracesHandler_Error;
    public static String SynchronizeTracesHandler_ErrorSynchingExperiment;
    public static String SynchronizeTracesHandler_ErrorSynchingForTrace;

    public static String ClearTraceOffsetHandler_Title;
    public static String ClearTraceOffsetHandler_ConfirmMessage;

    public static String DeleteSupplementaryFiles_DeletionTask;
    public static String DeleteSupplementaryFiles_ProjectRefreshTask;

    public static String TrimTraceHandler_failMsg;

    public static String TrimTraces_JobName;
    public static String TrimTraces_DirectoryChooser_DialogTitle;
    public static String TrimTraces_InvalidTimeRange_DialogTitle;
    public static String TrimTraces_InvalidTimeRange_DialogText;
    public static String TrimTraces_InvalidDirectory_DialogTitle;
    public static String TrimTraces_InvalidDirectory_DialogText;
    public static String TrimTraces_NoWriteAccess_DialogText;

    public static String AnalysisModule_Help;

    public static String TmfActionProvider_OpenWith;

    public static String OpenAsExperimentHandler_DefaultExperimentName;
    public static String OpenAsExperimentHandler_ValidationErrorTitle;
    public static String OpenAsExperimentHandler_ValidationErrorMessage;
    public static String OpenAsExperimentHandler_OpeningErrorTitle;
    public static String OpenAsExperimentHandler_OpeningErrorMessage;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
