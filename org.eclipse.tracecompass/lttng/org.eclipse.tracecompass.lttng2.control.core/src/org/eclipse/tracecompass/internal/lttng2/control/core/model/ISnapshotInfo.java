/**********************************************************************
 * Copyright (c) 2013, 2014 Ericsson
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
 * Interface for retrieval of snapshot information of a session.
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface ISnapshotInfo extends ITraceInfo {

    /**
     * @return path string where snapshot is located.
     */
    String getSnapshotPath();

    /**
     * Sets the path string (where snapshot is located) to the given value.
     * @param path - session path to set.
     */
    void setSnapshotPath(String path);

    /**
     * @return the snapshot ID.
     */
    int getId();

    /**
     * Sets the snapshot ID.
     * @param id - the ID to set.
     */
    void setId(int id);

    /**
     * Sets whether snapshot is streamed over the network or stored locally
     * at the tracers host.
     *
     * @param isStreamed - <code>true</code> if streamed else <code>false</code>
     */
    void setStreamedSnapshot(boolean isStreamed);

    /**
     * Gets whether snapshot is streamed over the network or stored locally
     * at the tracers host.
     *
     * @return <code>true</code> if streamed else <code>false</code>
     */
    boolean isStreamedSnapshot();

}
