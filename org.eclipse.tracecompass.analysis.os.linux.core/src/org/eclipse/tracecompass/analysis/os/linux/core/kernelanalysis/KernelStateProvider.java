/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.ImmutableMap;

/**
 * This is the state change input plugin for TMF's state system which handles
 * the LTTng 2.0 kernel traces in CTF format.
 *
 * It uses the reference handler defined in CTFKernelHandler.java.
 *
 * @author alexmont
 *
 */
public class KernelStateProvider extends AbstractTmfStateProvider {

    // ------------------------------------------------------------------------
    // Static fields
    // ------------------------------------------------------------------------

    /**
     * Version number of this state provider. Please bump this if you modify the
     * contents of the generated state history in some way.
     */
    private static final int VERSION = 5;

    private static final int IRQ_HANDLER_ENTRY_INDEX = 1;
    private static final int IRQ_HANDLER_EXIT_INDEX = 2;
    private static final int SOFT_IRQ_ENTRY_INDEX = 3;
    private static final int SOFT_IRQ_EXIT_INDEX = 4;
    private static final int SOFT_IRQ_RAISE_INDEX = 5;
    private static final int SCHED_SWITCH_INDEX = 6;
    private static final int SCHED_PROCESS_FORK_INDEX = 7;
    private static final int SCHED_PROCESS_EXIT_INDEX = 8;
    private static final int SCHED_PROCESS_FREE_INDEX = 9;
    private static final int STATEDUMP_PROCESS_STATE_INDEX = 10;
    private static final int SCHED_WAKEUP_INDEX = 11;

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    private final Map<String, Integer> fEventNames;
    private final IKernelAnalysisEventLayout fLayout;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Instantiate a new state provider plugin.
     *
     * @param trace
     *            The LTTng 2.0 kernel trace directory
     * @param layout
     *            The event layout to use for this state provider. Usually
     *            depending on the tracer implementation.
     */
    public KernelStateProvider(ITmfTrace trace, IKernelAnalysisEventLayout layout) {
        super(trace, "Kernel"); //$NON-NLS-1$
        fLayout = layout;
        fEventNames = buildEventNames(layout);
    }

    // ------------------------------------------------------------------------
    // Event names management
    // ------------------------------------------------------------------------

    private static Map<String, Integer> buildEventNames(IKernelAnalysisEventLayout layout) {
        ImmutableMap.Builder<String, Integer> builder = ImmutableMap.builder();

        builder.put(layout.eventIrqHandlerEntry(), IRQ_HANDLER_ENTRY_INDEX);
        builder.put(layout.eventIrqHandlerExit(), IRQ_HANDLER_EXIT_INDEX);
        builder.put(layout.eventSoftIrqEntry(), SOFT_IRQ_ENTRY_INDEX);
        builder.put(layout.eventSoftIrqExit(), SOFT_IRQ_EXIT_INDEX);
        builder.put(layout.eventSoftIrqRaise(), SOFT_IRQ_RAISE_INDEX);
        builder.put(layout.eventSchedSwitch(), SCHED_SWITCH_INDEX);
        builder.put(layout.eventSchedProcessFork(), SCHED_PROCESS_FORK_INDEX);
        builder.put(layout.eventSchedProcessExit(), SCHED_PROCESS_EXIT_INDEX);
        builder.put(layout.eventSchedProcessFree(), SCHED_PROCESS_FREE_INDEX);

        final String eventStatedumpProcessState = layout.eventStatedumpProcessState();
        if (eventStatedumpProcessState != null) {
            builder.put(eventStatedumpProcessState, STATEDUMP_PROCESS_STATE_INDEX);
        }

        for (String eventSchedWakeup : layout.eventsSchedWakeup()) {
            builder.put(eventSchedWakeup, SCHED_WAKEUP_INDEX);
        }

        return checkNotNull(builder.build());
    }

    // ------------------------------------------------------------------------
    // IStateChangeInput
    // ------------------------------------------------------------------------

    @Override
    public int getVersion() {
        return VERSION;
    }

