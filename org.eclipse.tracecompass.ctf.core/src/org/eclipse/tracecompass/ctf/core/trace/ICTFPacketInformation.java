/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.ctf.core.trace;

/**
 * CTF Packet information, can come from a packet header or an index file
 *
 * @since 1.0
 */
public interface ICTFPacketInformation {

    /**
     * Returns whether the packet includes (inclusively) the given timestamp in
     * the begin-end timestamp range.
     *
     * @param ts
     *            The timestamp to check.
     * @return True if the packet includes the timestamp.
     */
    boolean includes(long ts);

    /**
     * @return the offsetBytes
     */
    long getOffsetBits();

    /**
     * @return the packetSizeBits
     */
    long getPacketSizeBits();

    /**
     * @return the contentSizeBits
     */
    long getContentSizeBits();

    /**
     * @return the timestampBegin
     */
    long getTimestampBegin();

    /**
     * @return the timestampEnd
     */
    long getTimestampEnd();

    /**
     * @return the lostEvents in this packet
     */
    long getLostEvents();

    /**
     * Retrieve the value of an existing attribute
     *
     * @param field
     *            The name of the attribute
     * @return The value that was stored, or null if it wasn't found
     */
    Object lookupAttribute(String field);

    /**
     * @return The target that is being traced
     */
    String getTarget();

    /**
     * @return The ID of the target
     */
    long getTargetId();

    /**
     * Get the offset of the packet in bytes
     *
     * @return The offset of the packet in bytes
     */
    long getOffsetBytes();

}