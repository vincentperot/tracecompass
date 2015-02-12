/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Alexandre Montplaisir - Port to JUnit4, enable CTF and statistics tests
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests;

import org.eclipse.tracecompass.tmf.core.tests.shared.DebugSuite;
import org.junit.runner.RunWith;

/**
 * Master test suite for TMF Core.
 */
@RunWith(DebugSuite.class)
@DebugSuite.SuiteClasses({
    TmfCorePluginTest.class,
    org.eclipse.tracecompass.tmf.core.tests.analysis.AllTests.class,
    org.eclipse.tracecompass.tmf.core.tests.callstack.AllTests.class,
    org.eclipse.tracecompass.tmf.core.tests.component.AllTests.class,
    org.eclipse.tracecompass.tmf.core.tests.event.AllTests.class,
    org.eclipse.tracecompass.tmf.core.tests.event.lookup.AllTests.class,
    org.eclipse.tracecompass.tmf.core.tests.filter.AllTests.class,
    org.eclipse.tracecompass.tmf.core.tests.model.AllTests.class,
    org.eclipse.tracecompass.tmf.core.tests.request.AllTests.class,
    org.eclipse.tracecompass.tmf.core.tests.signal.AllTests.class,
    org.eclipse.tracecompass.tmf.core.tests.statesystem.AllTests.class,
    org.eclipse.tracecompass.tmf.core.tests.statesystem.mipmap.AllTests.class,
    org.eclipse.tracecompass.tmf.core.tests.synchronization.AllTests.class,
    org.eclipse.tracecompass.tmf.core.tests.trace.AllTests.class,
    org.eclipse.tracecompass.tmf.core.tests.trace.indexer.AllTests.class,
    org.eclipse.tracecompass.tmf.core.tests.trace.indexer.checkpoint.AllTests.class,
    org.eclipse.tracecompass.tmf.core.tests.trace.location.AllTests.class,
    org.eclipse.tracecompass.tmf.core.tests.trace.text.AllTests.class,
    org.eclipse.tracecompass.tmf.core.tests.uml2sd.AllTests.class,
    org.eclipse.tracecompass.tmf.core.tests.util.AllTests.class
})
public class AllTmfCoreTests {

}
