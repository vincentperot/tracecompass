/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.tracecompass.internal.tmf.core.request.TmfCoalescedEventRequest;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest.ExecutionType;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.signal.TmfEndSynchSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfStartSynchSignal;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfExperimentStub;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test suite for the TmfEventProvider class focusing on coalescing of experiments.
 */
public class TmfEventProviderCoalescingTest {

    private static TmfTraceStub fTmfTrace1;
    private static TmfTraceStub fTmfTrace2;
    private static TmfTraceStub fTmfTrace3;
    private static TmfExperimentStub fExperiment;
    private static TmfExperimentStub fExperiment2;

    /**
     * Test class initialization
     */
    @BeforeClass
    public static void setUp() {
        fTmfTrace1 = (TmfTraceStub)TmfTestTrace.A_TEST_10K.getTrace();
        fTmfTrace2 = (TmfTraceStub)TmfTestTrace.A_TEST_10K2.getTrace();
        fTmfTrace3 = (TmfTraceStub)TmfTestTrace.E_TEST_10K.getTrace();

        ITmfTrace[] traces2 = new ITmfTrace[1];
        traces2[0] = fTmfTrace3;
        fExperiment2 = new TmfExperimentStub();
        fExperiment2.initExperiment(ITmfEvent.class, fExperiment2.getName(), traces2, 100, null);

        ITmfTrace[] traces = new ITmfTrace[3];
        traces[0] = fTmfTrace1;
        traces[1] = fTmfTrace2;
        traces[2] = fExperiment2;
        fExperiment = new TmfExperimentStub();
        fExperiment.initExperiment(ITmfEvent.class, fExperiment.getName(), traces, 100, null);

        // Disable the timer
        fExperiment.indexTrace(true);
    }

    /**
     * Test class clean-up
     */
    @AfterClass
    public static void tearDown() {
        fExperiment.dispose();
            }

    /**
     * Test setup-up
     */
    @Before
    public void testSetup() {
        // Reset the request IDs
        TmfEventRequest.reset();
    }

    /**
     * Test clean-up
     */
    @After
    public void testCleanUp() {
        // Reset the request IDs
        TmfEventRequest.reset();
        // clear pending request
        clearPendingRequests();
        setTimerFlags(false);
    }

    // ------------------------------------------------------------------------
    // Test coalescing across providers - parent request is send first
    // ------------------------------------------------------------------------

    /***/
    @Test
    public void testParentFirstCoalescing() {
        InnerEventRequest expReq = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        InnerEventRequest trace1Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        InnerEventRequest trace2Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        fExperiment.sendRequest(expReq);
        fTmfTrace1.sendRequest(trace1Req);
        fTmfTrace2.sendRequest(trace2Req);

        // Verify that requests are coalesced properly with the experiment request
        List<TmfCoalescedEventRequest> pending = fExperiment.getPendingRequests();
        assertEquals(1, pending.size());

        assertEquals(0, fTmfTrace1.getPendingRequests().size());
        assertEquals(0, fTmfTrace2.getPendingRequests().size());

        TmfCoalescedEventRequest coalescedRequest = pending.get(0);
        assertEquals("[0, 1, 2]", coalescedRequest.getSubRequestIds());

        // Now trigger manually the sending of the request
        fExperiment.notifyPendingRequest(false);

        try {
            expReq.waitForCompletion();
        } catch (InterruptedException e) {
        }

        // Verify that requests only received events from the relevant traces
        assertTrue(expReq.isTraceHandled(fTmfTrace1));
        assertTrue(expReq.isTraceHandled(fTmfTrace2));

        assertTrue(trace1Req.isTraceHandled(fTmfTrace1));
        assertFalse(trace1Req.isTraceHandled(fTmfTrace2));

        assertFalse(trace2Req.isTraceHandled(fTmfTrace1));
        assertTrue(trace2Req.isTraceHandled(fTmfTrace2));

    }

