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

/**
 * Criterion representing the timestamp of an event.
 *
 * @author Alexandre Montplaisir
 */
public class TmfTimestampCriterion implements ITmfEventCriterion {

    private static final @Nullable String NAME = Messages.CriterionName_Timestamp;

    /**
     * Non-public constructor, should only be accessed via
     * {@link ITmfEventCriterion.BaseCriteria#TIMESTAMP} to use the singleton
     * instance.
     */
    TmfTimestampCriterion() {}

    @Override
    public String getName() {
        String ret = NAME;
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

}
