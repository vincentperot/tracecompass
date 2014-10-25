/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Vincent Perot - Initial generation with CodePro tools
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.pcap.core.tests;

import org.eclipse.tracecompass.tmf.core.tests.shared.DebugSuite;
import org.junit.runner.RunWith;

/**
 * The class <code>AllTmfPcapCoreTests</code> builds a suite to run all the
 * tests.
 *
 * @author Vincent Perot
 */
@RunWith(DebugSuite.class)
@DebugSuite.SuiteClasses({
        org.eclipse.tracecompass.tmf.pcap.core.tests.analysis.AllTests.class,
        org.eclipse.tracecompass.tmf.pcap.core.tests.event.AllTests.class,
        org.eclipse.tracecompass.tmf.pcap.core.tests.trace.AllTests.class
})
public class AllTmfPcapCoreTests {

}
