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

package org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.model;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * @author Geneviève Bastien
 *
 */
public class VirtualCPU {

    @SuppressWarnings("null")
    private static final Table<VirtualMachine, Long, VirtualCPU> VIRTUAL_CPU_TABLE = HashBasedTable.create();

    private final VirtualMachine fVm;
    private final Long fCpu;

    /**
     * Return the virtual CPU corresponding to the virtual machine and requested cpu ID
     *
     * @param vm The virtual Machine
     * @param cpu the CPU number
     * @return the corresponding virtual CPU
     */
    public static VirtualCPU getVirtualCPU(VirtualMachine vm, Long cpu) {
        VirtualCPU ht = VIRTUAL_CPU_TABLE.get(vm, cpu);
        if (ht == null) {
            ht = new VirtualCPU(vm, cpu);
            VIRTUAL_CPU_TABLE.put(vm, cpu, ht);
        }
        return ht;
    }

    private VirtualCPU(VirtualMachine vm, Long cpu) {
        fVm = vm;
        fCpu = cpu;
    }

    /**
     * Get the CPU ID of this virtual CPU
     *
     * @return The zero-based CPU id
     */
    public Long getCpu() {
        return fCpu;
    }

    /**
     * Get the virtual machine object this virtual CPU belongs to
     *
     * @return The guest Virtual Machine
     */
    public VirtualMachine getVm() {
        return fVm;
    }

    @Override
    public String toString() {
        return "VirtualCPU: " + fVm + "," + fCpu; //$NON-NLS-1$ //$NON-NLS-2$
    }

}