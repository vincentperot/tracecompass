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
import org.junit.Test;

/**
 * Benchmark of the splitting a trace
 *
 * @author Bernd Hufmann
 *
 */
public class SplitTraceBenchmark {

//    private static final String TEST_SUITE_NAME = "CTF Read Benchmark";
//    private static final String TEST_ID = "org.eclipse.linuxtools#" + TEST_SUITE_NAME;
    private static final int LOOP_COUNT = 1;

    /**
     * Benchmark reading the trace "kernel"
     */
    @Test
    public void testKernelTrace() {
        readTrace(CtfTestTrace.KERNEL, "trace-kernel", true);
    }

//    /**
//     * Benchmark reading the bigger trace "kernel_vm"
//     */
//    @Test
//    public void testKernelVmTrace() {
//        readTrace(CtfTestTrace.KERNEL_VM, "trace-kernel-vm", false);
//    }

    private static void readTrace(CtfTestTrace testTrace, String testName, boolean inGlobalSummary) {
        assumeTrue(testTrace.exists());

//        Performance perf = Performance.getDefault();
//        PerformanceMeter pm = perf.createPerformanceMeter(TEST_ID + '#' + testName);
//        perf.tagAsSummary(pm, TEST_SUITE_NAME + ':' + testName, Dimension.CPU_TIME);

        if (inGlobalSummary) {
//            perf.tagAsGlobalSummary(pm, TEST_SUITE_NAME + ':' + testName, Dimension.CPU_TIME);
        }

        for (int loop = 0; loop < LOOP_COUNT; loop++) {
//            pm.start();
            try {
                CTFTrace trace = testTrace.getTrace();
                long start = 1332166405241713987L + 4277170993912L - 1 ;
                long end = 1332166405241713987L + 4277170993912L + 2;

//                long end = 4277258593522L - 1;

//                long start = 0 ;
//                long end = Long.MAX_VALUE;

                trace.crop(start, end, "/tmp/hallo");
//                    while (traceReader.hasMoreEvents()) {
//                        EventDefinition ed = traceReader.getCurrentEventDef();
//                        /* Do something with the event */
//                        ed.getCPU();
//                        traceReader.advance();
            } catch (CTFReaderException e) {
                /* Should not happen if assumeTrue() passed above */
                fail("Test failed at iteration " + loop + ':' + e.getMessage());
            }
//            pm.stop();
        }
//        pm.commit();
    }
}
