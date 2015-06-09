/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.commands;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.filter.ITmfFilter;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest.ExecutionType;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.viewers.events.columns.TmfEventTableColumn;
import org.eclipse.ui.PlatformUI;

/**
 * This job copies the trace selection to the clipboard.
 */
public class CopyToClipboardJob extends Job {

    private final ITmfTrace fTrace;
    private final ITmfFilter fFilter;
    private final List<TmfEventTableColumn> fColumns;
    private final long fStart;
    private final long fEnd;

    /**
     * Job constructor.
     *
     * @param trace
     *            the trace to copy events from
     * @param filter
     *            the filter to apply to trace events, or null
     * @param columns
     *            the list of event table columns
     * @param start
     *            the start rank of the selection
     * @param end
     *            the end rank of the selection
     */
    public CopyToClipboardJob(ITmfTrace trace, ITmfFilter filter, List<TmfEventTableColumn> columns, long start, long end) {
        super(Messages.CopyToClipboardJob_Name);
        fTrace = trace;
        fFilter = filter;
        fColumns = columns;
        fStart = start;
        fEnd = end;
    }

    @Override
    public IStatus run(IProgressMonitor monitor) {
        final StringBuilder sb = new StringBuilder();
        monitor.beginTask(Messages.CopyToClipboardJob_Name, (int) (fEnd - fStart + 1));

        if (fColumns != null) {
            boolean needTab = false;
            for (TmfEventTableColumn column : fColumns) {
                if (needTab) {
                    sb.append('\t');
                }
                sb.append(column.getHeaderName());
                needTab = true;
            }
            sb.append('\n');
        }

        copy(sb, monitor);

        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                if (sb.length() == 0) {
                    return;
                }
                Clipboard clipboard = new Clipboard(Display.getDefault());
                clipboard.setContents(new Object[] { sb.toString() },
                        new Transfer[] { TextTransfer.getInstance() });
            }
        });

        monitor.done();
        return Status.OK_STATUS;
    }

    private IStatus copy(final StringBuilder sb, final IProgressMonitor monitor) {
        ITmfEventRequest request = new TmfEventRequest(ITmfEvent.class, TmfTimeRange.ETERNITY, fStart, (int) (fEnd - fStart + 1), ExecutionType.FOREGROUND) {
            @Override
            public void handleData(ITmfEvent event) {
                super.handleData(event);
                if (monitor.isCanceled()) {
                    cancel();
                    return;
                }
                monitor.worked(1);
                if (fFilter == null || fFilter.matches(event)) {
                    try {
                        boolean needTab = false;
                        for (TmfEventTableColumn column : fColumns) {
                            if (needTab) {
                                sb.append('\t');
                            }
                            sb.append(column.getItemString(event));
                            needTab = true;
                        }
                        sb.append('\n');
                    } catch (java.lang.OutOfMemoryError e) {
                        sb.setLength(0);
                        sb.trimToSize();
                        showErrorDialog();
                        cancel();
                    }
                }
            }
        };
        fTrace.sendRequest(request);
        try {
            request.waitForCompletion();
        } catch (InterruptedException e) {
            Activator.getDefault().logError("Wait for completion interrupted for copy to clipboard ", e); //$NON-NLS-1$
        }
        return Status.OK_STATUS;
    }

    private static void showErrorDialog() {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                MessageBox confirmOperation = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                confirmOperation.setText(Messages.CopyToClipboardJob_OutOfMemoryErrorTitle);
                confirmOperation.setMessage(Messages.CopyToClipboardJob_OutOfMemoryErrorMessage);
                confirmOperation.open();
            }
        });
    }
}
