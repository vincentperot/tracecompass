package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.softirq;

import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.Attributes;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.StateValues;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.AbstractKernelEventHandlerWithCpuAndThread;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

public class SoftIrqEntry extends AbstractKernelEventHandlerWithCpuAndThread {

    public SoftIrqEntry(IKernelAnalysisEventLayout layout) {
        super(layout);
    }

    @Override
    public boolean handleEvent(ITmfEvent event, ITmfStateSystemBuilder ss) throws AttributeNotFoundException {
        if (!super.handleEvent(event, ss)) {
            return false;
        }

        Integer softIrqId = ((Long) event.getContent().getField(getLayout().fieldVec()).getValue()).intValue();

        /*
         * Mark this SoftIRQ as active in the resource tree. The state value =
         * the CPU on which this SoftIRQ is processed
         */
        int quark = ss.getQuarkRelativeAndAdd(getNodeSoftIRQs(ss), softIrqId.toString());
        ITmfStateValue value = TmfStateValue.newValueInt(getCpu().intValue());
        ss.modifyAttribute(getTimestamp(), value, quark);

        /* Change the status of the running process to interrupted */
        quark = ss.getQuarkRelativeAndAdd(getCurrentThreadNode(), Attributes.STATUS);
        value = StateValues.PROCESS_STATUS_INTERRUPTED_VALUE;
        ss.modifyAttribute(getTimestamp(), value, quark);

        /* Change the status of the CPU to interrupted */
        quark = ss.getQuarkRelativeAndAdd(getCurrentCPUNode(), Attributes.STATUS);
        value = StateValues.CPU_STATUS_SOFTIRQ_VALUE;
        ss.modifyAttribute(getTimestamp(), value, quark);
        return true;
    }
}
