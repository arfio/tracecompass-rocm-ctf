/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.AbstractSegmentStoreAnalysisModule;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.IAnalysisProgressListener;
import org.eclipse.tracecompass.datastore.core.interval.IHTIntervalReader;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.segment.TmfXmlPatternSegment;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentStoreFactory.SegmentStoreType;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Segment store module for pattern analysis defined in XML. This module will
 * receive all the segments provided by an external source and will build a
 * segment store
 *
 * @author Jean-Christian Kouame
 */
public class XmlPatternSegmentStoreModule extends AbstractSegmentStoreAnalysisModule implements ISegmentListener {

    /**
     * Fake segment indicated that the last segment have been received
     */
    public static final @NonNull EndSegment END_SEGMENT = new EndSegment();
    private final List<@NonNull ISegment> fCachedSegments = new ArrayList<>();
    private ISegmentStore<@NonNull ISegment> fFinalSegments;
    private final CountDownLatch fFinished = new CountDownLatch(1);
    private final @NonNull XmlPatternAnalysis fParent;
    private boolean fSegmentStoreCompleted;

    /**
     * Constructor
     *
     * @param parent
     *            The parent analysis
     */
    public XmlPatternSegmentStoreModule(@NonNull XmlPatternAnalysis parent) {
        super();
        fParent = parent;
    }

    @Override
    protected boolean buildAnalysisSegments(@NonNull ISegmentStore<@NonNull ISegment> segments, @NonNull IProgressMonitor monitor) throws TmfAnalysisException {
        final @Nullable ITmfTrace trace = getTrace();
        if (trace == null) {
            /* This analysis was cancelled in the meantime */
            segmentStoreReady(false);
            segments.close(true);
            return false;
        }
        fFinalSegments = segments;
        if (waitForSegmentStoreCompletion()) {
            fFinalSegments.close(false);
            return true;
        }
        fFinalSegments.close(true);
        return false;
    }

    @Override
    protected void canceling() {
        super.cancel();
        segmentStoreReady(false);
    }

    @Override
    protected String getDataFileName() {
        return getId() + XmlPatternAnalysis.SEGMENT_STORE_EXTENSION;
    }

    @Override
    protected @NonNull SegmentStoreType getSegmentStoreType() {
        return SegmentStoreType.OnDisk;
    }

    @Override
    protected @NonNull IHTIntervalReader<@NonNull ISegment> getSegmentReader() {
        return TmfXmlPatternSegment.READER;
    }

    /**
     * Broadcast the segment store to its listeners. Since this analysis is not
     * directly register to the trace, the parent analysis is used as the source.
     *
     * @param store
     *            The store to broadcast
     */
    @Override
    protected void sendUpdate(final ISegmentStore<@NonNull ISegment> store) {
        for (IAnalysisProgressListener listener : getListeners()) {
            listener.onComplete(fParent, store);
        }
    }

    @Override
    public void onNewSegment(@NonNull ISegment segment) {
        // We can accept segments until the first END_SEGMENT arrives. Nothing
        // should be accept after it. This prevents to receive new segments if
        // the analysis that generates the segments is rescheduled
        if (!fSegmentStoreCompleted) {
            if (segment == END_SEGMENT) {
                segmentStoreReady(true);
                return;
            }
            if (fFinalSegments == null) {
                getCachedSegments().add(segment);
            } else {
                if (!getCachedSegments().isEmpty()) {
                    fFinalSegments.addAll(getCachedSegments());
                    getCachedSegments().clear();
                }
                fFinalSegments.add(segment);
            }
        }
    }

    /**
     * Get the internal segment store of this module
     *
     * @return The segment store
     */
    private synchronized List<@NonNull ISegment> getCachedSegments() {
        return fCachedSegments;
    }

    /**
     * Wait until internal segment store of the module is fully filled. If all
     * the segments have been received, the completion succeeded, otherwise it
     * is not.
     *
     * @return True if the completion succeeded, false otherwise
     */
    public boolean waitForSegmentStoreCompletion() {
        try {
            fFinished.await();
        } catch (InterruptedException e) {
            return false;
        }
        return fSegmentStoreCompleted;
    }

    /**
     * Make the module available and set whether the segment store completion
     * succeeded or not. If not, no segment store is available and
     * {@link #waitForSegmentStoreCompletion()} should return false.
     *
     * @param success
     *            True if the segment store completion succeeded, false
     *            otherwise
     */
    private void segmentStoreReady(boolean succeeded) {
        fSegmentStoreCompleted = succeeded;
        fFinished.countDown();
    }

    /**
     * Fake segment indicating the build is over, and the segment store is fully
     * filled
     */
    public static class EndSegment implements ISegment {
        /**
         * The serial version UID
         */
        private static final long serialVersionUID = 7834984029618274707L;

        @Override
        public long getStart() {
            return Long.MIN_VALUE;
        }

        @Override
        public long getEnd() {
            return Long.MIN_VALUE;
        }
    }
}
