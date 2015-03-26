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

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.tracecompass.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ImageHelper;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
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
    private SWTBotTable fTableBot;
    // remove me
    private boolean pass = false;
    private ImageHelper fAfter;
    private ImageHelper fAfterFilter;
    private ImageHelper fBefore;

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
     * Bring up the table
     */
    @Before
    public void setup() {
        SWTBotUtils.createProject(TRACE_PROJECT_NAME);

        // Open the actual trace
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, fTestFile.getAbsolutePath(), COLUMN_TRACE_TYPE);
        SWTBotEditor editorBot = SWTBotUtils.activateEditor(fBot, fTestFile.getName());

        fTableBot = editorBot.bot().table();

        maximizeTable(fTableBot);
    }

    /**
     * Remove the project
     */
    @After
    public void cleanup() {
        try {
            if (!pass) {
                if (fBefore != null) {
                    fBefore.writePng(new File("before" + fBefore.hashCode() + ".png"));
                }
                if (fAfter != null) {
                    fAfter.writePng(new File("after" + fBefore.hashCode() + ".png"));
                }
                if (fAfterFilter != null) {
                    fAfterFilter.writePng(new File("afterFilter" + fBefore.hashCode() + ".png"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        SWTBotUtils.deleteProject(TRACE_PROJECT_NAME, fBot);
        SWTBotUtils.waitForJobs();
    }

    /**
     * Test highlighting multiple elements in a message
     */
    @Test
    public void testMultiHighlightMessage() {
        final Rectangle cellBounds = SWTBotUtils.getCellBounds(fTableBot.widget, ROW, 6);

        fBefore = ImageHelper.getScreenGrab(cellBounds);
        Map<RGB, Integer> colorBefore = fBefore.getHistogram();
        // Maximize editor area
        fTableBot.click(0, 6);
        fBot.text().typeText("e\n", 100);
        fTableBot.select(4);
        fAfter = ImageHelper.getScreenGrab(cellBounds);
        Map<RGB, Integer> colorAfter = fAfter.getHistogram();

        assertTrue(colorBefore.containsKey(BLACK));
        assertTrue(colorBefore.containsKey(WHITE));
        assertFalse(colorBefore.containsKey(YELLOW));

        assertTrue(colorAfter.containsKey(BLACK));
        assertTrue(colorAfter.containsKey(WHITE));
        assertTrue(colorAfter.containsKey(YELLOW));

        int start = -1;
        int end;
        List<Point> intervals = new ArrayList<>();
        List<RGB> pixelRow = fAfter.getPixelRow(1);
        for (int i = 1; i < pixelRow.size(); i++) {
            RGB prevPixel = pixelRow.get(i - 1);
            RGB pixel = pixelRow.get(i);
            if (!prevPixel.equals(YELLOW) && pixel.equals(YELLOW)) {
                start = i;
            }
            if (prevPixel.equals(YELLOW) && pixel.equals(WHITE)) {
                end = i;
                if (start == -1) {
                    fail();
                }
                intervals.add(new Point(start, end));
            }
        }
        assertEquals(2, intervals.size());

        // may be unstable
        assertEquals(intervals.get(1).y - intervals.get(1).x, intervals.get(0).y - intervals.get(0).x);
        pass = true;
    }

    /**
     * Test basic highlight
     */
    @Test
    public void testHighlight() {
        final Rectangle cellBounds = SWTBotUtils.getCellBounds(fTableBot.widget, ROW, COLUMN);

        fBefore = ImageHelper.getScreenGrab(cellBounds);
        Map<RGB, Integer> colorBefore = fBefore.getHistogram();
        // Maximize editor area
        fTableBot.click(0, COLUMN);
        fBot.text().typeText("HostF\n", 100);
        fTableBot.select(4);
        fAfter = ImageHelper.getScreenGrab(cellBounds);
        Map<RGB, Integer> colorAfter = fAfter.getHistogram();

        assertTrue(colorBefore.containsKey(BLACK));
        assertTrue(colorBefore.containsKey(WHITE));
        assertFalse(colorBefore.containsKey(YELLOW));

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
        pass = true;
    }

    /**
     * Switch to filter and back
     */
    @Test
    public void testSwitchToFilter() {
        final Rectangle cellBounds = SWTBotUtils.getCellBounds(fTableBot.widget, ROW, 1);
        fBefore = ImageHelper.getScreenGrab(cellBounds);
        fTableBot.click(0, 1);
        fBot.text().typeText("00\n", 100);
        fTableBot.select(4);
        fAfter = ImageHelper.getScreenGrab(cellBounds);

        fTableBot.click(0, 0);
        fAfterFilter = ImageHelper.getScreenGrab(cellBounds);

        fTableBot.click(0, 0);
        List<RGB> beforeLine = fBefore.getPixelRow(1);
        List<RGB> afterLine = fAfter.getPixelRow(1);
        List<RGB> afterFilterLine = fAfterFilter.getPixelRow(1);
        assertEquals(beforeLine.size(), afterLine.size());
        assertEquals(beforeLine.size(), afterFilterLine.size());
        for (int i = 0; i < beforeLine.size(); i++) {
            RGB afterFilterPixel = afterFilterLine.get(i);
            RGB beforePixel = beforeLine.get(i);
            RGB afterPixel = afterLine.get(i);

            assertEquals(beforePixel, afterFilterPixel);
            if (!afterPixel.equals(YELLOW)) {
                assertEquals(beforePixel, afterPixel);
            } else {
                assertNotEquals(YELLOW, beforePixel);
            }

        }
        assertEquals(beforeLine, afterFilterLine);
        assertNotEquals(afterLine, beforeLine);
        pass = true;
    }

    private static void maximizeTable(SWTBotTable tableBot) {
        try {
            tableBot.pressShortcut(KeyStroke.getInstance(IKeyLookup.CTRL_NAME + "+"), KeyStroke.getInstance("M"));
        } catch (ParseException e) {
            fail();
        }
    }

}
