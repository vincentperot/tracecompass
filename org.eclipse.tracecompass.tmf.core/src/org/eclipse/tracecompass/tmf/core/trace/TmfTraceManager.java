/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Patrick Tasse - Support selection range
 *   Xavier Raynaud - Support filters tracking
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.trace;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.core.filter.ITmfFilter;
import org.eclipse.tracecompass.tmf.core.signal.TmfEventFilterAppliedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;

import com.google.common.collect.ImmutableSet;

/**
 * Central trace manager for TMF. It tracks the currently opened traces and
 * experiment, as well as the currently-selected time or time range and the
 * current window time range for each one of those. It also tracks filters
 * applied for each trace.
 *
 * It's a singleton class, so only one instance should exist (available via
 * {@link #getInstance()}).
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public final class TmfTraceManager {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final Map<ITmfTrace, TmfTraceContext> fTraces;

    /** The currently-selected trace. Should always be part of the trace map */
    private ITmfTrace fCurrentTrace = null;

    private static final String TEMP_DIR_NAME = ".temp"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    private TmfTraceManager() {
        fTraces = new LinkedHashMap<>();
        TmfSignalManager.registerVIP(this);
    }

    /** Singleton instance */
    private static TmfTraceManager tm = null;

    /**
     * Get an instance of the trace manager.
     *
     * @return The trace manager
     */
    public static synchronized TmfTraceManager getInstance() {
        if (tm == null) {
            tm = new TmfTraceManager();
        }
        return tm;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return The begin timestamp of selection
     * @since 2.1
     */
    public ITmfTimestamp getSelectionBeginTime() {
        return getCurrentTraceContext().getSelectionBegin();
    }

    /**
     * @return The end timestamp of selection
     * @since 2.1
     */
    public ITmfTimestamp getSelectionEndTime() {
        return getCurrentTraceContext().getSelectionEnd();
    }

    /**
     * Return the current window time range.
     *
     * @return the current window time range
     */
    public synchronized TmfTimeRange getCurrentRange() {
        return getCurrentTraceContext().getWindowRange();
    }

    /**
     * Gets the filter applied to the current trace
     *
     * @return a filter, or <code>null</code>
     * @since 2.2
     */
    public synchronized ITmfFilter getCurrentFilter() {
        return getCurrentTraceContext().getFilter();
    }

    /**
     * Get the currently selected trace (normally, the focused editor).
     *
     * @return The active trace
     */
    public synchronized ITmfTrace getActiveTrace() {
        return fCurrentTrace;
    }

    /**
     * Get the trace set of the currently active trace.
     *
     * @return The active trace set
     * @see #getTraceSet(ITmfTrace)
     */
    public synchronized @NonNull Collection<ITmfTrace> getActiveTraceSet() {
        final ITmfTrace trace = fCurrentTrace;
        return getTraceSet(trace);
    }

    /**
     * Get the currently-opened traces, as an unmodifiable set.
     *
     * @return A set containing the opened traces
     */
    public synchronized Set<ITmfTrace> getOpenedTraces() {
        return Collections.unmodifiableSet(fTraces.keySet());
    }

    /**
     * Get the editor file for an opened trace.
     *
     * @param trace
     *            the trace
     * @return the editor file or null if the trace is not opened
     * @since 3.0
     */
    public synchronized IFile getTraceEditorFile(ITmfTrace trace) {
        TmfTraceContext ctx = fTraces.get(trace);
        if (ctx != null) {
            return ctx.getEditorFile();
        }
        return null;
    }

    private TmfTraceContext getCurrentTraceContext() {
        TmfTraceContext curCtx = fTraces.get(fCurrentTrace);
        if (curCtx == null) {
            /* There are no traces opened at the moment. */
            return TmfTraceContext.NULL_CONTEXT;
        }
        return curCtx;
    }

    // ------------------------------------------------------------------------
    // Public utility methods
    // ------------------------------------------------------------------------

    /**
     * Get the trace set of a given trace. For a standard trace, this is simply
     * an array with only that trace in it. For experiments, this is an array of
     * all the traces contained in this experiment.
     *
     * @param trace
     *            The trace or experiment
     * @return The corresponding trace set.
     */
    public static @NonNull Collection<ITmfTrace> getTraceSet(ITmfTrace trace) {
        if (trace == null) {
            return NonNullUtils.checkNotNull(ImmutableSet.<ITmfTrace> of());
        }
        List<ITmfTrace> traces = trace.getChildren(ITmfTrace.class);
        if (traces.size() > 0) {
            return NonNullUtils.checkNotNull(ImmutableSet.copyOf(traces));
        }
        return NonNullUtils.checkNotNull(ImmutableSet.of(trace));
    }

    /**
     * Get the trace set of a given trace or experiment, including the
     * experiment. For a standard trace, this is simply a set containing only
     * that trace. For experiments, it is the set of all the traces contained in
     * this experiment, along with the experiment.
     *
     * @param trace
     *            The trace or experiment
     * @return The corresponding trace set, including the experiment.
     * @since 3.1
     */
    public static @NonNull Collection<ITmfTrace> getTraceSetWithExperiment(ITmfTrace trace) {
        if (trace == null) {
            return checkNotNull(ImmutableSet.<ITmfTrace> of());
        }
        if (trace instanceof TmfExperiment) {
            TmfExperiment exp = (TmfExperiment) trace;
            List<ITmfTrace> traces = exp.getTraces();
            Set<ITmfTrace> alltraces = new LinkedHashSet<>(traces);
            alltraces.add(exp);
            return NonNullUtils.checkNotNull(ImmutableSet.copyOf(alltraces));
        }
        return checkNotNull(Collections.singleton(trace));
    }

    /**
     * Return the path (as a string) to the directory for supplementary files to
     * use with a given trace. If no supplementary file directory has been
     * configured, a temporary directory based on the trace's name will be
     * provided.
     *
     * @param trace
     *            The trace
     * @return The path to the supplementary file directory (trailing slash is
     *         INCLUDED!)
     */
    public static String getSupplementaryFileDir(ITmfTrace trace) {
        IResource resource = trace.getResource();
        if (resource == null) {
            return getTemporaryDir(trace);
        }

        String supplDir = null;
        try {
            supplDir = resource.getPersistentProperty(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER);
        } catch (CoreException e) {
            return getTemporaryDir(trace);
        }
        return supplDir + File.separator;
    }

    /**
     * Refresh the supplementary files resources for a trace, so it can pick up
     * new files that got created.
     *
     * @param trace
     *            The trace for which to refresh the supplementary files
     * @since 3.0
     */
    public static void refreshSupplementaryFiles(ITmfTrace trace) {
        IResource resource = trace.getResource();
        if (resource != null && resource.exists()) {
            String supplFolderPath = getSupplementaryFileDir(trace);
            IProject project = resource.getProject();
            /* Remove the project's path from the supplementary path dir */
            if (!supplFolderPath.startsWith(project.getLocationURI().getPath())) {
                Activator.logWarning(String.format("Supplementary files folder for trace %s is not within the project.", trace.getName())); //$NON-NLS-1$
                return;
            }
            IFolder supplFolder = project.getFolder(supplFolderPath.substring(project.getLocationURI().getPath().length()));
            if (supplFolder.exists()) {
                try {
                    supplFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
                } catch (CoreException e) {
                    Activator.logError("Error refreshing resources", e); //$NON-NLS-1$
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * Signal handler for the traceOpened signal.
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public synchronized void traceOpened(final TmfTraceOpenedSignal signal) {
        final ITmfTrace trace = signal.getTrace();
        final IFile editorFile = signal.getEditorFile();
        final ITmfTimestamp startTs = trace.getStartTime();

        /* Calculate the initial time range */
        final int SCALE = ITmfTimestamp.NANOSECOND_SCALE;
        long offset = trace.getInitialRangeOffset().normalize(0, SCALE).getValue();
        long endTime = startTs.normalize(0, SCALE).getValue() + offset;
        final TmfTimeRange startTr = new TmfTimeRange(startTs, new TmfTimestamp(endTime, SCALE));

        final TmfTraceContext startCtx = new TmfTraceContext(startTs, startTs, startTr, editorFile);

        fTraces.put(trace, startCtx);

        /* We also want to set the newly-opened trace as the active trace */
        fCurrentTrace = trace;
    }

    /**
     * Handler for the TmfTraceSelectedSignal.
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public synchronized void traceSelected(final TmfTraceSelectedSignal signal) {
        final ITmfTrace newTrace = signal.getTrace();
        if (!fTraces.containsKey(newTrace)) {
            throw new RuntimeException();
        }
        fCurrentTrace = newTrace;
    }

    /**
     * Signal handler for the filterApplied signal.
     *
     * @param signal
     *            The incoming signal
     * @since 2.2
     */
    @TmfSignalHandler
    public synchronized void filterApplied(TmfEventFilterAppliedSignal signal) {
        final ITmfTrace newTrace = signal.getTrace();
        TmfTraceContext context = fTraces.get(newTrace);
        if (context == null) {
            throw new RuntimeException();
        }
        fTraces.put(newTrace, new TmfTraceContext(context, signal.getEventFilter()));
    }

    /**
     * Signal handler for the traceClosed signal.
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public synchronized void traceClosed(final TmfTraceClosedSignal signal) {
        fTraces.remove(signal.getTrace());
        if (fTraces.size() == 0) {
            fCurrentTrace = null;
            /*
             * In other cases, we should receive a traceSelected signal that
             * will indicate which trace is the new one.
             */
        }
    }

    /**
     * Signal handler for the TmfTimeSynchSignal signal.
     *
     * The current time of *all* traces whose range contains the requested new
     * selection time range will be updated.
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public synchronized void timeUpdated(final TmfTimeSynchSignal signal) {
        final ITmfTimestamp beginTs = signal.getBeginTime();
        final ITmfTimestamp endTs = signal.getEndTime();

        for (Map.Entry<ITmfTrace, TmfTraceContext> entry : fTraces.entrySet()) {
            final ITmfTrace trace = entry.getKey();
            if (beginTs.intersects(getValidTimeRange(trace)) || endTs.intersects(getValidTimeRange(trace))) {
                TmfTraceContext prevCtx = entry.getValue();
                TmfTraceContext newCtx = new TmfTraceContext(prevCtx, beginTs, endTs);
                entry.setValue(newCtx);
            }
        }
    }

    /**
     * Signal handler for the TmfRangeSynchSignal signal.
     *
     * The current window time range of *all* valid traces will be updated to
     * the new requested times.
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public synchronized void timeRangeUpdated(final TmfRangeSynchSignal signal) {
        for (Map.Entry<ITmfTrace, TmfTraceContext> entry : fTraces.entrySet()) {
            final ITmfTrace trace = entry.getKey();
            final TmfTraceContext curCtx = entry.getValue();

            final TmfTimeRange validTr = getValidTimeRange(trace);

            /* Determine the new time range */
            TmfTimeRange targetTr = signal.getCurrentRange().getIntersection(validTr);
            TmfTimeRange newTr = (targetTr == null ? curCtx.getWindowRange() : targetTr);

            /* Update the values */
            TmfTraceContext newCtx = new TmfTraceContext(curCtx, newTr);
            entry.setValue(newCtx);
        }
    }

    // ------------------------------------------------------------------------
    // Private utility methods
    // ------------------------------------------------------------------------

    /**
     * Return the valid time range of a trace (not the current window time
     * range, but the range of all possible valid timestamps).
     *
     * For a real trace this is the whole range of the trace. For an experiment,
     * it goes from the start time of the earliest trace to the end time of the
     * latest one.
     *
     * @param trace
     *            The trace to check for
     * @return The valid time span, or 'null' if the trace is not valid
     */
    private TmfTimeRange getValidTimeRange(ITmfTrace trace) {
        if (!fTraces.containsKey(trace)) {
            /* Trace is not part of the currently opened traces */
            return null;
        }

        List<ITmfTrace> traces = trace.getChildren(ITmfTrace.class);

        if (traces.isEmpty()) {
            /* "trace" is a single trace, return its time range directly */
            return trace.getTimeRange();
        }

        if (traces.size() == 1) {
            /* Trace is an experiment with only 1 trace */
            return traces.get(0).getTimeRange();
        }

        /*
         * Trace is an trace set with 2+ traces, so get the earliest start and
         * the latest end.
         */
        ITmfTimestamp start = traces.get(0).getStartTime();
        ITmfTimestamp end = traces.get(0).getEndTime();

        for (int i = 1; i < traces.size(); i++) {
            ITmfTrace curTrace = traces.get(i);
            if (curTrace.getStartTime().compareTo(start) < 0) {
                start = curTrace.getStartTime();
            }
            if (curTrace.getEndTime().compareTo(end) > 0) {
                end = curTrace.getEndTime();
            }
        }
        return new TmfTimeRange(start, end);
    }

    /**
     * Get the temporary directory path. If there is an instance of Eclipse
     * running, the temporary directory will reside under the workspace.
     *
     * @return the temporary directory path suitable to be passed to the
     *         java.io.File constructor without a trailing separator
     * @since 3.2
     */
    public static String getTemporaryDirPath() {
        // Get the workspace path from the properties
        String property = System.getProperty("osgi.instance.area"); //$NON-NLS-1$
        if (property != null) {
            try {
                File dir = URIUtil.toFile(URIUtil.fromString(property));
                dir = new File(dir.getAbsolutePath() + File.separator + TEMP_DIR_NAME);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                return dir.getAbsolutePath();
            } catch (URISyntaxException e) {
                Activator.logError(e.getLocalizedMessage(), e);
            }
        }
        return System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
    }

    /**
     * Get a temporary directory based on a trace's name. We will create the
     * directory if it doesn't exist, so that it's ready to be used.
     */
    private static String getTemporaryDir(ITmfTrace trace) {
        String pathName = getTemporaryDirPath() +
                File.separator +
                trace.getName() +
                File.separator;
        File dir = new File(pathName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return pathName;
    }
}
