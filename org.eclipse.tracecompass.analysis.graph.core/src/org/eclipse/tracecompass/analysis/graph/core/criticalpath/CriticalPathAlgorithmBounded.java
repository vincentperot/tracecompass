/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francis Giraldeau- Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.graph.core.criticalpath;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfEdge;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfGraph;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex;

/**
 * Critical path bounded algorithm: backward resolution of blockage limited to
 * the blocking window
 *
 * This algorithm is described in
 *
 * F. Giraldeau and M.Dagenais,
 * "System-level Computation of Program Execution Critical Path", not published
 * yet
 *
 * @author Francis Giraldeau
 */
public class CriticalPathAlgorithmBounded extends AbstractCriticalPathAlgorithm {

    /**
     * Constructor
     *
     * @param graph
     *            The graph on which to calculate the critical path
     */
    public CriticalPathAlgorithmBounded(TmfGraph graph) {
        super(graph);
    }

    @Override
    public TmfGraph compute(TmfVertex start, @Nullable TmfVertex end) {
        /* Create new graph for the critical path result */
        TmfGraph criticalPath = new TmfGraph();

        /* Get the main graph from which to get critical path */
        TmfGraph graph = getGraph();

        /* Calculate path starting from the object the start vertex belongs to */
        Object parent = graph.getParentOf(start);
        if (parent == null) {
            throw new NullPointerException();
        }
        criticalPath.add(parent, new TmfVertex(start));
        TmfVertex currentVertex = start;
        TmfVertex nextVertex = currentVertex.outh();

        long endTime = Long.MAX_VALUE;
        if (end != null) {
            endTime = end.getTs();
        }

        /*
         * Run through all horizontal edges from this object and resolve each
         * blocking as they come
         */
        while (nextVertex != null && (nextVertex.getTs() < endTime)) {
            TmfEdge link = currentVertex.getEdges()[TmfVertex.OUTH];
            switch (link.getType()) {
            case USER_INPUT:
            case BLOCK_DEVICE:
            case TIMER:
            case INTERRUPTED:
            case PREEMPTED:
            case RUNNING:
                /**
                 * This edge is not blocked, so nothing to resolve, just add the
                 * edge to the critical path
                 */
                /**
                 * TODO: Normally, the parent of the link's vertex to should be
                 * the object itself, verify if that is true
                 */
                Object parentTo = graph.getParentOf(link.getVertexTo());
                if (parentTo == null) {
                    throw new NullPointerException();
                }
                if (parentTo != parent) {
                    System.out.println("no, the parents of horizontal edges are not always identical... shouldn't they be?"); //$NON-NLS-1$
                }
                criticalPath.append(parentTo, new TmfVertex(link.getVertexTo()), link.getType());
                break;
            case NETWORK:
            case BLOCKED:
                List<TmfEdge> links = resolveBlockingBounded(link, link.getVertexFrom());
                Collections.reverse(links);
                glue(criticalPath, graph, currentVertex, links);
                break;
            case EPS:
                if (link.getDuration() != 0) {
                    throw new RuntimeException("epsilon duration is not zero " + link); //$NON-NLS-1$
                }
                break;
            case DEFAULT:
                throw new RuntimeException("Illegal link type " + link.getType()); //$NON-NLS-1$
            case UNKNOWN:
            default:
                break;
            }
            currentVertex = nextVertex;
            nextVertex = currentVertex.outh();
        }
        return criticalPath;
    }

