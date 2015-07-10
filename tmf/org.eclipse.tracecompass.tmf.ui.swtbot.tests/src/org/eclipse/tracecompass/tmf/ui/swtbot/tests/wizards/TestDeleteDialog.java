/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation.
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.swtbot.tests.wizards;

import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * Import operation for gz traces
 *
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class TestDeleteDialog {
    private static final String PROJECT_NAME = "Tracing";
    private SWTWorkbenchBot fBot;

    /** The Log4j logger instance. */
    protected static final Logger fLogger = Logger.getRootLogger();

    /**
     * create a gzip file
     */
    @BeforeClass
    public static void init() {
        SWTBotUtils.failIfUIThread();
        fLogger.removeAllAppenders();
        fLogger.addAppender(new NullAppender());
    }

    /**
     * create the project
     */
    @Before
    public void setup() {
        createProject();
        SWTBotPreferences.TIMEOUT = 20000;
    }

    /**
     * cleanup
     */
    @AfterClass
    public static void destroy() {
        fLogger.removeAllAppenders();
    }

    /**
     * Tear down the test
     */
    @After
    public void tearDown() {
        SWTBotUtils.deleteProject(PROJECT_NAME, fBot);
    }

    private void createProject() {
        fBot = new SWTWorkbenchBot();
        /* Close welcome view */
        SWTBotUtils.closeView("Welcome", fBot);

        SWTBotUtils.createProject(PROJECT_NAME);
    }

    /**
     * Test the delete project dialog
     */
    @Test
    public void test() {
    }
}
