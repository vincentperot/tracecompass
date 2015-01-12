/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.event.lookup;

import org.eclipse.tracecompass.tmf.core.tests.event.lookup.TmfCallsiteTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for org.eclipse.linuxtools.tmf.core.event.lookup
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TmfCallsiteTest.class
})
public class AllTests {

}
