/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien and Francis Giraldeau - Initial implementation and API
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.graph.core.base;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Timed vertex for TmfGraph
 *
 * @author Francis Giraldeau
 * @author Geneviève Bastien
 */
public class TmfVertex implements Comparable<TmfVertex> {

    private static long count = 0;

    /**
     * Constant for the outgoing vertical edge (to other object)
     */
    public static final int OUTGOING_VERTICAL_EDGE = 0;
    /**
     * Constant for the incoming vertical edge (from other object)
     */
    public static final int INCOMING_VERTICAL_EDGE = 1;
    /**
     * Constant for the outgoing horizontal edge (to same object)
     */
    public static final int OUTGOING_HORIZONTAL_EDGE = 2;
    /**
     * Constant for the incoming horizontal edge (from same object)
     */
    public static final int INCOMING_HORIZONTAL_EDGE = 3;

    /**
     * Compare vertices by ascending timestamps
     */
    public static Comparator<TmfVertex> ascending = new Comparator<TmfVertex>() {
        @Override
        public int compare(@Nullable TmfVertex v1, @Nullable TmfVertex v2) {
            if (v1 == null) {
                return 1;
            }
            if (v2 == null) {
                return -1;
            }
            return v1.getTs() > v2.getTs() ? 1 : (v1.getTs() == v2.getTs() ? 0 : -1);
        }
    };

    /**
     * Compare vertices by descending timestamps
     */
    public static Comparator<TmfVertex> descending = new Comparator<TmfVertex>() {
        @Override
        public int compare(@Nullable TmfVertex v1, @Nullable TmfVertex v2) {
            if (v1 == null) {
                return -1;
            }
            if (v2 == null) {
                return 1;
            }
            return v1.getTs() < v2.getTs() ? 1 : (v1.getTs() == v2.getTs() ? 0 : -1);
        }
    };

    private List<TmfEdge> fEdges;
    private final long fTimestamp;
    private final long fId;

    /**
     * Default Constructor
     */
    public TmfVertex() {
        this(0);
    }

    /**
     * Constructor with timestamp
     *
     * @param ts
     *            The vertex's timestamp
     */
    public TmfVertex(final long ts) {
        fEdges = new ArrayList<>(4);
        this.fTimestamp = ts;
        this.fId = count++;
        fEdges.add(null);
        fEdges.add(null);
        fEdges.add(null);
        fEdges.add(null);
    }

    /**
     * Copy constructor. Keeps same timestamp, but does not keep edges
     *
     * @param node
     *            vertex to copy
     */
    public TmfVertex(TmfVertex node) {
        this(node.fTimestamp);
    }

    /**
     * Copy constructor, but changes the timestamp
     *
     * @param node
     *            vertex to copy
     * @param ts
     *            The timestamp of this new node
     */
    public TmfVertex(TmfVertex node, final long ts) {
        fEdges = new ArrayList<>(4);
        this.fTimestamp = ts;
        this.fId = count++;
        fEdges.addAll(node.fEdges);
    }

    /*
     * Getters and setters
     */

    /**
     * Returns the timestamps of this node
     *
     * @return the timstamp
     */
    public long getTs() {
        return fTimestamp;
    }

    /**
     * Returns the unique ID of this node
     *
     * @return the vertex's id
     */
    public long getID() {
        return fId;
    }

    /**
     * Returns the links from this vertex
     *
     * @return the edges
     */
    public List<TmfEdge> getEdges() {
        return fEdges;
    }

    /**
     * Set OUTH and INH pointers
     *
     * @param to
     *            The vertex to link to, belongs to the same object
     *
     * @return The new edge
     */
    public TmfEdge linkHorizontal(TmfVertex to) {
        checkTimestamps(to);
        return linkHorizontalRaw(to);
    }

    private TmfEdge linkHorizontalRaw(TmfVertex node) {
        TmfEdge link = new TmfEdge(this, node);
        fEdges.set(OUTGOING_HORIZONTAL_EDGE,link);
        node.fEdges.set(INCOMING_HORIZONTAL_EDGE,link);
        return link;
    }

