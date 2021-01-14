/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.parsers.custom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfBaseAspects;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition.Tag;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtEvent;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtTrace;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtTraceDefinition;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;

/**
 * Test the events parsed by a custom txt trace
 *
 * @author Geneviève Bastien
 */
@RunWith(Parameterized.class)
public class CustomTxtTraceDataTest extends AbstractCustomTraceDataTest {

    private static final String TRACE_PATH = TRACE_DIRECTORY + File.separator + "test.txt";
    private static final String DEFINITION_PATH = "testfiles" + File.separator + "txt" + File.separator + "testTxtDefinition.xml";

    /**
     * Constructor
     *
     * @param name
     *            The name of the test
     * @param data
     *            The test data
     */
    public CustomTxtTraceDataTest(String name, @NonNull ICustomTestData data) {
        super(data);
    }

    private static CustomTxtTraceDefinition getDefinition(int index) {
        CustomTxtTraceDefinition[] definitions = CustomTxtTraceDefinition.loadAll(new File(DEFINITION_PATH).toString());
        return definitions[index];
    }

    private static final ICustomTestData CUSTOM_TXT = new ICustomTestData() {

        private static final int NB_EVENTS = 10;
        private CustomTxtTraceDefinition fDefinition;
        private ITmfEventAspect<?> fTimestampAspect;

        @Override
        public ITmfTrace getTrace() throws IOException, TmfTraceException {
            fDefinition = getDefinition(0);
            final File file = new File(TRACE_PATH);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file));) {
                for (int i = 0; i < NB_EVENTS; ++i) {
                    SimpleDateFormat f = new SimpleDateFormat(TIMESTAMP_FORMAT);
                    String eventStr = f.format(new Date(i)) + " hello world\n";
                    writer.write(eventStr);
                    int extra = i % 3;
                    for (int j = 0; j < extra; j++) {
                        writer.write("extra line\n");
                    }
                }
            }
            ITmfTrace trace = new CustomTxtTrace(null, fDefinition, file.getPath(), BLOCK_SIZE);
            ArrayList<@NonNull ITmfEventAspect<?>> aspects = Lists.newArrayList(trace.getEventAspects());
            fTimestampAspect = aspects.stream().filter(aspect -> aspect.getName().equals("Timestamp")).findFirst().get();
            return trace;
        }

        @Override
        public void validateEvent(ITmfEvent event) {
            assertTrue(event instanceof CustomTxtEvent);
            String name = fDefinition.definitionName;
            assertEquals("Event name", name, event.getName());
            assertEquals("Event name and type", event.getType().getName(), event.getName());
            assertEquals("Timestamp", Long.toString(event.getTimestamp().toNanos()), fTimestampAspect.resolve(event));
        }

        @Override
        public void validateEventCount(int eventCount) {
            assertEquals("Event count", NB_EVENTS, eventCount);
        }

    };

    private static final ICustomTestData CUSTOM_TXT_EVENT_NAME = new ICustomTestData() {

        private static final int NB_EVENTS = 10;
        private static final String DEFAULT_EVENT = "DefaultName";
        private static final String ODD_EVENT = "OddName";
        private static final String EVEN_EVENT = "EvenName";
        private CustomTxtTraceDefinition fDefinition;
        private ITmfEventAspect<?> fTimestampAspect;

        @Override
        public ITmfTrace getTrace() throws IOException, TmfTraceException {
            fDefinition = getDefinition(1);
            final File file = new File(TRACE_PATH);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file));) {
                for (int i = 1; i <= NB_EVENTS; ++i) {
                    String evName = (i % 5) == 0 ? DEFAULT_EVENT : ((i % 2) == 0) ? EVEN_EVENT : ODD_EVENT;
                    String eventStr = i + " " + evName + "\n";
                    writer.write(eventStr);
                    int extra = i % 3;
                    for (int j = 0; j < extra; j++) {
                        writer.write("extra line\n");
                    }
                }
            }
            ITmfTrace trace = new CustomTxtTrace(null, fDefinition, file.getPath(), BLOCK_SIZE);
            ArrayList<@NonNull ITmfEventAspect<?>> aspects = Lists.newArrayList(trace.getEventAspects());
            fTimestampAspect = aspects.stream().filter(aspect -> aspect.getName().equals("Timestamp")).findFirst().get();
            return trace;
        }

        @Override
        public void validateEvent(ITmfEvent event) {
            assertTrue(event instanceof CustomTxtEvent);
            long ts = event.getTimestamp().getValue();
            if (ts % 5 == 0) {
                assertEquals("Event name", DEFAULT_EVENT, event.getName());
            } else if (ts % 2 == 0) {
                assertEquals("Event name", EVEN_EVENT, event.getName());
            } else {
                assertEquals("Event name", ODD_EVENT, event.getName());
            }
            assertEquals("Event name and type", event.getType().getName(), event.getName());
            assertEquals("Timestamp", TmfBaseAspects.getTimestampAspect().resolve(event), fTimestampAspect.resolve(event));
        }

        @Override
        public void validateEventCount(int eventCount) {
            assertEquals("Event count", NB_EVENTS, eventCount);
        }

    };

    private static final ICustomTestData CUSTOM_TXT_EXTRA_FIELDS = new ICustomTestData() {

        private static final int NB_EVENTS = 6;
        private static final String FOO = "foo";
        private static final String BAR = "bar";
        private static final String BAZ = "baz";
        private static final String MESSAGE = "message";
        private CustomTxtTraceDefinition fDefinition;

        @Override
        public ITmfTrace getTrace() throws IOException, TmfTraceException {
            fDefinition = getDefinition(2);
            final File file = new File(TRACE_PATH);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file));) {
                // Event with one field to set
                String eventStr = String.format("1 %s %s=%s\n", MESSAGE, FOO, BAR);
                writer.write(eventStr);
                // Event with 2 different fields and different values
                eventStr = String.format("2 %s %s=%s %s=%s\n", MESSAGE, FOO, BAR, BAR, FOO);
                writer.write(eventStr);
                // Event with an extra field that conflicts with a built-in field
                eventStr = String.format("3 %s Message=%s\n", MESSAGE, FOO);
                writer.write(eventStr);
                // Event with 2 extra fields with same name where the values
                // should be appended
                eventStr = String.format("4 %s %s=%s %s=%s\n", MESSAGE, FOO, BAR, FOO, BAZ);
                writer.write(eventStr);
                // Event with 2 extra fields with same name, where the values
                // should be set
                eventStr = String.format("5 %s %s=%s %s=%s %s=%s\n", MESSAGE, FOO, BAR, FOO, BAZ, BAR, BAZ);
                writer.write(eventStr);
                // Event with 2 non matching number extra field names/values
                eventStr = String.format("6 %s %s=%s other %s\n", MESSAGE, FOO, BAR, BAZ);
                writer.write(eventStr);
            }
            return new CustomTxtTrace(null, fDefinition, file.getPath(), BLOCK_SIZE);
        }

        @Override
        public void validateEvent(ITmfEvent event) {
            assertTrue(event instanceof CustomTxtEvent);
            long ts = event.getTimestamp().getValue();
            switch ((int) ts) {
            case 1:
                assertNotNull(event.getContent().getField(FOO));
                assertEquals(BAR, event.getContent().getField(FOO).getValue());
                assertNull(event.getContent().getField(BAR));
                break;
            case 2:
                assertNotNull(event.getContent().getField(FOO));
                assertEquals(BAR, event.getContent().getField(FOO).getValue());
                assertNotNull(event.getContent().getField(BAR));
                assertEquals(FOO, event.getContent().getField(BAR).getValue());
                break;
            case 3:
                assertNotNull(event.getContent().getField(Tag.MESSAGE.toString()));
                assertEquals(MESSAGE, event.getContent().getField(Tag.MESSAGE.toString()).getValue());
                break;
            case 4:
                assertNotNull(event.getContent().getField(FOO));
                assertEquals(BAR + CustomTraceDefinition.SEPARATOR + BAZ, event.getContent().getField(FOO).getValue());
                assertNull(event.getContent().getField(BAR));
                break;
            case 5:
                assertNotNull(event.getContent().getField(FOO));
                assertEquals(BAZ, event.getContent().getField(FOO).getValue());
                assertNotNull(event.getContent().getField(BAR));
                assertEquals(BAZ, event.getContent().getField(BAR).getValue());
                break;
            case 6:
                assertNotNull(event.getContent().getField(FOO));
                assertEquals(BAR + CustomTraceDefinition.SEPARATOR + BAZ, event.getContent().getField(FOO).getValue());
                assertNull(event.getContent().getField(BAR));
                break;
            default:
                fail("unknown timestamp " + ts);
            }
            assertEquals("Event name and type", event.getType().getName(), event.getName());
        }

        @Override
        public void validateEventCount(int eventCount) {
            assertEquals("Event count", NB_EVENTS, eventCount);
        }

    };

    /**
     * @return The arrays of parameters
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                { "Base parser", CUSTOM_TXT },
                { "Parse with event name", CUSTOM_TXT_EVENT_NAME },
                { "Parse with extra fields", CUSTOM_TXT_EXTRA_FIELDS }
        });
    }

}
