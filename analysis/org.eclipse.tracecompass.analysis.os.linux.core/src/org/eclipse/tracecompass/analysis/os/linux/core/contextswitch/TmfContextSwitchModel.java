/*******************************************************************************
 * Copyright (c) 2015 Ericsson Canada
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexis Cabana-Loriaux - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.contextswitch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.Attributes;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.KernelAnalysisModule;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * @author Alexis Cabana-Loriaux
 * @since 1.0
 *
 */
public class TmfContextSwitchModel {

    // ----------------------------------------------------------
    // Fields
    // ----------------------------------------------------------

    /** The name this model */
    private String fName;

    /** The trace represented by this model */
    private ITmfTrace fTrace;

    /**
     * Thread-safe variable used to indicate if the model is in construction or
     * not
     */
    private AtomicBoolean fIsModelComplete = new AtomicBoolean(false);

    /**
     * The containers used to pair the data between Kernel traces, context
     * switches and the CPUs (see {@link KernelContextSwitchContainer}). Each
     * kernel trace has a container as we can't merge the data together.
     */
    private Map<ITmfTrace, KernelContextSwitchContainer> fDataHolder = new HashMap<>();

    /**
     * The granularity of the queries. The higher this parameter is, the slower
     * the queries will be.
     */
    private static Integer fResolution;

    /**
     * Container of all tĥe jobs in progress.
     */
    private List<Job> fUpdateJobs = new ArrayList<>();

    // ----------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------

    /**
     * This constructor builds a model of the context switches per interval of
     * the trace passed in argument
     *
     * @param trace
     *            The trace represented by the model.
     * @param name
     *            The name of this model. If null, "ContextSwitchModel" is used
     */
    public TmfContextSwitchModel(ITmfTrace trace, String name) {
        setTrace(trace);

        if (name == null) {
            setName(Messages.TmfContextSwitchView_DefaultModelName);
        } else {
            setName(name);
        }
    }

    /**
     * Default constructor.
     */
    public TmfContextSwitchModel() {
        this(null, null);
    }

    // ----------------------------------------------------------
    // Methods
    // ----------------------------------------------------------

    /**
     * @return True if this model is completely built, false otherwise.
     */
    public boolean isModelReady() {
        return fIsModelComplete.get();
    }

    /**
     * The model will be entirely constructed. If the trace of the model is part
     * of an experiment, every trace's statesystem of that experiment will be
     * queried
     */
    public void queryAllKernelContextSwitches() {
        for (ITmfTrace trace : TmfTraceManager.getTraceSet(fTrace)) {
            queryTraceContextSwitches(trace);
        }
    }

    /**
     * The trace parameter has to be part of the experiment of the current Trace
     *
     * @param trace
     *            The trace to be queried
     */
    public void queryTraceContextSwitches(ITmfTrace trace) {
        if (!TmfTraceManager.getTraceSet(fTrace).contains(trace)) {
            /* Don't query */
            return;
        }
        fIsModelComplete.set(false);

        KernelContextSwitchContainer container;
        if (fDataHolder.containsKey(trace)) {
            container = fDataHolder.get(trace);
            container.clear();
        } else {
            container = new KernelContextSwitchContainer();
        }

        ModelUpdateJob updateJob = new ModelUpdateJob(Messages.TmfContextSwitchView_DefaultModelUpdateJobName, container, trace, trace.getTimeRange());
        fUpdateJobs.add(updateJob);
        updateJob.schedule();
        fDataHolder.put(trace, container);
    }

    /**
     * Internal method to notify the model that one of its querying thread has
     * finished. Normally, it should only be called by {@link ModelUpdateJob}s
     *
     * @param job
     *            The calling job
     */
    private void notifyQueryComplete(Job job) {

        Iterator<Job> i = fUpdateJobs.iterator();
        while (i.hasNext()) {
            Job j = i.next();
            if (j.equals(job)) {
                i.remove();
                if(fUpdateJobs.isEmpty()){
                    fIsModelComplete.set(true);
                }
                return;
            }
        }

    }

    // ----------------------------------------------------------
    // Getters and setters
    // ----------------------------------------------------------

    /**
     * @return The trace represented by this model
     */
    public ITmfTrace getTrace() {
        return fTrace;
    }

