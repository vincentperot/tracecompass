/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.softirq;

import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.AbstractKernelIrqHandler;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Soft Irq exit handler
 */
public class SoftIrqExit extends AbstractKernelIrqHandler {

    /**
     * Constructor
     *
     * @param layout
     *            event layout
     */
    public SoftIrqExit(IKernelAnalysisEventLayout layout) {
        super(layout);
    }

    @Override
    public boolean handleEvent(ITmfEvent event, ITmfStateSystemBuilder ss) throws AttributeNotFoundException {
        if (!super.handleEvent(event, ss)) {
            return false;
        }
        Integer softIrqId = ((Long) event.getContent().getField(getLayout().fieldVec()).getValue()).intValue();

        /* Put this SoftIRQ back to inactive (= -1) in the resource tree */
        int quark = ss.getQuarkRelativeAndAdd(getNodeSoftIRQs(ss), softIrqId.toString());
        ITmfStateValue value = TmfStateValue.nullValue();
        ss.modifyAttribute(getTimestamp(), value, quark);

        /* Set the previous process back to running */
        setProcessToRunning(ss);

        /* Set the CPU status back to "busy" or "idle" */
        cpuExitInterrupt(ss);
        return true;
    }

}
