/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.viewers.events;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.internal.tmf.core.filter.TmfCollapseFilter;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.core.component.ITmfEventProvider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest.ExecutionType;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Wrapper Thread object for the filtering thread.
 */
class TmfFilterJob extends Job {

    private final TmfEventsTable fEventsTable;
    private final ITmfFilterTreeNode fFilter;
    private final Object fSyncObj = new Object();

    private boolean fRefreshBusy = false;
    private boolean fRefreshPending = false;
    private TmfEventRequest fRequest;

    /**
     * Constructor.
     *
     * @param filter
     *            The filter this thread will be processing
     * @param tmfEventsTable
     *            The events table to filter
     */
    public TmfFilterJob(TmfEventsTable tmfEventsTable, final ITmfFilterTreeNode filter) {
        super("Filter Thread"); //$NON-NLS-1$
        fEventsTable = tmfEventsTable;
        fFilter = filter;
    }

    @Override
    public IStatus run(IProgressMonitor ipm) {
        final ITmfTrace trace = fEventsTable.getTrace();
        if (trace == null) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "No trace to filter"); //$NON-NLS-1$
        }
        final int nbRequested = (int) (trace.getNbEvents() - fEventsTable.getFilterCheckCount());
        if (nbRequested <= 0) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Trace has no events"); //$NON-NLS-1$
        }
        ipm.beginTask("Filtering trace", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
        fRequest = new TmfEventRequest(ITmfEvent.class, TmfTimeRange.ETERNITY,
                (int) fEventsTable.getFilterCheckCount(), nbRequested, ExecutionType.BACKGROUND) {
            @Override
            public void handleData(final ITmfEvent event) {
                super.handleData(event);
                if (fRequest.isCancelled()) {
                    return;
                }
                boolean refresh = false;
                if (getFilter().matches(event)) {
                    final long rank = fEventsTable.getFilterCheckCount();
                    final int index = (int) fEventsTable.getFilterMatchCount();
                    fEventsTable.incrementFilterMatchCount();
                    fEventsTable.getCache().storeEvent(event, rank, index);
                    refresh = true;
                } else {
                    if (getFilter() instanceof TmfCollapseFilter) {
                        fEventsTable.getCache().updateCollapsedEvent((int) fEventsTable.getFilterMatchCount() - 1);
                    }
                }

                if (refresh || (TmfFilterJob.this.fEventsTable.getFilterCheckCount() % 100) == 0) {
                    refreshTable();
                }
                fEventsTable.incrementFilterCheckCount();
            }
        };
        ((ITmfEventProvider) trace).sendRequest(fRequest);
        try {
            while (fRequest.isCompleted()) {
                if (ipm.isCanceled()) {
                    /*
                     * Cancel this filtering thread.
                     */
                    fRequest.cancel();
                }
                /*
                 * arbitrary wait period
                 */
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            Activator.getDefault().logError(e.getMessage(), e);
        }
        refreshTable();
        synchronized (fEventsTable.getFilterSyncObj()) {
            fEventsTable.setFilterJob(null);
            if (fEventsTable.isFilterThreadResume()) {
                fEventsTable.setFilterThreadResume(false);
                fEventsTable.setFilterJob(new TmfFilterJob(fEventsTable, getFilter()));
            }
        }
        return Status.OK_STATUS;
    }

    /**
     * Refresh the filter.
     */
    public void refreshTable() {
        synchronized (fSyncObj) {
            if (fRefreshBusy) {
                fRefreshPending = true;
                return;
            }
            fRefreshBusy = true;
        }
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fRequest.isCancelled()) {
                    return;
                }
                if (fEventsTable.getTable().isDisposed()) {
                    return;
                }
                // +1 for header row, +2 for top and bottom filter status rows
                fEventsTable.getTable().setItemCount((int) fEventsTable.getFilterMatchCount() + 3);
                fEventsTable.getTable().refresh();
                synchronized (fSyncObj) {
                    fRefreshBusy = false;
                    if (fRefreshPending) {
                        fRefreshPending = false;
                        refreshTable();
                    }
                }
            }
        });
    }

    ITmfFilterTreeNode getFilter() {
        return fFilter;
    }
}