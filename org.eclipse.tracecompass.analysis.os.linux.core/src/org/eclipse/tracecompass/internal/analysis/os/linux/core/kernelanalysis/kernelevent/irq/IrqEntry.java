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

package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.irq;

import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.Attributes;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.StateValues;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.AbstractKernelIrqHandler;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Irq Entry Handler
 */
public class IrqEntry extends AbstractKernelIrqHandler {

    /**
     * Constructor
     * @param layout event layout
     */
    public IrqEntry(IKernelAnalysisEventLayout layout) {
        super(layout);
    }

    @Override
    public boolean handleEvent(ITmfEvent event, ITmfStateSystemBuilder ss) throws AttributeNotFoundException {

        if (!super.handleEvent(event, ss)) {
            return false;
        }
        Integer irqId = ((Long) event.getContent().getField(getLayout().fieldIrq()).getValue()).intValue();

        /*
         * Mark this IRQ as active in the resource tree. The state value = the
         * CPU on which this IRQ is sitting
         */
        int quark = ss.getQuarkRelativeAndAdd(getNodeIRQs(ss), irqId.toString());
        ITmfStateValue value = TmfStateValue.newValueInt(getCpu().intValue());
        ss.modifyAttribute(getTimestamp(), value, quark);

        /* Change the status of the running process to interrupted */
        quark = ss.getQuarkRelativeAndAdd(getCurrentThreadNode(), Attributes.STATUS);
        value = StateValues.PROCESS_STATUS_INTERRUPTED_VALUE;
        ss.modifyAttribute(getTimestamp(), value, quark);

        /* Change the status of the CPU to interrupted */
        quark = ss.getQuarkRelativeAndAdd(getCurrentCPUNode(), Attributes.STATUS);
        value = StateValues.CPU_STATUS_IRQ_VALUE;
        ss.modifyAttribute(getTimestamp(), value, quark);
        return true;
    }

}
