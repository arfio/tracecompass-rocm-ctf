/*******************************************************************************
 * Copyright (c) 2013, 2018 Ericsson
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

/**
 * Constants used in the trace package (XML attribute and element names, etc).
 *
 * @author Marc-Andre Laperle
 */
public interface ITracePackageConstants {

    /**
     * The file name for the package manifest file
     */
    public static final String MANIFEST_FILENAME = "export-manifest.xml"; //$NON-NLS-1$

    /**
     * The root element of an export
     */
    public static final String TMF_EXPORT_ELEMENT = "tmf-export"; //$NON-NLS-1$

    /**
     * Element representing a single trace
     */
    public static final String TRACE_ELEMENT = "trace"; //$NON-NLS-1$

    /**
     * Attribute for the name of a trace
     */
    public static final String TRACE_NAME_ATTRIB = "name"; //$NON-NLS-1$

    /**
     * Attribute for the type of a trace
     */
    public static final String TRACE_TYPE_ATTRIB = "type"; //$NON-NLS-1$

    /**
     * Element representing a single supplementary file
     */
    public static final String SUPPLEMENTARY_FILE_ELEMENT = "supplementary-file"; //$NON-NLS-1$

    /**
     * Attribute for the name of a supplementary file
     */
    public static final String SUPPLEMENTARY_FILE_NAME_ATTRIB = "name"; //$NON-NLS-1$

    /**
     * Element representing a trace file or folder
     */
    public static final String TRACE_FILE_ELEMENT = "file"; //$NON-NLS-1$

    /**
     * Attribute for the name of the file
     */
    public static final String TRACE_FILE_NAME_ATTRIB = "name"; //$NON-NLS-1$

    /**
     * Element representing the bookmarks of a trace
     */
    public static final String BOOKMARKS_ELEMENT = "bookmarks"; //$NON-NLS-1$

    /**
     * Element representing a single bookmark of a trace
     */
    public static final String BOOKMARK_ELEMENT = "bookmark"; //$NON-NLS-1$

    /**
     * Element representing a single experiment
     */
    public static final String EXPERIMENT_ELEMENT = "experiment"; //$NON-NLS-1$

    /**
     * Element representing an experiment trace
     */
    public static final String EXP_TRACE_ELEMENT = "exp-trace"; //$NON-NLS-1$
}
