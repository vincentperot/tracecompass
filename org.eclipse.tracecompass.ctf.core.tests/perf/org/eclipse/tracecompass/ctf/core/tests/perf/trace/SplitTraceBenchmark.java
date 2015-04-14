/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.perf.trace;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import org.eclipse.tracecompass.ctf.core.CTFReaderException;
import org.eclipse.tracecompass.ctf.core.tests.shared.CtfTestTrace;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.core.trace.CTFTraceWriter;
import org.junit.Test;

/**
 * Benchmark of the splitting a trace
 *
 * @author Bernd Hufmann
 *
 */
public class SplitTraceBenchmark {

    private static final int LOOP_COUNT = 1;

    /**
     * Benchmark reading the trace "kernel"
     */
    @Test
    public void testKernelTrace() {
        readTrace(CtfTestTrace.KERNEL, "trace-kernel", true);
    }

    private static void readTrace(CtfTestTrace testTrace, String testName, boolean inGlobalSummary) {
        assumeTrue(testTrace.exists());

        if (inGlobalSummary) {
        }

        for (int loop = 0; loop < LOOP_COUNT; loop++) {
            try {
                CTFTrace trace = testTrace.getTrace();
                long start = 1332166405241713987L + 4277170993912L - 1 ;
                long end = 1332166405241713987L + 4277170993912L + 2;

                CTFTraceWriter ctfWriter = new CTFTraceWriter(trace);
                ctfWriter.copyPackets(start, end, "/tmp/hallo");
            } catch (CTFReaderException e) {
                /* Should not happen if assumeTrue() passed above */
                fail("Test failed at iteration " + loop + ':' + e.getMessage());
            }
        }
    }
}
