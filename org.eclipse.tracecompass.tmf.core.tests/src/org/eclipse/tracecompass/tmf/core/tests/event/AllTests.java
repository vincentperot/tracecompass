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
 *   Francois Chouinard - Adjusted for new Event Model
 *   Alexandre Montplaisir - Port to JUnit4
 *   Patrick Tasse - Added TmfNanoTimestampTest
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.event;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for org.eclipse.linuxtools.tmf.core.event
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TmfEventFieldTest.class,
    TmfEventTest.class,
    TmfEventTypeTest.class,
    TmfNanoTimestampTest.class,
    TmfSimpleTimestampTest.class,
    TmfTimePreferencesTest.class,
    TmfTimeRangeTest.class,
    TmfTimestampDeltaTest.class,
    TmfTimestampTest.class,
    TmfTimestampFormatTest.class,
})
public class AllTests {

}
