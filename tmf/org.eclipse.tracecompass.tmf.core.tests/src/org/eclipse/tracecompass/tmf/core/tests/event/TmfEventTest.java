/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Adjusted for new Event Model
 *   Alexandre Montplaisir - Port to JUnit4
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventType;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.junit.After;
import org.junit.Test;

/**
 * Test suite for the TmfEvent class.
 */
@SuppressWarnings("javadoc")
public class TmfEventTest {

    /** A trace to associate events with */
    private static final TmfTestTrace STUB_TRACE = TmfTestTrace.A_TEST_10K;

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private final @NonNull ITmfTrace fTrace = STUB_TRACE.getTrace();

    private final String fTypeId = "TestType";
    private final String fLabel1 = "AString";
    private final String fLabel2 = "AnInteger";
    private final String[] fLabels = new String[] { fLabel1, fLabel2 };
    private final TmfEventType fType = new TmfEventType(fTypeId, TmfEventField.makeRoot(fLabels));

    private final Object fValue1a = "Some string";
    private final Object fValue1b = Integer.valueOf(10);
    private final ITmfEventField fField1a = new TmfEventField(fLabel1, fValue1a, null);
    private final ITmfEventField fField1b = new TmfEventField(fLabel2, fValue1b, null);
    private final ITmfEventField[] fFields1 = new ITmfEventField[] { fField1a, fField1b };
    private final String fRawContent1 = fField1a.toString() + fField1b.toString();
    private final ITmfEventField fContent1 = new TmfEventField(fRawContent1, null, fFields1);
    private final TmfTimestamp fTimestamp1 = new TmfTimestamp(12345, 2);
    private final @NonNull ITmfEvent fEvent1 = new TmfEvent(fTrace, 0, fTimestamp1, fType, fContent1);

    private final Object fValue2a = "Another string";
    private final Object fValue2b = Integer.valueOf(-4);
    private final ITmfEventField fField2a = new TmfEventField(fLabel1, fValue2a, null);
    private final ITmfEventField fField2b = new TmfEventField(fLabel2, fValue2b, null);
    private final ITmfEventField[] fFields2 = new ITmfEventField[] { fField2a, fField2b };
    private final String fRawContent2 = fField2a.toString() + fField2b.toString();
    private final ITmfEventField fContent2 = new TmfEventField(fRawContent2, null, fFields2);
    private final TmfTimestamp fTimestamp2 = new TmfTimestamp(12350, 2);
    private final @NonNull ITmfEvent fEvent2 = new TmfEvent(fTrace, 1, fTimestamp2, fType, fContent2);

    // ------------------------------------------------------------------------
    // Helper functions
    // ------------------------------------------------------------------------

    @After
    public void disposeTrace() {
        fTrace.dispose();
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    @Test
    public void testDefaultConstructor() {
        final ITmfEvent event = new TmfEvent(fTrace, ITmfContext.UNKNOWN_RANK, null, null, null);
        assertNotNull("getTrace", event.getTrace());
        assertEquals("getRank", ITmfContext.UNKNOWN_RANK, event.getRank());
        assertEquals("getTimestamp", TmfTimestamp.ZERO, event.getTimestamp());
        assertNull("getType", event.getType());
        assertNull("getContent", event.getContent());
    }

    @Test
    public void testFullConstructor() {
        assertNotNull("getTrace", fEvent1.getTrace());
        assertEquals("getRank", 0, fEvent1.getRank());
        assertEquals("getTimestamp", fTimestamp1, fEvent1.getTimestamp());
        assertEquals("getType", fType, fEvent1.getType());
        assertEquals("getContent", fContent1, fEvent1.getContent());

        assertNotNull("getTrace", fEvent2.getTrace());
        assertEquals("getRank", 1, fEvent2.getRank());
        assertEquals("getTimestamp", fTimestamp2, fEvent2.getTimestamp());
        assertEquals("getType", fType, fEvent2.getType());
        assertEquals("getContent", fContent2, fEvent2.getContent());
    }

    @Test
    public void testNoRankConstructor() {
        final ITmfEvent event = new TmfEvent(fTrace, ITmfContext.UNKNOWN_RANK, fTimestamp1, fType, fContent1);
        assertNotNull("getTrace", event.getTrace());
        assertEquals("getRank", ITmfContext.UNKNOWN_RANK, event.getRank());
        assertEquals("getTimestamp", fTimestamp1, event.getTimestamp());
        assertEquals("getType", fType, event.getType());
        assertEquals("getContent", fContent1, event.getContent());
    }

    @Test
    public void testConstructorWithTrace() {
        final ITmfTrace trace = fTrace;
        final ITmfEvent event = new TmfEvent(trace, 0, fTimestamp1, fType, fContent1);
        assertNotNull("getTrace", event.getTrace());
        assertEquals("getRank", 0, event.getRank());
        assertEquals("getTimestamp", fTimestamp1, event.getTimestamp());
        assertEquals("getType", fType, event.getType());
        assertEquals("getContent", fContent1, event.getContent());
        trace.dispose();
    }

    @Test
    public void testTmfEventCopy() {
        final ITmfEvent event = new TmfEvent(fEvent1);
        assertNotNull("getTrace", event.getTrace());
        assertEquals("getRank", 0, event.getRank());
        assertEquals("getTimestamp", fTimestamp1, event.getTimestamp());
        assertEquals("getType", fType, event.getType());
        assertEquals("getContent", fContent1, event.getContent());
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    @Test
    public void testToString() {
        final String expected1 = "TmfEvent [fTimestamp=" + fTimestamp1 + ", fTrace=" + fTrace +
                ", fRank=0, fType=" + fType + ", fContent=" + fContent1 + "]";
        assertEquals("toString", expected1, fEvent1.toString());

        final String expected2 = "TmfEvent [fTimestamp=" + fTimestamp2 + ", fTrace=" + fTrace +
                ", fRank=1, fType=" + fType + ", fContent=" + fContent2 + "]";
        assertEquals("toString", expected2, fEvent2.toString());
    }

    /**
     * Test the .toString() with extended classes. It should print the correct
     * class name.
     */
    @Test
    public void testToStringExtended() {
        class ExtendedEvent extends TmfEvent {
            ExtendedEvent(@NonNull ITmfEvent event) {
                super(event);
            }
        }
        ExtendedEvent event = new ExtendedEvent(fEvent1);
        String expected = "ExtendedEvent [fTimestamp=" + fTimestamp1
                + ", fTrace=" + fTrace + ", fRank=0"
                + ", fType=" + fType + ", fContent=" + fContent1 + "]";

        assertEquals(expected, event.toString());
    }

}
