/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.views;

/**
 * Interface for a view to support zoom to selection.
 *
 * @author Bernd Hufmann
 *
 */
public interface ITmfZoomToSelectionProvider {

    /**
     * Zoom to selection
     */
    void zoomToSelection();
}
