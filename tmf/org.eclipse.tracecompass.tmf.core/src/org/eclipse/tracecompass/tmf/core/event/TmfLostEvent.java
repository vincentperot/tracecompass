/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Alexandre Montplaisir - Made immutable
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event;

import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * A basic implementation of ITmfLostEvent.
 *
 * @author Francois Chouinard
 */
public class TmfLostEvent extends TmfEvent implements ITmfLostEvent {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final TmfTimeRange fTimeRange;
    private final long fNbLostEvents;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Full constructor
     *
     * @param trace
     *            the parent trace
     * @param rank
     *            the event rank (in the trace)
     * @param timestamp
     *            the event timestamp
     * @param type
     *            the event type
     * @param timeRange
     *            the 'problematic' time range
     * @param nbLostEvents
     *            the number of lost events in the time range
     */
    public TmfLostEvent(final ITmfTrace trace,
            final long rank,
            final ITmfTimestamp timestamp,
            final ITmfEventType type,
            final TmfTimeRange timeRange,
            final long nbLostEvents) {
        super(trace, rank, timestamp, type, null);
        fTimeRange = timeRange;
        fNbLostEvents = nbLostEvents;
    }

    // ------------------------------------------------------------------------
    // ITmfLostEvent
    // ------------------------------------------------------------------------

    @Override
    public TmfTimeRange getTimeRange() {
        return fTimeRange;
    }

    @Override
    public long getNbLostEvents() {
        return fNbLostEvents;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return getClass().getSimpleName() + " [Event=" + super.toString() +
                ", fTimeRange=" + fTimeRange + ", fNbLostEvents=" + fNbLostEvents + "]";
    }

}
