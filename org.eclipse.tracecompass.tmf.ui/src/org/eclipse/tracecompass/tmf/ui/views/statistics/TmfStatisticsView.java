/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Generalized version based on LTTng
 *   Bernd Hufmann - Updated to use trace reference in TmfEvent and streaming
 *   Mathieu Denis - New request added to update the statistics from the selected time range
 *   Mathieu Denis - Generalization of the view to instantiate a viewer specific to a trace type
 *
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.statistics;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.statistics.ITmfStatistics;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStatisticsEventTypesModule;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStatisticsModule;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.ui.viewers.piecharts.TmfPieChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.statistics.TmfStatisticsViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.statistics.model.TmfStatisticsTree;
import org.eclipse.tracecompass.tmf.ui.viewers.statistics.model.TmfStatisticsTreeManager;
import org.eclipse.tracecompass.tmf.ui.viewers.statistics.model.TmfStatisticsTreeNode;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;
import org.eclipse.tracecompass.tmf.ui.widgets.tabsview.TmfViewerFolder;

/**
 * The generic Statistics View displays statistics for any kind of traces.
 *
 * It is implemented according to the MVC pattern. - The model is a
 * TmfStatisticsTreeNode and a Map built by the State Manager. - The view is
 * built with a TreeViewer as well as a PieChartViewer. - The controller that
 * keeps model and view synchronized is an observer of the model.
 *
 * @author Mathieu Denis
 */
public class TmfStatisticsView extends TmfView {
    /**
     * The ID corresponds to the package in which this class is embedded.
     */
    public static final @NonNull String ID = "org.eclipse.linuxtools.tmf.ui.views.statistics"; //$NON-NLS-1$

    /**
     * The view name.
     */
    public static final String TMF_STATISTICS_VIEW = "StatisticsView"; //$NON-NLS-1$

    /** Timestamp scale used for all statistics (nanosecond) */
    private static final byte TIME_SCALE = ITmfTimestamp.NANOSECOND_SCALE;

    /** The delay (in ms) between each update in live-reading mode */
    private static final long LIVE_UPDATE_DELAY = 1000;
    /**
     * The viewer that builds the columns to show the statistics.
     *
     * @since 1.0
     */
    protected final TmfViewerFolder fFolderViewer;
    /**
     * The viewer that builds the piecharts to show statistics
     *
     * @since 1.0
     */
    private final TmfPieChartViewer fPieChartViewer;

    /**
     * Stores a reference to the selected trace.
     */
    private ITmfTrace fTrace;

    /**
     * The viewer that contains the tree showing the statistics
     */
    private TmfStatisticsViewer fGlobalViewer;

    /** Update range synchronization object */
    private final Object fStatisticsRangeUpdateSyncObj = new Object();

    /** Tells to send a time range request when the trace gets updated. */
    private boolean fSendRangeRequest = true;

    /** The update jobs containers */
    private final Map<ITmfTrace, Job> fUpdateJobsPartial = new HashMap<>();
    private final Map<ITmfTrace, Job> fUpdateJobsGlobal = new HashMap<>();

    /** The model for the PieChart viewer */
    private final Map<String, Long> fPieChartGlobalModel = new ConcurrentHashMap<>();
    private final Map<String, Long> fPieChartSelectionModel = new ConcurrentHashMap<>();

    /** The time ranges between which we query the statesystem */
    private TmfTimeRange fTimeRange;
    private TmfTimeRange fTimeRangePartial;

    /** Object to store the cursor while waiting for the trace to load */
    private Cursor fWaitCursor = null;

    /**
     * Counts the number of times waitCursor() has been called. It avoids
     * removing the waiting cursor, since there may be multiple requests running
     * at the same time.
     */
    private int fWaitCursorCount = 0;

    /**
     * Constructor of a statistics view.
     *
     * @param viewName
     *            The name to give to the view.
     */
    public TmfStatisticsView(String viewName) {
        super(viewName);
        /*
         * Create a fake parent for initialization purpose, than set the parent
         * as soon as createPartControl is called.
         */
        Composite temporaryParent = new Shell();
        fFolderViewer = new TmfViewerFolder(temporaryParent);
        fPieChartViewer = new TmfPieChartViewer(temporaryParent,
                Messages.TmfStatisticsView_GlobalTabName,
                Messages.TmfStatisticsView_TimeRangeSelectionPieChartName,
                Messages.TmfStatisticsView_PieChartOthersSliceName);
        TmfSignalManager.register(this);
    }

