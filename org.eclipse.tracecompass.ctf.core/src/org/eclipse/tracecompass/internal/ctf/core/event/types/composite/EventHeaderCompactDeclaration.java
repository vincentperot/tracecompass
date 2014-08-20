/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.ctf.core.event.types.composite;

import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.tracecompass.ctf.core.event.types.Declaration;
import org.eclipse.tracecompass.ctf.core.event.types.Encoding;
import org.eclipse.tracecompass.ctf.core.event.types.EnumDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IEventHeaderDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.VariantDeclaration;
import org.eclipse.tracecompass.ctf.core.trace.CTFReaderException;

/**
 * An event header declaration is a declaration of a structure defined in the
 * CTF spec examples section 6.1.1 . It is used in LTTng traces. This will
 * accelerate reading of the trace.
 *
 * Reminder
 *
 * <pre>
 * struct event_header_compact {
 *     enum : uint5_t { compact = 0 ... 30, extended = 31 } id;
 *     variant <id> {
 *         struct {
 *             uint27_clock_monotonic_t timestamp;
 *         } compact;
 *         struct {
 *             uint32_t id;
 *             uint64_clock_monotonic_t timestamp;
 *         } extended;
 *     } v;
 * } align(8);
 * </pre>
 *
 * @author Matthew Khouzam
 */
@NonNullByDefault
public class EventHeaderCompactDeclaration extends Declaration implements IEventHeaderDeclaration {

    /**
     * The id is 5 bits
     */
    private static final int COMPACT_ID = 5;
    private static final int EXTENDED_VALUE = 31;
    /**
     * Full sized id is 32 bits
     */
    private static final int ID_SIZE = 32;
    /**
     * Full sized timestamp is 64 bits
     */
    private static final int FULL_TS = 64;
    /**
     * Compact timestamp is 27 bits,
     */
    private static final int COMPACT_TS = 27;
    /**
     * Maximum size = largest this header can be
     */
    private static final int MAX_SIZE = 104;
    /**
     * Byte aligned
     */
    private static final int ALIGN = 8;

    private final ByteOrder fByteOrder;

    /**
     * Event Header Declaration
     *
     * @param byteOrder
     *            the byteorder
     */
    public EventHeaderCompactDeclaration(@Nullable ByteOrder byteOrder) {
        if (byteOrder == null) {
            throw new IllegalArgumentException("byteOrder cannot be null"); //$NON-NLS-1$
        }
        fByteOrder = byteOrder;
    }

    private StructDeclaration getReference() {
        StructDeclaration ref = new StructDeclaration(ALIGN);
        EnumDeclaration id = new EnumDeclaration(IntegerDeclaration.createDeclaration(5, false, 10, fByteOrder, Encoding.NONE, "", 8)); //$NON-NLS-1$
        id.add(0, 30, "compact"); //$NON-NLS-1$
        id.add(31, 31, "extended"); //$NON-NLS-1$
        ref.addField("id", id); //$NON-NLS-1$
        VariantDeclaration v = new VariantDeclaration();
        StructDeclaration compact = new StructDeclaration(1);
        compact.addField("timestamp", IntegerDeclaration.createDeclaration(27, false, 10, fByteOrder, Encoding.NONE, "", 1)); //$NON-NLS-1$ //$NON-NLS-2$
        StructDeclaration extended = new StructDeclaration(8);
        extended.addField("id", IntegerDeclaration.createDeclaration(32, false, 10, fByteOrder, Encoding.NONE, "", 8)); //$NON-NLS-1$ //$NON-NLS-2$
        extended.addField("timestamp", IntegerDeclaration.createDeclaration(64, false, 10, fByteOrder, Encoding.NONE, "clock_monotonic", 8)); //$NON-NLS-1$ //$NON-NLS-2$
        v.addField("compact", compact); //$NON-NLS-1$
        v.addField("extended", extended); //$NON-NLS-1$
        ref.addField("v", v); //$NON-NLS-1$
        return ref;
    }

    @Override
    public EventHeaderDefinition createDefinition(@Nullable IDefinitionScope definitionScope, String fieldName, BitBuffer input) throws CTFReaderException {
        alignRead(input);
        ByteOrder bo = input.getByteOrder();
        input.setByteOrder(fByteOrder);
        int enumId = (int) input.get(COMPACT_ID, false);
        if (enumId != EXTENDED_VALUE) {
            long timestamp2 = input.get(COMPACT_TS, false);
            input.setByteOrder(bo);
            return new EventHeaderDefinition(this, enumId, timestamp2, COMPACT_TS);
        }
        // needed since we read 5 bits
        input.position(input.position() + 3);
        long id = input.get(ID_SIZE, false);
        if (id > Integer.MAX_VALUE) {
            throw new CTFReaderException("ID " + id + " larger than " + Integer.MAX_VALUE + " is currently unsupported by the parser"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
        }
        long timestampLong = input.get(FULL_TS, false);
        input.setByteOrder(bo);
        return new EventHeaderDefinition(this, (int) id, timestampLong, FULL_TS);

    }

    @Override
    public long getAlignment() {
        return ALIGN;
    }

    @Override
    public int getMaximumSize() {
        return MAX_SIZE;
    }

    /**
     * Check if a given struct declaration is an event header
     *
     * @param declaration
     *            the declaration
     * @return true if the struct is a compact event header
     */
    public boolean isCompactEventHeader(@Nullable StructDeclaration declaration) {
        if (declaration == null) {
            return false;
        }
        return getReference().equals(declaration);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (fByteOrder.equals(ByteOrder.BIG_ENDIAN) ? 4321 : 1234);
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EventHeaderCompactDeclaration other = (EventHeaderCompactDeclaration) obj;
        if (!fByteOrder.equals(other.fByteOrder)) {
            return false;
        }
        return true;
    }

}