    // ------------------------------------------------------------------------
    // Test coalescing across providers - children requests are send first
    // ------------------------------------------------------------------------
    /***/
    @Test
    public void testChildrenFirstCoalescing() {
        InnerEventRequest expReq = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        InnerEventRequest trace1Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        InnerEventRequest trace2Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        fTmfTrace1.sendRequest(trace1Req);
        fTmfTrace2.sendRequest(trace2Req);
        fExperiment.sendRequest(expReq);

        // Verify that requests are coalesced properly with the experiment request
        List<TmfCoalescedEventRequest> pending = fExperiment.getPendingRequests();
        assertEquals(1, pending.size());

        assertEquals(0, fTmfTrace1.getPendingRequests().size());
        assertEquals(0, fTmfTrace2.getPendingRequests().size());

        TmfCoalescedEventRequest coalescedRequest = pending.get(0);
        assertEquals("[0, 1, 2]", coalescedRequest.getSubRequestIds());

        // Now trigger manually the sending of the request
        fExperiment.notifyPendingRequest(false);

        try {
            expReq.waitForCompletion();
        } catch (InterruptedException e) {
        }

        // Verify that requests only received events from the relevant traces
        assertTrue(expReq.isTraceHandled(fTmfTrace1));
        assertTrue(expReq.isTraceHandled(fTmfTrace2));

        assertTrue(trace1Req.isTraceHandled(fTmfTrace1));
        assertFalse(trace1Req.isTraceHandled(fTmfTrace2));

        assertFalse(trace2Req.isTraceHandled(fTmfTrace1));
        assertTrue(trace2Req.isTraceHandled(fTmfTrace2));
    }

    /***/
    @Test
    public void testChildrenFirstCoalescing2() {

        InnerEventRequest expReq = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        InnerEventRequest exp2Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        InnerEventRequest trace1Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        InnerEventRequest trace2Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        InnerEventRequest trace3Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        fTmfTrace1.sendRequest(trace1Req);
        fTmfTrace2.sendRequest(trace2Req);
        fTmfTrace3.sendRequest(trace3Req);
        fExperiment2.sendRequest(exp2Req);
        fExperiment.sendRequest(expReq);

        // Verify that requests are coalesced properly with the experiment request
        List<TmfCoalescedEventRequest> pending = fExperiment.getPendingRequests();
        assertEquals(1, pending.size());

        assertEquals(0, fTmfTrace1.getPendingRequests().size());
        assertEquals(0, fTmfTrace2.getPendingRequests().size());

        TmfCoalescedEventRequest coalescedRequest = pending.get(0);
        assertEquals("[0, 2, 3, 1, 4]", coalescedRequest.getSubRequestIds());

        // Now trigger manually the sending of the request
        fExperiment.notifyPendingRequest(false);

        try {
            expReq.waitForCompletion();
        } catch (InterruptedException e) {
        }

        // Verify that requests only received events from the relevant traces
        assertTrue(expReq.isTraceHandled(fTmfTrace1));
        assertTrue(expReq.isTraceHandled(fTmfTrace2));
        assertTrue(expReq.isTraceHandled(fTmfTrace3));

        assertFalse(exp2Req.isTraceHandled(fTmfTrace1));
        assertFalse(exp2Req.isTraceHandled(fTmfTrace2));
        assertTrue(exp2Req.isTraceHandled(fTmfTrace3));

        assertTrue(trace1Req.isTraceHandled(fTmfTrace1));
        assertFalse(trace1Req.isTraceHandled(fTmfTrace2));
        assertFalse(trace1Req.isTraceHandled(fTmfTrace3));

        assertFalse(trace2Req.isTraceHandled(fTmfTrace1));
        assertTrue(trace2Req.isTraceHandled(fTmfTrace2));
        assertFalse(trace2Req.isTraceHandled(fTmfTrace3));

        assertFalse(trace3Req.isTraceHandled(fTmfTrace1));
        assertFalse(trace3Req.isTraceHandled(fTmfTrace2));
        assertTrue(trace3Req.isTraceHandled(fTmfTrace3));
    }

    /***/
    @Test
    public void testMixedOrderCoalescing() {
        InnerEventRequest expReq = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        InnerEventRequest trace1Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        InnerEventRequest trace2Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.BACKGROUND);
        fTmfTrace1.sendRequest(trace1Req);
        fExperiment.sendRequest(expReq);
        fTmfTrace2.sendRequest(trace2Req);

        // Verify that requests are coalesced properly with the experiment request
        List<TmfCoalescedEventRequest> pending = fExperiment.getPendingRequests();
        assertEquals(1, pending.size());

        assertEquals(0, fTmfTrace1.getPendingRequests().size());
        assertEquals(0, fTmfTrace2.getPendingRequests().size());

        TmfCoalescedEventRequest coalescedRequest = pending.get(0);
        assertEquals("[0, 1, 2]", coalescedRequest.getSubRequestIds());

        // Now trigger manually the sending of the request
        fExperiment.notifyPendingRequest(false);

        try {
            expReq.waitForCompletion();
        } catch (InterruptedException e) {
        }

        // Verify that requests only received events from the relevant traces
        assertTrue(expReq.isTraceHandled(fTmfTrace1));
        assertTrue(expReq.isTraceHandled(fTmfTrace2));

        assertTrue(trace1Req.isTraceHandled(fTmfTrace1));
        assertFalse(trace1Req.isTraceHandled(fTmfTrace2));

