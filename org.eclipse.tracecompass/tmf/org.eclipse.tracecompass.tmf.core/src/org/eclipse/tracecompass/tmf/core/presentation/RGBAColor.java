/**********************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.presentation;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents a color with its red, green and blue component. The
 * red, green and blue values must be between 0 and 255.
 *
 * @author Yonni Chen
 * @since 4.0
 */
public class RGBAColor {

    /**
     * Size of a circle in radians
     */
    public static final double TAU = Math.PI * 2;

    private static final int BYTEMASK = 0xFF;
    private final short fRed;
    private final short fGreen;
    private final short fBlue;
    private final short fAlpha;

    /**
     * Generate an RGBColor from an HSV (or HSB) value
     *
     * @param hue
     *            the hue
     * @param saturation
     *            the saturation
     * @param brightness
     *            the brightness
     * @param alpha
     *            the alpha
     * @return the color
     */
    public static final RGBAColor fromHSBA(float hue, float saturation, float brightness, float alpha) {
        if (hue < 0 || hue > TAU || saturation < 0 || saturation > 1 ||
                brightness < 0 || brightness > 1) {
            throw new IllegalArgumentException(String.format("Invalid color value (%f,%f,%f,%f)", hue, saturation, brightness, alpha)); //$NON-NLS-1$
        }
        double r, g, b;
        if (saturation == 0) {
            r = g = b = brightness;
        } else {
            double min = Math.min(TAU, hue);
            double quadHue = (min / (Math.PI / 3.0));
            int i = (int) quadHue;
            double f = quadHue - i;
            double p = brightness * (1 - saturation);
            double q = brightness * (1 - saturation * f);
            double t = brightness * (1 - saturation * (1 - f));
            switch (i) {
            case 0:
                r = brightness;
                g = t;
                b = p;
                break;
            case 1:
                r = q;
                g = brightness;
                b = p;
                break;
            case 2:
                r = p;
                g = brightness;
                b = t;
                break;
            case 3:
                r = p;
                g = q;
                b = brightness;
                break;
            case 4:
                r = t;
                g = p;
                b = brightness;
                break;
            case 5:
            default:
                r = brightness;
                g = p;
                b = q;
                break;
            }
        }
        return new RGBAColor((int) Math.round(r * 255), (int) Math.round(g * 255), (int) Math.round(b * 255), Math.round(alpha * 255));
    }

    /**
     * String parser, parses the output of {@link RGBAColor#toString()}
     *
     * @param rgbaString
     *            the color string
     * @return the RGBA or null if invalid
     * @since 4.1
     */
    public static @Nullable RGBAColor fromString(String rgbaString) {
        if (rgbaString.length() != 9 || rgbaString.charAt(0) != '#') {
            return null;
        }
        int r = toInt(rgbaString.charAt(1), rgbaString.charAt(2));
        int g = toInt(rgbaString.charAt(3), rgbaString.charAt(4));
        int b = toInt(rgbaString.charAt(5), rgbaString.charAt(6));
        int a = toInt(rgbaString.charAt(7), rgbaString.charAt(8));
        if (r == -1 || g == -1 || b == -1 || a == -1) {
            return null;
        }
        return new RGBAColor(r, g, b, a);
    }

    /**
     * String parser, parses the RGB value in format "#RRGGBB" with the
     * specified alpha value
     *
     * @param rgbString
     *            the color string
     * @param alpha
     *            the alpha
     * @return the RGBA or null if invalid
     * @since 6.0
     */
    public static @Nullable RGBAColor fromString(String rgbString, int alpha) {
        if (rgbString.length() != 7 || rgbString.charAt(0) != '#') {
            return null;
        }
        int r = toInt(rgbString.charAt(1), rgbString.charAt(2));
        int g = toInt(rgbString.charAt(3), rgbString.charAt(4));
        int b = toInt(rgbString.charAt(5), rgbString.charAt(6));
        if (r == -1 || g == -1 || b == -1) {
            return null;
        }
        return new RGBAColor(r, g, b, alpha);
    }

