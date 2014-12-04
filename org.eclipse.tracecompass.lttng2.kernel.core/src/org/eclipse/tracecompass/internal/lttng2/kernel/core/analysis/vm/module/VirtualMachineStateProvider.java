/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mohamad Gebai - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.module;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.Activator;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.VcpuStateValues;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.VmAttributes;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.model.HostThread;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.model.IVirtualMachineModel;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.model.VirtualCPU;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.model.VirtualMachine;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.model.qemukvm.QemuKvmVmModel;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.trace.VirtualMachineExperiment;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout.LttngEventLayout;
import org.eclipse.tracecompass.lttng2.kernel.core.analysis.kernel.LttngKernelAnalysis;
import org.eclipse.tracecompass.lttng2.kernel.core.trace.LttngKernelTrace;
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
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperimentUtils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * This is the state provider which translates the virtual machine experiment
 * events to a state system.
 *
 * Attribute tree:
 *
 * <pre>
 * |- Virtual Machines
 * |  |- <Guest Host ID>  -> Friendly name (trace name)
 * |  |  |- <VCPU number>
 * |  |  |  |- Status
 * |  |  |  |- Previous Status
 * |  |  |  |- Host thread
 * </pre>
 *
 * @author Mohamad Gebai
 *
 *         Notes: Possible values for a VCPU: { CPU_STATUS_IDLE,
 *         CPU_STATUS_RUN_USERMODE, CPU_STATUS_RUN_SYSCALL, CPU_STATUS_IRQ,
 *         CPU_STATUS_SOFTIRQ, VCPU_BLOCKED, VCPU_WAIT_FOR_VMM }
 *
 *         Possible values for a Thread under VCPU: { PROCESS_STATUS_UNKNOWN,
 *         PROCESS_STATUS_WAIT_BLOCKED, PROCESS_STATUS_RUN_USERMODE,
 *         PROCESS_STATUS_USERMODE_VIRT_BLOCKED, PROCESS_STATUS_RUN_SYSCALL,
 *         PROCESS_STATUS_SYSCALL_VIRT_BLOCKED, PROCESS_STATUS_INTERRUPTED,
 *         PROCESS_STATUS_WAIT_FOR_CPU }
 *
 *         Interrupts aren't handled for now because they break everything:
 *         interrupts happen too often and because of the timestamping problem
 *         for virtual machines, it breaks the view.
 *
 */
public class VirtualMachineStateProvider extends AbstractTmfStateProvider {

    /**
     * Version number of this state provider. Please bump this if you modify the
     * contents of the generated state history in some way.
     */
    private static final int VERSION = 1;

    private static final int SCHED_SWITCH_INDEX = 0;

    private IVirtualMachineModel fModel;
    private final Table<ITmfTrace, String, Integer> fEventNames;
    private final Map<ITmfTrace, IKernelAnalysisEventLayout> fLayouts;


    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Instantiate a new state provider.
     *
     * @param trace
     *            The virtual machine experiment
     */
    public VirtualMachineStateProvider(TmfExperiment trace) {
        super(trace, ITmfEvent.class, "Virtual Machine State Provider"); //$NON-NLS-1$
        if (!(trace instanceof VirtualMachineExperiment)) {
            throw new IllegalArgumentException();
        }

        fModel = new QemuKvmVmModel((VirtualMachineExperiment) trace);
        @SuppressWarnings("null")
        @NonNull Table<ITmfTrace, String, Integer> table = HashBasedTable.create();
        fEventNames = table;
        fLayouts = new HashMap<>();
    }

    // ------------------------------------------------------------------------
    // Event names management
    // ------------------------------------------------------------------------

    private void buildEventNames(ITmfTrace trace) {
        IKernelAnalysisEventLayout layout;
        if (trace instanceof LttngKernelTrace) {
            layout = ((LttngKernelTrace) trace).getEventLayout();
        } else {
            /* Fall-back to the base LttngEventLayout */
            layout = LttngEventLayout.getInstance();
        }
        fLayouts.put(trace, layout);
        fEventNames.put(trace, layout.eventSchedSwitch(), SCHED_SWITCH_INDEX);
    }

    // ------------------------------------------------------------------------
    // IStateChangeInput
    // ------------------------------------------------------------------------

    @Override
    public TmfExperiment getTrace() {
        ITmfTrace trace = super.getTrace();
        if (trace instanceof TmfExperiment) {
            return (TmfExperiment) trace;
        }
        throw new IllegalStateException("VirtualMachineStateProvider: The associated trace should be an experiment"); //$NON-NLS-1$
    }



    @Override
    public int getVersion() {
        return VERSION;
    }

