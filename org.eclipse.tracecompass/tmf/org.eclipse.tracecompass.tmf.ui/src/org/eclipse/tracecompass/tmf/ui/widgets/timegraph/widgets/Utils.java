/*****************************************************************************
 * Copyright (c) 2007, 2019 Intel Corporation, Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - Initial API and implementation
 *     Ruslan A. Scherbakov, Intel - Initial API and implementation
 *     Alvaro Sanchez-Leon - Udpated for TMF
 *     Patrick Tasse - Refactoring
 *     Marc-Andre Laperle - Add time zone preference
 *****************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets;

import java.util.Collections;
import java.util.Iterator;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.common.core.format.LongToPercentFormat;
import org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

import com.google.common.collect.Iterables;

/**
 * General utilities and definitions used by the time graph widget
 *
 * @author Alvaro Sanchez-Leon
 * @author Patrick Tasse
 */
public class Utils {

    private Utils() {
    }

    /**
     * Time format for dates and timestamp
     */
    public enum TimeFormat {
        /** Relative to the start of the trace */
        RELATIVE(FormatTimeUtils.TimeFormat.RELATIVE),

        /**
         * Absolute timestamp (ie, relative to the Unix epoch)
         */
        CALENDAR(FormatTimeUtils.TimeFormat.CALENDAR),

        /**
         * Timestamp displayed as a simple number
         */
        NUMBER(FormatTimeUtils.TimeFormat.NUMBER),

        /**
         * Timestamp displayed as cycles
         */
        CYCLES((FormatTimeUtils.TimeFormat.CYCLES)),

        /**
         * Timestamp displayed as percentages, where 100% maps to the long value
         * {@link LongToPercentFormat#MAX_PERCENT_VALUE}
         *
         * @since 4.0
         */
        PERCENTAGE((FormatTimeUtils.TimeFormat.PERCENTAGE));

        private final FormatTimeUtils.TimeFormat tf;
        private TimeFormat(FormatTimeUtils.TimeFormat tf) {
            this.tf = tf;
        }

        /**
         * Convert this {@link TimeFormat} to a
         * {@link org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils.TimeFormat}
         *
         * @return the converted time format
         * @since 3.3
         */
        public FormatTimeUtils.TimeFormat convert() {
            return tf;
        }

        /**
         * Convert the specified
         * {@link org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils.TimeFormat}
         * to a {@link TimeFormat}
         *
         * @param timeFormat
         *            the time format
         * @return the converted time format
         * @since 5.0
         */
        public static TimeFormat convert(FormatTimeUtils.TimeFormat timeFormat) {
            for (TimeFormat format : values()) {
                if (format.tf == timeFormat) {
                    return format;
                }
            }
            return null;
        }
    }

    /**
     * Timestamp resolution
     */
    public enum Resolution {
        /** seconds */
        SECONDS(FormatTimeUtils.Resolution.SECONDS),

        /** milliseconds */
        MILLISEC(FormatTimeUtils.Resolution.MILLISEC),

        /** microseconds */
        MICROSEC(FormatTimeUtils.Resolution.MICROSEC),

        /** nanoseconds */
        NANOSEC(FormatTimeUtils.Resolution.NANOSEC);


        private final FormatTimeUtils.Resolution res;
        private Resolution(FormatTimeUtils.Resolution res) {
            this.res = res;
        }
        /**
         * Convert the {@link Resolution} to a {@link org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils.Resolution}
         * @return the converted resolution
         * @since 3.3
         */
        public FormatTimeUtils.Resolution convert() {
            return res;
        }
    }

    /**
     * Ellipsis character, used to shorten strings that don't fit in their
     * target area.
     *
     * @since 2.1
     */
    public static final String ELLIPSIS = "…"; //$NON-NLS-1$

    static Rectangle clone(Rectangle source) {
        return new Rectangle(source.x, source.y, source.width, source.height);
    }

