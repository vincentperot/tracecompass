/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.synchronization;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for org.eclipse.linuxtools.tmf.core.synchronization
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ TsTransformTest.class,
        SyncTest.class,
        TsTransformFactoryTest.class,
        TsTransformTest.class,
        TsTransformFastTest.class,
        TimeOffsetTest.class })
public class AllTests {

}
