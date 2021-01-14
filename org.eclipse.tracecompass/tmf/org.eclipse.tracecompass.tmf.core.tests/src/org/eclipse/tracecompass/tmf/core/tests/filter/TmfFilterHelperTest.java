/*******************************************************************************
 * Copyright (c) 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filter.parser.FilterCu;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filter.parser.IFilterStrings;
import org.eclipse.tracecompass.internal.tmf.core.filter.TmfFilterHelper;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventType;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfBaseAspects;
import org.eclipse.tracecompass.tmf.core.filter.ITmfFilter;
import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterAndNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterCompareNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterContainsNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterEqualsNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterMatchesNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterOrNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterRootNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterTraceTypeNode;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

/**
 * Test the {@link TmfFilterHelper} class to convert event filter to/from
 * regexes
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault
public class TmfFilterHelperTest {

    private static @Nullable ITmfTrace STUB_TRACE;

    private static final String FIELD1_NAME = "afield";
    private static final String FIELD2_NAME = "bfield";
    private static final String EVENT_NAME1 = "type1";
    private static final String EVENT_NAME2 = "type2";
    private static final String EVENT_NAME3 = "type3";
    private static final ITmfEventType EVENT_TYPE1 = new TmfEventType(EVENT_NAME1, TmfEventField.makeRoot(new String[] { FIELD1_NAME }));
    private static final ITmfEventType EVENT_TYPE2 = new TmfEventType(EVENT_NAME2, TmfEventField.makeRoot(new String[] { FIELD1_NAME }));
    private static final ITmfEventType EVENT_TYPE3 = new TmfEventType(EVENT_NAME3, TmfEventField.makeRoot(new String[] { FIELD1_NAME }));
    private static final String FIELD1_VALUE1 = "afield value 1";
    private static final String FIELD1_VALUE2 = "another afield value";
    private static final String FIELD2_VALUE1 = "1";
    private static final String FIELD2_VALUE2 = "2";

    private ITmfEventField[] fFields1 = new ITmfEventField[] { new TmfEventField(FIELD1_NAME, FIELD1_VALUE1, null) };
    private ITmfEventField[] fFields2 = new ITmfEventField[] { new TmfEventField(FIELD2_NAME, FIELD2_VALUE1, null) };
    private ITmfEventField[] fFields3 = new ITmfEventField[] { new TmfEventField(FIELD1_NAME, FIELD1_VALUE2, null), new TmfEventField(FIELD2_NAME, FIELD2_VALUE2, null) };
    private ITmfEventField fContent1 = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, null, fFields1);
    private ITmfEventField fContent2 = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, null, fFields2);
    private ITmfEventField fContent3 = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, null, fFields3);
    private TmfEvent fEvent1 = new TmfEvent(STUB_TRACE, 0, TmfTimestamp.fromNanos(1), EVENT_TYPE1, fContent1);
    private TmfEvent fEvent2 = new TmfEvent(STUB_TRACE, 1, TmfTimestamp.fromNanos(2), EVENT_TYPE2, fContent2);
    private TmfEvent fEvent3 = new TmfEvent(STUB_TRACE, 2, TmfTimestamp.fromNanos(3), EVENT_TYPE3, fContent3);

    private Multimap<String, Object> fObjectMap1 = ImmutableMultimap.of(FIELD1_NAME, FIELD1_VALUE1, TmfBaseAspects.getEventTypeAspect().getName(), EVENT_NAME1);
    private Multimap<String, Object> fObjectMap2 = ImmutableMultimap.of(FIELD2_NAME, FIELD2_VALUE1, TmfBaseAspects.getEventTypeAspect().getName(), EVENT_NAME2);
    private Multimap<String, Object> fObjectMap3 = ImmutableMultimap.of(FIELD1_NAME, FIELD1_VALUE2, FIELD2_NAME, FIELD2_VALUE2, TmfBaseAspects.getEventTypeAspect().getName(), EVENT_NAME3);

    /**
     * Initialize the trace
     */
    @BeforeClass
    public static void initTrace() {
        STUB_TRACE = TmfTestTrace.A_TEST_10K.getTrace();
    }

    /**
     * Cleanup the trace
     */
    @AfterClass
    public static void cleanUp() {
        ITmfTrace trace = STUB_TRACE;
        if (trace != null) {
            trace.dispose();
        }
    }

    private static ITmfFilter getFilter(String regex) {
        ITmfTrace trace = STUB_TRACE;
        assertNotNull(trace);
        ITmfFilter filter = TmfFilterHelper.buildFilterFromRegex(Collections.singleton(regex), trace);
        assertNotNull(filter);
        return filter;
    }

    private static Predicate<Multimap<String, Object>> getRegex(ITmfFilter filter, String expected) {
        String regex = TmfFilterHelper.getRegexFromFilter(filter);
        assertEquals(expected, regex);
        FilterCu compile = FilterCu.compile(regex);
        assertNotNull(compile);
        Predicate<Multimap<String, Object>> predicate = compile.generate();
        return predicate;
    }

    /**
     * Test a regex whose parameter corresponds to an aspect name
     */
    @Test
    public void testInputRegexAspect() {
        ITmfEventAspect<@NonNull String> eventTypeAspect = TmfBaseAspects.getEventTypeAspect();
        String regex = "\"" + eventTypeAspect.getName() + "\" == " + EVENT_NAME1;
        ITmfFilter filter = getFilter(regex);

        // verify the main root node
        assertTrue(filter instanceof TmfFilterRootNode);
        TmfFilterRootNode node = (TmfFilterRootNode) filter;
        assertEquals(1, node.getChildrenCount());
        ITmfFilterTreeNode child = node.getChild(0);
        assertTrue(child instanceof TmfFilterEqualsNode);

        // verify the equals node
        TmfFilterEqualsNode equalsNode = (TmfFilterEqualsNode) child;
        assertEquals(eventTypeAspect, equalsNode.getEventAspect());
        assertEquals(EVENT_NAME1, equalsNode.getValue());

        // Test expected behavior on events
        assertTrue(filter.matches(fEvent1));
        assertFalse(filter.matches(fEvent2));
        assertFalse(filter.matches(fEvent3));
    }

    /**
     * Test a regex whose parameter is not an aspect name
     */
    @Test
    public void testInputRegexNoAspect() {
        ITmfEventAspect<@NonNull Object> contentAspect = TmfBaseAspects.getContentsAspect().forField(FIELD1_NAME);
        String regex = FIELD1_NAME + " == \"" + FIELD1_VALUE1 + "\"";
        ITmfFilter filter = getFilter(regex);

        assertTrue(filter instanceof TmfFilterRootNode);
        TmfFilterRootNode node = (TmfFilterRootNode) filter;
        assertEquals(1, node.getChildrenCount());
        ITmfFilterTreeNode child = node.getChild(0);
        assertTrue(child instanceof TmfFilterEqualsNode);

        TmfFilterEqualsNode equalsNode = (TmfFilterEqualsNode) child;
        assertEquals(contentAspect, equalsNode.getEventAspect());
        assertEquals(FIELD1_VALUE1, equalsNode.getValue());

        // Test expected behavior on events
        assertTrue(filter.matches(fEvent1));
        assertFalse(filter.matches(fEvent2));
        assertFalse(filter.matches(fEvent3));
    }

    /**
     * Test a regex with no parameter
     */
    @Test
    public void testInputRegexNoField() {
        String regex = "afield";
        ITmfFilter filter = getFilter(regex);

        assertTrue(filter instanceof TmfFilterRootNode);
        TmfFilterRootNode node = (TmfFilterRootNode) filter;
        assertEquals(1, node.getChildrenCount());
        ITmfFilterTreeNode child = node.getChild(0);
        assertTrue(child instanceof TmfFilterOrNode);

        // Verify the orNode
        TmfFilterOrNode orNode = (TmfFilterOrNode) child;
        ITmfTrace trace = STUB_TRACE;
        assertNotNull(trace);
        Iterable<@NonNull ITmfEventAspect<?>> eventAspects = trace.getEventAspects();

        // Verify that each child is a matches node and there's one per aspect
        assertEquals(Iterables.size(eventAspects), orNode.getChildrenCount());
        for (int i = 0; i < orNode.getChildrenCount(); i++) {
            assertTrue(orNode.getChild(i) instanceof TmfFilterMatchesNode);
        }
        for (ITmfEventAspect<?> aspect : eventAspects) {
            // Find a contains condition for each aspect
            ITmfFilterTreeNode[] children = orNode.getChildren();
            TmfFilterMatchesNode found = null;
            for (int i = 0; i < children.length; i++) {
                TmfFilterMatchesNode childFilter = (TmfFilterMatchesNode) children[i];
                if (aspect.equals(childFilter.getEventAspect())) {
                    found = childFilter;
                    break;
                }
            }
            assertNotNull("found aspect " + aspect.getName(), found);
            assertEquals(regex, found.getRegex());
        }

        // Test expected behavior on events
        assertTrue(filter.matches(fEvent1));
        assertFalse(filter.matches(fEvent2));
        assertTrue(filter.matches(fEvent3));
    }

    /**
     * Test a regex with contains
     */
    @Test
    public void testInputRegexContains() {
        ITmfEventAspect<@NonNull String> eventTypeAspect = TmfBaseAspects.getEventTypeAspect();
        String regex = "\"" + eventTypeAspect.getName() + "\" contains 1";
        ITmfFilter filter = getFilter(regex);

        // verify the main root node
        assertTrue(filter instanceof TmfFilterRootNode);
        TmfFilterRootNode node = (TmfFilterRootNode) filter;
        assertEquals(1, node.getChildrenCount());
        ITmfFilterTreeNode child = node.getChild(0);
        assertTrue(child instanceof TmfFilterContainsNode);

        // verify the equals node
        TmfFilterContainsNode equalsNode = (TmfFilterContainsNode) child;
        assertEquals(eventTypeAspect, equalsNode.getEventAspect());
        assertEquals("1", equalsNode.getValue());

        // Test expected behavior on events
        assertTrue(filter.matches(fEvent1));
        assertFalse(filter.matches(fEvent2));
        assertFalse(filter.matches(fEvent3));
    }

    /**
     * Test a regex with present
     */
    @Test
    public void testInputRegexPresent() {
        ITmfEventAspect<@NonNull Object> aspect = TmfBaseAspects.getContentsAspect().forField(FIELD1_NAME);
        String regex = FIELD1_NAME + " present";
        ITmfFilter filter = getFilter(regex);

        // verify the main root node
        assertTrue(filter instanceof TmfFilterRootNode);
        TmfFilterRootNode node = (TmfFilterRootNode) filter;
        assertEquals(1, node.getChildrenCount());
        ITmfFilterTreeNode child = node.getChild(0);
        assertTrue(child instanceof TmfFilterMatchesNode);

        // verify the equals node
        TmfFilterMatchesNode equalsNode = (TmfFilterMatchesNode) child;
        assertEquals(aspect, equalsNode.getEventAspect());
        assertEquals(".*", equalsNode.getRegex());

        // Test expected behavior on events
        assertTrue(filter.matches(fEvent1));
        assertFalse(filter.matches(fEvent2));
        assertTrue(filter.matches(fEvent3));
    }

    /**
     * Test a regex with matches
     */
    @Test
    public void testInputRegexMatches() {
        ITmfEventAspect<@NonNull Object> contentAspect = TmfBaseAspects.getContentsAspect().forField(FIELD1_NAME);
        String regex = FIELD1_NAME + " matches .*other.*";
        ITmfFilter filter = getFilter(regex);

        // verify the main root node
        assertTrue(filter instanceof TmfFilterRootNode);
        TmfFilterRootNode node = (TmfFilterRootNode) filter;
        assertEquals(1, node.getChildrenCount());
        ITmfFilterTreeNode child = node.getChild(0);
        assertTrue(child instanceof TmfFilterMatchesNode);

        // verify the equals node
        TmfFilterMatchesNode equalsNode = (TmfFilterMatchesNode) child;
        assertEquals(contentAspect, equalsNode.getEventAspect());
        assertEquals(".*other.*", equalsNode.getRegex());

        // Test expected behavior on events
        assertFalse(filter.matches(fEvent1));
        assertFalse(filter.matches(fEvent2));
        assertTrue(filter.matches(fEvent3));
    }

    /**
     * Test a regex whose parameter is not an aspect name
     */
    @Test
    public void testInputRegexCompare() {
        ITmfEventAspect<@NonNull Object> contentAspect = TmfBaseAspects.getContentsAspect().forField(FIELD2_NAME);
        /* Test the greater than operator */
        String regex = FIELD2_NAME + " > " + FIELD2_VALUE1;
        ITmfFilter filter = getFilter(regex);

        assertTrue(filter instanceof TmfFilterRootNode);
        TmfFilterRootNode node = (TmfFilterRootNode) filter;
        assertEquals(1, node.getChildrenCount());
        ITmfFilterTreeNode child = node.getChild(0);
        assertTrue(child instanceof TmfFilterCompareNode);

        TmfFilterCompareNode equalsNode = (TmfFilterCompareNode) child;
        assertEquals(contentAspect, equalsNode.getEventAspect());
        assertEquals(FIELD2_VALUE1, equalsNode.getValue());
        assertEquals(1, equalsNode.getResult());

        // Test expected behavior on events
        assertFalse(filter.matches(fEvent1));
        assertFalse(filter.matches(fEvent2));
        assertTrue(filter.matches(fEvent3));

        /* Test the less than operator */
        regex = FIELD2_NAME + " < " + FIELD2_VALUE2;
        filter = getFilter(regex);

        assertTrue(filter instanceof TmfFilterRootNode);
        node = (TmfFilterRootNode) filter;
        assertEquals(1, node.getChildrenCount());
        child = node.getChild(0);
        assertTrue(child instanceof TmfFilterCompareNode);

        equalsNode = (TmfFilterCompareNode) child;
        assertEquals(contentAspect, equalsNode.getEventAspect());
        assertEquals(FIELD2_VALUE2, equalsNode.getValue());
        assertEquals(-1, equalsNode.getResult());

        // Test expected behavior on events
        assertFalse(filter.matches(fEvent1));
        assertTrue(filter.matches(fEvent2));
        assertFalse(filter.matches(fEvent3));
    }

    /**
     * Test a regex with &&
     */
    @Test
    public void testInputRegexAnd() {
        String regex = FIELD1_NAME + " matches .*afield.* && " + FIELD2_NAME + " present";
        ITmfFilter filter = getFilter(regex);
        ITmfEventAspect<@NonNull Object> aspectF1 = TmfBaseAspects.getContentsAspect().forField(FIELD1_NAME);
        ITmfEventAspect<@NonNull Object> aspectF2 = TmfBaseAspects.getContentsAspect().forField(FIELD2_NAME);

        // verify the main root node
        assertTrue(filter instanceof TmfFilterRootNode);
        TmfFilterRootNode node = (TmfFilterRootNode) filter;
        assertEquals(1, node.getChildrenCount());
        ITmfFilterTreeNode child = node.getChild(0);
        assertTrue(child instanceof TmfFilterAndNode);
        TmfFilterAndNode andNode = (TmfFilterAndNode) child;
        assertEquals(2, andNode.getChildrenCount());

        // Verify first child, a matches node
        child = andNode.getChild(0);
        assertTrue(child instanceof TmfFilterMatchesNode);
        TmfFilterMatchesNode equalsNode = (TmfFilterMatchesNode) child;
        assertEquals(aspectF1, equalsNode.getEventAspect());
        assertEquals(".*afield.*", equalsNode.getRegex());

        // Verify second child the present node
        child = andNode.getChild(1);
        assertTrue(child instanceof TmfFilterMatchesNode);
        equalsNode = (TmfFilterMatchesNode) child;
        assertEquals(aspectF2, equalsNode.getEventAspect());
        assertEquals(".*", equalsNode.getRegex());

        // Test expected behavior on events
        assertFalse(filter.matches(fEvent1));
        assertFalse(filter.matches(fEvent2));
        assertTrue(filter.matches(fEvent3));
    }

    /**
     * Test a regex with a not matches
     */
    @Test
    public void testInputRegexMatchesNot() {
        ITmfEventAspect<@NonNull Object> contentAspect = TmfBaseAspects.getContentsAspect().forField(FIELD1_NAME);
        String regex = "!(" + FIELD1_NAME + " matches .*other.*)";
        ITmfFilter filter = getFilter(regex);

        // verify the main root node
        assertTrue(filter instanceof TmfFilterRootNode);
        TmfFilterRootNode node = (TmfFilterRootNode) filter;
        assertEquals(1, node.getChildrenCount());
        ITmfFilterTreeNode child = node.getChild(0);
        assertTrue(child instanceof TmfFilterMatchesNode);

        // verify the equals node
        TmfFilterMatchesNode equalsNode = (TmfFilterMatchesNode) child;
        assertEquals(contentAspect, equalsNode.getEventAspect());
        assertEquals(".*other.*", equalsNode.getRegex());

        // Test expected behavior on events
        assertTrue(filter.matches(fEvent1));
        assertTrue(filter.matches(fEvent2));
        assertFalse(filter.matches(fEvent3));
    }

    /**
     * Test a negative regex on an && regex
     */
    @Test
    public void testInputRegexNot() {
        String regex = "!(" + FIELD1_NAME + " matches .*afield.* && " + FIELD2_NAME + " present)";
        ITmfFilter filter = getFilter(regex);
        ITmfEventAspect<@NonNull Object> aspectF1 = TmfBaseAspects.getContentsAspect().forField(FIELD1_NAME);
        ITmfEventAspect<@NonNull Object> aspectF2 = TmfBaseAspects.getContentsAspect().forField(FIELD2_NAME);

        // verify the main root node
        assertTrue(filter instanceof TmfFilterRootNode);
        TmfFilterRootNode node = (TmfFilterRootNode) filter;
        assertEquals(1, node.getChildrenCount());
        ITmfFilterTreeNode child = node.getChild(0);
        assertTrue(child instanceof TmfFilterAndNode);
        TmfFilterAndNode andNode = (TmfFilterAndNode) child;
        assertEquals(2, andNode.getChildrenCount());

        // Verify first child, a matches node
        child = andNode.getChild(0);
        assertTrue(child instanceof TmfFilterMatchesNode);
        TmfFilterMatchesNode equalsNode = (TmfFilterMatchesNode) child;
        assertEquals(aspectF1, equalsNode.getEventAspect());
        assertEquals(".*afield.*", equalsNode.getRegex());

        // Verify second child the present node
        child = andNode.getChild(1);
        assertTrue(child instanceof TmfFilterMatchesNode);
        equalsNode = (TmfFilterMatchesNode) child;
        assertEquals(aspectF2, equalsNode.getEventAspect());
        assertEquals(".*", equalsNode.getRegex());

        // Test expected behavior on events
        assertTrue(filter.matches(fEvent1));
        assertTrue(filter.matches(fEvent2));
        assertFalse(filter.matches(fEvent3));
    }

    private static String notRegex(String string) {
        return "!(" + string + ")";
    }

    /**
     * Test converting a filter that does not support regex conversion
     */
    @Test
    public void testInputFilterUnsupported() {
        TmfFilterTraceTypeNode filter = new TmfFilterTraceTypeNode(null);

        String regex = TmfFilterHelper.getRegexFromFilter(filter);
        assertEquals("", regex);
    }

    /**
     * Test converting a compare filter to regex
     */
    @Test
    public void testInputFilterCompare() {
        ITmfEventAspect<@NonNull Object> aspect = TmfBaseAspects.getContentsAspect().forField(FIELD2_NAME);
        /* Test the greater than operator */
        String expected = "\"" + FIELD2_NAME + "\" " + IFilterStrings.GT + " \"" + FIELD2_VALUE1 + "\"";
        TmfFilterCompareNode compareFilter = new TmfFilterCompareNode(null);
        compareFilter.setEventAspect(aspect);
        compareFilter.setValue(FIELD2_VALUE1);
        compareFilter.setResult(1);

        Predicate<Multimap<String, Object>> predicate = getRegex(compareFilter, expected);
        assertFalse(predicate.test(fObjectMap1));
        assertFalse(predicate.test(fObjectMap2));
        assertTrue(predicate.test(fObjectMap3));

        // Test the negation of this filter
        compareFilter.setNot(true);
        predicate = getRegex(compareFilter, notRegex(expected));
        assertTrue(predicate.test(fObjectMap1));
        assertTrue(predicate.test(fObjectMap2));
        assertFalse(predicate.test(fObjectMap3));

        /* Test the less than operator */
        expected = "\"" + FIELD2_NAME + "\" " + IFilterStrings.LT + " \"" + FIELD2_VALUE2 + "\"";
        compareFilter = new TmfFilterCompareNode(null);
        compareFilter.setEventAspect(aspect);
        compareFilter.setValue(FIELD2_VALUE2);
        compareFilter.setResult(-1);

        predicate = getRegex(compareFilter, expected);
        assertFalse(predicate.test(fObjectMap1));
        assertTrue(predicate.test(fObjectMap2));
        assertFalse(predicate.test(fObjectMap3));

        /* Test the less than operator */
        expected = "\"" + FIELD2_NAME + "\" " + IFilterStrings.EQUAL + " \"" + FIELD2_VALUE1 + "\"";
        compareFilter = new TmfFilterCompareNode(null);
        compareFilter.setEventAspect(aspect);
        compareFilter.setValue(FIELD2_VALUE1);
        compareFilter.setResult(0);

        predicate = getRegex(compareFilter, expected);
        assertFalse(predicate.test(fObjectMap1));
        assertTrue(predicate.test(fObjectMap2));
        assertFalse(predicate.test(fObjectMap3));
    }

    /**
     * Test converting a contains filter to regex
     */
    @Test
    public void testInputFilterContains() {
        ITmfEventAspect<@NonNull Object> aspect = TmfBaseAspects.getContentsAspect().forField(FIELD1_NAME);
        /* Test the greater than operator */
        String expected = "\"" + FIELD1_NAME + "\" " + IFilterStrings.CONTAINS + " \"other\"";
        TmfFilterContainsNode containsFilter = new TmfFilterContainsNode(null);
        containsFilter.setEventAspect(aspect);
        containsFilter.setValue("other");

        Predicate<Multimap<String, Object>> predicate = getRegex(containsFilter, expected);
        assertFalse(predicate.test(fObjectMap1));
        assertFalse(predicate.test(fObjectMap2));
        assertTrue(predicate.test(fObjectMap3));

        // Test the negation
        containsFilter.setNot(true);
        predicate = getRegex(containsFilter, notRegex(expected));
        assertTrue(predicate.test(fObjectMap1));
        assertTrue(predicate.test(fObjectMap2));
        assertFalse(predicate.test(fObjectMap3));
    }

    /**
     * Test converting an equals filter to regex
     */
    @Test
    public void testInputFilterEquals() {
        ITmfEventAspect<@NonNull String> aspect = TmfBaseAspects.getEventTypeAspect();

        /* Test the greater than operator */
        String expected = "\"" + aspect.getName() + "\" " + IFilterStrings.EQUAL + " \"" + EVENT_NAME1 + "\"";
        TmfFilterEqualsNode equalsFilter = new TmfFilterEqualsNode(null);
        equalsFilter.setEventAspect(aspect);
        equalsFilter.setValue(EVENT_NAME1);

        Predicate<Multimap<String, Object>> predicate = getRegex(equalsFilter, expected);
        assertTrue(predicate.test(fObjectMap1));
        assertFalse(predicate.test(fObjectMap2));
        assertFalse(predicate.test(fObjectMap3));

        equalsFilter.setNot(true);
        expected = "\"" + aspect.getName() + "\" " + IFilterStrings.NOT_EQUAL + " \"" + EVENT_NAME1 + "\"";
        predicate = getRegex(equalsFilter, expected);
        assertFalse(predicate.test(fObjectMap1));
        assertTrue(predicate.test(fObjectMap2));
        assertTrue(predicate.test(fObjectMap3));
    }

    /**
     * Test conversion of matches filters to regex
     */
    @Test
    public void testInputFilterMatches() {
        ITmfEventAspect<@NonNull Object> aspect = TmfBaseAspects.getContentsAspect().forField(FIELD1_NAME);
        /* Test a simple match */
        String expected = "\"" + FIELD1_NAME + "\" " + IFilterStrings.MATCHES + " \".*other.*\"";

        TmfFilterMatchesNode matchesFilter = new TmfFilterMatchesNode(null);
        matchesFilter.setEventAspect(aspect);
        matchesFilter.setRegex(".*other.*");

        Predicate<Multimap<String, Object>> predicate = getRegex(matchesFilter, expected);
        assertFalse(predicate.test(fObjectMap1));
        assertFalse(predicate.test(fObjectMap2));
        assertTrue(predicate.test(fObjectMap3));

        /* Test the negation */
        matchesFilter.setNot(true);
        predicate = getRegex(matchesFilter, notRegex(expected));
        assertTrue(predicate.test(fObjectMap1));
        assertTrue(predicate.test(fObjectMap2));
        assertFalse(predicate.test(fObjectMap3));

        /* Test a wildcard regex, should match with the presence of the field */
        expected = "\"" + FIELD1_NAME + "\" " + IFilterStrings.PRESENT;
        matchesFilter.setRegex(".*");
        matchesFilter.setNot(false);
        predicate = getRegex(matchesFilter, expected);
        assertTrue(predicate.test(fObjectMap1));
        assertFalse(predicate.test(fObjectMap2));
        assertTrue(predicate.test(fObjectMap3));

        /* Test the negation of present */
        matchesFilter.setNot(true);
        predicate = getRegex(matchesFilter, notRegex(expected));
        assertFalse(predicate.test(fObjectMap1));
        assertTrue(predicate.test(fObjectMap2));
        assertFalse(predicate.test(fObjectMap3));
    }

    /**
     * Test conversion of and filters to regex
     */
    @Test
    public void testInputFilterAnd() {
        ITmfEventAspect<@NonNull Object> aspectF1 = TmfBaseAspects.getContentsAspect().forField(FIELD1_NAME);
        ITmfEventAspect<@NonNull Object> aspectF2 = TmfBaseAspects.getContentsAspect().forField(FIELD2_NAME);

        String expected = "\"" + FIELD1_NAME + "\" " + IFilterStrings.MATCHES + " \".*afield.*\" " + IFilterStrings.AND +
                " \"" + FIELD2_NAME + "\" present";
        TmfFilterMatchesNode matchesFilter1 = new TmfFilterMatchesNode(null);
        matchesFilter1.setEventAspect(aspectF1);
        matchesFilter1.setRegex(".*afield.*");

        TmfFilterMatchesNode matchesFilter2 = new TmfFilterMatchesNode(null);
        matchesFilter2.setEventAspect(aspectF2);
        matchesFilter2.setRegex(".*");

        TmfFilterAndNode andFilter = new TmfFilterAndNode(null);
        andFilter.addChild(matchesFilter1);
        andFilter.addChild(matchesFilter2);

        Predicate<Multimap<String, Object>> predicate = getRegex(andFilter, expected);
        assertFalse(predicate.test(fObjectMap1));
        assertFalse(predicate.test(fObjectMap2));
        assertTrue(predicate.test(fObjectMap3));

        /* Test the negation */
        andFilter.setNot(true);
        predicate = getRegex(andFilter, notRegex(expected));
        assertTrue(predicate.test(fObjectMap1));
        assertTrue(predicate.test(fObjectMap2));
        assertFalse(predicate.test(fObjectMap3));
    }

    /**
     * Test conversion of an or filter
     */
    @Test
    public void testInputFilterOr() {
        ITmfEventAspect<@NonNull Object> aspectF1 = TmfBaseAspects.getContentsAspect().forField(FIELD1_NAME);
        ITmfEventAspect<@NonNull Object> aspectF2 = TmfBaseAspects.getContentsAspect().forField(FIELD2_NAME);

        String expected = "\"" + FIELD1_NAME + "\" " + IFilterStrings.MATCHES + " \".*afield.*\" " + IFilterStrings.OR +
                " \"" + FIELD2_NAME + "\" " + IFilterStrings.MATCHES + " \".*2.*\"";
        TmfFilterMatchesNode matchesFilter1 = new TmfFilterMatchesNode(null);
        matchesFilter1.setEventAspect(aspectF1);
        matchesFilter1.setRegex(".*afield.*");

        TmfFilterMatchesNode matchesFilter2 = new TmfFilterMatchesNode(null);
        matchesFilter2.setEventAspect(aspectF2);
        matchesFilter2.setRegex(".*2.*");

        TmfFilterOrNode orFilter = new TmfFilterOrNode(null);
        orFilter.addChild(matchesFilter1);
        orFilter.addChild(matchesFilter2);

        Predicate<Multimap<String, Object>> predicate = getRegex(orFilter, expected);
        assertTrue(predicate.test(fObjectMap1));
        assertFalse(predicate.test(fObjectMap2));
        assertTrue(predicate.test(fObjectMap3));

        /* Test the negation */
        orFilter.setNot(true);
        predicate = getRegex(orFilter, notRegex(expected));
        assertFalse(predicate.test(fObjectMap1));
        assertTrue(predicate.test(fObjectMap2));
        assertFalse(predicate.test(fObjectMap3));
    }

}
