/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.graph.core.criticalpath;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.graph.core.base.IGraphWorker;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfEdge;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfGraph;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex.EdgeDirection;
import org.eclipse.tracecompass.analysis.graph.core.criticalpath.ICriticalPathAlgorithm;

/**
 * Abstract class for critical path algorithms
 *
 * @author Francis Giraldeau
 */
public abstract class AbstractCriticalPathAlgorithm implements ICriticalPathAlgorithm {

    private final TmfGraph fGraph;

    /**
     * Constructor
     *
     * @param graph
     *            The graph on which to calculate critical path
     */
    public AbstractCriticalPathAlgorithm(TmfGraph graph) {
        fGraph = graph;
    }

    /**
     * Get the graph
     *
     * @return the graph
     */
    public TmfGraph getGraph() {
        return fGraph;
    }

    /**
     * Copy link of type TYPE between nodes FROM and TO in the graph PATH. The
     * return value is the tail node for the new path.
     *
     * @param criticalPath
     *            The graph on which to add the link
     * @param graph
     *            The original graph on which to calculate critical path
     * @param anchor
     *            The anchor vertex from the path graph
     * @param from
     *            The origin vertex in the main graph
     * @param to
     *            The destination vertex in the main graph
     * @param ts
     *            The timestamp of the edge
     * @param type
     *            The type of the edge to create
     * @param string
     *            An optional qualifier for the link
     * @return The destination vertex in the path graph
     */
    protected TmfVertex copyLink(TmfGraph criticalPath, TmfGraph graph, TmfVertex anchor, TmfVertex from, TmfVertex to, long ts, TmfEdge.EdgeType type, @Nullable String string) {
        IGraphWorker parentFrom = graph.getParentOf(from);
        IGraphWorker parentTo = graph.getParentOf(to);
        if (parentTo == null) {
            throw new NullPointerException();
        }
        TmfVertex tmp = new TmfVertex(ts);
        criticalPath.add(parentTo, tmp);
        if (parentFrom == parentTo) {
            anchor.linkHorizontal(tmp, type, string);
        } else {
            anchor.linkVertical(tmp, type, string);
        }
        return tmp;
    }

    /**
     * Find the next incoming vertex from another object (in vertical) from a
     * node in a given direction
     *
     * @param vertex
     *            The starting vertex
     * @param dir
     *            The direction in which to search
     * @return The next incoming vertex
     */
    public static @Nullable TmfVertex findIncoming(TmfVertex vertex, EdgeDirection dir) {
        TmfVertex currentVertex = vertex;
        while (true) {
            TmfEdge incoming = currentVertex.getEdge(EdgeDirection.INCOMING_VERTICAL_EDGE);
            if (incoming != null) {
                return currentVertex;
            }
            TmfEdge edge = currentVertex.getEdge(dir);
            if (edge == null || edge.getType() != TmfEdge.EdgeType.EPS) {
                break;
            }
            currentVertex = TmfVertex.getNeighborFromEdge(edge, dir);
        }
        return null;
    }

    @Override
    public String getID() {
        return getClass().getName();
    }

    @Override
    public String getDisplayName() {
        return getClass().getSimpleName();
    }

}
