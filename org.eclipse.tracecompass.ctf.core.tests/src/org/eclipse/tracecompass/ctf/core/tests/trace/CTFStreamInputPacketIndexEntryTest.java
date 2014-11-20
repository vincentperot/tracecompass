/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;

import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.scope.LexicalScope;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDefinition;
import org.eclipse.tracecompass.ctf.core.trace.CTFReaderException;
import org.eclipse.tracecompass.internal.ctf.core.trace.StreamInputPacketIndexEntry;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>StreamInputPacketIndexEntryTest</code> contains tests for the
 * class <code>{@link StreamInputPacketIndexEntry}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class CTFStreamInputPacketIndexEntryTest {

    private StreamInputPacketIndexEntry fixture;

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        fixture = new StreamInputPacketIndexEntry(1L, 1L);
    }

    /**
     * Run the StreamInputPacketIndexEntry(long) constructor test.
     */
    @Test
    public void testStreamInputPacketIndexEntry_1() {
        String expectedResult = "StreamInputPacketIndexEntry [offsetBytes=1, " +
                "timestampBegin=" + Long.MIN_VALUE +
                ", timestampEnd=" + Long.MAX_VALUE +
                "]";

        assertNotNull(fixture);
        assertEquals(expectedResult, fixture.toString());
    }

    /**
     * Run the StreamInputPacketIndexEntry(long) constructor test.
     */
    @Test
    public void testStreamInputPacketIndexEntry_includes() {
        assertTrue(fixture.includes(0));
    }


    /**
     * Run the String toString() method test.
     *
     * @throws CTFReaderException
     *             won't happen
     */
    @Test
    public void testToString() throws CTFReaderException {

        String expectedResult = "StreamInputPacketIndexEntry [offsetBytes=0, timestampBegin=0, timestampEnd=0]";
        StructDeclaration sd = new StructDeclaration(8);
        sd.addField("timestamp_begin", IntegerDeclaration.INT_32B_DECL);
        sd.addField("timestamp_end", IntegerDeclaration.INT_32B_DECL);
        @SuppressWarnings("null")
        BitBuffer bb = new BitBuffer(ByteBuffer.allocate(128));

        StructDefinition sdef = sd.createDefinition(null, LexicalScope.PACKET_HEADER, bb);
        assertEquals(expectedResult, new StreamInputPacketIndexEntry(0, sdef, 10000, 0).toString());
    }
}