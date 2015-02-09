/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien and Francis Giraldeau - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.graph.core.tests.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.graph.core.base.ITmfGraphVisitor;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfEdge;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfEdge.EdgeType;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfGraph;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfGraphStatistics;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.junit.Test;

/**
 * Test the basic functionalities of the {@link TmfGraph}, {@link TmfVertex} and
 * {@link TmfEdge} classes.
 *
 * @author Geneviève Bastien
 * @author Francis Giraldeau
 */
public class TmfGraphTest {

    private static final @NonNull Object A = "A";
    private static final @NonNull Object B = "B";

    private final @NonNull TmfGraph fGraph = new TmfGraph();
    private final @NonNull TmfVertex fV0 = new TmfVertex(0);
    private final @NonNull TmfVertex fV1 = new TmfVertex(1);

    /**
     * Test the graph constructor
     */
    @Test
    public void testDefaultConstructor() {
        TmfGraph g = new TmfGraph();
        assertEquals(0, g.size());
    }

    /**
     * Test the {@link TmfGraph#add(Object, TmfVertex)} method: vertices are
     * added, but no edge between them is created
     */
    @Test
    public void testAddVertex() {
        fGraph.add(A, fV0);
        fGraph.add(A, fV1);
        List<TmfVertex> list = fGraph.getNodesOf(A);
        assertEquals(2, list.size());
        for (int i = 0; i < list.size() - 1; i++) {
            TmfVertex vertex = list.get(i);
            assertEquals(i, vertex.getTs());
            assertNull(vertex.outh());
            assertNull(vertex.inh());
            assertNull(vertex.outv());
            assertNull(vertex.inv());
        }
    }

    /**
     * Test the {@link TmfGraph#append(Object, TmfVertex)} and
     * {@link TmfGraph#append(Object, TmfVertex, EdgeType)} methods: vertices
     * are added and links are created between them.
     */
    @Test
    public void testAppendVertex() {
        /* Append without type */
        fGraph.append(A, fV0);
        TmfEdge edge = fGraph.append(A, fV1);
        assertNotNull(edge);
        assertEquals(EdgeType.DEFAULT, edge.getType());
        assertEquals(fV1, edge.getVertexTo());
        assertEquals(fV0, edge.getVertexFrom());
        assertEquals(fV1.getTs() - fV0.getTs(), edge.getDuration());

        List<TmfVertex> list = fGraph.getNodesOf(A);
        assertEquals(2, list.size());
        checkLinkHorizontal(list);
        assertEquals(fV0, fGraph.getHead(A));
        assertEquals(fV1, fGraph.getTail(A));

        /* Append with a type */
        TmfVertex v2 = new TmfVertex(2);
        edge = fGraph.append(A, v2, EdgeType.BLOCKED);
        assertNotNull(edge);
        assertEquals(EdgeType.BLOCKED, edge.getType());
        assertEquals(v2, edge.getVertexTo());
        assertEquals(fV1, edge.getVertexFrom());
        assertEquals(v2.getTs() - fV1.getTs(), edge.getDuration());

        list = fGraph.getNodesOf(A);
        assertEquals(3, list.size());
        checkLinkHorizontal(list);
        assertEquals(fV0, fGraph.getHead(A));
        assertEquals(v2, fGraph.getTail(A));
    }

    /**
     * Test that appending vertices in non chronological order gives error
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIllegalVertex() {
        fGraph.append(A, fV1);
        fGraph.append(A, fV0);
    }

    /**
     * Test the {@link TmfGraph#replace(Object, TmfVertex)} method
     */
    @Test
    public void testReplaceVertex() {
        assertEquals(0, fGraph.getNodesOf(A).size());
        fGraph.replace(A, fV0);
        assertEquals(1, fGraph.getNodesOf(A).size());
        assertEquals(fV0, fGraph.getHead(A));
        assertEquals(fV0, fGraph.getTail(A));
        fGraph.replace(A, fV1);
        assertEquals(1, fGraph.getNodesOf(A).size());
        assertEquals(fV1, fGraph.getHead(A));
        assertEquals(fV1, fGraph.getTail(A));
    }

