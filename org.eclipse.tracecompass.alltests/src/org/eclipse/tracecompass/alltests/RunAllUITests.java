/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.alltests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Master test suite for all Linux Tools LTTng unit tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    org.eclipse.tracecompass.gdbtrace.ui.tests.AllGdbTraceUITests.class,
    org.eclipse.tracecompass.lttng2.control.ui.tests.AllTests.class,
    org.eclipse.tracecompass.lttng2.ust.ui.tests.AllTests.class,
    org.eclipse.tracecompass.tmf.analysis.xml.ui.tests.AllAnalysisXmlUiTests.class,
    org.eclipse.tracecompass.tmf.ui.tests.AllTmfUITests.class,
})
public class RunAllUITests {

}
