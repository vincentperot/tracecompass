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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.segmentstore.core.tests.stubs.TestInterval;
import org.eclipse.tracecompass.segmentstore.core.treemap.TreeMapStore;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;

/**
 * Unit tests for intersecting elements in a TreeMapStore
 *
 * @author France Lapointe Nguyen
 */
public class TestIntersectingElements {

    @Nullable TreeMapStore<TestInterval> fData = null;
    @Nullable Iterable<TestInterval> fIntersecting;

    /**
     * Initialize data (test vector) that will be tested
     */
    @Before
    public void setup() {

        List<TestInterval> data = new ArrayList<>();
        data.add(new TestInterval(5, 10));
        data.add(new TestInterval(16, 18));
        data.add(new TestInterval(30, 34));

        TreeMapStore<TestInterval> treeMapStore = new TreeMapStore<>();

        for (TestInterval interval : data) {
            assertNotNull(interval);
            treeMapStore.addElement(interval);
        }

        fData = treeMapStore;
        fIntersecting = treeMapStore;
    }

    // ------------------------------------------------------------------------
    // Tests for one segment
    // ------------------------------------------------------------------------

    /**
     * Time before a segment start time -> Time after a segment end time
     * Expected result : 1 segment
     */
    @Test
    public void testBeforeStartAfterEnd() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(4, 11);
        assertEquals(1, Iterables.size(fIntersecting));
    }

    /**
     * Time before a segment start time -> Time before a segment end time
     * Expected result : 1 segment
     */
    @Test
    public void testBeforeStartBeforeEnd() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(4, 9);
        assertEquals(1, Iterables.size(fIntersecting));
    }

    /**
     * Time after a segment start time -> Time after a segment end time Expected
     * result : 0 segment
     */
    @Test
    public void testAfterStartAfterEnd() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(6, 11);
        assertEquals(0, Iterables.size(fIntersecting));
    }

    /**
     * Time after a segment start time -> Time before a segment end time
     * Expected result : 0 segment
     */
    @Test
    public void testAfterStartBeforeEnd() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(6, 9);
        assertEquals(0, Iterables.size(fIntersecting));
    }

    /**
     * Time equals to a segment start time -> Time equals to a segment end time
     * Expected result : 1 segment
     */
    @Test
    public void testOnStartOnEnd() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(5, 10);
        assertEquals(1, Iterables.size(fIntersecting));
    }

    /**
     * Time before a segment start time -> Time equals to a segment start time
     * Expected result : 1 segment
     */
    @Test
    public void testBeforeStartOnStart() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(2, 5);
        assertEquals(1, Iterables.size(fIntersecting));
    }

    /**
     * Time equals to a segment end time -> Time after a segment end time
     * Expected result : 0 segment
     */
    @Test
    public void testOnEndAfterEnd() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(10, 12);
        assertEquals(0, Iterables.size(fIntersecting));
    }

    /**
     * Time before a segment start time -> Time before a segment start time
     * Expected result : 0 segment
     */
    @Test
    public void testBeforeStartBeforeStart() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(1, 2);
        assertEquals(0, Iterables.size(fIntersecting));
    }

    /**
     * Time after a segment start time -> Time after a segment end time Expected
     * result : 0 segment
     */
    @Test
    public void testAfterEndAfterEnd() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(12, 14);
        assertEquals(0, Iterables.size(fIntersecting));
    }

    /**
     * Time on a segment start time -> Time on a segment start time Expected
     * result : 1 segment
     */
    @Test
    public void testOnStartOnStart() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(5, 5);
        assertEquals(1, Iterables.size(fIntersecting));
    }

    // ------------------------------------------------------------------------
    // Tests for 2 latencies
    // ------------------------------------------------------------------------

    /**
     * Time before 1st segment start time -> Time after 2nd segment end time
     * Expected result : 2
     */
    @Test
    public void testBefore1After2() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(4, 19);
        assertEquals(2, Iterables.size(fIntersecting));
    }

    /**
     * Time before 1st segment start time -> Time on 2nd segment end time
     * Expected result : 2
     */
    @Test
    public void testBefore1On2End() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(4, 18);
        assertEquals(2, Iterables.size(fIntersecting));
    }

    /**
     * Time before 1st segment start time -> Time before 2nd segment end time
     * Expected result : 2
     */
    @Test
    public void testBefore1Before2End() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(4, 17);
        assertEquals(2, Iterables.size(fIntersecting));
    }

    /**
     * Time before 1st segment start time -> Time on 2nd segment start time
     * Expected result : 2
     */
    @Test
    public void testBefore1On2Start() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(4, 16);
        assertEquals(2, Iterables.size(fIntersecting));
    }

    /**
     * Time before 1st segment start time -> Time before 2nd segment start time
     * Expected result : 1
     */
    @Test
    public void testBefore1Before2Start() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(4, 15);
        assertEquals(1, Iterables.size(fIntersecting));
    }

    /**
     * Time on 1st segment start time -> Time after 2nd segment end time
     * Expected result : 2
     */
    @Test
    public void testOn1After2() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(5, 19);
        assertEquals(2, Iterables.size(fIntersecting));
    }

    /**
     * Time on 1st segment start time -> Time on 2nd segment end time Expected
     * result : 2
     */
    @Test
    public void testOn1On2End() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(5, 18);
        assertEquals(2, Iterables.size(fIntersecting));
    }

    /**
     * Time on 1st segment start time -> Time before 2nd segment end time
     * Expected result : 2
     */
    @Test
    public void testOn1Before2End() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(5, 17);
        assertEquals(2, Iterables.size(fIntersecting));
    }

    /**
     * Time on 1st segment start time -> Time on 2nd segment start time Expected
     * result : 2
     */
    @Test
    public void testOn1On2Start() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(5, 16);
        assertEquals(2, Iterables.size(fIntersecting));
    }

    /**
     * Time on 1st segment start time -> Time before 2nd segment start time
     * Expected result : 1
     */
    @Test
    public void testOn1Before2Start() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(5, 15);
        assertEquals(1, Iterables.size(fIntersecting));
    }

    /**
     * Time before 2nd segment start time -> Time on 2nd segment end time
     * Expected result : 1
     */
    @Test
    public void testBefore2On2() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(15, 18);
        assertEquals(1, Iterables.size(fIntersecting));
    }

    /**
     * Time before 2nd segment start time -> Time after 2nd segment end time
     * Expected result : 1
     */
    @Test
    public void testBefore2After2() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(15, 19);
        assertEquals(1, Iterables.size(fIntersecting));
    }

    /**
     * Time on 2nd segment start time -> Time on 2nd segment end time Expected
     * result : 1
     */
    @Test
    public void testOn2On2End() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(16, 18);
        assertEquals(1, Iterables.size(fIntersecting));
    }

    /**
     * Time after 2nd segment end time -> Time after 2nd segment end time
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
     * Time before 1st segment start time -> Time after 3rd segment end time
     * Expected result : 3
     */
    @Test
    public void testBefore1After3() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(1, 35);
        assertEquals(3, Iterables.size(fIntersecting));
    }

    /**
     * Time before 1st segment start time -> Time before 3rd segment start time
     * Expected result : 2
     */
    @Test
    public void testBefore1Before3() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(1, 28);
        assertEquals(2, Iterables.size(fIntersecting));
    }

    /**
     * Time after 1st segment end time -> Time after 3rd segment end time
     * Expected result : 2
     */
    @Test
    public void testAfter1After3() {
        assertNotNull(fData);
        fIntersecting = fData.getIntersectingElements(12, 50);
        assertEquals(2, Iterables.size(fIntersecting));
    }
}