    @Override
    public void assignTargetStateSystem(ITmfStateSystemBuilder ssb) {
        /* We can only set up the locations once the state system is assigned */
        super.assignTargetStateSystem(ssb);
    }

    @Override
    public KernelStateProvider getNewInstance() {
        return new KernelStateProvider(this.getTrace(), fLayout);
    }

    @Override
    protected void eventHandle(@Nullable ITmfEvent event) {
        if (event == null) {
            return;
        }

        Object cpuObj = TmfTraceUtils.resolveEventAspectOfClassForEvent(event.getTrace(), TmfCpuAspect.class, event);
        if (cpuObj == null) {
            /* We couldn't find any CPU information, ignore this event */
            return;
        }
        Integer cpu = (Integer) cpuObj;

        final String eventName = event.getType().getName();
        final long ts = event.getTimestamp().getValue();

        try {
            final ITmfStateSystemBuilder ss = checkNotNull(getStateSystemBuilder());

            /* Shortcut for the "current CPU" attribute node */
            final int currentCPUNode = ss.getQuarkRelativeAndAdd(getNodeCPUs(ss), cpu.toString());

            /*
             * Shortcut for the "current thread" attribute node. It requires
             * querying the current CPU's current thread.
             */
            int quark = ss.getQuarkRelativeAndAdd(currentCPUNode, Attributes.CURRENT_THREAD);
            ITmfStateValue value = ss.queryOngoingState(quark);
            int thread = value.isNull() ? -1 : value.unboxInt();
            final int currentThreadNode = ss.getQuarkRelativeAndAdd(getNodeThreads(ss), String.valueOf(thread));

            /*
             * Feed event to the history system if it's known to cause a state
             * transition.
             */
            Integer idx = fEventNames.get(eventName);
            int intval = (idx == null ? -1 : idx.intValue());
            switch (intval) {

            case IRQ_HANDLER_ENTRY_INDEX:
                handleIrqHandlerEntry(ss, ts, event, cpu, currentCPUNode, currentThreadNode);
                break;

            case IRQ_HANDLER_EXIT_INDEX:
                handleIrqHandlerExit(ss, ts, event, currentCPUNode, currentThreadNode);
                break;

            case SOFT_IRQ_ENTRY_INDEX:
                handleSoftIrqHandlerEntry(ss, ts, event, cpu, currentCPUNode, currentThreadNode);
                break;

            case SOFT_IRQ_EXIT_INDEX:
                handleSoftIrqExit(ss, ts, event, currentCPUNode, currentThreadNode);
                break;

            case SOFT_IRQ_RAISE_INDEX:
                handleSoftIrqRaise(ss, ts, event);
                break;

            case SCHED_SWITCH_INDEX:
                handleSchedSwitch(ss, ts, event, currentCPUNode);
                break;

            case SCHED_PROCESS_FORK_INDEX:
                handleSchedProcessFork(ss, ts, event);
                break;

            case SCHED_PROCESS_EXIT_INDEX:
                break;

            case SCHED_PROCESS_FREE_INDEX:
                handleSchedProcessFree(ss, ts, event);
                break;

            case STATEDUMP_PROCESS_STATE_INDEX:
                handleLttngStatedumpProcessState(ss, ts, event);
                break;

            case SCHED_WAKEUP_INDEX:
                handleSchedWakeup(ss, ts, event);
                break;

            default:
            /* Other event types not covered by the main switch */
            {
                if (eventName.startsWith(fLayout.eventSyscallEntryPrefix())
                        || eventName.startsWith(fLayout.eventCompatSyscallEntryPrefix())) {
                    handleSyscallEntry(ss, ts, eventName, currentCPUNode, currentThreadNode);
                } else if (eventName.startsWith(fLayout.eventSyscallExitPrefix())) {
                    handleSyscallExit(ss, ts, currentCPUNode, currentThreadNode);
                }

            }
                break;
            } // End of big switch

        } catch (AttributeNotFoundException ae) {
            /*
             * This would indicate a problem with the logic of the manager here,
             * so it shouldn't happen.
             */
            ae.printStackTrace();

        } catch (TimeRangeException tre) {
            /*
             * This would happen if the events in the trace aren't ordered
             * chronologically, which should never be the case ...
             */
            System.err.println("TimeRangeExcpetion caught in the state system's event manager."); //$NON-NLS-1$
            System.err.println("Are the events in the trace correctly ordered?"); //$NON-NLS-1$
            tre.printStackTrace();

        } catch (StateValueTypeException sve) {
            /*
             * This would happen if we were trying to push/pop attributes not of
             * type integer. Which, once again, should never happen.
             */
            sve.printStackTrace();
        }
    }