    /**
     * Test the {@link TmfGraph#link(TmfVertex, TmfVertex)} and
     * {@link TmfGraph#link(TmfVertex, TmfVertex, EdgeType)} methods
     */
    @Test
    public void testLink() {
        // Start with a first node
        fGraph.add(A, fV0);

        // Link with second node not in graph
        TmfEdge edge = fGraph.link(fV0, fV1);
        assertEquals(fV1, edge.getVertexTo());
        assertEquals(fV0, edge.getVertexFrom());
        assertEquals(EdgeType.DEFAULT, edge.getType());
        assertEquals(fV1.getTs() - fV0.getTs(), edge.getDuration());

        List<TmfVertex> list = fGraph.getNodesOf(A);
        assertEquals(2, list.size());
        assertEquals(fV0, fV1.inh());
        assertEquals(fV1, fV0.outh());

        // Link with second node for the same object
        TmfVertex v2 = new TmfVertex(2);
        fGraph.add(A, v2);
        edge = fGraph.link(fV1, v2, EdgeType.NETWORK);
        assertEquals(v2, edge.getVertexTo());
        assertEquals(fV1, edge.getVertexFrom());
        assertEquals(EdgeType.NETWORK, edge.getType());

        list = fGraph.getNodesOf(A);
        assertEquals(3, list.size());
        assertEquals(v2, fV1.outh());
        assertEquals(fV1, v2.inh());

        // Link with second node for another object
        TmfVertex v3 = new TmfVertex(3);
        fGraph.add(B, v3);
        edge = fGraph.link(v2, v3, EdgeType.NETWORK);
        assertEquals(v3, edge.getVertexTo());
        assertEquals(v2, edge.getVertexFrom());
        assertEquals(EdgeType.NETWORK, edge.getType());

        list = fGraph.getNodesOf(B);
        assertEquals(1, list.size());

        list = fGraph.getNodesOf(A);
        assertEquals(3, list.size());
        assertEquals(v2, v3.inv());
        assertEquals(v3, v2.outv());

    }

    /**
     * Verify that vertices in the list form a chain linked by edges and have no
     * vertical edges
     */
    private static void checkLinkHorizontal(List<TmfVertex> list) {
        if (list.isEmpty()) {
            return;
        }
        for (int i = 0; i < list.size() - 1; i++) {
            TmfVertex v0 = list.get(i);
            TmfVertex v1 = list.get(i + 1);
            assertEquals(v0.outh(), v1);
            assertEquals(v1.inh(), v0);
            assertEquals(v0.getEdges()[TmfVertex.OUTH].getVertexFrom(), v0);
            assertEquals(v1.getEdges()[TmfVertex.INH].getVertexTo(), v1);
            assertNull(v1.outv());
            assertNull(v1.inv());
            assertNull(v0.outv());
            assertNull(v0.inv());
        }
    }

    /**
     * Test the {@link TmfGraph#getTail(Object)} and
     * {@link TmfGraph#removeTail(Object)} methods
     */
    @Test
    public void testTail() {
        fGraph.append(A, fV0);
        fGraph.append(A, fV1);
        assertEquals(fV1, fGraph.getTail(A));
        assertEquals(fV1, fGraph.removeTail(A));
        assertEquals(fV0, fGraph.getTail(A));
    }

    /**
     * Test the {@link TmfGraph#getHead()} methods
     */
    @Test
    public void testHead() {
        fGraph.append(A, fV0);
        fGraph.append(A, fV1);
        assertEquals(fV0, fGraph.getHead());
        assertEquals(fV0, fGraph.getHead(A));
        assertEquals(fV0, fGraph.getHead(fV1));
        assertEquals(fV0, fGraph.getHead(fV0));
    }

    /**
     * The test {@link TmfGraph#getParentOf(TmfVertex)} method
     */
    @Test
    public void testParent() {
        fGraph.append(A, fV0);
        fGraph.append(B, fV1);
        assertEquals(A, fGraph.getParentOf(fV0));
        assertNotSame(A, fGraph.getParentOf(fV1));
        assertEquals(B, fGraph.getParentOf(fV1));
    }

    /**
     * Test the {@link TmfGraph#getVertexAt(ITmfTimestamp, Object)} method
     */
    @Test
    public void testVertexAt() {
        TmfVertex[] vertices = new TmfVertex[5];
        for (int i = 0; i < 5; i++) {
            TmfVertex v = new TmfVertex((i + 1) * 5);
            vertices[i] = v;
            fGraph.append(A, v);
        }
        assertEquals(vertices[0], fGraph.getVertexAt(new TmfTimestamp(5), A));
        assertEquals(vertices[0], fGraph.getVertexAt(new TmfTimestamp(0), A));
        assertEquals(vertices[1], fGraph.getVertexAt(new TmfTimestamp(6), A));
        assertEquals(vertices[3], fGraph.getVertexAt(new TmfTimestamp(19), A));
        assertNull(fGraph.getVertexAt(new TmfTimestamp(19), B));
        assertEquals(vertices[3], fGraph.getVertexAt(new TmfTimestamp(20), A));
        assertEquals(vertices[4], fGraph.getVertexAt(new TmfTimestamp(21), A));
        assertNull(fGraph.getVertexAt(new TmfTimestamp(26), A));
    }

