/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.internal.xaf.core.statemachine.builder;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;

/**
 * TODO Review this class
 *
 * @author Raphaël Beamonte
 */
public class BuilderEventInfo implements Comparable<BuilderEventInfo> {
    private String name;
    private Map<String, String> content;

    /**
     * Constructor
     *
     * @param event
     *            the event
     */
    public BuilderEventInfo(ITmfEvent event) {
        name = event.getName();
        content = new HashMap<>();
        for (ITmfEventField field : event.getContent().getFields()) {
            if(field == null || field.getName().startsWith("context.")) { //$NON-NLS-1$
                continue;
            }
            content.put(field.getName(), field.getFormattedValue());
        }
    }

    /**
     * Constructor
     *
     * @param eventName
     *            the event name
     */
    public BuilderEventInfo(String eventName) {
        this.name = eventName;
        this.content = new HashMap<>();
    }

    /**
     * Constructor
     *
     * @param eventName
     *            the event name
     * @param content
     *            the event content
     */
    public BuilderEventInfo(String eventName, Map<String, String> content) {
        this.name = eventName;
        this.content = content;
    }

    /**
     * Get the event name
     *
     * @return the event name
     */
    public String getEventName() {
        return name;
    }

    /**
     * Get the content
     *
     * @return the content
     */
    public Map<String, String> getContent() {
        return content;
    }

    /**
     * Get the content size
     *
     * @return the content size
     */
    public int getContentSize() {
        return content.size();
    }

    @Override
    public String toString() {
        String contentAsString = ""; //$NON-NLS-1$
        if (!content.isEmpty()) {
            contentAsString += "["; //$NON-NLS-1$
            boolean comma = false;
            for (Entry<String, String> entry : content.entrySet()) {
                if (comma) {
                    contentAsString += ", "; //$NON-NLS-1$
                } else {
                    comma = true;
                }
                contentAsString += String.format("%s=%s", //$NON-NLS-1$
                        entry.getKey(),
                        entry.getValue());
            }
            contentAsString += "]"; //$NON-NLS-1$
        }

        return String.format("%s%s", //$NON-NLS-1$
                name,
                contentAsString);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((content == null) ? 0 : content.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BuilderEventInfo other = (BuilderEventInfo) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (content == null) {
            if (other.content != null) {
                return false;
            }
        } else if (!content.equals(other.content)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(BuilderEventInfo other) {
        if (other == null) {
            return -1;
        }
        int cmp = name.compareTo(other.name);
        if (cmp == 0) {
            cmp = Integer.compare(content.size(), other.content.size());
            if (cmp == 0) {
                Set<String> keys = new TreeSet<>(content.keySet());
                keys.addAll(other.content.keySet());
                for (String key : keys) {
                    String localValue = content.get(key);
                    String remoteValue = other.content.get(key);
                    if (localValue == null) {
                        cmp = -1;
                    } else if (remoteValue == null) {
                        cmp = 1;
                    } else {
                        cmp = localValue.compareTo(remoteValue);
                    }
                    if (cmp != 0) {
                        break;
                    }
                }
            }
        }
        return cmp;
    }

    /**
     * Get the matching rate between this and another instance
     *
     * @param other
     *            the other instance to compare to
     * @return the matching rate
     */
    public double getMatchingRate(BuilderEventInfo other) {
        if (!name.equals(other.name)) {
            return -1;
        }

        int max = Math.max(content.size(), other.content.size());
        int discrepancy = Math.abs(content.size() - other.content.size());

        Map<String, String> shortestContent, longestContent;
        if (content.size() > other.content.size()) {
            shortestContent = other.content;
            longestContent = content;
        } else {
            shortestContent = content;
            longestContent = other.content;
        }

        for (Entry<String, String> entry : shortestContent.entrySet()) {
            String value = longestContent.get(entry.getKey());
            if (value == null || !value.equals(entry.getValue())) {
                discrepancy++;
            }
        }

        return (max - discrepancy) * 1. / max;
    }

    /**
     * Get an instance with common event info between this and another instance
     *
     * @param other
     *            the other instance
     * @return an instance with common content
     */
    public BuilderEventInfo getCommonBuilderEventInfo(BuilderEventInfo other) {
        if (!name.equals(other.name)) {
            return null;
        }

        Map<String, String> commonContent = new HashMap<>();

        Map<String, String> shortestContent, longestContent;
        if (content.size() > other.content.size()) {
            shortestContent = other.content;
            longestContent = content;
        } else {
            shortestContent = content;
            longestContent = other.content;
        }

        for (Entry<String, String> entry : shortestContent.entrySet()) {
            String value = longestContent.get(entry.getKey());
            if (value != null && value.equals(entry.getValue())) {
                commonContent.put(entry.getKey(), entry.getValue());
            }
        }

        return new BuilderEventInfo(name, commonContent);
    }

}
