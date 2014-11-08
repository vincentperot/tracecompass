/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event.criterion;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;

/**
 * A criterion (plural: criteria), in the sense of a "characterizing mark of
 * something", is a piece of information that can be extracted, directly or
 * indirectly, from a trace event.
 *
 * Simple examples could be timestamp, or event fields. But it could also be
 * something like a state system request, at the timestamp of the given event.
 *
 * A criterion can then be used to populate event table columns, or to filter
 * on to only keep certain events.
 *
 * @author Alexandre Montplaisir
 */
public interface ITmfEventCriterion {

    /**
     * Static definition of an empty string. You can use this instead of 'null'!
     */
    String EMPTY_STRING = ""; //$NON-NLS-1$

    /**
     * Some basic criteria that all trace types should be able to use, using
     * methods found in {@link ITmfEvent}.
     */
    interface BaseCriteria {

        /** Criterion for the event timestamp */
        ITmfEventCriterion TIMESTAMP = new ITmfEventCriterion() {
            @Override
            public String getName() {
                String ret = Messages.CriterionName_Timestamp;
                return (ret == null ? EMPTY_STRING : ret);
            }

            @Override
            public String resolveCriterion(ITmfEvent event) {
                String ret = event.getTimestamp().toString();
                return (ret == null ? EMPTY_STRING : ret);
            }

            @Override
            public String getHelpText() {
                return EMPTY_STRING;
            }

            @Override
            public @NonNull String getFilterId() {
                return ITmfEvent.EVENT_FIELD_TIMESTAMP;
            }
        };

        /** Criterion for the event type */
        ITmfEventCriterion EVENT_TYPE = new ITmfEventCriterion() {
            @Override
            public String getName() {
                String ret = Messages.CriterionName_EventType;
                return (ret == null ? EMPTY_STRING : ret);
            }

            @Override
            public String resolveCriterion(ITmfEvent event) {
                ITmfEventType type = event.getType();
                if (type == null) {
                    return EMPTY_STRING;
                }
                String typeName = type.getName();
                return (typeName == null ? EMPTY_STRING : typeName);
            }

            @Override
            public String getHelpText() {
                return EMPTY_STRING;
            }

            @Override
            public @NonNull String getFilterId() {
                return ITmfEvent.EVENT_FIELD_TYPE;
            }
        };

        /** Criterion for the aggregated event contents (fields) */
        ITmfEventCriterion CONTENTS = new ITmfEventCriterion() {
            @Override
            public String getName() {
                String ret = Messages.CriterionName_Contents;
                return (ret == null ? EMPTY_STRING : ret);
            }

            @Override
            public String resolveCriterion(ITmfEvent event) {
                String ret = event.getContent().toString();
                return (ret == null ? EMPTY_STRING : ret);
            }

            @Override
            public String getHelpText() {
                return EMPTY_STRING;
            }

            @Override
            public @NonNull String getFilterId() {
                return ITmfEvent.EVENT_FIELD_CONTENT;
            }
        };
    }

    /**
     * Get the name of this criterion. This name will be user-visible and, as
     * such, should be localized.
     *
     * @return The name of this criterion.
     */
    String getName();

    /**
     * The "functor" representing this criterion. Basically, what to do for an
     * event that is passed in parameter.
     *
     * Note to implementers:
     *
     * The parameter type here is {@link ITmfEvent}. This is because you could
     * receive any type of event here. Do not assume you will only receive
     * events of your own trace type. It is perfectly fine to return
     * {@link #EMPTY_STRING} for event types you don't support.
     *
     * @param event
     *            The event to process
     * @return The resulting tidbit of information for this event.
     */
    String resolveCriterion(ITmfEvent event);

    /**
     * Return a descriptive help text of what this criterion does. This could
     * then be shown in tooltip or in option dialogs for instance. It should
     * also be localized.
     *
     * You can return {@link #EMPTY_STRING} if you judge the criterion name
     * makes it obvious.
     *
     * @return The help text of this criterion
     */
    String getHelpText();

    /**
     * The filter ID of this criterion. This is currently used by the Filter
     * View, and to filter on columns in the event table.
     *
     * TODO Remove this, replace with calls to {@link #resolveCriterion}
     * instead.
     *
     * @return The filter_id
     */
    @Nullable String getFilterId();
}
