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
package org.eclipse.tracecompass.internal.lttng2.control.stubs.dialogs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.tracecompass.internal.lttng2.control.ui.views.dialogs.IAddContextDialog;

/**
 * Add Context dialog stub.
 */
@SuppressWarnings("javadoc")
public class AddContextDialogStub implements IAddContextDialog {

    private Set<String> fAvailableContexts = null;
    private List<String> fContexts = null;

    @Override
    public int open() {
        return 0;
    }

    @Override
    public void setAvalibleContexts(List<String> contexts) {
        fAvailableContexts = new HashSet<>();
        fAvailableContexts.addAll(contexts);
    }

    @Override
    public List<String> getContexts() {
        List<String> ret = new ArrayList<>();
        ret.addAll(fContexts);
        return ret;
    }

    public void setContexts(List<String> contexts) throws IllegalArgumentException{
        fContexts = new ArrayList<>();
        fContexts.addAll(contexts);
        // If availableContexts are null we cannot verify
        if (fAvailableContexts != null) {
            for (Iterator<String> iterator = fContexts.iterator(); iterator.hasNext();) {
                String string = iterator.next();
                if (!fAvailableContexts.contains(string)) {
                    throw new IllegalArgumentException();
                }
            }
        }
    }
}

