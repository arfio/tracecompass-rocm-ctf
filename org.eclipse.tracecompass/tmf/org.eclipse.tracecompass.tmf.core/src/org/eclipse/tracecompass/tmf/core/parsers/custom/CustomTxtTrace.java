/*******************************************************************************
 * Copyright (c) 2010, 2016 Ericsson
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
 *   Bernd Hufmann - Add trace type id handling
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.parsers.custom;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.internal.tmf.core.parsers.custom.CustomEventAspects;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtTraceDefinition.InputLine;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfContext;
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
 * Base class for custom plain text traces.
 *
 * @author Patrick Tassé
 */
public class CustomTxtTrace extends TmfTrace implements ITmfPersistentlyIndexable {

    private static final TmfLongLocation NULL_LOCATION = new TmfLongLocation(-1L);
    private static final int DEFAULT_CACHE_SIZE = 100;
    private static final int MAX_LINES = 100;
    private static final int MAX_CONFIDENCE = 100;

    private final CustomTxtTraceDefinition fDefinition;
    private final ITmfEventField fRootField;
    private BufferedRandomAccessFile fFile;
    private final @NonNull String fTraceTypeId;

    private static final char SEPARATOR = ':';
    private static final String CUSTOM_TXT_TRACE_TYPE_PREFIX = "custom.txt.trace" + SEPARATOR; //$NON-NLS-1$
    private static final String LINUX_TOOLS_CUSTOM_TXT_TRACE_TYPE_PREFIX = "org.eclipse.linuxtools.tmf.core.parsers.custom.CustomTxtTrace" + SEPARATOR; //$NON-NLS-1$
    private static final String EARLY_TRACE_COMPASS_CUSTOM_TXT_TRACE_TYPE_PREFIX = "org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtTrace" + SEPARATOR; //$NON-NLS-1$

    /**
     * Basic constructor.
     *
     * @param definition
     *            Text trace definition
     */
    public CustomTxtTrace(final CustomTxtTraceDefinition definition) {
        fDefinition = definition;
        fRootField = CustomEventType.getRootField(definition);
        fTraceTypeId = buildTraceTypeId(definition.categoryName, definition.definitionName);
        setCacheSize(DEFAULT_CACHE_SIZE);
    }

    /**
     * Full constructor.
     *
     * @param resource
     *            Trace's resource.
     * @param definition
     *            Text trace definition
     * @param path
     *            Path to the trace file
     * @param cacheSize
     *            Cache size to use
     * @throws TmfTraceException
     *             If we couldn't open the trace at 'path'
     */
    public CustomTxtTrace(final IResource resource,
            final CustomTxtTraceDefinition definition, final String path,
            final int cacheSize) throws TmfTraceException {
        this(definition);
        setCacheSize((cacheSize > 0) ? cacheSize : DEFAULT_CACHE_SIZE);
        initTrace(resource, path, CustomTxtEvent.class);
    }

