/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mohamad Gebai - Initial API and implementation
 *   Patrick Tasse - Fix experiment name
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.internal.virtual.machine.analysis.core.trace;

import java.util.Collections;
import java.util.Set;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;

/**
 * Experiment class containing traces from physical machine and the virtual
 * guests running on them.
 *
 * @author Mohamad Gebai
 */
public class VirtualMachineExperiment extends TmfExperiment {

    /**
     * Default constructor. Needed by the extension point.
     */
    public VirtualMachineExperiment() {
        this("", Collections.EMPTY_SET); //$NON-NLS-1$
    }

    /**
     * Constructor with traces and id
     *
     * @param id
     *            The ID of this experiment
     * @param traces
     *            The set of traces that are part of this experiment
     */
    public VirtualMachineExperiment(String id, Set<ITmfTrace> traces) {
        super(ITmfEvent.class, id, traces.toArray(new ITmfTrace[traces.size()]), TmfExperiment.DEFAULT_INDEX_PAGE_SIZE, null);
    }

}
