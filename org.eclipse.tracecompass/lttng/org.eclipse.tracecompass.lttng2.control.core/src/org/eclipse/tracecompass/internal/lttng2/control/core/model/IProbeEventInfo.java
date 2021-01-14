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
package org.eclipse.tracecompass.internal.lttng2.control.core.model;

/**
 * <p>
 * Interface for retrieval of probe event information.
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface IProbeEventInfo extends IEventInfo {

    /**
     * @return the address of the probe. (null if not used)
     */
    String getAddress();

    /**
     * Sets the address of the probe.
     * @param address - a address (null if not used)
     */
    void setAddress(String address);

    /**
     * @return the offset applied to the symbol (null if not used).
     */
    String getOffset();

    /**
     * Sets the offset applied to the symbol.
     * @param offset - a offset ((null if not used)
     */
    void setOffset(String offset);

    /**
     * @return the symbol name. ((null if not used))
     */
    String getSymbol();

    /**
     * Sets the symbol name.
     * @param symbol - a symbol name ((null if not used))
     */
    void setSymbol(String symbol);

    /**
     * @return the probe string whether it be an address or a symbol and an
     *         offset
     */
    String getProbeString();
}
