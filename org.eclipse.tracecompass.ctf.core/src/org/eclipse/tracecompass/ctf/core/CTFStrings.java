/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Non-externalized strings for use with the CTF plugin (event names, field
 * names, etc.)
 *
 * @author Alexandre Montplaisir
 * @since 2.2
 */
@SuppressWarnings("nls")
@NonNullByDefault
public interface CTFStrings {

    /** Event name for lost events */
    String LOST_EVENT_NAME = "Lost event";

    /**
     * Name of the field in lost events indicating how many actual events were
     * lost
     */
    String LOST_EVENTS_FIELD = "Lost events";

    /** Name of the field in lost events indicating the time range */
    static final String LOST_EVENTS_DURATION = "duration";

    /** The last timestamp of a packet context */
    public static final String TIMESTAMP_END = "timestamp_end";

    /** The first timestamp of a packet context */
    public static final String TIMESTAMP_BEGIN = "timestamp_begin";

    /** The size of a packet in a packet context */
    public static final String PACKET_SIZE = "packet_size";

    /** The amount of data used in a packet in a packet context */
    public static final String CONTENT_SIZE = "content_size";

    /** The cpu_id of a packet in a packet context */
    public static final String CPU_ID = "cpu_id";

    /** The number of lost events in a packet in a packet context */
    public static final String EVENTS_DISCARDED = "events_discarded";
}
