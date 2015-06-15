/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial implementation
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.ust.core.tests.callstack;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for org.eclipse.tracecompass.lttng2.ust.core.tests.trace.callstack
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    LttngUstCallStackProviderFastTest.class,
    LttngUstCallStackProviderTest.class
})
public class AllTests {

}
