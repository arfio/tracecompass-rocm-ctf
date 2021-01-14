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
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.tests.stubs.trace;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventType;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfEventParser;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * <b><u>TmfEventParserStub</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
@SuppressWarnings("javadoc")
public class TmfEventParserStub implements ITmfEventParser {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private static final int NB_TYPES = 10;
    private final TmfEventType[] fTypes;
    private final ITmfTrace fEventStream;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public TmfEventParserStub(final ITmfTrace eventStream) {
        fEventStream = eventStream;
        fTypes = new TmfEventType[NB_TYPES];
        for (int i = 0; i < NB_TYPES; i++) {
            final Vector<String> fields = new Vector<>();
            for (int j = 1; j <= i; j++) {
                final String field = "Fmt-" + i + "-Fld-" + j;
                fields.add(field);
            }
            final String[] fieldArray = new String[i];
            final ITmfEventField rootField = TmfEventField.makeRoot(fields.toArray(fieldArray));
            fTypes[i] = new TmfEventType("Type-" + i, rootField);
        }
    }

    // ------------------------------------------------------------------------
    // Operators
    // ------------------------------------------------------------------------

    static final String typePrefix = "Type-";
    @Override
    public ITmfEvent parseEvent(final ITmfContext context) {

        if (! (fEventStream instanceof TmfTraceStub)) {
            return null;
        }

        // Highly inefficient...
        final RandomAccessFile stream = ((TmfTraceStub) fEventStream).getStream();
        if (stream == null) {
            return null;
        }

        //           String name = eventStream.getName();
        //           name = name.substring(name.lastIndexOf('/') + 1);

        // no need to use synchronized since it's already cover by the calling method

        long location = 0;
        if (context != null && context.getLocation() != null) {
            location = (Long) context.getLocation().getLocationInfo();
            try {
                stream.seek(location);

                final long ts        = stream.readLong();
                stream.readUTF(); /* Previously source, now unused */
                final String type    = stream.readUTF();
                stream.readInt(); /* Previously reference, now unused */
                final int typeIndex  = Integer.parseInt(type.substring(typePrefix.length()));
                final String[] fields = new String[typeIndex];
                for (int i = 0; i < typeIndex; i++) {
                    fields[i] = stream.readUTF();
                }

                final StringBuffer content = new StringBuffer("[");
                if (typeIndex > 0) {
                    content.append(fields[0]);
                }
                for (int i = 1; i < typeIndex; i++) {
                    content.append(", ").append(fields[i]);
                }
                content.append("]");

                final TmfEventField root = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, content.toString(), null);
                final ITmfEvent event = new TmfEvent(fEventStream,
                        ITmfContext.UNKNOWN_RANK,
                        fEventStream.createTimestamp(ts * 1000000L),
                        fTypes[typeIndex], root);
                return event;
            } catch (final EOFException e) {
            } catch (final IOException e) {
            }
        }
        return null;
    }

}