    /**
     * Initialize a Rectangle object to default values (all equal to 0)
     *
     * @param rect
     *            The Rectangle to initialize
     */
    public static void init(Rectangle rect) {
        rect.x = 0;
        rect.y = 0;
        rect.width = 0;
        rect.height = 0;
    }

    /**
     * Initialize a Rectangle object with all the given values
     *
     * @param rect
     *            The Rectangle object to initialize
     * @param x
     *            The X coordinate
     * @param y
     *            The Y coordinate
     * @param width
     *            The width of the rectangle
     * @param height
     *            The height of the rectangle
     */
    public static void init(Rectangle rect, int x, int y, int width, int height) {
        rect.x = x;
        rect.y = y;
        rect.width = width;
        rect.height = height;
    }

    /**
     * Initialize a Rectangle object to another existing Rectangle's values.
     *
     * @param rect
     *            The Rectangle to initialize
     * @param source
     *            The reference Rectangle to copy
     */
    public static void init(Rectangle rect, Rectangle source) {
        rect.x = source.x;
        rect.y = source.y;
        rect.width = source.width;
        rect.height = source.height;
    }

    /**
     * Reduce the size of a given rectangle by the given amounts.
     *
     * @param rect
     *            The rectangle to modify
     * @param x
     *            The reduction in width
     * @param y
     *            The reduction in height
     */
    public static void deflate(Rectangle rect, int x, int y) {
        rect.x += x;
        rect.y += y;
        rect.width -= x + x;
        rect.height -= y + y;
    }

    /**
     * Increase the size of a given rectangle by the given amounts.
     *
     * @param rect
     *            The rectangle to modify
     * @param x
     *            The augmentation in width
     * @param y
     *            The augmentation in height
     */
    public static void inflate(Rectangle rect, int x, int y) {
        rect.x -= x;
        rect.y -= y;
        rect.width += x + x;
        rect.height += y + y;
    }

    static void dispose(Color col) {
        if (null != col) {
            col.dispose();
        }
    }

    /**
     * Get the resulting color from a mix of two existing ones for a given
     * display.
     *
     * @param display
     *            The display device (which might affect the color conversion)
     * @param c1
     *            The first color
     * @param c2
     *            The second color
     * @param w1
     *            The gamma level for color 1
     * @param w2
     *            The gamma level for color 2
     * @return The resulting color
     */
    public static Color mixColors(Device display, Color c1, Color c2, int w1,
            int w2) {
        return new Color(display, (w1 * c1.getRed() + w2 * c2.getRed())
                / (w1 + w2), (w1 * c1.getGreen() + w2 * c2.getGreen())
                / (w1 + w2), (w1 * c1.getBlue() + w2 * c2.getBlue())
                / (w1 + w2));
    }

    /**
     * Get the system color with the given ID.
     *
     * @param id
     *            The color ID
     * @return The resulting color
     */
    public static Color getSysColor(int id) {
        Color col = Display.getCurrent().getSystemColor(id);
        return new Color(col.getDevice(), col.getRGB());
    }

    /**
     * Get the resulting color from a mix of two existing ones for the current
     * display.
     *
     * @param col1
     *            The first color
     * @param col2
     *            The second color
     * @param w1
     *            The gamma level for color 1
     * @param w2
     *            The gamma level for color 2
     * @return The resulting color
     */
    public static Color mixColors(Color col1, Color col2, int w1, int w2) {
        return mixColors(Display.getCurrent(), col1, col2, w1, w2);
    }

