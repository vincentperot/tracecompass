/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Sébastien Lorrain - Initial API and implementation
 *   Francis Jolivet - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.container;

/**
 * Represent a task that changed the CPU state at a given time. Also contains
 * information about the PID namespace of that tasks
 *
 * @author Sebastien Lorrain
 * @author Francis Jolivet
 */
public class TaskContainerEvent extends TaskCPUEvent {

    private final int fVtid;
    private final int fPpid;
    private final int fNsInode;

    /**
     * CTor
     *
     * @param ts
     *            The timestamp
     * @param tid
     *            The task tid
     * @param vTID
     *            The task VTID
     * @param pPID
     *            The task parent pid
     * @param status
     *            The CPU status occasionned by the task
     * @param cpuID
     *            The CPU numerical ID
     * @param nsInode
     *            The INode of the task PID namespace
     */
    public TaskContainerEvent(long ts, int tid, int vTID, int pPID, int status, int cpuID, int nsInode) {
        super(ts, tid, status, cpuID);
        fVtid = vTID;
        fPpid = pPID;
        fNsInode = nsInode;
    }

    @SuppressWarnings("javadoc")
    public int getVtid() {
        return fVtid;
    }

    @SuppressWarnings("javadoc")
    public int getPpid() {
        return fPpid;
    }

    @SuppressWarnings("javadoc")
    public int getNsInode() {
        return fNsInode;
    }
}