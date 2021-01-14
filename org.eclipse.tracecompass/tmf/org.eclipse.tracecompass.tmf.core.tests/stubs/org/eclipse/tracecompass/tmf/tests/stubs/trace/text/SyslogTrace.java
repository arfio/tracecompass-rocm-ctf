/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
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
 *   Patrick Tasse - Move field declarations to trace
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.tests.stubs.trace.text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfBaseAspects;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfContentFieldAspect;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimePreferences;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.tracecompass.tmf.core.trace.text.TextTrace;
import org.eclipse.tracecompass.tmf.core.trace.text.TextTraceEventContent;

import com.google.common.collect.ImmutableList;

/**
 * Extension of TmfTrace for handling of system logs.
 */
public class SyslogTrace extends TextTrace<SyslogEvent> {

    /** The cache size for system log traces. */
    private static final int CACHE_SIZE = 100;
    /** The time stamp format of the trace type. */
    public static final String TIMESTAMP_FORMAT = "MMM dd HH:mm:ss"; //$NON-NLS-1$
    /** The corresponding date format of the time stamp. */
    public static final SimpleDateFormat TIMESTAMP_SIMPLEDATEFORMAT = new SimpleDateFormat(
            TIMESTAMP_FORMAT, TmfTimePreferences.getLocale());
    /** The regular expression pattern of the first line of an event. */
    public static final Pattern LINE1_PATTERN = Pattern.compile(
            "\\s*(\\S\\S\\S \\d\\d? \\d\\d:\\d\\d:\\d\\d)\\s*(\\S*)\\s*(\\S*):+\\s*(\\S*):([0-9]*)\\s*(.*\\S)?"); //$NON-NLS-1$

    /* The current calendar to use */
    private static final Calendar CURRENT = Calendar.getInstance();

    /** The event fields */
    @SuppressWarnings({"javadoc", "nls"})
    public interface Field {
        @NonNull String HOST = "Host";
        @NonNull String LOGGER = "Logger";
        @NonNull String FILE = "File";
        @NonNull String LINE = "Line";
        @NonNull String MESSAGE = "Message";
    }

    /** The event aspects */
    public static final @NonNull Collection<ITmfEventAspect<?>> ASPECTS =
            ImmutableList.of(
                    TmfBaseAspects.getTimestampAspect(),
                    new TmfContentFieldAspect(Field.HOST, Field.HOST),
                    new TmfContentFieldAspect(Field.LOGGER, Field.LOGGER),
                    new TmfContentFieldAspect(Field.FILE, Field.FILE),
                    new TmfContentFieldAspect(Field.LINE, Field.LINE),
                    new TmfContentFieldAspect(Field.MESSAGE, Field.MESSAGE)
                    );

    /**
     * Constructor
     */
    public SyslogTrace() {
        setCacheSize(CACHE_SIZE);
    }

    @Override
    protected Pattern getFirstLinePattern() {
        return LINE1_PATTERN;
    }

    @Override
    protected SyslogEvent parseFirstLine(Matcher matcher, String line) {

        ITmfTimestamp timestamp = null;

        try {
            synchronized (TIMESTAMP_SIMPLEDATEFORMAT) {
                TIMESTAMP_SIMPLEDATEFORMAT.setTimeZone(TmfTimestampFormat.getDefaulTimeFormat().getTimeZone());
                Date date = TIMESTAMP_SIMPLEDATEFORMAT.parse(matcher.group(1));
                GregorianCalendar calendar = new GregorianCalendar();
                calendar.setTime(date);
                calendar.set(Calendar.YEAR, CURRENT.get(Calendar.YEAR));
                if (calendar.after(CURRENT)) {
                    calendar.set(Calendar.YEAR, CURRENT.get(Calendar.YEAR) - 1);
                }
                long ns = calendar.getTimeInMillis() * 1000000;
                timestamp = createTimestamp(ns);
            }
        } catch (ParseException e) {
            timestamp = TmfTimestamp.create(0, ITmfTimestamp.SECOND_SCALE);
        }

        TextTraceEventContent content = new TextTraceEventContent(5);
        content.setValue(new StringBuffer(line));
        content.addField(Field.HOST, matcher.group(2));
        content.addField(Field.LOGGER, matcher.group(3));
        content.addField(Field.FILE, matcher.group(4));
        content.addField(Field.LINE, matcher.group(5));
        content.addField(Field.MESSAGE, new StringBuffer(matcher.group(6) != null ? matcher.group(6) : "")); //$NON-NLS-1$

        SyslogEvent event = new SyslogEvent(
                this,
                timestamp,
                SyslogEventType.INSTANCE,
                content); //$NON-NLS-1$

        return event;
    }

    @Override
    protected void parseNextLine(SyslogEvent event, String line) {
        TextTraceEventContent content = event.getContent();
        ((StringBuffer) content.getValue()).append("\n").append(line); //$NON-NLS-1$
        if (line.trim().length() > 0) {
            ((StringBuffer) content.getFieldValue(Field.MESSAGE)).append(SEPARATOR + line.trim());
        }
    }

    @Override
    public ITmfTimestamp getInitialRangeOffset() {
        return TmfTimestamp.fromSeconds(60);
    }

    @Override
    public Iterable<ITmfEventAspect<?>> getEventAspects() {
        return ASPECTS;
    }
}