    /**
     * Default constructor.
     */
    public TmfStatisticsView() {
        this(TMF_STATISTICS_VIEW);
    }

    @Override
    public void createPartControl(Composite parent) {
        SashForm sash = new SashForm(parent, SWT.NONE);
        sash.setLayout(new FillLayout());
        /*
         * The Tree Viewer
         */
        fFolderViewer.setParent(sash);
        /*
         * Create the Statistics Tree viewer that will included in the folder
         * viewer
         */
        createStatisticsTreeViewer();

        /*
         * The Piechart Viewer
         */
        getPieChartViewer().setParent(sash);

        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            traceSelected(new TmfTraceSelectedSignal(this, trace));
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (fWaitCursor != null) {
            fWaitCursor.dispose();
        }
        for (Job j : fUpdateJobsGlobal.values()) {
            j.cancel();
        }

        for (Job j : fUpdateJobsPartial.values()) {
            j.cancel();
        }
        fFolderViewer.dispose();
        getPieChartViewer().dispose();
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * Handler called when an trace is opened.
     *
     * @param signal
     *            Contains the information about the selection.
     */
    @TmfSignalHandler
    public void traceOpened(final TmfTraceOpenedSignal signal) {
        if (signal.getTrace() == fTrace) {
            // no need to reopen the same trace.
            return;
        }
        clearModel();
        // Update the current trace
        fTrace = signal.getTrace();
        /*
         * fetch data and (re)construct the model using the statistics modules
         * as well as the two separated views.
         */
        fFolderViewer.clear();
        createStatisticsTreeViewer();
        requestData(fTrace, fTrace.getTimeRange());
        fFolderViewer.layout();
        getPieChartViewer().layout();
    }

    /**
     * Clears both the model of the piecharts and the treeview
     */
    private void clearModel() {
        /*
         * Dispose the current viewer and adapt the new one to the trace type of
         * the trace opened. Also clear the internal data stored about the
         * trace's statistics
         */
        fPieChartGlobalModel.clear();
        fPieChartSelectionModel.clear();
    }

    /**
     * Handles the signal about new trace range.
     *
     * @param signal
     *            The trace range updated signal
     * @since 1.0
     */
    @TmfSignalHandler
    public void traceRangeUpdated(final TmfTraceRangeUpdatedSignal signal) {
        final ITmfTrace trace = signal.getTrace();
        // validate
        if (!isListeningTo(trace)) {
            return;
        }
        synchronized (fStatisticsRangeUpdateSyncObj) {
            if (fSendRangeRequest) {
                fSendRangeRequest = false;

                TmfTraceContext ctx = TmfTraceManager.getInstance().getCurrentTraceContext();
                TmfTimeRange timeRange = ctx.getSelectionRange();
                requestTimeRangeData(trace, timeRange);
            }
        }
        requestData(trace, signal.getRange());
    }

    /**
     * @param signal
     *            The selection range updated signal.
     * @since 1.0
     */
    @TmfSignalHandler
    public void traceSelectionRangeUpdated(final TmfSelectionRangeUpdatedSignal signal) {
        if (fTrace == null) {
            return;
        }
        ITmfTimestamp begin = signal.getBeginTime();
        ITmfTimestamp end = signal.getEndTime();
        TmfTimeRange timeRange = new TmfTimeRange(begin, end);
        requestTimeRangeData(fTrace, timeRange);
    }

    /**
     * Handler called when an trace is selected. Checks if the trace has changed
     * and requests the selected trace if it has not yet been cached.
     *
     * @param signal
     *            Contains the information about the selection.
     */
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        // Does not reload the same trace if already opened
        if (signal.getTrace() != fTrace) {
            /*
             * Dispose the current viewer and adapt the new one to the trace
             * type of the trace selected
             */
            fFolderViewer.clear();
            // Update the current trace
            fTrace = signal.getTrace();
            createStatisticsTreeViewer();
            // update the pieChart model and view
            fFolderViewer.layout();

            TmfTraceRangeUpdatedSignal updateSignal = new TmfTraceRangeUpdatedSignal(this, fTrace, fTrace.getTimeRange());

            sendPartialRequestOnNextUpdate();
            traceRangeUpdated(updateSignal);
        } else {
            /*
             * If the same trace is reselected, sends a notification to the
             * viewers to make sure they reload correctly their partial event
             * count.
             */
            sendPartialRequestOnNextUpdate();
        }
    }

