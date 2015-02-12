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
package org.eclipse.tracecompass.lttng2.ust.ui.swtbot.tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.views.callstack.CallStackView;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test for Colors views in trace compass
 */
public class CallStackViewTest {

    private static final int NUM_DOWN = 22;

    private static final String UST_ID = "org.eclipse.linuxtools.lttng2.ust.tracetype";

    private static final String PROJECT_NAME = "TestForCallstack";

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();
    private static SWTWorkbenchBot fBot;

    private String[] fExpected;

    /**
     * Initialization, creates a temp trace
     */
    @BeforeClass
    public static void init() {
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
    }

    /**
     * Open a trace in an editor
     */
    @Before
    public void beforeTest() {
        SWTBotUtils.createProject(PROJECT_NAME);
        SWTBotTreeItem treeItem = SWTBotUtils.selectTracesFolder(fBot, PROJECT_NAME);
        assertNotNull(treeItem);
        final CtfTmfTestTrace cygProfile = CtfTmfTestTrace.CYG_PROFILE;
        SWTBotUtils.openTrace(PROJECT_NAME, cygProfile.getTrace().getPath(), UST_ID);
        SWTBotUtils.openView(CallStackView.ID);
        SWTBotUtils.waitForJobs();
        fExpected = new String[] { "40472b", "4045c8", "404412", "", "" };
    }

    /**
     * Close the editor
     */
    @After
    public void tearDown() {
        fBot.closeAllEditors();
        SWTBotUtils.deleteProject(PROJECT_NAME, fBot);
    }

    /**
     * Test if callstack is populated
     */
    @Test
    public void testOpenCallstack() {
        SWTBotView viewBot = fBot.viewById(CallStackView.ID);
        viewBot.setFocus();
        String[] expected = { "glxgears-cyg-profile", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "" };
        SWTBotTree tree = getTree(viewBot);
        List<String> names = new ArrayList<>();
        for (SWTBotTreeItem swtBotTreeItem : tree.getAllItems()) {
            names.add(swtBotTreeItem.getText());
        }
        assertArrayEquals(expected, names.toArray(new String[0]));
    }

    /**
     * Test check callstack at a time
     */
    @Test
    public void testGoToTimeAndCheckStack() {

        goToEvent(NUM_DOWN);
        final SWTBotView viewBot = fBot.viewById(CallStackView.ID);
        viewBot.setFocus();
        SWTBotUtils.waitForJobs();
        SWTBotTree tree = getTree(viewBot);
        List<String> names = new ArrayList<>();
        for (SWTBotTreeItem swtBotTreeItem : tree.getAllItems()) {
            for (SWTBotTreeItem items : swtBotTreeItem.getItems()) {
                for (SWTBotTreeItem item : items.getItems()) {
                    names.add(item.cell(0));
                }
            }
        }
        assertArrayEquals(fExpected, names.toArray(new String[0]));
    }

    /**
     * Test check callstack at a time after navigating
     */
    @Test
    public void testGoToTimeGoBackAndForthAndCheckStack() {
        goToEvent(NUM_DOWN);
        final SWTBotView viewBot = fBot.viewById(CallStackView.ID);
        // forward 10 times
        for (int i = 0; i < 10; i++) {
            viewBot.getToolbarButtons().get(9).click();
            SWTBotUtils.waitForJobs();
        }
        // back twice
        for (int i = 0; i < 2; i++) {
            viewBot.getToolbarButtons().get(8).click();
            SWTBotUtils.waitForJobs();
        }
        // move up and down once to make sure it doesn't explode
        viewBot.getToolbarButtons().get(10).click();
        SWTBotUtils.waitForJobs();
        viewBot.getToolbarButtons().get(11).click();
        SWTBotUtils.waitForJobs();

        // Zoom in and out too
        viewBot.getToolbarButtons().get(12).click();
        SWTBotUtils.waitForJobs();
        viewBot.getToolbarButtons().get(13).click();
        SWTBotUtils.waitForJobs();

        viewBot.setFocus();
        SWTBotUtils.waitForJobs();
        SWTBotTree tree = getTree(viewBot);
        List<String> names = new ArrayList<>();
        for (SWTBotTreeItem swtBotTreeItem : tree.getAllItems()) {
            for (SWTBotTreeItem items : swtBotTreeItem.getItems()) {
                for (SWTBotTreeItem item : items.getItems()) {
                    names.add(item.cell(0));
                }
            }
        }
        assertArrayEquals(fExpected, names.toArray(new String[0]));
    }

