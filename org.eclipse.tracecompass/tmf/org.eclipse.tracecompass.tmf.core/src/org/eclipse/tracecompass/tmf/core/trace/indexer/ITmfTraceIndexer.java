/*******************************************************************************
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
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.trace.indexer;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * The generic trace indexer in TMF with support for incremental indexing.
 *
 * @see ITmfTrace
 * @see ITmfEvent
 *
 * @author Francois Chouinard
 */
public interface ITmfTraceIndexer {

    /**
     * Start an asynchronous index building job and waits for the job completion
     * if required. Typically, the indexing job sends notifications at regular
     * intervals to indicate its progress.
     * <p>
     * <b>Example 1</b>: Index a whole trace asynchronously
     *
     * <pre>
     * trace.getIndexer().buildIndex(0, TmfTimeRange.ETERNITY, false);
     * </pre>
     *
     * <b>Example 2</b>: Index a whole trace synchronously
     *
     * <pre>
     * trace.getIndexer().buildIndex(0, TmfTimeRange.ETERNITY, true);
     * </pre>
     *
     * <b>Example 3</b>: Index a trace asynchronously, starting at rank 100
     *
     * <pre>
     * trace.getIndexer().buildIndex(100, TmfTimeRange.ETERNITY, false);
     * </pre>
     *
     * <b>Example 4</b>: Index a trace asynchronously, starting at rank 100 for
     * events between T1 and T2 (inclusive). This is used for incremental
     * indexing.
     *
     * <pre>
     * TmfTimeRange range = new TmfTimeRange(T1, T2);
     * trace.getIndexer().buildIndex(100, range, false);
     * </pre>
     *
     * @param offset
     *            The offset of the first event to consider
     * @param range
     *            The time range to consider
     * @param waitForCompletion
     *            Should we block the calling thread until the build is
     *            complete?
     */
    void buildIndex(long offset, TmfTimeRange range, boolean waitForCompletion);

    /**
     * Indicates that the indexer is busy indexing the trace.
     * Will always return false if the indexing is done synchronously.
     *
     * @return the state of the indexer (indexing or not)
     */
    boolean isIndexing();

    /**
     * Adds an entry to the trace index.
     *
     * @param context The trace context to save
     * @param timestamp The timestamp matching this context
     */
    void updateIndex(ITmfContext context, ITmfTimestamp timestamp);

    /**
     * Returns the context of the checkpoint immediately preceding the requested
     * timestamp (or at the timestamp if it coincides with a checkpoint).
     *
     * @param timestamp the requested timestamp
     * @return the checkpoint context
     */
    ITmfContext seekIndex(ITmfTimestamp timestamp);

    /**
     * Returns the context of the checkpoint immediately preceding the requested
     * rank (or at rank if it coincides with a checkpoint).
     *
     * @param rank the requested event rank
     * @return the checkpoint context
     */
    ITmfContext seekIndex(long rank);

    /**
     * Perform cleanup when the indexer is no longer required.
     */
    void dispose();

}