    /**
     * Constructor
     *
     * @param red
     *            The red component of the color
     * @param green
     *            The green component of the color
     * @param blue
     *            The blue component of the color
     * @param alpha
     *            The alpha (transparency) component of the color
     */
    public RGBAColor(int red, int green, int blue, int alpha) {
        if (red > 255 || red < 0) {
            throw new IllegalArgumentException("Red component must be between 0 and 255. Was : " + red); //$NON-NLS-1$
        }

        if (green > 255 || green < 0) {
            throw new IllegalArgumentException("Green component must be between 0 and 255. Was : " + green); //$NON-NLS-1$
        }

        if (blue > 255 || blue < 0) {
            throw new IllegalArgumentException("Blue component must be between 0 and 255. Was : " + blue); //$NON-NLS-1$
        }

        if (alpha > 255 || alpha < 0) {
            throw new IllegalArgumentException("Alpha component must be between 0 and 255. Was : " + alpha); //$NON-NLS-1$
        }

        fRed = (short) red;
        fGreen = (short) green;
        fBlue = (short) blue;
        fAlpha = (short) alpha;
    }

    /**
     * Constructor
     *
     * @param red
     *            The red component of the color
     * @param green
     *            The green component of the color
     * @param blue
     *            The blue component of the color
     */
    public RGBAColor(int red, int green, int blue) {
        this(red, green, blue, BYTEMASK);
    }

    /**
     * Constructor. This constructor extract RGB components from an integer.
     *
     * <li>0 maps to 0x00000000 or rgba(0, 0, 0, 0)</li>
     * <li>-1 maps to 0xFFFFFFFF or rgba(255, 255, 255, 255)</li> <br/>
     *
     * @param color
     *            The color as an integer
     */
    public RGBAColor(int color) {
        fRed = (short) ((color >> 24) & BYTEMASK);
        fGreen = (short) ((color >> 16) & BYTEMASK);
        fBlue = (short) ((color >> 8) & BYTEMASK);
        fAlpha = (short) (color & BYTEMASK);
    }

    /**
     * Gets the red component of the color
     *
     * @return The red component of the color
     */
    public short getRed() {
        return fRed;
    }

    /**
     * Gets the green component of the color
     *
     * @return The green component of the color
     */
    public short getGreen() {
        return fGreen;
    }

    /**
     * Gets the blue component of the color
     *
     * @return The blue component of the color
     */
    public short getBlue() {
        return fBlue;
    }

    /**
     * Gets the alpha component of the color
     *
     * @return the alpha component of the color
     */
    public short getAlpha() {
        return fAlpha;
    }

    /**
     * Get the HSBA in encoded floats
     *
     * @return a <em>float[]</em> of size 4
     *         <ol>
     *         <li>the hue in radians</li>
     *         <li>the saturation between 0 and 1</li>
     *         <li>the brightness or value between 0 and 1</li>
     *         <li>the alpha between 0 and 1</li>
     *         </ol>
     */
    public float[] getHSBA() {
        float r = fRed / 255f;
        float g = fGreen / 255f;
        float b = fBlue / 255f;
        float max = Math.max(Math.max(r, g), b);
        float min = Math.min(Math.min(r, g), b);
        float delta = max - min;
        float hue = 0;
        float brightness = max;
        float saturation = max == 0 ? 0 : (max - min) / max;
        if (delta != 0) {
            if (r == max) {
                hue = (g - b) / delta;
            } else {
                if (g == max) {
                    hue = 2 + (b - r) / delta;
                } else {
                    hue = 4 + (r - g) / delta;
                }
            }
            hue *= TAU / 6;
            if (hue < 0) {
                hue += TAU;
            }
        }
        return new float[] { hue, saturation, brightness, fAlpha / 255f };
    }

    /**
     * Get the integer (web color) representation of an RGBColor
     *
     * @return the integer representation of a color
     */
    public int toInt() {
        return (getRed() << 24) | (getGreen() << 16) | (getBlue() << 8) | getAlpha();
    }

    @Override
    public String toString() {
        return String.format("#%08X", toInt()); //$NON-NLS-1$
    }

    private static int toInt(char h, char l) {
        int high = fromDigit(h);
        if (high == -1) {
            return -1;
        }
        int low = fromDigit(l);
        if (low == -1) {
            return -1;
        }
        return 16 * high + low;
    }

    private static int fromDigit(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'A' && c <= 'F') {
            return 10 + c - 'A';
        }
        if (c >= 'a' && c <= 'f') {
            return 10 + c - 'a';
        }
        return -1;
    }

    @Override
    public boolean equals(@Nullable Object arg0) {
        if (!(arg0 instanceof RGBAColor)) {
            return false;
        }
        RGBAColor other = (RGBAColor) arg0;
        return fRed == other.fRed && fBlue == other.fBlue && fGreen == other.fGreen && fAlpha == other.fAlpha;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fRed, fGreen, fBlue, fAlpha);
    }

}
