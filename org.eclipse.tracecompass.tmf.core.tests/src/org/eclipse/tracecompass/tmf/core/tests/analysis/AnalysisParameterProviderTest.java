/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisParameterProvider;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAnalysisManager;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.tests.stubs.analysis.TestAnalysis;
import org.eclipse.tracecompass.tmf.tests.stubs.analysis.TestAnalysisParameterProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Multimap;

/**
 * Test the TmfAbstractParameterProvider class
 *
 * @author Geneviève Bastien
 */
public class AnalysisParameterProviderTest {

    private static IAnalysisModuleHelper getModuleHelper(@NonNull String moduleId) {
        Multimap<String, IAnalysisModuleHelper> helpers = TmfAnalysisManager.getAnalysisModules();
        assertEquals(1, helpers.get(moduleId).size());
        return helpers.get(moduleId).iterator().next();
    }

    /**
     * Registers the parameter provider
     */
    @Before
    public void setup() {
        TmfAnalysisManager.registerParameterProvider(AnalysisManagerTest.MODULE_PARAM, TestAnalysisParameterProvider.class);
    }

    /**
     * Cleanup the trace after testing
     */
    @After
    public void cleanupTrace() {
        TmfTestTrace.A_TEST_10K.dispose();
    }

    /**
     * Test that the provider's value is used
     */
    @Test
    public void testProviderTmfTrace() {
        ITmfTrace trace = TmfTestTrace.A_TEST_10K.getTrace();
        /* Make sure the value is set to null */
        IAnalysisModuleHelper helper = getModuleHelper(AnalysisManagerTest.MODULE_PARAM);
        assertNotNull(helper);
        IAnalysisModule module = null;
        try {
            module = helper.newModule(trace);

            assertEquals(10, module.getParameter(TestAnalysis.PARAM_TEST));

            /* Change the value of the parameter in the provider */
            List<IAnalysisParameterProvider> providers = TmfAnalysisManager.getParameterProviders(module, trace);
            assertEquals(1, providers.size());
            TestAnalysisParameterProvider provider = (TestAnalysisParameterProvider) providers.get(0);
            provider.setValue(5);
            assertEquals(5, module.getParameter(TestAnalysis.PARAM_TEST));

        } catch (TmfAnalysisException e) {
            fail(e.getMessage());
        } finally {
            if (module != null) {
                module.dispose();
            }
        }
    }

}
