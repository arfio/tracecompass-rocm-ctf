/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.presentation;

import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

/**
 * This represents the appearance of a Y series for a XY chart. It contains
 * information about the name, type (ex. Bar, Line, Scatter), the style (ex.
 * Dot, Dash, Solid) and the color of the Y Series. There is no information
 * about the data.
 *
 * NOTE: This interface is kept as an internal API, it is the equivalent of an
 * {@link OutputElementStyle} instead, where name maps to
 * {@link StyleProperties#STYLE_NAME}, type maps to
 * {@link StyleProperties#SERIES_TYPE}, style maps to
 * {@link StyleProperties#SERIES_STYLE}, color maps to
 * {@link StyleProperties#COLOR}, width maps to {@link StyleProperties#WIDTH},
 * symbolStyle maps to {@link StyleProperties#SYMBOL_TYPE}, symbolSize maps to
 * {@link StyleProperties#HEIGHT}
 *
 * @author Yonni Chen
 * @since 4.0
 */
public interface IYAppearance {

    /**
     * This contains strings defining a series type. It could be for example
     * bar, line, scatter, etc.
     *
     * @author Yonni Chen
     */
    public final class Type {

        /**
         * Line series
         */
        public static final String LINE = "line"; //$NON-NLS-1$

        /**
         * Area series
         */
        public static final String AREA = "area"; //$NON-NLS-1$

        /**
         * Scatter series
         */
        public static final String SCATTER = "scatter"; //$NON-NLS-1$

        /**
         * Bar series
         */
        public static final String BAR = "bar"; //$NON-NLS-1$

        /**
         * Constructor
         */
        private Type() {
            // do nothing
        }
    }

    /**
     * This contains strings defining a series line style. It could be for
     * example solid, dash, dot, etc.
     *
     * @author Yonni Chen
     */
    public final class Style {

        /**
         * No line
         */
        public static final String NONE = "NONE"; //$NON-NLS-1$

        /**
         * Solid line
         */
        public static final String SOLID = "SOLID"; //$NON-NLS-1$

        /**
         * Dotted line
         */
        public static final String DOT = "DOT"; //$NON-NLS-1$

        /**
         * Dashed line
         */
        public static final String DASH = "DASH"; //$NON-NLS-1$

        /**
         * Dashed Dot (-.-.-.-) line
         */
        public static final String DASHDOT = "DASHDOT"; //$NON-NLS-1$

        /**
         * Dashed Dot Dot (-..-..-..) line
         */
        public static final String DASHDOTDOT = "DASHDOTDOT"; //$NON-NLS-1$

        /**
         * Constructor
         */
        private Style() {
            // do nothing
        }
    }

    /**
     * Symbol styles, contains strings defining potential appearances of a dot
     * in a chart.
     *
     * @author Matthew Khouzam
     * @since 4.1
     */
    public final class SymbolStyle {

        /** No tick */
        public static final String NONE = "NONE"; //$NON-NLS-1$

        /** Diamond tick */
        public static final String DIAMOND = "DIAMOND"; //$NON-NLS-1$

        /** Circle tick */
        public static final String CIRCLE = "CIRCLE"; //$NON-NLS-1$

        /** Square tick */
        public static final String SQUARE = "SQUARE"; //$NON-NLS-1$

        /** triangle tick */
        public static final String TRIANGLE = "TRIANGLE"; //$NON-NLS-1$

        /** inverted triangle */
        public static final String INVERTED_TRIANGLE = "INVERTED_TRIANGLE"; //$NON-NLS-1$

        /** st andrews cross, like an X */
        public static final String CROSS = "CROSS"; //$NON-NLS-1$

        /** Plus tick */
        public static final String PLUS = "PLUS"; //$NON-NLS-1$

        /** Constructor */
        private SymbolStyle() {
            // do nothing
        }
    }

    /**
     * Gets the Y series name
     *
     * @return the name of the Y serie
     */
    String getName();

    /**
     * Gets the Y series line style. Serie style can be DOT, DOTDOT, DASH, etc.
     *
     * @return the style of the Y serie
     */
    String getStyle();

    /**
     * Gets the Y serie color
     *
     * @return the color of the Y serie
     */
    RGBAColor getColor();

    /**
     * Gets Y serie type. Serie type define the type of chart : Bar, Line,
     * Scatter, etc.
     *
     * @return the type of the Y serie
     */
    String getType();

    /**
     * Gets Y serie width.
     *
     * @return the width of the Y serie
     */
    int getWidth();

    /**
     * Symbol Style. Used for ticks. Styles type define the type of ticks for
     * actual points. Ticks can be:
     * <ul>
     * <li>{@link SymbolStyle#CIRCLE}</li>
     * <li>{@link SymbolStyle#CROSS}</li>
     * <li>{@link SymbolStyle#DIAMOND}</li>
     * <li>{@link SymbolStyle#INVERTED_TRIANGLE}</li>
     * <li>{@link SymbolStyle#TRIANGLE}</li>
     * <li>{@link SymbolStyle#PLUS}</li>
     * <li>{@link SymbolStyle#SQUARE}</li>
     * </ul>
     *
     * If the tick is {@link SymbolStyle#NONE}, no tick shall be displayed.
     *
     * @return the appearance
     * @since 4.1
     */
    default String getSymbolStyle() {
        return SymbolStyle.NONE;
    }

    /**
     * Get the symbol size
     *
     * @return the size of the symbol in pixels
     * @since 4.1
     */
    default int getSymbolSize() {
        return 3;
    }

    /**
     * Convert this IYAppearance to an OutputElementStyle
     *
     * @return The OutputElementStyle from this style
     * @since 6.0
     */
    default OutputElementStyle toOutputElementStyle() {
        Builder<String, Object> builder = ImmutableMap.builder();
        builder.put(StyleProperties.STYLE_NAME, getName());
        builder.put(StyleProperties.SERIES_TYPE, getType());
        builder.put(StyleProperties.SERIES_STYLE, getStyle());
        builder.put(StyleProperties.COLOR, getColor());
        builder.put(StyleProperties.WIDTH, getWidth());
        builder.put(StyleProperties.SYMBOL_TYPE, getSymbolStyle());
        builder.put(StyleProperties.HEIGHT, getSymbolSize());
        return new OutputElementStyle(null, builder.build());
    }

}