    /**
     * @param signal
     *            the incoming signal
     */
    @TmfSignalHandler
    public void traceClosed(TmfTraceClosedSignal signal) {
        if (signal.getTrace() != fTrace) {
            return;
        }

        // Clear the internal data
        fTrace = null;
        clearModel();

        // Clear the UI widgets
        fFolderViewer.clear(); // Also cancels ongoing requests
        createStatisticsTreeViewer();
        getPieChartViewer().reinitializeCharts();
        fFolderViewer.layout();
    }

    // ------------------------------------------------------------------------
    // Class Methods
    // ------------------------------------------------------------------------

    @Override
    public void setFocus() {
        fFolderViewer.setFocus();
    }

    /**
     * Will force a request on the partial event count if one is needed.
     *
     * @since 1.0
     */
    public void sendPartialRequestOnNextUpdate() {
        synchronized (fStatisticsRangeUpdateSyncObj) {
            fSendRangeRequest = true;
        }
    }

    /**
     * Sends the request to the trace for the whole trace
     *
     * @param trace
     *            The trace used to send the request
     * @param timeRange
     *            The range to request to the trace
     * @since 1.0
     */
    private void requestData(final ITmfTrace trace, final TmfTimeRange timeRange) {
        buildStatisticsViewers(trace, timeRange, true);
    }

