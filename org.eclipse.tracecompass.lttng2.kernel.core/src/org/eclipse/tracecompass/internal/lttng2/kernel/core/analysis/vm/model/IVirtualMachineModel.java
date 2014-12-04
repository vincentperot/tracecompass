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

import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Interface that models an hypervisor. Each hypervisor (or tracing method for
 * an hypervisor) should implement this.
 *
 * @author Geneviève Bastien
 */
public interface IVirtualMachineModel {

    /**
     * Get the machine that ran this event
     *
     * @param event
     *            The trace event
     * @return The machine this event was run on or <code>null</code> if the
     *         machine is not one belonging to this model.
     */
    @Nullable
    VirtualMachine getCurrentMachine(ITmfEvent event);

    /**
     * Get a the set of events required for this model to apply.
     *
     * @return The set of required events for this model
     */
    Set<String> getRequiredEvents();

    /**
     * Get the virtual CPU that is entering hypervisor mode with this event.
     *
     * @param event
     *            The event being handled
     * @param ht
     *            The current thread this event belongs to
     * @return The virtual CPU entering hypervisor mode or <code>null</code> if
     *         the hypervisor is not being entered with this event.
     */
    @Nullable
    VirtualCPU enteringHypervisorMode(ITmfEvent event, HostThread ht);

    /**
     * Get the virtual CPU that is exiting hypervisor mode with this event.
     *
     * @param event
     *            The event being handled
     * @param ht
     *            The current thread this event belongs to
     * @return The virutal CPU exiting hypervisor mode or {@code null} if the
     *         hypervisor is not exiting with this event.
     */
    @Nullable
    VirtualCPU exitingHypervisorMode(ITmfEvent event, HostThread ht);

    /**
     * Get the virtual CPU from a guest that corresponds to a specific thread
     * from the host
     *
     * @param event
     *            The event being handled
     * @param ht
     *            The current thread this event belongs to
     * @return The virtual CPU corresponding to this thread
     */
    @Nullable
    VirtualCPU getVirtualCpu(HostThread ht);

    /**
     * Handles the event
     *
     * @param event
     */
    void eventHandle(ITmfEvent event);

}