    // ------------------------------------------------------------------------
    // Kernel state provider helpers
    // ------------------------------------------------------------------------

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
    private static void handleSyscallEntry(final ITmfStateSystemBuilder ss, final long ts, final String eventName, final int currentCPUNode, final int currentThreadNode) throws AttributeNotFoundException {
        int quark;
        ITmfStateValue value;
        /* Assign the new system call to the process */
        quark = ss.getQuarkRelativeAndAdd(currentThreadNode, Attributes.SYSTEM_CALL);
        value = TmfStateValue.newValueString(eventName);
        ss.modifyAttribute(ts, value, quark);

        /* Put the process in system call mode */
        quark = ss.getQuarkRelativeAndAdd(currentThreadNode, Attributes.STATUS);
        value = StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE;
        ss.modifyAttribute(ts, value, quark);

        /* Put the CPU in system call (kernel) mode */
        quark = ss.getQuarkRelativeAndAdd(currentCPUNode, Attributes.STATUS);
        value = StateValues.CPU_STATUS_RUN_SYSCALL_VALUE;
        ss.modifyAttribute(ts, value, quark);
    }

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
    private static void handleSyscallExit(final ITmfStateSystemBuilder ss, final long ts, final int currentCPUNode, final int currentThreadNode) throws AttributeNotFoundException {
        int quark;
        ITmfStateValue value;
        /* Clear the current system call on the process */
        quark = ss.getQuarkRelativeAndAdd(currentThreadNode, Attributes.SYSTEM_CALL);
        value = TmfStateValue.nullValue();
        ss.modifyAttribute(ts, value, quark);

        /* Put the process' status back to user mode */
        quark = ss.getQuarkRelativeAndAdd(currentThreadNode, Attributes.STATUS);
        value = StateValues.PROCESS_STATUS_RUN_USERMODE_VALUE;
        ss.modifyAttribute(ts, value, quark);

        /* Put the CPU's status back to user mode */
        quark = ss.getQuarkRelativeAndAdd(currentCPUNode, Attributes.STATUS);
        value = StateValues.CPU_STATUS_RUN_USERMODE_VALUE;
        ss.modifyAttribute(ts, value, quark);
    }

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
    private void handleSchedWakeup(final ITmfStateSystemBuilder ss, final long ts, ITmfEvent event) throws AttributeNotFoundException {
        int quark;
        ITmfStateValue value;
        {
            final int tid = ((Long) event.getContent().getField(fLayout.fieldTid()).getValue()).intValue();
            final int threadNode = ss.getQuarkRelativeAndAdd(getNodeThreads(ss), String.valueOf(tid));

            /*
             * The process indicated in the event's payload is now ready to run.
             * Assign it to the "wait for cpu" state, but only if it was not
             * already running.
             */
            quark = ss.getQuarkRelativeAndAdd(threadNode, Attributes.STATUS);
            int status = ss.queryOngoingState(quark).unboxInt();

            if (status != StateValues.PROCESS_STATUS_RUN_SYSCALL &&
                    status != StateValues.PROCESS_STATUS_RUN_USERMODE) {
                value = StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE;
                ss.modifyAttribute(ts, value, quark);
            }
        }
    }

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
    private void handleSchedProcessFork(final ITmfStateSystemBuilder ss, final long ts, ITmfEvent event) throws AttributeNotFoundException {
        int quark;
        ITmfStateValue value;
        {
            ITmfEventField content = event.getContent();
            String childProcessName = (String) content.getField(fLayout.fieldChildComm()).getValue();

            Integer parentTid = ((Long) content.getField(fLayout.fieldParentTid()).getValue()).intValue();
            Integer childTid = ((Long) content.getField(fLayout.fieldChildTid()).getValue()).intValue();

            Integer parentTidNode = ss.getQuarkRelativeAndAdd(getNodeThreads(ss), parentTid.toString());
            Integer childTidNode = ss.getQuarkRelativeAndAdd(getNodeThreads(ss), childTid.toString());

            /* Assign the PPID to the new process */
            quark = ss.getQuarkRelativeAndAdd(childTidNode, Attributes.PPID);
            value = TmfStateValue.newValueInt(parentTid);
            ss.modifyAttribute(ts, value, quark);

            /* Set the new process' exec_name */
            quark = ss.getQuarkRelativeAndAdd(childTidNode, Attributes.EXEC_NAME);
            value = TmfStateValue.newValueString(childProcessName);
            ss.modifyAttribute(ts, value, quark);

            /* Set the new process' status */
            quark = ss.getQuarkRelativeAndAdd(childTidNode, Attributes.STATUS);
            value = StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE;
            ss.modifyAttribute(ts, value, quark);

            /* Set the process' syscall name, to be the same as the parent's */
            quark = ss.getQuarkRelativeAndAdd(parentTidNode, Attributes.SYSTEM_CALL);
            value = ss.queryOngoingState(quark);
            if (value.isNull()) {
                /*
                 * Maybe we were missing info about the parent? At least we will
                 * set the child right. Let's suppose "sys_clone".
                 */
                value = TmfStateValue.newValueString(fLayout.eventSyscallEntryPrefix() + IKernelAnalysisEventLayout.INITIAL_SYSCALL_NAME);
            }
            quark = ss.getQuarkRelativeAndAdd(childTidNode, Attributes.SYSTEM_CALL);
            ss.modifyAttribute(ts, value, quark);
        }
    }

