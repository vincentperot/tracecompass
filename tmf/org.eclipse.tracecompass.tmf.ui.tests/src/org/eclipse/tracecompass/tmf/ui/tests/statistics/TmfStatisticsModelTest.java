/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexis Cabana-Loriaux <alex021994@gmail.com> - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.tests.statistics;

import static org.junit.Assert.fail;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.statistics.ITmfStatistics;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStatisticsModule;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestHelper;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.tracecompass.tmf.ui.views.statistics.TmfStatisticsModel;
import org.eclipse.tracecompass.tmf.ui.views.statistics.TmfStatisticsView;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class runs model tests for the pie charts in the statistics view
 *
 * @author Alexis Cabana-Loriaux <alex021994@gmail.com>
 *
 */
public class TmfStatisticsModelTest {

    // ------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------
    /** The model to test */
    private static TmfStatisticsModel model;

    /** The dummy view used to construct the model object to test */
    private static TmfStatisticsView dummyView;

    /** The number of times the tests is executed */
    //private static final int LOOP_COUNT = 25;

    /** The variables used to run the test */
    private static @NonNull ITmfTimestamp START_QUERY = new TmfTimestamp();
    private static @NonNull ITmfTimestamp END_QUERY = new TmfTimestamp();
    // ------------------------------------------------------------------
    // Initialize and take down
    // ------------------------------------------------------------------
    /**
     * Initialize the model
     */
    @BeforeClass
    public static void buildUp() {
        dummyView = new TmfStatisticsView();
        //have to get the reference of model built by the view
        model = dummyView.getModel();
        model.getClass();
    }

    /**
     * Tear down the tests
     */
    @AfterClass
    public static void tearDown() {
        dummyView.dispose();
    }

    // ------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------

    /**
     * Test that checks the validity of a query of the model in the statesystem
     */
    @Test
    public void testPartialDataQueryDjangoHttpd() {
        executeQuery(CtfTmfTestTrace.DJANGO_HTTPD.getTrace(), false, new TmfTimeRange(START_QUERY, END_QUERY));
    }

    /**
     * Test that checks the validity of a query of the model in the statesystem
     */
    @Test
    public void testGlobalDataQueryDjangoHttpd() {
        executeQuery(CtfTmfTestTrace.DJANGO_HTTPD.getTrace(), true, new TmfTimeRange(START_QUERY, END_QUERY));
    }

    /**
     *
     */
    private static void executeQuery(final ITmfTrace trace, final boolean isGlobal, TmfTimeRange queryRange) {
        /*Get the right time-range*/
        TmfTimeRange range = queryRange;
        if(isGlobal){
            range = trace.getTimeRange();
        }

        /* initialize the statmodule */
        TmfStatisticsModule module = new TmfStatisticsModule();
        module.setId("test");

        try {
            trace.initTrace(null, trace.getPath(), CtfTmfEvent.class);
            module.setTrace(trace);
        } catch (TmfTraceException | TmfAnalysisException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        TmfTestHelper.executeAnalysis(module);
        ITmfStatistics stats = module.getStatistics();
        if (stats == null) {
            throw new IllegalStateException();
        }
        Iterable<IAnalysisModule> modules = trace.getAnalysisModules();
        for(IAnalysisModule m :  modules){
            m.getClass();
        }
        range.getClass();
    }
}