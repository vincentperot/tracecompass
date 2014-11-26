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

package org.eclipse.tracecompass.tmf.core.event.aspect;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;

/**
 * An aspect is a piece of information that can be extracted, directly or
 * indirectly, from a trace event.
 *
 * Simple examples could be timestamp, or event fields. But it could also be
 * something like a state system request, at the timestamp of the given event.
 *
 * The aspect can then be used to populate event table columns, to filter
 * on to only keep certain events, to plot XY charts, etc.
 *
 * @author Alexandre Montplaisir
 */
public interface ITmfEventAspect {

    /**
     * Static definition of an empty string. You can use this instead of 'null'!
     */
    String EMPTY_STRING = ""; //$NON-NLS-1$

    /**
     * Some basic aspects that all trace types should be able to use, using
     * methods found in {@link ITmfEvent}.
     */
    interface BaseAspects {

        /**
         * Aspect for the event timestamp
         */
        ITmfEventAspect TIMESTAMP = new ITmfEventAspect() {
            @Override
            public String getName() {
                return Messages.getMessage(Messages.AspectName_Timestamp);
            }

            @Override
            public String getHelpText() {
                return EMPTY_STRING;
            }

            @Override
            public String resolve(ITmfEvent event) {
                String ret = event.getTimestamp().toString();
                return (ret == null ? EMPTY_STRING : ret);
            }

            @Override
            public @NonNull String getFilterId() {
                return ITmfEvent.EVENT_FIELD_TIMESTAMP;
            }
        };

        /**
         * Aspect for the event type
         */
        ITmfEventAspect EVENT_TYPE = new ITmfEventAspect() {
            @Override
            public String getName() {
                return Messages.getMessage(Messages.AspectName_EventType);
            }

            @Override
            public String getHelpText() {
                return Messages.getMessage(Messages.AspectHelpText_EventType);
            }

            @Override
            public String resolve(ITmfEvent event) {
                ITmfEventType type = event.getType();
                if (type == null) {
                    return EMPTY_STRING;
                }
                String typeName = type.getName();
                return (typeName == null ? EMPTY_STRING : typeName);
            }

            @Override
            public @NonNull String getFilterId() {
                return ITmfEvent.EVENT_FIELD_TYPE;
            }
        };

        /**
         * Aspect for the aggregated event contents (fields)
         */
        ITmfEventAspect CONTENTS = new ITmfEventAspect() {
            @Override
            public String getName() {
                return Messages.getMessage(Messages.AspectName_Contents);
            }

            @Override
            public String getHelpText() {
                return Messages.getMessage(Messages.AspectHelpText_Contents);
            }

            @Override
            public String resolve(ITmfEvent event) {
                String ret = event.getContent().toString();
                return (ret == null ? EMPTY_STRING : ret);
            }

            @Override
            public @NonNull String getFilterId() {
                return ITmfEvent.EVENT_FIELD_CONTENT;
            }
        };

        /**
         * Aspect for the trace's name (for experiments with many traces)
         */
        ITmfEventAspect TRACE_NAME = new ITmfEventAspect() {
            @Override
            public String getName() {
                return Messages.getMessage(Messages.AspectName_TraceName);
            }

            @Override
            public String getHelpText() {
                return Messages.getMessage(Messages.AspectHelpText_TraceName);
            }

            @Override
            public String resolve(ITmfEvent event) {
                String ret = event.getTrace().getName();
                return (ret == null ? EMPTY_STRING : ret);
            }

            @Override
            public @Nullable String getFilterId() {
                return null;
            }
        };
    }

    /**
     * Get the name of this aspect. This name will be user-visible and, as such,
     * should be localized.
     *
     * @return The name of this aspect.
     */
    String getName();

    /**
     * Return a descriptive help text of what this aspect does. This could then
     * be shown in tooltip or in option dialogs for instance. It should also be
     * localized.
     *
     * You can return {@link #EMPTY_STRING} if you judge that the aspect name
     * makes it obvious.
     *
     * @return The help text of this aspect
     */
    String getHelpText();

    /**
     * The "functor" representing this aspect. Basically, what to do for an
     * event that is passed in parameter.
     *
     * Note to implementers:
     *
     * The parameter type here is {@link ITmfEvent}. This is because you could
     * receive any type of event here. Do not assume you will only receive
     * events of your own trace type. It is perfectly fine to return
     * {@link #EMPTY_STRING} for event types you don't support.
     *
     * You also can (and should) provide a more specific return type than
     * Object.
     *
     * @param event
     *            The event to process
     * @return The resulting tidbit of information for this event.
     */
    Object resolve(ITmfEvent event);

    /**
     * The filter ID of this aspect. This is currently used by the Filter View,
     * and to filter on columns in the event table.
     *
     * TODO Remove this, replace with calls to {@link #resolve(ITmfEvent)}
     * instead.
     *
     * @return The filter_id
     */
    @Nullable String getFilterId();
}
