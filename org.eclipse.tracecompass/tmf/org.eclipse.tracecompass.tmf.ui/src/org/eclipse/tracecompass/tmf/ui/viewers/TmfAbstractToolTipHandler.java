/*******************************************************************************
 * Copyright (c) 2017, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers;

import java.text.Format;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.tracecompass.common.core.format.DecimalUnitFormat;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.ITmfUIPreferences;
import org.eclipse.tracecompass.tmf.core.TmfStrings;
import org.eclipse.tracecompass.tmf.core.event.lookup.TmfCallsite;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.ui.actions.OpenSourceCodeAction;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphTooltipHandler;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.primitives.Longs;

/**
 * Abstract tool tip handler.
 *
 * @since 3.2
 * @author Loic Prieur-Drevon - extracted from {@link TimeGraphTooltipHandler}
 */
public abstract class TmfAbstractToolTipHandler {

    private static Format sNumberFormat = NumberFormat.getNumberInstance(Locale.getDefault());

    /**
     * String used for tool tip category, name or value
     *
     * @since 5.0
     */
    @NonNullByDefault
    public static class ToolTipString {

        private final String fText;
        private final String fHtmlString;

        private ToolTipString(String text, String htmlString) {
            fText = text;
            fHtmlString = htmlString;
        }

        /**
         * Returns the HTML string representation of this tool tip string.
         *
         * @return the HTML string
         */
        public String toHtmlString() {
            return fHtmlString;
        }

        /**
         * Returns the plain text representation of this tool tip string.
         *
         * @return the plain text string
         */
        @Override
        public String toString() {
            return fText;
        }

        /**
         * Creates a tool tip string from a plain text string
         *
         * @param text the plain text string
         * @return the tool tip string
         */
        public static ToolTipString fromString(String text) {
            return new ToolTipString(text, toHtmlString(text));
        }

        /**
         * Creates a tool tip string from an HTML string
         *
         * @param htmlString the HTML string
         * @return the tool tip string
         */
        public static ToolTipString fromHtml(String htmlString) {
            return new ToolTipString(toText(htmlString), htmlString);
        }

        /**
         * Creates a tool tip string from a decimal number. The HTML string mirror the string value.
         *
         * @param decimal
         *            The number to format
         * @return the tool tip string
         */
        public static ToolTipString fromDecimal(Number decimal) {
            Format format = sNumberFormat;
            if (format == null) {
                format = NumberFormat.getInstance(Locale.getDefault());
                if (format == null) {
                    format = new DecimalUnitFormat();
                }
                sNumberFormat = format;
            }
            String number = Objects.requireNonNull(format.format(decimal));
            return new ToolTipString(number, toHtmlString(number));
        }

        /**
         * Creates a tool tip string from a timestamp. The HTML string will
         * contain an hyperlink to the timestamp.
         *
         * @param text
         *            the timestamp plain text representation
         * @param timestamp
         *            the timestamp in nanoseconds
         * @return the tool tip string
         */
        public static ToolTipString fromTimestamp(String text, long timestamp) {
            return new ToolTipString(text, Objects.requireNonNull(String.format(TIME_HYPERLINK, timestamp, toHtmlString(text))));
        }

        private static String toHtmlString(String text) {
            return Objects.requireNonNull(StringEscapeUtils.escapeHtml4(text)
                    .replaceAll("[ \\t]", "&nbsp;") //$NON-NLS-1$ //$NON-NLS-2$
                    .replaceAll("\\r?\\n", "<br>")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        private static String toText(String htmlString) {
            return Objects.requireNonNull(StringEscapeUtils.unescapeHtml4(htmlString.replaceAll("\\<[^>]*>", ""))); //$NON-NLS-1$ //$NON-NLS-2$
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (obj.getClass() != getClass()) {
                return false;
            }
            ToolTipString other = (ToolTipString) obj;
            return Objects.equals(fText, other.fText) && Objects.equals(fHtmlString, other.fHtmlString);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fText, fHtmlString);
        }
    }