    /**
     * Test the {@link TmfVertex#linkHorizontal(TmfVertex)} with non
     * chronological timestamps
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCheckHorizontal() {
        TmfVertex n0 = new TmfVertex(10);
        TmfVertex n1 = new TmfVertex(0);
        n0.linkHorizontal(n1);
    }

    /**
     * Test the {@link TmfVertex#linkVertical(TmfVertex)} with non chronological
     * timestamps
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCheckVertical() {
        TmfVertex n0 = new TmfVertex(10);
        TmfVertex n1 = new TmfVertex(0);
        n0.linkVertical(n1);
    }

    private class ScanCountVertex implements ITmfGraphVisitor {
        public int nbVertex = 0;
        public int nbVLink = 0;
        public int nbHLink = 0;
        public int nbStartVertex = 0;

        @Override
        public void visitHead(TmfVertex node) {
            nbStartVertex++;
        }

        @Override
        public void visit(TmfVertex node) {
            nbVertex++;

        }

        @Override
        public void visit(TmfEdge edge, boolean horizontal) {
            if (horizontal) {
                nbHLink++;
            } else {
                nbVLink++;
            }
        }
    }

    /**
     * The following graph will be used
     *
     * <pre>
     * ____0___1___2___3___4___5___6___7___8___9___10___11___12___13___14___15
     *
     * A   *-------*       *---*-------*---*---*    *---*----*----*---------*
     *             |           |           |            |    |
     * B       *---*---*-------*   *-------*------------*    *----------*
     * </pre>
     */
    @SuppressWarnings("null")
    private static @NonNull TmfGraph buildFullGraph() {
        TmfGraph graph = new TmfGraph();
        TmfVertex[] vertexA;
        TmfVertex[] vertexB;
        long[] timesA = { 0, 2, 4, 5, 7, 8, 9, 10, 11, 12, 13, 15 };
        long[] timesB = { 1, 2, 3, 5, 6, 8, 11, 12, 14 };
        vertexA = new TmfVertex[timesA.length];
        vertexB = new TmfVertex[timesB.length];
        for (int i = 0; i < timesA.length; i++) {
            vertexA[i] = new TmfVertex(timesA[i]);
        }
        for (int i = 0; i < timesB.length; i++) {
            vertexB[i] = new TmfVertex(timesB[i]);
        }
        graph.append(A, vertexA[0]);
        graph.append(A, vertexA[1]);
        graph.add(A, vertexA[2]);
        graph.append(A, vertexA[3]);
        graph.append(A, vertexA[4]);
        graph.append(A, vertexA[5]);
        graph.append(A, vertexA[6]);
        graph.add(A, vertexA[7]);
        graph.append(A, vertexA[8]);
        graph.append(A, vertexA[9]);
        graph.append(A, vertexA[10]);
        graph.append(A, vertexA[11]);
        graph.append(B, vertexB[0]);
        graph.append(B, vertexB[1]);
        graph.append(B, vertexB[2]);
        graph.append(B, vertexB[3]);
        graph.add(B, vertexB[4]);
        graph.append(B, vertexB[5]);
        graph.append(B, vertexB[6]);
        graph.add(B, vertexB[7]);
        graph.append(B, vertexB[8]);
        vertexA[1].linkVertical(vertexB[1]);
        vertexB[3].linkVertical(vertexA[3]);
        vertexA[5].linkVertical(vertexB[5]);
        vertexB[6].linkVertical(vertexA[8]);
        vertexA[9].linkVertical(vertexB[7]);
        return graph;
    }

    /**
     * Test the {@link TmfGraph#scanLineTraverse(Object, ITmfGraphVisitor)} method
     */
    @Test
    public void testScanCount() {
        TmfGraph graph = buildFullGraph();
        ScanCountVertex visitor = new ScanCountVertex();
        graph.scanLineTraverse(graph.getHead(A), visitor);
        assertEquals(21, visitor.nbVertex);
        assertEquals(6, visitor.nbStartVertex);
        assertEquals(5, visitor.nbVLink);
        assertEquals(15, visitor.nbHLink);
    }

    /**
     * Test the {@link TmfGraphStatistics} class
     */
    @Test
    public void testGraphStatistics() {
        TmfGraph graph = buildFullGraph();
        TmfGraphStatistics stats = new TmfGraphStatistics();
        stats.getGraphStatistics(graph, A);
        assertEquals(12, stats.getSum(A).longValue());
        assertEquals(11, stats.getSum(B).longValue());
        assertEquals(23, stats.getSum().longValue());
    }

}