    /** Add the links to the critical path, with currentVertex to glue to */
    private void glue(TmfGraph criticalPath, TmfGraph graph, TmfVertex currentVertex, List<TmfEdge> links) {
        Object currentActor = graph.getParentOf(currentVertex);
        if (currentActor == null) {
            throw new NullPointerException();
        }
        if (links.isEmpty()) {
            /*
             * The next vertex should not be null, since we glue only after
             * resolve of the blocking of the edge to that vertex
             */
            TmfVertex next = currentVertex.neighbor(TmfVertex.OUTH);
            if (next == null) {
                return;
            }
            criticalPath.append(currentActor, new TmfVertex(next), currentVertex.getEdges()[TmfVertex.OUTH].getType());
            return;
        }
        // FIXME: assert last link.to actor == currentActor

        // attach subpath to b1 and b2
        TmfVertex b1 = criticalPath.getTail(currentActor);
        if (b1 == null) {
            throw new NullPointerException();
        }
        // TmfVertex b2 = new TmfVertex(curr.neighbor(TmfVertex.OUTH));

        // glue head
        TmfEdge lnk = links.get(0);
        TmfVertex anchor = null;
        Object objSrc = graph.getParentOf(lnk.getVertexFrom());
        if (objSrc == null) {
            throw new NullPointerException();
        }
        if (objSrc == currentActor) {
            anchor = b1;
        } else {
            anchor = new TmfVertex(currentVertex);
            criticalPath.add(objSrc, anchor);
            b1.linkVertical(anchor);
            /* fill any gap with UNKNOWN */
            if (lnk.getVertexFrom().compareTo(anchor) > 0) {
                anchor = new TmfVertex(lnk.getVertexFrom());
                TmfEdge edge = criticalPath.append(objSrc, anchor);
                if (edge == null) {
                    throw new NullPointerException();
                }
                edge.setType(TmfEdge.EdgeType.UNKNOWN);
            }
        }

        // glue body
        TmfEdge prev = null;
        for (TmfEdge link : links) {
            // check connectivity
            if (prev != null && prev.getVertexTo() != link.getVertexFrom()) {
                anchor = copyLink(criticalPath, graph, anchor, prev.getVertexTo(), link.getVertexFrom(),
                        prev.getVertexTo().getTs(), TmfEdge.EdgeType.DEFAULT);
            }
            anchor = copyLink(criticalPath, graph, anchor, link.getVertexFrom(), link.getVertexTo(),
                    link.getVertexTo().getTs(), link.getType());
            prev = link;
        }
    }

    /**
     * Resolve a blocking by going through the graph vertically from the
     * blocking edge
     *
     * FIXME: build a tree with partial subpath in order to return the best
     * path, not the last one traversed
     *
     * @param blocking
     *            The blocking edge
     * @param bound
     *            The vertex that limits the boundary until which to resolve the
     *            blocking
     * @return The list of non-blocking edges
     */
    private List<TmfEdge> resolveBlockingBounded(TmfEdge blocking, TmfVertex bound) {

        LinkedList<TmfEdge> subPath = new LinkedList<>();
        TmfVertex junction = findIncoming(blocking.getVertexTo(), TmfVertex.OUTH);
        /* if wake-up source is not found, return empty list */
        if (junction == null) {
            return subPath;
        }

        TmfEdge down = junction.getEdges()[TmfVertex.INV];
        if (down == null) {
            throw new NullPointerException();
        }
        subPath.add(down);
        TmfVertex vertexFrom = down.getVertexFrom();

        TmfVertex currentBound = bound.compareTo(blocking.getVertexFrom()) < 0 ? blocking.getVertexFrom() : bound;

        Stack<TmfVertex> stack = new Stack<>();
        while (vertexFrom != null && vertexFrom.compareTo(currentBound) > 0) {
            /* shortcut for down link that goes beyond the blocking */
            TmfVertex inVertical = vertexFrom.inv();
            if (inVertical != null && inVertical.compareTo(currentBound) <= 0) {
                subPath.add(vertexFrom.getEdges()[TmfVertex.INV]);
                break;
            }

            /*
             * Add DOWN links to explore stack in case dead-end occurs
             *
             * Do not add if left is BLOCKED, because this link would be visited
             * twice
             *
             * FIXME: Better document here or see if it can be done more
             * clearly. I got a headache trying to understand this
             */
            TmfVertex inHorizontal = vertexFrom.inh();
            if (inVertical != null &&
                    (inHorizontal == null ||
                            vertexFrom.getEdges()[TmfVertex.INH].getType() != TmfEdge.EdgeType.BLOCKED ||
                    vertexFrom.getEdges()[TmfVertex.INH].getType() != TmfEdge.EdgeType.NETWORK)) {
                stack.push(vertexFrom);
            }
            if (inHorizontal != null) {
                TmfEdge link = vertexFrom.getEdges()[TmfVertex.INH];
                if (link.getType() == TmfEdge.EdgeType.BLOCKED || link.getType() == TmfEdge.EdgeType.NETWORK) {
                    subPath.addAll(resolveBlockingBounded(link, currentBound));
                } else {
                    subPath.add(link);
                }
            } else {
                if (!stack.isEmpty()) {
                    TmfVertex v = stack.pop();
                    /* rewind subpath */
                    while (!subPath.isEmpty() && subPath.getLast().getVertexFrom() != v) {
                        subPath.removeLast();
                    }
                    subPath.add(v.getEdges()[TmfVertex.INV]);
                    vertexFrom = v.neighbor(TmfVertex.INV);
                    continue;
                }
            }
            vertexFrom = inHorizontal;
        }
        return subPath;
    }

}
