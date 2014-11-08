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
 * A criterion representing the contents (or payload) of an event.
 *
 * It does not look for an individual field, it puts all the fields into one
 * String.
 *
 * @author Alexandre Montplaisir
 */
public class TmfContentsCriterion implements ITmfEventCriterion {

    private static final @Nullable String NAME = Messages.CriterionName_Contents;

    /**
     * Non-public constructor, should only be accessed via
     * {@link ITmfEventCriterion.BaseCriteria#CONTENTS} to use the singleton
     * instance.
     */
    TmfContentsCriterion() {}

    @Override
    public String getName() {
        String ret = NAME;
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

}
