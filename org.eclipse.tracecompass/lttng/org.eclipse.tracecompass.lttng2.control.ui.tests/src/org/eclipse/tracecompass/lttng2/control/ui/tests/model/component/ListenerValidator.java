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
package org.eclipse.tracecompass.lttng2.control.ui.tests.model.component;

import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.ITraceControlComponentChangedListener;

/**
 * The class can be used to validate the listener interface.
 */
@SuppressWarnings("javadoc")
public class ListenerValidator implements ITraceControlComponentChangedListener {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private boolean fisAddedCalled = false;
    private boolean fisRemoveCalled = false;
    private boolean fisChangedCalled = false;

    private ITraceControlComponent fParent = null;
    private ITraceControlComponent fChild = null;
    private ITraceControlComponent fChangedComponent = null;

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    public boolean isAddedCalled() {
        return fisAddedCalled;
    }

    public boolean isRemovedCalled() {
        return fisRemoveCalled;
    }

    public boolean isChangedCalled() {
        return fisChangedCalled;
    }

    public ITraceControlComponent getSavedParent() {
        return fParent;
    }

    public ITraceControlComponent getSavedChild() {
        return fChild;
    }

    public ITraceControlComponent getSavedComponent() {
        return fChangedComponent;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    public void initialize() {
        fisAddedCalled = false;
        fisRemoveCalled = false;
        fisChangedCalled = false;
        fParent = null;
        fChild = null;
        fChangedComponent = null;
    }

    @Override
    public void componentAdded(ITraceControlComponent parent, ITraceControlComponent component) {
        fisAddedCalled = true;
        fParent = parent;
        fChild = component;
    }

    @Override
    public void componentRemoved(ITraceControlComponent parent, ITraceControlComponent component) {
        fisRemoveCalled = true;
        fParent = parent;
        fChild = component;
    }

    @Override
    public void componentChanged(ITraceControlComponent component) {
        fisChangedCalled = true;
        fParent = null;
        fChangedComponent = component;
    }

}
