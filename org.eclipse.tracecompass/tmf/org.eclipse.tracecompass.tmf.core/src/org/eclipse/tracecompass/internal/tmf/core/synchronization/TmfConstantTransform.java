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

package org.eclipse.tracecompass.internal.tmf.core.synchronization;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.synchronization.ITmfTimestampTransform;
import org.eclipse.tracecompass.tmf.core.synchronization.TimestampTransformFactory;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;

/**
 * Constant transform, just offset your timestamp with another.
 *
 * @author Matthew Khouzam
 */
public class TmfConstantTransform implements ITmfTimestampTransformInvertible {

    /**
     * Serial ID
     */
    private static final long serialVersionUID = 417299521984404532L;
    private final long fOffset;

    /**
     * Default constructor
     */
    public TmfConstantTransform() {
        // we really should be using an identity transform here.
        fOffset = 0;
    }

    /**
     * Constructor with offset
     *
     * @param offset
     *            The offset of the linear transform in nanoseconds
     */
    public TmfConstantTransform(long offset) {
        fOffset = offset;
    }

    /**
     * Constructor with offset timestamp
     *
     * @param offset
     *            The offset of the linear transform
     */
    public TmfConstantTransform(@NonNull ITmfTimestamp offset) {
        this(offset.toNanos());
    }

    @Override
    public ITmfTimestamp transform(ITmfTimestamp timestamp) {
        return timestamp.normalize(fOffset, ITmfTimestamp.NANOSECOND_SCALE);
    }

    /**
     * {@inheritDoc}
     *
     * @param timestamp
     *            the timestamp in nanoseconds
     * @return the timestamp in nanoseconds
     */
    @Override
    public long transform(long timestamp) {
        return fOffset + timestamp;
    }

    @Override
    public ITmfTimestampTransform composeWith(ITmfTimestampTransform composeWith) {
        if (composeWith.equals(TmfTimestampTransform.IDENTITY)) {
            /* If composing with identity, just return this */
            return this;
        } else if (composeWith instanceof TmfConstantTransform) {
            TmfConstantTransform tct = (TmfConstantTransform) composeWith;
            final long offset = fOffset + tct.fOffset;
            if (offset == 0) {
                return TmfTimestampTransform.IDENTITY;
            }
            return new TmfConstantTransform(offset);
        } else if (composeWith instanceof TmfTimestampTransformLinear) {
            throw new UnsupportedOperationException("Cannot compose a constant and linear transform yet"); //$NON-NLS-1$
        } else {
            /*
             * We do not know what to do with this kind of transform, just
             * return this
             */
            return this;
        }
    }

    @Override
    public ITmfTimestampTransform inverse() {
        return TimestampTransformFactory.createWithOffset(-1 * fOffset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fOffset);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TmfConstantTransform other = (TmfConstantTransform) obj;
        return (fOffset == other.fOffset);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TmfConstantTransform [ offset = "); //$NON-NLS-1$
        builder.append(fOffset);
        builder.append(" ]"); //$NON-NLS-1$
        return builder.toString();
    }

}
