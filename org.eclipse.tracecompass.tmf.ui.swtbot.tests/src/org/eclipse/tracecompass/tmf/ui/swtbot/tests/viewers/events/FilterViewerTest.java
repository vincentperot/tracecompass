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
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.views.filter.FilterView;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for filter views in trace compass
 */
public class FilterViewerTest {

    private static final String XMLSTUB_ID = "org.eclipse.linuxtools.tmf.core.tests.xmlstub";
    private static final String TRACETYPE = "Test trace : XML Trace Stub";
    private static final String AND = "AND";
    private static final String WITH_TRACETYPE = "WITH TRACETYPE " + TRACETYPE;
    private static final String FILTER_TEST = "FILTER test";

    private static final String EVENT_END = "\" type=\"int\" />" + "</event>";

    private static final String EVENT_MIDDLE = " \" name=\"event\"><field name=\"field\" value=\"";

    private static final String EVENT_BEGIN = "<event timestamp=\"";

    private static final String TRACE_END = "</trace>";

    private static final String TRACE_START = "<trace>";

    private static final String TEST_FOR_FILTERING = "TestForOffsetting";

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
     * Update
     */
    @Test
    public void test(){
        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        SWTBotTreeItem treeItem = SWTBotUtils.createProject(fBot, TEST_FOR_FILTERING);
        assertNotNull(treeItem);
        SWTBotUtils.openTrace(TEST_FOR_FILTERING, fLocation.getAbsolutePath(), XMLSTUB_ID);
        openView(FilterView.ID);
        bot = new SWTWorkbenchBot();
        SWTBotView viewBot = bot.viewById(FilterView.ID);
        viewBot.setFocus();
        SWTBot filterBot = viewBot.bot();
        SWTBotTree treeBot = filterBot.tree();

        viewBot.toolbarButton("Add new filter").click();
        treeBot.getTreeItem("FILTER <name>").select();
        SWTBotText textBot = filterBot.text();
        textBot.setFocus();
        textBot.setText("test");
        SWTBotTreeItem filterNodeBot = treeBot.getTreeItem(FILTER_TEST);
        filterNodeBot.click();
        filterNodeBot.contextMenu("TRACETYPE").click();
        filterNodeBot.expand();
        SWTBotCombo comboBot = filterBot.comboBox();
        comboBot.setSelection(TRACETYPE);
        filterNodeBot.getNode(WITH_TRACETYPE).expand();
        filterNodeBot.getNode(WITH_TRACETYPE).contextMenu(AND).click();
        filterNodeBot.getNode(WITH_TRACETYPE).getNode(AND).contextMenu("CONTAINS").click();
        filterNodeBot.getNode(WITH_TRACETYPE).getNode(AND).expand();
        comboBot = selectCombo(filterBot);
        comboBot.setSelection(comboBot.itemCount()-3);
        textBot = filterBot.text();
        textBot.setFocus();
        textBot.setText("100");
        filterNodeBot.getNode(WITH_TRACETYPE).getNode(AND).getNode("Timestamp CONTAINS \"100\"").select();
        filterNodeBot.getNode(WITH_TRACETYPE).getNode(AND).select();
        viewBot.toolbarButton("Save filters").click();
    }

    private static void openView(final String id) {
        final PartInitException res[] =new PartInitException[1];
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                try {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(id);
                } catch (PartInitException e) {
                    res[0]=e;
                }
            }
        });
        if(res[0]!= null){
            fail(res[0].getMessage());
        }
        SWTBotUtils.waitForJobs();
    }

    private static SWTBotCombo selectCombo(SWTBot bot) {
        SWTBotCombo comboBox;
        comboBox = bot.comboBox();
        assertNotNull(comboBox);
        return comboBox;
    }

}
