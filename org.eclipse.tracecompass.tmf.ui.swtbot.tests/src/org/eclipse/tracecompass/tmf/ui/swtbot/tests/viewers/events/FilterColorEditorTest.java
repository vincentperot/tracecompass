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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.tracecompass.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * SWTBot test for testing movable column feature.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class FilterColorEditorTest {

    private static final RGB YELLOW = new RGB(255, 255, 0);
    private static final RGB WHITE = new RGB(255, 255, 255);
    private static final RGB BLACK = new RGB(0, 0, 0);
    private static final int ROW = 8;
    private static final int COLUMN = 2;
    private static final String TRACE_PROJECT_NAME = "test";
    private static final String COLUMN_TRACE = "syslog_collapse";
    private static final String COLUMN_TRACE_PATH = "testfiles/" + COLUMN_TRACE;
    private static final String COLUMN_TRACE_TYPE = "org.eclipse.linuxtools.tmf.tests.stubs.trace.text.testsyslog";

    private static File fTestFile = null;

    private static SWTWorkbenchBot fBot;

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();

    /**
     * Test Class setup
     */
    @BeforeClass
    public static void init() {
        SWTBotUtils.failIfUIThread();

        /* set up test trace */
        URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(COLUMN_TRACE_PATH), null);
        URI uri;
        try {
            uri = FileLocator.toFileURL(location).toURI();
            fTestFile = new File(uri);
        } catch (URISyntaxException | IOException e) {
            fail(e.getMessage());
        }

        assumeTrue(fTestFile.exists());

        /* Set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";

        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
        fBot = new SWTWorkbenchBot();

        /* Close welcome view */
        SWTBotUtils.closeView("Welcome", fBot);

        /* Switch perspectives */
        SWTBotUtils.switchToTracingPerspective();

        /* Finish waiting for eclipse to load */
        SWTBotUtils.waitForJobs();
    }

    /**
     * Test class tear down method.
     */
    @AfterClass
    public static void tearDown() {
        fLogger.removeAllAppenders();
    }

    /**
     * Switch the font to system then back to default.
     */
    @Test
    public void testChangeFont() {
        SWTBotUtils.createProject(TRACE_PROJECT_NAME);

        // Open the actual trace
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, fTestFile.getAbsolutePath(), COLUMN_TRACE_TYPE);
        SWTBotEditor editorBot = SWTBotUtils.activateEditor(fBot, fTestFile.getName());

        final SWTBotTable tableBot = editorBot.bot().table();

        maximizeTable(tableBot);

        final Rectangle cellBounds = SWTBotUtils.getCellBounds(tableBot.widget, ROW, COLUMN);

        Map<RGB, Integer> colorBefore = SWTBotUtils.getColorsOfArea(cellBounds);
        // Maximize editor area
        tableBot.click(0, COLUMN);
        fBot.text().typeText("HostF\n", 100);
        tableBot.select(4);
        Map<RGB, Integer> colorAfter = SWTBotUtils.getColorsOfArea(cellBounds);

        assertTrue(colorBefore.containsKey(BLACK));
        assertTrue(colorBefore.containsKey(WHITE));

        assertTrue(colorAfter.containsKey(BLACK));
        assertTrue(colorAfter.containsKey(WHITE));
        assertTrue(colorAfter.containsKey(YELLOW));
        Map<RGB, Integer> diff = new HashMap<>();
        /*
         * make the histogram difference This will allow us to verify what has
         * changed in the two images. Hopefully the sum will be zero
         */
        for (Entry<RGB, Integer> entry : colorAfter.entrySet()) {
            RGB key = entry.getKey();
            if (colorBefore.containsKey(key)) {
                diff.put(key, entry.getValue() - colorBefore.get(key));
            } else {
                diff.put(key, entry.getValue());
            }
        }
        /*
         * Check that the white became yellow
         */
        assertTrue(diff.get(WHITE).equals(-diff.get(YELLOW)));
        SWTBotUtils.deleteProject(TRACE_PROJECT_NAME, fBot);
    }

    private static void maximizeTable(SWTBotTable tableBot) {
        try {
            tableBot.pressShortcut(KeyStroke.getInstance(IKeyLookup.CTRL_NAME + "+"), KeyStroke.getInstance("M"));
        } catch (ParseException e) {
            fail();
        }
    }

}
