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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.tracecompass.tmf.core.filter.ITmfFilter;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.viewers.events.columns.TmfEventTableColumn;

/**
 * This handler copies the text of selected trace events to the clipboard.
 */
public class CopyToClipboardCommandHandler extends AbstractHandler {

    /** Id of the copy-to-clipboard command */
    public static final String COMMAND_ID = "org.eclipse.linuxtools.tmf.ui.copy_to_clipboard"; //$NON-NLS-1$
    /** Id of the columns variable. List of event table columns. */
    public static final String COLUMNS_VAR = "org.eclipse.linuxtools.tmf.ui.copy_to_clipboard.columns"; //$NON-NLS-1$
    /** Id of the start variable. Event rank of the selection start. */
    public static final String START_VAR = "org.eclipse.linuxtools.tmf.ui.copy_to_clipboard.start"; //$NON-NLS-1$
    /** Id of the end variable. Event rank of the selection end. */
    public static final String END_VAR = "org.eclipse.linuxtools.tmf.ui.copy_to_clipboard.end"; //$NON-NLS-1$

    /**
     * Constructor
     */
    public CopyToClipboardCommandHandler() {
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        List<TmfEventTableColumn> columns = getColumns(event.getApplicationContext());
        long start = getStart(event.getApplicationContext());
        long end = getEnd(event.getApplicationContext());
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        ITmfFilter filter = TmfTraceManager.getInstance().getCurrentTraceContext().getFilter();
        if (trace != null) {
            Job job = new CopyToClipboardJob(trace, filter, columns, start, end);
            job.schedule();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static List<TmfEventTableColumn> getColumns(Object evaluationContext) {
        if (evaluationContext instanceof IEvaluationContext) {
            Object o = ((IEvaluationContext) evaluationContext).getVariable(COLUMNS_VAR);
            if (o instanceof List<?>) {
                return (List<TmfEventTableColumn>) o;
            }
        }
        return null;
    }

    private static long getStart(Object evaluationContext) {
        if (evaluationContext instanceof IEvaluationContext) {
            Object o = ((IEvaluationContext) evaluationContext).getVariable(START_VAR);
            if (o instanceof Long) {
                return (long) o;
            }
        }
        return 0;
    }

    private static long getEnd(Object evaluationContext) {
        if (evaluationContext instanceof IEvaluationContext) {
            Object o = ((IEvaluationContext) evaluationContext).getVariable(END_VAR);
            if (o instanceof Long) {
                return (long) o;
            }
        }
        return 0;
    }

}
