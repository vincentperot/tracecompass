/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Alexandre Montplaisir - Update for TmfEventTableColumn
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.parsers.custom;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.criterion.ITmfEventCriterion;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomEvent;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition.OutputColumn;

import com.google.common.collect.ImmutableList;

/**
 * Event criteria for Custom {Text|XML} traces.
 *
 * Since this definition will be different for every single custom trace, we
 * cannot define specific {@link ITmfEventCriterion} in advance.
 *
 * Instead, one has to call {@link #generateCriteria(CustomTraceDefinition)}
 * with the CustomTraceDefinition of the the particular trace to display.
 *
 * @author Alexandre Montplaisir
 */
public class CustomEventCriteria {

    /**
     * Criteria for custom events, which uses an integer ID to represent each
     * field.
     */
    private static final class CustomEventFieldCriterion implements ITmfEventCriterion {

        private final @NonNull String fName;
        private final int fIndex;

        /**
         * Constructor
         *
         * @param name
         *            The name of this criteria
         * @param idx
         *            The index of this field in the event's content to display
         */
        public CustomEventFieldCriterion(@NonNull String name, int idx) {
            fName = name;
            fIndex = idx;
        }

        @Override
        public String getName() {
            return fName;
        }

        @Override
        public String resolveCriterion(ITmfEvent event) {
            if (event instanceof CustomEvent) {
                String ret = ((CustomEvent) event).getEventString(fIndex);
                return (ret == null ? EMPTY_STRING : ret);
            }
            return EMPTY_STRING;
        }

        @Override
        public String getHelpText() {
            return EMPTY_STRING;
        }

        @Override
        public String getFilterId() {
            return fName;
        }
    }

    /**
     * Build a set of criteria for a given trace definition
     *
     * @param definition
     *            The {@link CustomTraceDefinition} of the trace for which you
     *            want the columns
     * @return The set of criteria for the given trace
     */
    public static Iterable<ITmfEventCriterion> generateCriteria(CustomTraceDefinition definition) {
        ImmutableList.Builder<ITmfEventCriterion> builder = new ImmutableList.Builder<>();
        List<OutputColumn> outputs = definition.outputs;
        for (int i = 0; i < outputs.size(); i++) {
            String name = outputs.get(i).name;
            if (name != null) {
                builder.add(new CustomEventFieldCriterion(name, i));
            }
        }
        return builder.build();
    }
}
