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
 *   Patrick Tasse - Fix editor handling
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.ui.swtbot.tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test invalid trace openning
 *
 * @author Matthew Khouzam
 */
@RunWith(Parameterized.class)
public class TestInvalidCtfTrace {

    private static final String PROJET_NAME = "TestInvalidCtfTraces";
    private static final Path BASE_PATH = Paths.get("../org.eclipse.tracecompass.ctf.core.tests", "traces", "ctf-testsuite", "tests", "1.8");

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();

    private static SWTWorkbenchBot fBot;

    private final File fLocation;

    private final String fExpectedMessage;

    private static final Map<String, String> ERRORS = new HashMap<>();
    static {
        ERRORS.put("array-redefinition", "bla");
        ERRORS.put("integer-encoding-as-string", "bla");
        ERRORS.put("struct-align-enum", "bla");
        ERRORS.put("array-size-identifier", "bla");
        ERRORS.put("integer-encoding-invalid", "bla");
        ERRORS.put("struct-align-huge", "bla");
        ERRORS.put("array-size-keyword", "bla");
        ERRORS.put("integer-negative-bit-size", "bla");
        ERRORS.put("struct-align-negative", "bla");
        ERRORS.put("array-size-negative", "bla");
        ERRORS.put("integer-range", "bla");
        ERRORS.put("struct-align-string", "bla");
        ERRORS.put("array-size-not-present", "bla");
        ERRORS.put("integer-signed-as-string", "bla");
        ERRORS.put("struct-align-zero", "bla");
        ERRORS.put("array-size-string", "bla");
        ERRORS.put("integer-signed-invalid", "bla");
        ERRORS.put("struct-duplicate-field-name", "bla");
        ERRORS.put("array-size-type", "bla");
        ERRORS.put("integer-size-as-string", "bla");
        ERRORS.put("struct-duplicate-struct-name", "bla");
        ERRORS.put("array-size-type-field", "bla");
        ERRORS.put("integer-size-missing", "bla");
        ERRORS.put("struct-field-name-keyword", "bla");
        ERRORS.put("content-size-larger-than-packet-size", "bla");
        ERRORS.put("out-of-bound-empty-event-with-aligned-struct", "bla");
        ERRORS.put("cross-packet-event-alignment-empty-struct", "bla");
        ERRORS.put("out-of-bound-float", "bla");
        ERRORS.put("cross-packet-event-alignment-integer", "bla");
        ERRORS.put("out-of-bound-integer", "bla");
        ERRORS.put("cross-packet-event-array-of-integers", "bla");
        ERRORS.put("out-of-bound-large-sequence-length", "bla");
        ERRORS.put("cross-packet-event-float", "bla");
        ERRORS.put("out-of-bound-len-of-sequence", "bla");
        ERRORS.put("cross-packet-event-integer", "bla");
        ERRORS.put("out-of-bound-packet-header", "bla");
        ERRORS.put("cross-packet-event-len-of-sequence", "bla");
        ERRORS.put("out-of-bound-sequence-between-elements", "bla");
        ERRORS.put("cross-packet-event-sequence-between-elements", "bla");
        ERRORS.put("out-of-bound-sequence-start", "bla");
        ERRORS.put("cross-packet-event-sequence-start", "bla");
        ERRORS.put("out-of-bound-sequence-within-element", "bla");
    }

    /**
     * Populate the parameters
     *
     * @return the parameters. Basically all the errors with lookuped paths
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getTracePaths() {
        final List<Object[]> dirs = new LinkedList<>();

        Path badStreams = BASE_PATH.resolve(Paths.get("regression", "stream", "fail"));
        FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                final Path fileName = dir.getFileName();
                String res = ERRORS.get(fileName.toString());
                if (res != null) {
                    dirs.add(new Object[] { dir.toFile(), res });
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

        };
        try {
            Files.walkFileTree(badStreams, visitor);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Path badMetadata = BASE_PATH.resolve(Paths.get("regression", "metadata", "fail"));
        try {
            Files.walkFileTree(badMetadata, visitor);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dirs;
    }

    /**
     * Constructor
     *
     * @param location
     *            trace file
     * @param errorMessage
     *            error message
     */
    public TestInvalidCtfTrace(File location, String errorMessage) {
        fLocation = location;
        fExpectedMessage = errorMessage;
    }

    /**
     * Initialization
     */
    @Before
    public void init() {
        SWTBotUtils.failIfUIThread();
        Thread.currentThread().setName("SWTBot Thread"); // for the debugger
        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout()));
        fBot = new SWTWorkbenchBot();

        SWTBotUtils.closeView("welcome", fBot);

        SWTBotUtils.switchToTracingPerspective();
        /* finish waiting for eclipse to load */
        SWTBotUtils.waitForJobs();
        SWTBotUtils.createProject(PROJET_NAME);

    }

    /**
     * Delete file
     */
    @After
    public void cleanup() {
        SWTBotUtils.deleteProject(PROJET_NAME, fBot);
        fLogger.removeAllAppenders();
    }

    /**
     * Open an invalid trace and see the message
     */
    @Test
    public void testOpen() {
        SWTBotUtils.selectTracesFolder(fBot, PROJET_NAME);
        SWTBotUtils.openTrace(PROJET_NAME, fLocation.getAbsolutePath(), "org.eclipse.linuxtools.tmf.core.tests.xmlstub");
        String text = fBot.activeShell().bot().text().getText();
        fBot.activeShell().bot().button().click();
        assertEquals(fExpectedMessage, text);

    }

}