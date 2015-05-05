package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.softirq;

import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.StateValues;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.AbstractKernelEventHandler;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

public class SoftIrqRaise extends AbstractKernelEventHandler {

    public SoftIrqRaise(IKernelAnalysisEventLayout layout) {
        super(layout);
    }

    @Override
    public boolean handleEvent(ITmfEvent event, ITmfStateSystemBuilder ss) throws AttributeNotFoundException {

        if (!super.handleEvent(event, ss)) {
            return false;
        }
        Integer softIrqId = ((Long) event.getContent().getField(getLayout().fieldVec()).getValue()).intValue();

        /*
         * Mark this SoftIRQ as *raised* in the resource tree. State value = -2
         */
        int quark = ss.getQuarkRelativeAndAdd(getNodeSoftIRQs(ss), softIrqId.toString());
        ITmfStateValue value = StateValues.SOFT_IRQ_RAISED_VALUE;
        ss.modifyAttribute(getTimestamp(), value, quark);
        return true;
    }
}
