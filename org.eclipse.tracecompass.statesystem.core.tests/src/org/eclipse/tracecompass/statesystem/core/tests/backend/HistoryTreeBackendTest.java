/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests.backend;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.statesystem.core.StateSystem;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HistoryTreeBackend;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.backend.IStateHistoryBackend;
import org.eclipse.tracecompass.statesystem.core.backend.StateHistoryBackendFactory;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test cases for the history tree backend
 *
 * @author Patrick Tasse
 */
public class HistoryTreeBackendTest {

    private static final String THREADS = "Threads";
    private static final String[] NAMES = {
        "",      // ''
        "\0",    // 'Null'
        "a",     // 'a'
        "/",     // '/'
        "\\",    // '\'
        "ab",    // 'ab'
        "a/",    // 'a/'
        "a\\",   // 'a\'
        "/a",    // '/a'
        "//",    // '//'
        "/\\",   // '/\'
        "\\a",   // '\a'
        "\\/",   // '\/'
        "\\\\",  // '\\'
        "abc",   // 'abc'
        "ab/",   // 'ab/'
        "ab\\",  // 'ab\'
        "a/b",   // 'a/b'
        "a//",   // 'a//'
        "a/\\",  // 'a/\'
        "a\\b",  // 'a\b'
        "a\\/",  // 'a\/'
        "a\\\\", // 'a\\'
        "/ab",   // '/ab'
        "/a/",   // '/a/'
        "/a\\",  // '/a\'
        "//a",   // '//a'
        "///",   // '///'
        "//\\",  // '//\'
        "/\\a",  // '/\a'
        "/\\/",  // '/\/'
        "/\\\\", // '/\\'
        "\\ab",  // '\ab'
        "\\a/",  // '\a/'
        "\\a\\", // '\a\'
        "\\/a",  // '\/a'
        "\\//",  // '\//'
        "\\/\\", // '\/\'
        "\\\\a", // '\\a'
        "\\\\/", // '\\/'
        "\\\\\\" // '\\\'
    };
    private static final String STATUS = "Status";

    private static File testHtFile;

    private StateSystem ss;
    private int threadsQuark;

    /**
     * Test class setup.
     *
     * @throws IOException
     *             if there is an error accessing the test file
     */
    @BeforeClass
    public static void beforeClass() throws IOException {
        testHtFile = File.createTempFile("test", ".ht");
        IStateHistoryBackend backend = StateHistoryBackendFactory.createHistoryTreeBackendNewFile("test", checkNotNull(testHtFile), 0, 0L, 0);
        StateSystem ss = new StateSystem(backend);
        for (String name : NAMES) {
            ss.getQuarkAbsoluteAndAdd(THREADS, name, STATUS);
        }
        ss.closeHistory(0L);
        ss.dispose();
    }

    /**
     * Test class cleanup.
     */
    @AfterClass
    public static void afterClass() {
        if (testHtFile != null && testHtFile.exists()) {
            testHtFile.delete();
        }
    }

    /**
     * Test setup.
     *
     * @throws IOException
     *             if there is an error accessing the test file
     * @throws AttributeNotFoundException
     *             if an attribute is not found
     */
    @Before
    public void before() throws IOException, AttributeNotFoundException {
        HistoryTreeBackend backend = new HistoryTreeBackend("test", testHtFile, 0);
        ss = new StateSystem(backend, false);
        threadsQuark = ss.getQuarkAbsolute(THREADS);
    }

    /**
     * Test cleanup.
     */
    @After
    public void after() {
        if (ss != null) {
            ss.dispose();
        }
    }

    /**
     * Test inverse composition of attribute names.
     * <p>
     * Tests that getAttributeName(getQuarkRelative(name)).equals(name).
     */
    @Test
    public void testNameInverseComposition() {
        for (String name : NAMES) {
            try {
                int quark = ss.getQuarkRelative(threadsQuark, name);
                assertEquals(name, ss.getAttributeName(quark));
            } catch (AttributeNotFoundException e) {
                fail("AttributeNotFoundException name=" + name);
            }
        }
    }

    /**
     * Test inverse composition of attribute paths.
     * <p>
     * Tests that pathToArray(getFullAttributePath(getQuarkAbsolute(array)))).equals(array).
     * <p>
     * Tests that getQuarkAbsolute(pathToArray(getFullAttributePath(quark))).equals(quark).
     */
    @Test
    public void testPathInverseComposition() {
        for (String name : NAMES) {
            try {
                int quark = ss.getQuarkAbsolute(THREADS, name, STATUS);
                @NonNull String path = checkNotNull(ss.getFullAttributePath(quark));
                String[] array = StateSystemUtils.pathToArray(path);
                assertArrayEquals(new String[] { THREADS, name, STATUS }, array);
                try {
                    assertEquals(quark, ss.getQuarkAbsolute(array));
                } catch (AttributeNotFoundException e) {
                    fail("AttributeNotFoundException array=" + Arrays.toString(array));
                }
            } catch (AttributeNotFoundException e) {
                fail("AttributeNotFoundException name=" + name);
            }
        }
    }
}