    /**
     * Test check callstack at a time with sorting, the trace is not sortable,
     * this is a smoke test
     */
    @Test
    public void testGoToTimeSortAndCheckStack() {
        goToEvent(NUM_DOWN);
        final SWTBotView viewBot = fBot.viewById(CallStackView.ID);
        // Sort by name
        viewBot.getToolbarButtons().get(3).click();
        // Sort by ID
        viewBot.getToolbarButtons().get(4).click();
        // Sort by time
        viewBot.getToolbarButtons().get(5).click();
        viewBot.setFocus();
        SWTBotUtils.waitForJobs();
        SWTBotTree tree = getTree(viewBot);
        List<String> names = new ArrayList<>();
        for (SWTBotTreeItem swtBotTreeItem : tree.getAllItems()) {
            for (SWTBotTreeItem items : swtBotTreeItem.getItems()) {
                for (SWTBotTreeItem item : items.getItems()) {
                    names.add(item.cell(0));
                }
            }
        }
        assertArrayEquals(fExpected, names.toArray(new String[0]));
    }

    private static SWTBotTree getTree(final SWTBotView viewBot) {
        final SWTBotTree[] trees = new SWTBotTree[1];
        UIThreadRunnable.syncExec(new VoidResult() {
            Tree tree = null;

            @Override
            public void run() {
                Composite widget = (Composite) viewBot.getWidget();
                Control[] children = widget.getChildren();
                for (Control child : children) {
                    if (child instanceof Composite) {
                        recurse((Composite) child);
                    }
                }
            }

            private void recurse(Composite parent) {
                for (Control child : parent.getChildren()) {
                    if (tree != null) {
                        return;
                    }
                    if (child instanceof Composite) {
                        recurse((Composite) child);
                    }
                    if (child instanceof Tree) {
                        tree = (Tree) child;
                        trees[0] = new SWTBotTree(tree);
                        return;
                    }
                }
            }
        });
        SWTBotTree tree = trees[0];
        return tree;
    }

    private static void goToEvent(int rank) {
        SWTBotTable table = fBot.activeEditor().bot().table();
        table.setFocus();
        // select first event
        KeyStroke[] keyStrokes = new KeyStroke[1];
        for (int i = 0; i < rank; i++) {
            keyStrokes[0] = KeyStroke.getInstance(SWT.ARROW_DOWN);
            table.pressShortcut(keyStrokes);
        }
    }

    /**
     * Test check callstack at a time with function map
     */
    @Ignore
    @Test
    public void testGoToTimeAndCheckStackWithNames() {
        goToEvent(NUM_DOWN);
        final SWTBotView viewBot = fBot.viewById(CallStackView.ID);
        viewBot.setFocus();
        // no way to load mappings yet! :(
        SWTBotTree tree = getTree(viewBot);
        SWTBotUtils.waitForJobs();
        List<String> names = new ArrayList<>();
        for (SWTBotTreeItem swtBotTreeItem : tree.getAllItems()) {
            for (SWTBotTreeItem items : swtBotTreeItem.getItems()) {
                for (SWTBotTreeItem item : items.getItems()) {
                    names.add(item.cell(0));
                }
            }
        }
    }

    /**
     * Test check callstack toolbar buttons
     */
    @Test
    public void testCallstackNavigation() {
        SWTBotView viewBot = fBot.viewById(CallStackView.ID);
        viewBot.setFocus();
        List<String> buttons = new ArrayList<>();
        for (SWTBotToolbarButton swtBotToolbarButton : viewBot.getToolbarButtons()) {
            buttons.add(swtBotToolbarButton.getToolTipText());
        }
        String[] expected = { "Import a binary file containing debugging symbols",
                "Import a text file containing the mapping between addresses and function names",
                "", "Sort threads by thread name", "Sort threads by thread id", "Sort threads by start time",
                "", "Reset the Time Scale to Default", "Select Previous Event", "Select Next Event",
                "Select Previous Item", "Select Next Item", "Zoom In", "Zoom Out", "", "Pin View"
        };
        assertArrayEquals(expected, buttons.toArray(new String[0]));
    }
}
