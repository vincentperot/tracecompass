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

package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.sched;

import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.Attributes;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.StateValues;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.AbstractKernelEventHandlerWithCpuAndThread;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;

/**
 * Scheduler switch event handler
 */
public class Switch extends AbstractKernelEventHandlerWithCpuAndThread {

    /**
     * Constructor
     *
     * @param layout
     *            event layout
     */
    public Switch(IKernelAnalysisEventLayout layout) {
        super(layout);
    }

    @Override
    public boolean handleEvent(ITmfEvent event, ITmfStateSystemBuilder ss) throws AttributeNotFoundException {

        if (!super.handleEvent(event, ss)) {
            return false;
        }
        ITmfEventField content = event.getContent();
        Integer prevTid = ((Long) content.getField(getLayout().fieldPrevTid()).getValue()).intValue();
        Long prevState = (Long) content.getField(getLayout().fieldPrevState()).getValue();
        String nextProcessName = (String) content.getField(getLayout().fieldNextComm()).getValue();
        Integer nextTid = ((Long) content.getField(getLayout().fieldNextTid()).getValue()).intValue();
        Integer nextPrio = ((Long) content.getField(getLayout().fieldNextPrio()).getValue()).intValue();

        Integer formerThreadNode = ss.getQuarkRelativeAndAdd(getNodeThreads(ss), prevTid.toString());
        Integer newCurrentThreadNode = ss.getQuarkRelativeAndAdd(getNodeThreads(ss), nextTid.toString());

        /* Set the status of the process that got scheduled out. */
        int quark = ss.getQuarkRelativeAndAdd(formerThreadNode, Attributes.STATUS);
        ITmfStateValue value = (prevState != 0) ? StateValues.PROCESS_STATUS_WAIT_BLOCKED_VALUE : StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE;

        ss.modifyAttribute(getTimestamp(), value, quark);

        /* Set the status of the new scheduled process */
        setProcessToRunning(ss);

        /* Set the exec name of the new process */
        quark = ss.getQuarkRelativeAndAdd(newCurrentThreadNode, Attributes.EXEC_NAME);
        value = TmfStateValue.newValueString(nextProcessName);
        ss.modifyAttribute(getTimestamp(), value, quark);

        /* Set the current prio for the new process */
        quark = ss.getQuarkRelativeAndAdd(newCurrentThreadNode, Attributes.PRIO);
        value = TmfStateValue.newValueInt(nextPrio);
        ss.modifyAttribute(getTimestamp(), value, quark);

        /* Make sure the PPID and system_call sub-attributes exist */
        ss.getQuarkRelativeAndAdd(newCurrentThreadNode, Attributes.SYSTEM_CALL);
        ss.getQuarkRelativeAndAdd(newCurrentThreadNode, Attributes.PPID);

        /* Set the current scheduled process on the relevant CPU */
        quark = ss.getQuarkRelativeAndAdd(getCurrentCPUNode(), Attributes.CURRENT_THREAD);
        value = TmfStateValue.newValueInt(nextTid);
        ss.modifyAttribute(getTimestamp(), value, quark);

        /* Set the status of the CPU itself */
        if (nextTid > 0) {
            /* Check if the entering process is in kernel or user mode */
            quark = ss.getQuarkRelativeAndAdd(newCurrentThreadNode, Attributes.SYSTEM_CALL);
            if (ss.queryOngoingState(quark).isNull()) {
                value = StateValues.CPU_STATUS_RUN_USERMODE_VALUE;
            } else {
                value = StateValues.CPU_STATUS_RUN_SYSCALL_VALUE;
            }
        } else {
            value = StateValues.CPU_STATUS_IDLE_VALUE;
        }
        quark = ss.getQuarkRelativeAndAdd(getCurrentCPUNode(), Attributes.STATUS);
        ss.modifyAttribute(getTimestamp(), value, quark);
        return true;
    }

}
