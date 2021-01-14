/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Philippe Proulx
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.handler;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.LamiConfigUtils;
import org.eclipse.tracecompass.tmf.core.analysis.ondemand.OnDemandAnalysisManager;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfUserDefinedOnDemandAnalysisElement;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * The command handler for the "Remove External Analysis" menu option.
 *
 * @author Philippe Proulx
 */
public class RemoveAnalysisHandler extends AbstractHandler {

    @Override
    public @Nullable Object execute(@Nullable ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);

        // Selection type should have been validated by the plugin.xml
        if (!(selection instanceof IStructuredSelection)) {
            throw new IllegalStateException("Handler called on invalid selection"); //$NON-NLS-1$
        }
        IStructuredSelection sel = (IStructuredSelection) selection;
        List<?> elements = sel.toList();

        OnDemandAnalysisManager mgr = OnDemandAnalysisManager.getInstance();

        Optional<TmfUserDefinedOnDemandAnalysisElement> optionalParent = elements.stream()
                .filter(TmfUserDefinedOnDemandAnalysisElement.class::isInstance)
                .map(TmfUserDefinedOnDemandAnalysisElement.class::cast)
                .findFirst();

        elements.stream()
                .filter(TmfUserDefinedOnDemandAnalysisElement.class::isInstance)
                .map(TmfUserDefinedOnDemandAnalysisElement.class::cast)
                .map(TmfUserDefinedOnDemandAnalysisElement::getAnalysis)
                .forEach(analysis -> {
                    /* Unregister from the manager */
                    mgr.unregisterAnalysis(analysis);

                    /* Remove the corresponding configuration file */
                    try {
                        LamiConfigUtils.removeConfigFile(analysis.getName());
                    } catch (IOException e) {
                        // Ignore this: not the end of the world
                    }
                });

        /* Refresh the project explorer */
        optionalParent.ifPresent(TmfUserDefinedOnDemandAnalysisElement::refresh);

        return null;
    }

}
