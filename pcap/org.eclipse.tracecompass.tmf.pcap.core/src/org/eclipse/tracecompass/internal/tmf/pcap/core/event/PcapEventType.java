/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.pcap.core.event;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;

/**
 * Class that represents the type of a PcapEvent.
 *
 * @author Vincent Perot
 */
public class PcapEventType {

    /**
     * The default Pcap Type ID for a PcapEvent
     */
    public static final String DEFAULT_PCAP_TYPE_ID = NonNullUtils.nullToEmptyString(Messages.PcapEventType_DefaultTypeID);

    private final String fName;

    /**
     * Default constructor
     */
    public PcapEventType() {
        fName = DEFAULT_PCAP_TYPE_ID;
    }

    /**
     * Full constructor
     *
     * @param typeId
     *            the type name
     */
    public PcapEventType(final String typeId) {
        fName = typeId;
    }

    /**
     * Copy constructor
     *
     * @param type
     *            the other type
     */
    public PcapEventType(final PcapEventType type) {
        fName = type.fName;
    }

    /**
     * @return the type name
     */
    public String getName() {
        return fName;
    }

    @Override
    public @Nullable String toString() {
        return fName;
    }

}
