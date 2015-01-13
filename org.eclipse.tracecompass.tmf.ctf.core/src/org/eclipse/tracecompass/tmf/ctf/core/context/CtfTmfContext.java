/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Simon Delisle - Remove the iterator in dispose()
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.context;

import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.eclipse.tracecompass.tmf.ctf.core.trace.iterator.CtfIterator;

/**
 * Lightweight Context for CtfTmf traces. Should only use 3 references, 1 ref to
 * a boxed Long, a long and an int.
 *
 * @author Matthew Khouzam
 * @version 1.0
 * @since 2.0
 */
public class CtfTmfContext implements ITmfContext {

    // -------------------------------------------
    // Fields
    // -------------------------------------------

    private CtfLocation fCurLocation;
    private long fCurRank;

    private final CtfTmfTrace fTrace;

    // -------------------------------------------
    // Constructor
    // -------------------------------------------

    /**
     * Constructor
     *
     * @param ctfTmfTrace
     *            the parent trace
     * @since 1.1
     */
    public CtfTmfContext(CtfTmfTrace ctfTmfTrace) {
        fTrace = ctfTmfTrace;
        fCurLocation = new CtfLocation(new CtfLocationInfo(0, 0));
    }

    // -------------------------------------------
    // TmfContext Overrides
    // -------------------------------------------

    @Override
    public long getRank() {
        return fCurRank;
    }

    /**
     * @since 3.0
     */
    @Override
    public synchronized ITmfLocation getLocation() {
        return fCurLocation;
    }

    @Override
    public boolean hasValidRank() {
        return fCurRank != CtfLocation.INVALID_LOCATION.getTimestamp();
    }

    /**
     * @since 3.0
     */
    @Override
    public synchronized void setLocation(ITmfLocation location) {
        fCurLocation = (CtfLocation) location;
        if (fCurLocation != null) {
            getIterator().seek(fCurLocation.getLocationInfo());
        }
    }

    @Override
    public void setRank(long rank) {
        fCurRank = rank;

    }

    @Override
    public void increaseRank() {
        if (hasValidRank()) {
            fCurRank++;
        }
    }

    // -------------------------------------------
    // CtfTmfTrace Helpers
    // -------------------------------------------

    /**
     * Gets the trace of this context.
     *
     * @return The trace of this context
     */
    public CtfTmfTrace getTrace() {
        return fTrace;
    }

    /**
     * Gets the current event. Wrapper to help CtfTmfTrace
     *
     * @return The event or null
     */
    public synchronized CtfTmfEvent getCurrentEvent() {
        return getIterator().getCurrentEvent();
    }

    /**
     * Advances to a the next event. Wrapper to help CtfTmfTrace
     *
     * @return success or not
     */
    public synchronized boolean advance() {
        final CtfLocationInfo curLocationData = fCurLocation.getLocationInfo();
        CtfIterator iterator = getIterator();
        boolean retVal = iterator.advance();
        CtfTmfEvent currentEvent = iterator.getCurrentEvent();

        if (currentEvent != null) {
            final long timestampValue = iterator.getCurrentTimestamp();
            if (curLocationData.getTimestamp() == timestampValue) {
                fCurLocation = new CtfLocation(timestampValue, curLocationData.getIndex() + 1);
            } else {
                fCurLocation = new CtfLocation(timestampValue, 0L);
            }
        } else {
            fCurLocation = new CtfLocation(CtfLocation.INVALID_LOCATION);
        }

        return retVal;
    }

    @Override
    public void dispose() {
        fTrace.getIteratorManager().removeIterator(this);
    }

    /**
     * Seeks to a given timestamp. Wrapper to help CtfTmfTrace
     *
     * @param timestamp
     *            desired timestamp
     * @return success or not
     */
    public synchronized boolean seek(final long timestamp) {
        fCurLocation = new CtfLocation(timestamp, 0);
        return getIterator().seek(timestamp);
    }

    /**
     * Seeks to a given location. Wrapper to help CtfTmfTrace
     * @param location
     *              unique location to find the event.
     *
     * @return success or not
     * @since 2.0
     */
    public synchronized boolean seek(final CtfLocationInfo location) {
        fCurLocation = new CtfLocation(location);
        return getIterator().seek(location);
    }

    // -------------------------------------------
    // Private helpers
    // -------------------------------------------

    /**
     * Get iterator, called every time to get an iterator, no local copy is
     * stored so that there is no need to "update"
     *
     * @return an iterator
     */
    private CtfIterator getIterator() {
        return fTrace.getIteratorManager().getIterator(this);
    }
}
