/*******************************************************************************
 * Copyright (c) 2011, 2015 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.ctf.core.trace;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.tracecompass.ctf.core.event.types.EnumDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.FloatDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.StringDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.StructDefinition;

/**
 * <b><u>StreamInputPacketIndexEntry</u></b>
 * <p>
 * Represents an entry in the index of event packets.
 */
public class StreamInputPacketIndexEntry {

    private static final int UNKNOWN = -1;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Position of the start of the packet header in the file, in bits
     */
    private final long fOffsetBits;

    /**
     * Position of the start of the packet header in the file, in bytes
     */
    private final long fOffsetBytes;

    /**
     * Packet size, in bits
     */
    private final long fPacketSizeBits;

    /**
     * Content size, in bits
     */
    private final long fContentSizeBits;

    /**
     * Begin timestamp
     */
    private final long fTimestampBegin;

    /**
     * End timestamp
     */
    private final long fTimestampEnd;

    /**
     * How many lost events are there?
     */
    private final long fLostEvents;

    /**
     * Which target is being traced
     */
    private final String fTarget;
    private final long fTargetID;

    /**
     * Attributes of this index entry
     */
    private final Map<String, Object> fAttributes = new HashMap<>();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs an index entry.
     *
     * @param dataOffsetBits
     *            offset in the file for the start of data in bits
     * @param fileSizeBytes
     *            number of bytes in a file
     */

    public StreamInputPacketIndexEntry(long dataOffsetBits, long fileSizeBytes) {
        fContentSizeBits = (fileSizeBytes * Byte.SIZE);
        fPacketSizeBits = (fileSizeBytes * Byte.SIZE);
        fOffsetBits = dataOffsetBits;
        fOffsetBytes = dataOffsetBits / Byte.SIZE;
        fLostEvents = 0;
        fTarget = ""; //$NON-NLS-1$
        fTargetID = 0;
        fTimestampBegin = Long.MIN_VALUE;
        fTimestampEnd = Long.MAX_VALUE;
    }

    /**
     * full Constructor
     *
     * @param dataOffsetBits
     *            offset in the file for the start of data in bits
     * @param streamPacketContextDef
     *            packet context
     * @param fileSizeBytes
     *            number of bytes in a file
     * @param lostSoFar
     *            number of lost events so far
     */
    public StreamInputPacketIndexEntry(long dataOffsetBits, StructDefinition streamPacketContextDef, long fileSizeBytes, long lostSoFar) {
        for (String field : streamPacketContextDef.getDeclaration().getFieldsList()) {
            IDefinition id = streamPacketContextDef.lookupDefinition(field);
            if (id instanceof IntegerDefinition) {
                addAttribute(field, ((IntegerDefinition) id).getValue());
            } else if (id instanceof FloatDefinition) {
                addAttribute(field, ((FloatDefinition) id).getValue());
            } else if (id instanceof EnumDefinition) {
                addAttribute(field, ((EnumDefinition) id).getValue());
            } else if (id instanceof StringDefinition) {
                addAttribute(field, ((StringDefinition) id).getValue());
            }
        }

        Long contentSize = (Long) this.lookupAttribute("content_size"); //$NON-NLS-1$
        Long packetSize = (Long) this.lookupAttribute("packet_size"); //$NON-NLS-1$
        Long tsBegin = (Long) this.lookupAttribute("timestamp_begin"); //$NON-NLS-1$
        Long tsEnd = (Long) this.lookupAttribute("timestamp_end"); //$NON-NLS-1$
        String device = (String) this.lookupAttribute("device"); //$NON-NLS-1$
        // LTTng Specific
        Long cpuId = (Long) this.lookupAttribute("cpu_id"); //$NON-NLS-1$
        Long lostEvents = (Long) this.lookupAttribute("events_discarded"); //$NON-NLS-1$

        fContentSizeBits = computeContentSize(fileSizeBytes, contentSize, packetSize);

        fPacketSizeBits = computePacketSize(fileSizeBytes, packetSize);

        fTimestampBegin = computeTsBegin(tsBegin);

        fTimestampEnd = computeTsEnd(tsEnd);

        if (device != null) {
            fTarget = device;
            fTargetID = Integer.parseInt(device.replaceAll("[\\D]", "")); //$NON-NLS-1$ //$NON-NLS-2$ // slow
        } else if (cpuId != null) {
            fTarget = ("CPU" + cpuId.toString()); //$NON-NLS-1$
            fTargetID = cpuId;
        } else {
            fTarget = null;
            fTargetID = UNKNOWN;
        }

        fLostEvents = computeLostEvents(lostSoFar, lostEvents);

        fOffsetBits = dataOffsetBits;
        fOffsetBytes = dataOffsetBits / Byte.SIZE;
    }

