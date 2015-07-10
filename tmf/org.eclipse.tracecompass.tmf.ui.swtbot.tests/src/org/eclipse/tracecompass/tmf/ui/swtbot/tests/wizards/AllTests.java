/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.wizards;

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;

/**
 * SWTBot test suite for testing of the TMF events table.
 */
public class AllTests extends TestSuite {
   public static TestSuite suite() {
       TestSuite s = new TestSuite("Test suite");
       for (int i = 0; i < 100; i++) {
           s.addTest(new JUnit4TestAdapter(TestDeleteDialog.class));
       }

       return s;
   }
}
