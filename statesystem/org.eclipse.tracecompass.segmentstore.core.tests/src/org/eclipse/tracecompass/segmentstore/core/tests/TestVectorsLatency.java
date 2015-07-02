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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tracecompass.segmentstore.core.tests.stubs.TestInterval;

/**
 * @author France Lapointe Nguyen
 * @since 1.0
 */
public class TestVectorsLatency {

    List<TestInterval> fTrs = new ArrayList<>();

    /**
     * @param testNumber
     *            test number to choose the test vector
     */
    public TestVectorsLatency(int testNumber) {
        switch (testNumber) {
        // Linear low size
        case 1:
            linear(10);
            break;
        // Linear high size
        case 2:
            linear(100);
            break;
        // Latency in the beginning and end only
        case 3:
            beginningAndEnd();
            break;
        case 4:
            oneLatency();
            break;
        case 5:
            twoLatencies();
            break;
        case 6:
            threeLatencies();
            break;
        default:
            break;

        }
    }

    private void linear(int nb) {
        for (int i = 0; i < nb; i++) {
            int start = i * 10;
            long end = start + 10 + i;
            fTrs.add(new TestInterval(start, end));
        }
    }

    private void beginningAndEnd() {
        fTrs.add(new TestInterval(0, 5));
        fTrs.add(new TestInterval(6, 12));
        fTrs.add(new TestInterval(15, 20));
        fTrs.add(new TestInterval(100, 102));
        fTrs.add(new TestInterval(105, 110));
        fTrs.add(new TestInterval(108, 112));

    }

    private void oneLatency() {
        fTrs.add(new TestInterval(5, 10));
    }

    private void twoLatencies() {
        fTrs.add(new TestInterval(5, 10));
        fTrs.add(new TestInterval(16, 18));
    }

    private void threeLatencies() {
        fTrs.add(new TestInterval(5, 10));
        fTrs.add(new TestInterval(16, 18));
        fTrs.add(new TestInterval(30, 34));
    }


    /**
     * @return List of ISegment
     */
    public List<TestInterval> getDataList() {
        return fTrs;
    }
}