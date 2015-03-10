/*******************************************************************************
 * Copyright (c) 2015 Keba AG
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Christian Mansky - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.ui.views.controlflow;

import java.util.List;

import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.Attributes;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.KernelAnalysis;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.StateValues;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.ITimeGraphEntryActiveProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;

/**
 * Provides Functionality for check Active / uncheck inactive
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 * @since 1.0
 */
public class ControlFlowCheckActiveProvider implements ITimeGraphEntryActiveProvider {

    String fLabel;
    String fTooltip;

    /**
     * @param label Button label
     * @param tooltip Button tooltip
     */
    public ControlFlowCheckActiveProvider(String label, String tooltip) {
        fLabel = label;
        fTooltip = tooltip;
    }

    @Override
    public String getLabel() {
        return fLabel;
    }

    @Override
    public String getTooltip() {
        return fTooltip;
    }

    @Override
    public boolean isActive(ITimeGraphEntry element) {
        if (element instanceof ControlFlowEntry) {
            ControlFlowEntry cfe = (ControlFlowEntry) element;

            TmfTraceManager traceManager = TmfTraceManager.getInstance();
            TmfTraceContext traceContext = traceManager.getCurrentTraceContext();
            TmfTimeRange winRange = traceContext.getWindowRange();
            TmfTimeRange selRange = traceContext.getSelectionRange();

            /* Take precedence of selection over window range. */
            long beginTS = selRange.getStartTime().getValue();
            long endTS = selRange.getEndTime().getValue();

            /* No selection, take window range */
            if (beginTS == endTS) {
                beginTS = winRange.getStartTime().getValue();
                endTS = winRange.getEndTime().getValue();
            }

            for (ITmfTrace trace : traceManager.getActiveTraceSet()) {
                if (trace == null) {
                    continue;
                }
                ITmfStateSystem ssq = TmfStateSystemAnalysisModule.getStateSystem(trace, KernelAnalysis.ID);
                if (ssq == null) {
                    continue;
                }

                try {
                    int statusQuark = ssq.getQuarkAbsolute(Attributes.THREADS, Integer.toString(cfe.getThreadId()), Attributes.STATUS);
                    List<ITmfStateInterval> ivals = StateSystemUtils.queryHistoryRange(ssq, statusQuark, beginTS, endTS);
                    for (ITmfStateInterval state : ivals) {
                        int value = state.getStateValue().unboxInt();
                        /* An entry is only active when running */
                        if (value == StateValues.PROCESS_STATUS_RUN_USERMODE || value == StateValues.PROCESS_STATUS_RUN_SYSCALL ||
                                value == StateValues.PROCESS_STATUS_INTERRUPTED) {
                            return true;
                        }
                    }
                } catch (AttributeNotFoundException | StateSystemDisposedException e) {
                    /* Ignore ... */
                }

            }
        }

        return false;
    }

}
