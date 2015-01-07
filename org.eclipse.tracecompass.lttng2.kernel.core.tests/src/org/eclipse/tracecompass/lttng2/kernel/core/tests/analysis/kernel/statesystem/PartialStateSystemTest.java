/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Bernd Hufmann - Use state system analysis module instead of factory
 ******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.tests.analysis.kernel.statesystem;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.KernelStateProvider;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout.LttngEventLayout;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * State system tests using a partial history.
 *
 * @author Alexandre Montplaisir
 */
public class PartialStateSystemTest extends StateSystemTest {

    private static final @NonNull String TEST_FILE_NAME = "test-partial";

    private static File stateFile;
    private static TestLttngKernelAnalysisModule module;

    /**
     * Test class setup
     */
    @BeforeClass
    public static void initialize() {
        if (!testTrace.exists()) {
            traceIsPresent = false;
            return;
        }
        traceIsPresent = true;

        stateFile = new File(TmfTraceManager.getSupplementaryFileDir(testTrace.getTrace()) + TEST_FILE_NAME);
        if (stateFile.exists()) {
            stateFile.delete();
        }

        module = new TestLttngKernelAnalysisModule(TEST_FILE_NAME);
        try {
            module.setTrace(testTrace.getTrace());
        } catch (TmfAnalysisException e) {
            fail();
        }
        module.schedule();
        assertTrue(module.waitForCompletion());

        fixture = module.getStateSystem();
    }

    /**
     * Class clean-up
     */
    @AfterClass
    public static void cleanup() {
        if (module != null) {
            module.dispose();
        }
        if (stateFile != null) {
            stateFile.delete();
        }
        if (fixture != null) {
            fixture.dispose();
        }
        module = null;
        fixture = null;
    }

    /**
     * Partial histories cannot get the intervals' end times. The fake value that
     * is returned is equal to the query's timestamp. So override this here
     * so that {@link #testFullQueryThorough} keeps working.
     */
    @Override
    protected long getEndTimes(int idx) {
        return interestingTimestamp1;
    }

    // ------------------------------------------------------------------------
    // Skip tests using single-queries (unsupported in partial history)
    // ------------------------------------------------------------------------

    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testSingleQuery1() {
        super.testSingleQuery1();
    }

    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testRangeQuery1() {
        super.testRangeQuery1();
    }

    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testRangeQuery2() {
        super.testRangeQuery2();
    }

    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testRangeQuery3() {
        super.testRangeQuery3();
    }

    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testSingleQueryInvalidTime1() throws TimeRangeException {
        super.testSingleQueryInvalidTime1();
    }

    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testSingleQueryInvalidTime2() throws TimeRangeException {
        super.testSingleQueryInvalidTime2();
    }

    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testRangeQueryInvalidTime1() throws TimeRangeException {
        super.testRangeQueryInvalidTime1();
    }

    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testRangeQueryInvalidTime2() throws TimeRangeException {
        super.testRangeQueryInvalidTime2();
    }

    @Ignore
    @Override
    @Test
    public void testFullQueryThorough() {
        try {
            List<ITmfStateInterval> state = fixture.queryFullState(interestingTimestamp1);
            int stateQuark = getPartialQuark();
            assertEquals(TestValues.size + 1, state.size()); // +1 for the checkpoint
            boolean passedCheckpoint = false;
            for (int i = 0; i < state.size(); i++) {
                /* Test each component of the intervals */
                if (i == stateQuark) {
                    passedCheckpoint = true;
                    i++;
                }
                int pos = i - (passedCheckpoint ? 1 : 0);

                assertEquals("quark " + i, getStartTimes(pos), state.get(i).getStartTime());
                assertEquals("quark " + i, getEndTimes(pos), state.get(i).getEndTime());
                assertEquals("quark " + i, i, state.get(i).getAttribute());
                assertEquals("quark " + i, getStateValues(pos), state.get(i).getStateValue());
            }

        } catch (StateSystemDisposedException e) {
            fail();
        }
    }

    @Ignore
    @Override
    @Test
    public void testFirstIntervalIsConsidered() {
        try {
            List<ITmfStateInterval> list = fixture.queryFullState(1331668248014135800L);
            ITmfStateInterval interval = list.get(233 + 1); // +1 for the checkpoint
            assertEquals(1331668247516664825L, interval.getStartTime());

            int valueInt = interval.getStateValue().unboxInt();
            assertEquals(1, valueInt);

        } catch (StateSystemDisposedException e) {
            fail();
        }
    }

    private static int getPartialQuark() {
        try {
            return fixture.getQuarkAbsolute("checkpoint");
        } catch (AttributeNotFoundException e) {
        }
        fail("Checkpoint not found!");
        return 0;
    }


    @NonNullByDefault
    private static class TestLttngKernelAnalysisModule extends TmfStateSystemAnalysisModule {

        private final String htFileName;

        /**
         * Constructor adding the views to the analysis
         * @param htFileName
         *      The History File Name
         */
        public TestLttngKernelAnalysisModule(String htFileName) {
            super();
            this.htFileName = htFileName;
        }

        @Override
        public void setTrace(@Nullable ITmfTrace trace) throws TmfAnalysisException {
            if (!(trace instanceof CtfTmfTrace)) {
                throw new IllegalStateException("TestLttngKernelAnalysisModule: trace should be of type CtfTmfTrace"); //$NON-NLS-1$
            }
            super.setTrace(trace);
        }

        @Override
        protected ITmfStateProvider createStateProvider() {
            return new KernelStateProvider(checkNotNull(getTrace()), LttngEventLayout.getInstance());
        }

        @Override
        protected StateSystemBackendType getBackendType() {
            return StateSystemBackendType.PARTIAL;
        }

        @Override
        protected String getSsFileName() {
            return htFileName;
        }

    }
}
