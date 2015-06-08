/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexis Cabana-Loriaux <alex021994@gmail.com>- Initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.statistics;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStatisticsModule;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.ui.viewers.statistics.model.TmfStatisticsTree;
import org.eclipse.tracecompass.tmf.ui.viewers.statistics.model.TmfStatisticsTreeManager;
import org.eclipse.tracecompass.tmf.ui.viewers.statistics.model.TmfStatisticsTreeNode;

/**
 * This class contains the model shown by the {@link TmfStatisticsView}.
 *
 * @author Alexis Cabana-Loriaux
 * @since 1.0
 */
public class TmfStatisticsModel {
    /**
     * The ID corresponds to the package in which this class is embedded.
     */
    public static final @NonNull String ID = "org.eclipse.linuxtools.tmf.ui.views.statistics"; //$NON-NLS-1$

    /**
     * The update jobs containers
     */
    private final Map<ITmfTrace, Job> fUpdateJobsPartial = new HashMap<>();
    private final Map<ITmfTrace, Job> fUpdateJobsGlobal = new HashMap<>();

    /**
     * The time ranges between which we query the statesystem
     */
    private TmfTimeRange fTimeRange;
    private TmfTimeRange fTimeRangePartial;

    /**
     * A reference to the view paired with this model
     */
    private TmfStatisticsView fView;

    /**
     * Stores a reference to the selected trace.
     */
    private ITmfTrace fTrace;

    /**
     * The current model representation holding the data to be shown
     */
    private TmfStatisticsTree fStatisticsData;

    /**
     * Default constructor
     *
     * @param view
     *            the view paired with this model
     */
    public TmfStatisticsModel(TmfStatisticsView view) {
        this.fView = view;
    }

    // ------------------------------------------------------------------------
    // Class Methods
    // ------------------------------------------------------------------------

    /**
     * Clean out the entire model
     */
    public void clear() {
        for (Job j : fUpdateJobsGlobal.values()) {
            j.cancel();
        }

        for (Job j : fUpdateJobsPartial.values()) {
            j.cancel();
        }
        TmfStatisticsTreeManager.removeStatTreeRoot(getTreeID());
        fStatisticsData = null;
    }

    /**
     * Method normally only used by Jobs to remove themselves from the update
     * queue
     *
     * @param isGlobal
     *            If the current updated the global or the selection model
     * @param trace
     *            The trace updated by the model
     */
    public void removeFromJobs(boolean isGlobal, ITmfTrace trace) {
        Map<ITmfTrace, Job> updateJobs = isGlobal ? fUpdateJobsGlobal : fUpdateJobsPartial;
        updateJobs.remove(trace);
    }

    /**
     * Requests all the data of the trace to the state system which contains
     * information about the statistics.
     *
     * Since the viewers may be listening to multiple traces, it may receive an
     * experiment rather than a single trace.
     *
     * A call to this method constructs the model of either the global range, or
     * the raange selected, depending on its argument isGlobal.
     *
     * @param trace
     *            The trace for which a request must be done
     * @param timeRange
     *            The time range that will be requested to the state system
     * @param isGlobal
     *            Tells if the request is for the global event count or the
     *            partial one.
     */
    private void buildStatisticsViewers(final ITmfTrace trace, final TmfTimeRange timeRange, final boolean isGlobal) {
        final TmfStatisticsTree statsData = TmfStatisticsTreeManager.getStatTree(getTreeID());
        if (statsData == null) {
            return;
        }

        Map<ITmfTrace, Job> updateJobs;
        if (isGlobal) {
            updateJobs = fUpdateJobsGlobal;
            fTimeRange = timeRange;
        } else {
            updateJobs = fUpdateJobsPartial;
            fTimeRangePartial = timeRange;
        }
        for (ITmfTrace aTrace : TmfTraceManager.getTraceSet(trace)) {
            aTrace = checkNotNull(aTrace);
            if (!isListeningTo(aTrace)) {
                continue;
            }

            /* Retrieve the statistics object */
            final TmfStatisticsModule statsMod = TmfTraceUtils.getAnalysisModuleOfClass(aTrace, TmfStatisticsModule.class, TmfStatisticsModule.ID);
            if (statsMod == null) {
                /* No statistics module available for this trace */
                continue;
            }

            Job job = updateJobs.get(aTrace);
            if (job == null) {
                job = new StatisticsUpdateJob("Statistics update", aTrace, isGlobal, statsMod, this); //$NON-NLS-1$
                updateJobs.put(aTrace, job);
                job.setSystem(true);
                job.schedule();
            }
        }
    }