    @Override
    public void initTrace(final IResource resource, final String path, final Class<? extends ITmfEvent> eventType) throws TmfTraceException {
        super.initTrace(resource, path, eventType);
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
    public ITmfTraceIndexer getIndexer() {
        return super.getIndexer();
    }

    @Override
    public Iterable<ITmfEventAspect<?>> getEventAspects() {
        return CustomEventAspects.generateAspects(fDefinition);
    }

    @Override
    public synchronized TmfContext seekEvent(final ITmfLocation location) {
        final CustomTxtTraceContext context = new CustomTxtTraceContext(NULL_LOCATION, ITmfContext.UNKNOWN_RANK);
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
                for (final InputLine input : getFirstLines()) {
                    final Matcher matcher = input.getPattern().matcher(line);
                    if (matcher.matches()) {
                        context.setLocation(new TmfLongLocation(rawPos));
                        context.firstLineMatcher = matcher;
                        context.firstLine = line;
                        context.nextLineLocation = fFile.getFilePointer();
                        context.inputLine = input;
                        return context;
                    }
                }
                rawPos = fFile.getFilePointer();
                line = fFile.getNextLine();
            }
            return context;
        } catch (final FileNotFoundException e) {
            Activator.logError("Error seeking event. File not found: " + getPath(), e); //$NON-NLS-1$
            return context;
        } catch (final IOException e) {
            Activator.logError("Error seeking event. File: " + getPath(), e); //$NON-NLS-1$
            return context;
        }

    }

    @Override
    public synchronized TmfContext seekEvent(final double ratio) {
        if (fFile == null) {
            return new CustomTxtTraceContext(NULL_LOCATION, ITmfContext.UNKNOWN_RANK);
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
            final ITmfLocation location = new TmfLongLocation(pos);
            final TmfContext context = seekEvent(location);
            context.setRank(ITmfContext.UNKNOWN_RANK);
            return context;
        } catch (final IOException e) {
            Activator.logError("Error seeking event. File: " + getPath(), e); //$NON-NLS-1$
            return new CustomTxtTraceContext(NULL_LOCATION, ITmfContext.UNKNOWN_RANK);
        }
    }

    @Override
    public synchronized double getLocationRatio(final ITmfLocation location) {
        if (fFile == null) {
            return 0;
        }
        try {
            if (location.getLocationInfo() instanceof Long) {
                return ((Long) location.getLocationInfo()).doubleValue() / fFile.length();
            }
        } catch (final IOException e) {
            Activator.logError("Error seeking event. File: " + getPath(), e); //$NON-NLS-1$
        }
        return 0;
    }

    @Override
    public ITmfLocation getCurrentLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public synchronized CustomTxtEvent parseEvent(final ITmfContext tmfContext) {
        ITmfContext context = seekEvent(tmfContext.getLocation());
        return parse(context);
    }

    @Override
    public synchronized CustomTxtEvent getNext(final ITmfContext context) {
        final ITmfContext savedContext = new TmfContext(context.getLocation(), context.getRank());
        final CustomTxtEvent event = parse(context);
        if (event != null) {
            updateAttributes(savedContext, event);
            context.increaseRank();
        }
        return event;
    }

    private synchronized CustomTxtEvent parse(final ITmfContext tmfContext) {
        if (fFile == null) {
            return null;
        }
        if (!(tmfContext instanceof CustomTxtTraceContext)) {
            return null;
        }

        final CustomTxtTraceContext context = (CustomTxtTraceContext) tmfContext;
        ITmfLocation location = context.getLocation();
        if (location == null || !(location.getLocationInfo() instanceof Long) || NULL_LOCATION.equals(location)) {
            return null;
        }

        CustomTxtEvent event = parseFirstLine(context);

        final HashMap<InputLine, Integer> countMap = new HashMap<>();
        InputLine currentInput = null;
        if (context.inputLine.childrenInputs != null && !context.inputLine.childrenInputs.isEmpty()) {
            currentInput = context.inputLine.childrenInputs.get(0);
            countMap.put(currentInput, 0);
        }

        try {
            if (fFile.getFilePointer() != context.nextLineLocation) {
                fFile.seek(context.nextLineLocation);
            }
            long rawPos = fFile.getFilePointer();
            String line = fFile.getNextLine();
            while (line != null) {
                boolean processed = false;
                if (currentInput == null) {
                    for (final InputLine input : getFirstLines()) {
                        final Matcher matcher = input.getPattern().matcher(line);
                        if (matcher.matches()) {
                            context.setLocation(new TmfLongLocation(rawPos));
                            context.firstLineMatcher = matcher;
                            context.firstLine = line;
                            context.nextLineLocation = fFile.getFilePointer();
                            context.inputLine = input;
                            return event;
                        }
                    }
                } else {
                    if (checkNotNull(countMap.get(currentInput)) >= currentInput.getMinCount()) {
                        final List<InputLine> nextInputs = currentInput.getNextInputs(countMap);
                        if (nextInputs.isEmpty() || nextInputs.get(nextInputs.size() - 1).getMinCount() == 0) {
                            for (final InputLine input : getFirstLines()) {
                                final Matcher matcher = input.getPattern().matcher(line);
                                if (matcher.matches()) {
                                    context.setLocation(new TmfLongLocation(rawPos));
                                    context.firstLineMatcher = matcher;
                                    context.firstLine = line;
                                    context.nextLineLocation = fFile.getFilePointer();
                                    context.inputLine = input;
                                    return event;
                                }
                            }
                        }
                        for (final InputLine input : nextInputs) {
                            final Matcher matcher = input.getPattern().matcher(line);
                            if (matcher.matches()) {
                                event.processGroups(input, matcher);
                                currentInput = input;
                                if (countMap.get(currentInput) == null) {
                                    countMap.put(currentInput, 1);
                                } else {
                                    countMap.put(currentInput, checkNotNull(countMap.get(currentInput)) + 1);
                                }
                                Iterator<InputLine> iter = countMap.keySet().iterator();
                                while (iter.hasNext()) {
                                    final InputLine inputLine = iter.next();
                                    if (inputLine.level > currentInput.level) {
                                        iter.remove();
                                    }
                                }
                                if (currentInput.childrenInputs != null && !currentInput.childrenInputs.isEmpty()) {
                                    currentInput = currentInput.childrenInputs.get(0);
                                    countMap.put(currentInput, 0);
                                } else if (checkNotNull(countMap.get(currentInput)) >= currentInput.getMaxCount()) {
                                    if (!currentInput.getNextInputs(countMap).isEmpty()) {
                                        currentInput = currentInput.getNextInputs(countMap).get(0);
                                        if (countMap.get(currentInput) == null) {
                                            countMap.put(currentInput, 0);
                                        }
                                        iter = countMap.keySet().iterator();
                                        while (iter.hasNext()) {
                                            final InputLine inputLine = iter.next();
                                            if (inputLine.level > currentInput.level) {
                                                iter.remove();
                                            }
                                        }
                                    } else {
                                        currentInput = null;
                                    }
                                }
                                processed = true;
                                break;
                            }
                        }
                    }
                    if (!processed && currentInput != null) {
                        final Matcher matcher = currentInput.getPattern().matcher(line);
                        if (matcher.matches()) {
                            event.processGroups(currentInput, matcher);
                            countMap.put(currentInput, checkNotNull(countMap.get(currentInput)) + 1);
                            if (currentInput.childrenInputs != null && !currentInput.childrenInputs.isEmpty()) {
                                currentInput = currentInput.childrenInputs.get(0);
                                countMap.put(currentInput, 0);
                            } else if (checkNotNull(countMap.get(currentInput)) >= currentInput.getMaxCount()) {
                                if (!currentInput.getNextInputs(countMap).isEmpty()) {
                                    currentInput = currentInput.getNextInputs(countMap).get(0);
                                    if (countMap.get(currentInput) == null) {
                                        countMap.put(currentInput, 0);
                                    }
                                    final Iterator<InputLine> iter = countMap.keySet().iterator();
                                    while (iter.hasNext()) {
                                        final InputLine inputLine = iter.next();
                                        if (inputLine.level > currentInput.level) {
                                            iter.remove();
                                        }
                                    }
                                } else {
                                    currentInput = null;
                                }
                            }
                        }
                        ((StringBuffer) event.getContentValue()).append("\n").append(line); //$NON-NLS-1$
                    }
                }
                rawPos = fFile.getFilePointer();
                line = fFile.getNextLine();
            }
        } catch (final IOException e) {
            Activator.logError("Error seeking event. File: " + getPath(), e); //$NON-NLS-1$
        }
        for (final Entry<InputLine, Integer> entry : countMap.entrySet()) {
            if (entry.getValue() < entry.getKey().getMinCount()) {
                event = null;
            }
        }
        context.setLocation(NULL_LOCATION);
        return event;
    }

    /**
     * @return The first few lines of the text file
     */
    public List<InputLine> getFirstLines() {
        return fDefinition.inputs;
    }

    /**
     * Parse the first line of the trace (to recognize the type).
     *
     * @param context
     *            Trace context
     * @return The first event
     */
    public CustomTxtEvent parseFirstLine(final CustomTxtTraceContext context) {
        CustomTxtEventType eventType = new CustomTxtEventType(checkNotNull(fDefinition.definitionName), fRootField);
        final CustomTxtEvent event = new CustomTxtEvent(fDefinition, this, TmfTimestamp.ZERO, eventType);
        event.processGroups(context.inputLine, context.firstLineMatcher);
        event.setContent(new CustomEventContent(event, new StringBuffer(context.firstLine)));
        return event;
    }

    /**
     * Get the trace definition.
     *
     * @return The trace definition
     */
    public CustomTraceDefinition getDefinition() {
        return fDefinition;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation computes the confidence as the percentage of
     * lines in the first 100 lines of the file which match any of the root
     * input line patterns.
     */
    @Override
    public IStatus validate(IProject project, String path) {
        File file = new File(path);
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.CustomTrace_FileNotFound + ": " + path); //$NON-NLS-1$
        }
        int confidence = 0;
        try {
            if (!TmfTraceUtils.isText(file)) {
                return new TraceValidationStatus(confidence, Activator.PLUGIN_ID);
            }
        } catch (IOException e) {
            Activator.logError("Error validating file: " + path, e); //$NON-NLS-1$
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "IOException validating file: " + path, e); //$NON-NLS-1$
        }
        try (BufferedRandomAccessFile rafile = new BufferedRandomAccessFile(path, "r")) { //$NON-NLS-1$
            int lineCount = 0;
            double matches = 0.0;
            String line = rafile.getNextLine();

            while ((line != null) && (lineCount++ < MAX_LINES)) {
                for (InputLine inputLine : fDefinition.inputs) {
                    Matcher matcher = inputLine.getPattern().matcher(line);
                    if (matcher.matches()) {
                        int groupCount = matcher.groupCount();
                        matches += (1.0 + groupCount / ((double) groupCount + 1));
                        break;
                    }
                }
                confidence = (int) (MAX_CONFIDENCE * matches / lineCount);
                line = rafile.getNextLine();
            }
        } catch (IOException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "IOException validating file: " + path, e); //$NON-NLS-1$
        }
        return new TraceValidationStatus(confidence, Activator.PLUGIN_ID);
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
    public ITmfLocation restoreLocation(ByteBuffer bufferIn) {
        return new TmfLongLocation(bufferIn);
    }

    @Override
    protected ITmfTraceIndexer createIndexer(int interval) {
        return new TmfBTreeTraceIndexer(this, interval);
    }

    @Override
    public String getTraceTypeId() {
        return fTraceTypeId;
    }

    /**
     * Build the trace type id for a custom text trace
     *
     * @param category
     *            the category
     * @param definitionName
     *            the definition name
     * @return the trace type id
     */
    public static @NonNull String buildTraceTypeId(String category, String definitionName) {
        return CUSTOM_TXT_TRACE_TYPE_PREFIX + category + SEPARATOR + definitionName;
    }

    /**
     * Checks whether the given trace type ID is a custom text trace type ID
     *
     * @param traceTypeId
     *                the trace type ID to check
     * @return <code>true</code> if it's a custom text trace type ID else <code>false</code>
     */
    public static boolean isCustomTraceTypeId(@NonNull String traceTypeId) {
        return traceTypeId.startsWith(CUSTOM_TXT_TRACE_TYPE_PREFIX);
    }

    /**
     * This methods builds a trace type ID from a given ID taking into
     * consideration any format changes that were done for the IDs of custom
     * text traces. For example, such format change took place when moving to
     * Trace Compass. Trace type IDs that are part of the plug-in extension for
     * trace types won't be changed.
     *
     * This method is useful for IDs that were persisted in the workspace before
     * the format changes (e.g. in the persistent properties of a trace
     * resource).
     *
     * It ensures backwards compatibility of the workspace for custom text
     * traces.
     *
     * @param traceTypeId
     *            the legacy trace type ID
     * @return the trace type id in Trace Compass format
     */
    public static @NonNull String buildCompatibilityTraceTypeId(@NonNull String traceTypeId) {
        // Handle early Trace Compass custom text trace type IDs
        if (traceTypeId.startsWith(EARLY_TRACE_COMPASS_CUSTOM_TXT_TRACE_TYPE_PREFIX)) {
            return CUSTOM_TXT_TRACE_TYPE_PREFIX + traceTypeId.substring(EARLY_TRACE_COMPASS_CUSTOM_TXT_TRACE_TYPE_PREFIX.length());
        }

        // Handle Linux Tools custom text trace type IDs (with and without category)
        int index = traceTypeId.lastIndexOf(SEPARATOR);
        if ((index != -1) && (traceTypeId.startsWith(LINUX_TOOLS_CUSTOM_TXT_TRACE_TYPE_PREFIX))) {
            String definitionName = index < traceTypeId.length() ? traceTypeId.substring(index + 1) : ""; //$NON-NLS-1$
            if (traceTypeId.contains(CustomTxtTrace.class.getSimpleName() + SEPARATOR) && traceTypeId.indexOf(SEPARATOR) == index) {
                return buildTraceTypeId(CustomTxtTraceDefinition.CUSTOM_TXT_CATEGORY, definitionName);
            }
            return CUSTOM_TXT_TRACE_TYPE_PREFIX + traceTypeId.substring(LINUX_TOOLS_CUSTOM_TXT_TRACE_TYPE_PREFIX.length());
        }
        return traceTypeId;
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
                ITmfEvent event = parseEvent(context);
                context.dispose();
                if (event != null) {
                    /* The last event in the trace was successfully parsed. */
                    return event.getTimestamp();
                }
                /* pos was after the beginning of the lines of the last event. */
                pos--;
            }
        } catch (IOException e) {
            Activator.logError("Error seeking last event. File: " + getPath(), e); //$NON-NLS-1$
        }

        /* Empty trace */
        return null;
    }
}
