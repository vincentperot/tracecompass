/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, Ecole Polytechnique de Montreal and others
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

import java.util.Collections;
import java.util.Map;

import org.eclipse.tracecompass.ctf.core.CTFStrings;
import org.eclipse.tracecompass.ctf.core.event.types.IDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IResolvable;
import org.eclipse.tracecompass.ctf.core.event.types.StructDefinition;

import com.google.common.collect.ImmutableMap.Builder;

/**
 * <b><u>StreamInputPacketIndexEntry</u></b>
 * <p>
 * Represents an entry in the index of event packets.
 */
public class StreamInputPacketIndexEntry implements Comparable<StreamInputPacketIndexEntry> {

    private static final long BITS_PER_BYTE = 8L;



    private static final String CTF_DEVICE = "device"; //$NON-NLS-1$



    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Offset of the packet in the file, in bytes
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
    private final Map<String, Object> fAttributes;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs an index entry.
     *
     * @param dataOffsetBits
     *            offset in the file for the start of data
     * @param fileSizeBytes
     *            number of bytes in a file
     */

    public StreamInputPacketIndexEntry(long dataOffsetBits, long fileSizeBytes) {
        fContentSizeBits = (fileSizeBytes * 8);
        fPacketSizeBits = (fileSizeBytes * 8);
        fOffsetBytes = dataOffsetBits;
        fLostEvents = 0;
        fTarget = ""; //$NON-NLS-1$
        fTargetID = 0;
        fTimestampBegin = Long.MIN_VALUE;
        fTimestampEnd = Long.MAX_VALUE;
        fAttributes = Collections.emptyMap();
    }

    /**
     * full Constructor
     *
     * @param dataOffsetBits
     *            offset in the file for the start of data
     * @param streamPacketContextDef
     *            packet context
     * @param fileSizeBytes
     *            number of bytes in a file
     * @param lostSoFar
     *            number of lost events so far
     */
    public StreamInputPacketIndexEntry(long dataOffsetBits, StructDefinition streamPacketContextDef, long fileSizeBytes, long lostSoFar) {
        Builder<String, Object> builder = new Builder<>();
        for (String field : streamPacketContextDef.getDeclaration()
                .getFieldsList()) {
            IDefinition definition = streamPacketContextDef.lookupDefinition(field);
            if (definition instanceof IResolvable) {
                builder.put(field, ((IResolvable) definition).resolve());
            }
        }
        fAttributes = builder.build();

        Long contentSize = (Long) this.lookupAttribute(CTFStrings.CONTENT_SIZE);
        Long packetSize = (Long) this.lookupAttribute(CTFStrings.PACKET_SIZE);
        Long tsBegin = (Long) this.lookupAttribute(CTFStrings.TIMESTAMP_BEGIN);
        Long tsEnd = (Long) this.lookupAttribute(CTFStrings.TIMESTAMP_END);
        String device = (String) this.lookupAttribute(CTF_DEVICE);
        // LTTng Specific
        Long cpuId = (Long) this.lookupAttribute(CTFStrings.CPU_ID);
        Long lostEvents = (Long) this.lookupAttribute(CTFStrings.EVENTS_DISCARDED);

        /* Read the content size in bits */
        if (contentSize != null) {
            fContentSizeBits = (contentSize.longValue());
        } else if (packetSize != null) {
            fContentSizeBits = (packetSize.longValue());
        } else {
            fContentSizeBits = (fileSizeBytes * BITS_PER_BYTE);
        }

        /* Read the packet size in bits */
        if (packetSize != null) {
            fPacketSizeBits = (packetSize.longValue());
        } else if (this.getContentSizeBits() != 0) {
            fPacketSizeBits = (getContentSizeBits());
        } else {
            fPacketSizeBits = (fileSizeBytes * BITS_PER_BYTE);
        }

        /* Read the begin timestamp */
        if (tsBegin != null) {
            fTimestampBegin = (tsBegin.longValue());
        } else {
            fTimestampBegin = Long.MIN_VALUE;
        }

        /* Read the end timestamp */
        if (tsEnd != null) {
            if (tsEnd == -1) {
                tsEnd = Long.MAX_VALUE;
            }
            fTimestampEnd = (tsEnd.longValue());
        } else {
            fTimestampEnd = Long.MAX_VALUE;
        }

        if (device != null) {
            fTarget = device;
            fTargetID = Integer.parseInt(device.replaceAll("[\\D]", "")); //$NON-NLS-1$ //$NON-NLS-2$ // slow
        } else if (cpuId != null) {
            fTarget = ("CPU" + cpuId.toString()); //$NON-NLS-1$
            fTargetID = cpuId;
        } else {
            fTarget = ""; //$NON-NLS-1$
            fTargetID = 0;
        }

        if (lostEvents != null) {
            fLostEvents = (lostEvents - lostSoFar);
        } else {
            fLostEvents = 0;
        }

        fOffsetBytes = dataOffsetBits;
    }

    /**
     * Assign a target to this index entry
     *
     * @param target
     *            The target to assign
     */
    public void setTarget(String target) {

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
    boolean includes(long ts) {
        return (ts >= fTimestampBegin) && (ts <= fTimestampEnd);
    }

    @Override
    public String toString() {
        return "StreamInputPacketIndexEntry [offsetBytes=" + fOffsetBytes //$NON-NLS-1$
                + ", timestampBegin=" + fTimestampBegin + ", timestampEnd=" //$NON-NLS-1$ //$NON-NLS-2$
                + fTimestampEnd + "]"; //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // Getters and Setters
    // ------------------------------------------------------------------------

    /**
     * @return the offsetBytes
     */
    public long getOffsetBytes() {
        return fOffsetBytes;
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

    @Override
    public int compareTo(StreamInputPacketIndexEntry o) {
        if (fTimestampBegin > o.fTimestampBegin) {
            return 1;
        }
        if (fTimestampBegin < o.fTimestampBegin) {
            return -1;
        }
        if (fTimestampEnd > o.fTimestampEnd) {
            return 1;
        }
        if (fTimestampEnd < o.fTimestampEnd) {
            return -1;
        }
        return 0;
    }
}