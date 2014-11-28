/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.tests.stubs.analysis.TestAnalysis;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * Test suite for {@link TmfTraceUtils}
 */
public class TmfTraceUtilsTest {

    private static final TmfTestTrace TEST_TRACE = TmfTestTrace.A_TEST_10K;

    private TmfTrace fTrace;

    // ------------------------------------------------------------------------
    // Test trace class definition
    // ------------------------------------------------------------------------

    private static class TmfTraceStubWithAspects extends TmfTraceStub {

        private static final Collection<ITmfEventAspect> EVENT_ASPECTS;
        static {
            ImmutableList.Builder<ITmfEventAspect> builder = ImmutableList.builder();
            builder.add(new TmfCpuAspect() {
                @Override
                public String getFilterId() {
                    return null;
                }
                @Override
                public String resolve(ITmfEvent event) {
                    return "1";
                }
            });
            builder.addAll(TmfTrace.BASE_ASPECTS);
            EVENT_ASPECTS = builder.build();
        }

        public TmfTraceStubWithAspects(String path) throws TmfTraceException {
            super(path, ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, false, null);
        }

        @Override
        public Iterable<ITmfEventAspect> getEventAspects() {
            return EVENT_ASPECTS;
        }

    }

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * Test setup
     */
    @Before
    public void setUp() {
        try {
            final URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(TEST_TRACE.getFullPath()), null);
            final File test = new File(FileLocator.toFileURL(location).toURI());
            fTrace = new TmfTraceStubWithAspects(test.toURI().getPath());
            TmfSignalManager.deregister(fTrace);
            fTrace.indexTrace(true);
        } catch (final TmfTraceException | URISyntaxException | IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test cleanup
     */
    @After
    public void tearDown() {
        fTrace.dispose();
        fTrace = null;
    }

    // ------------------------------------------------------------------------
    // Test methods
    // ------------------------------------------------------------------------

    /**
     * Test the {@link TmfTraceUtils#getAnalysisModuleOfClass} method.
     */
    @Test
    public void testGetModulesByClass() {
        /* There should not be any modules at this point */
        Iterable<IAnalysisModule> modules = fTrace.getAnalysisModules();
        assertFalse(modules.iterator().hasNext());

        /* Open the trace, the modules should be populated */
        fTrace.traceOpened(new TmfTraceOpenedSignal(this, fTrace, null));

        modules = fTrace.getAnalysisModules();
        Iterable<TestAnalysis> testModules = TmfTraceUtils.getAnalysisModulesOfClass(fTrace, TestAnalysis.class);
        assertTrue(modules.iterator().hasNext());
        assertTrue(testModules.iterator().hasNext());

        /*
         * Make sure all modules of type TestAnalysis are returned in the second
         * call
         */
        for (IAnalysisModule module : modules) {
            if (module instanceof TestAnalysis) {
                IAnalysisModule otherModule = fTrace.getAnalysisModule(module.getId());
                assertNotNull(otherModule);
                assertTrue(otherModule.equals(module));
            }
        }
    }

    /**
     * Test the {@link TmfTraceUtils#getEventAspectsOfClass} method.
     */
    @Test
    public void testGetEventAspectsOfClass() {
        /* Our custom trace type adds 1 aspect in addition to the base ones */
        Iterable<ITmfEventAspect> aspects = TmfTraceUtils.getEventAspectsOfClass(fTrace, ITmfEventAspect.class);
        Collection<ITmfEventAspect> aspectColl = ImmutableList.copyOf(aspects);
        assertEquals(TmfTrace.BASE_ASPECTS.size() + 1, aspectColl.size());

        /* Make sure there is one and only one CpuAspect */
        Iterable<TmfCpuAspect> cpuAspects = TmfTraceUtils.getEventAspectsOfClass(fTrace, TmfCpuAspect.class);
        Iterator<TmfCpuAspect> iter = cpuAspects.iterator();
        assertNotNull(iter.next());
        assertFalse(iter.hasNext());
    }
}
