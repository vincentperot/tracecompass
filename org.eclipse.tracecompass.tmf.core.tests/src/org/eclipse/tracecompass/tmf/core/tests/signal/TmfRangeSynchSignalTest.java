/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam- Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.signal;

import org.eclipse.tracecompass.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfNanoTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.junit.*;

import static org.junit.Assert.*;

/**
 * The class <code>TmfRangeSynchSignalTest</code> contains tests for the class
 * <code>{@link TmfRangeSynchSignal}</code>.
 *
 * @author Matthew Khouzam
 */
public class TmfRangeSynchSignalTest {
    private static final TmfNanoTimestamp START_TIME = new TmfNanoTimestamp(1);
    private static final TmfNanoTimestamp MIDDLE_TIME = new TmfNanoTimestamp(50);
    private static final TmfNanoTimestamp END_TIME = new TmfNanoTimestamp(100);
    private static final TmfTimeRange RANGE = new TmfTimeRange(START_TIME, END_TIME);

    /**
     * Run the TmfRangeSynchSignal(Object,TmfTimeRange) constructor test.
     */
    @Test
    public void testTmfRangeSynchSignal_1() {
        TmfRangeSynchSignal result = new TmfRangeSynchSignal(TmfRangeSynchSignalTest.this, RANGE);
        assertNotNull(result);
    }

    /**
     * Run the TmfTimeRange getCurrentRange() method test.
     */
    @Test
    public void testGetCurrentRange() {
        TmfRangeSynchSignal fixture = new TmfRangeSynchSignal(TmfRangeSynchSignalTest.this, RANGE);
        TmfTimeRange result = fixture.getCurrentRange();
        assertEquals(RANGE, result);
    }

    /**
     * Run the TmfTimeRange ToString() method test.
     */
    @Test
    public void testToString1() {
        String source = "org.eclipse.tracecompass.tmf.core.tests.signal.TmfRangeSynchSignalTest";
        String expected = createString(source, START_TIME, END_TIME);
        TmfRangeSynchSignal fixture = new TmfRangeSynchSignal(source, RANGE);
        assertEquals(expected, fixture.toString());
    }

    /**
     * Run the TmfTimeRange ToString() method test.
     */
    @Test
    public void testToString2() {
        String source = "hello world";
        String expected = createString(source, START_TIME, MIDDLE_TIME);
        TmfRangeSynchSignal fixture = new TmfRangeSynchSignal(source, new TmfTimeRange(START_TIME, MIDDLE_TIME));
        assertEquals(expected, fixture.toString());
    }

    /**
     * Run the TmfTimeRange ToString() method test.
     */
    @Test
    public void testToStringNullSource() {
        String expected = createString("null", START_TIME, END_TIME);
        TmfRangeSynchSignal fixture = new TmfRangeSynchSignal(null, RANGE);
        assertEquals(expected, fixture.toString());
    }

    private static String createString(String source, ITmfTimestamp start, ITmfTimestamp end) {
        return "TmfRangeSynchSignal [source=" + source + ", range=TmfTimeRange [fStartTime=" + start.toString() + ", fEndTime=" + end.toString() + "]]";
    }

    /**
     * Run the TmfTimeRange ToString() method test.
     */
    @Test
    public void testToStringNullRange() {
        String expected = "TmfRangeSynchSignal [source=hello world, range=null]";
        TmfRangeSynchSignal fixture = new TmfRangeSynchSignal("hello world", null);
        assertEquals(expected, fixture.toString());
    }

    /**
     * Run the TmfTimeRange ToString() method test.
     */
    @Test
    public void testToStringAllNull() {
        String expected = "TmfRangeSynchSignal [source=null, range=null]";
        TmfRangeSynchSignal fixture = new TmfRangeSynchSignal(null, null);
        assertEquals(expected, fixture.toString());
    }
}