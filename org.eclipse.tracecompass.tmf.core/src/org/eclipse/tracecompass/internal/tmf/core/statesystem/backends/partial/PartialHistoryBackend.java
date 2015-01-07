/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Matthew Khouzam - Make state system work in a single pass
 *   Fabien Reumont-Locke - Improve single pass generation,
 *                          implement cache for queries
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.statesystem.backends.partial;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.backend.IStateHistoryBackend;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.interval.TmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Partial state history back-end.
 *
 * This is a shim inserted between the real state system and a "real" history
 * back-end. It will keep checkpoints, every n trace events (where n is called
 * the granularity) and will only forward to the real state history the state
 * intervals that crosses at least one checkpoint. Every other interval will be
 * discarded.
 *
 * This would mean that it can only answer queries exactly at the checkpoints.
 * For any other timestamps (ie, most of the time), it will load the closest
 * earlier checkpoint, and will re-feed the state-change-input with events from
 * the trace, to restore the real state at the time that was requested.
 *
 * @author Alexandre Montplaisir
 */
public class PartialHistoryBackend implements IStateHistoryBackend {

    /**
     * Checkpoint attribute name
     */
    private static final String CHECKPOINT_NAME = "checkpoint"; //$NON-NLS-1$

    /**
     * A partial history needs the state input plugin to re-generate state
     * between checkpoints.
     */
    private final @NonNull ITmfStateProvider fPartialInput;

    /**
     * Fake state system that is used for partially rebuilding the states (when
     * going from a checkpoint to a target query timestamp).
     */
    private final @NonNull PartialStateSystem fPartialSS;

    /** Reference to the "real" state history that is used for storage */
    private final @NonNull IStateHistoryBackend fInnerHistory;

    /** Checkpoints set, <Timestamp> */
    private final @NonNull TreeSet<Long> fCheckpoints = new TreeSet<>();

    /** Quarks added between the last two checkpoints */
    private final ArrayList<Boolean> fAddedQuarks = new ArrayList<>();

    private long fLatestTime;

    /** Has at least one state been inserted */
    private boolean fInitialized = false;

    /** the quark of a checkpoint */
    private int fCheckpointQuark;

    /**
     * Constructor
     *
     * @param partialInput
     *            The state change input object that was used to build the
     *            upstream state system. This partial history will make its own
     *            copy (since they have different targets).
     * @param pss
     *            The partial history's inner state system. It should already be
     *            assigned to partialInput.
     * @param realBackend
     *            The real state history back-end to use. It's supposed to be
     *            modular, so it should be able to be of any type.
     */
    public PartialHistoryBackend(@NonNull ITmfStateProvider partialInput, @NonNull PartialStateSystem pss,
            IStateHistoryBackend realBackend) {
        if (partialInput.getAssignedStateSystem() != pss) {
            throw new IllegalArgumentException();
        }

        final long startTime = realBackend.getStartTime();

        fPartialInput = partialInput;
        fPartialSS = pss;

        fInnerHistory = realBackend;

        fLatestTime = startTime;

    }

    @Override
    public long getStartTime() {
        return fInnerHistory.getStartTime();
    }

    @Override
    public long getEndTime() {
        return fLatestTime;
    }

    @Override
    public void insertPastState(long stateStartTime, long stateEndTime,
            int quark, ITmfStateValue value) throws TimeRangeException {
        // TODO : come up with a better way to get this information
        int nbAttributes = ((ITmfStateSystem) fPartialSS).getNbAttributes();

        if (!fInitialized) {
            try {
                fCheckpointQuark = ((ITmfStateSystem) fPartialSS).getQuarkAbsolute(CHECKPOINT_NAME);
            } catch (AttributeNotFoundException e) {
                Activator.logError("Checkpoint node not found", e); //$NON-NLS-1$
            }
            fAddedQuarks.clear();
            resetAddedQuarks(nbAttributes, Boolean.TRUE);
            fCheckpoints.add(fPartialInput.getStartTime());
            fInitialized = true;
        }
        if (quark == fCheckpointQuark) {
            fAddedQuarks.clear();
            resetAddedQuarks(nbAttributes, Boolean.TRUE);
            fCheckpoints.add(stateEndTime);
            return;
        }

        /* Update the latest time */
        if (stateEndTime > fLatestTime) {
            fLatestTime = stateEndTime;
        }

        /*
         * Check if the interval intersects the previous checkpoint. If so,
         * insert it in the real history back-end.
         *
         * FIXME since intervals are inserted in order of rank, we could avoid
         * doing a map lookup every time here (just compare with the known
         * previous one).
         */
        boolean insert = false;
        if (quark >= fAddedQuarks.size()) {
            resetAddedQuarks(nbAttributes, Boolean.FALSE);
            insert = true;
        }
        if (fAddedQuarks.get(quark)) {
            fAddedQuarks.set(quark, Boolean.FALSE);
            insert = true;
        }
        if (insert) {
            fInnerHistory.insertPastState(stateStartTime, stateEndTime, quark, value);
        }

    }

