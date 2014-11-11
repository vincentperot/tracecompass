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

package org.eclipse.tracecompass.internal.lttng2.kernel.core.event.criterion;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.criterion.ITmfEventCriterion;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;

import com.google.common.collect.ImmutableList;

/**
 * Event table columns for LTTng 2.x kernel traces
 */
public final class LttngEventCriteria {

    private LttngEventCriteria() {}

    @SuppressWarnings("null")
    private static final @NonNull Iterable<ITmfEventCriterion> LTTNG_CRITERIA =
            ImmutableList.of(
                    ITmfEventCriterion.BaseCriteria.TIMESTAMP,
                    new LttngChannelCriterion(),
                    ITmfEventCriterion.BaseCriteria.EVENT_TYPE,
                    ITmfEventCriterion.BaseCriteria.CONTENTS);

    private static class LttngChannelCriterion implements ITmfEventCriterion {

        @Override
        public String getName() {
            String ret = Messages.CriterionName_Channel;
            return (ret == null ? EMPTY_STRING : ret);
        }

        @Override
        public String resolveCriterion(ITmfEvent event) {
            if (!(event instanceof CtfTmfEvent)) {
                return EMPTY_STRING;
            }
            String ret = ((CtfTmfEvent) event).getReference();
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
     * Get the criteria defined for LTTng kernel traces.
     *
     * @return The set of criteria
     */
    public static Iterable<ITmfEventCriterion> getCriteria() {
        return LTTNG_CRITERIA;
    }
}
