/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Yann N. Dauphin <dhaemon@gmail.com> - Implementation for stats
 *   Francois Godin <copelnug@gmail.com> - Re-design for new stats structure
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Re-design for new stats structure (2)
 *   Alexandre Montplaisir - Move the tree structure logic into the nodes
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.viewers.statistics.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A tree where nodes can be accessed efficiently using paths.
 *
 * It works like file systems. Each node is identified by a key. A path is an
 * array of String. The elements of the array represent the path from the root
 * to this node.
 *
 * @author Mathieu Denis
 */
public class TmfStatisticsTreeNode {

    /** Tree to which this node belongs */
    private final TmfStatisticsTree fTree;

    /** Path of this node. The last element represents its basename. */
    private final String[] fPath;

    /** Parent node */
    private final TmfStatisticsTreeNode fParent;

    /** Children of this node, indexed by their basename. */
    private final Map<String, TmfStatisticsTreeNode> fChildren;

    /** Statistics values associated to this node. */
    private final TmfStatisticsValues fValues;

    /**
     * Return the node at the top of the branch
     */
    private final TmfStatisticsTreeNode fTopNode;

    /**
     * Constructor.
     *
     * @param tree
     *            Owner tree of this node
     * @param parent
     *            Parent node of this one
     * @param path
     *            Path to the node.
     */
    public TmfStatisticsTreeNode(TmfStatisticsTree tree,
            TmfStatisticsTreeNode parent, final String... path) {
        /*
         * The path must not contain any null element, or else we won't be able
         * to walk the tree.
         */
        for (String elem : path) {
            if (elem == null) {
                throw new IllegalArgumentException();
            }
        }

        fTree = tree;
        fPath = path;
        fParent = parent;
        fChildren = new ConcurrentHashMap<>();
        fValues = new TmfStatisticsValues();

        /* calculating top node */
        TmfStatisticsTreeNode topNode = this;
        while (topNode.getParent() != null && topNode.getParent().getParent() != null) {
            topNode = topNode.getParent();
        }
        fTopNode = topNode;
    }

    /**
     * Get the name for this node. It's used as the key in the parent's node.
     *
     * @return Name of this node.
     */
    public String getName() {
        if (fPath.length == 0) {
            /* This means we are the root node, which has no path itself */
            return "root"; //$NON-NLS-1$
        }
        return fPath[fPath.length - 1];
    }

    /**
     * Test if a node contain the specified child.
     *
     * @param childName
     *            Name of the child.
     * @return true: if child with given key is present, false: if no child
     *         exists with given key name
     */
    public boolean containsChild(String childName) {
        return fChildren.containsKey(childName);
    }

    /**
     * Retrieve the given child from this node.
     *
     * @param childName
     *            The (base)name of the child you want
     * @return The child object, or null if it doesn't exist
     */
    public TmfStatisticsTreeNode getChild(String childName) {
        return fChildren.get(childName);
    }

    /**
     * Get the children of this node.
     *
     * @return Direct children of this node.
     */
    public Collection<TmfStatisticsTreeNode> getChildren() {
        return fChildren.values();
    }

    /**
     * @param childrenName
     *            the name to search for
     * @param recursive
     *            if the search should be recursive
     * @return a Collection of children node (could be empty) with the same name
     *         as childrenName
     */
    public Collection<TmfStatisticsTreeNode> findChildren(String childrenName, boolean recursive) {

        if(childrenName.equals(getName())){
            return Collections.singletonList(this);
        }

        if (fChildren.isEmpty()) {
            return Collections.emptyList();
        }

        if (!recursive) {
            if (!fChildren.containsKey(childrenName)) {
                return Collections.emptyList();
            }
            return Collections.singletonList(fChildren.get(childrenName));
        }

        List<TmfStatisticsTreeNode> returnList = new LinkedList<>();
        for (TmfStatisticsTreeNode node : fChildren.values()) {
            returnList.addAll(node.findChildren(childrenName, true));
        }
        return returnList;
    }

    /**
     * Add a child to this node.
     *
     * @param childName
     *            Name of the child to add
     * @return The newly-created child
     */
    public TmfStatisticsTreeNode addChild(String childName) {
        TmfStatisticsTreeNode child;
        String[] childPath = new String[fPath.length + 1];
        System.arraycopy(fPath, 0, childPath, 0, fPath.length);
        childPath[fPath.length] = childName;

        child = new TmfStatisticsTreeNode(this.fTree, this, childPath);
        fChildren.put(childName, child);
        return child;
    }

    /**
     * Get the number of children this node have.
     *
     * @return Number of direct children of this node.
     */
    public int getNbChildren() {
        return fChildren.size();
    }

    /**
     * Return the parent node.
     *
     * @return Parent node.
     */
    public TmfStatisticsTreeNode getParent() {
        return fParent;
    }

    /**
     * Return the top node.
     *
     * @return Top node.
     */
    public TmfStatisticsTreeNode getTop() {
        return fTopNode;
    }

    /**
     * Get the path of the node.
     *
     * @return The path of the node.
     */
    public String[] getPath() {
        return fPath;
    }

    /**
     * Get the value of this node.
     *
     * @return Value associated with this node.
     */
    public TmfStatisticsValues getValues() {
        return fValues;
    }

    /**
     * Indicate if the node have children.
     *
     * @return True if the node has children.
     */
    public boolean hasChildren() {
        return (fChildren.size() > 0);
    }

    /**
     * Start from creation time i.e. keep key and parent but new statistics and
     * no children.
     */
    public void reset() {
        fValues.resetTotalCount();
        fValues.resetPartialCount();
        fChildren.clear();
    }

    /**
     * Resets the global number of events. It doesn't remove any node and
     * doesn't modify the partial event count. Works recursively.
     */
    public void resetGlobalValue() {
        for (TmfStatisticsTreeNode child : fChildren.values()) {
            child.resetGlobalValue();
        }
        fValues.resetTotalCount();
    }

    /**
     * Resets the number of events in the time range. It doesn't remove any node
     * and doesn't modify the global event count. Works recursively.
     */
    public void resetTimeRangeValue() {
        for (TmfStatisticsTreeNode child : fChildren.values()) {
            child.resetTimeRangeValue();
        }
        fValues.resetPartialCount();
    }

    @Override
    public String toString() {
        /* Used for debugging only */
        return "Stats node, path = " + Arrays.toString(fPath) + //$NON-NLS-1$
                ", values = " + fValues.toString(); //$NON-NLS-1$
    }
}
