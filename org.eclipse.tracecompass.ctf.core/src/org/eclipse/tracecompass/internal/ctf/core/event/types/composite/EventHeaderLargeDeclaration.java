/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Matthew Khouzam - Initial API and implementation
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
 * struct event_header_large {
 *     enum : uint16_t { compact = 0 ... 65534, extended = 65535 } id;
 *     variant <id> {
 *         struct {
 *             uint32_clock_monotonic_t timestamp;
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
public class EventHeaderLargeDeclaration extends Declaration implements IEventHeaderDeclaration {

    /**
     * The id is 16 bits
     */
    private static final int COMPACT_ID = 16;
    private static final int EXTENDED_VALUE = 65535;
    /**
     * Full sized timestamp is 64 bits
     */
    private static final int FULL_TS = 64;
    /**
     * Compact timestamp is 32 bits,
     */
    private static final int COMPACT_TS = 32;
    /**
     * Maximum size = largest this header can be
     */
    private static final int MAX_SIZE = 112;
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
    public EventHeaderLargeDeclaration(@Nullable ByteOrder byteOrder) {
        if (byteOrder == null) {
            throw new IllegalArgumentException("byteOrder cannot be null"); //$NON-NLS-1$
        }
        fByteOrder = byteOrder;
    }

    private StructDeclaration getReference() {
        StructDeclaration ref = new StructDeclaration(ALIGN);
        EnumDeclaration id = new EnumDeclaration(IntegerDeclaration.createDeclaration(16, false, 10, fByteOrder, Encoding.NONE, "", 8)); //$NON-NLS-1$
        id.add(0, 65534, "compact"); //$NON-NLS-1$
        id.add(65535, 65535, "extended"); //$NON-NLS-1$
        ref.addField("id", id); //$NON-NLS-1$
        VariantDeclaration v = new VariantDeclaration();
        StructDeclaration compact = new StructDeclaration(1);
        compact.addField("timestamp", IntegerDeclaration.createDeclaration(32, false, 10, fByteOrder, Encoding.NONE, "", 8)); //$NON-NLS-1$ //$NON-NLS-2$
        StructDeclaration extended = new StructDeclaration(1);
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
        int first = (int) input.get(COMPACT_ID, false);
        long second = input.get(COMPACT_TS, false);
        if (first != EXTENDED_VALUE) {
            input.setByteOrder(bo);
            return new EventHeaderDefinition(this, first, second, COMPACT_TS);
        }
        long timestampLong = input.get(FULL_TS, false);
        input.setByteOrder(bo);
        if (second > Integer.MAX_VALUE) {
            throw new CTFReaderException("ID " + second + " larger than " + Integer.MAX_VALUE + " is currently unsupported by the parser"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
        }
        return new EventHeaderDefinition(this, (int) second, timestampLong, FULL_TS);
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
     * @return true if the event is a large event header
     */
    public boolean isLargeEventHeader(@Nullable StructDeclaration declaration) {
        if (declaration == null) {
            return false;
        }
        return declaration.equals(getReference());
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
        EventHeaderLargeDeclaration other = (EventHeaderLargeDeclaration) obj;
        if (!fByteOrder.equals(other.fByteOrder)) {
            return false;
        }
        return true;
    }

}