    private static final int MAX_SHELL_WIDTH = 750;
    private static final int MAX_SHELL_HEIGHT = 700;
    private static final int MOUSE_DEADZONE = 5;
    private static final String TIME_HYPERLINK = "<a href=time://%d>%s</a>"; //$NON-NLS-1$
    private static final String SOURCE_HYPERLINK = "<a href=" + TmfStrings.source() + "://%s>%s</a>"; //$NON-NLS-1$//$NON-NLS-2$
    private static final Pattern TIME_PATTERN = Pattern.compile("\\s*time\\:\\/\\/(\\d+).*"); //$NON-NLS-1$
    private static final Pattern SOURCE_PATTERN = Pattern.compile(TmfStrings.source().toLowerCase() +"\\:\\/\\/(.*):(\\d+).*"); //$NON-NLS-1$

    private static final ToolTipString UNCATEGORIZED = ToolTipString.fromString(""); //$NON-NLS-1$
    private static final int OFFSET = 16;
    private static Point fScrollBarSize = null;
    private Composite fTipComposite;
    private Shell fTipShell;
    private Rectangle fInitialDeadzone;
    private MouseTrackAdapter fMouseTrackAdapter;
    /** Table of tooltip string information as (category, name, value) tuples */
    private Table<ToolTipString, ToolTipString, ToolTipString> fModel = HashBasedTable.create();

    private static synchronized boolean isBrowserAvailable(Composite parent) {
        boolean isBrowserAvailable = Activator.getDefault().getPreferenceStore().getBoolean(ITmfUIPreferences.USE_BROWSER_TOOLTIPS);
        if (isBrowserAvailable) {
            try {
                getScrollbarSize(parent);

                Browser browser = new Browser(parent, SWT.NONE);
                browser.dispose();
                isBrowserAvailable = true;
            } catch (SWTError er) {
                isBrowserAvailable = false;
            }
        }
        return isBrowserAvailable;
    }

    private static synchronized Point getScrollbarSize(Composite parent) {
        if (fScrollBarSize == null) {
            // Don't move these lines below the new Browser() line
            Slider sliderV = new Slider(parent, SWT.VERTICAL);
            Slider sliderH = new Slider(parent, SWT.HORIZONTAL);
            int width = sliderV.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
            int height = sliderH.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
            Point scrollBarSize = new Point(width, height);
            sliderV.dispose();
            sliderH.dispose();
            fScrollBarSize = scrollBarSize;
        }
        return fScrollBarSize;
    }

    /**
     * Important note: this is being added to a display filter, this may leak,
     * make sure it is removed when not needed.
     */
    private final Listener fListener = this::disposeIfExited;
    private final Listener fFocusLostListener = event -> {
        Shell tipShell = fTipShell;
        // Don't dispose if the tooltip is clicked.
        if (tipShell != null && event.display.getActiveShell() != tipShell) {
            tipShell.dispose();
        }
    };

    /**
     * Dispose the shell if we exit the range.
     *
     * @param e
     *            The event which occurred
     */
    private void disposeIfExited(Event e) {
        if (!(e.widget instanceof Control)) {
            return;
        }
        Control control = (Control) e.widget;
        if (control != null && !control.isDisposed()) {
            Point pt = control.toDisplay(e.x, e.y);
            Shell tipShell = fTipShell;
            if (tipShell != null && !tipShell.isDisposed()) {
                Rectangle bounds = getBounds(tipShell);
                bounds.x -= OFFSET;
                bounds.y -= OFFSET;
                bounds.height += 2 * OFFSET;
                bounds.width += 2 * OFFSET;
                if (!bounds.contains(pt) && !fInitialDeadzone.contains(pt)) {
                    tipShell.dispose();
                }
            }
        }
    }

    /**
     * Callback for the mouse-over tooltip
     *
     * @param control
     *            The control object to use
     */
    public void activateHoverHelp(final Control control) {
        MouseTrackAdapter adapter = fMouseTrackAdapter;
        if (adapter == null) {
            adapter = new MouseTrackAdapter() {
                @Override
                public void mouseHover(MouseEvent event) {
                    // Is application not in focus?
                    // -OR- a mouse button is pressed
                    if (Display.getDefault().getFocusControl() == null
                            || (event.stateMask & SWT.BUTTON_MASK) != 0
                            || (event.stateMask & SWT.KEY_MASK) != 0) {
                        return;
                    }
                    Point pt = new Point(event.x, event.y);
                    Control timeGraphControl = (Control) event.widget;
                    Point ptInDisplay = control.toDisplay(event.x, event.y);
                    fInitialDeadzone = new Rectangle(ptInDisplay.x - MOUSE_DEADZONE, ptInDisplay.y - MOUSE_DEADZONE, 2 * MOUSE_DEADZONE, 2 * MOUSE_DEADZONE);
                    createTooltipShell(timeGraphControl.getShell(), control, event, pt);
                    if (fTipShell == null || fTipShell.isDisposed()) {
                        return;
                    }
                    Point tipPosition = control.toDisplay(pt);
                    setHoverLocation(fTipShell, tipPosition);
                    fTipShell.setVisible(true);
                    // Register Display filters.
                    Display display = Display.getDefault();
                    display.addFilter(SWT.MouseMove, fListener);
                    display.addFilter(SWT.FocusOut, fFocusLostListener);
                }
            };
            control.addMouseTrackListener(adapter);
            fMouseTrackAdapter = adapter;
        }
    }

