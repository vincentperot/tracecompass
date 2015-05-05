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

/**
 * Abstract handler with Irq support
 */
public abstract class AbstractKernelIrqHandler extends AbstractKernelEventHandlerWithCpuAndThread {

    /**
     * Constructor
     *
     * @param layout
     *            event layout
     */
    public AbstractKernelIrqHandler(IKernelAnalysisEventLayout layout) {
        super(layout);
    }

    /**
     * Similar logic as above, but to set the CPU's status when it's coming out
     * of an interruption.
     *
     * @param ssb
     *            State system
     * @throws StateValueTypeException
     *             the attribute is not set as an int
     * @throws AttributeNotFoundException
     *             the attribute was not created yet
     * @throws TimeRangeException
     *             the time is out of range
     */
    protected final void cpuExitInterrupt(ITmfStateSystemBuilder ssb)
            throws StateValueTypeException, AttributeNotFoundException,
            TimeRangeException {
        int quark;
        ITmfStateValue value;

        quark = ssb.getQuarkRelativeAndAdd(getCurrentCPUNode(), Attributes.CURRENT_THREAD);
        if (ssb.queryOngoingState(quark).unboxInt() > 0) {
            /* There was a process on the CPU */
            quark = ssb.getQuarkRelative(getCurrentThreadNode(), Attributes.SYSTEM_CALL);
            if (ssb.queryOngoingState(quark).isNull()) {
                /* That process was in user mode */
                value = StateValues.CPU_STATUS_RUN_USERMODE_VALUE;
            } else {
                /* That process was in a system call */
                value = StateValues.CPU_STATUS_RUN_SYSCALL_VALUE;
            }
        } else {
            /* There was no real process scheduled, CPU was idle */
            value = StateValues.CPU_STATUS_IDLE_VALUE;
        }
        quark = ssb.getQuarkRelativeAndAdd(getCurrentCPUNode(), Attributes.STATUS);
        ssb.modifyAttribute(getTimestamp(), value, quark);
    }
}
