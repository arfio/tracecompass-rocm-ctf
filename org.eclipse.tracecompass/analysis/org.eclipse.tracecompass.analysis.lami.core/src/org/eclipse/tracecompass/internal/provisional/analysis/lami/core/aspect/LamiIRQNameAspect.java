/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Philippe Proulx
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiTableEntry;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types.LamiData;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types.LamiIRQ;

/**
 * Aspect for the IRQ handler names.
 *
 * This resolves the interrupt handler name, (like i915) from a given table
 * entry.
 *
 * @author Philippe Proulx
 */
public class LamiIRQNameAspect extends LamiGenericAspect {

    /**
     * Constructor
     *
     * @param colName
     *            Column name
     * @param colIndex
     *            Column index
     */
    public LamiIRQNameAspect(String colName, int colIndex) {
        super(colName + " (" + Messages.LamiAspect_Name +')', null, colIndex, false, false); //$NON-NLS-1$
    }

    @Override
    public @Nullable String resolveString(LamiTableEntry entry) {
        LamiData data = entry.getValue(getColIndex());
        if (data instanceof LamiIRQ) {
            return ((LamiIRQ) data).getName();
        }
        /* Could be null, unknown, etc. */
        return data.toString();
    }

    @Override
    public @Nullable Number resolveNumber(LamiTableEntry entry) {
        return null;
    }
}
