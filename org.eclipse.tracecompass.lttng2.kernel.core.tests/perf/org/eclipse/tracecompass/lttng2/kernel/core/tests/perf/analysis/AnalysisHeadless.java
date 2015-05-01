/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *   Alexandre Montplaisir - Convert to org.eclipse.test.performance test
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.tests.perf.analysis;

import java.io.File;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.KernelAnalysis;
import org.eclipse.tracecompass.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestHelper;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.junit.Test;

/**
 * This is a test of the time to build a kernel state system
 *
 * @author Genevieve Bastien
 */
public class AnalysisHeadless {

    /**
     * Run the benchmark with "trace2"
     * @param args args
     */

    public static void main(String[] args) {
        new AnalysisHeadless().runTest();
    }

    /**
     * Runs the test
     */
    @Test
    public void runTest() {
        try (LttngKernelTrace trace = new LttngKernelTrace();) {
            trace.initTrace(null, "/home/ematkho/lttng-traces/stress-20150408-172509/kernel", CtfTmfEvent.class);
            runTest(trace, "test");
        } catch (TmfTraceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void runTest(@NonNull LttngKernelTrace trace, String testName) {
        KernelAnalysis module = null;
        module = new KernelAnalysis();
        module.setId("test");
        try {
            module.setTrace(trace);
            System.out.println("Started");
            long ts = System.currentTimeMillis();

            TmfTestHelper.executeAnalysis(module);
            long time = System.currentTimeMillis() - ts;
            System.out.println("Time taken: " + time + " ms");
            /*
             * Delete the supplementary files, so that the next iteration
             * rebuilds the state system.
             */
            File suppDir = new File(TmfTraceManager.getSupplementaryFileDir(trace));
            for (File file : suppDir.listFiles()) {
                file.delete();
            }

        } catch (TmfAnalysisException e) {
        } finally {
            module.dispose();
        }
    }
}
