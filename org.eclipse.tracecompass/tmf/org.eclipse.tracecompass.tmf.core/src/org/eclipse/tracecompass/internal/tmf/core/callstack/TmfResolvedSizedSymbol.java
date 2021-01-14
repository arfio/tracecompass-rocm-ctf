/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.callstack;

import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.tmf.core.symbols.TmfResolvedSymbol;

/**
 * A resolved symbol that has a size
 *
 * @author Geneviève Bastien
 */
public class TmfResolvedSizedSymbol extends TmfResolvedSymbol implements ISegment {

    /**
     * The generated serial UID
     */
    private static final long serialVersionUID = -726052365583654243L;
    private final long fSize;

    /**
     * Constructor
     *
     * @param address
     *            The address of this symbol
     * @param name
     *            The name this symbol resolves to
     * @param size
     *            The size of the symbol space
     */
    public TmfResolvedSizedSymbol(long address, String name, long size) {
        super(address, name);
        fSize = size;
    }

    @Override
    public long getStart() {
        return getBaseAddress();
    }

    @Override
    public long getEnd() {
        return getBaseAddress() + fSize;
    }

    @Override
    public long getLength() {
        return fSize;
    }

}
