/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests.attribute;

import org.eclipse.tracecompass.internal.statesystem.core.Attribute;
import org.junit.Test;

/**
 * Test class for state system attributes
 *
 * @author Alexandre Montplaisir
 */
public class AttributeTest {

    /**
     * Attempt to create an attribute with unescaped protected characters in it.
     * This should throw an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testProtectedCharacters() {
        String attribName = "This/is/a/test";
        new Attribute(null, attribName, 1); // should throw exception
    }

    /**
     * Attempt to create an attribute containing escaped protected characters.
     * This should be allowed.
     */
    @Test
    public void testEscapedProtectedCharacters() {
        String attribName = "This\\/is\\/a\\/test";
        new Attribute(null, attribName, 1);

        attribName = "This\\:is\\:another\\/test";
        new Attribute(null, attribName, 2);
    }
}
