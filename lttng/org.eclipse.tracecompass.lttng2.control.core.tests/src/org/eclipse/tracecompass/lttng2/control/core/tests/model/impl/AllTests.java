/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Alexandre Montplaisir - Port to JUnit4
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.control.core.tests.model.impl;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Runner for the test suite
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    BaseEventInfoTest.class,
    ChannelInfoTest.class,
    DomainInfoTest.class,
    EventInfoTest.class,
    FieldInfoTest.class,
    ProbeEventInfoTest.class,
    SessionInfoTest.class,
    TraceInfoTest.class,
    UstProviderInfoTest.class,
    SnapshotInfoTest.class
})
public class AllTests {

}
