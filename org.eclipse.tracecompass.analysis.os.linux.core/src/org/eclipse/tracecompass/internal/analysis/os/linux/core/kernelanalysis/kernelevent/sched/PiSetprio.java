package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.sched;

import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.Attributes;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.AbstractKernelEventHandlerWithCpu;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;

public class PiSetprio extends AbstractKernelEventHandlerWithCpu {

    public PiSetprio(IKernelAnalysisEventLayout layout) {
        super(layout);
    }

    @Override
    public boolean handleEvent(ITmfEvent event, ITmfStateSystemBuilder ss) throws AttributeNotFoundException {
        if (!super.handleEvent(event, ss)) {
            return false;
        }
        ITmfEventField content = event.getContent();
        Integer tid = ((Long) content.getField(getLayout().fieldTid()).getValue()).intValue();
        Integer prio = ((Long) content.getField(getLayout().fieldNewPrio()).getValue()).intValue();

        Integer updateThreadNode = ss.getQuarkRelativeAndAdd(getNodeThreads(ss), tid.toString());

        /* Set the current prio for the new process */
        int quark = ss.getQuarkRelativeAndAdd(updateThreadNode, Attributes.PRIO);
        ITmfStateValue value = TmfStateValue.newValueInt(prio);
        ss.modifyAttribute(getTimestamp(), value, quark);
        return true;
    }
}
