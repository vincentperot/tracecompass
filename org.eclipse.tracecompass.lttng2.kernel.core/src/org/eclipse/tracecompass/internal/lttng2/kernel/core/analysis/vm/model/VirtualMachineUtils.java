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



/**
 *
 *
 * @author Geneviève Bastien
 */
public final class VirtualMachineUtils {

    private VirtualMachineUtils() {

    }

//    private static @Nullable HostTid getCurrentHostTid(ITmfEvent event) {
//        String hostId = event.getTrace().getHostId();
//        if (hostId == null) {
//            return null;
//        }
//        Integer currentTid = runningProcesses.get(hostId, Integer.parseInt(event.getSource()));
//        if (currentTid == null) {
//            return null;
//        }
//        HostTid ht = new HostTid(hostId, currentTid);
//        return ht;
//    }
}
