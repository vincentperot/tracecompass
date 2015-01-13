/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial implementation
 ******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.tests.analysis.kernel.statesystem;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Runner for the LTTng kernel state system tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    LttngKernelStateProviderTest.class,
    PartialStateSystemTest.class,
    StateSystemFullHistoryTest.class,
    StateSystemInMemoryTest.class
})
public class TestAll {

}
