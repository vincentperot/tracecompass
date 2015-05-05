/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent;

import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.Attributes;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.StateValues;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * This adds the support for thread quark lookups
 */
public abstract class AbstractKernelEventHandlerWithCpuAndThread extends AbstractKernelEventHandlerWithCpu {

    private int fCurrentThreadNode;

    /**
     * Constructor
     *
     * @param layout
     *            the analysis layout
     */
    public AbstractKernelEventHandlerWithCpuAndThread(IKernelAnalysisEventLayout layout) {
        super(layout);
    }

    /**
     * Get the current thread node
     *
     * @return the current thread node quark
     */
    protected int getCurrentThreadNode() {
        return fCurrentThreadNode;
    }

    @Override
    public boolean handleEvent(ITmfEvent event, ITmfStateSystemBuilder ss) throws AttributeNotFoundException {
        if (!super.handleEvent(event, ss)) {
            return false;
        }
        /*
         * Shortcut for the "current thread" attribute node. It requires
         * querying the current CPU's current thread.
         */
        int quark = ss.getQuarkRelativeAndAdd(getCurrentCPUNode(), Attributes.CURRENT_THREAD);
        ITmfStateValue value = ss.queryOngoingState(quark);
        int thread = value.isNull() ? -1 : value.unboxInt();
        fCurrentThreadNode = ss.getQuarkRelativeAndAdd(getNodeThreads(ss), String.valueOf(thread));
        return true;
    }

    /**
     * When we want to set a process back to a "running" state, first check its
     * current System_call attribute. If there is a system call active, we put
     * the process back in the syscall state. If not, we put it back in user
     * mode state.
     *
     * @param ssb
     *            the state system
     * @throws AttributeNotFoundException
     *             an attribute does not exists yet
     * @throws TimeRangeException
     *             the time is out of range
     * @throws StateValueTypeException
     *             the attribute was not set with int values
     */
    protected final void setProcessToRunning(ITmfStateSystemBuilder ssb)
            throws AttributeNotFoundException, TimeRangeException,
            StateValueTypeException {
        int quark;
        ITmfStateValue value;

        quark = ssb.getQuarkRelativeAndAdd(getCurrentThreadNode(), Attributes.SYSTEM_CALL);
        if (ssb.queryOngoingState(quark).isNull()) {
            /* We were in user mode before the interruption */
            value = StateValues.PROCESS_STATUS_RUN_USERMODE_VALUE;
        } else {
            /* We were previously in kernel mode */
            value = StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE;
        }
        quark = ssb.getQuarkRelativeAndAdd(getCurrentThreadNode(), Attributes.STATUS);
        ssb.modifyAttribute(getTimestamp(), value, quark);
    }
}
