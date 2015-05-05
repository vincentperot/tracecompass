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

package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent;

import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.Attributes;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.IKernelEventHandler;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Abstract Kernel Event Handler, this will get the layout and the timestamp
 */
public abstract class AbstractKernelEventHandler implements IKernelEventHandler {

    private final IKernelAnalysisEventLayout fLayout;

    /**
     * Constructor
     *
     * @param layout
     *            the analysis layout
     */
    public AbstractKernelEventHandler(IKernelAnalysisEventLayout layout) {
        fLayout = layout;
    }

    /**
     * Get the analysis layout
     *
     * @return the analysis layout
     */
    protected IKernelAnalysisEventLayout getLayout() {
        return fLayout;
    }

    private long fTimestamp;

    /**
     * Get the timestamp of the event
     *
     * @return the timestamp in long format
     */
    protected long getTimestamp() {
        return fTimestamp;
    }

    @Override
    public boolean handleEvent(ITmfEvent event, ITmfStateSystemBuilder ss) throws AttributeNotFoundException {
        fTimestamp = event.getTimestamp().getValue();
        return true;
    }

    /**
     * Get the IRQs node
     *
     * @param ss
     *            the state system
     * @return the IRQ node quark
     */
    protected int getNodeIRQs(ITmfStateSystemBuilder ss) {
        return ss.getQuarkAbsoluteAndAdd(Attributes.RESOURCES, Attributes.IRQS);
    }

    /**
     * Get the CPUs node
     *
     * @param ss
     *            the state system
     * @return the CPU node quark
     */
    protected static int getNodeCPUs(ITmfStateSystemBuilder ss) {
        return ss.getQuarkAbsoluteAndAdd(Attributes.CPUS);
    }

    /**
     * Get the Soft IRQs node
     *
     * @param ss
     *            the state system
     * @return the Soft IRQ node quark
     */
    protected static int getNodeSoftIRQs(ITmfStateSystemBuilder ss) {
        return ss.getQuarkAbsoluteAndAdd(Attributes.RESOURCES, Attributes.SOFT_IRQS);
    }

    /**
     * Get the threads node
     *
     * @param ss
     *            the state system
     * @return the threads quark
     */
    protected static int getNodeThreads(ITmfStateSystemBuilder ss) {
        return ss.getQuarkAbsoluteAndAdd(Attributes.THREADS);
    }

}
