/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests.temp.statistics;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TmfEventsStatisticsTest.class,
    TmfStateStatisticsTest.class
})
public class AllTests {}
