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

import static org.junit.Assert.assertNotNull;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.util.ListIterator;

import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.scope.LexicalScope;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDefinition;
import org.eclipse.tracecompass.ctf.core.trace.CTFReaderException;
import org.eclipse.tracecompass.internal.ctf.core.trace.StreamInputPacketIndex;
import org.eclipse.tracecompass.internal.ctf.core.trace.StreamInputPacketIndexEntry;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>StreamInputPacketIndexTest</code> contains tests for the
 * class <code>{@link StreamInputPacketIndex}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
@SuppressWarnings("javadoc")
public class CTFStreamInputPacketIndexTest {

    private StreamInputPacketIndex fixture;

    /**
     * Perform pre-test initialization.
     *
     * @throws CTFReaderException
     */
    @Before
    public void setUp() throws CTFReaderException {
        fixture = new StreamInputPacketIndex();
        fixture.append(new StreamInputPacketIndexEntry(1L, 0L));
    }

    /**
     * Run the StreamInputPacketIndex() constructor test.
     */
    @Test
    public void testStreamInputPacketIndex() {
        assertNotNull(fixture);
        assertNotNull(fixture.getElement(0));
    }

    @Test
    public void testAppend() throws CTFReaderException {
        StreamInputPacketIndex red = new StreamInputPacketIndex();
        StructDeclaration sDec = createPacketHeader();
        for (int i = 0; i < 100; i += 10) {
            StructDefinition sdef = createStructDef(sDec, i * 10, i * 10 + 5);
            assertTrue(red.append(new StreamInputPacketIndexEntry(i, sdef, 0, 0)));
        }
        StreamInputPacketIndexEntry element = red.getElement(5);
        assertEquals(502L, element.lookupAttribute("timestamp_middle"));
        assertTrue(element.includes(501));
        assertFalse(element.includes(506));
        assertFalse(element.includes(409));
    }

    private static StructDefinition createStructDef(StructDeclaration sDec, long tsStart, long tsEnd) throws CTFReaderException {
        ByteBuffer bb = ByteBuffer.allocate(Long.SIZE * 3);
        assertNotNull(bb);
        BitBuffer bib = new BitBuffer(bb);
        bb.putLong(tsStart);
        bb.putLong(tsEnd);
        bb.putLong((tsStart + tsEnd) / 2);
        StructDefinition sdef = sDec.createDefinition(null, LexicalScope.EVENT, bib);
        return sdef;
    }

    private static StructDeclaration createPacketHeader() {
        StructDeclaration sDec = new StructDeclaration(64);
        sDec.addField("timestamp_begin", IntegerDeclaration.UINT_64B_DECL);
        sDec.addField("timestamp_end", IntegerDeclaration.UINT_64B_DECL);
        sDec.addField("timestamp_middle", IntegerDeclaration.UINT_64B_DECL);
        return sDec;
    }

    /**
     * Run the ListIterator<StreamInputPacketIndexEntry> search(long) method
     * test with a valid timestamp.
     */
    @Test
    public void testSearch_valid() {
        ListIterator<StreamInputPacketIndexEntry> result = fixture.search(1L);

        assertNotNull(result);
        assertEquals(true, result.hasNext());
        assertEquals(-1, result.previousIndex());
        assertEquals(false, result.hasPrevious());
        assertEquals(0, result.nextIndex());
    }

    /**
     * Run the ListIterator<StreamInputPacketIndexEntry> search(long) method
     * test with an invalid timestamp.
     */
    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testSearch_invalid() {
        ListIterator<StreamInputPacketIndexEntry> result = fixture.search(-1L);

        assertNotNull(result);
    }
}