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

package org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.model.qemukvm;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.model.HostThread;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.model.IVirtualMachineModel;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.model.VirtualCPU;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.model.VirtualMachine;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.trace.VirtualMachineExperiment;
import org.eclipse.tracecompass.lttng2.kernel.core.analysis.kernel.LttngKernelAnalysis;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperimentUtils;

import com.google.common.collect.ImmutableSet;

/**
 * The virtual machine model corresponding to the Qemu/KVM hypervisor. It uses
 * the kvm_entry/kvm_exit events to identify entry to and exit from the
 * hypervisor. It also requires vmsync_* events from both guests and hosts to
 * identify which thread from a host belongs to which machine.
 *
 * @author Geneviève Bastien
 */
public class QemuKvmVmModel implements IVirtualMachineModel {

    private static final String KVM = "kvm_"; //$NON-NLS-1$

    private final Map<HostThread, VirtualCPU> tidtovcpu;
    // /* VM host, TID on host */
    // private Map<String, Integer> vmtotid;
    /* Host ID, TID, VM name */
    private final Map<HostThread, String> tidtovm;

    /** Maps a virtual machine name to a virtual machine. TODO: Am I needed? */
    private final Map<String, VirtualMachine> fKnownMachines;

    private final VirtualMachineExperiment fExperiment;

    @SuppressWarnings("null")
    static final ImmutableSet<String> REQUIRED_EVENTS = ImmutableSet.of(
            QemuKvmStrings.KVM_ENTRY,
            QemuKvmStrings.KVM_EXIT,
            QemuKvmStrings.VMSYNC_GH_GUEST,
            QemuKvmStrings.VMSYNC_GH_HOST,
            QemuKvmStrings.VMSYNC_HG_GUEST,
            QemuKvmStrings.VMSYNC_HG_HOST
            );

    /**
     * Constructor
     * @param exp The experiment this model applies to
     */
    public QemuKvmVmModel(VirtualMachineExperiment exp) {
        tidtovcpu = new HashMap<>();
        tidtovm = new HashMap<>();
        fKnownMachines = new HashMap<>();
        fExperiment = exp;
    }

    @Override
    public @Nullable VirtualMachine getCurrentMachine(ITmfEvent event) {
        final String hostId = event.getTrace().getHostId();
        VirtualMachine machine = fKnownMachines.get(hostId);
        if (machine != null) {
            return machine;
        }

        /* Try to get the virtual machine from the event */
        String eventName = event.getType().getName();
        if (eventName.startsWith(KVM)) {
            machine = VirtualMachine.newHostMachine(event.getTrace().getHostId());
        } else if (eventName.equals(QemuKvmStrings.VMSYNC_GH_GUEST) || eventName.equals(QemuKvmStrings.VMSYNC_HG_GUEST)) {
            TmfEventField field = (TmfEventField) event.getContent();
            ITmfEventField data = field.getField(QemuKvmStrings.VM_UID_PAYLOAD);
            machine = VirtualMachine.newGuestMachine((Long) data.getValue(), event.getTrace().getHostId());
        }
        if (machine != null) {
            fKnownMachines.put(event.getTrace().getHostId(), machine);
        }
        return machine;
    }

    @Override
    public Set<String> getRequiredEvents() {
        return REQUIRED_EVENTS;
    }

    @Override
    public @Nullable VirtualCPU exitingHypervisorMode(ITmfEvent event, HostThread ht) {
        final String eventName = event.getType().getName();
        /* TODO: Use event layouts for this part also */
        if (!eventName.equals(QemuKvmStrings.KVM_ENTRY)) {
            return null;
        }

        /*
         * Are we entering the hypervisor and if so, which virtual CPU is
         * concerned?
         */
        /* Update the status of this virtual CPU */
        String vmName = tidtovm.get(ht);
        if (vmName == null) {
            /*
             * Maybe the parent of the current thread has a VM associated, if we
             * can infer the VM of this thread
             */
            LttngKernelAnalysis module = getLttngKernelModuleFor(ht.getHost());
            if (module == null) {
                return null;
            }

            Integer ppid = module.getPpid(ht.getTid(), event.getTimestamp().getValue());
            if (ppid == LttngKernelAnalysis.NO_TID) {
                return null;
            }

            HostThread parentHt = new HostThread(ht.getHost(), ppid);
            vmName = tidtovm.get(parentHt);
            if (vmName == null) {
                return null;
            }
            tidtovm.put(ht, vmName);
        }
        final ITmfEventField content = event.getContent();
        long vcpu_id = (Long) content.getField(QemuKvmStrings.VCPU_ID).getValue();

        VirtualMachine vm = fKnownMachines.get(vmName);
        if (vm == null) {
            return null;
        }

        VirtualCPU virtualCPU = VirtualCPU.getVirtualCPU(vm, vcpu_id);
        tidtovcpu.put(ht, virtualCPU);

        return virtualCPU;

    }

    @Override
    public @Nullable VirtualCPU enteringHypervisorMode(ITmfEvent event, HostThread ht) {
        final String eventName = event.getType().getName();
        if (!eventName.equals(QemuKvmStrings.KVM_EXIT)) {
            return null;
        }

        VirtualCPU vcpu = tidtovcpu.get(ht);
        return vcpu;
    }

    @Override
    public @Nullable VirtualCPU getVirtualCpu(HostThread ht) {
        return tidtovcpu.get(ht);
    }

    @Override
    public void eventHandle(ITmfEvent event) {
        /* Is the event one of those the analysis manages */
        final String eventName = event.getType().getName();
        if (!REQUIRED_EVENTS.contains(eventName)) {
            return;
        }

        final ITmfEventField content = event.getContent();
        final long ts = event.getTimestamp().getValue();
        final String hostId = event.getTrace().getHostId();

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

        switch (eventName) {
        case QemuKvmStrings.VMSYNC_GH_HOST: // vmsync_gh_host
        {
            /* Find a virtual machine with the vm uid payload value */
            ITmfEventField data = content.getField(QemuKvmStrings.VM_UID_PAYLOAD);
            if (data == null) {
                return;
            }
            long vmUid = (Long) data.getValue();
            for (Entry<String, VirtualMachine> entry : fKnownMachines.entrySet()) {
                if (entry.getValue().getVmUid() == vmUid) {
                    /*
                     * We found the VM being run, let's associate it with
                     * the thread ID
                     */
                    LttngKernelAnalysis module = getLttngKernelModuleFor(hostId);
                    if (module == null) {
                        break;
                    }
                    Integer proc = module.getThreadOnCpu(cpu, ts);
                    if (proc == null) {
                        /*
                         * We do not know which process is running at this
                         * point. It may happen at the beginning of the
                         * trace
                         */
                        break;
                    }
                    HostThread ht = new HostThread(hostId, proc);
                    tidtovm.put(ht, entry.getKey());

                    /*
                     * To make sure siblings are also associated with this
                     * VM, also add an entry for the parent TID
                     */
                    Integer ppid = module.getPpid(proc, ts);
                    if (!(ppid.equals(LttngKernelAnalysis.NO_TID))) {
                        HostThread parentHt = new HostThread(hostId, ppid);
                        tidtovm.put(parentHt, entry.getKey());
                    }
                }
            }

        }
            break;
        default:
            break;
        }
    }

    private @Nullable LttngKernelAnalysis getLttngKernelModuleFor(String hostId) {
        return TmfExperimentUtils.getAnalysisModuleOfClassForHost(fExperiment, hostId, LttngKernelAnalysis.class);
    }

}
