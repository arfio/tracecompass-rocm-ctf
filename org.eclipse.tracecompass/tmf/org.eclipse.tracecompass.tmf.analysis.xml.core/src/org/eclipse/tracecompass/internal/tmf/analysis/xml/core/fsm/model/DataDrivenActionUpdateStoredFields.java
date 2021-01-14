/*******************************************************************************
 * Copyright (c) 2016 Ecole Polytechnique de Montreal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model;

import java.util.Map.Entry;

import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.runtime.DataDrivenScenarioInfo;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.IAnalysisDataContainer;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.pattern.DataDrivenPattern;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;

/**
 * This action will update the value of the stored fields in the state system
 * based on the current event data.
 *
 * @author Jean-Christian Kouame
 */
public final class DataDrivenActionUpdateStoredFields implements DataDrivenAction  {

    private static final DataDrivenAction INSTANCE = new DataDrivenActionUpdateStoredFields();

    /**
     * Get the instance of this action
     *
     * @return The action
     */
    public static DataDrivenAction getInstance() {
        return INSTANCE;
    }

    /**
     * Constructor
     */
    private DataDrivenActionUpdateStoredFields() {
        // Do nothing
    }

    @Override
    public void eventHandle(ITmfEvent event, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container) {
        if (container instanceof DataDrivenPattern) {
            DataDrivenPattern patternSp = (DataDrivenPattern) container;
            for (Entry<String, String> entry : patternSp.getStoredFields().entrySet()) {
                ITmfEventField eventField = event.getContent().getField(entry.getKey());
                ITmfStateValue stateValue = null;
                if (eventField != null) {
                    final String alias = entry.getValue();
                    Object field = eventField.getValue();
                    if (field instanceof String) {
                        stateValue = TmfStateValue.newValueString((String) field);
                    } else if (field instanceof Long) {
                        stateValue = TmfStateValue.newValueLong(((Long) field).longValue());
                    } else if (field instanceof Integer) {
                        stateValue = TmfStateValue.newValueInt(((Integer) field).intValue());
                    } else if (field instanceof Double) {
                        stateValue = TmfStateValue.newValueDouble(((Double) field).doubleValue());
                    }
                    if (stateValue == null) {
                        throw new IllegalStateException("State value is null. Invalid type."); //$NON-NLS-1$
                    }
                    patternSp.getExecutionData().getHistoryBuilder().updateStoredFields(container, alias, stateValue, scenarioInfo, event);
                }
            }
        }
    }
}
