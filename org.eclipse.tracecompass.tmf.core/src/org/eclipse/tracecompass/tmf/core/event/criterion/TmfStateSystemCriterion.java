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
import org.eclipse.osgi.util.NLS;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Criterion representing a query in a given state system, at the timestamp of
 * the event.
 *
 * @author Alexandre Montplaisir
 */
public class TmfStateSystemCriterion implements ITmfEventCriterion {

    private final @Nullable String fName;
    private final ITmfStateSystem fSS;
    private final int fAttribute;

    /**
     * Constructor
     *
     * @param name
     *            The name of this criterion. You can use 'null' to use the
     *            default name, which is the (base) name of the attribute.
     * @param ss
     *            The state system in which we want to query
     * @param attribute
     *            The attribute in the state system to look for
     */
    public TmfStateSystemCriterion(@Nullable String name, ITmfStateSystem ss, int attribute) {
        fName = name;
        fSS = ss;
        fAttribute = attribute;
    }

    @Override
    public String getName() {
        String name = fName;
        if (name != null) {
            return name;
        }

        name = fSS.getAttributeName(fAttribute);
        return (name == null ? EMPTY_STRING : name);
    }

    @Override
    public String resolveCriterion(ITmfEvent event) {
        try {
            ITmfStateValue value = fSS.querySingleState(event.getTimestamp().getValue(), fAttribute).getStateValue();

            @SuppressWarnings("null")
            @NonNull String ret = value.toString();
            return ret;
        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            return EMPTY_STRING;
        }
    }

    @Override
    public @NonNull String getHelpText() {
        String ret = NLS.bind(Messages.CriterionHelpText_Statesystem,
                fSS.getSSID(), fSS.getFullAttributePath(fAttribute));
        return (ret == null ? EMPTY_STRING : ret);
    }

    @Override
    public @Nullable String getFilterId() {
        return null;
    }

}
