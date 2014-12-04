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
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm;

/**
 * State system values used by the VM analysis
 *
 * @author Mohamad Gebai
 */
public interface VcpuStateValues {

    /* VCPU Status */
    /** The virtual CPU state is unknown */
    static final int VCPU_UNKNOWN = 0;
    /** The virtual CPU is idle */
    static final int VCPU_IDLE = 1;
    /** The virtual CPU is running */
    static final int VCPU_RUNNING = 2;
    /** Flag for when the virtual CPU is in hypervisor mode */
    static final int VCPU_VMM = 128;
    /** Flag for when the virtual CPU is preempted */
    static final int VCPU_PREEMPT = 256;

}
