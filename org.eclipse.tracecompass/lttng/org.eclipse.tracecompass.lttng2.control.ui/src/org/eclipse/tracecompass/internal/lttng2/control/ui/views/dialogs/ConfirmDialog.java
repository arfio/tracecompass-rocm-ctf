/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/

package org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs;


import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;

/**
 * <p>
 * Confirmation dialog implementation.
 * </p>
 *
 * @author Bernd Hufmann
 */

public class ConfirmDialog implements IConfirmDialog {

    @Override
    public boolean openConfirm(Shell parent, String title, String message) {
        return MessageDialog.openConfirm(parent,
                Messages.TraceControl_DestroyConfirmationTitle,
                Messages.TraceControl_DestroyConfirmationMessage);
    }
}
