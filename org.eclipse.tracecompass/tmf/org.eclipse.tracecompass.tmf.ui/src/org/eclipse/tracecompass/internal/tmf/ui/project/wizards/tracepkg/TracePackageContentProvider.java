/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * A content provider to display the content of a trace package in a tree
 *
 * @author Marc-Andre Laperle
 */
public class TracePackageContentProvider implements ITreeContentProvider {

    @Override
    public void dispose() {
        // Do nothing
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // Do nothing
    }

    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof TracePackageElement[]) {
            return (TracePackageElement[]) inputElement;
        }
        return null;
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        return ((TracePackageElement) parentElement).getVisibleChildren();
    }

    @Override
    public Object getParent(Object element) {
        return ((TracePackageElement) element).getParent();
    }

    @Override
    public boolean hasChildren(Object element) {
        TracePackageElement traceTransferElement = (TracePackageElement) element;
        TracePackageElement[] visibleChildren = traceTransferElement.getVisibleChildren();
        return visibleChildren != null && visibleChildren.length > 0;
    }

}