    /**
     * Resets added quarks
     *
     * @param nbAttributes
     *            the number of quarks
     * @param value
     *            set them all to true or false
     */
    private void resetAddedQuarks(final int nbAttributes, Boolean value) {
        fAddedQuarks.ensureCapacity(nbAttributes);
        while (fAddedQuarks.size() < nbAttributes) {
            fAddedQuarks.add(value);
        }
    }

    @Override
    public void finishedBuilding(long endTime) throws TimeRangeException {
        fInnerHistory.finishedBuilding(endTime);
    }

    @Override
    public FileInputStream supplyAttributeTreeReader() {
        return fInnerHistory.supplyAttributeTreeReader();
    }

    @Override
    public File supplyAttributeTreeWriterFile() {
        return fInnerHistory.supplyAttributeTreeWriterFile();
    }

    @Override
    public long supplyAttributeTreeWriterFilePosition() {
        return fInnerHistory.supplyAttributeTreeWriterFilePosition();
    }

    @Override
    public void removeFiles() {
        fInnerHistory.removeFiles();
    }

    @Override
    public void dispose() {
        fPartialInput.dispose();
        fPartialSS.dispose();
        fInnerHistory.dispose();
    }

    @Override
    public void doQuery(List<ITmfStateInterval> currentStateInfo, long t)
            throws TimeRangeException, StateSystemDisposedException {
        fPartialSS.getUpstreamSS().waitUntilBuilt();

        if (!checkValidTime(t)) {
            throw new TimeRangeException();
        }

        /* Reload the previous checkpoint */
        long checkpointTime = fCheckpoints.floor(t);
        fInnerHistory.doQuery(currentStateInfo, checkpointTime);

        fillStateInfoWithNull(t, currentStateInfo);

        /*
         * Set the initial contents of the partial state system (which is the
         * contents of the query at the checkpoint).
         */
        fPartialSS.takeQueryLock();
        fPartialSS.replaceOngoingState(currentStateInfo);

        /* Send an event request to update the state system to the target time. */
        TmfTimeRange range = new TmfTimeRange(
                /*
                 * The state at the checkpoint already includes any state change
                 * caused by the event(s) happening exactly at 'checkpointTime',
                 * if any. We must not include those events in the query.
                 */
                new TmfTimestamp(checkpointTime + 1, ITmfTimestamp.NANOSECOND_SCALE),
                new TmfTimestamp(t, ITmfTimestamp.NANOSECOND_SCALE));
        ITmfEventRequest request = new PartialStateSystemRequest(fPartialInput, range);
        fPartialInput.getTrace().sendRequest(request);

        try {
            request.waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /*
         * Now the partial state system should have the ongoing time we are
         * looking for. However, the method expects a List of *state intervals*,
         * not state values, so we'll create intervals with a dummy end time.
         */
        try {
            for (int i = 0; i < currentStateInfo.size(); i++) {
                long start = 0;
                ITmfStateValue val = null;
                start = ((ITmfStateSystem) fPartialSS).getOngoingStartTime(i);
                val = ((ITmfStateSystem) fPartialSS).queryOngoingState(i);

                ITmfStateInterval interval = new TmfStateInterval(start, t, i, val);
                currentStateInfo.set(i, interval);
            }
        } catch (AttributeNotFoundException e) {
            /* Should not happen, we iterate over existing values. */
            e.printStackTrace();
        }

        fPartialSS.releaseQueryLock();
    }

    /**
     * Single queries are not supported in partial histories. To get the same
     * result you can do a full query, then call fullState.get(attribute).
     *
     * @throws StateSystemDisposedException
     *             if the ss is disposed and you query it
     * @throws TimeRangeException
     *             if you query out of range
     */
    @Override
    public ITmfStateInterval doSingularQuery(long t, int attributeQuark) throws TimeRangeException, StateSystemDisposedException {
        fPartialSS.getUpstreamSS().waitUntilBuilt();
        /* In previous checkpoint? */
        long previousCheckpointTime = fCheckpoints.floor(t);
        ITmfStateInterval interval = null;
        try {
            interval = fInnerHistory.doSingularQuery(previousCheckpointTime, attributeQuark);
            if (interval != null && interval.getEndTime() >= t) {
                return interval;
            }
        } catch (AttributeNotFoundException e) {
            // TODO: make this check instead of throw an exception
            // Not in this checkpoint, keep going
        }

        /* In next checkpoint? */
        long nextCheckpointTime = fCheckpoints.ceiling(t + 1);
        try {
            interval = fInnerHistory.doSingularQuery(nextCheckpointTime, attributeQuark);
            if (interval != null && interval.getStartTime() <= t) {
                return interval;
            }
        } catch (AttributeNotFoundException e) {
            // TODO: make this check instead of throw an exception
            // Not in this checkpoint, keep going
        }

        /* In neither checkpoint, check partial state system backend */
        try {
            interval = ((ITmfStateSystem) fPartialSS).querySingleState(t, attributeQuark);
            return interval;
        } catch (AttributeNotFoundException | TimeRangeException e) {
            // TODO: make this check instead of throw an exception
            // Not in backend cache, keep going
        }

        /* Not in partial state backend, read all events between checkpoints */
        int nbAttributes = ((ITmfStateSystem) fPartialSS).getNbAttributes();
        List<ITmfStateInterval> currentStateInfo = new ArrayList<>(nbAttributes);
        /* Bring the size of the array to the current number of attributes */
        for (int i = 0; i < nbAttributes; i++) {
            currentStateInfo.add(null);
        }

        /* Reload the previous checkpoint */
        fInnerHistory.doQuery(currentStateInfo, previousCheckpointTime);

        /*
         * Set the initial contents of the partial state system (which is the
         * contents of the query at the checkpoint).
         */
        fPartialSS.takeQueryLock();
        // no lonnger exists
        // TODO check if still valid.
        // fPartialSS.reset();
        fillStateInfoWithNull(t, currentStateInfo);
        fPartialSS.replaceOngoingState(currentStateInfo);

        /* Send an event request to update the state system to the target time. */
        TmfTimeRange range = new TmfTimeRange(
                /*
                 * The state at the checkpoint already includes any state change
                 * caused by the event(s) happening exactly at 'checkpointTime',
                 * if any. We must not include those events in the query.
                 */
                new TmfTimestamp(previousCheckpointTime + 1, ITmfTimestamp.NANOSECOND_SCALE),
                new TmfTimestamp(nextCheckpointTime, ITmfTimestamp.NANOSECOND_SCALE));
        ITmfEventRequest request = new PartialStateSystemRequest(fPartialInput, range);
        fPartialInput.getTrace().sendRequest(request);

        try {
            request.waitForCompletion();
        } catch (InterruptedException e) {
            Activator.logError(e.getMessage(), e);
        }

        try {
            interval = ((ITmfStateSystem) fPartialSS).querySingleState(t, attributeQuark);
            // Don't forget to release the lock (should go in a finally)
            fPartialSS.releaseQueryLock();
            return interval;
        } catch (AttributeNotFoundException e) {
            // Shouldn't happen...
        }
        fPartialSS.releaseQueryLock();
        return null;
    }

    private static void fillStateInfoWithNull(long t, List<ITmfStateInterval> currentStateInfo) {
        /* Fill in any null values */
        for (int i = 0; i < currentStateInfo.size(); i++) {
            if (currentStateInfo.get(i) == null) {
                currentStateInfo.set(i, new TmfStateInterval(t, t, i, TmfStateValue.nullValue()));
            }
        }
    }

    private boolean checkValidTime(long t) {
        return (t >= getStartTime() && t <= getEndTime());
    }

    @Override
    public void debugPrint(PrintWriter writer) {
        // TODO Auto-generated method stub
    }

    // ------------------------------------------------------------------------
    // Event requests types
    // ------------------------------------------------------------------------

    private class PartialStateSystemRequest extends TmfEventRequest {
        private final ITmfStateProvider sci;
        private final ITmfTrace trace;

        PartialStateSystemRequest(ITmfStateProvider sci, TmfTimeRange range) {
            super(sci.getExpectedEventType(),
                    range,
                    0,
                    ITmfEventRequest.ALL_DATA,
                    ITmfEventRequest.ExecutionType.BACKGROUND);
            this.sci = sci;
            this.trace = sci.getTrace();
        }

        @Override
        public void handleData(final ITmfEvent event) {
            super.handleData(event);
            if (event.getTrace() == trace) {
                sci.processEvent(event);
            }
        }

        @Override
        public void handleCompleted() {
            /*
             * If we're using a threaded state provider, we need to make sure
             * all events have been handled by the state system before doing
             * queries on it.
             */
            if (fPartialInput instanceof AbstractTmfStateProvider) {
                ((AbstractTmfStateProvider) fPartialInput).waitForEmptyQueue();
            }
            super.handleCompleted();
        }

    }
}
