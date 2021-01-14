/*******************************************************************************
 * Copyright (c) 2013, 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *     Matthew Khouzam - Added timestamp string tests
 *     Patrick Tasse - Updated for fraction of second
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.event;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimePreferencesConstants;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimePreferences;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;
import org.junit.Test;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Test suite for the TmfTimestampFormat class.
 */
public class TmfTimestampFormatTest {

    private static final @NonNull String TEST_PATTERN = "HH:mm:ss.SSS";
    private static final TimeZone TEST_TIME_ZONE = TimeZone.getTimeZone(TimeZone.getAvailableIDs(0)[0]);
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final Locale US = Locale.US;

    private static final TmfTimestampFormat tsf = new TmfTimestampFormat("yyyy-MM-dd HH:mm:ss.SSSSSSSSS", GMT, US);
    private static final TmfTimestampFormat tsf1 = new TmfTimestampFormat(TEST_PATTERN);
    private static final TmfTimestampFormat tsf2 = new TmfTimestampFormat(TEST_PATTERN, TEST_TIME_ZONE);

    /**
     * Test that the default value is loaded when using the default constructor
     */
    @Test
    public void testDefaultConstructor() {
        TmfTimestampFormat ts0 = new TmfTimestampFormat();
        assertEquals("HH:mm:ss.SSS SSS SSS", ts0.toPattern());
    }

    /**
     * Test that the value constructor properly assigns the value
     */
    @Test
    public void testValueConstructor() {
        assertEquals(TEST_PATTERN, tsf1.toPattern());
    }

    /**
     * Test that the value constructor using a time zone properly assigns the
     * pattern and time zone
     */
    @Test
    public void testValueTimeZoneConstructor() {
        assertEquals(TEST_PATTERN, tsf2.toPattern());
        assertEquals(TEST_TIME_ZONE, tsf2.getTimeZone());
    }

    /**
     * Make sure that the default formats in TmfTimestampFormat get updated when
     * updateDefaultFormats is called.
     */
    @Test
    public void testUpdateDefaultFormats() {
        IEclipsePreferences node = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);

        String dateTimeTestValue = ITmfTimePreferencesConstants.TIME_HOUR_FMT + ":";
        node.put(ITmfTimePreferencesConstants.DATIME, dateTimeTestValue);

