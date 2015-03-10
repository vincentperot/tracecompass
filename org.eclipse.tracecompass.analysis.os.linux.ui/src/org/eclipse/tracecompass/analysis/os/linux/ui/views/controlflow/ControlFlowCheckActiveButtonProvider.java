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

import java.util.Iterator;

import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.StateValues;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.ITimeGraphFilterAdditionalButtonInfo;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;

/**
 * Provides Functionality for check Active / uncheck inactive
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 * @since 1.0
 */
public class ControlFlowCheckActiveButtonProvider implements ITimeGraphFilterAdditionalButtonInfo {

    private boolean fIsCheckActive;

    /**
     * @param isCheckActive
     *            changes the behavior. True, check active. False, check
     *            inactive
     */
    public ControlFlowCheckActiveButtonProvider(boolean isCheckActive) {
        fIsCheckActive = isCheckActive;
    }

    @Override
    public String getButtonLabel() {
        if (fIsCheckActive) {
            return "Check active"; //$NON-NLS-1$
        }

        return "Uncheck inactive"; //$NON-NLS-1$
    }

    @Override
    public String getToolTip() {
        if (fIsCheckActive) {
            return "Checks all threads executing within the time frame."; //$NON-NLS-1$
        }

        return "Unchecks all threads not executing within the time frame."; //$NON-NLS-1$
    }

    @Override
    public boolean checkSelectionStatus(Object element) {
        if (!fIsCheckActive) {
            return false;
        }

        if (element instanceof ControlFlowEntry) {
            return isActive((ControlFlowEntry) element);
        }
        return false; // do nothing per default
    }

    @Override
    public boolean checkUnselectionStatus(Object element) {
        if (fIsCheckActive) {
            return false;
        }

        if (element instanceof ControlFlowEntry) {
            return !isActive((ControlFlowEntry) element);
        }

        return false; // do nothing per default
    }

    private static boolean isActive(ControlFlowEntry cfe) {
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

        Iterator<ITimeEvent> iterator = cfe.getTimeEventsIterator(beginTS, endTS, 1);

        while (iterator.hasNext()) {
            ITimeEvent event = iterator.next();
            if ((event.getTime() <= endTS && (event.getTime() + event.getDuration() >= beginTS))) {
                TimeEvent timeEvent = (TimeEvent) event;
                int value = timeEvent.getValue();
                /* An entry is only active when running */
                if (value == StateValues.PROCESS_STATUS_RUN_USERMODE || value == StateValues.PROCESS_STATUS_RUN_SYSCALL ||
                        value == StateValues.PROCESS_STATUS_INTERRUPTED) {
                    return true;
                }
            }
        }
        return false;
    }

}
