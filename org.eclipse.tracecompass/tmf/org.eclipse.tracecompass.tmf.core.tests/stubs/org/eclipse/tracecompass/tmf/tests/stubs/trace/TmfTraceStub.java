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
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Updated for removal of context clone
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.tests.stubs.trace;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.internal.tmf.core.request.TmfCoalescedEventRequest;
import org.eclipse.tracecompass.tmf.core.component.TmfEventProvider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfEventParser;
import org.eclipse.tracecompass.tmf.core.trace.TmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.indexer.ITmfPersistentlyIndexable;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.TmfCheckpoint;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;
import org.eclipse.tracecompass.tmf.core.trace.location.TmfLongLocation;

/**
 * <b><u>TmfTraceStub</u></b>
 * <p>
 * Dummy test trace. Use in conjunction with TmfEventParserStub.
 */
public class TmfTraceStub extends TmfTrace implements ITmfPersistentlyIndexable {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The actual stream
    private RandomAccessFile fTrace;

    // The associated event parser
    private final @NonNull ITmfEventParser fParser;

    // The synchronization lock
    private final ReentrantLock fLock = new ReentrantLock();

    private ITmfTimestamp fInitialRangeOffset = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public TmfTraceStub() {
        super();
        fParser = new TmfEventParserStub(this);
    }

    /**
     * Constructor with which you can specify a custom streaming interval. The
     * parser and indexer won't be specified.
     *
     * @param path
     *            The path to the trace file
     * @param cacheSize
     *            The cache size
     * @param interval
     *            The trace streaming interval
     * @throws TmfTraceException
     *             If an error occurred opening the trace
     */
    public TmfTraceStub(final String path,
            final int cacheSize,
            final long interval) throws TmfTraceException {
        super(null, ITmfEvent.class, path, cacheSize, interval);
        setupTrace(path);
        fParser = new TmfEventParserStub(this);
    }

    /**
     * Constructor to specify the parser and indexer. The streaming interval
     * will be 0.
     *
     * @param path
     *            The path to the trace file
     * @param cacheSize
     *            The cache size
     * @param waitForCompletion
     *            Do we block the caller until the trace is indexed, or not.
     * @param parser
     *            The trace parser. If left 'null', it will use a
     *            {@link TmfEventParserStub}.
     * @throws TmfTraceException
     *             If an error occurred opening the trace
     */
    public TmfTraceStub(final String path,
            final int cacheSize,
            final boolean waitForCompletion,
            final ITmfEventParser parser) throws TmfTraceException {
        super(null, ITmfEvent.class, path, cacheSize, 0);
        setupTrace(path);
        fParser = ((parser != null) ? parser : new TmfEventParserStub(this));
        if (waitForCompletion) {
            indexTrace(true);
        }
    }

    /**
     * Constructor to specify the resource, parser and indexer. The streaming
     * interval will be 0.
     *
     * @param resource
     *            The trace resource
     * @param path
     *            The path to the trace file
     * @param cacheSize
     *            The cache size
     * @param waitForCompletion
     *            Do we block the caller until the trace is indexed, or not.
     * @param parser
     *            The trace parser. If left 'null', it will use a
     *            {@link TmfEventParserStub}.
     * @throws TmfTraceException
     *             If an error occurred opening the trace
     */
    public TmfTraceStub(final IResource resource,
            final String path,
            final int cacheSize,
            final boolean waitForCompletion,
            final ITmfEventParser parser) throws TmfTraceException {
        super(resource, ITmfEvent.class, path, cacheSize, 0);
        setupTrace(path);
        fParser = ((parser != null) ? parser : new TmfEventParserStub(this));
        if (waitForCompletion) {
            indexTrace(true);
        }
    }

    /**
     * Copy constructor
     *
     * @param trace
     *            The trace to copy
     * @throws TmfTraceException
     *             If an error occurred opening the trace
     */
    public TmfTraceStub(final TmfTraceStub trace) throws TmfTraceException {
        super(trace);
        setupTrace(getPath()); // fPath will be set by the super-constructor
        fParser = new TmfEventParserStub(this);
    }


    private void setupTrace(String path) throws TmfTraceException {
        try {
            fTrace = new RandomAccessFile(path, "r"); //$NON-NLS-1$
        } catch (FileNotFoundException e) {
            throw new TmfTraceException(e.getMessage());
        }
    }

    // ------------------------------------------------------------------------
    // Initializers
    // ------------------------------------------------------------------------

    @Override
    public void initTrace(final IResource resource, final String path, final Class<? extends ITmfEvent> type) throws TmfTraceException {
        try {
            fTrace = new RandomAccessFile(path, "r"); //$NON-NLS-1$
        } catch (FileNotFoundException e) {
            throw new TmfTraceException(e.getMessage());
        }
        super.initTrace(resource, path, type);
    }

