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

package org.eclipse.tracecompass.lttng2.kernel.core.tests.cpuusage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.Attributes;
import org.eclipse.tracecompass.lttng2.kernel.core.cpuusage.LttngKernelCpuUsageAnalysis;
import org.eclipse.tracecompass.lttng2.kernel.core.tests.Activator;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestHelper;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.xml.TmfXmlTraceStub;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for the {@link LttngKernelCpuUsageAnalysis} class
 *
 * @author Geneviève Bastien
 */
public class CpuUsageStateProviderTest {

    private static final String CPU_USAGE_FILE = "testfiles/cpu_analysis.xml";
    /**
     * The ID of the cpu usage analysis module for development traces
     */
    public static final String CPU_USAGE_ANALYSIS_ID = "lttng2.kernel.core.tests.cpuusage";

    private ITmfTrace fTrace;
    private LttngKernelCpuUsageAnalysis fModule;

    private static void deleteSuppFiles(ITmfTrace trace) {
        /* Remove supplementary files */
        File suppDir = new File(TmfTraceManager.getSupplementaryFileDir(trace));
        for (File file : suppDir.listFiles()) {
            file.delete();
        }
    }

    /**
     * Setup the trace for the tests
     */
    @Before
    public void setUp() {
        fTrace = new TmfXmlTraceStub();
        IPath filePath = Activator.getAbsoluteFilePath(CPU_USAGE_FILE);
        IStatus status = fTrace.validate(null, filePath.toOSString());
        if (!status.isOK()) {
            fail(status.getException().getMessage());
        }
        try {
            fTrace.initTrace(null, filePath.toOSString(), TmfEvent.class);
        } catch (TmfTraceException e) {
            fail(e.getMessage());
        }
        deleteSuppFiles(fTrace);
        ((TmfTrace) fTrace).traceOpened(new TmfTraceOpenedSignal(this, fTrace, null));
        fModule = fTrace.getAnalysisModuleOfClass(LttngKernelCpuUsageAnalysis.class, CPU_USAGE_ANALYSIS_ID);
        assertNotNull(fModule);
    }

    /**
     * Clean up
     */
    @After
    public void tearDown() {
        deleteSuppFiles(fTrace);
        fTrace.dispose();
    }

    /**
     * Test that the analysis executes without problems
     */
    @Test
    public void testAnalysisExecution() {
        /* Make sure the analysis hasn't run yet */
        assertNull(fModule.getStateSystem());

        /* Execute the analysis */
        assertTrue(TmfTestHelper.executeAnalysis(fModule));
        assertNotNull(fModule.getStateSystem());
    }

