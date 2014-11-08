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
 * Event criterion getting the event type of the event.
 *
 * @author Alexandre Montplaisir
 */
public class TmfEventTypeCriterion implements ITmfEventCriterion {

    private static final @Nullable String NAME = Messages.CriterionName_EventType;

    /**
     * Non-public constructor, should only be accessed via
     * {@link ITmfEventCriterion.BaseCriteria#EVENT_TYPE} to use the singleton
     * instance.
     */
    TmfEventTypeCriterion() {}

    @Override
    public String getName() {
        String ret = NAME;
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

}