    @Override
    public VirtualMachineStateProvider getNewInstance() {
        TmfExperiment trace = getTrace();
        return new VirtualMachineStateProvider(trace);
    }

    @Override
    protected void eventHandle(@Nullable ITmfEvent event) {
        if (event == null) {
            return;
        }

        /* Is the event one of those the analysis manages */
        final String eventName = event.getType().getName();
        /* TODO When requirements work again, reimplement this */
//        if (!VirtualMachineCpuAnalysis.REQUIRED_EVENTS.contains(eventName) && !fModel.getRequiredEvents().contains(eventName)) {
//            return;
//        }

        ITmfStateValue value;

        final ITmfEventField content = event.getContent();
        final long ts = event.getTimestamp().getValue();
        final String hostId = event.getTrace().getHostId();
        try {
            /* Do we know this trace's role yet? */
            VirtualMachine host = fModel.getCurrentMachine(event);
            if (host == null) {
                return;
            }

            /* Start by checking if the event comes from a guest trace */
            if (host.isGuest()) { // If event from guest OS
                /*
                 * If event from a guest OS, make sure it exists in the state
                 * system
                 */
                int currentVMNode = -1;
                try {
                    currentVMNode = ss.getQuarkRelative(getNodeVirtualMachines(), host.getHostId());
                } catch (AttributeNotFoundException e) {
                    /*
                     * We should enter this catch only once per machine, so it
                     * is not so costly to do
                     */
                    currentVMNode = ss.getQuarkRelativeAndAdd(getNodeVirtualMachines(), host.getHostId());
                    TmfStateValue machineName = TmfStateValue.newValueString(event.getTrace().getName());
                    ss.modifyAttribute(ts, machineName, currentVMNode);
                }
            }

            if (!fEventNames.containsRow(event.getTrace())) {
                buildEventNames(event.getTrace());
            }
            Integer idx = fEventNames.get(event.getTrace(), eventName);
            int intval = (idx == null ? -1 : idx.intValue());
            switch (intval) {
            case SCHED_SWITCH_INDEX: // "sched_switch":
            /*
             * Fields: string prev_comm, int32 prev_tid, int32 prev_prio, int64
             * prev_state, string next_comm, int32 next_tid, int32 next_prio
             */
            {
                Integer prevTid = ((Long) content.getField(fLayouts.get(event.getTrace()).fieldPrevTid()).getValue()).intValue();
                Integer nextTid = ((Long) content.getField(fLayouts.get(event.getTrace()).fieldNextTid()).getValue()).intValue();

                if (prevTid == null || nextTid == null) {
                    break;
                }

                if (host.isGuest()) {
                    /* Get the event's CPU */
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
                        break;
                    }
                    /* For a guest, update the status of the vcpu to either running or idle */
                    int vmQuark = ss.getQuarkRelative(getNodeVirtualMachines(), host.getHostId());
                    int vcpuQuark = ss.getQuarkRelativeAndAdd(vmQuark, cpu.toString());
                    int curStatusQuark = ss.getQuarkRelativeAndAdd(vcpuQuark, VmAttributes.STATUS);
                    value = TmfStateValue.newValueInt(VcpuStateValues.VCPU_IDLE);
                    if (nextTid > 0) {
                        value = TmfStateValue.newValueInt(VcpuStateValues.VCPU_RUNNING);
                    }
                    ss.modifyAttribute(ts, value, curStatusQuark);
                    break;
                }

                /* Verify if the previous thread corresponds to a virtual CPU */
                HostThread ht = new HostThread(hostId, prevTid);
                VirtualCPU vcpu = fModel.getVirtualCpu(ht);

                /*
                 * If previous thread is virtual CPU, update status of the
                 * virtual CPU to preempted
                 */
                if (vcpu != null) {
                    VirtualMachine vm = vcpu.getVm();
                    try {
                        int vmQuark = ss.getQuarkRelative(getNodeVirtualMachines(), vm.getHostId());
                        /*
                         * Add the preempted flag to the status
                         */
                        int vcpuQuark = ss.getQuarkRelativeAndAdd(vmQuark, vcpu.getCpu().toString());
                        int curStatusQuark = ss.getQuarkRelativeAndAdd(vcpuQuark, VmAttributes.STATUS);
                        value = ss.queryOngoingState(curStatusQuark);
                        int newVal = Math.max(VcpuStateValues.VCPU_UNKNOWN, value.unboxInt());
                        value = TmfStateValue.newValueInt(newVal | VcpuStateValues.VCPU_PREEMPT);
                        ss.modifyAttribute(ts, value, curStatusQuark);

                    } catch (AttributeNotFoundException e) {
                        /* Ignore */
                        System.out.println(e);
                    }
                }

                /* Verify if the next thread corresponds to a virtual CPU */
                ht = new HostThread(hostId, nextTid);
                vcpu = fModel.getVirtualCpu(ht);

                /*
                 * If next thread is virtual CPU, update status of the virtual
                 * CPU the previous status
                 */
                if (vcpu != null) {
                    VirtualMachine vm = vcpu.getVm();
                    try {
                        int vmQuark = ss.getQuarkRelative(getNodeVirtualMachines(), vm.getHostId());
                        /*
                         * Remove the preempted flag from the status
                         */
                        int vcpuQuark = ss.getQuarkRelativeAndAdd(vmQuark, vcpu.getCpu().toString());
                        int curStatusQuark = ss.getQuarkRelativeAndAdd(vcpuQuark, VmAttributes.STATUS);
                        value = ss.queryOngoingState(curStatusQuark);
                        int newVal = Math.max(VcpuStateValues.VCPU_UNKNOWN, value.unboxInt());
                        value = TmfStateValue.newValueInt(newVal & ~VcpuStateValues.VCPU_PREEMPT);
                        ss.modifyAttribute(ts, value, curStatusQuark);

                    } catch (AttributeNotFoundException e) {
                        /* Ignore */
                        System.out.println(e);
                    }
                }

            }
                break;

            default:
            /* Other event types not covered by the main switch */
            {
                /* Check this event with the hypervisor models */
                fModel.eventHandle(event);

                /*
                 * Are we entering the hypervisor and if so, which virtual CPU
                 * is concerned?
                 */
                HostThread ht = getCurrentHostTid(event, ts);
                if (ht == null) {
                    break;
                }
                VirtualCPU virtualCpu = fModel.enteringHypervisorMode(event, ht);
                if (virtualCpu != null) {
                    /* Add the hypervisor flag to the status */
                    VirtualMachine vm = virtualCpu.getVm();
                    int vcpuQuark = ss.getQuarkRelativeAndAdd(getNodeVirtualMachines(), vm.getHostId(), Long.toString(virtualCpu.getCpu()));
                    int curStatusQuark = ss.getQuarkRelativeAndAdd(vcpuQuark, VmAttributes.STATUS);
                    value = ss.queryOngoingState(curStatusQuark);
                    int newVal = Math.max(VcpuStateValues.VCPU_UNKNOWN, value.unboxInt());
                    value = TmfStateValue.newValueInt(newVal | VcpuStateValues.VCPU_VMM);
                    ss.modifyAttribute(ts, value, curStatusQuark);

                }

                virtualCpu = fModel.exitingHypervisorMode(event, ht);
                if (virtualCpu != null) {
                    VirtualMachine vm = virtualCpu.getVm();

                    /* Remove the hypervisor flag from the status */
                    int vcpuQuark = ss.getQuarkRelativeAndAdd(getNodeVirtualMachines(), vm.getHostId(), Long.toString(virtualCpu.getCpu()));
                    int curStatusQuark = ss.getQuarkRelativeAndAdd(vcpuQuark, VmAttributes.STATUS);

                    value = ss.queryOngoingState(curStatusQuark);
                    int newVal = Math.max(VcpuStateValues.VCPU_UNKNOWN, value.unboxInt());
                    value = TmfStateValue.newValueInt(newVal & ~VcpuStateValues.VCPU_VMM);
                    ss.modifyAttribute(ts, value, curStatusQuark);
                }

            }
                break;
            } // End of big switch

        } catch (AttributeNotFoundException | TimeRangeException | StateValueTypeException e) {
            Activator.getDefault().logError("Error handling event in VirtualMachineStateProvider", e); //$NON-NLS-1$
        }
    }

    // ------------------------------------------------------------------------
    // Convenience methods for commonly-used attribute tree locations
    // ------------------------------------------------------------------------

    private int getNodeVirtualMachines() {
        return ss.getQuarkAbsoluteAndAdd(VmAttributes.VIRTUAL_MACHINES);
    }

    private @Nullable HostThread getCurrentHostTid(ITmfEvent event, long ts) {
        String hostId = event.getTrace().getHostId();
        LttngKernelAnalysis module = TmfExperimentUtils.getAnalysisModuleOfClassForHost(getTrace(), hostId, LttngKernelAnalysis.class);
        if (module == null) {
            return null;
        }
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
            return null;
        }

        Integer currentTid = module.getThreadOnCpu(cpu, ts);
        if (currentTid == null) {
            return null;
        }
        HostThread ht = new HostThread(hostId, currentTid);
        return ht;
    }

}