/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.gdbtrace.core.trace;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.gdbtrace.core.event.GdbTraceEvent;
import org.eclipse.tracecompass.internal.gdbtrace.core.event.GdbTraceEventContent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.criterion.ITmfEventCriterion;
import org.eclipse.tracecompass.tmf.core.event.criterion.TmfEventFieldCriterion;

import com.google.common.collect.ImmutableList;

/**
 * Event table column definition for GDB traces.
 *
 * @author Alexandre Montplaisir
 */
public final class GdbEventCriteria {

    private GdbEventCriteria() {}

    @SuppressWarnings("null")
    private static final @NonNull Iterable<ITmfEventCriterion> GDB_CRITERIA = ImmutableList.of(
            new GdbTraceFrameCriterion(),
            new GdbTracepointCriterion(),
            new GdbFileCriterion()
            );

    private static class GdbTraceFrameCriterion extends TmfEventFieldCriterion {
        public GdbTraceFrameCriterion() {
            super(GdbTraceEventContent.TRACE_FRAME,
                    GdbTraceEventContent.TRACE_FRAME);
        }
    }

    private static class GdbTracepointCriterion extends TmfEventFieldCriterion {
        public GdbTracepointCriterion() {
            super(GdbTraceEventContent.TRACEPOINT,
                    GdbTraceEventContent.TRACEPOINT);
        }
    }

    private static class GdbFileCriterion implements ITmfEventCriterion {

        @Override
        public String getName() {
            return "File"; //$NON-NLS-1$
        }

        @Override
        public String resolveCriterion(ITmfEvent event) {
            if (!(event instanceof GdbTraceEvent)) {
                return EMPTY_STRING;
            }
            String ret = ((GdbTraceEvent) event).getReference();
            return (ret == null ? EMPTY_STRING : ret);
        }

        @Override
        public String getHelpText() {
            return EMPTY_STRING;
        }

        @Override
        public String getFilterId() {
            return ITmfEvent.EVENT_FIELD_REFERENCE;
        }
    }

    /**
     * Get the criteria specific to GDB traces.
     *
     * @return The set of criteria
     */
    public static Iterable<ITmfEventCriterion> getCriteria() {
        return GDB_CRITERIA;
    }
}
