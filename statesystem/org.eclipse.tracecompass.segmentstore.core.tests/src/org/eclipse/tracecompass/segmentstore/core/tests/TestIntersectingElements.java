/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   France Lapointe Nguyen - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.segmentstore.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.segmentstore.core.tests.stubs.TestInterval;
import org.eclipse.tracecompass.segmentstore.core.treemap.TreeMapStore;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;

/**
 * Unit tests for intersecting elements in a LatencyDataStore
 *
 * @author France Lapointe Nguyen
 * @since 1.0
 */
public class TestIntersectingElements {

    @Nullable TreeMapStore<TestInterval> fData = null;
    @Nullable Iterable<TestInterval> fIntersecting;

    /**
     * Initialize data (test vector) that will be tested
     */
    @Before
    public void setup() {
        // Testing with trace number "6" for now : 3 latencies, #1 starting at
        // 5 and ending at 10, #2 starting at 16 and ending at 18 and #3
        // starting at 30 and ending at 34
        TestVectorsLatency data = new TestVectorsLatency(6);
        TreeMapStore<TestInterval> latencyDataStore = new TreeMapStore<>();

        for (TestInterval interval : data.getDataList()) {
            assertNotNull(interval);
            latencyDataStore.addElement(interval);
        }

        fData = latencyDataStore;
        fIntersecting = latencyDataStore;
    }

    // ------------------------------------------------------------------------
    // Tests for one latency
    // ------------------------------------------------------------------------