    /**
     * Handle scheduler process free
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
    private void handleSchedProcessFree(final ITmfStateSystemBuilder ss, final long ts, ITmfEvent event) throws AttributeNotFoundException {
        int quark;
        {
            Integer tid = ((Long) event.getContent().getField(fLayout.fieldTid()).getValue()).intValue();
            /*
             * Remove the process and all its sub-attributes from the current
             * state
             */
            quark = ss.getQuarkRelativeAndAdd(getNodeThreads(ss), tid.toString());
            ss.removeAttribute(ts, quark);
        }
    }

    /**
     * Handle a scheduler's switch event
     *
     * @param ss
     *            the state system provider
     * @param ts
     *            the current timestamp the current timestamp
     * @param event
     *            the event triggering the state change
     * @param currentCPUNode
     *            the current cpu node in the state system the cpu node
     * @throws AttributeNotFoundException
     *             an exception if the attribute it not found
     */
    private void handleSchedSwitch(final ITmfStateSystemBuilder ss, final long ts, ITmfEvent event, final int currentCPUNode) throws AttributeNotFoundException {
        int quark;
        ITmfStateValue value;
        {
            ITmfEventField content = event.getContent();
            Integer prevTid = ((Long) content.getField(fLayout.fieldPrevTid()).getValue()).intValue();
            Long prevState = (Long) content.getField(fLayout.fieldPrevState()).getValue();
            String nextProcessName = (String) content.getField(fLayout.fieldNextComm()).getValue();
            Integer nextTid = ((Long) content.getField(fLayout.fieldNextTid()).getValue()).intValue();

            Integer formerThreadNode = ss.getQuarkRelativeAndAdd(getNodeThreads(ss), prevTid.toString());
            Integer newCurrentThreadNode = ss.getQuarkRelativeAndAdd(getNodeThreads(ss), nextTid.toString());

            /* Set the status of the process that got scheduled out. */
            quark = ss.getQuarkRelativeAndAdd(formerThreadNode, Attributes.STATUS);
            if (prevState != 0) {
                value = StateValues.PROCESS_STATUS_WAIT_BLOCKED_VALUE;
            } else {
                value = StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE;
            }
            ss.modifyAttribute(ts, value, quark);

            /* Set the status of the new scheduled process */
            setProcessToRunning(ss, ts, newCurrentThreadNode);

            /* Set the exec name of the new process */
            quark = ss.getQuarkRelativeAndAdd(newCurrentThreadNode, Attributes.EXEC_NAME);
            value = TmfStateValue.newValueString(nextProcessName);
            ss.modifyAttribute(ts, value, quark);

            /* Make sure the PPID and system_call sub-attributes exist */
            ss.getQuarkRelativeAndAdd(newCurrentThreadNode, Attributes.SYSTEM_CALL);
            ss.getQuarkRelativeAndAdd(newCurrentThreadNode, Attributes.PPID);

            /* Set the current scheduled process on the relevant CPU */
            quark = ss.getQuarkRelativeAndAdd(currentCPUNode, Attributes.CURRENT_THREAD);
            value = TmfStateValue.newValueInt(nextTid);
            ss.modifyAttribute(ts, value, quark);

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
            quark = ss.getQuarkRelativeAndAdd(currentCPUNode, Attributes.STATUS);
            ss.modifyAttribute(ts, value, quark);
        }
    }

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
    private void handleSoftIrqRaise(final ITmfStateSystemBuilder ss, final long ts, ITmfEvent event) throws AttributeNotFoundException {
        int quark;
        ITmfStateValue value;
        /* Fields: int32 vec */
        {
            Integer softIrqId = ((Long) event.getContent().getField(fLayout.fieldVec()).getValue()).intValue();

            /*
             * Mark this SoftIRQ as *raised* in the resource tree. State value =
             * -2
             */
            quark = ss.getQuarkRelativeAndAdd(getNodeSoftIRQs(ss), softIrqId.toString());
            value = StateValues.SOFT_IRQ_RAISED_VALUE;
            ss.modifyAttribute(ts, value, quark);
        }
    }

    /**
     * Handle a softirq handler entry event TODO: make me per-cpu
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
    private void handleSoftIrqHandlerEntry(final ITmfStateSystemBuilder ss, final long ts, ITmfEvent event, Integer cpu, final int currentCPUNode, final int currentThreadNode) throws AttributeNotFoundException {
        int quark;
        ITmfStateValue value;
        {
            Integer softIrqId = ((Long) event.getContent().getField(fLayout.fieldVec()).getValue()).intValue();

            /*
             * Mark this SoftIRQ as active in the resource tree. The state value
             * = the CPU on which this SoftIRQ is processed
             */
            quark = ss.getQuarkRelativeAndAdd(getNodeSoftIRQs(ss), softIrqId.toString());
            value = TmfStateValue.newValueInt(cpu.intValue());
            ss.modifyAttribute(ts, value, quark);

            /* Change the status of the running process to interrupted */
            quark = ss.getQuarkRelativeAndAdd(currentThreadNode, Attributes.STATUS);
            value = StateValues.PROCESS_STATUS_INTERRUPTED_VALUE;
            ss.modifyAttribute(ts, value, quark);

            /* Change the status of the CPU to interrupted */
            quark = ss.getQuarkRelativeAndAdd(currentCPUNode, Attributes.STATUS);
            value = StateValues.CPU_STATUS_SOFTIRQ_VALUE;
            ss.modifyAttribute(ts, value, quark);
        }
    }

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
    private void handleSoftIrqExit(final ITmfStateSystemBuilder ss, final long ts, ITmfEvent event, final int currentCPUNode, final int currentThreadNode) throws AttributeNotFoundException {
        int quark;
        ITmfStateValue value;
        {
            Integer softIrqId = ((Long) event.getContent().getField(fLayout.fieldVec()).getValue()).intValue();

            /* Put this SoftIRQ back to inactive (= -1) in the resource tree */
            quark = ss.getQuarkRelativeAndAdd(getNodeSoftIRQs(ss), softIrqId.toString());
            value = TmfStateValue.nullValue();
            ss.modifyAttribute(ts, value, quark);

            /* Set the previous process back to running */
            setProcessToRunning(ss, ts, currentThreadNode);

            /* Set the CPU status back to "busy" or "idle" */
            cpuExitInterrupt(ss, ts, currentCPUNode, currentThreadNode);
        }
    }

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
    private void handleIrqHandlerEntry(final ITmfStateSystemBuilder ss, final long ts, ITmfEvent event, Integer cpu, final int currentCPUNode, final int currentThreadNode) throws AttributeNotFoundException {
        int quark;
        ITmfStateValue value;
        {
            Integer irqId = ((Long) event.getContent().getField(fLayout.fieldIrq()).getValue()).intValue();

            /*
             * Mark this IRQ as active in the resource tree. The state value =
             * the CPU on which this IRQ is sitting
             */
            quark = ss.getQuarkRelativeAndAdd(getNodeIRQs(ss), irqId.toString());
            value = TmfStateValue.newValueInt(cpu.intValue());
            ss.modifyAttribute(ts, value, quark);

            /* Change the status of the running process to interrupted */
            quark = ss.getQuarkRelativeAndAdd(currentThreadNode, Attributes.STATUS);
            value = StateValues.PROCESS_STATUS_INTERRUPTED_VALUE;
            ss.modifyAttribute(ts, value, quark);

            /* Change the status of the CPU to interrupted */
            quark = ss.getQuarkRelativeAndAdd(currentCPUNode, Attributes.STATUS);
            value = StateValues.CPU_STATUS_IRQ_VALUE;
            ss.modifyAttribute(ts, value, quark);
        }
    }

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
    private void handleIrqHandlerExit(final ITmfStateSystemBuilder ss, final long ts, ITmfEvent event, final int currentCPUNode, final int currentThreadNode) throws AttributeNotFoundException {
        int quark;
        ITmfStateValue value;
        {
            Integer irqId = ((Long) event.getContent().getField(fLayout.fieldIrq()).getValue()).intValue();

            /* Put this IRQ back to inactive in the resource tree */
            quark = ss.getQuarkRelativeAndAdd(getNodeIRQs(ss), irqId.toString());
            value = TmfStateValue.nullValue();
            ss.modifyAttribute(ts, value, quark);

            /* Set the previous process back to running */
            setProcessToRunning(ss, ts, currentThreadNode);

            /* Set the CPU status back to running or "idle" */
            cpuExitInterrupt(ss, ts, currentCPUNode, currentThreadNode);
        }
    }

    /**
     * Handle an Lttng state dump process state event
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
    private static void handleLttngStatedumpProcessState(final ITmfStateSystemBuilder ss, final long ts, ITmfEvent event) throws AttributeNotFoundException {
        int quark;
        ITmfStateValue value;
        /* LTTng-specific */
        {
            ITmfEventField content = event.getContent();
            int tid = ((Long) content.getField("tid").getValue()).intValue(); //$NON-NLS-1$
            int pid = ((Long) content.getField("pid").getValue()).intValue(); //$NON-NLS-1$
            int ppid = ((Long) content.getField("ppid").getValue()).intValue(); //$NON-NLS-1$
            int status = ((Long) content.getField("status").getValue()).intValue(); //$NON-NLS-1$
            String name = (String) content.getField("name").getValue(); //$NON-NLS-1$
            /*
             * "mode" could be interesting too, but it doesn't seem to be
             * populated with anything relevant for now.
             */

            int curThreadNode = ss.getQuarkRelativeAndAdd(getNodeThreads(ss), String.valueOf(tid));

            /* Set the process' name */
            quark = ss.getQuarkRelativeAndAdd(curThreadNode, Attributes.EXEC_NAME);
            if (ss.queryOngoingState(quark).isNull()) {
                /* If the value didn't exist previously, set it */
                value = TmfStateValue.newValueString(name);
                ss.modifyAttribute(ts, value, quark);
            }

            /* Set the process' PPID */
            quark = ss.getQuarkRelativeAndAdd(curThreadNode, Attributes.PPID);
            if (ss.queryOngoingState(quark).isNull()) {
                if (pid == tid) {
                    /* We have a process. Use the 'PPID' field. */
                    value = TmfStateValue.newValueInt(ppid);
                } else {
                    /* We have a thread, use the 'PID' field for the parent. */
                    value = TmfStateValue.newValueInt(pid);
                }
                ss.modifyAttribute(ts, value, quark);
            }

            /* Set the process' status */
            quark = ss.getQuarkRelativeAndAdd(curThreadNode, Attributes.STATUS);
            if (ss.queryOngoingState(quark).isNull()) {
                /*
                 * "2" here means "WAIT_FOR_CPU", and "5" "WAIT_BLOCKED" in the
                 * LTTng kernel.
                 */
                if (status == 2) {
                    value = StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE;
                } else if (status == 5) {
                    value = StateValues.PROCESS_STATUS_WAIT_BLOCKED_VALUE;
                } else {
                    value = StateValues.PROCESS_STATUS_UNKNOWN_VALUE;
                }
                ss.modifyAttribute(ts, value, quark);
            }
        }
    }
    // ------------------------------------------------------------------------
    // Convenience methods for commonly-used attribute tree locations
    // ------------------------------------------------------------------------

    private static int getNodeCPUs(ITmfStateSystemBuilder ssb) {
        return ssb.getQuarkAbsoluteAndAdd(Attributes.CPUS);
    }

    private static int getNodeThreads(ITmfStateSystemBuilder ssb) {
        return ssb.getQuarkAbsoluteAndAdd(Attributes.THREADS);
    }

    private static int getNodeIRQs(ITmfStateSystemBuilder ssb) {
        return ssb.getQuarkAbsoluteAndAdd(Attributes.RESOURCES, Attributes.IRQS);
    }

    private static int getNodeSoftIRQs(ITmfStateSystemBuilder ssb) {
        return ssb.getQuarkAbsoluteAndAdd(Attributes.RESOURCES, Attributes.SOFT_IRQS);
    }

    // ------------------------------------------------------------------------
    // Advanced state-setting methods
    // ------------------------------------------------------------------------

    /**
     * When we want to set a process back to a "running" state, first check its
     * current System_call attribute. If there is a system call active, we put
     * the process back in the syscall state. If not, we put it back in user
     * mode state.
     */
    private static void setProcessToRunning(ITmfStateSystemBuilder ssb, long ts, int currentThreadNode)
            throws AttributeNotFoundException, TimeRangeException,
            StateValueTypeException {
        int quark;
        ITmfStateValue value;

        quark = ssb.getQuarkRelativeAndAdd(currentThreadNode, Attributes.SYSTEM_CALL);
        if (ssb.queryOngoingState(quark).isNull()) {
            /* We were in user mode before the interruption */
            value = StateValues.PROCESS_STATUS_RUN_USERMODE_VALUE;
        } else {
            /* We were previously in kernel mode */
            value = StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE;
        }
        quark = ssb.getQuarkRelativeAndAdd(currentThreadNode, Attributes.STATUS);
        ssb.modifyAttribute(ts, value, quark);
    }

    /**
     * Similar logic as above, but to set the CPU's status when it's coming out
     * of an interruption.
     */
    private static void cpuExitInterrupt(ITmfStateSystemBuilder ssb, long ts,
            int currentCpuNode, int currentThreadNode)
            throws StateValueTypeException, AttributeNotFoundException,
            TimeRangeException {
        int quark;
        ITmfStateValue value;

        quark = ssb.getQuarkRelativeAndAdd(currentCpuNode, Attributes.CURRENT_THREAD);
        if (ssb.queryOngoingState(quark).unboxInt() > 0) {
            /* There was a process on the CPU */
            quark = ssb.getQuarkRelative(currentThreadNode, Attributes.SYSTEM_CALL);
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
        quark = ssb.getQuarkRelativeAndAdd(currentCpuNode, Attributes.STATUS);
        ssb.modifyAttribute(ts, value, quark);
    }
}
