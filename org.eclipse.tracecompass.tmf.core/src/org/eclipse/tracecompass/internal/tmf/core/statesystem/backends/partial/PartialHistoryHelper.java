/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Patrick Tasse - Add message to exceptions
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.statesystem.backends.partial;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.eclipse.tracecompass.statesystem.core.backend.IPartialStateHelper;
import org.eclipse.tracecompass.tmf.core.component.ITmfEventProvider;
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
public class PartialHistoryHelper implements IPartialStateHelper {

    private final ITmfStateProvider fPartialInput;
    private final long fGranularity;
    private Map<Long, Long> fCheckpoints;
    private CountDownLatch fCheckpointsReady;

    public PartialHistoryHelper(ITmfStateProvider provider, long granularity) {
        fPartialInput = provider;
        fGranularity = granularity;
    }

    @Override
    public void registerCheckpoints() {
        ITmfEventRequest request = new CheckpointsRequest(fPartialInput, fCheckpoints);
        getTrace().sendRequest(request);
        /*
         * The request will countDown the checkpoints latch once it's finished
         */
    }

    private ITmfEventProvider getTrace() {
        return fPartialInput.getTrace();
    }

    @Override
    public void getCheckpoints(long t, long checkpointTime) {
        /*
         * Send an event request to update the state system to the target time.
         */
        TmfTimeRange range = new TmfTimeRange(
                /*
                 * The state at the checkpoint already includes any state change
                 * caused by the event(s) happening exactly at 'checkpointTime',
                 * if any. We must not include those events in the query.
                 */
                new TmfTimestamp(checkpointTime + 1, ITmfTimestamp.NANOSECOND_SCALE),
                new TmfTimestamp(t, ITmfTimestamp.NANOSECOND_SCALE));
        ITmfEventRequest request = new PartialStateSystemRequest(fPartialInput, range);
        getTrace().sendRequest(request);

        try {
            request.waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------------
    // Event requests types
    // ------------------------------------------------------------------------

    class CheckpointsRequest extends TmfEventRequest {
        private final ITmfTrace trace;
        private final Map<Long, Long> checkpts;
        private long eventCount;
        private long lastCheckpointAt;

        public CheckpointsRequest(ITmfStateProvider input, Map<Long, Long> checkpoints) {
            super(ITmfEvent.class,
                    TmfTimeRange.ETERNITY,
                    0,
                    ITmfEventRequest.ALL_DATA,
                    ITmfEventRequest.ExecutionType.FOREGROUND);
            checkpoints.clear();
            this.trace = input.getTrace();
            this.checkpts = checkpoints;
            eventCount = 0;
            lastCheckpointAt = 0;

            /* Insert a checkpoint at the start of the trace */
            checkpoints.put(input.getStartTime(), 0L);
        }

        @Override
        public void handleData(final ITmfEvent event) {
            super.handleData(event);
            if (event.getTrace() == trace) {
                eventCount++;

                /* Check if we need to register a new checkpoint */
                if (eventCount >= lastCheckpointAt + fGranularity) {
                    checkpts.put(event.getTimestamp().getValue(), eventCount);
                    lastCheckpointAt = eventCount;
                }
            }
        }

        @Override
        public void handleCompleted() {
            super.handleCompleted();
            fCheckpointsReady.countDown();
        }
    }

    class PartialStateSystemRequest extends TmfEventRequest {
        private final ITmfStateProvider sci;
        private final ITmfTrace trace;

        PartialStateSystemRequest(ITmfStateProvider sci, TmfTimeRange range) {
            super(ITmfEvent.class,
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


    @Override
    public void setCountdown(CountDownLatch checkpointsReady) {
        fCheckpointsReady = checkpointsReady;

    }

    @Override
    public void setCheckpoints(Map<Long, Long> checkpoints) {
        fCheckpoints = checkpoints;

    }

}
