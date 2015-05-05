package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.softirq;

import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.irq.KernelIrqHandler;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

public class SoftIrqExit extends KernelIrqHandler {

    public SoftIrqExit(IKernelAnalysisEventLayout layout) {
        super(layout);
    }

    @Override
    public boolean handleEvent(ITmfEvent event, ITmfStateSystemBuilder ss) throws AttributeNotFoundException {
        if(!super.handleEvent(event, ss)){
            return false;
        }
        Integer softIrqId = ((Long) event.getContent().getField(getLayout().fieldVec()).getValue()).intValue();

        /* Put this SoftIRQ back to inactive (= -1) in the resource tree */
        int quark = ss.getQuarkRelativeAndAdd(getNodeSoftIRQs(ss), softIrqId.toString());
        ITmfStateValue value = TmfStateValue.nullValue();
        ss.modifyAttribute(getTimestamp(), value, quark);

        /* Set the previous process back to running */
        setProcessToRunning(ss, getTimestamp(), getCurrentThreadNode());

        /* Set the CPU status back to "busy" or "idle" */
        cpuExitInterrupt(ss, getTimestamp(), getCurrentCPUNode(), getCurrentThreadNode());
        return true;
    }

}
