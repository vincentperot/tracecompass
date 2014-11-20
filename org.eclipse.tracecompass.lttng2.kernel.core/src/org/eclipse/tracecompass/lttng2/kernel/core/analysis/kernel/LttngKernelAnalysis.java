/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *   Mathieu Rail - Provide the requirements of the analysis
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.analysis.kernel;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.Attributes;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout.LttngEventLayout;
import org.eclipse.tracecompass.lttng2.control.core.session.SessionConfigStrings;
import org.eclipse.tracecompass.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAnalysisRequirement;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAnalysisRequirement.ValuePriorityLevel;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.ImmutableSet;

/**
 * State System Module for lttng kernel traces
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class LttngKernelAnalysis extends TmfStateSystemAnalysisModule {

    /**
     * The file name of the History Tree
     */
    public static final @NonNull String HISTORY_TREE_FILE_NAME = "stateHistory.ht"; //$NON-NLS-1$

    /** The ID of this analysis module */
    public static final @NonNull String ID = "org.eclipse.linuxtools.lttng2.kernel.analysis"; //$NON-NLS-1$

    /** An impossible value for a Thread ID, used when a thread is not found */
    public static final @NonNull Integer NO_TID = -1;

    /*
     * TODO: Decide which events should be mandatory for the analysis, once the
     * appropriate error messages and session setup are in place.
     */
    private static final ImmutableSet<String> REQUIRED_EVENTS = ImmutableSet.of();

    private static final ImmutableSet<String> OPTIONAL_EVENTS = ImmutableSet.of(
            // FIXME These cannot be declared statically anymore, they depend on
            // the OriginTracer of the kernel trace.
//            LttngStrings.EXIT_SYSCALL,
//            LttngStrings.IRQ_HANDLER_ENTRY,
//            LttngStrings.IRQ_HANDLER_EXIT,
//            LttngStrings.SOFTIRQ_ENTRY,
//            LttngStrings.SOFTIRQ_EXIT,
//            LttngStrings.SOFTIRQ_RAISE,
//            LttngStrings.SCHED_PROCESS_FORK,
//            LttngStrings.SCHED_PROCESS_EXIT,
//            LttngStrings.SCHED_PROCESS_FREE,
//            LttngStrings.SCHED_SWITCH,
//            LttngStrings.STATEDUMP_PROCESS_STATE,
//            LttngStrings.SCHED_WAKEUP,
//            LttngStrings.SCHED_WAKEUP_NEW,
//
//            /* FIXME Add the prefix for syscalls */
//            LttngStrings.SYSCALL_PREFIX
            );

    /** The requirements as an immutable set */
    private static final @NonNull ImmutableSet<TmfAnalysisRequirement> REQUIREMENTS;

    static {
        /* initialize the requirement: domain and events */
        TmfAnalysisRequirement domainReq = new TmfAnalysisRequirement(SessionConfigStrings.CONFIG_ELEMENT_DOMAIN);
        domainReq.addValue(SessionConfigStrings.CONFIG_DOMAIN_TYPE_KERNEL, ValuePriorityLevel.MANDATORY);

        TmfAnalysisRequirement eventReq = new TmfAnalysisRequirement(SessionConfigStrings.CONFIG_ELEMENT_EVENT, REQUIRED_EVENTS, ValuePriorityLevel.MANDATORY);
        eventReq.addValues(OPTIONAL_EVENTS, ValuePriorityLevel.OPTIONAL);


        @SuppressWarnings("null")
        @NonNull ImmutableSet<TmfAnalysisRequirement> reqSet = ImmutableSet.of(domainReq, eventReq);
        REQUIREMENTS = reqSet;
    }

    @Override
    protected @NonNull ITmfStateProvider createStateProvider() {
        ITmfTrace trace = getTrace();
        IKernelAnalysisEventLayout layout;

        if (trace instanceof LttngKernelTrace) {
            layout = ((LttngKernelTrace) trace).getEventLayout();
        } else {
            /* Fall-back to the base LttngEventLayout */
            layout = LttngEventLayout.getInstance();
        }

        return new LttngKernelStateProvider(trace, layout);
    }

    @Override
    @NonNull
    protected String getSsFileName() {
        return HISTORY_TREE_FILE_NAME;
    }

    @Override
    protected String getFullHelpText() {
        @SuppressWarnings("null")
        @NonNull String helpText = Messages.LttngKernelAnalysisModule_Help;
        return helpText;
    }

    @Override
    public Iterable<TmfAnalysisRequirement> getAnalysisRequirements() {
        return REQUIREMENTS;
    }

    // ------------------------------------------------------------------------
    // Analysis-specific methods
    // ------------------------------------------------------------------------

    /**
     * Get the ID of the thread running on the CPU at time ts
     *
     * @param cpuId
     *            The CPU number the process is running on
     * @param ts
     *            The timestamp at which we want the running process
     * @return The TID of the thread running on CPU cpuId at time ts or
     *         {@link #NO_TID} if either no thread is running or we do not know.
     */
    public int getThreadOnCpu(long cpuId, long ts) {
        ITmfStateSystem ss = getStateSystem();
        if (ss == null) {
            return NO_TID;
        }
        try {
            int cpuQuark = ss.getQuarkAbsolute(Attributes.CPUS, Long.toString(cpuId), Attributes.CURRENT_THREAD);
            ITmfStateInterval interval = ss.querySingleState(ts, cpuQuark);
            ITmfStateValue val = interval.getStateValue();
            switch (val.getType()) {
            case INTEGER:
                return val.unboxInt();
            case LONG:
            case DOUBLE:
            case NULL:
            case STRING:
            default:
                break;
            }
        } catch (AttributeNotFoundException | StateSystemDisposedException | TimeRangeException e) {
        }
        return NO_TID;
    }

    /**
     * Get the quarks corresponding to the threads
     *
     * @return The list of quarks corresponding to the threads
     */
    public @NonNull Collection<Integer> getThreadQuarks() {
        ITmfStateSystem ss = getStateSystem();
        if (ss == null) {
            @SuppressWarnings("null")
            @NonNull
            Collection<Integer> empty = Collections.EMPTY_SET;
            return empty;
        }
        int threadQuark;
        try {
            threadQuark = ss.getQuarkAbsolute(Attributes.THREADS);
            return ss.getSubAttributes(threadQuark, false);
        } catch (AttributeNotFoundException e) {
        }
        @SuppressWarnings("null")
        @NonNull
        Collection<Integer> empty = Collections.EMPTY_SET;
        return empty;
    }

    /**
     * Get the parent process ID of a thread
     *
     * @param threadId
     *            The thread ID of the process for which to get the parent
     * @param ts
     *            The timestamp at which to get the parent
     * @return The parent PID or {@link #NO_TID} if the PPID is not found.
     */
    public @NonNull Integer getPpid(@NonNull Integer threadId, long ts) {
        Integer ppid = NO_TID;
        ITmfStateSystem ss = getStateSystem();
        if (ss == null) {
            return ppid;
        }
        Integer ppidNode;
        try {
            ppidNode = ss.getQuarkAbsolute(Attributes.THREADS, threadId.toString(), Attributes.PPID);
            ITmfStateInterval ppidInterval = ss.querySingleState(ts, ppidNode);
            ITmfStateValue ppidValue = ppidInterval.getStateValue();

            switch (ppidValue.getType()) {
            case INTEGER:
                @SuppressWarnings("null")
                @NonNull Integer value = Integer.valueOf(ppidValue.unboxInt());
                ppid = value;
                break;
            case DOUBLE:
            case LONG:
            case NULL:
            case STRING:
            default:
                break;
            }
        } catch (AttributeNotFoundException | StateSystemDisposedException | TimeRangeException e) {
        }
        return ppid;
    }

    /**
     * Get the status intervals for a given thread with a resolution
     *
     * @param threadQuark
     *            The quark of the thread to get the intervals for
     * @param start
     *            The start time of the requested range
     * @param end
     *            The end time of the requested range
     * @param resolution
     *            The resolution or the minimal time between the requested
     *            intervals. If interval times are smaller than resolution, only
     *            the first interval is returned, the others are ignored.
     * @param monitor
     *            A progress monitor for this task
     * @return The list of status intervals for this thread, an empty list is
     *         returned if either the state system is {@code null} or the quark
     *         is not found
     */
    public @NonNull List<ITmfStateInterval> getStatusIntervalsForThread(int threadQuark, long start, long end, long resolution, IProgressMonitor monitor) {
        ITmfStateSystem ss = getStateSystem();
        if (ss == null) {
            @SuppressWarnings("null")
            @NonNull
            List<ITmfStateInterval> empty = Collections.EMPTY_LIST;
            return empty;
        }

        try {
            int statusQuark = ss.getQuarkRelative(threadQuark, Attributes.STATUS);
            List<ITmfStateInterval> statusIntervals = StateSystemUtils.queryHistoryRange(ss, statusQuark, Math.max(start, ss.getStartTime()), Math.min(end - 1, ss.getCurrentEndTime()), resolution, monitor);
            return statusIntervals;
        } catch (AttributeNotFoundException | StateSystemDisposedException | TimeRangeException e) {
        }
        @SuppressWarnings("null")
        @NonNull
        List<ITmfStateInterval> empty = Collections.EMPTY_LIST;
        return empty;
    }

}
