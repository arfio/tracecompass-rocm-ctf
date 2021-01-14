/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import org.eclipse.jdt.annotation.Nullable;

final class LamiBoolean extends LamiData {

    private static final LamiBoolean TRUE = new LamiBoolean(true);
    private static final LamiBoolean FALSE = new LamiBoolean(false);

    public static LamiBoolean instance(boolean value) {
        return (value ? TRUE : FALSE);
    }

    private final boolean fValue;

    private LamiBoolean(boolean value) {
        fValue = value;
    }

    public boolean getValue() {
        return fValue;
    }

    @Override
    public @Nullable String toString() {
        return (fValue ?
                nullToEmptyString(Messages.LamiBoolean_Yes) :
                nullToEmptyString(Messages.LamiBoolean_No));
    }
}
