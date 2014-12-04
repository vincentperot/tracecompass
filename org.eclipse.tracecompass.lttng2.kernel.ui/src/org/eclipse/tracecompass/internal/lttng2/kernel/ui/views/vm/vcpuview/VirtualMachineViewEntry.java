/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mohamad Gebai - Initial API and implementation
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.ui.views.vm.vcpuview;

import java.util.Collection;
import java.util.Comparator;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.trace.VirtualMachineExperiment;
import org.eclipse.tracecompass.internal.lttng2.kernel.ui.views.vm.vcpuview.VirtualMachineCommon.Type;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

import com.google.common.collect.Multimap;

/**
 * An entry, or row, in the resource view
 *
 * @author Mohamad Gebai
 */
public class VirtualMachineViewEntry extends TimeGraphEntry {

    private static final Comparator<ITimeGraphEntry> COMPARATOR = new Comparator<ITimeGraphEntry>() {

        @Override
        public int compare(@Nullable ITimeGraphEntry o1, @Nullable ITimeGraphEntry o2) {

            int result = 0;

            if ((o1 instanceof VirtualMachineViewEntry) && (o2 instanceof VirtualMachineViewEntry)) {
                VirtualMachineViewEntry entry1 = (VirtualMachineViewEntry) o1;
                VirtualMachineViewEntry entry2 = (VirtualMachineViewEntry) o2;
                result = entry1.getType().compareTo(entry2.getType());
                if (result == 0) {
                    result = entry1.getId().compareTo(entry2.getId());
                }
            }
            return result;
        }
    };

    private final String fId;
    private final @Nullable String fVmName;
    private final ITmfTrace fTrace;
    private final VirtualMachineExperiment fExperiment;
    private final Type fType;
    private final int fQuark;
    private @Nullable Multimap<String, ITmfStateInterval> fThreadIntervals = null;

    /**
     * Private constructor using a builder to build an entry
     *
     * @param builder
     *            The builder from which to build this entry
     */
    private VirtualMachineViewEntry(VmEntryBuilder builder) {
        super(builder.fbEntryName, builder.fbStartTime, builder.fbEndTime);
        fId = builder.fbId;
        fExperiment = builder.fbExperiment;
        /* If trace is not set, initialize to experiment */
        ITmfTrace trace = builder.fbTrace;
        if (trace == null) {
            trace = fExperiment;
        }
        fTrace = trace;
        Type type = builder.fbType;
        if (type == null) {
            type = Type.NULL;
        }
        fType = type;
        fQuark = builder.fbQuark;
        fVmName = builder.fbVmName;
        this.sortChildren(COMPARATOR);
    }

    /**
     * Builder class that allows to build an entry by setting the parameters
     * independently, instead of using directly the constructors with many
     * parameters.
     *
     * @author Geneviève Bastien
     */
    public static class VmEntryBuilder {

        private final String fbEntryName;
        private final long fbStartTime;
        private final long fbEndTime;
        private final VirtualMachineExperiment fbExperiment;

        private String fbId;
        private @Nullable String fbVmName;
        private @Nullable ITmfTrace fbTrace;
        private @Nullable Type fbType;
        private int fbQuark;

        /**
         * Virtual Machine Entry builder constructor.
         *
         * @param name
         *            The name of this entry. It is also the default ID of this
         *            entry. So the ID does not need to be set if it is the same
         *            as the name.
         * @param startTime
         *            The start time of the entry
         * @param endTime
         *            The end time of the entry
         * @param experiment
         *            The experiment this entry applies to
         */
        public VmEntryBuilder(String name, long startTime, long endTime, VirtualMachineExperiment experiment) {
            fbEntryName = name;
            fbStartTime = startTime;
            fbEndTime = endTime;
            fbExperiment = experiment;
            fbId = name;
        }

        /**
         * Sets the ID of this entry
         *
         * @param id
         *            The ID of the virtual machine entry
         * @return The builder with updated fields
         */
        public VmEntryBuilder setId(String id) {
            fbId = id;
            return this;
        }

        /**
         * Sets the virtual machine name of this entry
         *
         * @param vmName
         *            The virtual machine name of the virtual machine entry
         * @return The builder with updated fields
         */
        public VmEntryBuilder setVmName(String vmName) {
            fbVmName = vmName;
            return this;
        }

        /**
         * Sets the trace this entry applies to
         *
         * @param trace
         *            The trace this entry is for
         * @return The builder with updated fields
         */
        public VmEntryBuilder setTrace(ITmfTrace trace) {
            fbTrace = trace;
            return this;
        }

        /**
         * Sets the type of this entry
         *
         * @param type
         *            The type of the virtual machine entry
         * @return The builder with updated fields
         */
        public VmEntryBuilder setType(Type type) {
            fbType = type;
            return this;
        }

        /**
         * Sets the quark of this entry
         *
         * @param quark
         *            The quark of the virtual machine entry
         * @return The builder with updated fields
         */
        public VmEntryBuilder setQuark(int quark) {
            fbQuark = quark;
            return this;
        }

        /**
         * Creates a new instance of {@link VirtualMachineViewEntry} with the
         * fields corresponding to those set in the builder.
         *
         * @return A new {@link VirtualMachineViewEntry} object
         */
        public VirtualMachineViewEntry build() {
            return new VirtualMachineViewEntry(this);
        }

    }

    /**
     * Get the entry's id
     *
     * @return the entry's id
     */
    public String getId() {
        return fId;
    }

    /**
     * @return The name of the virtual machine
     */
    public @Nullable String getVmName() {
        return fVmName;
    }

    /**
     * Get the entry's kernel trace
     *
     * @return the entry's kernel trace
     */
    public ITmfTrace getTrace() {
        return fTrace;
    }

    /**
     * Get the entry's kernel trace
     *
     * @return the entry's kernel trace
     */
    public VirtualMachineExperiment getExperiment() {
        return fExperiment;
    }

    /**
     * Get the entry Type of this entry. Uses the virtual machine enum
     * {@link Type}
     *
     * @return The entry type
     */
    public Type getType() {
        return fType;
    }

    /**
     * Retrieve the attribute quark that's represented by this entry.
     *
     * @return The integer quark The attribute quark matching the entry
     */
    public int getQuark() {
        return fQuark;
    }

    @Override
    public boolean hasTimeEvents() {
        if (fType == Type.NULL) {
            return false;
        }
        return true;
    }

    /**
     * Get the state intervals for a given thread ID
     *
     * @param threadId
     *            The thread ID for which to get the intervals
     * @return A collection of intervals for this thread, or {@code null} if no
     *         intervals are available for this thread
     */
    public @Nullable Collection<ITmfStateInterval> getThreadIntervals(String threadId) {
        final Multimap<String, ITmfStateInterval> threadIntervals = fThreadIntervals;
        if (threadIntervals == null) {
            return null;
        }
        return threadIntervals.get(threadId);

    }

    /**
     * Set the intervals for the threads of the corresponding virtual machine.
     * This should be called only if the type of this entry is {@link Type#VM}.
     *
     * @param threadIntervals
     *            The map of intervals for each thread ID
     */
    public void setThreadIntervals(Multimap<String, ITmfStateInterval> threadIntervals) {
        fThreadIntervals = threadIntervals;
    }

}