    /**
     * Get a distinct color from the specified RGB color, based on its
     * relative luminance.
     *
     * @param rgb
     *            An RGB color
     * @return The black or white system color, whichever is more distinct.
     * @since 3.0
     */
    public static Color getDistinctColor(RGB rgb) {
        /* Calculate the relative luminance of the color, high value is bright */
        final int luminanceThreshold = 160;
        /* Relative luminance (Y) coefficients as defined in ITU.R Rec. 709 */
        final double redCoefficient = 0.2126;
        final double greenCoefficient = 0.7152;
        final double blueCoefficient = 0.0722;
        int luminance = (int) (redCoefficient * rgb.red + greenCoefficient * rgb.green + blueCoefficient * rgb.blue);
        /* Use black over bright colors and white over dark colors */
        return Display.getDefault().getSystemColor(
                luminance > luminanceThreshold ? SWT.COLOR_BLACK : SWT.COLOR_WHITE);
    }

    /**
     * Draw text in a rectangle.
     *
     * @param gc
     *            The SWT GC object
     * @param text
     *            The text to draw
     * @param rect
     *            The rectangle object which is being drawn
     * @param transp
     *            If true the background will be transparent
     * @return The width of the written text
     */
    public static int drawText(GC gc, String text, Rectangle rect, boolean transp) {
        Point size = gc.stringExtent(text);
        gc.drawText(text, rect.x, rect.y, transp);
        return size.x;
    }

    /**
     * Draw text at a given location.
     *
     * @param gc
     *            The SWT GC object
     * @param text
     *            The text to draw
     * @param x
     *            The X coordinate of the starting point
     * @param y
     *            the Y coordinate of the starting point
     * @param transp
     *            If true the background will be transparent
     * @return The width of the written text
     */
    public static int drawText(GC gc, String text, int x, int y, boolean transp) {
        Point size = gc.stringExtent(text);
        gc.drawText(text, x, y, transp);
        return size.x;
    }

    /**
     * Draw text in a rectangle, trimming the text to prevent exceeding the specified width.
     *
     * @param gc
     *            The SWT GC object
     * @param text
     *            The string to be drawn
     * @param x
     *            The x coordinate of the top left corner of the rectangular area where the text is to be drawn
     * @param y
     *            The y coordinate of the top left corner of the rectangular area where the text is to be drawn
     * @param width
     *            The width of the area to be drawn
     * @param height
     *            The height of the area to be drawn
     * @param isCentered
     *            If <code>true</code> the text will be centered in the available area if space permits
     * @param isTransparent
     *            If <code>true</code> the background will be transparent, otherwise it will be opaque
     * @return The number of characters written
     * @since 2.0
     */
    public static int drawText(GC gc, String text, int x, int y, int width, int height, boolean isCentered, boolean isTransparent) {
        if (width < 1 || text.isEmpty()) {
            return 0;
        }

        String stringToDisplay;
        int len;

        boolean isCenteredWidth = isCentered;
        int realX = x;
        int realY = y;

        /* First check if the whole string fits */
        Point textExtent = gc.textExtent(text);
        if (textExtent.x <= width) {
            len = text.length();
            stringToDisplay = text;
        } else {
            /*
             * The full string doesn't fit, try to find the longest one with
             * "..." at the end that does fit.
             *
             * Iterate on the string length sizes, starting from 1 going up,
             * until we find a string that does not fit. Once we do, we keep the
             * one just before that did fit.
             */
            isCenteredWidth = false;
            int prevLen = 0;
            len = 1;
            while (len <= text.length()) {
                textExtent = gc.textExtent(text.substring(0, len) + ELLIPSIS);
                if (textExtent.x > width) {
                    /*
                     * Here is the first length that does not fit, the one from
                     * the previous iteration is the one we will use.
                     */
                    len = prevLen;
                    break;
                }
                /* This string would fit, try the next one */
                prevLen = len;
                len++;
            }
            stringToDisplay = text.substring(0, len) + ELLIPSIS;
        }

        if (len <= 0) {
            /* Nothing fits, we end up drawing nothing */
            return 0;
        }

        if (isCenteredWidth) {
            realX += (width - textExtent.x) / 2;
        }
        if (isCentered) {
            realY += (height - textExtent.y) / 2 - 1;
        }
        gc.drawText(stringToDisplay, realX, realY, isTransparent);

        return len;
    }

