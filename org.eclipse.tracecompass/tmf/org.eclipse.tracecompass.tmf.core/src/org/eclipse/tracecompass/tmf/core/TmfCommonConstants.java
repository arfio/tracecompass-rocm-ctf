/*******************************************************************************
 * Copyright (c) 2011, 2017 Ericsson
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
 *   Patrick Tasse - Add support for source location
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.core;

import org.eclipse.core.runtime.QualifiedName;

/**
 *  This class provides a common container for TMF constants.
 *
 *  @author Bernd Hufmann
 */
public class TmfCommonConstants {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The trace type ID persistent property of a trace resource.
     */
    public static final QualifiedName TRACETYPE = new QualifiedName("org.eclipse.linuxtools.tmf", "tracetype.id"); //$NON-NLS-1$//$NON-NLS-2$

    /**
     * The source location persistent property of a trace resource.
     */
    public static final QualifiedName SOURCE_LOCATION = new QualifiedName("org.eclipse.linuxtools.tmf", "source.location"); //$NON-NLS-1$//$NON-NLS-2$

    /**
     * The supplementary folder name persistent property of a trace resource.
     */
    public static final QualifiedName TRACE_SUPPLEMENTARY_FOLDER = new QualifiedName("org.eclipse.linuxtools.tmf", "trace.suppl.folder"); //$NON-NLS-1$//$NON-NLS-2$

    /**
     * The name of the parent folder for storing trace specific supplementary data.
     * Each trace will have a sub-directory underneath with folder name equal to the
     * trace name.
     */
    public static final String TRACE_SUPPLEMENTARY_FOLDER_NAME = ".tracing"; //$NON-NLS-1$

    /**
     * The name of the properties sub-folder within a trace's supplementary folder.
     * Files stored in this folder do not get deleted when the trace is modified.
     *
     * @since 3.1
     */
    public static final String TRACE_PROPERTIES_FOLDER = ".properties"; //$NON-NLS-1$

    /**
     * The name of the default project that can be created under various conditions
     * when there is no tracing project in the workspace.
     */
    public static final String DEFAULT_TRACE_PROJECT_NAME = Messages.DefaultTraceProjectName;

}