    /**
     * Callback for the mouse-over tooltip to deactivate hoverhelp
     *
     * @param control
     *            The control object to use
     * @since 5.0
     */
    public void deactivateHoverHelp(final Control control) {
        MouseTrackAdapter adapter = fMouseTrackAdapter;
        if (adapter != null) {
            control.removeMouseTrackListener(adapter);
            fMouseTrackAdapter = null;
        }
    }

    /**
     * Create the tooltip shell.
     *
     * @param parent
     *            the parent shell
     * @param control
     *            the underlying control
     * @param event
     *            the mouse event to react to
     * @param pt
     *            the mouse hover position in the control's coordinates
     */
    private void createTooltipShell(Shell parent, Control control, MouseEvent event, Point pt) {
        final Display display = parent.getDisplay();
        if (fTipShell != null && !fTipShell.isDisposed()) {
            fTipShell.dispose();
        }
        fModel.clear();
        fTipShell = new Shell(parent, SWT.ON_TOP | SWT.TOOL | SWT.RESIZE);
        // Deregister display filters on dispose
        fTipShell.addDisposeListener(e -> e.display.removeFilter(SWT.MouseMove, fListener));
        fTipShell.addDisposeListener(e -> e.display.removeFilter(SWT.FocusOut, fFocusLostListener));
        fTipShell.addListener(SWT.Deactivate, e -> {
            if (!fTipShell.isDisposed()) {
                fTipShell.dispose();
            }
        });
        fTipShell.setLayout(new FillLayout());
        fTipShell.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));

        fTipComposite = new Composite(fTipShell, SWT.NO_FOCUS);
        fTipComposite.setLayout(new FillLayout());
        fill(control, event, pt);

