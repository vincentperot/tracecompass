package org.eclipse.tracecompass.statesystem.core.backend;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.statesystem.core.IStateProvider;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.interval.TmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;

/**
 * @author ematkho
 * @since 1.0
 *
 */
public class PartialHistoryBackend implements IStateHistoryBackend {

    @NonNull
    protected final String fSSID;
    /**
     * A partial history needs the state input plugin to re-generate state
     * between checkpoints.
     */
    @NonNull
    protected final IStateProvider fPartialInput;
    /**
     * Fake state system that is used for partially rebuilding the states (when
     * going from a checkpoint to a target query timestamp).
     */
    @NonNull
    private final PartialStateSystem fPartialSS;
    /** Reference to the "real" state history that is used for storage */
    @NonNull
    protected final IStateHistoryBackend fInnerHistory;
    /** Checkpoints map, <Timestamp, Rank in the trace> */
    @NonNull
    private final TreeMap<Long, Long> fCheckpoints = new TreeMap<>();
    /** Latch tracking if the initial checkpoint registration is done */
    @NonNull
    protected final CountDownLatch fCheckpointsReady = new CountDownLatch(1);
    protected final long fGranularity;
    protected long fLatestTime;

    private IPartialStateHelper fHelper;

    /**
     * Constructor
     *
     * @param ssid
     *            The state system's ID
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
     * @param granularity
     *            Configuration parameter indicating how many trace events there
     *            should be between each checkpoint
     */
    public PartialHistoryBackend(@NonNull String ssid,
            IStateProvider partialInput,
            PartialStateSystem pss,
            IStateHistoryBackend realBackend,
            long granularity,
            IPartialStateHelper helper) {
        if (granularity <= 0 || partialInput == null || pss == null ||
                partialInput.getAssignedStateSystem() != pss) {
            throw new IllegalArgumentException();
        }

        final long startTime = realBackend.getStartTime();

        fSSID = ssid;
        fPartialInput = partialInput;
        fPartialSS = pss;

        fInnerHistory = realBackend;
        fGranularity = granularity;

        fLatestTime = startTime;

        fHelper = helper;
        helper.setCountdown(fCheckpointsReady);
        helper.setCheckpoints(fCheckpoints);
        registerCheckpoints();
    }

    private void registerCheckpoints() {
        fHelper.registerCheckpoints();
    }

    @Override
    public String getSSID() {
        return fSSID;
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
    public void insertPastState(long stateStartTime, long stateEndTime, int quark, ITmfStateValue value) throws TimeRangeException {
        waitForCheckpoints();

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
        if (stateStartTime <= getCheckpoints().floorKey(stateEndTime)) {
            fInnerHistory.insertPastState(stateStartTime, stateEndTime, quark, value);
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
    public void doQuery(List<ITmfStateInterval> currentStateInfo, long t) throws TimeRangeException, StateSystemDisposedException {
        /* Wait for required steps to be done */
        waitForCheckpoints();
        fPartialSS.getUpstreamSS().waitUntilBuilt();

        if (!checkValidTime(t)) {
            throw new TimeRangeException(fSSID + " Time:" + t + ", Start:" + getStartTime() + ", End:" + getEndTime()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        /* Reload the previous checkpoint */
        long checkpointTime = getCheckpoints().floorKey(t);
        fInnerHistory.doQuery(currentStateInfo, checkpointTime);

        /*
         * Set the initial contents of the partial state system (which is the
         * contents of the query at the checkpoint).
         */
        fPartialSS.takeQueryLock();
        fPartialSS.replaceOngoingState(currentStateInfo);

        getCheckpoints(t, checkpointTime);

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

    protected void getCheckpoints(long t, long checkpointTime) {
        fHelper.getCheckpoints(t, checkpointTime);
    }

    /**
     * Single queries are not supported in partial histories. To get the same
     * result you can do a full query, then call fullState.get(attribute).
     */
    @Override
    public ITmfStateInterval doSingularQuery(long t, int attributeQuark) {
        throw new UnsupportedOperationException();
    }

    private boolean checkValidTime(long t) {
        return (t >= getStartTime() && t <= getEndTime());
    }

    @Override
    public void debugPrint(PrintWriter writer) {
        // TODO Auto-generated method stub
    }

    private void waitForCheckpoints() {
        try {
            fCheckpointsReady.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private TreeMap<Long, Long> getCheckpoints() {
        return fCheckpoints;
    }

    protected Object getTrace() {
        return fPartialInput.getTrace();
    }

}