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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.pcap.core.event.PcapEvent;
import org.eclipse.tracecompass.internal.tmf.pcap.core.protocol.TmfPcapProtocol;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;

/**
 * The "packet source" aspect for pcap events
 *
 * @author Alexandre Montplaisir
 */
public class PcapSourceAspect implements ITmfEventAspect {

    @Override
    public String getName() {
        String ret = Messages.PcapAspectName_Source;
        return (ret == null ? EMPTY_STRING : ret);
    }

    @Override
    public String resolve(ITmfEvent event) {
        if (!(event instanceof PcapEvent)) {
            return EMPTY_STRING;
        }
        PcapEvent pcapEvent = (PcapEvent) event;
        TmfPcapProtocol protocol = pcapEvent.getMostEncapsulatedProtocol();

        String ret = pcapEvent.getSourceEndpoint(protocol);
        return (ret == null ? EMPTY_STRING : ret);
    }

    @Override
    public String getHelpText() {
        return EMPTY_STRING;
    }

    @Override
    public @NonNull String getFilterId() {
        return PcapEvent.EVENT_FIELD_PACKET_SOURCE;
    }
}