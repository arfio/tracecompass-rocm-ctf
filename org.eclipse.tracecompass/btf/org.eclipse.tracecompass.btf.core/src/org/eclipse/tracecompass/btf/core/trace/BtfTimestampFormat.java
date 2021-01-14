/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.btf.core.trace;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;

/**
 * Timstamp format of BTF timescale as per v2.1 of the spec
 *
 * @author Matthew Khouzam
 * @since 1.0
 */
public enum BtfTimestampFormat {
    /**
     * Picoseconds
     */
    PS("ps", -12, 0.001), //$NON-NLS-1$
    /**
     * Nanoseconds
     */
    NS("ns", -9, 1.0), //$NON-NLS-1$
    /**
     * Microseconds
     */
    US("us", -6, 1000.0), //$NON-NLS-1$
    /**
     * Milliseconds
     */
    MS("ms", -3, 1000000.0), //$NON-NLS-1$
    /**
     * Seconds
     */
    S("s", 0, 1000000000.0); //$NON-NLS-1$

    private final String fName;
    private final int fScale;
    private final double fScaleFactor;

    private BtfTimestampFormat(String name, int scale, double scaleFactor) {
        fName = name;
        fScale = scale;
        fScaleFactor = scaleFactor;
    }

    /**
     * Get the scaling factor
     *
     * @return the scaling factor
     */
    public double getScaleFactor() {
        return fScaleFactor;
    }

    @Override
    public String toString() {
        return fName;
    }

    /**
     * Parse a string to get a scale
     *
     * @param text
     *            the timestamp in text "ns", "ms" ...
     * @return a BtfTimestampFormat object
     */
    public static BtfTimestampFormat parse(String text) {
        switch (text.toLowerCase()) {
        case "ps": //$NON-NLS-1$
            throw new IllegalArgumentException("ps not yet supported"); //$NON-NLS-1$
        case "ns": //$NON-NLS-1$
            return BtfTimestampFormat.NS;
        case "us": //$NON-NLS-1$
            return BtfTimestampFormat.US;
        case "ms": //$NON-NLS-1$
            return BtfTimestampFormat.MS;
        case "s": //$NON-NLS-1$
            return BtfTimestampFormat.S;
        default:
            throw new IllegalArgumentException(text + " not a valid argument, use ps, ns, us, ms, s"); //$NON-NLS-1$
        }
    }

    /**
     * Create an ITmfTimestamp with a proper scale
     *
     * @param timestamp
     *            timestamp without scale
     * @return TmfTimestamp with proper scale
     */
    public @NonNull ITmfTimestamp createTimestamp(long timestamp) {
        return TmfTimestamp.create(timestamp, fScale);
    }
}