        assertFalse(trace2Req.isTraceHandled(fTmfTrace1));
        assertTrue(trace2Req.isTraceHandled(fTmfTrace2));
    }

    /***/
    @Test
    public void testMultipleRequestsCoalescing() {
        InnerEventRequest expReq = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.FOREGROUND);
        InnerEventRequest expReq2 = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.FOREGROUND);
        InnerEventRequest trace1Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.FOREGROUND);
        InnerEventRequest trace1Req2 = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.FOREGROUND);
        InnerEventRequest trace2Req = new InnerEventRequest(ITmfEvent.class, 0, ITmfEventRequest.ALL_DATA, ExecutionType.FOREGROUND);
        sendSync(true);
        fTmfTrace1.sendRequest(trace1Req);
        fTmfTrace1.sendRequest(trace1Req2);
        fExperiment.sendRequest(expReq);
        fTmfTrace2.sendRequest(trace2Req);
        fExperiment.sendRequest(expReq2);

        // Verify that requests are coalesced properly with the experiment request
        List<TmfCoalescedEventRequest> pending = fExperiment.getPendingRequests();
        assertEquals(1, pending.size());

        assertEquals(0, fTmfTrace1.getPendingRequests().size());
        assertEquals(0, fTmfTrace2.getPendingRequests().size());
        assertEquals(0, fTmfTrace3.getPendingRequests().size());
        assertEquals(0, fExperiment2.getPendingRequests().size());

        TmfCoalescedEventRequest coalescedRequest = pending.get(0);
        assertEquals("[0, 2, 3, 4, 1]", coalescedRequest.getSubRequestIds());
        sendSync(false);

        try {
            expReq.waitForCompletion();
        } catch (InterruptedException e) {
        }

        assertTrue(expReq.isTraceHandled(fTmfTrace1));
        assertTrue(expReq.isTraceHandled(fTmfTrace2));
        assertTrue(expReq.isTraceHandled(fTmfTrace3));

        assertTrue(expReq2.isTraceHandled(fTmfTrace1));
        assertTrue(expReq2.isTraceHandled(fTmfTrace2));
        assertTrue(expReq2.isTraceHandled(fTmfTrace3));

        assertTrue(trace1Req.isTraceHandled(fTmfTrace1));
        assertFalse(trace1Req.isTraceHandled(fTmfTrace2));
        assertFalse(trace1Req.isTraceHandled(fTmfTrace3));

        assertFalse(trace2Req.isTraceHandled(fTmfTrace1));
        assertTrue(trace2Req.isTraceHandled(fTmfTrace2));
        assertFalse(trace2Req.isTraceHandled(fTmfTrace3));

    }

    private static void sendSync(boolean isStart) {
        if (isStart) {
            TmfStartSynchSignal signal = new TmfStartSynchSignal(0);
            fTmfTrace1.startSynch(signal);
            fTmfTrace1.startSynch(signal);
            fExperiment.startSynch(signal);
            fTmfTrace2.startSynch(signal);
            fExperiment.startSynch(signal);

        } else {
            TmfEndSynchSignal signal = new TmfEndSynchSignal(0);
            fTmfTrace1.endSynch(signal);
            fTmfTrace1.endSynch(signal);
            fExperiment.endSynch(signal);
            fTmfTrace2.endSynch(signal);
            fExperiment.endSynch(signal);
        }
    }

    private static void setTimerFlags(boolean flag) {
        fExperiment.setTimerEnabled(flag);
        fExperiment2.setTimerEnabled(flag);
        fTmfTrace1.setTimerEnabled(flag);
        fTmfTrace2.setTimerEnabled(flag);
        fTmfTrace3.setTimerEnabled(flag);
    }

    private static void clearPendingRequests() {
        fExperiment.clearPendingRequests();
        fExperiment2.clearPendingRequests();
        fTmfTrace1.clearPendingRequests();
        fTmfTrace2.clearPendingRequests();
        fTmfTrace3.clearPendingRequests();
    }

    private static class InnerEventRequest extends TmfEventRequest {
        private Set<String> traces = new HashSet<>();

        public InnerEventRequest(Class<? extends ITmfEvent> dataType, long index, int nbRequested, ExecutionType priority) {
            super(dataType, index, nbRequested, priority);
        }

        @Override
        public void handleData(ITmfEvent event) {
            super.handleData(event);
            if (!traces.contains(event.getTrace().getName())) {
                traces.add(event.getTrace().getName());
            }
        }

        public boolean isTraceHandled(ITmfTrace trace) {
            return traces.contains(trace.getName());
        }
    }
}
