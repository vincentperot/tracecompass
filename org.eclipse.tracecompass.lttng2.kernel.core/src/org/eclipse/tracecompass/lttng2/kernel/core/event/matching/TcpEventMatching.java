/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.event.matching;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.tracecompass.internal.lttng2.kernel.core.TcpEventStrings;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.matching.ITmfNetworkMatchDefinition;
import org.eclipse.tracecompass.tmf.core.event.matching.TmfEventMatching.MatchingType;
import org.eclipse.tracecompass.tmf.core.event.matching.TmfNetworkEventMatching.Direction;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfEventTypeCollectionHelper;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;

import com.google.common.collect.ImmutableSet;

/**
 * Class to match tcp type events. This matching class applies to traces
 * obtained with the 'addons' lttng module. This module can be obtained with
 * lttng-modules to generate traces at
 * https://github.com/giraldeau/lttng-modules/tree/addons
 *
 * Note: this module only allows to generate traces to be read and analyzed by
 * TMF, no code from this module is being used here
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class TcpEventMatching implements ITmfNetworkMatchDefinition {

    private static final ImmutableSet<String> REQUIRED_EVENTS = ImmutableSet.of(
            TcpEventStrings.INET_SOCK_LOCAL_IN,
            TcpEventStrings.INET_SOCK_LOCAL_OUT);

    private static boolean canMatchPacket(final ITmfEvent event) {
        /* Make sure all required fields are present to match with this event */
        ITmfEventField content = event.getContent();
        if ((content.getField(TcpEventStrings.SEQ) != null) &&
                (content.getField(TcpEventStrings.ACKSEQ) != null) && (content.getField(TcpEventStrings.FLAGS) != null)) {
            return true;
        }
        return false;
    }

    @Override
    public Direction getDirection(ITmfEvent event) {
        String evname = event.getType().getName();

        if (!canMatchPacket(event)) {
            return null;
        }

        /* Is the event a tcp socket in or out event */
        if (evname.equals(TcpEventStrings.INET_SOCK_LOCAL_IN)) {
            return Direction.IN;
        } else if (evname.equals(TcpEventStrings.INET_SOCK_LOCAL_OUT)) {
            return Direction.OUT;
        }
        return null;
    }

    /**
     * The key to uniquely identify a TCP packet depends on many fields. This
     * method computes the key for a given event.
     *
     * @param event
     *            The event for which to compute the key
     * @return the unique key for this event
     */
    @Override
    public List<Object> getUniqueField(ITmfEvent event) {
        List<Object> keys = new ArrayList<>();

        keys.add(event.getContent().getField(TcpEventStrings.SEQ).getValue());
        keys.add(event.getContent().getField(TcpEventStrings.ACKSEQ).getValue());
        keys.add(event.getContent().getField(TcpEventStrings.FLAGS).getValue());

        return keys;
    }

    @Override
    public boolean canMatchTrace(ITmfTrace trace) {
        if (!(trace instanceof CtfTmfTrace)) {
            return false;
        }
        CtfTmfTrace ktrace = (CtfTmfTrace) trace;

        Set<String> traceEvents = TmfEventTypeCollectionHelper.getEventNames(ktrace.getContainedEventTypes());
        traceEvents.retainAll(REQUIRED_EVENTS);
        return !traceEvents.isEmpty();
    }

    @Override
    public MatchingType[] getApplicableMatchingTypes() {
        MatchingType[] types = { MatchingType.NETWORK };
        return types;
    }

}
