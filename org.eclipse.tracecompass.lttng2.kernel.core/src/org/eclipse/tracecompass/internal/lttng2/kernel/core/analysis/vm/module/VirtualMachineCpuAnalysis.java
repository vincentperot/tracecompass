/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.module;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.VcpuStateValues;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.VmAttributes;
import org.eclipse.tracecompass.lttng2.kernel.core.analysis.kernel.LttngKernelAnalysis;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.interval.TmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperimentUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

/**
 * Module for the virtual machine state systems
 *
 * @author Geneviève Bastien
 */
public class VirtualMachineCpuAnalysis extends TmfStateSystemAnalysisModule {

    /** ID of this analysis module */
    public static final String ID = "lttng2.analysis.vm.core.VirtualMachineAnalysisModule"; //$NON-NLS-1$

    @SuppressWarnings("null")
    static final ImmutableSet<String> REQUIRED_EVENTS = ImmutableSet.of(
//            LttngStrings.SCHED_SWITCH
            );

    private static final ITmfStateValue VCPU_PREEMPT_VALUE = TmfStateValue.newValueInt(VcpuStateValues.VCPU_PREEMPT);

    /**
     * Constructor
     */
    public VirtualMachineCpuAnalysis() {
    }

    @Override
    protected @NonNull ITmfStateProvider createStateProvider() {
        ITmfTrace trace = getTrace();
        if (!(trace instanceof TmfExperiment)) {
            throw new IllegalStateException();
        }
        return new VirtualMachineStateProvider((TmfExperiment) trace);
    }

    @Override
    protected @NonNull StateSystemBackendType getBackendType() {
        return StateSystemBackendType.FULL;
    }

    @Override
    public String getHelpText() {
        return super.getHelpText();
    }

    @Override
    protected Iterable<IAnalysisModule> getDependentAnalyses() {
        Set<IAnalysisModule> modules = new HashSet<>();
        /* Depends on the LTTng Kernel analysis modules */
        for (ITmfTrace trace : TmfTraceManager.getTraceSet(getTrace())) {
            for (LttngKernelAnalysis module : TmfTraceUtils.getAnalysisModulesOfClass(trace, LttngKernelAnalysis.class)) {
                modules.add(module);
            }
        }
        return modules;
    }

    /**
     * Get the status intervals for the thread from a virtual machine. Those
     * intervals are correlated with the data from the virtual CPU's preemption
     * status.
     *
     * This method uses the Linux Kernel Analysis data for the thread's status
     * intervals.
     *
     * @param vmQuark
     *            The quark of the virtual machine
     * @param start
     *            The start time of the period to get the intervals from
     * @param end
     *            The end time of the period to get the intervals from
     * @param resolution
     *            The resolution
     * @param monitor
     *            A progress monitor for this task
     * @return A map of status intervals for the machine's threads, including
     *         preempted intervals, ordered by their start time.
     */
    public Multimap<String, ITmfStateInterval> getUpdatedThreadIntervals(int vmQuark, long start, long end, long resolution, IProgressMonitor monitor) {
        @SuppressWarnings("null")
        @NonNull
        final Multimap<String, ITmfStateInterval> map = TreeMultimap.create(new Comparator<String>() {
            @Override
            public int compare(@Nullable String arg0, @Nullable String arg1) {
                /* Keys do not have to be sorted */
                if (arg0 == null || arg1 == null) {
                    return -1;
                }
                return arg0.compareTo(arg1);
            }
        }, new Comparator<ITmfStateInterval>() {
            @Override
            public int compare(@Nullable ITmfStateInterval arg0, @Nullable ITmfStateInterval arg1) {
                /* Compare intervals by their start time and end time */
                if (arg0 == null || arg1 == null) {
                    return 0;
                }
                if (arg1.getStateValue() == VCPU_PREEMPT_VALUE) {
                    /* For VCPU_PREEMPT state values, the state has to be behind any other state that overlaps it */
                    return ((Long) arg0.getEndTime()).compareTo(arg1.getStartTime());
                }
                /* Otherwise, we use ordering by start time */
                return ((Long) arg0.getStartTime()).compareTo(arg1.getStartTime());
            }
        });
        ITmfStateSystem ssq = getStateSystem();
        if (ssq == null) {
            return map;
        }
        ITmfTrace trace = getTrace();
        if (!(trace instanceof TmfExperiment)) {
            return map;
        }
        @SuppressWarnings("null")
        @NonNull
        String vmHostId = ssq.getAttributeName(vmQuark);
        LttngKernelAnalysis kernelModule = TmfExperimentUtils.getAnalysisModuleOfClassForHost((TmfExperiment) trace, vmHostId, LttngKernelAnalysis.class);
        if (kernelModule == null) {
            return map;
        }

        /*
         * Initialize the map with the original status intervals from the kernel
         * module
         */
        for (Integer threadQuark : kernelModule.getThreadQuarks()) {
            if (threadQuark == null) {
                throw new IllegalStateException();
            }
            map.putAll(kernelModule.getAttributeName(threadQuark), kernelModule.getStatusIntervalsForThread(threadQuark, start, end, resolution, monitor));
            if (monitor.isCanceled()) {
                return map;
            }
        }

        try {
            for (Integer vcpuQuark : ssq.getSubAttributes(vmQuark, false)) {
                Long currentCPU = Long.parseLong(ssq.getAttributeName(vcpuQuark));
                Integer statusQuark = ssq.getQuarkRelative(vcpuQuark, VmAttributes.STATUS);
                for (ITmfStateInterval cpuInterval : StateSystemUtils.queryHistoryRange(ssq, statusQuark, start, end - 1, resolution, monitor)) {
                    ITmfStateValue stateValue = cpuInterval.getStateValue();
                    switch (stateValue.getType()) {
                    case INTEGER:
                        int value = stateValue.unboxInt();
                        /*
                         * If the current CPU is either preempted or in
                         * hypervisor mode, add preempted intervals to running
                         * processes
                         */
                        if ((value & (VcpuStateValues.VCPU_PREEMPT | VcpuStateValues.VCPU_VMM)) == 0) {
                            break;
                        }
                        int threadOnCpu = kernelModule.getThreadOnCpu(currentCPU, cpuInterval.getStartTime());
                        map.put(String.valueOf(threadOnCpu), new TmfStateInterval(cpuInterval.getStartTime(), cpuInterval.getEndTime(), threadOnCpu, VCPU_PREEMPT_VALUE));
                        break;
                    case DOUBLE:
                    case LONG:
                    case NULL:
                    case STRING:
                    default:
                        break;
                    }

                }
            }
        } catch (AttributeNotFoundException | StateSystemDisposedException e) {

        }
        if (map == null) {
            throw new IllegalStateException();
        }
        return map;
    }

}
