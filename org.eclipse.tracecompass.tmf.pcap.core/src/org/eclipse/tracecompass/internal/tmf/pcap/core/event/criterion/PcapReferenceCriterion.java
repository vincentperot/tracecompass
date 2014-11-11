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
 *   Alexandre Montplaisir - Update to new ITmfEventCriterion API
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.pcap.core.event.criterion;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.pcap.core.event.PcapEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.criterion.ITmfEventCriterion;

/**
 * The "packet reference" criterion for pcap events
 *
 * @author Alexandre Montplaisir
 */
public class PcapReferenceCriterion implements ITmfEventCriterion {

    @Override
    public String getName() {
        String ret = Messages.PcapCriterionName_Reference;
        return (ret == null ? EMPTY_STRING : ret);
    }

    @Override
    public String resolveCriterion(ITmfEvent event) {
        if (!(event instanceof PcapEvent)) {
            return EMPTY_STRING;
        }
        return ((PcapEvent) event).getReference();
    }

    @Override
    public String getHelpText() {
        return EMPTY_STRING;
    }

    @Override
    public @Nullable String getFilterId() {
        return ITmfEvent.EVENT_FIELD_REFERENCE;
    }
}
