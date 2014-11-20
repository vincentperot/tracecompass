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

package org.eclipse.tracecompass.lttng2.kernel.core.tests.analysis.kernel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.StateValues;
import org.eclipse.tracecompass.lttng2.kernel.core.analysis.kernel.LttngKernelAnalysis;
import org.eclipse.tracecompass.lttng2.kernel.core.tests.Activator;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.xml.TmfXmlTraceStub;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test analysis-specific methods for the {@link LttngKernelAnalysis} class.
 *
 * @author Geneviève Bastien
 */
public class LttngKernelAnalysisMethodsTest {

    private static final @NonNull String LTTNG_KERNEL_FILE = "testfiles/lttng_kernel_analysis.xml";
    /**
     * The ID of the cpu usage analysis module for development traces
     */
    public static final String LTTNG_KERNEL_ANALYSIS_ID = "lttng2.kernel.core.tests.kernel.analysis";

    private ITmfTrace fTrace;
    private LttngKernelAnalysis fModule;

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
        IPath filePath = Activator.getAbsoluteFilePath(LTTNG_KERNEL_FILE);
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
        IAnalysisModule module = null;
        for (IAnalysisModule mod : TmfTraceUtils.getAnalysisModulesOfClass(fTrace, LttngKernelAnalysis.class)) {
            module = mod;
        }
        assertNotNull(module);
        module.schedule();
        module.waitForCompletion();
        fModule = TmfTraceUtils.getAnalysisModuleOfClass(fTrace, LttngKernelAnalysis.class, LTTNG_KERNEL_ANALYSIS_ID);
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
     * Test the {@link LttngKernelAnalysis#getThreadQuarks()} method
     */
    @Test
    public void testGetThreadQuarks() {
        Collection<Integer> threadQuarks = fModule.getThreadQuarks();
        assertEquals(7, threadQuarks.size());
    }

    /**
     * Test the {@link LttngKernelAnalysis#getThreadOnCpu(long, long)} method
     */
    @Test
    public void testGetThreadOnCpu() {

        /* Check with invalid timestamps */
        Integer tid = fModule.getThreadOnCpu(0, -1);
        assertEquals(LttngKernelAnalysis.NO_TID, tid);

        tid = fModule.getThreadOnCpu(0, 80);
        assertEquals(LttngKernelAnalysis.NO_TID, tid);

        /* Check with invalid cpus */
        tid = fModule.getThreadOnCpu(2, 20);
        assertEquals(LttngKernelAnalysis.NO_TID, tid);

        tid = fModule.getThreadOnCpu(-1, 20);
        assertEquals(LttngKernelAnalysis.NO_TID, tid);

        /* Check valid values */
        tid = fModule.getThreadOnCpu(0, 4);
        assertEquals(LttngKernelAnalysis.NO_TID, tid);

        tid = fModule.getThreadOnCpu(0, 15);
        assertEquals(LttngKernelAnalysis.NO_TID, tid);

        tid = fModule.getThreadOnCpu(1, 15);
        assertEquals(Integer.valueOf(11), tid);

        tid = fModule.getThreadOnCpu(1, 29);
        assertEquals(Integer.valueOf(20), tid);

        tid = fModule.getThreadOnCpu(1, 30);
        assertEquals(Integer.valueOf(21), tid);

        tid = fModule.getThreadOnCpu(0, 59);
        assertEquals(Integer.valueOf(11), tid);

        tid = fModule.getThreadOnCpu(1, 59);
        assertEquals(Integer.valueOf(30), tid);

        tid = fModule.getThreadOnCpu(0, 60);
        assertEquals(Integer.valueOf(11), tid);

        tid = fModule.getThreadOnCpu(1, 60);
        assertEquals(Integer.valueOf(21), tid);

    }

    /**
     * Test the {@link LttngKernelAnalysis#getPpid(Integer, long)} method
     */
    @Test
    public void testGetPpid() {

        /* Check with invalid timestamps */
        Integer ppid = fModule.getPpid(11, -1);
        assertEquals(LttngKernelAnalysis.NO_TID, ppid);

        ppid = fModule.getPpid(11, 80);
        assertEquals(LttngKernelAnalysis.NO_TID, ppid);

        /* Check with invalid cpus */
        ppid = fModule.getPpid(-4, 20);
        assertEquals(LttngKernelAnalysis.NO_TID, ppid);

        ppid = fModule.getPpid(12, 20);
        assertEquals(LttngKernelAnalysis.NO_TID, ppid);

        /* Check values with no parent */
        ppid = fModule.getPpid(10, 20);
        assertEquals(Integer.valueOf(0), ppid);

        ppid = fModule.getPpid(30, 60);
        assertEquals(Integer.valueOf(0), ppid);

        /* Check parent determined at statedump */
        ppid = fModule.getPpid(11, 4);
        assertEquals(LttngKernelAnalysis.NO_TID, ppid);

        ppid = fModule.getPpid(11, 5);
        assertEquals(Integer.valueOf(10), ppid);

        /* Check parent after process fork */
        ppid = fModule.getPpid(21, 25);
        assertEquals(Integer.valueOf(20), ppid);

        ppid = fModule.getPpid(21, 70);
        assertEquals(Integer.valueOf(20), ppid);

    }

    private static void testIntervals(String info, List<ITmfStateInterval> intervals, ITmfStateValue[] values) {
        assertEquals(info + " interval count", values.length, intervals.size());
        for (int i = 0; i < values.length; i++) {
            assertEquals(info + " interval " + i, values[i], intervals.get(i).getStateValue());
        }
    }

    /**
     * Test the
     * {@link LttngKernelAnalysis#getStatusIntervalsForThread(int, long, long, long, org.eclipse.core.runtime.IProgressMonitor)}
     * method
     */
    @Test
    public void testGetStatusIntervalsForThread() {

        IProgressMonitor monitor = new NullProgressMonitor();
        String process21 = "21";
        String process20 = "20";

        /* Find the quark for process proc21 */
        Integer quark21 = null;
        Integer quark20 = null;
        for (Integer threadQuark : fModule.getThreadQuarks()) {
            assertNotNull(threadQuark);
            if (fModule.getAttributeName(threadQuark).equals(process21)) {
                quark21 = threadQuark;
            }
            if (fModule.getAttributeName(threadQuark).equals(process20)) {
                quark20 = threadQuark;
            }
        }
        assertNotNull(quark21);
        assertNotNull(quark20);

        /* Check invalid time ranges */
        List<ITmfStateInterval> intervals = fModule.getStatusIntervalsForThread(quark21, -15, -5, 3, monitor);
        assertTrue(intervals.isEmpty());

        intervals = fModule.getStatusIntervalsForThread(quark21, 80, 1500000000L, 50, monitor);
        assertTrue(intervals.isEmpty());

        /* Check invalid quarks */
        intervals = fModule.getStatusIntervalsForThread(-1, 0, 70L, 3, monitor);
        assertTrue(intervals.isEmpty());

        intervals = fModule.getStatusIntervalsForThread(0, 0, 70L, 3, monitor);
        assertTrue(intervals.isEmpty());

        /* Check different time ranges and resolutions */
        ITmfStateValue[] values = {TmfStateValue.nullValue(), StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE,
                StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE, StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE,
                StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE};
        intervals = fModule.getStatusIntervalsForThread(quark21, 0, 70L, 3, monitor);
        testIntervals("tid 21 [0,70,3]", intervals, values);

        ITmfStateValue[] values2 = {TmfStateValue.nullValue(), StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE,
                StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE};
        intervals = fModule.getStatusIntervalsForThread(quark21, 1, 70L, 30, monitor);
        testIntervals("tid 21 [0,70,30]", intervals, values2);

        ITmfStateValue[] values3 = {StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE,
                StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE};
        intervals = fModule.getStatusIntervalsForThread(quark21, 25, 50L, 3, monitor);
        testIntervals("tid 21 [25,50,3]", intervals, values3);

        ITmfStateValue[] values4 = {TmfStateValue.nullValue(), StateValues.PROCESS_STATUS_WAIT_BLOCKED_VALUE,
                StateValues.PROCESS_STATUS_RUN_USERMODE_VALUE, StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE};
        intervals = fModule.getStatusIntervalsForThread(quark20, 0, 70L, 3, monitor);
        testIntervals("tid 20 [0,70,3]", intervals, values4);

        ITmfStateValue[] values5 = {TmfStateValue.nullValue(), StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE};
        intervals = fModule.getStatusIntervalsForThread(quark20, 1, 70L, 30, monitor);
        testIntervals("tid 20 [0,70,30]", intervals, values5);

        ITmfStateValue[] values6 = {StateValues.PROCESS_STATUS_RUN_USERMODE_VALUE,
                StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE};
        intervals = fModule.getStatusIntervalsForThread(quark20, 25, 50L, 3, monitor);
        testIntervals("tid 20 [25,50,3]", intervals, values6);

    }

}
