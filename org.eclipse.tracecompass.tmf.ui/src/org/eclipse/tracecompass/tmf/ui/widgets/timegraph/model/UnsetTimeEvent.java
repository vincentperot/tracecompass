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

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.tmf.core.util.Pair;

/**
 * An "unset" value for ITimeEvent. This can be used instead of null references
 * for time events.
 *
 * @author Alexandre Montplaisir
 */
@NonNullByDefault
public final class UnsetTimeEvent implements ITimeEvent2 {

    private UnsetTimeEvent() {}

    private static final UnsetTimeEvent INSTANCE = new UnsetTimeEvent();

    /**
     * Get the singleton instance of this object.
     *
     * @return The singleton instance
     */
    public static UnsetTimeEvent instance() {
        return INSTANCE;
    }

    @Override
    public ITimeGraphEntry getEntry() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getDuration() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Pair<ITimeEvent, ITimeEvent> split(long time) {
        throw new UnsupportedOperationException();
    }

}