    /**
     * Sets this model to represent another trace's context switches. <br>
     * <b>Note</b>: a call to this method with a different trace than the
     * current one erases the data.
     *
     * @param newTrace
     *            The new trace to be represented by this model
     */
    public void setTrace(ITmfTrace newTrace) {
        if (fTrace == newTrace) {
            return;
        }
        fIsModelComplete.set(false);
        fDataHolder.clear();
        fTrace = newTrace;
    }

    /**
     * @return the name of this model
     */
    public String getName() {
        return fName;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        fName = name;
    }

    /**
     * @return te current granularity of the query
     */
    public static Integer getResolution() {
        return fResolution;
    }

    /**
     * @param resolution the new resolution
     */
    public void setResolution(Integer resolution) {
        fResolution = resolution;
    }

    /**
     * Querying thread of this model.
     *
     * @author Alexis Cabana-Loriaux
     */
    private class ModelUpdateJob extends Job {
        /** The model to update */
        private final KernelContextSwitchContainer fContainer;

        /** The trace ofthe request */
        private final ITmfTrace fJobTrace;

        /** The time range of the query */
        private final TmfTimeRange fTimeRange;

        /**
         * @param name
         *            The name of this job
         * @param container
         *            The container to update
         * @param trace
         *            The trace of the query
         * @param timerange
         *            The Time range of the query (normally )
         */
        public ModelUpdateJob(String name, KernelContextSwitchContainer container, ITmfTrace trace, TmfTimeRange timerange) {
            super(name);
            fContainer = container;
            fJobTrace = trace;
            fTimeRange = timerange;
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            if (fJobTrace == null) {
                return Status.OK_STATUS;
            }

            long startTime = fTimeRange.getStartTime().getValue();
            long endTime = fTimeRange.getEndTime().getValue();
            Long timeRangeOffset = (endTime - startTime) / fResolution.longValue();


            /* Store querying intervals */
            List<TmfTimeRange> queryingIntervals = new LinkedList<>();
            for (int j = 0; j < fResolution; j++) {
                TmfTimestamp intervalBegin = new TmfTimestamp(startTime + j * timeRangeOffset,ITmfTimestamp.NANOSECOND_SCALE);
                TmfTimestamp intervalEnd = new TmfTimestamp(startTime + (j + 1) * timeRangeOffset, ITmfTimestamp.NANOSECOND_SCALE);
                queryingIntervals.add(new TmfTimeRange(intervalBegin, intervalEnd));
            }

            KernelAnalysisModule kam = TmfTraceUtils.getAnalysisModuleOfClass(fJobTrace, KernelAnalysisModule.class, KernelAnalysisModule.ID);

            if (kam == null) {
                /* TODO: Maybe throw something to warn? */
                return Status.OK_STATUS;
            }

            /* It's a kernel trace, begin query */
            stateSystemQuery(kam, fContainer, queryingIntervals);
            notifyQueryComplete(this);
            return Status.OK_STATUS;
        }

        /*
         * Queries are made in O(m*n), where m is the granularity and n is the #
         * of CPUs. The new container is assumed to be empty.
         */
        private void stateSystemQuery(KernelAnalysisModule kam, KernelContextSwitchContainer newContainer, List<TmfTimeRange> queryingIntervals) {

            kam.waitForCompletion();
            final ITmfStateSystem ss = kam.getStateSystem();
            if (ss == null) {
                return;
            }

            try {
                int rootQuark = ss.getQuarkAbsolute(Attributes.CPUS);
                List<Integer> cpuQuarks = ss.getSubAttributes(rootQuark, false);

                /* Query for every cpu */
                for (int i = 0; i < cpuQuarks.size(); i++) {
                    List<Integer> listAttributesCPU = ss.getSubAttributes(cpuQuarks.get(i), false);
                    if (listAttributesCPU.isEmpty()) {
                        // TODO: maybe throw an exception?
                        continue;
                    }

                    for (TmfTimeRange queryingInterval : queryingIntervals) {
                        List<ITmfStateInterval> threadChangesInTimeRange = StateSystemUtils.queryHistoryRange(ss, listAttributesCPU.get(0), queryingInterval.getStartTime().getValue(), queryingInterval.getEndTime().getValue());
                        /* The number of intervals is (# of context switch -1) */
                        newContainer.addCPUContextSwitches(i, queryingInterval, Math.max(threadChangesInTimeRange.size() - 1L, 0));
                    }
                }

            } catch (AttributeNotFoundException | StateSystemDisposedException e) {
                return;
            }
        }
    }

}