    /**
     * Time before a latency start time -> Time after a latency end time
     * Expected result : 1 latency
     */
    @Test
    public void testBeforeStartAfterEnd() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(4, 11);
        assertEquals(1, Iterables.size(fIntersecting));
    }

    /**
     * Time before a latency start time -> Time before a latency end time
     * Expected result : 1 latency
     */
    @Test
    public void testBeforeStartBeforeEnd() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(4, 9);
        assertEquals(1, Iterables.size(fIntersecting));
    }

    /**
     * Time after a latency start time -> Time after a latency end time Expected
     * result : 0 latency
     */
    @Test
    public void testAfterStartAfterEnd() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(6, 11);
        assertEquals(0, Iterables.size(fIntersecting));
    }

    /**
     * Time after a latency start time -> Time before a latency end time
     * Expected result : 0 latency
     */
    @Test
    public void testAfterStartBeforeEnd() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(6, 9);
        assertEquals(0, Iterables.size(fIntersecting));
    }

    /**
     * Time equals to a latency start time -> Time equals to a latency end time
     * Expected result : 1 latency
     */
    @Test
    public void testOnStartOnEnd() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(5, 10);
        assertEquals(1, Iterables.size(fIntersecting));
    }

    /**
     * Time before a latency start time -> Time equals to a latency start time
     * Expected result : 0 latency
     */
    @Test
    public void testBeforeStartOnStart() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(2, 5);
        assertEquals(0, Iterables.size(fIntersecting));
    }

    /**
     * Time equals to a latency end time -> Time after a latency end time
     * Expected result : 0 latency
     */
    @Test
    public void testOnEndAfterEnd() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(10, 12);
        assertEquals(0, Iterables.size(fIntersecting));
    }

    /**
     * Time before a latency start time -> Time before a latency start time
     * Expected result : 0 latency
     */
    @Test
    public void testBeforeStartBeforeStart() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(1, 2);
        assertEquals(0, Iterables.size(fIntersecting));
    }

    /**
     * Time after a latency start time -> Time after a latency end time Expected
     * result : 0 latency
     */
    @Test
    public void testAfterEndAfterEnd() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(12, 14);
        assertEquals(0, Iterables.size(fIntersecting));
    }

    // ------------------------------------------------------------------------
    // Tests for 2 latencies
    // ------------------------------------------------------------------------

    /**
     * Time before 1st latency start time -> Time after 2nd latency end time
     * Expected result : 2
     */
    @Test
    public void testBefore1After2() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(4, 19);
        assertEquals(2, Iterables.size(fIntersecting));
    }

    /**
     * Time before 1st latency start time -> Time on 2nd latency end time
     * Expected result : 2
     */
    @Test
    public void testBefore1On2End() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(4, 18);
        assertEquals(2, Iterables.size(fIntersecting));
    }

    /**
     * Time before 1st latency start time -> Time before 2nd latency end time
     * Expected result : 2
     */
    @Test
    public void testBefore1Before2End() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(4, 17);
        assertEquals(2, Iterables.size(fIntersecting));
    }

    /**
     * Time before 1st latency start time -> Time on 2nd latency start time
     * Expected result : 1
     */
    @Test
    public void testBefore1On2Start() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(4, 16);
        assertEquals(1, Iterables.size(fIntersecting));
    }

    /**
     * Time before 1st latency start time -> Time before 2nd latency start time
     * Expected result : 1
     */
    @Test
    public void testBefore1Before2Start() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(4, 15);
        assertEquals(1, Iterables.size(fIntersecting));
    }

    /**
     * Time on 1st latency start time -> Time after 2nd latency end time
     * Expected result : 2
     */
    @Test
    public void testOn1After2() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(5, 19);
        assertEquals(2, Iterables.size(fIntersecting));
    }

    /**
     * Time on 1st latency start time -> Time on 2nd latency end time Expected
     * result : 2
     */
    @Test
    public void testOn1On2End() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(5, 18);
        assertEquals(2, Iterables.size(fIntersecting));
    }

    /**
     * Time on 1st latency start time -> Time before 2nd latency end time
     * Expected result : 2
     */
    @Test
    public void testOn1Before2End() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(5, 17);
        assertEquals(2, Iterables.size(fIntersecting));
    }

    /**
     * Time on 1st latency start time -> Time on 2nd latency start time Expected
     * result : 1
     */
    @Test
    public void testOn1On2Start() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(5, 16);
        assertEquals(1, Iterables.size(fIntersecting));
    }

    /**
     * Time on 1st latency start time -> Time before 2nd latency start time
     * Expected result : 1
     */
    @Test
    public void testOn1Before2Start() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(5, 15);
        assertEquals(1, Iterables.size(fIntersecting));
    }

    /**
     * Time before 2nd latency start time -> Time on 2nd latency end time
     * Expected result : 1
     */
    @Test
    public void testBefore2On2() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(15, 18);
        assertEquals(1, Iterables.size(fIntersecting));
    }

    /**
     * Time before 2nd latency start time -> Time after 2nd latency end time
     * Expected result : 1
     */
    @Test
    public void testBefore2After2() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(15, 19);
        assertEquals(1, Iterables.size(fIntersecting));
    }

    /**
     * Time on 2nd latency start time -> Time on 2nd latency end time Expected
     * result : 1
     */
    @Test
    public void testOn2On2() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(16, 18);
        assertEquals(1, Iterables.size(fIntersecting));
    }

    /**
     * Time after 2nd latency end time -> Time after 2nd latency end time
     * Expected result : 1
     */
    @Test
    public void testAfter2After2() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(20, 25);
        assertEquals(0, Iterables.size(fIntersecting));
    }

    // ------------------------------------------------------------------------
    // Tests for 3 latencies
    // ------------------------------------------------------------------------

    /**
     * Time before 1st latency start time -> Time after 3rd latency end time
     * Expected result : 3
     */
    @Test
    public void testBefore1After3() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(1, 35);
        assertEquals(3, Iterables.size(fIntersecting));
    }

    /**
     * Time before 1st latency start time -> Time before 3rd latency start time
     * Expected result : 2
     */
    @Test
    public void testBefore1Before3() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(1, 28);
        assertEquals(2, Iterables.size(fIntersecting));
    }

    /**
     * Time after 1st latency end time -> Time after 3rd latency end time
     * Expected result : 2
     */
    @Test
    public void testAfter1After3() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(12, 50);
        assertEquals(2, Iterables.size(fIntersecting));
    }
}