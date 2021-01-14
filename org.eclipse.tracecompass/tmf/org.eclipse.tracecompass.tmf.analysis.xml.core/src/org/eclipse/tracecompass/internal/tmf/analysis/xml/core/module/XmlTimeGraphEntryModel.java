/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.AnalysisCompilationData;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlStateSystemPathCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenStateSystemPath;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;
import org.w3c.dom.Element;

/**
 * {@link TimeGraphEntryModel} for XML Time Graphs
 *
 * @author Loic Prieur-Drevon
 * @since 3.0
 */
public class XmlTimeGraphEntryModel extends TimeGraphEntryModel {

    private static final @NonNull String EMPTY_STRING = ""; //$NON-NLS-1$

    /**
     * Builder for the {@link XmlTimeGraphEntryModel}, encapsulates logic and fields
     * necessary to build the {@link XmlTimeGraphEntryModel}, but that we do not
     * want to share with the client
     *
     * @author Loic Prieur-Drevon
     */
    protected static class Builder implements ITimeGraphEntryModel, IXmlStateSystemContainer {

        private final long fId;
        private long fParentId;
        private @NonNull List<@NonNull String> fLabels = Collections.singletonList(EMPTY_STRING);
        private final long fStart;
        private final long fEnd;
        private final @Nullable Element fElement;
        private @NonNull String fXmlId = EMPTY_STRING;
        private @NonNull String fXmlParentId = EMPTY_STRING;
        private final @NonNull ITmfStateSystem fSs;
        private @NonNull AnalysisCompilationData fCompilationData;

        /**
         * Constructor
         *
         * @param id
         *            unique entry model id
         * @param parentId
         *            parent's unique entry model id
         * @param labels
         *            default entry name
         * @param entryStart
         *            entry start time
         * @param entryEnd
         *            entry end time
         * @param entryElement
         *            {@link Element} associated to this entry
         * @param ss
         *            {@link ITmfStateSystem} from which this entry originates
         * @param baseQuark
         *            base quark for this entry in the XML structure
         * @param data
         *            The compilation data that comes with this entry
         */
        public Builder(long id, long parentId, @NonNull List<@NonNull String> labels, long entryStart, long entryEnd,
                @Nullable Element entryElement, @NonNull ITmfStateSystem ss, int baseQuark, @NonNull AnalysisCompilationData data) {
            fId = id;
            fParentId = parentId;
            fLabels = labels;
            fStart = entryStart;
            fEnd = entryEnd;
            fElement = entryElement;
            fSs = ss;
            fCompilationData = data;

            /* Get the parent if specified */
            if (entryElement != null) {
                List<Element> elements = TmfXmlUtils.getChildElements(fElement, TmfXmlStrings.PARENT_ELEMENT);
                if (!elements.isEmpty()) {
                    fXmlParentId = getFirstValue(baseQuark, Objects.requireNonNull(elements.get(0)));
                }

                /* Get the name of this entry */
                elements = TmfXmlUtils.getChildElements(fElement, TmfXmlStrings.NAME_ELEMENT);
                if (!elements.isEmpty()) {
                    String nameFromSs = getFirstValue(baseQuark, Objects.requireNonNull(elements.get(0)));
                    if (!nameFromSs.isEmpty()) {
                        fLabels = Collections.singletonList(nameFromSs);
                    }
                }

                /* Get the id of this entry */
                elements = TmfXmlUtils.getChildElements(fElement, TmfXmlStrings.ID_ELEMENT);
                if (!elements.isEmpty()) {
                    fXmlId = getFirstValue(baseQuark, Objects.requireNonNull(elements.get(0)));
                } else {
                    fXmlId = labels.get(0);
                }
            }
        }

        /** Return the state value of the first interval with a non-null value
         * @param baseQuark */
        private @NonNull String getFirstValue(int baseQuark, @NonNull Element stateAttribute) {
            TmfXmlStateSystemPathCu valueCu = TmfXmlStateSystemPathCu.compile(fCompilationData, Collections.singletonList(stateAttribute));
            if (valueCu == null) {
                return EMPTY_STRING;
            }
            DataDrivenStateSystemPath valuePath = valueCu.generate();
            int quark = valuePath.getQuark(baseQuark, this);
            if (quark >= 0) {
                ITmfStateInterval firstInterval = StateSystemUtils.queryUntilNonNullValue(fSs, quark, getStartTime(), getEndTime());
                if (firstInterval != null) {
                    return String.valueOf(firstInterval.getValue());
                }
            }
            return EMPTY_STRING;
        }

