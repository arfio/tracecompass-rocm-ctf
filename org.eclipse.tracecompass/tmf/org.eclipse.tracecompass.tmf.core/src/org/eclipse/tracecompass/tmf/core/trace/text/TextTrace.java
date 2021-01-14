/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Marc-Andre Laperle - Add persistent index support
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.trace.text;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.core.trace.TraceValidationStatus;
import org.eclipse.tracecompass.tmf.core.trace.indexer.ITmfPersistentlyIndexable;
import org.eclipse.tracecompass.tmf.core.trace.indexer.ITmfTraceIndexer;
import org.eclipse.tracecompass.tmf.core.trace.indexer.TmfBTreeTraceIndexer;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.TmfCheckpoint;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;
import org.eclipse.tracecompass.tmf.core.trace.location.TmfLongLocation;

/**
 * Extension of TmfTrace for handling of line-based text traces parsed using
 * regular expressions. Each line that matches the first line pattern indicates
 * the start of a new event. The subsequent lines can contain additional
 * information that is added to the current event.
 *
 * @param <T>
 *            TmfEvent class returned by this trace
 */
public abstract class TextTrace<T extends TextTraceEvent> extends TmfTrace implements ITmfPersistentlyIndexable {

    private static final TmfLongLocation NULL_LOCATION = new TmfLongLocation(-1L);
    private static final int MAX_LINES = 100;
    private static final int MAX_CONFIDENCE = 100;

    /** The default separator used for multi-line fields */
    protected static final String SEPARATOR = " | "; //$NON-NLS-1$

    /** The text file */
    protected BufferedRandomAccessFile fFile;