    private static long computeContentSize(long fileSizeBytes, Long contentSize, Long packetSize) {
        /* Read the content size in bits */
        if (contentSize != null) {
            return contentSize.longValue();
        } else if (packetSize != null) {
            return packetSize.longValue();
        }
        return fileSizeBytes * Byte.SIZE;
    }

    private long computePacketSize(long fileSizeBytes, Long packetSize) {
        /* Read the packet size in bits */
        if (packetSize != null) {
            return packetSize.longValue();
        } else if (fContentSizeBits != 0) {
            return fContentSizeBits;
        }
        return fileSizeBytes * Byte.SIZE;
    }

    private static long computeTsBegin(Long tsBegin) {
        /* Read the begin timestamp */
        if (tsBegin != null) {
            return tsBegin.longValue();
        }
        return Long.MIN_VALUE;
    }

    private static long computeTsEnd(Long tsEnd) {

        /* Read the end timestamp */
        if (tsEnd != null) {
            // check if tsEnd == unsigned long max value
            if (tsEnd == -1) {
                return Long.MAX_VALUE;
            }
            return tsEnd.longValue();
        }
        return Long.MAX_VALUE;
    }

    private static long computeLostEvents(long lostSoFar, Long lostEvents) {
        if (lostEvents != null) {
            return lostEvents - lostSoFar;
        }
        return 0;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Returns whether the packet includes (inclusively) the given timestamp in
     * the begin-end timestamp range.
     *
     * @param ts
     *            The timestamp to check.
     * @return True if the packet includes the timestamp.
     */
    public boolean includes(long ts) {
        return (ts >= fTimestampBegin) && (ts <= fTimestampEnd);
    }

    @Override
    public String toString() {
        return "StreamInputPacketIndexEntry [offsetBits=" + fOffsetBits //$NON-NLS-1$
                + ", timestampBegin=" + fTimestampBegin + ", timestampEnd=" //$NON-NLS-1$ //$NON-NLS-2$
                + fTimestampEnd + "]"; //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // Getters and Setters
    // ------------------------------------------------------------------------

    /**
     * @return the offsetBytes
     */
    public long getOffsetBits() {
        return fOffsetBits;
    }

    /**
     * @return the packetSizeBits
     */
    public long getPacketSizeBits() {
        return fPacketSizeBits;
    }

    /**
     * @return the contentSizeBits
     */
    public long getContentSizeBits() {
        return fContentSizeBits;
    }

    /**
     * @return the timestampBegin
     */
    public long getTimestampBegin() {
        return fTimestampBegin;
    }

    /**
     * @return the timestampEnd
     */
    public long getTimestampEnd() {
        return fTimestampEnd;
    }

    /**
     * @return the lostEvents in this packet
     */
    public long getLostEvents() {
        return fLostEvents;
    }

    /**
     * Add an attribute to this index entry
     *
     * @param field
     *            The name of the attribute
     * @param value
     *            The value to insert
     */
    public void addAttribute(String field, Object value) {
        fAttributes.put(field, value);
    }

    /**
     * Retrieve the value of an existing attribute
     *
     * @param field
     *            The name of the attribute
     * @return The value that was stored, or null if it wasn't found
     */
    public Object lookupAttribute(String field) {
        return fAttributes.get(field);
    }

    /**
     * @return The target that is being traced
     */
    public String getTarget() {
        return fTarget;
    }

    /**
     * @return The ID of the target
     */
    public long getTargetId() {
        return fTargetID;
    }

    /**
     * Get the offset of the packet in bytes
     *
     * @return The offset of the packet in bytes
     */
    public long getOffsetBytes() {
        return fOffsetBytes;
    }
}
