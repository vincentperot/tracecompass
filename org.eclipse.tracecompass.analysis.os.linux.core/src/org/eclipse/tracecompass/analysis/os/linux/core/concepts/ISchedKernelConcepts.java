/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.concepts;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.concept.IEventConcept;

/**
 * Interface for all concepts related to an operating system scheduler
 *
 * @author Geneviève Bastien
 * @noimplement
 */
public interface ISchedKernelConcepts {

    /**
     * This concept represents there is a switch of the active process on the
     * CPU
     */
    public interface ISchedSwitchConcept extends IEventConcept {

        Integer getPrevTid(ITmfEvent event);

        Long getPrevState(ITmfEvent event);

        String getNextProcName(ITmfEvent event);

        Integer getNextTid(ITmfEvent event);

    }

    /**
     * This concept represents when a thread starts actively waiting for the CPU
     */
    public interface ISchedWakeupConcept extends IEventConcept {

        Integer getPrevTid(ITmfEvent event);

    }

}
