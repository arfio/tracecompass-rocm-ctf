/**********************************************************************
 * Copyright (c) 2011, 2014 Ericsson
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
package org.eclipse.tracecompass.tmf.core.uml2sd;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;

/**
 * <p>
 * A basic implementation of ITmfSyncSequenceDiagramEvent.
 * </p>
 *
 * @version 1.0
 * @author Bernd Hufmann
 */
public class TmfSyncSequenceDiagramEvent implements ITmfSyncSequenceDiagramEvent {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The start time of the sequence diagram event (i.e. time when signal was sent).
     */
    private final ITmfTimestamp fStartTime;
    /**
     * The name of the sender of the signal.
     */
    private final String fSender;
    /**
     * The name of the receiver of the signal.
     */
    private final String fReceiver;
    /**
     * The name of the signal
     */
    private final String fName;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param startEvent The start event (on sender side).
     * @param sender The name of sender of signal.
     * @param receiver The Name of receiver of signal.
     * @param name - The signal name
     */
    public TmfSyncSequenceDiagramEvent(ITmfEvent startEvent, String sender, String receiver, String name) {

        if ((startEvent == null) || (sender == null) || (receiver == null) || (name == null)) {
            throw new IllegalArgumentException("TmfSyncSequenceDiagramEvent constructor: " +  //$NON-NLS-1$
                    (startEvent == null ? ", startEvent=null" : "") + //$NON-NLS-1$ //$NON-NLS-2$
                    (sender == null ? ", sender=null" : "") + //$NON-NLS-1$ //$NON-NLS-2$
                    (receiver == null ? ", receiver=null" : "") + //$NON-NLS-1$ //$NON-NLS-2$
                    (name == null ? ", name=null" : "")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        fStartTime = startEvent.getTimestamp();

        fSender = sender;
        fReceiver = receiver;

        fName = name;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public String getSender() {
        return fSender;
    }

    @Override
    public String getReceiver() {
        return fReceiver;
    }

    @Override
    public String getName() {
        return fName;
    }

    @Override
    public ITmfTimestamp getStartTime() {
        return fStartTime;
    }
}
