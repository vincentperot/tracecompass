/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.ui.swtbot.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

import java.io.File;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.SWTBotUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * SWTBot stress test for opening and closing of traces.
 *
 * @author Bernd Hufmann
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class OpenTraceStressTest {

    private static final String TRACE_TYPE = "org.eclipse.linuxtools.lttng2.kernel.tracetype";
    private static final String KERNEL_PERSPECTIVE_ID = "org.eclipse.linuxtools.lttng2.kernel.ui.perspective";

    private static CtfTmfTestTrace ctt = CtfTmfTestTrace.SYNC_DEST;

    private static final String TRACE_PROJECT_NAME = "test";

    private static SWTWorkbenchBot fBot;

    /**
     * Test Class setup
     */
    @BeforeClass
    public static void init() {
        SWTBotUtil.failIfUIThread();

        /* Set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */

        fBot = new SWTWorkbenchBot();

        /* Close welcome view */
        SWTBotUtil.closeView("Welcome", fBot);

        /* Switch perspectives */
        SWTBotUtil.switchToPerspective(KERNEL_PERSPECTIVE_ID);

        /* Finish waiting for eclipse to load */
        SWTBotUtil.waitForJobs();
    }

    /**
     * Main test case
     */
    @Test
    public void test() {
        SWTBotUtil.createProject(TRACE_PROJECT_NAME);

        File fTestFile = new File(ctt.getPath());

        String path = fTestFile.getAbsolutePath();

        assertNotNull(fTestFile);
        assumeTrue(fTestFile.exists());

        /*
         *  This opening and closing of traces will trigger several threads for analysis which
         *  will be closed concurrently. There used to be a concurrency bug (447434) which should
         *  be fixed by now and this test should run without any exceptions.
         *
         *  Since the failure depends on timing it only happened sometimes before the bug fix
         *  using this test.
         */
        for(int i = 0; i < 10; i++) {
            SWTBotUtil.openTrace(TRACE_PROJECT_NAME, path, TRACE_TYPE, false);
            SWTBotUtil.openTrace(TRACE_PROJECT_NAME, path, TRACE_TYPE, false);
            SWTBotUtil.openTrace(TRACE_PROJECT_NAME, path, TRACE_TYPE, false);
            SWTBotUtil.openTrace(TRACE_PROJECT_NAME, path, TRACE_TYPE, false);
            SWTBotUtil.openTrace(TRACE_PROJECT_NAME, path, TRACE_TYPE, false);
            // Add little delay so that treads have a chance to start
            SWTBotUtil.delay(1000);
            fBot.closeAllEditors();
        }
        SWTBotUtil.deleteProject(TRACE_PROJECT_NAME, fBot);
    }

}