    /**
     * Constructor
     */
    public TextTrace() {
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation computes the confidence as the sum of weighted
     * values of the first 100 lines of the file which match any of the provided
     * validation patterns. For each matching line a weighted value between 1.5
     * and 2.0 is assigned based on the group count of the matching patterns.
     * The higher the group count, the closer the weighted value will be to 2.0.
     */
    @Override
    public IStatus validate(IProject project, String path) {
        File file = new File(path);
        if (!file.exists()) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "File not found: " + path); //$NON-NLS-1$
        }
        if (!file.isFile()) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Not a file. It's a directory: " + path); //$NON-NLS-1$
        }
        try {
            if (!TmfTraceUtils.isText(file)) {
                return new TraceValidationStatus(0, Activator.PLUGIN_ID);
            }
        } catch (IOException e) {
            Activator.logError("Error validating file: " + path, e); //$NON-NLS-1$
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "IOException validating file: " + path, e); //$NON-NLS-1$
        }
        int confidence = 0;
        try (BufferedRandomAccessFile rafile = new BufferedRandomAccessFile(path, "r")) { //$NON-NLS-1$
            int lineCount = 0;
            double matches = 0.0;
            String line = rafile.getNextLine();
            List<Pattern> validationPatterns = getValidationPatterns();
            while ((line != null) && (lineCount++ < MAX_LINES)) {
                line = preProcessLine(line);
                for(Pattern pattern : validationPatterns) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.matches()) {
                        int groupCount = matcher.groupCount();
                        matches += (1.0 + groupCount / ((double) groupCount + 1));
                    }
                }
                confidence = (int) (MAX_CONFIDENCE * matches / lineCount);
                line = rafile.getNextLine();
            }
        } catch (IOException e) {
            Activator.logError("Error validating file: " + path, e); //$NON-NLS-1$
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "IOException validating file: " + path, e); //$NON-NLS-1$
        }

        return new TraceValidationStatus(confidence, Activator.PLUGIN_ID);

    }
    @Override
    public void initTrace(IResource resource, String path, Class<? extends ITmfEvent> type) throws TmfTraceException {
        super.initTrace(resource, path, type);
        initFile();
    }

    private void initFile() throws TmfTraceException {
        closeFile();
        try {
            fFile = new BufferedRandomAccessFile(getPath(), "r"); //$NON-NLS-1$
        } catch (IOException e) {
            throw new TmfTraceException(e.getMessage(), e);
        }
    }

    @Override
    public synchronized void dispose() {
        super.dispose();
        closeFile();
    }

    private void closeFile() {
        if (fFile != null) {
            try {
                fFile.close();
            } catch (IOException e) {
            } finally {
                fFile = null;
            }
        }
    }

    @Override
    public synchronized TextTraceContext seekEvent(ITmfLocation location) {
        TextTraceContext context = new TextTraceContext(NULL_LOCATION, ITmfContext.UNKNOWN_RANK);
        if (NULL_LOCATION.equals(location) || fFile == null) {
            return context;
        }
        try {
            if (location == null) {
                fFile.seek(0);
            } else if (location.getLocationInfo() instanceof Long) {
                fFile.seek((Long) location.getLocationInfo());
            }
            long rawPos = fFile.getFilePointer();
            String line = fFile.getNextLine();
            while (line != null) {
                line = preProcessLine(line);
                Matcher matcher = getFirstLinePattern().matcher(line);
                if (matcher.matches()) {
                    setupContext(context, rawPos, line, matcher);
                    return context;
                }
                rawPos = fFile.getFilePointer();
                line = fFile.getNextLine();
            }
            return context;
        } catch (IOException e) {
            Activator.logError("Error seeking file: " + getPath(), e); //$NON-NLS-1$
            return context;
        }
    }

    private void setupContext(TextTraceContext context, long rawPos, String line, Matcher matcher) throws IOException {
        context.setLocation(new TmfLongLocation(rawPos));
        context.firstLineMatcher = matcher;
        context.firstLine = line;
        context.nextLineLocation = fFile.getFilePointer();
    }

    @Override
    public synchronized TextTraceContext seekEvent(double ratio) {
        if (fFile == null) {
            return new TextTraceContext(NULL_LOCATION, ITmfContext.UNKNOWN_RANK);
        }
        try {
            long pos = Math.round(ratio * fFile.length());
            while (pos > 0) {
                fFile.seek(pos - 1);
                if (fFile.read() == '\n') {
                    break;
                }
                pos--;
            }
            ITmfLocation location = new TmfLongLocation(Long.valueOf(pos));
            TextTraceContext context = seekEvent(location);
            context.setRank(ITmfContext.UNKNOWN_RANK);
            return context;
        } catch (IOException e) {
            Activator.logError("Error seeking file: " + getPath(), e); //$NON-NLS-1$
            return new TextTraceContext(NULL_LOCATION, ITmfContext.UNKNOWN_RANK);
        }
    }

    @Override
    public double getLocationRatio(ITmfLocation location) {
        if (fFile == null) {
            return 0;
        }
        try {
            long length = fFile.length();
            if (length == 0) {
                return 0;
            }
            if (location.getLocationInfo() instanceof Long) {
                return (double) ((Long) location.getLocationInfo()) / length;
            }
        } catch (IOException e) {
            Activator.logError("Error reading file: " + getPath(), e); //$NON-NLS-1$
        }
        return 0;
    }

    @Override
    public ITmfLocation getCurrentLocation() {
        return null;
    }

    @Override
    public TextTraceEvent parseEvent(ITmfContext tmfContext) {
        TextTraceContext context = seekEvent(tmfContext.getLocation());
        return parse(context);
    }

    @Override
    public synchronized @Nullable T getNext(ITmfContext context) {
        if (!(context instanceof TextTraceContext)) {
            throw new IllegalArgumentException();
        }
        TextTraceContext savedContext = new TextTraceContext(context.getLocation(), context.getRank());
        @Nullable T event = parse((TextTraceContext) context);
        if (event != null) {
            updateAttributes(savedContext, event);
            context.increaseRank();
        }
        return event;
    }

    /**
     * Parse the next event. The context is advanced.
     *
     * @param tmfContext
     *            the context
     * @return the next event or null
     */
    protected synchronized @Nullable T parse(TextTraceContext tmfContext) {
        if (fFile == null) {
            return null;
        }
        TextTraceContext context = tmfContext;
        ITmfLocation location = context.getLocation();
        if (location == null || !(location.getLocationInfo() instanceof Long) || NULL_LOCATION.equals(location)) {
            return null;
        }

        T event = parseFirstLine(context.firstLineMatcher, context.firstLine);

        try {
            if (fFile.getFilePointer() != context.nextLineLocation) {
                fFile.seek(context.nextLineLocation);
            }
            long rawPos = fFile.getFilePointer();
            String line = fFile.getNextLine();
            while (line != null) {
                line = preProcessLine(line);
                Matcher matcher = getFirstLinePattern().matcher(line);
                if (matcher.matches()) {
                    setupContext(context, rawPos, line, matcher);
                    return event;
                }
                parseNextLine(event, line);
                rawPos = fFile.getFilePointer();
                line = fFile.getNextLine();
            }
        } catch (IOException e) {
            Activator.logError("Error reading file: " + getPath(), e); //$NON-NLS-1$
        }

        context.setLocation(NULL_LOCATION);
        return event;
    }

    /**
     * Pre-processes the input line. The default implementation returns the
     * input line.
     *
     * @param line
     *            non-null input string
     * @return the pre-processed input line
     */
    @NonNull
    protected String preProcessLine(@NonNull String line) {
        return line;
    }

    /**
     * Gets the first line pattern.
     *
     * @return The first line pattern
     */
    protected abstract Pattern getFirstLinePattern();

    /**
     * Parses the first line data and returns a new event. When constructing the
     * event, the concrete trace should use the trace's timestamp transform to
     * create the timestamp, by either transforming the parsed time value
     * directly or by using the method {@link #createTimestamp(long)}.
     *
     * @param matcher
     *            The matcher
     * @param line
     *            The line to parse
     * @return The parsed event
     */
    protected abstract T parseFirstLine(Matcher matcher, String line);

    /**
     * Parses the next line data for the current event.
     *
     * @param event
     *            The current event being parsed
     * @param line
     *            The line to parse
     */
    protected abstract void parseNextLine(T event, String line);

    /**
     * Returns a ordered list of validation patterns that will be used in
     * the default {@link #validate(IProject, String)} method to match
     * the first 100 to compute the confidence level
     *
     * @return collection of patterns to validate against
     */
    protected List<Pattern> getValidationPatterns() {
        return Collections.singletonList(getFirstLinePattern());
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    /**
     * Strip quotes surrounding a string
     *
     * @param input
     *            The input string
     * @return The string without quotes
     */
    protected static String replaceQuotes(String input) {
        String out = input.replaceAll("^\"|(\"\\s*)$", "");  //$NON-NLS-1$//$NON-NLS-2$
        return out;
    }

    /**
     * Strip brackets surrounding a string
     *
     * @param input
     *            The input string
     * @return The string without brackets
     */
    protected static String replaceBrackets(String input) {
        String out = input.replaceAll("^\\{|(\\}\\s*)$", "");  //$NON-NLS-1$//$NON-NLS-2$
        return out;
    }

    private static int fCheckpointSize = -1;

    @Override
    public synchronized int getCheckpointSize() {
        if (fCheckpointSize == -1) {
            TmfCheckpoint c = new TmfCheckpoint(TmfTimestamp.ZERO, new TmfLongLocation(0L), 0);
            ByteBuffer b = ByteBuffer.allocate(ITmfCheckpoint.MAX_SERIALIZE_SIZE);
            b.clear();
            c.serialize(b);
            fCheckpointSize = b.position();
        }

        return fCheckpointSize;
    }

    @Override
    protected ITmfTraceIndexer createIndexer(int interval) {
        return new TmfBTreeTraceIndexer(this, interval);
    }

    @Override
    public ITmfLocation restoreLocation(ByteBuffer bufferIn) {
        return new TmfLongLocation(bufferIn);
    }

    @TmfSignalHandler
    @Override
    public void traceRangeUpdated(TmfTraceRangeUpdatedSignal signal) {
        if (signal.getTrace() == this) {
            try {
                synchronized (this) {
                    // Reset the file handle in case it has reached the end of the
                    // file already. Otherwise, it will not be able to read new data
                    // pass the previous end.
                    initFile();
                }
            } catch (TmfTraceException e) {
                Activator.logError(e.getLocalizedMessage(), e);
            }
        }
        super.traceRangeUpdated(signal);
    }

    /**
     * @since 3.0
     */
    @Override
    public synchronized ITmfTimestamp readEnd() {
        try {
            Long pos = fFile.length() - 1;
            /* Outer loop to find the first line of a matcher group. */
            while (pos > 0) {
                /* Inner loop to find line beginning */
                while (pos > 0) {
                    fFile.seek(pos - 1);
                    if (fFile.read() == '\n') {
                        break;
                    }
                    pos--;
                }
                ITmfLocation location = new TmfLongLocation(pos);
                ITmfContext context = seekEvent(location);
                ITmfEvent event = getNext(context);
                context.dispose();
                if (event != null) {
                    return event.getTimestamp();
                }
                pos--;
            }
        } catch (IOException e) {
            return null;
        }

        /* Empty trace */
        return null;
    }
}