    /**
     * Set OUTV and INV pointers
     *
     * @param to
     *            The vertex to link to, belongs to a different object
     * @return The new edge
     */
    public TmfEdge linkVertical(TmfVertex to) {
        checkTimestamps(to);
        return linkVerticalRaw(to);
    }

    private TmfEdge linkVerticalRaw(TmfVertex to) {
        TmfEdge link = new TmfEdge(this, to);
        fEdges.set(OUTGOING_VERTICAL_EDGE, link);
        to.fEdges.set(INCOMING_VERTICAL_EDGE, link);
        return link;
    }

    private void checkTimestamps(TmfVertex to) {
        if (this.fTimestamp > to.fTimestamp) {
            throw new IllegalArgumentException(Messages.TmfVertex_ArgumentTimestampLower +
                    String.format("(curr=%d,next=%d,elapsed=%d)", fTimestamp, to.fTimestamp, to.fTimestamp - fTimestamp)); //$NON-NLS-1$
        }
    }

    /**
     * Returns the neighbor vertex at a given direction
     *
     * @param dir
     *            The neighbor direction
     * @return The neighbor vertex
     */
    public @Nullable TmfVertex neighbor(int dir) {
        switch (dir) {
        case OUTGOING_VERTICAL_EDGE:
            return outgoingVerticalEdge();
        case INCOMING_VERTICAL_EDGE:
            return incomingVerticalEdge();
        case OUTGOING_HORIZONTAL_EDGE:
            return outgoingHorizontalEdge();
        case INCOMING_HORIZONTAL_EDGE:
            return incomingHorizontalEdge();
        default:
            break;
        }
        return null;
    }

    /**
     * Returns whether the vertex has a neighbor at a given direction
     *
     * @param dir
     *            The neighbor direction
     * @return Whether this vertex has neighbor at direction
     */
    public boolean hasNeighbor(int dir) {
        return neighbor(dir) != null;
    }

    /**
     * Returns the number of non null neighbor of the current vertex
     *
     * @return The number of neighbor
     */
    public int numberOfNeighbor() {
        int i = 0;
        for (TmfEdge edge : fEdges) {
            if (edge != null) {
                i++;
            }
        }
        return i;
    }

    /**
     * Return the neighbor outgoing vertical vertex
     *
     * @return The neighbor vertex
     */
    public @Nullable TmfVertex outgoingVerticalEdge() {
        TmfEdge tmfEdge = fEdges.get(OUTGOING_VERTICAL_EDGE);
        if (tmfEdge != null) {
            return tmfEdge.getVertexTo();
        }
        return null;
    }

    /**
     * Return the neighbor incoming vertical vertex
     *
     * @return The neighbor vertex
     */
    public @Nullable TmfVertex incomingVerticalEdge() {
        TmfEdge tmfEdge = fEdges.get(INCOMING_VERTICAL_EDGE);
        if (tmfEdge != null) {
            return tmfEdge.getVertexFrom();
        }
        return null;
    }

    /**
     * Return the neighbor outgoing horizontal vertex
     *
     * @return The neighbor vertex
     */
    public @Nullable TmfVertex outgoingHorizontalEdge() {
        TmfEdge tmfEdge = fEdges.get(OUTGOING_HORIZONTAL_EDGE);
        if (tmfEdge != null) {
            return tmfEdge.getVertexTo();
        }
        return null;
    }

    /**
     * Return the neighbor incoming horizontal vertex
     *
     * @return The neighbor vertex
     */
    public @Nullable TmfVertex incomingHorizontalEdge() {
        TmfEdge tmfEdge = fEdges.get(INCOMING_HORIZONTAL_EDGE);
        if (tmfEdge != null) {
            return tmfEdge.getVertexFrom();
        }
        return null;
    }

    @Override
    public int compareTo(@Nullable TmfVertex other) {
        if (other == null) {
            return 1;
        }
        return this.fTimestamp > other.fTimestamp ? 1 : (this.fTimestamp == other.fTimestamp ? 0 : -1);
    }

    @Override
    public String toString() {
        return "[" + fId + "," + fTimestamp + "]";  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
    }

}
