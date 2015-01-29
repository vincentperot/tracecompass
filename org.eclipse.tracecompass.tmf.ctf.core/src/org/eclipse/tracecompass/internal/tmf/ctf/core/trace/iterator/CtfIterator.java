/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Florian Wininger - Performance improvements
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ctf.core.trace.iterator;

import org.eclipse.tracecompass.ctf.core.trace.CTFReaderException;
import org.eclipse.tracecompass.ctf.core.trace.CTFStreamInputReader;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.core.trace.CTFTraceReader;
import org.eclipse.tracecompass.internal.tmf.ctf.core.Activator;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;
import org.eclipse.tracecompass.tmf.ctf.core.context.CtfLocation;
import org.eclipse.tracecompass.tmf.ctf.core.context.CtfLocationInfo;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEventFactory;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;

/**
 * The CTF trace reader iterator.
 *
 * It doesn't reserve a file handle, so many iterators can be used without
 * worries of I/O errors or resource exhaustion.
 *
 * @author Matthew Khouzam
 */
public final class CtfIterator extends CTFTraceReader
        implements ITmfContext, Comparable<CtfIterator> {

    /** An invalid location */
    public static final CtfLocation NULL_LOCATION = new CtfLocation(CtfLocation.INVALID_LOCATION);

    private final CtfTmfTrace fTrace;

    private CtfLocation fCurLocation;
    private long fCurRank;

    private CtfLocation fPreviousLocation;
    private CtfTmfEvent fPreviousEvent;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Create a new CTF trace iterator, which initially points at the first
     * event in the trace.
     *
     * @param ctfTrace
     *            The {@link CTFTrace} linked to the trace. It should be
     *            provided by the corresponding 'ctfTmfTrace'.
     *
     * @param ctfTmfTrace
     *            The {@link CtfTmfTrace} to iterate over
     * @throws CTFReaderException
     *             If the iterator couldn't not be instantiated, probably due to
     *             a read error.
     */
    public CtfIterator(CTFTrace ctfTrace, CtfTmfTrace ctfTmfTrace) throws CTFReaderException {
        super(ctfTrace);
        fTrace = ctfTmfTrace;
        if (hasMoreEvents()) {
            setCurLocation(new CtfLocation(ctfTmfTrace.getStartTime()));
            fCurRank = 0;
        } else {
            setUnknownLocation();
        }
    }

    /**
     * Create a new CTF trace iterator, which will initially point to the given
     * location/rank.
     *
     * @param ctfTrace
     *            The {@link CTFTrace} linked to the trace. It should be
     *            provided by the corresponding 'ctfTmfTrace'.
     * @param ctfTmfTrace
     *            The {@link CtfTmfTrace} to iterate over
     * @param ctfLocationData
     *            The initial timestamp the iterator will be pointing to
     * @param rank
     *            The initial rank
     * @throws CTFReaderException
     *             If the iterator couldn't not be instantiated, probably due to
     *             a read error.
     * @since 2.0
     */
    public CtfIterator(CTFTrace ctfTrace, CtfTmfTrace ctfTmfTrace, CtfLocationInfo ctfLocationData, long rank)
            throws CTFReaderException {
        super(ctfTrace);

        fTrace = ctfTmfTrace;
        if (this.hasMoreEvents()) {
            setCurLocation(new CtfLocation(ctfLocationData));
            if (getCurrentEvent().getTimestamp().getValue() != ctfLocationData.getTimestamp()) {
                seek(ctfLocationData);
                fCurRank = rank;
            }
        } else {
            setUnknownLocation();
        }
    }

    @Override
    public void dispose() {
        close();
    }

    private void setUnknownLocation() {
        setCurLocation(NULL_LOCATION);
        fCurRank = UNKNOWN_RANK;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Return this iterator's trace.
     *
     * @return CtfTmfTrace The iterator's trace
     */
    public CtfTmfTrace getCtfTmfTrace() {
        return fTrace;
    }

    /**
     * Return the current event pointed to by the iterator.
     *
     * @return CtfTmfEvent The current event
     */
    public synchronized CtfTmfEvent getCurrentEvent() {
        final CTFStreamInputReader top = super.getPrio().peek();
        if (top != null) {
            if (!getLocation().equals(fPreviousLocation)) {
                fPreviousLocation = getLocation();
                fPreviousEvent = CtfTmfEventFactory.createEvent(top.getCurrentEvent(),
                        top.getFilename(), fTrace);
            }
            return fPreviousEvent;
        }
        return null;
    }

    /**
     * Return the current timestamp location pointed to by the iterator.
     * This is the timestamp for use in CtfLocation, not the event timestamp.
     *
     * @return long The current timestamp location
     */
    public synchronized long getCurrentTimestamp() {
        final CTFStreamInputReader top = super.getPrio().peek();
        if (top != null) {
            long ts = top.getCurrentEvent().getTimestamp();
            return fTrace.timestampCyclesToNanos(ts);
        }
        return 0;
    }

    /**
     * Seek this iterator to a given location.
     *
     * @param ctfLocationData
     *            The LocationData representing the position to seek to
     * @return boolean True if the seek was successful, false if there was an
     *         error seeking.
     * @since 2.0
     */
    public synchronized boolean seek(CtfLocationInfo ctfLocationData) {
        boolean ret = false;

        /* Avoid the cost of seeking at the current location. */
        if (getLocation().getLocationInfo().equals(ctfLocationData)) {
            return super.hasMoreEvents();
        }

        /* Adjust the timestamp depending on the trace's offset */
        long currTimestamp = ctfLocationData.getTimestamp();
        final long offsetTimestamp = this.getCtfTmfTrace().timestampNanoToCycles(currTimestamp);
        try {
            if (offsetTimestamp < 0) {
                ret = super.seek(0L);
            } else {
                ret = super.seek(offsetTimestamp);
            }
        } catch (CTFReaderException e) {
            Activator.getDefault().logError(e.getMessage(), e);
            return false;
        }
        /*
         * Check if there is already one or more events for that timestamp, and
         * assign the location index correctly
         */
        long index = 0;
        final CtfTmfEvent currentEvent = this.getCurrentEvent();
        if (currentEvent != null) {
            currTimestamp = currentEvent.getTimestamp().getValue();

            for (long i = 0; i < ctfLocationData.getIndex(); i++) {
                if (currTimestamp == currentEvent.getTimestamp().getValue()) {
                    index++;
                } else {
                    index = 0;
                }
                this.advance();
            }
        } else {
            ret = false;
        }
        /* Seek the current location accordingly */
        if (ret) {
            setCurLocation(new CtfLocation(new CtfLocationInfo(getCurrentEvent().getTimestamp().getValue(), index)));
        } else {
            setCurLocation(NULL_LOCATION);
        }

        return ret;
    }

    // ------------------------------------------------------------------------
    // CTFTraceReader
    // ------------------------------------------------------------------------

    @Override
    public boolean seek(long timestamp) {
        return seek(new CtfLocationInfo(timestamp, 0));
    }

    @Override
    public synchronized boolean advance() {
        boolean ret = false;
        try {
            ret = super.advance();
        } catch (CTFReaderException e) {
            Activator.getDefault().logError(e.getMessage(), e);
        }

        if (ret) {
            long timestamp = getLocation().getLocationInfo().getTimestamp();
            final long timestampValue = getCurrentTimestamp();
            if (timestamp == timestampValue) {
                long index = getLocation().getLocationInfo().getIndex();
                setCurLocation(new CtfLocation(timestampValue, index + 1));
            } else {
                setCurLocation(new CtfLocation(timestampValue, 0L));
            }
        } else {
            setCurLocation(NULL_LOCATION);
        }
        return ret;
    }

    // ------------------------------------------------------------------------
    // ITmfContext
    // ------------------------------------------------------------------------

    @Override
    public long getRank() {
        return fCurRank;
    }

    @Override
    public void setRank(long rank) {
        fCurRank = rank;
    }

    @Override
    public void increaseRank() {
        /* Only increase the rank if it's valid */
        if (hasValidRank()) {
            fCurRank++;
        }
    }

    @Override
    public boolean hasValidRank() {
        return (getRank() >= 0);
    }

    /**
     * @since 3.0
     */
    @Override
    public synchronized void setLocation(ITmfLocation location) {
        // FIXME alex: isn't there a cleaner way than a cast here?
        setCurLocation((CtfLocation) location);
        seek(((CtfLocation) location).getLocationInfo());
    }

    @Override
    public synchronized CtfLocation getLocation() {
        return fCurLocation;
    }

    private void setCurLocation(CtfLocation curLocation) {
        fCurLocation = curLocation;
    }

    // ------------------------------------------------------------------------
    // Comparable
    // ------------------------------------------------------------------------

    @Override
    public int compareTo(final CtfIterator o) {
        if (getRank() < o.getRank()) {
            return -1;
        } else if (getRank() > o.getRank()) {
            return 1;
        }
        return 0;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = (prime * result)
                + ((fTrace == null) ? 0 : fTrace.hashCode());
        final CtfLocation curLocation = getLocation(); // TODO: synchronize me
        result = (prime * result)
                + ((curLocation == null) ? 0 : curLocation.hashCode());
        result = (prime * result) + (int) (fCurRank ^ (fCurRank >>> Integer.SIZE));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof CtfIterator)) {
            return false;
        }
        CtfIterator other = (CtfIterator) obj;
        if (fTrace == null) {
            if (other.fTrace != null) {
                return false;
            }
        } else if (!fTrace.equals(other.fTrace)) {
            return false;
        }
        if (getLocation() == null) {
            if (other.getLocation() != null) {
                return false;
            }
        } else if (!getLocation().equals(other.getLocation())) {
            return false;
        }
        if (fCurRank != other.fCurRank) {
            return false;
        }
        return true;
    }


}
