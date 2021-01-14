/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Francois Chouinard - Initial API and implementation
 * Francois Chouinard - Updated as per TMF Trace Model 1.0
 * Patrick Tasse - Updated for ranks in experiment location
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.trace.experiment;

import java.nio.ByteBuffer;
import java.util.Objects;

import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;


/**
 * The experiment location in TMF.
 * <p>
 * An experiment location is actually the set of locations of the traces it
 * contains. By setting the individual traces to their corresponding locations,
 * the experiment can be positioned to read the next chronological event.
 * <p>
 * It is the responsibility of the user the individual trace locations are valid
 * and that they are matched to the correct trace.
 *
 * @version 1.0
 * @author Francois Chouinard
 *
 * @see TmfLocationArray
 */
public final class TmfExperimentLocation implements ITmfLocation {

    private final TmfLocationArray fLocation;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * The standard constructor
     *
     * @param locations the set of trace locations
     */
    public TmfExperimentLocation(TmfLocationArray locations) {
        fLocation = locations;
    }

    /**
     * The copy constructor
     *
     * @param location the other experiment location
     */
    public TmfExperimentLocation(TmfExperimentLocation location) {
        this(location.getLocationInfo());
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        StringBuilder result = new StringBuilder("TmfExperimentLocation [");
        result.append(fLocation.toString());
        result.append("]");
        return result.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fLocation != null) ? fLocation.hashCode() : 0);
        return result;
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
        final TmfExperimentLocation other = (TmfExperimentLocation) obj;
        return (Objects.equals(fLocation, other.fLocation));
    }

    @Override
    public TmfLocationArray getLocationInfo() {
        return fLocation;
    }

    @Override
    public void serialize(ByteBuffer bufferOut) {
        ITmfLocation[] locations = fLocation.getLocations();
        long[] ranks = fLocation.getRanks();
        for (int i = 0; i < locations.length; ++i) {
            locations[i].serialize(bufferOut);
            bufferOut.putLong(ranks[i]);
        }
    }
}
