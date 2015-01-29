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
package org.eclipse.tracecompass.tmf.ui.swtbot.tests.viewers.events;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.views.colors.ColorsView;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for Colors views in trace compass
 */
public class ColorViewTest {

    private static final String XMLSTUB_ID = "org.eclipse.linuxtools.tmf.core.tests.xmlstub";

    private static final String TRACE_START = "<trace>";
    private static final String EVENT_BEGIN = "<event timestamp=\"";
    private static final String EVENT_MIDDLE = " \" name=\"event\"><field name=\"field\" value=\"";
    private static final String EVENT_END = "\" type=\"int\" />" + "</event>";
    private static final String TRACE_END = "</trace>";

    private static final String TEST_FOR_FILTERING = "TestForFiltering";

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();
    private static SWTWorkbenchBot fBot;

    private static String makeEvent(int ts, int val) {
        return EVENT_BEGIN + Integer.toString(ts) + EVENT_MIDDLE + Integer.toString(val) + EVENT_END + "\n";
    }

    private static File FILE_LOCATION;

    /**
     * Initialization, creates a temp trace
     *
     * @throws IOException
     *             should not happen
     */
    @BeforeClass
    public static void init() throws IOException {
        SWTBotUtils.failIfUIThread();
        Thread.currentThread().setName("SWTBot Thread"); // for the debugger
        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout()));
        fBot = new SWTWorkbenchBot();

        SWTBotUtils.closeView("welcome", fBot);

        SWTBotUtils.switchToTracingPerspective();
        /* finish waiting for eclipse to load */
        SWTBotUtils.waitForJobs();
        FILE_LOCATION = File.createTempFile("sample", ".xml");
        try (BufferedRandomAccessFile braf = new BufferedRandomAccessFile(FILE_LOCATION, "rw")) {
            braf.writeBytes(TRACE_START);
            for (int i = 0; i < 100; i++) {
                braf.writeBytes(makeEvent(i * 100, i % 4));
            }
            braf.writeBytes(TRACE_END);
        }
    }

    /**
     * Open a trace in an editor
     */
    @Before
    public void beforeTest() {
        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        SWTBotUtils.createProject(TEST_FOR_FILTERING);
        SWTBotTreeItem treeItem = SWTBotUtils.selectTracesFolder(bot, TEST_FOR_FILTERING);
        assertNotNull(treeItem);
        SWTBotUtils.openTrace(TEST_FOR_FILTERING, FILE_LOCATION.getAbsolutePath(), XMLSTUB_ID);
        openView(ColorsView.ID);
    }

    /**
     * Delete the file
     */
    @AfterClass
    public static void cleanUp() {
        FILE_LOCATION.delete();
    }

    /**
     * Close the editor
     */
    @After
    public void tearDown() {
        fBot.closeAllEditors();
    }

    /**
     * Return all timestamps ending with 100... for reasons
     */
    @Test
    public void testYellow() {
        SWTBotView viewBot = fBot.viewById(ColorsView.ID);
        viewBot.setFocus();
        String insert = "Insert new color setting";
        String increasePriority = "Increase priority";
        String decreasePriority = "Decrease priority";
        String delete = "Delete color setting";
        viewBot.toolbarButton(insert).click();
        viewBot.toolbarButton(insert).click();
        viewBot.toolbarButton(insert).click();
        viewBot.toolbarButton(insert).click();
        viewBot.toolbarButton(increasePriority).click();
        viewBot.toolbarButton(decreasePriority).click();
        viewBot.toolbarButton(delete).click();
        viewBot.bot().label().setFocus();
        viewBot.toolbarButton(delete).click();
        viewBot.bot().label().setFocus();
        viewBot.toolbarButton(delete).click();
        viewBot.bot().label().setFocus();
        viewBot.toolbarButton(delete).click();


    }

    private static void openView(final String id) {
        final PartInitException res[] = new PartInitException[1];
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                try {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(id);
                } catch (PartInitException e) {
                    res[0] = e;
                }
            }
        });
        if (res[0] != null) {
            fail(res[0].getMessage());
        }
        SWTBotUtils.waitForJobs();
    }

}
