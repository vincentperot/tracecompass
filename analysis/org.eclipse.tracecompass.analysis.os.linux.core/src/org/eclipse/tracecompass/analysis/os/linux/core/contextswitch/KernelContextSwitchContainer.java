/*******************************************************************************
 * Copyright (c) 2015 Ericsson Canada
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexis Cabana-Loriaux - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.contextswitch;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Object used to represent data inside the ContextSwitchesModel. This class can
 * be overriden to offer another way of holding the data of the context switches
 * of a Kernel Trace. The container is queried in O(n) with the number of CPUs.
 *
 * @author Alexis Cabana-Loriaux
 *
 */
class KernelContextSwitchContainer {
    private Multimap<TmfTimeRange, Entry<Integer, Long>> fContextSwitchesOfCores = HashMultimap.create();

    public void addCPUContextSwitches(@NonNull Integer cpuNumber, @NonNull TmfTimeRange timeRange, Long nbCS) {
        if (nbCS < 0) {
            return;
        }
        fContextSwitchesOfCores.get(timeRange).add(new AbstractMap.SimpleEntry<>(cpuNumber, nbCS));
    }

    public void clear() {
        fContextSwitchesOfCores.clear();
    }

    public Collection<Entry<Integer, Long>> getAllCPUsNbContextSwitch(TmfTimeRange timerange) {
        return fContextSwitchesOfCores.get(timerange);
    }

    public Long getNbContextSwitch(TmfTimeRange timerange, Integer cpuNb) {

        for (Entry<Integer, Long> entry : getAllCPUsNbContextSwitch(timerange)) {
            if (entry.getKey() == cpuNb) {
                /* Only one entry per CPU */
                return entry.getValue();
            }
        }
        return null;
    }
}