        ITooltipContent content = null;
        if (isBrowserAvailable(fTipComposite)) {
            content = new BrowserContent(fTipComposite);
        } else {
            content = new DefaultContent(fTipComposite);
        }
        content.setInput(fModel);
        Point preferredSize = content.create();
        if (preferredSize == null) {
            fTipShell.dispose();
            return;
        }
        Rectangle trim = fTipShell.computeTrim(0, 0, preferredSize.x, preferredSize.y);
        fTipShell.setSize(Math.min(trim.width, MAX_SHELL_WIDTH), Math.min(trim.height, MAX_SHELL_HEIGHT));
    }

    private static void setHoverLocation(Shell shell, Point position) {
        Rectangle displayBounds = shell.getDisplay().getBounds();
        Rectangle shellBounds = getBounds(shell);
        if (position.x + shellBounds.width + OFFSET > displayBounds.width && position.x - shellBounds.width - OFFSET >= 0) {
            shellBounds.x = position.x - shellBounds.width - OFFSET;
        } else {
            shellBounds.x = Math.max(Math.min(position.x + OFFSET, displayBounds.width - shellBounds.width), 0);
        }
        if (position.y + shellBounds.height + OFFSET > displayBounds.height && position.y - shellBounds.height - OFFSET >= 0) {
            shellBounds.y = position.y - shellBounds.height - OFFSET;
        } else {
            shellBounds.y = Math.max(Math.min(position.y + OFFSET, displayBounds.height - shellBounds.height), 0);
        }
        shell.setBounds(shellBounds);
    }

    private static Rectangle getBounds(Shell shell) {
        Rectangle bounds = shell.getBounds();
        if (SWT.getVersion() < 4902 && SWT.getPlatform().equals("gtk")) { //$NON-NLS-1$
            /* Bug 319612 - [Gtk] Shell.getSize() returns wrong value when created with style SWT.RESIZE | SWT.ON_TOP */
            bounds = shell.computeTrim(bounds.x, bounds.y, bounds.width, bounds.height);
        }
        return bounds;
    }

    /**
     * Getter for the current underlying tip {@link Composite}
     *
     * @return the current underlying tip {@link Composite}
     */
    protected Composite getTipComposite() {
        return fTipComposite;
    }

    /**
     * Adds an uncategorized (name, value) tuple to the tool tip. The name and
     * value are plain text strings.
     *
     * @param name
     *            name of the line
     * @param value
     *            line value
     */
    protected void addItem(String name, String value) {
        addItem(null, ToolTipString.fromString(Objects.requireNonNull(name)), ToolTipString.fromString(Objects.requireNonNull(value)));
    }

    /**
     * Adds a (category, name, value) tuple to the tool tip. The category, name and
     * value are plain text strings.
     *
     * @param category
     *            the category of the item (used for grouping)
     * @param name
     *            name of the line
     * @param value
     *            line value
     * @since 5.0
     */
    protected void addItem(@Nullable String category, @NonNull String name, @NonNull String value) {
        if (Objects.equals(TmfStrings.source(), name)) {
            addItem(category == null ? null : ToolTipString.fromString(category), ToolTipString.fromString(name), ToolTipString.fromHtml(String.format(SOURCE_HYPERLINK, value, value)));
        } else {
            addItem(category == null ? null : ToolTipString.fromString(category), ToolTipString.fromString(name), ToolTipString.fromString(value));
        }
    }

    /**
     * Adds a (category, name, value) tuple to the tool tip.
     *
     * @param category
     *            the category of the item (used for grouping)
     * @param name
     *            name of the line
     * @param value
     *            line value
     * @since 5.0
     */
    protected void addItem(ToolTipString category, @NonNull ToolTipString name, @NonNull ToolTipString value) {
        fModel.put(category == null ? UNCATEGORIZED : category, name, value);
    }

    /**
     * Abstract method to override within implementations. Call the addItem()
     * methods to populate the tool tip.
     *
     * @param control
     *            the underlying control
     * @param event
     *            the mouse event to react to
     * @param pt
     *            the mouse hover position in the control's coordinates
     */
    protected abstract void fill(Control control, MouseEvent event, Point pt);

    private interface ITooltipContent {
        Point create();
        void setInput(Table<ToolTipString, ToolTipString, ToolTipString> model);
        Point computePreferredSize();

        default void setupControl(Control control) {
            control.setForeground(control.getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
            control.setBackground(control.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        }
    }

    private class BrowserContent extends AbstractContent {
        private static final int BODY_MARGIN = 3;
        private static final int CONTENT_MARGIN = 1;
        private static final int CELL_PADDING = 10;

        public BrowserContent(Composite parent) {
            super(parent);
        }

        @Override
        public Point create() {
            Composite parent = getParent();
            Table<ToolTipString, ToolTipString, ToolTipString> model = getModel();
            if (parent == null || model.size() == 0) {
                // avoid displaying empty tool tips.
                return null;
            }
            setupControl(parent);
            // vertical scroll is handled by the Browser
            ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL);
            scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            scrolledComposite.setExpandVertical(true);
            scrolledComposite.setExpandHorizontal(true);

            Browser browser = new Browser(scrolledComposite, SWT.NONE);
            browser.setJavascriptEnabled(false);
            browser.addLocationListener(new LocationListener() {
                @Override
                public void changing(LocationEvent ev) {
                    String locationValue = ev.location;
                    Matcher matcher = TIME_PATTERN.matcher(locationValue);
                    if (matcher.find()) {
                        String time = matcher.group(1);
                        Long val = Longs.tryParse(time);
                        if (val != null) {
                            TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(ev.getSource(), TmfTimestamp.fromNanos(val)));
                        }
                        ev.doit = false;
                    } else {
                        Matcher sMatcher = SOURCE_PATTERN.matcher(locationValue);
                        if (sMatcher.matches()) {
                            new OpenSourceCodeAction("", new TmfCallsite(sMatcher.group(1), Long.parseLong(sMatcher.group(2))), getParent().getShell()).run(); //$NON-NLS-1$
                            ev.doit = false;
                        }
                    }
                }
                @Override
                public void changed(LocationEvent ev) {
                    // Ignore
                }
            });
            setupControl(browser);

            String toolTipHtml = toHtml();
            browser.setText(toolTipHtml);
            scrolledComposite.setContent(browser);
            Point preferredSize = computePreferredSize();
            // do not set minimum height since Browser handles vertical scroll
            scrolledComposite.setMinSize(preferredSize.x, 0);
            return preferredSize;
        }

        @Override
        public Point computePreferredSize() {
            Table<ToolTipString, ToolTipString, ToolTipString> model = getModel();
            int widestCat = 0;
            int widestKey = 0;
            int widestVal = 0;
            int totalHeight = 0;
            Set<ToolTipString> rowKeySet = model.rowKeySet();
            GC gc = new GC(Display.getDefault());
            for (ToolTipString row : rowKeySet) {
                if (!row.equals(UNCATEGORIZED)) {
                    Point catExtent = gc.textExtent(row.toString());
                    widestCat = Math.max(widestCat, catExtent.x);
                    totalHeight += catExtent.y + 8;
                }
                Set<Entry<ToolTipString, ToolTipString>> entrySet = model.row(row).entrySet();
                for (Entry<ToolTipString, ToolTipString> entry : entrySet) {
                    Point keyExtent = gc.textExtent(entry.getKey().toString());
                    Point valExtent = gc.textExtent(entry.getValue().toString());
                    widestKey = Math.max(widestKey, keyExtent.x);
                    widestVal = Math.max(widestVal, valExtent.x);
                    totalHeight += Math.max(keyExtent.y, valExtent.y) + 4;
                }
            }
            gc.dispose();
            int w = Math.max(widestCat, widestKey + CELL_PADDING + widestVal) + 2 * CONTENT_MARGIN + 2 * BODY_MARGIN;
            int h = totalHeight + 2 * CONTENT_MARGIN + 2 * BODY_MARGIN;
            Point scrollBarSize = getScrollbarSize(getParent());
            return new Point(w + scrollBarSize.x, h);
        }

        @SuppressWarnings("nls")
        private String toHtml() {
            GC gc = new GC(Display.getDefault());
            FontData fontData = gc.getFont().getFontData()[0];
            String fontName = fontData.getName();
            String fontHeight = fontData.getHeight() + "pt";
            gc.dispose();
            Table<ToolTipString, ToolTipString, ToolTipString> model = getModel();
            StringBuilder toolTipContent = new StringBuilder();
            toolTipContent.append("<head>\n" +
                    "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                    "<style>\n" +
                    ".collapsible {\n" +
                    "  background-color: #777;\n" +
                    "  color: white;\n" +
//                    "  cursor: pointer;\n" + // Add when enabling JavaScript
                    "  padding: 0px;\n" +
                    "  width: 100%;\n" +
                    "  border: none;\n" +
                    "  text-align: left;\n" +
                    "  outline: none;\n" +
                    "  font-family: " + fontName +";\n" +
                    "  font-size: " + fontHeight + ";\n" +
                    "}\n" +
                    "\n" +
                    ".active, .collapsible:hover {\n" +
                    "  background-color: #555;\n" +
                    "}\n" +
                    "\n" +
                    ".content {\n" +
                    "  padding: 0px 0px;\n" +
                    "  display: block;\n" +
                    "  overflow: hidden;\n" +
                    "  background-color: #f1f1f1;\n" +
                    "}\n" +
                    ".tab {\n" +
                    "  padding:0px;\n" +
                    "  font-family: " + fontName + ";\n" +
                    "  font-size: " + fontHeight + ";\n" +
                    "}\n" +
                    ".leftPadding {\n" +
                    "  padding:0px 0px 0px " + CELL_PADDING + "px;\n" +
                    "}\n" +
                    ".bodystyle {\n" +
                    "  margin:" + BODY_MARGIN + "px;\n" +
                    "  padding:0px 0px;\n" +
                    "}\n" +
                    "</style>\n" +
                    "</head>");
            toolTipContent.append("<body class=\"bodystyle\">"); //$NON-NLS-1$

            toolTipContent.append("<div class=\"content\">");
            toolTipContent.append("<table class=\"tab\">");
            Set<ToolTipString> rowKeySet = model.rowKeySet();
            for (ToolTipString row : rowKeySet) {
                if (!row.equals(UNCATEGORIZED)) {
                    toolTipContent.append("<tr><th colspan=\"2\"><button class=\"collapsible\">").append(row.toHtmlString()).append("</button></th></tr>");
                }
                Set<Entry<ToolTipString, ToolTipString>> entrySet = model.row(row).entrySet();
                for (Entry<ToolTipString, ToolTipString> entry : entrySet) {
                    toolTipContent.append("<tr>");
                    toolTipContent.append("<td>");
                    toolTipContent.append(entry.getKey().toHtmlString());
                    toolTipContent.append("</td>");
                    toolTipContent.append("<td class=\"leftPadding\">");
                    toolTipContent.append(entry.getValue().toHtmlString());
                    toolTipContent.append("</td>");
                    toolTipContent.append("</tr>");
                }
            }
            toolTipContent.append("</table></div>");
            /* Add when enabling JavaScript
            toolTipContent.append("\n" +
                    "<script>\n" +
                    "var coll = document.getElementsByClassName(\"collapsible\");\n" +
                    "var i;\n" +
                    "\n" +
                    "for (i = 0; i < coll.length; i++) {\n" +
                    "  coll[i].addEventListener(\"click\", function() {\n" +
                    "    this.classList.toggle(\"active\");\n" +
                    "    var content = this.nextElementSibling;\n" +
                    "    if (content.style.display === \"block\") {\n" +
                    "      content.style.display = \"none\";\n" +
                    "    } else {\n" +
                    "      content.style.display = \"block\";\n" +
                    "    }\n" +
                    "  });\n" +
                    "}\n" +
                    "</script>");
            */
            toolTipContent.append("</body>"); //$NON-NLS-1$
            return toolTipContent.toString();
        }
    }

    private class DefaultContent extends AbstractContent {
        private Composite fComposite;

        public DefaultContent(Composite parent) {
            super(parent);
        }

        @Override
        public Point create() {
            Composite parent = getParent();
            Table<ToolTipString, ToolTipString, ToolTipString> model = getModel();
            if (parent == null || model.size() == 0) {
                // avoid displaying empty tool tips.
                return null;
            }
            setupControl(parent);
            ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
            scrolledComposite.setExpandVertical(true);
            scrolledComposite.setExpandHorizontal(true);
            setupControl(scrolledComposite);

            Composite composite = new Composite(scrolledComposite, SWT.NONE);
            fComposite = composite;
            composite.setLayout(new GridLayout(3, false));
            setupControl(composite);
            Set<ToolTipString> rowKeySet = model.rowKeySet();
            for (ToolTipString row : rowKeySet) {
                Set<Entry<ToolTipString, ToolTipString>> entrySet = model.row(row).entrySet();
                for (Entry<ToolTipString, ToolTipString> entry : entrySet) {
                    Label nameLabel = new Label(composite, SWT.NO_FOCUS);
                    nameLabel.setText(entry.getKey().toString());
                    setupControl(nameLabel);
                    Label separator = new Label(composite, SWT.NO_FOCUS | SWT.SEPARATOR | SWT.VERTICAL);
                    GridData gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
                    gd.heightHint = nameLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
                    separator.setLayoutData(gd);
                    setupControl(separator);
                    Label valueLabel = new Label(composite, SWT.NO_FOCUS);
                    valueLabel.setText(entry.getValue().toString());
                    setupControl(valueLabel);
                }
            }
            scrolledComposite.setContent(composite);
            Point preferredSize = computePreferredSize();
            scrolledComposite.setMinSize(preferredSize.x, preferredSize.y);
            return preferredSize;
        }

        @Override
        public Point computePreferredSize() {
            return fComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        }
    }

    private abstract class AbstractContent implements ITooltipContent {
        private Composite fParent = null;
        private Table<ToolTipString, ToolTipString, ToolTipString> fContentModel = null;

        public AbstractContent(Composite parent) {
            fParent = parent;
        }

        @Override
        public void setInput(Table<ToolTipString, ToolTipString, ToolTipString> model) {
            fContentModel = model;
        }

        @NonNull
        protected Table<ToolTipString, ToolTipString, ToolTipString> getModel() {
            Table<ToolTipString, ToolTipString, ToolTipString> model = fContentModel;
            if (model == null) {
                model = HashBasedTable.create();
            }
            return model;
        }

        protected Composite getParent() {
            return fParent;
        }
    }

}
