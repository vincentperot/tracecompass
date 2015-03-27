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
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests;

import static org.eclipse.tracecompass.internal.statesystem.core.AttributeUtils.pathArrayToString;
import static org.eclipse.tracecompass.internal.statesystem.core.AttributeUtils.pathStringToArray;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.eclipse.tracecompass.internal.statesystem.core.AttributeUtils;
import org.junit.Test;

/**
 * Test the {@link AttributeUtils} class
 *
 * @author Patrick Tasse
 */
public class AttributeUtilsTest {

    /**
     * Test the {@link AttributeUtils#pathArrayToString(String[])} method.
     */
    @Test
    public void testPathToString() {
        assertEquals("", pathArrayToString(new String[] { null }));
        assertEquals("", pathArrayToString(new String[] { "" }));
        assertEquals("\0", pathArrayToString(new String[] { "\0" }));
        assertEquals("/", pathArrayToString(new String[] { "", "" }));
        assertEquals("\\\\", pathArrayToString(new String[] { "\\" }));
        assertEquals("\\/", pathArrayToString(new String[] { "/" }));
        assertEquals("a", pathArrayToString(new String[] { "a" }));
        assertEquals("a/", pathArrayToString(new String[] { "a", "" }));
        assertEquals("/a", pathArrayToString(new String[] { "", "a" }));
        assertEquals("a\\\\", pathArrayToString(new String[] { "a\\" }));
        assertEquals("\\\\a", pathArrayToString(new String[] { "\\a" }));
        assertEquals("ab/", pathArrayToString(new String[] { "ab", "" }));
        assertEquals("a/b", pathArrayToString(new String[] { "a", "b" }));
        assertEquals("/ab", pathArrayToString(new String[] { "", "ab" }));
        assertEquals("ab\\\\", pathArrayToString(new String[] { "ab\\" }));
        assertEquals("a\\\\b", pathArrayToString(new String[] { "a\\b" }));
        assertEquals("\\\\ab", pathArrayToString(new String[] { "\\ab" }));
        assertEquals("a//", pathArrayToString(new String[] { "a", "", "" }));
        assertEquals("/a/", pathArrayToString(new String[] { "", "a", "" }));
        assertEquals("//a", pathArrayToString(new String[] { "", "", "a" }));
        assertEquals("a\\\\\\\\", pathArrayToString(new String[] { "a\\\\" }));
        assertEquals("\\\\a\\\\", pathArrayToString(new String[] { "\\a\\" }));
        assertEquals("\\\\\\\\a", pathArrayToString(new String[] { "\\\\a" }));
        assertEquals("a\\/", pathArrayToString(new String[] { "a/" }));
        assertEquals("\\\\a/", pathArrayToString(new String[] { "\\a", "" }));
        assertEquals("\\/a", pathArrayToString(new String[] { "/a" }));
        assertEquals("a/\\\\", pathArrayToString(new String[] { "a", "\\" }));
        assertEquals("/a\\\\", pathArrayToString(new String[] { "", "a\\" }));
        assertEquals("/\\\\a", pathArrayToString(new String[] { "", "\\a" }));
        assertEquals("///", pathArrayToString(new String[] { "", "", "", "" }));
        assertEquals("//\\\\", pathArrayToString(new String[] { "", "", "\\" }));
        assertEquals("/\\/", pathArrayToString(new String[] { "", "/" }));
        assertEquals("\\//", pathArrayToString(new String[] { "/", "" }));
        assertEquals("/\\\\", pathArrayToString(new String[] { "", "\\" }));
        assertEquals("\\/\\\\", pathArrayToString(new String[] { "/\\" }));
        assertEquals("\\\\/", pathArrayToString(new String[] { "\\", "" }));
        assertEquals("\\\\\\\\", pathArrayToString(new String[] { "\\\\" }));
    }

    /**
     * Test the {@link AttributeUtils#pathStringToArray(String)} method.
     */
    @Test
    public void testStringToPath() {
        assertArrayEquals(new String[] { "" }, pathStringToArray(""));
        assertArrayEquals(new String[] { "\0" }, pathStringToArray("\0"));
        assertArrayEquals(new String[] { "", "" }, pathStringToArray("/"));
        assertArrayEquals(new String[] { "\\" }, pathStringToArray("\\"));
        assertArrayEquals(new String[] { "/" }, pathStringToArray("\\/"));
        assertArrayEquals(new String[] { "a" }, pathStringToArray("a"));
        assertArrayEquals(new String[] { "a", "" }, pathStringToArray("a/"));
        assertArrayEquals(new String[] { "", "a" }, pathStringToArray("/a"));
        assertArrayEquals(new String[] { "a\\" }, pathStringToArray("a\\"));
        assertArrayEquals(new String[] { "\\a" }, pathStringToArray("\\a"));
        assertArrayEquals(new String[] { "ab", "" }, pathStringToArray("ab/"));
        assertArrayEquals(new String[] { "a", "b" }, pathStringToArray("a/b"));
        assertArrayEquals(new String[] { "", "ab" }, pathStringToArray("/ab"));
        assertArrayEquals(new String[] { "ab\\" }, pathStringToArray("ab\\"));
        assertArrayEquals(new String[] { "a\\b" }, pathStringToArray("a\\b"));
        assertArrayEquals(new String[] { "\\ab" }, pathStringToArray("\\ab"));
        assertArrayEquals(new String[] { "a", "", "" }, pathStringToArray("a//"));
        assertArrayEquals(new String[] { "", "a", "" }, pathStringToArray("/a/"));
        assertArrayEquals(new String[] { "", "", "a" }, pathStringToArray("//a"));
        assertArrayEquals(new String[] { "a\\" }, pathStringToArray("a\\\\"));
        assertArrayEquals(new String[] { "\\a\\" }, pathStringToArray("\\a\\"));
        assertArrayEquals(new String[] { "\\a" }, pathStringToArray("\\\\a"));
        assertArrayEquals(new String[] { "a/" }, pathStringToArray("a\\/"));
        assertArrayEquals(new String[] { "\\a", "" }, pathStringToArray("\\a/"));
        assertArrayEquals(new String[] { "/a" }, pathStringToArray("\\/a"));
        assertArrayEquals(new String[] { "a", "\\" }, pathStringToArray("a/\\"));
        assertArrayEquals(new String[] { "", "a\\" }, pathStringToArray("/a\\"));
        assertArrayEquals(new String[] { "", "\\a" }, pathStringToArray("/\\a"));
        assertArrayEquals(new String[] { "", "", "", "" }, pathStringToArray("///"));
        assertArrayEquals(new String[] { "", "", "\\" }, pathStringToArray("//\\"));
        assertArrayEquals(new String[] { "", "/" }, pathStringToArray("/\\/"));
        assertArrayEquals(new String[] { "/", "" }, pathStringToArray("\\//"));
        assertArrayEquals(new String[] { "", "\\" }, pathStringToArray("/\\\\"));
        assertArrayEquals(new String[] { "/\\" }, pathStringToArray("\\/\\"));
        assertArrayEquals(new String[] { "\\", "" }, pathStringToArray("\\\\/"));
        assertArrayEquals(new String[] { "\\\\" }, pathStringToArray("\\\\\\"));
    }
}