        String subSecTestValue = ITmfTimePreferencesConstants.SUBSEC_NANO_FMT + ":";
        node.put(ITmfTimePreferencesConstants.SUBSEC, subSecTestValue);
        try {
            node.flush();
        } catch (BackingStoreException e) {
        }
        TmfTimestampFormat.updateDefaultFormats();
        String expected = dateTimeTestValue + "." + subSecTestValue;
        String expected2 = "TTT." + subSecTestValue;
        assertEquals(expected, TmfTimestampFormat.getDefaulTimeFormat().toPattern());
        assertEquals(expected2, TmfTimestampFormat.getDefaulIntervalFormat().toPattern());
        // Revert preferences
        node.put(ITmfTimePreferencesConstants.DATIME, ITmfTimePreferencesConstants.TIME_HOUR_FMT);
        node.put(ITmfTimePreferencesConstants.SUBSEC, ITmfTimePreferencesConstants.SUBSEC_NANO_FMT);
        try {
            node.flush();
        } catch (BackingStoreException e) {
        }
        TmfTimestampFormat.updateDefaultFormats();
    }

    /**
     * Test that getDefaulTimeFormat returns the appropriate value (from the
     * default)
     */
    @Test
    public void testGetDefaulTimeFormat() {
        assertEquals(TmfTimestampFormat.getDefaulTimeFormat().toPattern(), TmfTimePreferences.getTimePattern());
    }

    /**
     * Test that getDefaulIntervalFormat returns the appropriate value (from the
     * default)
     */
    @Test
    public void testGetDefaulIntervalFormat() {
        assertEquals(TmfTimestampFormat.getDefaulIntervalFormat().toPattern(), TmfTimePreferences.getIntervalPattern());
    }

    /**
     * Test parsing of seconds and sub-seconds
     *
     * @throws ParseException
     *             should not happen, if it does, the test is a failure
     */
    @Test
    public void testParseSeconds() throws ParseException {
        assertEquals(7777777777123456789L, new TmfTimestampFormat("TTTTTTTTTT.SSSSSSSSS").parseValue("7777777777.123456789"));
        assertEquals(7777777777123456789L, new TmfTimestampFormat("T.SSSSSSSSS").parseValue("7777777777.123456789"));
        assertEquals(7123456789L, new TmfTimestampFormat("TTTTTTTTTT.SSSSSSSSS").parseValue("0000000007.123456789"));
        assertEquals(7123456789L, new TmfTimestampFormat("TTTTTTTTTT.SSSSSSSSS").parseValue("7.123456789"));
        assertEquals(7123456780L, new TmfTimestampFormat("TTTTTTTTTT.SSSSSSSSS").parseValue("7.12345678"));
        assertEquals(7123456700L, new TmfTimestampFormat("TTTTTTTTTT.SSSSSSSSS").parseValue("7.1234567"));
        assertEquals(7123456000L, new TmfTimestampFormat("TTTTTTTTTT.SSSSSSSSS").parseValue("7.123456"));
        assertEquals(7123450000L, new TmfTimestampFormat("TTTTTTTTTT.SSSSSSSSS").parseValue("7.12345"));
        assertEquals(7123400000L, new TmfTimestampFormat("TTTTTTTTTT.SSSSSSSSS").parseValue("7.1234"));
        assertEquals(7123000000L, new TmfTimestampFormat("TTTTTTTTTT.SSSSSSSSS").parseValue("7.123"));
        assertEquals(7120000000L, new TmfTimestampFormat("TTTTTTTTTT.SSSSSSSSS").parseValue("7.12"));
        assertEquals(7100000000L, new TmfTimestampFormat("TTTTTTTTTT.SSSSSSSSS").parseValue("7.1"));
        assertEquals(7000000000L, new TmfTimestampFormat("TTTTTTTTTT.SSSSSSSSS").parseValue("7."));
        assertEquals(7000000000L, new TmfTimestampFormat("TTTTTTTTTT.SSSSSSSSS").parseValue("7"));
        assertEquals(123456789L, new TmfTimestampFormat("TTTTTTTTTT.SSSSSSSSS").parseValue(".123456789"));
        assertEquals(123456789L, new TmfTimestampFormat(".SSSSSSSSS").parseValue(".123456789"));
        assertEquals(123456780L, new TmfTimestampFormat(".SSSSSSSS").parseValue(".123456789"));
        assertEquals(123456700L, new TmfTimestampFormat(".SSSSSSS").parseValue(".123456789"));
        assertEquals(123456000L, new TmfTimestampFormat(".SSSSSS").parseValue(".123456789"));
        assertEquals(123450000L, new TmfTimestampFormat(".SSSSS").parseValue(".123456789"));
        assertEquals(123400000L, new TmfTimestampFormat(".SSSS").parseValue(".123456789"));
        assertEquals(123000000L, new TmfTimestampFormat(".SSS").parseValue(".123456789"));
        assertEquals(120000000L, new TmfTimestampFormat(".SS").parseValue(".123456789"));
        assertEquals(100000000L, new TmfTimestampFormat(".S").parseValue(".123456789"));
        assertEquals(7123456789L, new TmfTimestampFormat("T.SSSSSSSSS").parseValue("7.123456789"));
        assertEquals(7123456789L, new TmfTimestampFormat("T.SSS SSS SSS").parseValue("7.123 456 789"));
        assertEquals(7123456789L, new TmfTimestampFormat("T.SSS SSS SSS").parseValue("7.123456789"));
        assertEquals(7123456789L, new TmfTimestampFormat("T.SSS.SSS.SSS").parseValue("7.123.456.789"));
        assertEquals(7123456789L, new TmfTimestampFormat("T.SSS.SSS.SSS").parseValue("7.123456789"));
        assertEquals(7123456789L, new TmfTimestampFormat("T.SSS,SSS,SSS").parseValue("7.123,456,789"));
        assertEquals(7123456789L, new TmfTimestampFormat("T.SSS,SSS,SSS").parseValue("7.123456789"));
        assertEquals(7123456789L, new TmfTimestampFormat("T.SSS-SSS-SSS").parseValue("7.123-456-789"));
        assertEquals(7123456789L, new TmfTimestampFormat("T.SSS-SSS-SSS").parseValue("7.123456789"));
        assertEquals(7123456789L, new TmfTimestampFormat("T.SSS_SSS_SSS").parseValue("7.123_456_789"));
        assertEquals(7123456789L, new TmfTimestampFormat("T.SSS_SSS_SSS").parseValue("7.123456789"));
        assertEquals(7123456789L, new TmfTimestampFormat("T.SSS:SSS:SSS").parseValue("7.123:456:789"));
        assertEquals(7123456789L, new TmfTimestampFormat("T.SSS:SSS:SSS").parseValue("7.123456789"));
        assertEquals(7123456789L, new TmfTimestampFormat("T.SSS;SSS;SSS").parseValue("7.123;456;789"));
        assertEquals(7123456789L, new TmfTimestampFormat("T.SSS;SSS;SSS").parseValue("7.123456789"));
        assertEquals(7123456789L, new TmfTimestampFormat("T.SSS/SSS/SSS").parseValue("7.123/456/789"));
        assertEquals(7123456789L, new TmfTimestampFormat("T.SSS/SSS/SSS").parseValue("7.123456789"));
        assertEquals(7123456789L, new TmfTimestampFormat("T.SSS''SSS''SSS").parseValue("7.123'456'789"));
        assertEquals(7123456789L, new TmfTimestampFormat("T.SSS''SSS''SSS").parseValue("7.123456789"));
        assertEquals(7123456789L, new TmfTimestampFormat("T.SSS\"SSS\"SSS").parseValue("7.123\"456\"789"));
        assertEquals(7123456789L, new TmfTimestampFormat("T.SSS\"SSS\"SSS").parseValue("7.123456789"));
        assertEquals(7000000000L, new TmfTimestampFormat("T. SSSSSSSSS").parseValue("7..123456789"));
        assertEquals(7100000000L, new TmfTimestampFormat("T.S SSSSSSSS").parseValue("7.1,23456789"));
        assertEquals(7120000000L, new TmfTimestampFormat("T.SS SSSSSSS").parseValue("7.12-3456789"));
        assertEquals(7123000000L, new TmfTimestampFormat("T.SSS SSSSSS").parseValue("7.123_456789"));
        assertEquals(7123400000L, new TmfTimestampFormat("T.SSSS SSSSS").parseValue("7.1234:56789"));
        assertEquals(7123450000L, new TmfTimestampFormat("T.SSSSS SSSS").parseValue("7.12345;6789"));
        assertEquals(7123456000L, new TmfTimestampFormat("T.SSSSSS SSS").parseValue("7.123456/789"));
        assertEquals(7123456700L, new TmfTimestampFormat("T.SSSSSSS SS").parseValue("7.1234567'89"));
        assertEquals(7123456780L, new TmfTimestampFormat("T.SSSSSSSS S").parseValue("7.12345678\"9"));
        assertEquals(7123456789L, new TmfTimestampFormat("T 's'.SSS ms SSS us SSS ns").parseValue("7 s.123 ms 456 us 789 ns"));
        assertEquals(7123456789L, new TmfTimestampFormat("T 'S'.SSS 'MS' SSS 'US' SSS 'NS'").parseValue("7 S.123 MS 456 US 789 NS"));
        assertEquals(7123000000L, new TmfTimestampFormat("T.SSSSSSSSS").parseValue("7 s.123 ms 456 ns 789"));
        assertEquals(0L, new TmfTimestampFormat("T.").parseValue("0.123456789"));
        assertEquals(0L, new TmfTimestampFormat("T.S").parseValue("."));
        assertEquals(0L, new TmfTimestampFormat(".S").parseValue("7."));
        assertEquals(0L, new TmfTimestampFormat("T.S").parseValue("-."));
        assertEquals(0L, new TmfTimestampFormat("T.S").parseValue("-0"));
        assertEquals(-100000000L, new TmfTimestampFormat("T.S").parseValue("-0.1"));
        assertEquals(-100000000L, new TmfTimestampFormat("T.S").parseValue("-.1"));
        assertEquals(-7000000000L, new TmfTimestampFormat("T.S").parseValue("-7"));
        assertEquals(-7000000000L, new TmfTimestampFormat("T.S").parseValue("-7."));
        assertEquals(-7000000000L, new TmfTimestampFormat("T.S").parseValue("-7.0"));
        assertEquals(-7100000000L, new TmfTimestampFormat("T.S").parseValue("-7.1"));
    }

    /**
     * Test unit of seconds format
     *
     * @throws ParseException
     *             should not happen, if it does, the test is a failure
     */
    @Test
    public void testUnitOfSecondsFormat() throws ParseException {
        assertEquals("000000071.234567890", new TmfTimestampFormat("TTTTTTTTTd.SSSSSSSSS").format(7123456789L));
        assertEquals("71.234567890", new TmfTimestampFormat("Td.SSSSSSSSS").format(7123456789L));
        assertEquals("71.23456789", new TmfTimestampFormat("Td.SSSSSSSS").format(7123456789L));

        assertEquals("000000712.345678900", new TmfTimestampFormat("TTTTTTTTTc.SSSSSSSSS").format(7123456789L));
        assertEquals("712.345678900", new TmfTimestampFormat("Tc.SSSSSSSSS").format(7123456789L));
        assertEquals("712.3456789", new TmfTimestampFormat("Tc.SSSSSSS").format(7123456789L));

        assertEquals("000007123.456789000", new TmfTimestampFormat("TTTTTTTTTm.SSSSSSSSS").format(7123456789L));
        assertEquals("7123.456789000", new TmfTimestampFormat("Tm.SSSSSSSSS").format(7123456789L));
        assertEquals("7123.456789", new TmfTimestampFormat("Tm.SSSSSS").format(7123456789L));

        assertEquals("007123456.789000000", new TmfTimestampFormat("TTTTTTTTTu.SSSSSSSSS").format(7123456789L));
        assertEquals("7123456.789000000", new TmfTimestampFormat("Tu.SSSSSSSSS").format(7123456789L));
        assertEquals("7123456.789", new TmfTimestampFormat("Tu.SSS").format(7123456789L));

        assertEquals("7123456789.000000000", new TmfTimestampFormat("TTTTTTTTTn.SSSSSSSSS").format(7123456789L));
        assertEquals("7123456789.000000000", new TmfTimestampFormat("Tn.SSSSSSSSS").format(7123456789L));
        assertEquals("7123456789", new TmfTimestampFormat("Tn").format(7123456789L));

        assertEquals(7123456789L, new TmfTimestampFormat("Td.SSSSSSSS").parseValue("71.23456789"));
        assertEquals(7123456789L, new TmfTimestampFormat("Tc.SSSSSSS").parseValue("712.3456789"));
        assertEquals(7123456789L, new TmfTimestampFormat("Tm.SSSSSS").parseValue("7123.456789"));
        assertEquals(7123456789L, new TmfTimestampFormat("Tu.SSS").parseValue("7123456.789"));
        assertEquals(7123456789L, new TmfTimestampFormat("Tn").parseValue("7123456789"));

        assertEquals(-100000L, new TmfTimestampFormat("Tm.S").parseValue("-0.1"));
        assertEquals(-100000L, new TmfTimestampFormat("Tm.S").parseValue("-.1"));
        assertEquals(-7000000L, new TmfTimestampFormat("Tm.S").parseValue("-7"));
        assertEquals(-7000000L, new TmfTimestampFormat("Tm.S").parseValue("-7."));
        assertEquals(-7000000L, new TmfTimestampFormat("Tm.S").parseValue("-7.0"));
        assertEquals(-7100000L, new TmfTimestampFormat("Tm.S").parseValue("-7.1"));

        assertEquals("9223372036854775807", new TmfTimestampFormat("Tn").format(9223372036854775807L));
        assertEquals(9223372036854775807L, new TmfTimestampFormat("Tn").parseValue("9223372036854775807"));
        assertEquals("-9223372036854775808", new TmfTimestampFormat("Tn").format(-9223372036854775808L));
        assertEquals(-9223372036854775808L, new TmfTimestampFormat("Tn").parseValue("-9223372036854775808"));
    }

    /**
     * Test parsing of date and time patterns
     *
     * @throws ParseException
     *             should not happen, if it does, the test is a failure
     */
    @Test
    public void testParseDateTime() throws ParseException {
        long time;

        time = new TmfTimestampFormat("yyyy", GMT, US).parseValue("2014");
        assertEquals("2014-01-01 00:00:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat("YYYY", GMT, US).parseValue("2014");
        assertEquals("2013-12-29 00:00:00.000000000", tsf.format(time)); // 1st day of week 1

        time = new TmfTimestampFormat("MM", GMT, US).parseValue("11");
        assertEquals("1970-11-01 00:00:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat("ww", GMT, US).parseValue("01");
        assertEquals("1969-12-28 00:00:00.000000000", tsf.format(time)); // Sunday of week 1

        time = new TmfTimestampFormat("DDD", GMT, US).parseValue("100");
        assertEquals("1970-04-10 00:00:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat("F", GMT, US).parseValue("2");
        assertEquals("1970-01-11 00:00:00.000000000", tsf.format(time)); // 2nd Sunday of month

        time = new TmfTimestampFormat("EEE", GMT, US).parseValue("Mon");
        assertEquals("1970-01-05 00:00:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat("u", GMT, US).parseValue("1");
        assertEquals("1970-01-05 00:00:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat("dd", GMT, US).parseValue("22");
        assertEquals("1970-01-22 00:00:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat("HH", GMT, US).parseValue("12");
        assertEquals("1970-01-01 12:00:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat("kk", GMT, US).parseValue("24");
        assertEquals("1970-01-01 00:00:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat("KK", GMT, US).parseValue("12");
        assertEquals("1970-01-01 12:00:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat("hh", GMT, US).parseValue("12");
        assertEquals("1970-01-01 00:00:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat("mm", GMT, US).parseValue("34");
        assertEquals("1970-01-01 00:34:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat("ss", GMT, US).parseValue("56");
        assertEquals("1970-01-01 00:00:56.000000000", tsf.format(time));

        time = new TmfTimestampFormat("yyyy-MM", GMT, US).parseValue("2014-11");
        assertEquals("2014-11-01 00:00:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat("yyyy-MM-dd", GMT, US).parseValue("2014-11-22");
        assertEquals("2014-11-22 00:00:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat("yyyy-MM-dd HH", GMT, US).parseValue("2014-11-22 12");
        assertEquals("2014-11-22 12:00:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat("yyyy-MM-dd HH:mm", GMT, US).parseValue("2014-11-22 12:34");
        assertEquals("2014-11-22 12:34:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat("yyyy-MM-dd HH:mm:ss", GMT, US).parseValue("2014-11-22 12:34:56");
        assertEquals("2014-11-22 12:34:56.000000000", tsf.format(time));

        time = new TmfTimestampFormat("yyyy-MM-dd HH:mm:ss.SSS", GMT, US).parseValue("2014-11-22 12:34:56.123");
        assertEquals("2014-11-22 12:34:56.123000000", tsf.format(time));

        time = new TmfTimestampFormat("yyyy-ww", GMT, US).parseValue("2014-01");
        assertEquals("2013-12-29 00:00:00.000000000", tsf.format(time)); // Sunday of week 1

        time = new TmfTimestampFormat("yyyy-DDD", GMT, US).parseValue("2014-100");
        assertEquals("2014-04-10 00:00:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat("yyyy-MM-F", GMT, US).parseValue("2014-11-2");
        assertEquals("2014-11-09 00:00:00.000000000", tsf.format(time)); // 2nd Sunday of month

        time = new TmfTimestampFormat("yyyy-MM-EEE", GMT, US).parseValue("2014-11-Mon");
        assertEquals("2014-11-03 00:00:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat("yyyy-MM-u", GMT, US).parseValue("2014-11-1");
        assertEquals("2014-11-03 00:00:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat("yyyy MM dd HH mm ss SSS SSS SSS", GMT, US).parseValue("2014 11 22 12 34 56 123 456 789");
        assertEquals("2014-11-22 12:34:56.123456789", tsf.format(time));

        time = new TmfTimestampFormat("yyyy.MM.dd.HH.mm.ss.SSS.SSS.SSS", GMT, US).parseValue("2014.11.22.12.34.56.123.456.789");
        assertEquals("2014-11-22 12:34:56.123456789", tsf.format(time));

        time = new TmfTimestampFormat("yyyy,MM,dd,HH,mm,ss,SSS,SSS,SSS", GMT, US).parseValue("2014,11,22,12,34,56,123,456,789");
        assertEquals("2014-11-22 12:34:56.123456789", tsf.format(time));

        time = new TmfTimestampFormat("yyyy-MM-dd-HH-mm-ss-SSS-SSS-SSS", GMT, US).parseValue("2014-11-22-12-34-56-123-456-789");
        assertEquals("2014-11-22 12:34:56.123456789", tsf.format(time));

        time = new TmfTimestampFormat("yyyy_MM_dd_HH_mm_ss_SSS_SSS_SSS", GMT, US).parseValue("2014_11_22_12_34_56_123_456_789");
        assertEquals("2014-11-22 12:34:56.123456789", tsf.format(time));

        time = new TmfTimestampFormat("yyyy:MM:dd:HH:mm:ss:SSS:SSS:SSS", GMT, US).parseValue("2014:11:22:12:34:56:123:456:789");
        assertEquals("2014-11-22 12:34:56.123456789", tsf.format(time));

        time = new TmfTimestampFormat("yyyy;MM;dd;HH;mm;ss;SSS;SSS;SSS", GMT, US).parseValue("2014;11;22;12;34;56;123;456;789");
        assertEquals("2014-11-22 12:34:56.123456789", tsf.format(time));

        time = new TmfTimestampFormat("yyyy/MM/dd/HH/mm/ss/SSS/SSS/SSS", GMT, US).parseValue("2014/11/22/12/34/56/123/456/789");
        assertEquals("2014-11-22 12:34:56.123456789", tsf.format(time));

        time = new TmfTimestampFormat("yyyy''MM''dd''HH''mm''ss''SSS''SSS''SSS", GMT, US).parseValue("2014'11'22'12'34'56'123'456'789");
        assertEquals("2014-11-22 12:34:56.123456789", tsf.format(time));

        time = new TmfTimestampFormat("yyyy\"MM\"dd\"HH\"mm\"ss\"SSS\"SSS\"SSS", GMT, US).parseValue("2014\"11\"22\"12\"34\"56\"123\"456\"789");
        assertEquals("2014-11-22 12:34:56.123456789", tsf.format(time));

        time = new TmfTimestampFormat("yyyy MM dd HH mm ss SSS SSS SSS", GMT, US).parseValue("2014  11  22  12  34  56  123 456 789");
        assertEquals("2014-11-22 12:34:56.123456789", tsf.format(time));

        time = new TmfTimestampFormat("yyyy MM dd HH mm ss SSS SSS SSS", GMT, US).parseValue("2014  1  2  3  4  5 123 456 789");
        assertEquals("2014-01-02 03:04:05.123456789", tsf.format(time));

        time = new TmfTimestampFormat("yyyy MM dd HH mm ss SSS SSS SSS", GMT, US).parseValue("2014 \t 1 \t 2 \t 3 \t 4 \t 5 \t 123 456 789");
        assertEquals("2014-01-02 03:04:05.123456789", tsf.format(time));

        time = new TmfTimestampFormat("yyyy-MM-dd HH:mm:ss.SSS", GMT, US).parseValue("1970-01-01 00:00:00.000");
        assertEquals("1970-01-01 00:00:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat("yyyy-MM-dd HH:mm:ss.SSS", GMT, US).parseValue("1969-12-31 23:59:59.123");
        assertEquals("1969-12-31 23:59:59.123000000", tsf.format(time));

    }

    /**
     * Test parsing of date and time patterns with reference time
     *
     * @throws ParseException
     *             should not happen, if it does, the test is a failure
     */
    @Test
    public void testParseDateTimeWithRef() throws ParseException {
        long ref = tsf.parseValue("2014-11-22 12:34:56.123456789"); // Saturday
        long time;

        time = new TmfTimestampFormat("yyyy", GMT, US).parseValue("1970", ref);
        assertEquals("1970-01-01 00:00:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat("YYYY", GMT, US).parseValue("1970", ref);
        assertEquals("1969-12-28 00:00:00.000000000", tsf.format(time)); // 1st day of week 1

        time = new TmfTimestampFormat("MM", GMT, US).parseValue("01", ref);
        assertEquals("2014-01-01 00:00:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat("ww", GMT, US).parseValue("01", ref);
        assertEquals("2014-01-04 00:00:00.000000000", tsf.format(time)); // Saturday of week 1

        time = new TmfTimestampFormat("DDD", GMT, US).parseValue("1", ref);
        assertEquals("2014-01-01 00:00:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat("F", GMT, US).parseValue("2", ref);
        assertEquals("2014-11-08 00:00:00.000000000", tsf.format(time)); // 2nd Saturday of month

        time = new TmfTimestampFormat("EEE", GMT, US).parseValue("Mon", ref);
        assertEquals("2014-11-17 00:00:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat("u", GMT, US).parseValue("1", ref);
        assertEquals("2014-11-17 00:00:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat("dd", GMT, US).parseValue("01", ref);
        assertEquals("2014-11-01 00:00:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat("HH", GMT, US).parseValue("00", ref);
        assertEquals("2014-11-22 00:00:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat("kk", GMT, US).parseValue("24", ref);
        assertEquals("2014-11-22 00:00:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat("KK", GMT, US).parseValue("00", ref);
        assertEquals("2014-11-22 00:00:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat("hh", GMT, US).parseValue("12", ref);
        assertEquals("2014-11-22 00:00:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat("mm", GMT, US).parseValue("00", ref);
        assertEquals("2014-11-22 12:00:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat("ss", GMT, US).parseValue("00", ref);
        assertEquals("2014-11-22 12:34:00.000000000", tsf.format(time));

        time = new TmfTimestampFormat(".S", GMT, US).parseValue(".9", ref);
        assertEquals("2014-11-22 12:34:56.900000000", tsf.format(time));

        time = new TmfTimestampFormat("T.S", GMT, US).parseValue("8.9", ref);
        assertEquals("1970-01-01 00:00:08.900000000", tsf.format(time));

        time = new TmfTimestampFormat("T.S", GMT, US).parseValue(".9", ref);
        assertEquals("1970-01-01 00:00:00.900000000", tsf.format(time));
    }
}
