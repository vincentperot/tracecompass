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
 *
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis;

import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

interface IKernelStateProvider {

    /**
     * Handle a system call entry event
     *
     * @param ss
     *            the state system provider
     * @param ts
     *            the current timestamp
     * @param eventName
     *            the name of the event (can be prepended with syscall prefix)
     * @param currentCPUNode
     *            the current cpu node in the state system the cpu node
     * @param currentThreadNode
     *            the current thread node in the state system the thread node
     * @throws AttributeNotFoundException
     *             an exception if the attribute it not found
     */
    void handleSyscallEntry(final ITmfStateSystemBuilder ss, final long ts, final String eventName, final int currentCPUNode, final int currentThreadNode) throws AttributeNotFoundException;

    /**
     * Handle a system call exit
     *
     * @param ss
     *            the state system provider
     * @param ts
     *            the current timestamp
     * @param currentCPUNode
     *            the current cpu node in the state system the cpu node
     * @param currentThreadNode
     *            the current thread node in the state system the thread node
     * @throws AttributeNotFoundException
     *             an exception if the attribute it not found
     */
    void handleSyscallExit(final ITmfStateSystemBuilder ss, final long ts, final int currentCPUNode, final int currentThreadNode) throws AttributeNotFoundException;

    /**
     * Handle a system call exit
     *
     * @param ss
     *            the state system provider
     * @param ts
     *            the current timestamp the current timestamp
     * @param event
     *            the event triggering the state change
     * @param currentCPUNode
     *            the current cpu node in the state system the cpu node
     * @param currentThreadNode
     *            the current thread node in the state system the thread node
     * @throws AttributeNotFoundException
     *             an exception if the attribute it not found
     */
    void handleSchedSwitch(final ITmfStateSystemBuilder ss, final long ts, ITmfEvent event, final int currentCPUNode) throws AttributeNotFoundException;

    /**
     * Handle a scheduler wake up event
     *
     * @param ss
     *            the state system provider
     * @param ts
     *            the current timestamp the current timestamp
     * @param event
     *            the event triggering this state change
     * @throws AttributeNotFoundException
     *             an exception if the attribute it not found
     */
    void handleSchedWakeup(final ITmfStateSystemBuilder ss, final long ts, ITmfEvent event) throws AttributeNotFoundException;

    /**
     * Handle a scheduler process fork event
     *
     * @param ss
     *            the state system provider
     * @param ts
     *            the current timestamp
     * @param event
     *            the event triggering this state change
     * @throws AttributeNotFoundException
     *             an exception if the attribute it not found
     */
    void handleSchedProcessFork(final ITmfStateSystemBuilder ss, final long ts, ITmfEvent event) throws AttributeNotFoundException;

    /**
     * handle scheduler process free
     *
     * @param ss
     *            the state system provider
     * @param ts
     *            the current timestamp
     * @param event
     *            the event triggering this state change
     * @throws AttributeNotFoundException
     *             an exception if the attribute it not found
     */
    void handleSchedProcessFree(final ITmfStateSystemBuilder ss, final long ts, ITmfEvent event) throws AttributeNotFoundException;

    /**
     *
     * Handle interrupt request handler entry
     *
     * @param ss
     *            the state system provider
     * @param ts
     *            the current timestamp
     * @param event
     *            the event triggering this state change
     * @param cpu
     *            which cpu had the irq
     * @param currentCPUNode
     *            the current cpu node in the state system
     * @param currentThreadNode
     *            the current thread node in the state system
     * @throws AttributeNotFoundException
     *             an exception if the attribute it not found
     */
    void handleIrqHandlerEntry(final ITmfStateSystemBuilder ss, final long ts, ITmfEvent event, Integer cpu, final int currentCPUNode, final int currentThreadNode) throws AttributeNotFoundException;

    /**
     * Handle an interrupt request handler exit
     *
     * @param ss
     *            the state system provider
     * @param ts
     *            the current timestamp
     * @param event
     *            the event triggering this state change
     * @param currentCPUNode
     *            the current cpu node in the state system
     * @param currentThreadNode
     *            the current thread node in the state system
     * @throws AttributeNotFoundException
     *             an exception if the attribute it not found
     */
    void handleIrqHandlerExit(final ITmfStateSystemBuilder ss, final long ts, ITmfEvent event, final int currentCPUNode, final int currentThreadNode) throws AttributeNotFoundException;

    /**
     * Handle a softirq raise event
     *
     * @param ss
     *            the state system provider
     * @param ts
     *            the current timestamp
     * @param event
     *            the event triggering this state change
     * @throws AttributeNotFoundException
     *             an exception if the attribute it not found
     */
    void handleSoftIrqRaise(final ITmfStateSystemBuilder ss, final long ts, ITmfEvent event) throws AttributeNotFoundException;

    /**
     * Handle a softirq handler entry event
     *
     * @param ss
     *            the state system provider
     * @param ts
     *            the current timestamp
     * @param event
     *            the event triggering this state change
     * @param cpu
     *            the cpu handling the softirq
     * @param currentCPUNode
     *            the current cpu node in the state system
     * @param currentThreadNode
     *            the current thread node in the state system
     * @throws AttributeNotFoundException
     *             an exception if the attribute it not found
     */
    void handleSoftIrqHandlerEntry(final ITmfStateSystemBuilder ss, final long ts, ITmfEvent event, Integer cpu, final int currentCPUNode, final int currentThreadNode) throws AttributeNotFoundException;

    /**
     * Handle softirq handler exit event
     *
     * @param ss
     *            the state system provider
     * @param ts
     *            the current timestamp
     * @param event
     *            the event triggering this state change
     * @param currentCPUNode
     *            the current cpu node in the state system
     * @param currentThreadNode
     *            the current thread node in the state system
     * @throws AttributeNotFoundException
     *             an exception if the attribute it not found
     */
    void handleSoftIrqExit(final ITmfStateSystemBuilder ss, final long ts, ITmfEvent event, final int currentCPUNode, final int currentThreadNode) throws AttributeNotFoundException;
}
