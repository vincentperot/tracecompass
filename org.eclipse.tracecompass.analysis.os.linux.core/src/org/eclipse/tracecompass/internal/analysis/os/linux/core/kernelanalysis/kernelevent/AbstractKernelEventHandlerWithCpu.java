package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent;

import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

public abstract class AbstractKernelEventHandlerWithCpu extends AbstractKernelEventHandler {

    public AbstractKernelEventHandlerWithCpu(IKernelAnalysisEventLayout layout) {
        super(layout);
    }

    private Integer fCpu;
    private int fCurrentCPUNode;

    protected Integer getCpu() {
        return fCpu;
    }

    protected int getCurrentCPUNode() {
        return fCurrentCPUNode;
    }

    @Override
    public boolean handleEvent(ITmfEvent event, ITmfStateSystemBuilder ss) throws AttributeNotFoundException {
        super.handleEvent(event, ss);
        Object cpuObj = TmfTraceUtils.resolveEventAspectOfClassForEvent(event.getTrace(), TmfCpuAspect.class, event);
        if (cpuObj == null) {
            /* We couldn't find any CPU information, ignore this event */
            return false;
        }
        fCpu = (Integer) cpuObj;
        /* Shortcut for the "current CPU" attribute node */
        fCurrentCPUNode = ss.getQuarkRelativeAndAdd(getNodeCPUs(ss), fCpu.toString());
        return true;
    }



}