    @Override
    public void initialize(final IResource resource, final String path, final Class<? extends ITmfEvent> type) throws TmfTraceException {
        super.initialize(resource, path, type);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return The file stream to the trace
     */
    public RandomAccessFile getStream() {
        return fTrace;
    }

    /**
     * Set the initial range offset.
     *
     * @param initOffset
     *            The new initial range offset
     */
    public void setInitialRangeOffset(ITmfTimestamp initOffset) {
        fInitialRangeOffset = initOffset;
    }

    @Override
    public ITmfTimestamp getInitialRangeOffset() {
        if (fInitialRangeOffset != null) {
            return fInitialRangeOffset;
        }
        return super.getInitialRangeOffset();
    }

    // ------------------------------------------------------------------------
    // Operators
    // ------------------------------------------------------------------------

    @Override
    public TmfContext seekEvent(final ITmfLocation location) {
        try {
            fLock.lock();
            try {
                if (fTrace != null) {
                    // Position the trace at the requested location and
                    // returns the corresponding context
                    long loc  = 0;
                    long rank = 0;
                    if (location != null) {
                        loc = (Long) location.getLocationInfo();
                        rank = ITmfContext.UNKNOWN_RANK;
                    }
                    if (loc != fTrace.getFilePointer()) {
                        fTrace.seek(loc);
                    }
                    final TmfContext context = new TmfContext(getCurrentLocation(), rank);
                    return context;
                }
            } catch (final IOException e) {
                e.printStackTrace();
            } catch (final NullPointerException e) {
                e.printStackTrace();
            }
            finally{
                fLock.unlock();
            }
        } catch (final NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public TmfContext seekEvent(final double ratio) {
        fLock.lock();
        try {
            if (fTrace != null) {
                final ITmfLocation location = new TmfLongLocation(Long.valueOf(Math.round(ratio * fTrace.length())));
                final TmfContext context = seekEvent(location);
                context.setRank(ITmfContext.UNKNOWN_RANK);
                return context;
            }
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            fLock.unlock();
        }

        return null;
    }

    @Override
    public double getLocationRatio(ITmfLocation location) {
        fLock.lock();
        try {
            if (fTrace != null) {
                if (location.getLocationInfo() instanceof Long) {
                    return ((Long) location.getLocationInfo()).doubleValue() / fTrace.length();
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            fLock.unlock();
        }
        return 0;
    }

    @Override
    public ITmfLocation getCurrentLocation() {
        fLock.lock();
        try {
            if (fTrace != null) {
                return new TmfLongLocation(fTrace.getFilePointer());
            }
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            fLock.unlock();
        }
        return null;
    }

    @Override
    public ITmfEvent parseEvent(final ITmfContext context) {
        fLock.lock();
        try {
            // parseNextEvent will update the context
            if (fTrace != null && context != null) {
                final ITmfEvent event = fParser.parseEvent(context);
                return event;
            }
        } finally {
            fLock.unlock();
        }
        return null;
    }

    @Override
    public ITmfTimestamp createTimestamp(long ts) {
        return TmfTimestamp.fromMillis(getTimestampTransform().transform(ts) / 1000000L);
    }

    @Override
    public synchronized void setNbEvents(final long nbEvents) {
        super.setNbEvents(nbEvents);
    }

    @Override
    public void setTimeRange(final TmfTimeRange range) {
        super.setTimeRange(range);
    }

    @Override
    public void setStartTime(final ITmfTimestamp startTime) {
        super.setStartTime(startTime);
    }

    @Override
    public void setEndTime(final ITmfTimestamp endTime) {
        super.setEndTime(endTime);
    }

    @Override
    public void setStreamingInterval(final long interval) {
        super.setStreamingInterval(interval);
    }

    @Override
    public synchronized void dispose() {
        fLock.lock();
        try {
            if (fTrace != null) {
                fTrace.close();
                fTrace = null;
            }
        } catch (final IOException e) {
            // Ignore
        } finally {
            fLock.unlock();
        }
        super.dispose();
    }

    @Override
    public IStatus validate(IProject project, String path) {
        if (fileExists(path)) {
            return Status.OK_STATUS;
        }
        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "File does not exist: " + path);
    }

    private static int fCheckpointSize = -1;

    @Override
    public synchronized int getCheckpointSize() {
        if (fCheckpointSize == -1) {
            TmfCheckpoint c = new TmfCheckpoint(TmfTimestamp.fromSeconds(0L), new TmfLongLocation(0L), 0);
            ByteBuffer b = ByteBuffer.allocate(ITmfCheckpoint.MAX_SERIALIZE_SIZE);
            b.clear();
            c.serialize(b);
            fCheckpointSize = b.position();
        }

        return fCheckpointSize;
    }

    @Override
    public ITmfLocation restoreLocation(ByteBuffer bufferIn) {
        return new TmfLongLocation(bufferIn);
    }

    /**
     * Simulate trace opening, to be called by tests who need an actively opened
     * trace
     */
    public void openTrace() {
        TmfSignalManager.dispatchSignal(new TmfTraceOpenedSignal(this, this, null));
        selectTrace();
    }

    /**
     * Simulate selecting the trace
     */
    public void selectTrace() {
        TmfSignalManager.dispatchSignal(new TmfTraceSelectedSignal(this, this));
    }

    /**
     * @return a copy of the pending request list
     * @throws Exception if java reflection failed
     */
    public List<TmfCoalescedEventRequest> getAllPendingRequests() throws Exception {
        Method m = TmfEventProvider.class.getDeclaredMethod("getPendingRequests");
        m.setAccessible(true);
        LinkedList<?> list= (LinkedList<?>) m.invoke(this);
        LinkedList<TmfCoalescedEventRequest> retList = new LinkedList<>();
        for (Object element : list) {
            retList.add((TmfCoalescedEventRequest) element);
        }
        return retList;
    }

    /**
     * Clears the pending request list
     * @throws Exception if java reflection failed
     */
    public void clearAllPendingRequests() throws Exception {
        Method m = TmfEventProvider.class.getDeclaredMethod("clearPendingRequests");
        m.setAccessible(true);
        m.invoke(this);
    }

    /**
     * Sets the timer flag
     * @param enabled
     *              flag to set
     * @throws Exception if java reflection failed
     */
    public void setTimerEnabledFlag(boolean enabled) throws Exception {
        Class<?>[] paramTypes = new Class[1];
        paramTypes[0] = Boolean.class;
        Method m = TmfEventProvider.class.getDeclaredMethod("setTimerEnabled", paramTypes);

        Object[] params = new Object[1];
        params[0] = Boolean.valueOf(enabled);
        m.setAccessible(true);
        m.invoke(this, params);
    }

}
