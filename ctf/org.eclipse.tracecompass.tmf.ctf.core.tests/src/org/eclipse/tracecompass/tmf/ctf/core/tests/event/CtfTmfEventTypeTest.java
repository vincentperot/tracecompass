/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial generation with CodePro tools
 *   Alexandre Montplaisir - Clean up, consolidate redundant tests
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEventType;
import org.junit.Test;

/**
 * The class <code>CtfTmfEventTypeTest</code> contains tests for the class
 * <code>{@link CtfTmfEventType}</code>.
 *
 * @author ematkho
 * @version 1.0
 */
public class CtfTmfEventTypeTest {

    /**
     * Run the CtfTmfEventType(String,String,ITmfEventField) constructor test.
     */
    @Test
    public void testCtfTmfEventType() {
        String eventName = "";
        CtfTmfEventType result = new CtfTmfEventType(eventName);

        assertNotNull(result);
        assertEquals("", result.toString());
        assertEquals("", result.getName());
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        CtfTmfEventType fixture = new CtfTmfEventType("" );

        String result = fixture.toString();

        assertEquals("", result);
    }
}
