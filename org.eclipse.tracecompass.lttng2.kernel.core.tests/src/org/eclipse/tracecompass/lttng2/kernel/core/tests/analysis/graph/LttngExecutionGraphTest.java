/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.tests.analysis.graph;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfGraph;
import org.eclipse.tracecompass.analysis.graph.core.building.TmfGraphBuilderModule;
import org.eclipse.tracecompass.lttng2.kernel.core.tests.Activator;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.xml.TmfXmlTraceStub;
import org.junit.Test;

/**
 * Test that the execution graph is built correctly
 *
 * @author Geneviève Bastien
 */
public class LttngExecutionGraphTest {

    /**
     * Setup the trace for the tests
     *
     * @param traceFile
     *            File name relative to this plugin for the trace file to load
     * @return The trace with its graph module executed
     */
    public ITmfTrace setUpTrace(String traceFile) {
        ITmfTrace trace = new TmfXmlTraceStub();
        IPath filePath = Activator.getAbsoluteFilePath(traceFile);
        IStatus status = trace.validate(null, filePath.toOSString());
        if (!status.isOK()) {
            fail(status.getException().getMessage());
        }
        try {
            trace.initTrace(null, filePath.toOSString(), TmfEvent.class);
        } catch (TmfTraceException e) {
            fail(e.getMessage());
        }
        ((TmfTrace) trace).traceOpened(new TmfTraceOpenedSignal(this, trace, null));
        IAnalysisModule module = null;
        for (IAnalysisModule mod : TmfTraceUtils.getAnalysisModulesOfClass(trace, TmfGraphBuilderModule.class)) {
            module = mod;
        }
        assertNotNull(module);
        module.schedule();
        module.waitForCompletion();
        return trace;
    }

    /**
     * Test the graph building with sched events only
     */
    @Test
    public void testSchedEvents() {
        ITmfTrace trace = setUpTrace("testfiles/graph/sched_only.xml");
        assertNotNull(trace);

        TmfGraphBuilderModule module = null;
        for (TmfGraphBuilderModule mod : TmfTraceUtils.getAnalysisModulesOfClass(trace, TmfGraphBuilderModule.class)) {
            module = mod;
        }
        assertNotNull(module);

        TmfGraph graph = module.getGraph();
        assertNotNull(graph);
    }
}
