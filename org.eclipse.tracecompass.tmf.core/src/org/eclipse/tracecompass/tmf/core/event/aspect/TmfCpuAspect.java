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

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Event aspect representing the CPU of a trace event.
 *
 * This is not used by default anywhere, but trace types that do have the notion
 * of CPU can use this to expose it in their traces.
 *
 * TODO Move to an eventualy kernel-trace-specific plugin.
 *
 * @author Alexandre Montplaisir
 */
public abstract class TmfCpuAspect implements ITmfEventAspect {

    @Override
    public String getName() {
        String ret = Messages.AspectName_CPU;
        return (ret == null ? EMPTY_STRING : ret);
    }

    @Override
    public abstract String resolve(ITmfEvent event);

    @Override
    public String getHelpText() {
        return EMPTY_STRING;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        /*
         * Consider all sub-instance of this type "equal", so that they get
         * merged in a single CPU column/aspect.
         */
        if (other instanceof TmfCpuAspect) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