    /**
     * Sends the time range request from the trace
     *
     * @param trace
     *            The trace used to send the request
     * @param timeRange
     *            The range to request to the trace
     * @since 1.0
     */
    private void requestTimeRangeData(final ITmfTrace trace, final TmfTimeRange timeRange) {
        buildStatisticsViewers(trace, timeRange, false);
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
            fPieChartGlobalModel.clear();
        } else {
            updateJobs = fUpdateJobsPartial;
            fTimeRangePartial = timeRange;
            fPieChartSelectionModel.clear();
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
                job = new UpdateJob("Statistics update", aTrace, isGlobal, statsMod); //$NON-NLS-1$
                updateJobs.put(aTrace, job);
                job.setSystem(true);
                job.schedule();
            }
        }
    }

    /**
     * Creates the statistics viewers for all traces in an experiment and
     * populates a viewer folder. Each viewer is placed in a different tab and
     * the first one is selected automatically.
     *
     * It uses the extension point that defines the statistics viewer to build
     * from the trace type. If no viewer is defined, another tab won't be
     * created, since the global viewer already contains all the basic
     * statistics. If there is no trace selected, a global statistics viewer
     * will still be created.
     *
     * @since 1.0
     */
    private void createStatisticsTreeViewer() {
        // Default style for the tabs that will be created
        int defaultStyle = SWT.NONE;

        // The folder composite that will contain the tabs
        Composite folder = fFolderViewer.getParentFolder();

        // Instantiation of the global viewer
        if (fTrace != null) {
            // Shows the name of the trace in the global tab
            fGlobalViewer = new TmfStatisticsViewer(folder, Messages.TmfStatisticsView_GlobalTabName + " - " + fTrace.getName()); //$NON-NLS-1$

            fFolderViewer.addTab(fGlobalViewer, Messages.TmfStatisticsView_GlobalTabName, defaultStyle);

        } else {
            // There is no trace selected. Shows an empty global tab
            fGlobalViewer = new TmfStatisticsViewer(folder, Messages.TmfStatisticsView_GlobalTabName);
            fFolderViewer.addTab(fGlobalViewer, Messages.TmfStatisticsView_GlobalTabName, defaultStyle);
        }
        // Makes the global viewer visible
        fFolderViewer.setSelection(0);
        initTreeViewer();
    }

    /**
     * Initializes the input for the tree viewer.
     *
     * @since 1.0
     */
    protected void initTreeViewer() {
        String treeID = getTreeID();
        TmfStatisticsTreeNode statisticsTreeNode;
        if (TmfStatisticsTreeManager.containsTreeRoot(treeID)) {
            // The statistics root is already present
            statisticsTreeNode = TmfStatisticsTreeManager.getStatTreeRoot(treeID);

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
                    fGlobalViewer.setTreeInput(statisticsTreeNode);
                    return;
                }
                // Clears the old content to start over
                statisticsTreeNode.reset();
            }
        } else {
            // Creates a new tree
            statisticsTreeNode = TmfStatisticsTreeManager.addStatsTreeRoot(treeID, fGlobalViewer.getStatisticData());
        }

        // Sets the input to a clean data model
        fGlobalViewer.setTreeInput(statisticsTreeNode);
    }

    /**
     * Normally only called by UpdateJob objects to add events and (re)populate
     * the pie chart (specified by isGlobal) based on new information
     *
     * @param isGlobal
     *            Indicates if the events have to be added in the global model
     *            or the selection one
     * @param eventsPerType
     *            a map of events in the pair (Name of the event, number of
     *            occurences) to add to said model
     */
    private synchronized void addEventsTypeCount(boolean isGlobal, Map<String, Long> eventsPerType) {
        Map<String, Long> map;
        if (isGlobal) {
            map = fPieChartGlobalModel;
        } else {
            map = fPieChartSelectionModel;
        }

        for (Entry<String, Long> entry : eventsPerType.entrySet()) {
            // Check for the existence of the key
            Long oldValue = map.get(entry.getKey());
            if (oldValue != null) {
                // key already exists
                map.put(entry.getKey(), oldValue + entry.getValue());
            } else {
                map.put(entry.getKey(), entry.getValue());
            }
        }

        if (fUpdateJobsGlobal.size() == 1 && isGlobal) {
            /*
             * This thread is the last, update the view
             */
            getPieChartViewer().setGlobalPieChartEntries(fPieChartGlobalModel);
        }

        if (fUpdateJobsPartial.size() == 1 && !isGlobal) {
            getPieChartViewer().setTimeRangePieChartEntries(fPieChartSelectionModel);
        }
    }

    /**
     * Returns a unique ID based on name to be associated with the statistics
     * tree for this viewer. For a same name, it will always return the same ID.
     *
     * @return a unique statistics tree ID.
     * @since 1.0
     */
    public String getTreeID() {
        return fGlobalViewer.getTreeID();
    }

    /**
     * Tells if the viewer is listening to a trace.
     *
     * @param trace
     *            The trace that the viewer may be listening
     * @return true if the viewer is listening to the trace, false otherwise
     * @since 1.0
     */
    private boolean isListeningTo(ITmfTrace trace) {
        return (fTrace instanceof TmfExperiment) || trace == fTrace;
    }

    /**
     * Called when an trace request has been completed successfully.
     *
     * @param global
     *            Tells if the request is a global or time range (partial)
     *            request.
     * @since 1.0
     */
    private void modelComplete(boolean global) {
        fGlobalViewer.refresh();
        waitCursor(false);
    }

    /**
     * When the trace is loading the cursor will be different so the user knows
     * that the processing is not finished yet.
     *
     * Calls to this method are stacked.
     *
     * @param waitRequested
     *            Indicates if we need to show the waiting cursor, or the
     *            default one.
     * @since 1.0
     */
    private void waitCursor(final boolean waitRequested) {
        TreeViewer fTreeViewer = fGlobalViewer.getTreeViewer();
        if ((fTreeViewer == null) || (fTreeViewer.getTree().isDisposed())) {
            return;
        }

        boolean needsUpdate = false;
        Display display = fTreeViewer.getControl().getDisplay();
        if (waitRequested) {
            fWaitCursorCount++;
            if (fWaitCursor == null) { // The cursor hasn't been initialized yet
                fWaitCursor = new Cursor(display, SWT.CURSOR_WAIT);
            }
            if (fWaitCursorCount == 1) { // The cursor is not in waiting mode
                needsUpdate = true;
            }
        } else {
            if (fWaitCursorCount > 0) { // The cursor is in waiting mode
                fWaitCursorCount--;
                if (fWaitCursorCount == 0) { // No more reason to wait
                    // Put back the default cursor
                    needsUpdate = true;
                }
            }
        }

        if (needsUpdate) {
            // Performs the updates on the UI thread
            display.asyncExec(new Runnable() {
                @Override
                public void run() {
                    TreeViewer tv = fGlobalViewer.getTreeViewer();
                    if ((tv != null)
                            && (!tv.getTree().isDisposed())) {
                        Cursor cursor = null; // indicates default
                        if (waitRequested) {
                            cursor = fWaitCursor;
                        }
                        tv.getControl().setCursor(cursor);
                    }
                }
            });
        }
    }

    /**
     * @return The viewer representing the piechart.
     * @since 1.0
     */
    private TmfPieChartViewer getPieChartViewer() {
        return fPieChartViewer;
    }

    private class UpdateJob extends Job {

        private final ITmfTrace fJobTrace;
        private final boolean fIsGlobal;
        private final TmfStatisticsModule fStatsMod;

        private UpdateJob(String name, ITmfTrace trace, boolean isGlobal, TmfStatisticsModule statsMod) {
            super(name);
            fJobTrace = trace;
            fIsGlobal = isGlobal;
            fStatsMod = statsMod;
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {

            /* Wait until the analysis is ready to be queried */
            fStatsMod.waitForInitialization();
            ITmfStatistics stats = fStatsMod.getStatistics();
            if (stats == null) {
                /* It should have worked, but didn't */
                throw new IllegalStateException();
            }

            /*
             * TODO Eventually this could be exposed through the
             * TmfStateSystemAnalysisModule directly.
             */
            ITmfStateSystem ss = fStatsMod.getStateSystem(TmfStatisticsEventTypesModule.ID);
            if (ss == null) {
                /*
                 * It should be instantiated after the
                 * statsMod.waitForInitialization() above.
                 */
                throw new IllegalStateException();
            }

            /*
             * Periodically update the statistics while they are being built
             * (or, if the back-end is already completely built, it will skip
             * over the while() immediately.
             */
            long start = 0;
            long end = 0;
            boolean finished = false;
            do {
                if (monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
                finished = ss.waitUntilBuilt(LIVE_UPDATE_DELAY);

                TmfTimeRange localtimeRange = fIsGlobal ? fTimeRange : fTimeRangePartial;
                /*
                 * The generic statistics are stored in nanoseconds, so we must
                 * make sure the time range is scaled correctly.
                 */
                start = localtimeRange.getStartTime().normalize(0, TIME_SCALE).getValue();
                end = localtimeRange.getEndTime().normalize(0, TIME_SCALE).getValue();

                Map<String, Long> map = stats.getEventTypesInRange(start, end);
                updateStats(map);
            } while (!finished);

            /* Query one last time for the final values */
            Map<String, Long> map = stats.getEventTypesInRange(start, end);
            /*
             * Add the result for the current to the global (or selection) model
             * for the pie charts
             */
            addEventsTypeCount(fIsGlobal, map);
            updateStats(map);

            /*
             * Remove job from map so that new range selection updates can be
             * processed.
             */
            Map<ITmfTrace, Job> updateJobs;
            if (fIsGlobal) {
                updateJobs = fUpdateJobsGlobal;
            } else {
                updateJobs = fUpdateJobsPartial;
            }
            updateJobs.remove(fJobTrace);
            return Status.OK_STATUS;
        }

        /*
         * Update statistics for a given trace
         */
        private void updateStats(Map<String, Long> eventsPerType) {

            final TmfStatisticsTree statsData = TmfStatisticsTreeManager.getStatTree(getTreeID());
            if (statsData == null) {
                /* The stat tree has been disposed, abort mission. */
                return;
            }

            Map<String, Long> map = eventsPerType;
            String name = fJobTrace.getName();

            /*
             * "Global", "partial", "total", etc., it's all very confusing...
             *
             * The base view shows the total count for the trace and for each
             * even types, organized in columns like this:
             *
             * | Global | Time range | trace name | A | B | Event Type | | |
             * <event 1> | C | D | <event 2> | ... | ... | ... | | |
             *
             * Here, we called the cells like this: A : GlobalTotal B :
             * TimeRangeTotal C : GlobalTypeCount(s) D : TimeRangeTypeCount(s)
             */

            /* Fill in an the event counts (either cells C or D) */
            for (Map.Entry<String, Long> entry : map.entrySet()) {
                statsData.setTypeCount(name, entry.getKey(), fIsGlobal, entry.getValue());
            }

            /*
             * Calculate the totals (cell A or B, depending if isGlobal). We
             * will use the results of the previous request instead of sending
             * another one.
             */
            long globalTotal = 0;
            for (long val : map.values()) {
                globalTotal += val;
            }
            statsData.setTotal(name, fIsGlobal, globalTotal);

            modelComplete(fIsGlobal);
        }
    }
}
