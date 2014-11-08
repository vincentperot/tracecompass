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

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Event criterion representing the CPU of a trace event.
 *
 * This is not used by default anywhere, but trace types that do have the notion
 * of CPU can use this to expose it in their traces.
 *
 * @author Alexandre Montplaisir
 */
public abstract class TmfCpuCriterion implements ITmfEventCriterion {

    @Override
    public String getName() {
        String ret = Messages.CriteronName_CPU;
        return (ret == null ? EMPTY_STRING : ret);
    }

    @Override
    public abstract String resolveCriterion(ITmfEvent event);

    @Override
    public String getHelpText() {
        return EMPTY_STRING;
    }
}
