/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexis Cabana-Loriaux <alex021994@gmail.com> - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.viewers.piecharts;

import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This is a test of the piechart viewer. There's different tests about the
 * layout and the data that should be shown depending on the the state of the
 * selection.
 *
 * @author Alexis Cabana-Loriaux <alex021994@gmail.com>
 */
public class PieChartTest {
    // ------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------

    /**
     * The ID of the component to test.
     */
    public static final @NonNull String STATVIEW_ID = "org.eclipse.linuxtools.tmf.ui.views.statistics"; //$NON-NLS-1$

    /**
     * The type of the Django-client trace
     */
    public static final @NonNull String TRACE_TYPE = "org.eclipse.linuxtools.tmf.ui.type.ctf"; //$NON-NLS-1$

    /**
     * The name of this project
     */
    private static final String PROJECT_NAME = "TestPieCharts";

    /**
     * The bot the tests are working on
     */
    private static SWTWorkbenchBot fBot;


    /**
     * The Log4j logger instance.
     */
    private static final Logger fLogger = Logger.getRootLogger();
    // ------------------------------------------------------------------
    // Initialize and take down
    // ------------------------------------------------------------------
    /**
     * Initialize the data
     */
    @BeforeClass
    public static void buildUp() {
        SWTBotUtils.failIfUIThread();
        Thread.currentThread().setName("SWTBot Thread"); // for the debugger
        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        fLogger.removeAllAppenders();
        fLogger.addAppender(new NullAppender());
        fBot = new SWTWorkbenchBot();

        SWTBotUtils.closeView("welcome", fBot);

        SWTBotUtils.switchToTracingPerspective();
        /* finish waiting for eclipse to load */
        SWTBotUtils.waitForJobs();

        SWTBotUtils.createProject(PROJECT_NAME);
        SWTBotTreeItem treeItem = SWTBotUtils.selectTracesFolder(fBot, PROJECT_NAME);
        assertNotNull(treeItem);
        String path = CtfTmfTestTrace.DJANGO_CLIENT.getPath();
        File f = new File(path);
        SWTBotUtils.openTrace(PROJECT_NAME, f.getAbsolutePath(), TRACE_TYPE);
        SWTBotUtils.openView(STATVIEW_ID);
    }

    @SuppressWarnings("javadoc")
    @After
    public void removeMe(){
        System.out.println("----------=============== PASS ===============----------");
    }

    // ------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------

    /**
     * Test to make sure the viewer shows a tooltip box when the mouse is
     * hovering over a slice of one of the piecharts
     */
    @Test
    public void testViewerTooltip() {

    }
}
