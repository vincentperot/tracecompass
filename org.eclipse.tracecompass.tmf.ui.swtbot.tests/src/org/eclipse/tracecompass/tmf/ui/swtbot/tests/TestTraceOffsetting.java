/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http:/www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.keyboard.KeyboardFactory;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Test trace offsetting
 *
 * @author Matthew Khouzam
 */
public class TestTraceOffsetting {

    private static final String EVENT_END = "\" type=\"int\" />" + "</event>";

    private static final String EVENT_MIDDLE = " \" name=\"event\"><field name=\"field\" value=\"";

    private static final String EVENT_BEGIN = "<event timestamp=\"";

    private static final String TRACE_END = "</trace>";

    private static final String TRACE_START = "<trace>";

    private static final String TEST_FOR_OFFSETTING = "TestForOffsetting";

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();
    private static SWTWorkbenchBot fBot;

    private static String makeEvent(int ts, int val) {
        return EVENT_BEGIN + Integer.toString(ts) + EVENT_MIDDLE + Integer.toString(val) + EVENT_END + "\n";
    }

    private File fLocation;

    /**
     * Initialization, creates a temp trace
     *
     * @throws IOException
     *             should not happen
     */
    @Before
    public void init() throws IOException {
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
        fLocation = File.createTempFile("sample", ".xml");
        try (BufferedRandomAccessFile braf = new BufferedRandomAccessFile(fLocation, "rw")) {
            braf.writeBytes(TRACE_START);
            for (int i = 0; i < 100; i++) {
                braf.writeBytes(makeEvent(i * 100, i % 4));
            }
            braf.writeBytes(TRACE_END);
        }
    }

    /**
     * Test offsetting by 99 ns
     */
    @Test
    public void testOffsetting() {
        SWTBotTreeItem treeItem = SWTBotUtils.createProject(fBot, TEST_FOR_OFFSETTING);
        SWTBotUtils.openTrace(TEST_FOR_OFFSETTING, fLocation.getAbsolutePath(), "org.eclipse.linuxtools.tmf.core.tests.xmlstub");
        ITmfTimestamp before = TmfTraceManager.getInstance().getActiveTrace().getEndTime();
        treeItem.select();
        treeItem.getItems()[0].contextMenu("Apply Time Offset...").click();
        SWTBotUtils.waitForJobs();
        // set offset to 99 ns
        SWTBotShell shell = fBot.shell("Apply time offset");
        shell.setFocus();
        SWTBot shellBot = shell.bot();
        assertNotNull(shellBot);
        SWTBotTreeItem[] allItems = shellBot.tree().getAllItems();
        final SWTBotTreeItem swtBotTreeItem = allItems[0];
        swtBotTreeItem.click(1);
        UIThreadRunnable.syncExec( new VoidResult() {
            @Override
            public void run() {
                KeyboardFactory.getSWTKeyboard().typeText("99\n", 33);
            }
        });
        swtBotTreeItem.select();
        SWTBotUtils.waitForJobs();
        // click "ok"
        shellBot.button("OK").click();
        // re-open trace
        SWTBotUtils.openTrace(TEST_FOR_OFFSETTING, fLocation.getAbsolutePath(), "org.eclipse.linuxtools.tmf.core.tests.xmlstub");
        SWTBotUtils.waitForJobs();
        ITmfTimestamp after = TmfTraceManager.getInstance().getActiveTrace().getEndTime();
        assertEquals(99, after.getDelta(before).getValue());

    }

}