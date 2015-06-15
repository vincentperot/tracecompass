/*******************************************************************************
 * Copyright (c) 2015 Ericsson
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
 *   Alexis Cabana-Loriaux <alex021994@gmail.com> -
 *
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.statistics;

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
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.TmfUiRefreshHandler;
import org.eclipse.tracecompass.tmf.ui.viewers.piecharts.TmfPieChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.statistics.TmfStatisticsViewer;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;
import org.eclipse.tracecompass.tmf.ui.widgets.tabsview.TmfViewerFolder;

/**
 * The generic Statistics View displays statistics for any kind of traces.
 *
 * It is implemented according to the MVC pattern. - The model is a
 * TmfStatisticsModel. The view is built with a TreeViewer as well as a
 * PieChartViewer. - The controller that keeps model and view synchronized is an
 * observer of the model.
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

    /**
     * Update range synchronization object
     */
    private final Object fStatisticsRangeUpdateSyncObj = new Object();

    /**
     * The viewer that builds the piecharts to show statistics
     *
     * @since 1.0
     */
    private final TmfPieChartViewer fPieChartViewer;

    /**
     * Tells to send a time range request when the trace gets updated.
     */
    private boolean fSendRangeRequest = true;

    /**
     * The viewer that builds the columns to show the statistics.
     *
     * @since 1.0
     */
    private final TmfViewerFolder fFolderViewer;

    /**
     * The viewer that contains the tree showing the statistics
     */
    private TmfStatisticsViewer fStatsViewer;

    /**
     * Object to store the cursor while waiting for the trace to load
     */
    private Cursor fWaitCursor = null;

    /**
     * The model holding the data for this view
     */
    private TmfStatisticsModel fModel;

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
        fModel = new TmfStatisticsModel(Messages.TmfStatisticsView_GlobalTabName, this);
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
        if (signal.getTrace() == fModel.getTrace()) {
            // no need to reopen the same trace.
            return;
        }
        // Update the current trace
        fModel.clear();
        fModel.setTrace(signal.getTrace());

        fFolderViewer.clear();
        createStatisticsTreeViewer();
        fModel.requestData(fModel.getTrace().getTimeRange());
        ViewUpdateJob viewerUpdateJob = new ViewUpdateJob(Messages.TmfStatisticsView_ViewUpdateJobName, true);
        viewerUpdateJob.schedule();
        fFolderViewer.layout();
        getPieChartViewer().layout();
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
        if (!fModel.isListeningTo(trace)) {
            return;
        }
        synchronized (fStatisticsRangeUpdateSyncObj) {
            if (fSendRangeRequest) {
                fSendRangeRequest = false;

                TmfTraceContext ctx = TmfTraceManager.getInstance().getCurrentTraceContext();
                TmfTimeRange timeRange = ctx.getSelectionRange();
                fModel.requestTimeRangeData(timeRange);
                ViewUpdateJob job = new ViewUpdateJob("Statistics View update", false); //$NON-NLS-1$
                job.schedule();
            }
        }
        fModel.requestData(signal.getRange());
        ViewUpdateJob job = new ViewUpdateJob("Statistics View update", true); //$NON-NLS-1$
        job.schedule();
    }

    /**
     * @param signal
     *            The selection range updated signal.
     * @since 1.0
     */
    @TmfSignalHandler
    public void traceSelectionRangeUpdated(final TmfSelectionRangeUpdatedSignal signal) {
        if (fModel.getTrace() == null) {
            return;
        }
        ITmfTimestamp begin = signal.getBeginTime();
        ITmfTimestamp end = signal.getEndTime();
        TmfTimeRange timeRange = new TmfTimeRange(begin, end);
        fModel.requestTimeRangeData(timeRange);
        ViewUpdateJob job = new ViewUpdateJob("Statistics View update", false); //$NON-NLS-1$
        job.schedule();
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
        if (signal.getTrace() != fModel.getTrace()) {
            /*
             * Dispose the current viewer and adapt the new one to the trace
             * type of the trace selected
             */
            fFolderViewer.clear();
            // Update the current trace
            fModel.clear();
            fModel.setTrace(signal.getTrace());
            createStatisticsTreeViewer();
            fFolderViewer.layout();
            sendPartialRequestOnNextUpdate();
            traceRangeUpdated(new TmfTraceRangeUpdatedSignal(this, fModel.getTrace(), fModel.getTrace().getTimeRange()));
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
        if (signal.getTrace() != fModel.getTrace()) {
            return;
        }

        // Clear the internal data
        fModel.clear();
        fModel.setTrace(null);

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
        if (fModel.getTrace() != null) {
            // Shows the name of the trace in the global tab
            fStatsViewer = new TmfStatisticsViewer(folder, Messages.TmfStatisticsView_GlobalTabName + " - " + fModel.getTrace().getName()); //$NON-NLS-1$
            fModel.setName(Messages.TmfStatisticsView_GlobalTabName + " - " + fModel.getTrace().getName()); //$NON-NLS-1$
            fFolderViewer.addTab(fStatsViewer, Messages.TmfStatisticsView_GlobalTabName, defaultStyle);
        } else {
            // There is no trace selected. Shows an empty global tab
            fStatsViewer = new TmfStatisticsViewer(folder, Messages.TmfStatisticsView_GlobalTabName);
            fModel.setName(Messages.TmfStatisticsView_GlobalTabName);
            fFolderViewer.addTab(fStatsViewer, Messages.TmfStatisticsView_GlobalTabName, defaultStyle);
        }

        fModel.initTreeInput();
        fStatsViewer.getTreeViewer().setInput(fModel.getStatisticData().getRootNode());
        // Makes the global viewer visible
        fFolderViewer.setSelection(0);
    }

    /**
     * Called when an trace request has been completed successfully.
     *
     * @param global
     *            Tells if the request is a global or time range (partial)
     *            request.
     * @since 1.0
     */
    public void modelComplete(boolean global) {
        fStatsViewer.refresh();
        // waitCursor(false);
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
        TreeViewer fTreeViewer = fStatsViewer.getTreeViewer();
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
                    TreeViewer tv = fStatsViewer.getTreeViewer();
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

    /**
     * Called when an trace request has been completed successfully.
     * Used to refresh the tree viewer once the model is finished
     * @since 1.0
     */
    private void signalTreeModelReady() {
        TmfUiRefreshHandler.getInstance().queueUpdate(this, new Runnable() {
            @Override
            public void run() {
                fStatsViewer.refresh();
                waitCursor(false);
            }
        });
    }

    /**
     * Called when an trace request has been completed successfully.
     *
     * @param global
     *            Tells if the request is a global or time range (partial)
     *            request.
     * @since 1.0
     */
    private void signalPieChartModelReady(boolean global) {
        if (global) {
            fPieChartViewer.setGlobalPieChartEntries(fModel.getPieChartGlobalModel());
        } else {
            fPieChartViewer.setTimeRangePieChartEntries(fModel.getPieChartSelectionModel());
        }
    }

    /**
     * @return the statistics viewer of this view
     * @since 1.0
     */
    public TmfStatisticsViewer getStatsViewer() {
        return fStatsViewer;
    }

    /**
     * @since 1.0
     * @return the model represented by this view
     */
    public TmfStatisticsModel getModel() {
        return fModel;
    }

    private class ViewUpdateJob extends Job {
        /** The delay (in ms) between each update in live-reading mode */
        private static final long LIVE_VIEW_UPDATE_DELAY = 500;

        /** A flag indicating if the update is global or time-range only */
        private final boolean fIsGlobal;

        public ViewUpdateJob(String name, boolean isGlobal) {
            super(name);
            fIsGlobal = isGlobal;
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            boolean finished = fIsGlobal ? fModel.isGlobalModelReady() : fModel.isPartialModelReady();
            do {
                /* Polling */
                if (monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }

                try {
                    Thread.sleep(LIVE_VIEW_UPDATE_DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                signalTreeModelReady();
                finished = fIsGlobal ? fModel.isGlobalModelReady() : fModel.isPartialModelReady();

            } while (!finished);
            /* send one last signal to update the tree and the charts*/
            signalTreeModelReady();
            signalPieChartModelReady(fIsGlobal);
            return Status.OK_STATUS;
        }
    }

}
