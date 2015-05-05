package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent;

import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.Attributes;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.IKernelEventHandler;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

public abstract class AbstractKernelEventHandler implements IKernelEventHandler {

    private final IKernelAnalysisEventLayout fLayout;

    public AbstractKernelEventHandler(IKernelAnalysisEventLayout layout) {
        fLayout = layout;
    }

    protected IKernelAnalysisEventLayout getLayout() {
        return fLayout;
    }

    private long fTimestamp;

    protected long getTimestamp() {
        return fTimestamp;
    }

    @Override
    public boolean handleEvent(ITmfEvent event, ITmfStateSystemBuilder ss) throws AttributeNotFoundException {
        fTimestamp = event.getTimestamp().getValue();
        return true;
    }

    protected int getNodeIRQs(ITmfStateSystemBuilder ssb) {
        return ssb.getQuarkAbsoluteAndAdd(Attributes.RESOURCES, Attributes.IRQS);
    }

    protected static int getNodeCPUs(ITmfStateSystemBuilder ssb) {
        return ssb.getQuarkAbsoluteAndAdd(Attributes.CPUS);
    }

    protected static int getNodeSoftIRQs(ITmfStateSystemBuilder ssb) {
        return ssb.getQuarkAbsoluteAndAdd(Attributes.RESOURCES, Attributes.SOFT_IRQS);
    }

    protected static int getNodeThreads(ITmfStateSystemBuilder ssb) {
        return ssb.getQuarkAbsoluteAndAdd(Attributes.THREADS);
    }

}
