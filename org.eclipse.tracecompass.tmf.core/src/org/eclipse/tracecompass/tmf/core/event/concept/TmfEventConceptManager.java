/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event.concept;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Event concept manager
 *
 * TODO: Dumbly taking the event names from concept and mapping them is not
 * flexible enough. A concept may be implemented by an event based on a prefix
 * (for instance OS system calls) or a combination of event name/fields, so it
 * needs rework.
 *
 * @author Geneviève Bastien
 */
public final class TmfEventConceptManager {

    private static final Map<Class<? extends TmfTrace>, Multimap<String, IEventConcept>> EVENT_CONCEPTS = new HashMap<>();

    private TmfEventConceptManager() {
    }

    /**
     * @param traceClass
     *            The class of the trace
     * @param concept
     *            The concept to add for this class
     */
    public static void registerConcept(Class<? extends TmfTrace> traceClass, IEventConcept concept) {
        Multimap<String, IEventConcept> conceptMap = EVENT_CONCEPTS.get(traceClass);
        if (conceptMap == null) {
            conceptMap = HashMultimap.create();
            EVENT_CONCEPTS.put(traceClass, conceptMap);
        }
        for (String eventName : concept.getEventNames()) {
            conceptMap.put(eventName, concept);
        }
    }

    /**
     * @param trace
     *            The trace for which to get the concepts
     * @param event
     *            The event
     * @return The applicable event concepts
     */
    public static Collection<IEventConcept> getConceptsFor(ITmfTrace trace, ITmfEvent event) {
        Multimap<String, IEventConcept> conceptMap = EVENT_CONCEPTS.get(trace.getClass());
        if (conceptMap == null) {
            return NonNullUtils.checkNotNull(Collections.EMPTY_SET);
        }
        String eventName = event.getType().getName();
        return NonNullUtils.checkNotNull(conceptMap.get(eventName));
    }

}
