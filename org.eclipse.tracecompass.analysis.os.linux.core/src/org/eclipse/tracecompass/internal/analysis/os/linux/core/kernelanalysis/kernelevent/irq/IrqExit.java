package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.irq;

import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

public class IrqExit extends KernelIrqHandler {

    public IrqExit(IKernelAnalysisEventLayout layout) {
        super(layout);
    }

    @Override
    public boolean handleEvent(ITmfEvent event, ITmfStateSystemBuilder ss) throws AttributeNotFoundException {

        if (!super.handleEvent(event, ss)) {
            return false;
        }
        Integer irqId = ((Long) event.getContent().getField(getLayout().fieldIrq()).getValue()).intValue();

        /* Put this IRQ back to inactive in the resource tree */
        int quark = ss.getQuarkRelativeAndAdd(getNodeIRQs(ss), irqId.toString());
        TmfStateValue value = TmfStateValue.nullValue();
        ss.modifyAttribute(getTimestamp(), value, quark);

        /* Set the previous process back to running */
        setProcessToRunning(ss, getTimestamp(), getCurrentThreadNode());

        /* Set the CPU status back to running or "idle" */
        cpuExitInterrupt(ss, getTimestamp(), getCurrentCPUNode(), getCurrentThreadNode());
        return true;
    }
}
