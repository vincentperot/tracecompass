/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.tests.perf.event.matching;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

import java.util.Collections;

import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.event.matching.TcpEventMatching;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.event.matching.TcpLttngEventMatching;
import org.eclipse.tracecompass.tmf.core.event.matching.TmfEventMatching;
import org.eclipse.tracecompass.tmf.core.synchronization.SynchronizationAlgorithm;
import org.eclipse.tracecompass.tmf.core.synchronization.SynchronizationManager;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Benchmark trace synchronization
 *
 * @author Geneviève Bastien
 */
public class TraceSynchronizationBenchmark {

    private static final String TEST_ID = "org.eclipse.linuxtools#Trace synchronization#";
    private static final String TIME = " (time)";
    private static final String MEMORY = " (memory usage)";
    private static final String TEST_SUMMARY = "Trace synchronization";

    /**
     * Initialize some data
     */
    @BeforeClass
    public static void setUp() {
        TmfEventMatching.registerMatchObject(new TcpEventMatching());
        TmfEventMatching.registerMatchObject(new TcpLttngEventMatching());
    }

    /**
     * Run the benchmark with 2 small traces
     */
    @Test
    public void testSmallTraces() {
        assumeTrue(CtfTmfTestTrace.SYNC_SRC.exists());
        assumeTrue(CtfTmfTestTrace.SYNC_DEST.exists());
        try (CtfTmfTrace trace1 = CtfTmfTestTrace.SYNC_SRC.getTrace();
                CtfTmfTrace trace2 = CtfTmfTestTrace.SYNC_DEST.getTrace();) {
            ITmfTrace[] traces = { trace1, trace2 };
            TmfExperiment experiment = new TmfExperiment(CtfTmfEvent.class, "Test experiment", traces, TmfExperiment.DEFAULT_INDEX_PAGE_SIZE, null);
            runCpuTest(experiment, "Match TCP events", 40);
        }
    }

    /**
     * Run the benchmark with 3 bigger traces
     */
    @Test
    public void testDjangoTraces() {
        assumeTrue(CtfTmfTestTrace.DJANGO_CLIENT.exists());
        assumeTrue(CtfTmfTestTrace.DJANGO_DB.exists());
        assumeTrue(CtfTmfTestTrace.DJANGO_HTTPD.exists());
        try (CtfTmfTrace trace1 = CtfTmfTestTrace.DJANGO_CLIENT.getTrace();
                CtfTmfTrace trace2 = CtfTmfTestTrace.DJANGO_DB.getTrace();
                CtfTmfTrace trace3 = CtfTmfTestTrace.DJANGO_HTTPD.getTrace();) {
            ITmfTrace[] traces = { trace1, trace2, trace3 };
            TmfExperiment experiment = new TmfExperiment(CtfTmfEvent.class, "Test experiment", traces, TmfExperiment.DEFAULT_INDEX_PAGE_SIZE, null);
            runCpuTest(experiment, "Django traces", 10);
            runMemoryTest(experiment, "Django traces", 10);
        }
    }

    private static void runCpuTest(TmfExperiment experiment, String testName, int loop_count) {
        Performance perf = Performance.getDefault();
        PerformanceMeter pm = perf.createPerformanceMeter(TEST_ID + testName + TIME);
        perf.tagAsSummary(pm, TEST_SUMMARY + ':' + testName + TIME, Dimension.CPU_TIME);

        for (int i = 0; i < loop_count; i++) {
            pm.start();
            SynchronizationManager.synchronizeTraces(null, Collections.<ITmfTrace> singleton(experiment), true);
            pm.stop();
        }
        pm.commit();

    }

    /* Benchmark memory used by the algorithm */
    private static void runMemoryTest(TmfExperiment experiment, String testName, int loop_count) {
        Performance perf = Performance.getDefault();
        PerformanceMeter pm = perf.createPerformanceMeter(TEST_ID + testName + MEMORY);
        perf.tagAsSummary(pm, TEST_SUMMARY + ':' + testName + MEMORY, Dimension.USED_JAVA_HEAP);

        for (int i = 0; i < loop_count; i++) {

            System.gc();
            pm.start();
            SynchronizationAlgorithm algo = SynchronizationManager.synchronizeTraces(null, Collections.<ITmfTrace> singleton(experiment), true);
            assertNotNull(algo);

            System.gc();
            pm.stop();
        }
        pm.commit();
    }
}
