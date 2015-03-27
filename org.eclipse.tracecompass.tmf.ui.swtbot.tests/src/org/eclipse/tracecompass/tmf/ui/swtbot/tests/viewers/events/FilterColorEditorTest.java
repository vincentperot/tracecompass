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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
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

import com.google.common.collect.Multiset;

/**
 * SWTBot test for testing highlighting
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class FilterColorEditorTest {

    private static final int TIMESTAMP_COLUMN = 1;
    private static final int SOURCE_COLUMN = 2;
    private static final int MESSAGE_COLUMN = 6;
    private static final RGB YELLOW = new RGB(255, 255, 0);
    private static final String TRACE_PROJECT_NAME = "test";
    private static final String COLUMN_TRACE = "syslog_collapse";
    private static final String COLUMN_TRACE_PATH = "testfiles/" + COLUMN_TRACE;
    private static final String COLUMN_TRACE_TYPE = "org.eclipse.linuxtools.tmf.tests.stubs.trace.text.testsyslog";

    private static File fTestFile = null;

    private static SWTWorkbenchBot fBot;

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();
    private SWTBotTable fTableBot;
    private static final int ROW = 8;
    private RGB fForeground;
    private RGB fBackground;

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
        fBackground = fTableBot.backgroundColor().getRGB();
        fForeground = fTableBot.foregroundColor().getRGB();

        SWTBotUtils.maximizeTable(fTableBot);
    }

    /**
     * Remove the project
     */
    @After
    public void cleanup() {
        SWTBotUtils.deleteProject(TRACE_PROJECT_NAME, fBot);
        SWTBotUtils.waitForJobs();
    }

    /**
     * Test basic highlight
     */
    @Test
    public void testHighlight() {
        final Rectangle cellBounds = SWTBotUtils.getCellBounds(fTableBot.widget, ROW, SOURCE_COLUMN);

        Multiset<RGB> colorBefore = ImageHelper.grabImage(cellBounds).getHistogram();
        // Select source column and enter regex
        fTableBot.click(0, SOURCE_COLUMN);
        fBot.text().typeText("HostF\n", 100);
        // make sure selected row is not matching row
        fTableBot.select(ROW - 1);
        Multiset<RGB> colorAfter = ImageHelper.grabImage(cellBounds).getHistogram();

        assertTrue(colorBefore.contains(fBackground));
        assertTrue(colorBefore.contains(fForeground));
        assertFalse(colorBefore.contains(YELLOW));

        assertTrue(colorAfter.contains(fBackground));
        assertTrue(colorAfter.contains(fForeground));
        assertTrue(colorAfter.contains(YELLOW));

        /*
         * Check that the some background became yellow.
         */
        assertTrue(colorAfter.count(fBackground) < colorBefore.count(fBackground));
        assertTrue(colorAfter.count(YELLOW) > colorBefore.count(YELLOW));
    }

    /**
     * Test highlighting multiple elements in a message
     */
    @Test
    public void testMultiHighlightMessage() {
        final Rectangle cellBounds = SWTBotUtils.getCellBounds(fTableBot.widget, ROW, MESSAGE_COLUMN);

        ImageHelper before = ImageHelper.grabImage(cellBounds);
        // enter regex in message column
        fTableBot.click(0, MESSAGE_COLUMN);
        fBot.text().typeText("e\n", 100);
        // make sure matching item is not selected
        fTableBot.select(ROW - 1);
        ImageHelper after = ImageHelper.grabImage(cellBounds);

        Multiset<RGB> colorBefore = before.getHistogram();
        Multiset<RGB> colorAfter = after.getHistogram();

        assertTrue(colorBefore.contains(fBackground));
        assertTrue(colorBefore.contains(fForeground));
        assertFalse(colorBefore.contains(YELLOW));

        assertTrue(colorAfter.contains(fBackground));
        assertTrue(colorAfter.contains(fForeground));
        assertTrue(colorAfter.contains(YELLOW));

        int start = -1;
        int end;
        List<Point> intervals = new ArrayList<>();
        List<RGB> pixelRow = after.getPixelRow(2);
        for (int i = 1; i < pixelRow.size(); i++) {
            RGB prevPixel = pixelRow.get(i - 1);
            RGB pixel = pixelRow.get(i);
            if (prevPixel.equals(fBackground) && pixel.equals(YELLOW)) {
                start = i;
            }
            if (prevPixel.equals(YELLOW) && pixel.equals(fBackground)) {
                end = i;
                if (start == -1) {
                    fail();
                }
                intervals.add(new Point(start, end));
            }
        }
        assertEquals(2, intervals.size());
    }

    /**
     * Switch to filter and back
     */
    @Test
    public void testSwitchToFilter() {
        final Rectangle cellBounds = SWTBotUtils.getCellBounds(fTableBot.widget, ROW, TIMESTAMP_COLUMN);
        ImageHelper before = ImageHelper.grabImage(cellBounds);
        // enter regex in message column
        fTableBot.click(0, TIMESTAMP_COLUMN);
        fBot.text().typeText("00\n", 100);
        // make sure matching column is not selected
        fTableBot.select(ROW - 1);
        ImageHelper after = ImageHelper.grabImage(cellBounds);
        // toggle filter mode
        fTableBot.click(0, 0);
        ImageHelper afterFilter = ImageHelper.grabImage(cellBounds);

        List<RGB> beforeLine = before.getPixelRow(2);
        List<RGB> afterLine = after.getPixelRow(2);
        List<RGB> afterFilterLine = afterFilter.getPixelRow(2);

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

        // toggle back to search mode
        fTableBot.click(0, 0);
    }
}