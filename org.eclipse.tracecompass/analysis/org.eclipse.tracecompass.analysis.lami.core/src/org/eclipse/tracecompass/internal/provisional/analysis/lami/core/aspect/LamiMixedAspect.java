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
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types.LamiData.DataType;

/**
 * Aspect for LAMI mixed types.
 *
 * The data in this column can contain any class of data.
 *
 * @author Philippe Proulx
 */
public class LamiMixedAspect extends LamiGenericAspect {

    /**
     * Constructor
     *
     * @param colName
     *            Column name
     * @param colIndex
     *            Column index
     */
    public LamiMixedAspect(String colName, int colIndex) {
        super(colName, null, colIndex, false, false);
    }

    @Override
    public @Nullable String resolveString(LamiTableEntry entry) {
        LamiData data = entry.getValue(getColIndex());
        Class<? extends LamiData> cls = data.getClass();

        DataType dataType = DataType.fromClass(cls);

        if (dataType == null) {
            return data.toString();
        }

        String str = data.toString();

        if (dataType.getUnits() != null) {
            str += " " + dataType.getUnits(); //$NON-NLS-1$
        }

        return str;
    }

    @Override
    public @Nullable Number resolveNumber(LamiTableEntry entry) {
        return null;
    }

}