    /**
     * FIXME Currently does nothing.
     *
     * @param opt
     *            The option name
     * @param def
     *            The option value
     * @param min
     *            The minimal accepted value
     * @param max
     *            The maximal accepted value
     * @return The value that was read
     */
    public static int loadIntOption(String opt, int def, int min, int max) {
        return def;
    }

    /**
     * FIXME currently does nothing
     *
     * @param opt
     *            The option name
     * @param val
     *            The option value
     */
    public static void saveIntOption(String opt, int val) {
    }

    static ITimeEvent getFirstEvent(ITimeGraphEntry entry) {
        if (null == entry || ! entry.hasTimeEvents()) {
            return null;
        }
        Iterator<? extends ITimeEvent> iterator = entry.getTimeEventsIterator();
        if (iterator != null && iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }

    /**
     * Gets the {@link ITimeEvent} at the given time from the given
     * {@link ITimeGraphEntry}.
     *
     * @param entry
     *            a {@link ITimeGraphEntry}
     * @param time
     *            a timestamp
     * @param n
     *            this parameter means: <list> <li>-1: Previous Event</li> <li>
     *            0: Current Event</li> <li>
     *            1: Next Event</li> <li>2: Previous Event when located in a non
     *            Event Area </list>
     * @return a {@link ITimeEvent}, or <code>null</code>.
     */
    public static ITimeEvent findEvent(ITimeGraphEntry entry, long time, int n) {
        if (null == entry || ! entry.hasTimeEvents()) {
            return null;
        }
        Iterator<@NonNull ? extends ITimeEvent> iterator = entry.getTimeEventsIterator();
        if (iterator == null) {
            return null;
        }
        ITimeEvent nextEvent = null;
        ITimeEvent currEvent = null;
        ITimeEvent prevEvent = null;

        while (iterator.hasNext()) {
            nextEvent = iterator.next();
            long nextStartTime = nextEvent.getTime();

            if (nextStartTime > time) {
                break;
            }

            if (currEvent == null || currEvent.getTime() != nextStartTime ||
                    (nextStartTime != time && currEvent.getDuration() != nextEvent.getDuration())) {
                prevEvent = currEvent;
                currEvent = nextEvent;
            }
        }

        if (n == -1) { //previous
            if (currEvent != null && currEvent.getTime() + currEvent.getDuration() >= time) {
                return prevEvent;
            }
            return currEvent;
        } else if (n == 0) { //current
            if (currEvent != null && currEvent.getTime() + currEvent.getDuration() >= time) {
                return currEvent;
            }
            return null;
        } else if (n == 1) { //next
            if (nextEvent != null && nextEvent.getTime() > time) {
                return nextEvent;
            }
            return null;
        } else if (n == 2) { //current or previous when in empty space
            return currEvent;
        }

        return null;
    }

    /**
     * Returns the time of the next event state change starting from the given time,
     * which is the end of the current event, or the beginning of the next event, or
     * {@link Long#MAX_VALUE}.
     *
     * @param entry
     *            the entry
     * @param time
     *            the time to start from
     * @return the time of the next event state change, or {@link Long#MAX_VALUE}
     * @since 3.1
     */
    public static long nextChange(ITimeGraphEntry entry, long time) {
        if (null == entry || ! entry.hasTimeEvents()) {
            return Long.MAX_VALUE;
        }
        Iterator<@NonNull ? extends ITimeEvent> iterator = entry.getTimeEventsIterator();
        if (iterator == null) {
            return Long.MAX_VALUE;
        }
        while (iterator.hasNext()) {
            ITimeEvent event = iterator.next();
            long start = event.getTime();
            if (start > time) {
                return start;
            }
            long end = start + event.getDuration();
            if (end > time) {
                return end;
            }
        }
        return Long.MAX_VALUE;
    }

    /**
     * Returns the time of the previous event state change starting from the given
     * time, which is the start of the current event, or the end of the previous
     * event, or {@link Long#MIN_VALUE}.
     *
     * @param entry
     *            the entry
     * @param time
     *            the time to start from
     * @return the time of the previous event state change, or {@link Long#MIN_VALUE}
     * @since 3.1
     */
    public static long prevChange(ITimeGraphEntry entry, long time) {
        if (null == entry || ! entry.hasTimeEvents()) {
            return Long.MIN_VALUE;
        }
        Iterator<@NonNull ? extends ITimeEvent> iterator = entry.getTimeEventsIterator();
        if (iterator == null) {
            return Long.MIN_VALUE;
        }
        long prevEnd = Long.MIN_VALUE;
        while (iterator.hasNext()) {
            ITimeEvent event = iterator.next();
            long start = event.getTime();
            if (start >= time) {
                return prevEnd;
            }
            long end = start + event.getDuration();
            if (end >= time) {
                return start;
            }
            prevEnd = end;
        }
        return prevEnd;
    }

    /**
     * Pretty-print a method signature.
     *
     * @param origSig
     *            The original signature
     * @return The pretty signature
     */
    public static String fixMethodSignature(String origSig) {
        String sig = origSig;
        int pos = sig.indexOf('(');
        if (pos >= 0) {
            String ret = sig.substring(0, pos);
            sig = sig.substring(pos);
            sig = sig + " " + ret; //$NON-NLS-1$
        }
        return sig;
    }

    /**
     * Restore an original method signature from a pretty-printed one.
     *
     * @param ppSig
     *            The pretty-printed signature
     * @return The original method signature
     */
    public static String restoreMethodSignature(String ppSig) {
        String ret = ""; //$NON-NLS-1$
        String sig = ppSig;

        int pos = sig.indexOf('(');
        if (pos >= 0) {
            ret = sig.substring(0, pos);
            sig = sig.substring(pos + 1);
        }
        pos = sig.indexOf(')');
        if (pos >= 0) {
            sig = sig.substring(0, pos);
        }
        String args[] = sig.split(","); //$NON-NLS-1$
        StringBuffer result = new StringBuffer("("); //$NON-NLS-1$
        for (int i = 0; i < args.length; i++) {
            String arg = args[i].trim();
            if (arg.length() == 0 && args.length == 1) {
                break;
            }
            result.append(getTypeSignature(arg));
        }
        result.append(")").append(getTypeSignature(ret)); //$NON-NLS-1$
        return result.toString();
    }

    /**
     * Get the mangled type information from an array of types.
     *
     * @param typeStr
     *            The types to convert. See method implementation for what it
     *            expects.
     * @return The mangled string of types
     */
    public static String getTypeSignature(String typeStr) {
        int dim = 0;
        String type = typeStr;
        for (int j = 0; j < type.length(); j++) {
            if (type.charAt(j) == '[') {
                dim++;
            }
        }
        int pos = type.indexOf('[');
        if (pos >= 0) {
            type = type.substring(0, pos);
        }
        StringBuffer sig = new StringBuffer(""); //$NON-NLS-1$
        for (int j = 0; j < dim; j++)
         {
            sig.append("["); //$NON-NLS-1$
        }
        if (type.equals("boolean")) { //$NON-NLS-1$
            sig.append('Z');
        } else if (type.equals("byte")) { //$NON-NLS-1$
            sig.append('B');
        } else if (type.equals("char")) { //$NON-NLS-1$
            sig.append('C');
        } else if (type.equals("short")) { //$NON-NLS-1$
            sig.append('S');
        } else if (type.equals("int")) { //$NON-NLS-1$
            sig.append('I');
        } else if (type.equals("long")) { //$NON-NLS-1$
            sig.append('J');
        } else if (type.equals("float")) { //$NON-NLS-1$
            sig.append('F');
        } else if (type.equals("double")) { //$NON-NLS-1$
            sig.append('D');
        } else if (type.equals("void")) { //$NON-NLS-1$
            sig.append('V');
        }
        else {
            sig.append('L').append(type.replace('.', '/')).append(';');
        }
        return sig.toString();
    }

    /**
     * Compare two doubles together.
     *
     * @param d1
     *            First double
     * @param d2
     *            Second double
     * @return 1 if they are different, and 0 if they are *exactly* the same.
     *         Because of the way doubles are stored, it's possible for the
     *         same number obtained in two different ways to actually look
     *         different.
     */
    public static int compare(double d1, double d2) {
        if (d1 > d2) {
            return 1;
        }
        if (d1 < d2) {
            return 1;
        }
        return 0;
    }

    /**
     * Compare two character strings alphabetically. This is simply a wrapper
     * around String.compareToIgnoreCase but that will handle cases where
     * strings can be null
     *
     * @param s1
     *            The first string
     * @param s2
     *            The second string
     * @return A number below, equal, or greater than zero if the first string
     *         is smaller, equal, or bigger (alphabetically) than the second
     *         one.
     */
    public static int compare(String s1, String s2) {
        if (s1 != null && s2 != null) {
            return s1.compareToIgnoreCase(s2);
        }
        if (s1 != null) {
            return 1;
        }
        if (s2 != null) {
            return -1;
        }
        return 0;
    }

    /**
     * Calculates the square of the distance between two points.
     *
     * @param x1
     *            x-coordinate of point 1
     * @param y1
     *            y-coordinate of point 1
     * @param x2
     *            x-coordinate of point 2
     * @param y2
     *            y-coordinate of point 2
     *
     * @return the square of the distance in pixels^2
     */
    public static double distance2(int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        int d2 = dx * dx + dy * dy;
        return d2;
    }

    /**
     * Calculates the distance between a point and a line segment. If the point
     * is in the perpendicular region between the segment points, return the
     * distance from the point to its projection on the segment. Otherwise
     * return the distance from the point to its closest segment point.
     *
     * @param px
     *            x-coordinate of the point
     * @param py
     *            y-coordinate of the point
     * @param x1
     *            x-coordinate of segment point 1
     * @param y1
     *            y-coordinate of segment point 1
     * @param x2
     *            x-coordinate of segment point 2
     * @param y2
     *            y-coordinate of segment point 2
     *
     * @return the distance in pixels
     */
    public static double distance(int px, int py, int x1, int y1, int x2, int y2) {
        double length2 = distance2(x1, y1, x2, y2);
        if (length2 == 0) {
            return Math.sqrt(distance2(px, py, x1, y1));
        }
        // 'r' is the ratio of the position, between segment point 1 and segment
        // point 2, of the projection of the point on the segment
        double r = ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / length2;
        if (r <= 0.0) {
            // the projection is before segment point 1, return distance from
            // the point to segment point 1
            return Math.sqrt(distance2(px, py, x1, y1));
        }
        if (r >= 1.0) {
            // the projection is after segment point 2, return distance from
            // the point to segment point 2
            return Math.sqrt(distance2(px, py, x2, y2));
        }
        // the projection is between the segment points, return distance from
        // the point to its projection on the segment
        int x = (int) (x1 + r * (x2 - x1));
        int y = (int) (y1 + r * (y2 - y1));
        return Math.sqrt(distance2(px, py, x, y));
    }

    /**
     * Flatten a {@link TimeGraphEntry} tree for easier iteration.
     *
     * @param root
     *            root entry from which to flatten the tree.
     * @return an {@link Iterable} over the entries.
     * @since 3.3
     */
    public static @NonNull Iterable<@NonNull TimeGraphEntry> flatten(TimeGraphEntry root) {
        Iterable<Iterable<TimeGraphEntry>> transform = Iterables.transform(root.getChildren(), Utils::flatten);
        return Iterables.concat(Collections.singleton(root), Iterables.concat(transform));
    }
}
