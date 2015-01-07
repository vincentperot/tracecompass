/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.analysis.kernel;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.Attributes;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.StateValues;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout.IKernelAnalysisEventLayout;
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
public class LttngKernelStateProvider extends AbstractTmfStateProvider {

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
    private final @NonNull IKernelAnalysisEventLayout fLayout;

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
    public LttngKernelStateProvider(ITmfTrace trace, @NonNull IKernelAnalysisEventLayout layout) {
        super(trace, ITmfEvent.class, "Kernel"); //$NON-NLS-1$
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

        if (layout.eventStatedumpProcessState() != null) {
            builder.put(layout.eventStatedumpProcessState(), STATEDUMP_PROCESS_STATE_INDEX);
        }

        for (String eventSchedWakeup : layout.eventsSchedWakeup()) {
            builder.put(eventSchedWakeup, SCHED_WAKEUP_INDEX);
        }

        return builder.build();
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
    public LttngKernelStateProvider getNewInstance() {
        return new LttngKernelStateProvider(this.getTrace(), fLayout);
    }

    @Override
    protected void eventHandle(ITmfEvent event) {
        Integer cpu = null;
        Iterable<TmfCpuAspect> aspects = TmfTraceUtils.getEventAspectsOfClass(event.getTrace(), TmfCpuAspect.class);
        for (TmfCpuAspect aspect : aspects) {
            if (!aspect.resolve(event).equals(TmfCpuAspect.CPU_UNAVAILABLE)) {
                cpu = aspect.resolve(event);
                break;
            }
        }
        if (cpu == null) {
            /* We couldn't find any CPU information, ignore this event */
            return;
        }

        final String eventName = event.getType().getName();
        final long ts = event.getTimestamp().getValue();

        try {
            /* Shortcut for the "current CPU" attribute node */
            final int currentCPUNode = getStateSystemBuilder().getQuarkRelativeAndAdd(getNodeCPUs(), cpu.toString());

            /*
             * Shortcut for the "current thread" attribute node. It requires
             * querying the current CPU's current thread.
             */
            int quark = getStateSystemBuilder().getQuarkRelativeAndAdd(currentCPUNode, Attributes.CURRENT_THREAD);
            ITmfStateValue value = getStateSystemBuilder().queryOngoingState(quark);
            int thread = value.isNull() ? -1 : value.unboxInt();
            final int currentThreadNode = getStateSystemBuilder().getQuarkRelativeAndAdd(getNodeThreads(), String.valueOf(thread));

            /*
             * Feed event to the history system if it's known to cause a state
             * transition.
             */
            Integer idx = fEventNames.get(eventName);
            int intval = (idx == null ? -1 : idx.intValue());
            switch (intval) {

            case IRQ_HANDLER_ENTRY_INDEX:
            {
                Integer irqId = ((Long) event.getContent().getField(fLayout.fieldIrq()).getValue()).intValue();

                /* Mark this IRQ as active in the resource tree.
                 * The state value = the CPU on which this IRQ is sitting */
                quark = getStateSystemBuilder().getQuarkRelativeAndAdd(getNodeIRQs(), irqId.toString());
                value = TmfStateValue.newValueInt(cpu.intValue());
                getStateSystemBuilder().modifyAttribute(ts, value, quark);

                /* Change the status of the running process to interrupted */
                quark = getStateSystemBuilder().getQuarkRelativeAndAdd(currentThreadNode, Attributes.STATUS);
                value = StateValues.PROCESS_STATUS_INTERRUPTED_VALUE;
                getStateSystemBuilder().modifyAttribute(ts, value, quark);

                /* Change the status of the CPU to interrupted */
                quark = getStateSystemBuilder().getQuarkRelativeAndAdd(currentCPUNode, Attributes.STATUS);
                value = StateValues.CPU_STATUS_IRQ_VALUE;
                getStateSystemBuilder().modifyAttribute(ts, value, quark);
            }
                break;

            case IRQ_HANDLER_EXIT_INDEX:
            {
                Integer irqId = ((Long) event.getContent().getField(fLayout.fieldIrq()).getValue()).intValue();

                /* Put this IRQ back to inactive in the resource tree */
                quark = getStateSystemBuilder().getQuarkRelativeAndAdd(getNodeIRQs(), irqId.toString());
                value = TmfStateValue.nullValue();
                getStateSystemBuilder().modifyAttribute(ts, value, quark);

                /* Set the previous process back to running */
                setProcessToRunning(ts, currentThreadNode);

                /* Set the CPU status back to running or "idle" */
                cpuExitInterrupt(ts, currentCPUNode, currentThreadNode);
            }
                break;

            case SOFT_IRQ_ENTRY_INDEX:
            {
                Integer softIrqId = ((Long) event.getContent().getField(fLayout.fieldVec()).getValue()).intValue();

                /* Mark this SoftIRQ as active in the resource tree.
                 * The state value = the CPU on which this SoftIRQ is processed */
                quark = getStateSystemBuilder().getQuarkRelativeAndAdd(getNodeSoftIRQs(), softIrqId.toString());
                value = TmfStateValue.newValueInt(cpu.intValue());
                getStateSystemBuilder().modifyAttribute(ts, value, quark);

                /* Change the status of the running process to interrupted */
                quark = getStateSystemBuilder().getQuarkRelativeAndAdd(currentThreadNode, Attributes.STATUS);
                value = StateValues.PROCESS_STATUS_INTERRUPTED_VALUE;
                getStateSystemBuilder().modifyAttribute(ts, value, quark);

                /* Change the status of the CPU to interrupted */
                quark = getStateSystemBuilder().getQuarkRelativeAndAdd(currentCPUNode, Attributes.STATUS);
                value = StateValues.CPU_STATUS_SOFTIRQ_VALUE;
                getStateSystemBuilder().modifyAttribute(ts, value, quark);
            }
                break;

            case SOFT_IRQ_EXIT_INDEX:
            {
                Integer softIrqId = ((Long) event.getContent().getField(fLayout.fieldVec()).getValue()).intValue();

                /* Put this SoftIRQ back to inactive (= -1) in the resource tree */
                quark = getStateSystemBuilder().getQuarkRelativeAndAdd(getNodeSoftIRQs(), softIrqId.toString());
                value = TmfStateValue.nullValue();
                getStateSystemBuilder().modifyAttribute(ts, value, quark);

                /* Set the previous process back to running */
                setProcessToRunning(ts, currentThreadNode);

                /* Set the CPU status back to "busy" or "idle" */
                cpuExitInterrupt(ts, currentCPUNode, currentThreadNode);
            }
                break;

            case SOFT_IRQ_RAISE_INDEX:
            /* Fields: int32 vec */
            {
                Integer softIrqId = ((Long) event.getContent().getField(fLayout.fieldVec()).getValue()).intValue();

                /* Mark this SoftIRQ as *raised* in the resource tree.
                 * State value = -2 */
                quark = getStateSystemBuilder().getQuarkRelativeAndAdd(getNodeSoftIRQs(), softIrqId.toString());
                value = StateValues.SOFT_IRQ_RAISED_VALUE;
                getStateSystemBuilder().modifyAttribute(ts, value, quark);
            }
                break;

            case SCHED_SWITCH_INDEX:
            {
                ITmfEventField content = event.getContent();
                Integer prevTid = ((Long) content.getField(fLayout.fieldPrevTid()).getValue()).intValue();
                Long prevState = (Long) content.getField(fLayout.fieldPrevState()).getValue();
                String nextProcessName = (String) content.getField(fLayout.fieldNextComm()).getValue();
                Integer nextTid = ((Long) content.getField(fLayout.fieldNextTid()).getValue()).intValue();

                Integer formerThreadNode = getStateSystemBuilder().getQuarkRelativeAndAdd(getNodeThreads(), prevTid.toString());
                Integer newCurrentThreadNode = getStateSystemBuilder().getQuarkRelativeAndAdd(getNodeThreads(), nextTid.toString());

                /* Set the status of the process that got scheduled out. */
                quark = getStateSystemBuilder().getQuarkRelativeAndAdd(formerThreadNode, Attributes.STATUS);
                if (prevState != 0) {
                    value = StateValues.PROCESS_STATUS_WAIT_BLOCKED_VALUE;
                } else {
                    value = StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE;
                }
                getStateSystemBuilder().modifyAttribute(ts, value, quark);

                /* Set the status of the new scheduled process */
                setProcessToRunning(ts, newCurrentThreadNode);

                /* Set the exec name of the new process */
                quark = getStateSystemBuilder().getQuarkRelativeAndAdd(newCurrentThreadNode, Attributes.EXEC_NAME);
                value = TmfStateValue.newValueString(nextProcessName);
                getStateSystemBuilder().modifyAttribute(ts, value, quark);

                /* Make sure the PPID and system_call sub-attributes exist */
                getStateSystemBuilder().getQuarkRelativeAndAdd(newCurrentThreadNode, Attributes.SYSTEM_CALL);
                getStateSystemBuilder().getQuarkRelativeAndAdd(newCurrentThreadNode, Attributes.PPID);

                /* Set the current scheduled process on the relevant CPU */
                quark = getStateSystemBuilder().getQuarkRelativeAndAdd(currentCPUNode, Attributes.CURRENT_THREAD);
                value = TmfStateValue.newValueInt(nextTid);
                getStateSystemBuilder().modifyAttribute(ts, value, quark);

                /* Set the status of the CPU itself */
                if (nextTid > 0) {
                    /* Check if the entering process is in kernel or user mode */
                    quark = getStateSystemBuilder().getQuarkRelativeAndAdd(newCurrentThreadNode, Attributes.SYSTEM_CALL);
                    if (getStateSystemBuilder().queryOngoingState(quark).isNull()) {
                        value = StateValues.CPU_STATUS_RUN_USERMODE_VALUE;
                    } else {
                        value = StateValues.CPU_STATUS_RUN_SYSCALL_VALUE;
                    }
                } else {
                    value = StateValues.CPU_STATUS_IDLE_VALUE;
                }
                quark = getStateSystemBuilder().getQuarkRelativeAndAdd(currentCPUNode, Attributes.STATUS);
                getStateSystemBuilder().modifyAttribute(ts, value, quark);
            }
                break;

            case SCHED_PROCESS_FORK_INDEX:
            {
                ITmfEventField content = event.getContent();
                // String parentProcessName = (String) event.getFieldValue("parent_comm");
                String childProcessName = (String) content.getField(fLayout.fieldChildComm()).getValue();
                // assert ( parentProcessName.equals(childProcessName) );

                Integer parentTid = ((Long) content.getField(fLayout.fieldParentTid()).getValue()).intValue();
                Integer childTid = ((Long) content.getField(fLayout.fieldChildTid()).getValue()).intValue();

                Integer parentTidNode = getStateSystemBuilder().getQuarkRelativeAndAdd(getNodeThreads(), parentTid.toString());
                Integer childTidNode = getStateSystemBuilder().getQuarkRelativeAndAdd(getNodeThreads(), childTid.toString());

                /* Assign the PPID to the new process */
                quark = getStateSystemBuilder().getQuarkRelativeAndAdd(childTidNode, Attributes.PPID);
                value = TmfStateValue.newValueInt(parentTid);
                getStateSystemBuilder().modifyAttribute(ts, value, quark);

                /* Set the new process' exec_name */
                quark = getStateSystemBuilder().getQuarkRelativeAndAdd(childTidNode, Attributes.EXEC_NAME);
                value = TmfStateValue.newValueString(childProcessName);
                getStateSystemBuilder().modifyAttribute(ts, value, quark);

                /* Set the new process' status */
                quark = getStateSystemBuilder().getQuarkRelativeAndAdd(childTidNode, Attributes.STATUS);
                value = StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE;
                getStateSystemBuilder().modifyAttribute(ts, value, quark);

                /* Set the process' syscall name, to be the same as the parent's */
                quark = getStateSystemBuilder().getQuarkRelativeAndAdd(parentTidNode, Attributes.SYSTEM_CALL);
                value = getStateSystemBuilder().queryOngoingState(quark);
                if (value.isNull()) {
                    /*
                     * Maybe we were missing info about the parent? At least we
                     * will set the child right. Let's suppose "sys_clone".
                     */
                    value = TmfStateValue.newValueString(fLayout.eventSyscallEntryPrefix() + IKernelAnalysisEventLayout.INITIAL_SYSCALL_NAME);
                }
                quark = getStateSystemBuilder().getQuarkRelativeAndAdd(childTidNode, Attributes.SYSTEM_CALL);
                getStateSystemBuilder().modifyAttribute(ts, value, quark);
            }
                break;

            case SCHED_PROCESS_EXIT_INDEX:
                break;

            case SCHED_PROCESS_FREE_INDEX:
            {
                Integer tid = ((Long) event.getContent().getField(fLayout.fieldTid()).getValue()).intValue();
                /*
                 * Remove the process and all its sub-attributes from the
                 * current state
                 */
                quark = getStateSystemBuilder().getQuarkRelativeAndAdd(getNodeThreads(), tid.toString());
                getStateSystemBuilder().removeAttribute(ts, quark);
            }
                break;

            case STATEDUMP_PROCESS_STATE_INDEX:
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

                int curThreadNode = getStateSystemBuilder().getQuarkRelativeAndAdd(getNodeThreads(), String.valueOf(tid));

                /* Set the process' name */
                quark = getStateSystemBuilder().getQuarkRelativeAndAdd(curThreadNode, Attributes.EXEC_NAME);
                if (getStateSystemBuilder().queryOngoingState(quark).isNull()) {
                    /* If the value didn't exist previously, set it */
                    value = TmfStateValue.newValueString(name);
                    getStateSystemBuilder().modifyAttribute(ts, value, quark);
                }

                /* Set the process' PPID */
                quark = getStateSystemBuilder().getQuarkRelativeAndAdd(curThreadNode, Attributes.PPID);
                if (getStateSystemBuilder().queryOngoingState(quark).isNull()) {
                    if (pid == tid) {
                        /* We have a process. Use the 'PPID' field. */
                        value = TmfStateValue.newValueInt(ppid);
                    } else {
                        /* We have a thread, use the 'PID' field for the parent. */
                        value = TmfStateValue.newValueInt(pid);
                    }
                    getStateSystemBuilder().modifyAttribute(ts, value, quark);
                }

                /* Set the process' status */
                quark = getStateSystemBuilder().getQuarkRelativeAndAdd(curThreadNode, Attributes.STATUS);
                if (getStateSystemBuilder().queryOngoingState(quark).isNull()) {
                     /* "2" here means "WAIT_FOR_CPU", and "5" "WAIT_BLOCKED" in the LTTng kernel. */
                    if (status == 2) {
                        value = StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE;
                    } else if (status == 5) {
                        value = StateValues.PROCESS_STATUS_WAIT_BLOCKED_VALUE;
                    } else {
                        value = StateValues.PROCESS_STATUS_UNKNOWN_VALUE;
                    }
                    getStateSystemBuilder().modifyAttribute(ts, value, quark);
                }
            }
                break;

            case SCHED_WAKEUP_INDEX:
            {
                final int tid = ((Long) event.getContent().getField(fLayout.fieldTid()).getValue()).intValue();
                final int threadNode = getStateSystemBuilder().getQuarkRelativeAndAdd(getNodeThreads(), String.valueOf(tid));

                /*
                 * The process indicated in the event's payload is now ready to
                 * run. Assign it to the "wait for cpu" state, but only if it
                 * was not already running.
                 */
                quark = getStateSystemBuilder().getQuarkRelativeAndAdd(threadNode, Attributes.STATUS);
                int status = getStateSystemBuilder().queryOngoingState(quark).unboxInt();

                if (status != StateValues.PROCESS_STATUS_RUN_SYSCALL &&
                    status != StateValues.PROCESS_STATUS_RUN_USERMODE) {
                    value = StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE;
                    getStateSystemBuilder().modifyAttribute(ts, value, quark);
                }
            }
                break;

            default:
            /* Other event types not covered by the main switch */
            {
                if (eventName.startsWith(fLayout.eventSyscallEntryPrefix())
                        || eventName.startsWith(fLayout.eventCompatSyscallEntryPrefix())) {

                    /* Assign the new system call to the process */
                    quark = getStateSystemBuilder().getQuarkRelativeAndAdd(currentThreadNode, Attributes.SYSTEM_CALL);
                    value = TmfStateValue.newValueString(eventName);
                    getStateSystemBuilder().modifyAttribute(ts, value, quark);

                    /* Put the process in system call mode */
                    quark = getStateSystemBuilder().getQuarkRelativeAndAdd(currentThreadNode, Attributes.STATUS);
                    value = StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE;
                    getStateSystemBuilder().modifyAttribute(ts, value, quark);

                    /* Put the CPU in system call (kernel) mode */
                    quark = getStateSystemBuilder().getQuarkRelativeAndAdd(currentCPUNode, Attributes.STATUS);
                    value = StateValues.CPU_STATUS_RUN_SYSCALL_VALUE;
                    getStateSystemBuilder().modifyAttribute(ts, value, quark);

                } else if (eventName.startsWith(fLayout.eventSyscallExitPrefix())) {

                    /* Clear the current system call on the process */
                    quark = getStateSystemBuilder().getQuarkRelativeAndAdd(currentThreadNode, Attributes.SYSTEM_CALL);
                    value = TmfStateValue.nullValue();
                    getStateSystemBuilder().modifyAttribute(ts, value, quark);

                    /* Put the process' status back to user mode */
                    quark = getStateSystemBuilder().getQuarkRelativeAndAdd(currentThreadNode, Attributes.STATUS);
                    value = StateValues.PROCESS_STATUS_RUN_USERMODE_VALUE;
                    getStateSystemBuilder().modifyAttribute(ts, value, quark);

                    /* Put the CPU's status back to user mode */
                    quark = getStateSystemBuilder().getQuarkRelativeAndAdd(currentCPUNode, Attributes.STATUS);
                    value = StateValues.CPU_STATUS_RUN_USERMODE_VALUE;
                    getStateSystemBuilder().modifyAttribute(ts, value, quark);
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
    // Convenience methods for commonly-used attribute tree locations
    // ------------------------------------------------------------------------

    private int getNodeCPUs() {
        return getStateSystemBuilder().getQuarkAbsoluteAndAdd(Attributes.CPUS);
    }

    private int getNodeThreads() {
        return getStateSystemBuilder().getQuarkAbsoluteAndAdd(Attributes.THREADS);
    }

    private int getNodeIRQs() {
        return getStateSystemBuilder().getQuarkAbsoluteAndAdd(Attributes.RESOURCES, Attributes.IRQS);
    }

    private int getNodeSoftIRQs() {
        return getStateSystemBuilder().getQuarkAbsoluteAndAdd(Attributes.RESOURCES, Attributes.SOFT_IRQS);
    }

    // ------------------------------------------------------------------------
    // Advanced state-setting methods
    // ------------------------------------------------------------------------

    /**
     * When we want to set a process back to a "running" state, first check
     * its current System_call attribute. If there is a system call active, we
     * put the process back in the syscall state. If not, we put it back in
     * user mode state.
     */
    private void setProcessToRunning(long ts, int currentThreadNode)
            throws AttributeNotFoundException, TimeRangeException,
            StateValueTypeException {
        int quark;
        ITmfStateValue value;

        quark = getStateSystemBuilder().getQuarkRelativeAndAdd(currentThreadNode, Attributes.SYSTEM_CALL);
        if (getStateSystemBuilder().queryOngoingState(quark).isNull()) {
            /* We were in user mode before the interruption */
            value = StateValues.PROCESS_STATUS_RUN_USERMODE_VALUE;
        } else {
            /* We were previously in kernel mode */
            value = StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE;
        }
        quark = getStateSystemBuilder().getQuarkRelativeAndAdd(currentThreadNode, Attributes.STATUS);
        getStateSystemBuilder().modifyAttribute(ts, value, quark);
    }

    /**
     * Similar logic as above, but to set the CPU's status when it's coming out
     * of an interruption.
     */
    private void cpuExitInterrupt(long ts, int currentCpuNode, int currentThreadNode)
            throws StateValueTypeException, AttributeNotFoundException,
            TimeRangeException {
        int quark;
        ITmfStateValue value;

        quark = getStateSystemBuilder().getQuarkRelativeAndAdd(currentCpuNode, Attributes.CURRENT_THREAD);
        if (getStateSystemBuilder().queryOngoingState(quark).unboxInt() > 0) {
            /* There was a process on the CPU */
            quark = getStateSystemBuilder().getQuarkRelative(currentThreadNode, Attributes.SYSTEM_CALL);
            if (getStateSystemBuilder().queryOngoingState(quark).isNull()) {
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
        quark = getStateSystemBuilder().getQuarkRelativeAndAdd(currentCpuNode, Attributes.STATUS);
        getStateSystemBuilder().modifyAttribute(ts, value, quark);
    }
}
