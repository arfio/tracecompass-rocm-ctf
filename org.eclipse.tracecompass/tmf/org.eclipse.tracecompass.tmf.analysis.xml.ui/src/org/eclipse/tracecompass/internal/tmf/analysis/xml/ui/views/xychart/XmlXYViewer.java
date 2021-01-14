/*******************************************************************************
 * Copyright (c) 2014, 2018 École Polytechnique de Montréal and others.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.xychart;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.DataDrivenXYDataProvider;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.XmlDataProviderManager;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.XmlViewInfo;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfXYDataProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.linechart.TmfFilteredXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.linechart.TmfXYChartSettings;
import org.w3c.dom.Element;

/**
 * Main viewer to display XML-defined xy charts. It uses an XML
 * {@link TmfXmlStrings#XY_VIEW} element from an XML file. This element defines
 * which entries from the state system will be shown and also gives additional
 * information on the presentation of the view.
 *
 * @author Geneviève Bastien
 */
public class XmlXYViewer extends TmfFilteredXYChartViewer {

    private final XmlViewInfo fViewInfo;

    /**
     * Constructor
     *
     * @param parent
     *            parent view
     * @param settings
     *            See {@link TmfXYChartSettings} to know what it contains
     * @param viewInfo
     *            The view info object
     */
    public XmlXYViewer(@Nullable Composite parent, TmfXYChartSettings settings, XmlViewInfo viewInfo) {
        super(parent, settings, DataDrivenXYDataProvider.ID);
        fViewInfo = viewInfo;
    }

    @Override
    protected @Nullable ITmfXYDataProvider initializeDataProvider(ITmfTrace trace) {
        Element viewElement = fViewInfo.getViewElement(TmfXmlStrings.XY_VIEW);
        if (viewElement == null) {
            return null;
        }
        return XmlDataProviderManager.getInstance().getXyProvider(trace, viewElement);
    }

    @Override
    public void updateContent() {
        // make public
        super.updateContent();
    }
}
