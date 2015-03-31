/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Sébastien Lorrain
 *   Francis Jolivet
 ******************************************************************************/
package org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.container;

import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.StateValues;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;

/**
 * Represent a task that changed the CPU state at a given time.
 *
 * @author Sebastien Lorrain
 * @author Francis Jolivet
 *
 */
public class TaskCPUEvent {

    private final long fTs;
    private final int fTid;
    private final ITmfStateValue fCpuState;
    private final int fCpuId;

    /**
     * CTor
     * @param ts
     *      The timestamp
     * @param tid
     *      The task tid
     * @param status
     *      The CPU status occasionned by the task
     * @param cpuID
     *      The CPU numerical ID
     */
    public TaskCPUEvent(long ts, int tid, int status, int cpuID)
    {
        this.fTs = ts;
        this.fTid = tid;
        this.fCpuId = cpuID;

        switch(status)
        {
        case StateValues.PROCESS_STATUS_INTERRUPTED:
        case StateValues.PROCESS_STATUS_UNKNOWN:
        case StateValues.PROCESS_STATUS_WAIT_FOR_CPU:
            this.fCpuState = ContainerCpuState.CPU_STATUS_IDLE_VALUE;
            break;
        case StateValues.PROCESS_STATUS_RUN_SYSCALL:
        case StateValues.PROCESS_STATUS_RUN_USERMODE:
            this.fCpuState = ContainerCpuState.CPU_STATUS_RUNNING_VALUE;
            break;
        default:
            this.fCpuState = ContainerCpuState.CPU_STATUS_IDLE_VALUE;
            break;
        }
    }


    @SuppressWarnings("javadoc")
    public long getTs() {
        return fTs;
    }

    @SuppressWarnings("javadoc")
    public int getTid() {
        return fTid;
    }

    @SuppressWarnings("javadoc")
    public ITmfStateValue getCpuState() {
        return fCpuState;
    }

    @SuppressWarnings("javadoc")
    public int getCpuId() {
        return fCpuId;
    }

}
