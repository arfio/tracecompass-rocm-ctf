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

package org.eclipse.tracecompass.incubator.internal.perf.profiling.core.symbol;

import org.eclipse.tracecompass.tmf.core.symbols.TmfResolvedSymbol;

/**
 * This class represents a resolved symbol that comes from a library. The symbol
 * name will thus contain the library name in parenthesis
 *
 * @author Geneviève Bastien
 */
public class TmfLibrarySymbol extends TmfResolvedSymbol {

    private final String fSourceFile;

    /**
     * Constructor
     *
     * @param address
     *            The address of this symbol
     * @param name
     *            The name this symbol resolves to
     * @param sourceFile
     *            The source file of this symbol
     */
    public TmfLibrarySymbol(long address, String name, String sourceFile) {
        super(address, name);
        fSourceFile = sourceFile;
    }

    @Override
    public String getSymbolName() {
        return super.getSymbolName() + ' ' +'(' + fSourceFile + ')';
    }



}