    /**
     * Test that the state system is returned with the expected results
     */
    @Test
    public void testReturnedStateSystem() {
        fModule.schedule();
        fModule.waitForCompletion();
        ITmfStateSystem ss = fModule.getStateSystem();
        assertNotNull(ss);
        assertEquals(1000000000L, ss.getStartTime());
        assertEquals(25000000000L, ss.getCurrentEndTime());

        try {
            int cpusQuark = ss.getQuarkAbsolute(Attributes.CPUS);

            /*
             * There should be 2 CPU entries: 0 and 1 and 3 process entries
             * under each
             */
            List<Integer> cpuQuarks = ss.getSubAttributes(cpusQuark, false);
            assertEquals(2, cpuQuarks.size());
            for (Integer cpuQuark : cpuQuarks) {
                assertEquals(3, ss.getSubAttributes(cpuQuark, false).size());
            }

            /* Proc 2 on CPU 0 should run from 1 to 20 seconds */
            int proc2Quark = ss.getQuarkAbsolute(Attributes.CPUS, "0", "2");
            ITmfStateInterval interval = ss.querySingleState(2000000000L, proc2Quark);
            assertEquals(1000000000L, interval.getStartTime());
            assertEquals(19999999999L, interval.getEndTime());

            /*
             * Query at the end and make sure all processes on all CPU have the
             * expected values
             */
            List<ITmfStateInterval> intervals = ss.queryFullState(25000000000L);
            Map<String, ITmfStateInterval> intervalMap = new HashMap<>();
            for (ITmfStateInterval oneInterval : intervals) {
                if (!oneInterval.getStateValue().isNull()) {
                    intervalMap.put(ss.getFullAttributePath(oneInterval.getAttribute()), oneInterval);
                }
            }
            assertEquals(6, intervalMap.size());
            ITmfStateInterval oneInterval = intervalMap.get("CPUs/0/1");
            assertNotNull(oneInterval);
            assertEquals(0L, oneInterval.getStateValue().unboxLong());
            oneInterval = intervalMap.get("CPUs/0/2");
            assertNotNull(oneInterval);
            assertEquals(19000000000L, oneInterval.getStateValue().unboxLong());
            oneInterval = intervalMap.get("CPUs/0/3");
            assertNotNull(oneInterval);
            assertEquals(5000000000L, oneInterval.getStateValue().unboxLong());
            oneInterval = intervalMap.get("CPUs/1/1");
            assertNotNull(oneInterval);
            assertEquals(5000000000L, oneInterval.getStateValue().unboxLong());
            oneInterval = intervalMap.get("CPUs/1/3");
            assertNotNull(oneInterval);
            assertEquals(6000000000L, oneInterval.getStateValue().unboxLong());
            oneInterval = intervalMap.get("CPUs/1/4");
            assertNotNull(oneInterval);
            assertEquals(8000000000L, oneInterval.getStateValue().unboxLong());

        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test the
     * {@link LttngKernelCpuUsageAnalysis#getCpuUsageInRange(long, long)}
     * method.
     */
    @Test
    public void testUsageInRange() {
        fModule.schedule();
        fModule.waitForCompletion();

        /* This range should query the total range */
        Map<String, Long> resultMap = fModule.getCpuUsageInRange(0L, 30000000000L);
        assertEquals(13, resultMap.size());
        Long value = resultMap.get("0/1");
        assertNotNull(value);
        assertEquals(0, value.longValue());
        value = resultMap.get("0/2");
        assertNotNull(value);
        assertEquals(19000000000L, value.longValue());
        value = resultMap.get("0/3");
        assertNotNull(value);
        assertEquals(5000000000L, value.longValue());
        value = resultMap.get("1/1");
        assertNotNull(value);
        assertEquals(5000000000L, value.longValue());
        value = resultMap.get("1/3");
        assertNotNull(value);
        assertEquals(6000000000L, value.longValue());
        value = resultMap.get("1/4");
        assertNotNull(value);
        assertEquals(13000000000L, value.longValue());
        value = resultMap.get("total");
        assertNotNull(value);
        assertEquals(48000000000L, value.longValue());
        value = resultMap.get("total/1");
        assertNotNull(value);
        assertEquals(5000000000L, value.longValue());
        value = resultMap.get("total/2");
        assertNotNull(value);
        assertEquals(19000000000L, value.longValue());
        value = resultMap.get("total/3");
        assertNotNull(value);
        assertEquals(11000000000L, value.longValue());
        value = resultMap.get("total/4");
        assertNotNull(value);
        assertEquals(13000000000L, value.longValue());
        value = resultMap.get("0");
        assertNotNull(value);
        assertEquals(24000000000L, value.longValue());
        value = resultMap.get("1");
        assertNotNull(value);
        assertEquals(24000000000L, value.longValue());

        /* Verify a range when a process runs at the start */
        resultMap = fModule.getCpuUsageInRange(22000000000L, 25000000000L);
        assertEquals(13, resultMap.size());
        value = resultMap.get("0/1");
        assertNotNull(value);
        assertEquals(0L, value.longValue());
        value = resultMap.get("0/2");
        assertNotNull(value);
        assertEquals(0L, value.longValue());
        value = resultMap.get("0/3");
        assertNotNull(value);
        assertEquals(3000000000L, value.longValue());
        value = resultMap.get("1/1");
        assertNotNull(value);
        assertEquals(0L, value.longValue());
        value = resultMap.get("1/3");
        assertNotNull(value);
        assertEquals(0L, value.longValue());
        value = resultMap.get("1/4");
        assertNotNull(value);
        assertEquals(3000000000L, value.longValue());
        value = resultMap.get("total");
        assertNotNull(value);
        assertEquals(6000000000L, value.longValue());
        value = resultMap.get("total/1");
        assertNotNull(value);
        assertEquals(0L, value.longValue());
        value = resultMap.get("total/2");
        assertNotNull(value);
        assertEquals(0L, value.longValue());
        value = resultMap.get("total/3");
        assertNotNull(value);
        assertEquals(3000000000L, value.longValue());
        value = resultMap.get("total/4");
        assertNotNull(value);
        assertEquals(3000000000L, value.longValue());
        value = resultMap.get("0");
        assertNotNull(value);
        assertEquals(3000000000L, value.longValue());
        value = resultMap.get("1");
        assertNotNull(value);
        assertEquals(3000000000L, value.longValue());

        /* Verify a range when a process runs at the end */
        resultMap = fModule.getCpuUsageInRange(1000000000L, 4000000000L);
        assertEquals(13, resultMap.size());
        value = resultMap.get("0/1");
        assertNotNull(value);
        assertEquals(0L, value.longValue());
        value = resultMap.get("0/2");
        assertNotNull(value);
        assertEquals(3000000000L, value.longValue());
        value = resultMap.get("0/3");
        assertNotNull(value);
        assertEquals(0L, value.longValue());
        value = resultMap.get("1/1");
        assertNotNull(value);
        assertEquals(0L, value.longValue());
        value = resultMap.get("1/3");
        assertNotNull(value);
        assertEquals(1000000000L, value.longValue());
        value = resultMap.get("1/4");
        assertNotNull(value);
        assertEquals(2000000000L, value.longValue());
        value = resultMap.get("total");
        assertNotNull(value);
        assertEquals(6000000000L, value.longValue());
        value = resultMap.get("total/1");
        assertNotNull(value);
        assertEquals(0L, value.longValue());
        value = resultMap.get("total/2");
        assertNotNull(value);
        assertEquals(3000000000L, value.longValue());
        value = resultMap.get("total/3");
        assertNotNull(value);
        assertEquals(1000000000L, value.longValue());
        value = resultMap.get("total/4");
        assertNotNull(value);
        assertEquals(2000000000L, value.longValue());
        value = resultMap.get("0");
        assertNotNull(value);
        assertEquals(3000000000L, value.longValue());
        value = resultMap.get("1");
        assertNotNull(value);
        assertEquals(3000000000L, value.longValue());

        /* Verify a range when a process runs at start and at the end */
        resultMap = fModule.getCpuUsageInRange(4000000000L, 13000000000L);
        assertEquals(13, resultMap.size());
        value = resultMap.get("0/1");
        assertNotNull(value);
        assertEquals(0L, value.longValue());
        value = resultMap.get("0/2");
        assertNotNull(value);
        assertEquals(9000000000L, value.longValue());
        value = resultMap.get("0/3");
        assertNotNull(value);
        assertEquals(0L, value.longValue());
        value = resultMap.get("1/1");
        assertNotNull(value);
        assertEquals(0L, value.longValue());
        value = resultMap.get("1/3");
        assertNotNull(value);
        assertEquals(5000000000L, value.longValue());
        value = resultMap.get("1/4");
        assertNotNull(value);
        assertEquals(4000000000L, value.longValue());
        value = resultMap.get("total");
        assertNotNull(value);
        assertEquals(18000000000L, value.longValue());
        value = resultMap.get("total/1");
        assertNotNull(value);
        assertEquals(0L, value.longValue());
        value = resultMap.get("total/2");
        assertNotNull(value);
        assertEquals(9000000000L, value.longValue());
        value = resultMap.get("total/3");
        assertNotNull(value);
        assertEquals(5000000000L, value.longValue());
        value = resultMap.get("total/4");
        assertNotNull(value);
        assertEquals(4000000000L, value.longValue());
        value = resultMap.get("0");
        assertNotNull(value);
        assertEquals(9000000000L, value.longValue());
        value = resultMap.get("1");
        assertNotNull(value);
        assertEquals(9000000000L, value.longValue());
    }
}
