/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.handler;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfReportElement;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * The command handler for the "Delete Report" menu option for Report project
 * model elements.
 *
 * @author Alexandre Montplaisir
 */
public class DeleteReportHandler extends AbstractHandler {

    @Override
    public @Nullable Object execute(@Nullable ExecutionEvent event) throws ExecutionException {
        /* Types should have been checked by the plugin.xml already */
        ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
        List<?> elements = ((IStructuredSelection) selection).toList();

        /* Ask the parent element to remove each corresponding report. */
        elements.stream()
                .filter(TmfReportElement.class::isInstance)
                .map(TmfReportElement.class::cast)
                .forEach(reportElem -> reportElem.getParent().removeReport(reportElem.getReport()));

        return null;
    }
}
