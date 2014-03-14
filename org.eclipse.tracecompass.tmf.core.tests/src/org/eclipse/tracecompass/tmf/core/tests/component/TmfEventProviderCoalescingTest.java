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

import java.util.List;

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
        fExperiment.setTimerEnabled(false);
        fExperiment2.setTimerEnabled(false);
        fTmfTrace1.setTimerEnabled(false);
        fTmfTrace2.setTimerEnabled(false);
        fTmfTrace3.setTimerEnabled(false);
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
        fExperiment.clearPendingRequests();
        fExperiment2.clearPendingRequests();
        fTmfTrace1.clearPendingRequests();
        fTmfTrace2.clearPendingRequests();
        fTmfTrace3.clearPendingRequests();
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

        List<TmfCoalescedEventRequest> pending = fExperiment.getPendingRequests();
        assertEquals(1, pending.size());

        assertEquals(0, fTmfTrace1.getPendingRequests().size());
        assertEquals(0, fTmfTrace2.getPendingRequests().size());

        TmfCoalescedEventRequest coalescedRequest = pending.get(0);
        assertEquals("[0, 1, 2]", coalescedRequest.getSubRequestIds());
        fExperiment.clearPendingRequests();
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

        List<TmfCoalescedEventRequest> pending = fExperiment.getPendingRequests();
        assertEquals(1, pending.size());

        assertEquals(0, fTmfTrace1.getPendingRequests().size());
        assertEquals(0, fTmfTrace2.getPendingRequests().size());

        TmfCoalescedEventRequest coalescedRequest = pending.get(0);
        assertEquals("[0, 3, 4]", coalescedRequest.getSubRequestIds());
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

        List<TmfCoalescedEventRequest> pending = fExperiment.getPendingRequests();
        assertEquals(1, pending.size());

        assertEquals(0, fTmfTrace1.getPendingRequests().size());
        assertEquals(0, fTmfTrace2.getPendingRequests().size());

        TmfCoalescedEventRequest coalescedRequest = pending.get(0);
        assertEquals("[0, 5, 6, 8]", coalescedRequest.getSubRequestIds());
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

        List<TmfCoalescedEventRequest> pending = fExperiment.getPendingRequests();
        assertEquals(1, pending.size());

        assertEquals(0, fTmfTrace1.getPendingRequests().size());
        assertEquals(0, fTmfTrace2.getPendingRequests().size());

        TmfCoalescedEventRequest coalescedRequest = pending.get(0);
        assertEquals("[0, 3, 2]", coalescedRequest.getSubRequestIds());
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

        List<TmfCoalescedEventRequest> pending = fExperiment.getPendingRequests();
        assertEquals(1, pending.size());

        assertEquals(0, fTmfTrace1.getPendingRequests().size());
        assertEquals(0, fTmfTrace2.getPendingRequests().size());
        assertEquals(0, fTmfTrace3.getPendingRequests().size());
        assertEquals(0, fExperiment2.getPendingRequests().size());

        TmfCoalescedEventRequest coalescedRequest = pending.get(0);
        assertEquals("[0, 5, 4, 1]", coalescedRequest.getSubRequestIds());
        fExperiment.clearPendingRequests();
        sendSync(false);
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

    private static class InnerEventRequest extends TmfEventRequest {
        public InnerEventRequest(Class<? extends ITmfEvent> dataType, long index, int nbRequested, ExecutionType priority) {
            super(dataType, index, nbRequested, priority);
        }
    }

}
