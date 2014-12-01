/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   François Rajotte - Initial API and implementation
 *   Geneviève Bastien - Revision of the initial implementation
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.analysis.cpuusage;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.Activator;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.Attributes;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * Creates a state system with the total time spent on CPU for each thread and
 * for each CPU from a kernel trace.
 *
 * This state system in itself keeps the total time on CPU since last time the
 * process was scheduled out. The state system queries will only be accurate
 * when the process is not in a running state. To have exact CPU usage when
 * running, this state system needs to be used along the LTTng Kernel analysis.
 *
 * It requires only the 'sched_switch' events enabled on the trace.
 *
 * @author François Rajotte
 * @since 3.0
 */
public class LttngKernelCpuUsageStateProvider extends AbstractTmfStateProvider {

    private static final int VERSION = 2;

    /* For each CPU, maps the last time a thread was scheduled in */
    private final Map<Integer, Long> fLastStartTimes = new HashMap<>();
    private final long fTraceStart;
    private final @NonNull IKernelAnalysisEventLayout fLayout;

    /**
     * Constructor
     *
     * @param trace
     *            The trace from which to get the CPU usage
     * @param layout
     *            The event layout to use for this state provider.
     */
    public LttngKernelCpuUsageStateProvider(ITmfTrace trace, @NonNull IKernelAnalysisEventLayout layout) {
        super(trace, ITmfEvent.class, "LTTng Kernel CPU usage"); //$NON-NLS-1$
        fTraceStart = trace.getStartTime().getValue();
        fLayout = layout;
    }

    // ------------------------------------------------------------------------
    // ITmfStateProvider
    // ------------------------------------------------------------------------

    @Override
    public int getVersion() {
        return VERSION;
    }

    @Override
    public LttngKernelCpuUsageStateProvider getNewInstance() {
        return new LttngKernelCpuUsageStateProvider(this.getTrace(), this.fLayout);
    }

    @Override
    protected void eventHandle(ITmfEvent event) {
        final String eventName = event.getType().getName();

        if (eventName.equals(fLayout.eventSchedSwitch())) {
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

            /*
             * Fields: string prev_comm, int32 prev_tid, int32 prev_prio, int64
             * prev_state, string next_comm, int32 next_tid, int32 next_prio
             */
            ITmfEventField content = event.getContent();
            long ts = event.getTimestamp().getValue();

            Long prevTid = (Long) content.getField(fLayout.fieldPrevTid()).getValue();

            try {
                Integer currentCPUNode = ss.getQuarkRelativeAndAdd(getNodeCPUs(), cpu.toString());

                /*
                 * This quark contains the value of the cumulative time spent on
                 * the source CPU by the currently running thread
                 */
                Integer cumulativeTimeQuark = ss.getQuarkRelativeAndAdd(currentCPUNode, prevTid.toString());
                Long startTime = fLastStartTimes.get(cpu);
                /*
                 * If start time is null, we haven't seen the start of the
                 * process, so we assume beginning of the trace
                 */
                if (startTime == null) {
                    startTime = fTraceStart;
                }

                /*
                 * We add the time from startTime until now to the cumulative
                 * time of the thread
                 */
                if (startTime != null) {
                    ITmfStateValue value = ss.queryOngoingState(cumulativeTimeQuark);

                    /*
                     * Modify cumulative time for this CPU/TID combo: The total
                     * time changes when the process is scheduled out. Nothing
                     * happens when the process is scheduled in.
                     */
                    long prevCumulativeTime = Math.max(0, value.unboxLong());
                    long newCumulativeTime = prevCumulativeTime + (ts - startTime);

                    value = TmfStateValue.newValueLong(newCumulativeTime);
                    ss.modifyAttribute(ts, value, cumulativeTimeQuark);
                    fLastStartTimes.put(cpu, ts);
                }
            } catch (AttributeNotFoundException e) {
                Activator.getDefault().logError("Attribute not found in LttngKernelCpuStateProvider", e); //$NON-NLS-1$
            }

        }
    }

    /* Shortcut for the "current CPU" attribute node */
    private int getNodeCPUs() {
        return ss.getQuarkAbsoluteAndAdd(Attributes.CPUS);
    }

}
