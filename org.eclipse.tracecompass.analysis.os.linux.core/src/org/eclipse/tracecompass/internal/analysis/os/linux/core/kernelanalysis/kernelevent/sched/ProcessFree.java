package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.sched;

import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.AbstractKernelEventHandler;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

public class ProcessFree extends AbstractKernelEventHandler {

    public ProcessFree(IKernelAnalysisEventLayout layout) {
        super(layout);
    }

    @Override
    public boolean handleEvent(ITmfEvent event, ITmfStateSystemBuilder ss) throws AttributeNotFoundException {
        if (!super.handleEvent(event, ss)) {
            return false;
        }

        Integer tid = ((Long) event.getContent().getField(getLayout().fieldTid()).getValue()).intValue();
        /*
         * Remove the process and all its sub-attributes from the current state
         */
        int quark = ss.getQuarkRelativeAndAdd(getNodeThreads(ss), tid.toString());
        ss.removeAttribute(getTimestamp(), quark);
        return true;
    }
}
