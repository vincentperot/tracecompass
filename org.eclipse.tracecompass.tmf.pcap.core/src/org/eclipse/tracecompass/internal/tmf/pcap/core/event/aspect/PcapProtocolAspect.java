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
 *   Alexandre Montplaisir - Update to new ITmfEventAspect API
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.pcap.core.event.aspect;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.pcap.core.event.PcapEvent;
import org.eclipse.tracecompass.internal.tmf.pcap.core.protocol.TmfPcapProtocol;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;

/**
 * The "packet protocol" aspect for pcap events
 *
 * @author Alexandre Montplaisir
 */
public class PcapProtocolAspect implements ITmfEventAspect {

    @Override
    public String getName() {
        return Messages.getMessage(Messages.PcapAspectName_Protocol);
    }

    @Override
    public String getHelpText() {
        return EMPTY_STRING;
    }

    @Override
    public String resolve(ITmfEvent event) {
        if (!(event instanceof PcapEvent)) {
            return EMPTY_STRING;
        }
        PcapEvent pcapEvent = (PcapEvent) event;
        TmfPcapProtocol protocol = pcapEvent.getMostEncapsulatedProtocol();

        return NonNullUtils.checkForNull(protocol.getShortName().toUpperCase());
    }

    @Override
    public @Nullable String getFilterId() {
        return PcapEvent.EVENT_FIELD_PACKET_PROTOCOL;
    }
}