    /**
     * Sends the request to the trace for the whole trace
     *
     * @param timeRange
     *            The range to request to the trace
     * @since 1.0
     */
    public void requestData(final TmfTimeRange timeRange) {
        buildStatisticsViewers(fTrace, timeRange, true);
    }

    /**
     * Sends the time range request from the trace
     *
     * @param timeRange
     *            The range to request to the trace
     * @since 1.0
     */
    public void requestTimeRangeData(final TmfTimeRange timeRange) {
        buildStatisticsViewers(fTrace, timeRange, false);
    }

    /**
     * Tells if the viewer is listening to a trace.
     *
     * @param trace
     *            The trace that the viewer may be listening
     * @return true if the viewer is listening to the trace, false otherwise
     * @since 1.0
     */
    public boolean isListeningTo(ITmfTrace trace) {
        return (fTrace instanceof TmfExperiment) || trace == fTrace;
    }

    /**
     * Returns a unique ID based on name to be associated with the statistics
     * tree for this viewer. For a same name, it will always return the same ID.
     * Uses a the name of the tab as the Tree unique ID, but can be overriden.
     *
     * @return a unique statistics tree ID.
     * @since 1.0
     */
    public String getTreeID() {
        return fView.getStatsViewer().getTreeID();
    }

    /**
     * Initializes the input for the tree viewer.
     *
     * @since 1.0
     */
    public void initTreeInput() {
        String treeID = getTreeID();
        TmfStatisticsTreeNode statisticsTreeNode;
        if (TmfStatisticsTreeManager.containsTreeRoot(treeID)) {
            // The statistics root is already present
            fStatisticsData = TmfStatisticsTreeManager.getStatTree(treeID);
            statisticsTreeNode = fStatisticsData.getRootNode();

            // Checks if the trace is already in the statistics tree.
            int numNodeTraces = statisticsTreeNode.getNbChildren();

            Collection<ITmfTrace> traces = TmfTraceManager.getTraceSet(fTrace);
            int numTraces = traces.size();

            if (numTraces == numNodeTraces) {
                boolean same = true;
                /*
                 * Checks if the experiment contains the same traces as when
                 * previously selected.
                 */
                for (ITmfTrace trace : traces) {
                    String traceName = trace.getName();
                    if (!statisticsTreeNode.containsChild(traceName)) {
                        same = false;
                        break;
                    }
                }

                if (same) {
                    // No need to reload data, all traces are already loaded
                    return;
                }
                // Clears the old content to start over
                statisticsTreeNode.reset();
            }
        } else {
            // Creates a new tree
            statisticsTreeNode = TmfStatisticsTreeManager.addStatsTreeRoot(treeID, getStatisticData());
        }
    }

    // ------------------------------------------------------------------------
    // Getters and setter
    // ------------------------------------------------------------------------

    /**
     * @return the Global Time-Range
     */
    public TmfTimeRange getTimeRange() {
        return fTimeRange;
    }

    /**
     * @param timeRange
     *            The new Global Time-Range
     */
    public void setTimeRange(TmfTimeRange timeRange) {
        fTimeRange = timeRange;
    }

    /**
     * @return the selection's time-range
     */
    public TmfTimeRange getTimeRangePartial() {
        return fTimeRangePartial;
    }

    /**
     * @param timeRangePartial
     *            the new selection's time-range
     */
    public void setTimeRangePartial(TmfTimeRange timeRangePartial) {
        fTimeRangePartial = timeRangePartial;
    }

    /**
     * @return the trace currently presented by this model
     */
    public ITmfTrace getTrace() {
        return fTrace;
    }

    /**
     * @param trace
     *            the new trace
     */
    public void setTrace(ITmfTrace trace) {
        fTrace = trace;
    }

    /**
     * This method can be overridden to implement another way of representing
     * the statistics data and to retrieve the information for display.
     *
     * @return a TmfStatisticsData object.
     */
    public TmfStatisticsTree getStatisticData() {
        if (fStatisticsData == null) {
            fStatisticsData = new TmfStatisticsTree();
        }
        return fStatisticsData;
    }

    /**
     * @param isGlobal
     *          Tells if the model was completed for a global request or not
     */
    public void signalModelComplete(boolean isGlobal) {
        fView.signalModelComplete(isGlobal);
    }

}