        @Override
        public long getId() {
            return fId;
        }

        @Override
        public long getParentId() {
            return fParentId;
        }

        @Override
        public @NonNull String getName() {
            return fLabels.isEmpty() ? "" : fLabels.get(0); //$NON-NLS-1$
        }

        @Override
        public @NonNull List<@NonNull String> getLabels() {
            return fLabels;
        }

        @Override
        public long getStartTime() {
            return fStart;
        }

        @Override
        public long getEndTime() {
            return fEnd;
        }

        @Override
        public @NonNull ITmfStateSystem getStateSystem() {
            return fSs;
        }

        @Override
        public String getAttributeValue(String name) {
            return name;
        }

        /**
         * Getter for this {@link XmlTimeGraphEntryModel}'s XML ID
         *
         * @return this entry's XML ID.
         */
        public String getXmlId() {
            return fXmlId;
        }

        /**
         * Getter for this Entry's XML parent
         *
         * @return this entry's XML parent ID.
         */
        public @NonNull String getXmlParentId() {
            return fXmlParentId;
        }

        /**
         * Setter for this Entry's XML parent
         *
         * @param newParentId
         *            new parent XML ID
         */
        public void setParentId(long newParentId) {
            fParentId = newParentId;
        }

        /**
         * Generate an {@link XmlTimeGraphEntryModel} from the builder.
         *
         * @return a new {@link XmlTimeGraphEntryModel} instance.
         */
        public @NonNull XmlTimeGraphEntryModel build() {
            Element element = fElement;
            boolean showText = false;
            String path = null;
            if (element != null) {
                showText = Boolean.parseBoolean(element.getAttribute(TmfXmlStrings.DISPLAY_TEXT));
                path = element.getAttribute(TmfXmlStrings.PATH);
            }
            return new XmlTimeGraphEntryModel(fId, fParentId, fLabels, fStart, fEnd, path, fXmlId, fXmlParentId, showText);
        }

        @Override
        public @NonNull AnalysisCompilationData getAnalysisCompilationData() {
            return fCompilationData;
        }

        @Override
        public String toString() {
            return fLabels + " - " + fXmlId + " - " + fXmlParentId + " - " + fId + " - " + fParentId; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }

    }

    private final String fPath;
    private final @NonNull String fXmlId;
    private final @NonNull String fXmlParentId;
    private final boolean fShowText;

    /**
     * Method to create an {@link XmlTimeGraphEntryModel} with some default
     * arguments
     *
     * @param id
     *            unique entry model id
     * @param parentId
     *            parent's unique entry model id
     * @param name
     *            default entry name
     * @param start
     *            entry start time
     * @param end
     *            entry end time
     * @return new instance
     */
    public static XmlTimeGraphEntryModel create(long id, long parentId, @NonNull List<@NonNull String> name, long start, long end) {
        return new XmlTimeGraphEntryModel(id, parentId, name, start, end,
                null, name.get(0), EMPTY_STRING, false);
    }

    /**
     * @param id
     *            unique entry model id
     * @param parentId
     *            parent's unique entry model id
     * @param labels
     *            default entry labels
     * @param startTime
     *            entry start time
     * @param endTime
     *            entry end time
     * @param path
     *            XML element path
     * @param xmlId
     *            XML ID
     * @param xmlParentId
     *            XML parent ID
     * @param showText
     *            if the text should be shown for this entry or not.
     */
    public XmlTimeGraphEntryModel(long id, long parentId, @NonNull List<@NonNull String> labels, long startTime, long endTime,
            String path, @NonNull String xmlId, @NonNull String xmlParentId, boolean showText) {
        super(id, parentId, labels, startTime, endTime);
        fPath = path;
        fXmlId = xmlId;
        fXmlParentId = xmlParentId;
        fShowText = showText;
    }


    /**
     * Getter for this {@link XmlTimeGraphEntryModel}'s XML ID
     *
     * @return this entry's XML ID.
     */
    public @NonNull String getXmlId() {
        return fXmlId;
    }

    /**
     * Getter for this Entry's XML parent
     *
     * @return this entry's XML parent ID.
     */
    public @NonNull String getXmlParentId() {
        return fXmlParentId;
    }

    /**
     * Getter for this Entry's XML element
     * @return this entry's XML element
     */
    public @Nullable String getPath() {
        return fPath;
    }

    /**
     * If the labels should be displayed on the states
     *
     * @return to display or not to display the text on states for this entry
     */
    public boolean showText() {
        return fShowText;
    }

}
