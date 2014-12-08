/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * SWTBot test suite for tmf.ui
 *
 * @author Matthew Khouzam
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestCustomTxtWizard.class,
        TestCustomXmlWizard.class,
        TracingPerspectiveChecker.class,
        TracingPerspectiveChecker.class,
        org.eclipse.tracecompass.tmf.ui.swtbot.tests.table.AllTests.class
})
public class AllTmfUISWTBotTests {
}
