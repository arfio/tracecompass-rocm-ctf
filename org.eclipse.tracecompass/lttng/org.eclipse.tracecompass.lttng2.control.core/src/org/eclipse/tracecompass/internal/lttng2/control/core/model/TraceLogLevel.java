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
 *   Jonathan Rajotte - Utility function
 *********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.core.model;


/**
 * Log Level enumeration.
 *
 * @author Bernd Hufmann
 */
@SuppressWarnings("nls")
public enum TraceLogLevel implements ITraceLogLevel {

    // ------------------------------------------------------------------------
    // Enum definition
    // ------------------------------------------------------------------------
    /** Log level 0 */
    TRACE_EMERG("TRACE_EMERG"),
    /** Log level 1 */
    TRACE_ALERT("TRACE_ALERT"),
    /** Log level 2 */
    TRACE_CRIT("TRACE_CRIT"),
    /** Log level 3 */
    TRACE_ERR("TRACE_ERR"),
    /** Log level 4 */
    TRACE_WARNING("TRACE_WARNING"),
    /** Log level 5 */
    TRACE_NOTICE("TRACE_NOTICE"),
    /** Log level 6 */
    TRACE_INFO("TRACE_INFO"),
    /** Log level 7 */
    TRACE_DEBUG_SYSTEM("TRACE_DEBUG_SYSTEM"),
    /** Log level 8 */
    TRACE_DEBUG_PROGRAM("TRACE_DEBUG_PROGRAM"),
    /** Log level 9 */
    TRACE_DEBUG_PROCESS("TRACE_DEBUG_PROCESS"),
    /** Log level 10 */
    TRACE_DEBUG_MODULE("TRACE_DEBUG_MODULE"),
    /** Log level 11 */
    TRACE_DEBUG_UNIT("TRACE_DEBUG_UNIT"),
    /** Log level 12 */
    TRACE_DEBUG_FUNCTION("TRACE_DEBUG_FUNCTION"),
    /** Log level 13 */
    TRACE_DEBUG_LINE("TRACE_DEBUG_LINE"),
    /** Log level 14 */
    TRACE_DEBUG("TRACE_DEBUG"),
    /** Log level 15 */
    LEVEL_UNKNOWN("LEVEL_UNKNOWN");

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * Name of enum.
     */
    private final String fInName;

    // ------------------------------------------------------------------------
    // Constuctors
    // ------------------------------------------------------------------------

    /**
     * Private constructor
     *
     * @param name
     *            the name of state
     */
    private TraceLogLevel(String name) {
        fInName = name;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    @Override
    public String getInName() {
        return fInName;
    }

    // ------------------------------------------------------------------------
    // Utility
    // ------------------------------------------------------------------------
    /**
     * Return the corresponding {@link TraceLogLevel} to String "name"
     *
     * @param name
     *            String to compare to retrieve the good {@link TraceLogLevel}
     * @return the corresponding {@link TraceLogLevel}
     */
    public static TraceLogLevel valueOfString(String name) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        for (TraceLogLevel tllevel : TraceLogLevel.values()) {
            if (tllevel.getInName().equalsIgnoreCase(name)) {
                return tllevel;
            }
        }
        // No match
        return LEVEL_UNKNOWN;
    }
}
