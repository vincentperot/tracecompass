package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent;

import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.Attributes;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.StateValues;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

public class AbstractKernelEventHandlerWithCpuAndThread extends AbstractKernelEventHandlerWithCpu {


    private int fCurrentThreadNode;

    public AbstractKernelEventHandlerWithCpuAndThread(IKernelAnalysisEventLayout layout) {
        super(layout);
    }

    protected int getCurrentThreadNode() {
        return fCurrentThreadNode;
    }

    @Override
    public boolean handleEvent(ITmfEvent event, ITmfStateSystemBuilder ss) throws AttributeNotFoundException {
        if (!super.handleEvent(event, ss)) {
            return false;
        }
        /*
         * Shortcut for the "current thread" attribute node. It requires
         * querying the current CPU's current thread.
         */
        int quark = ss.getQuarkRelativeAndAdd(getCurrentCPUNode(), Attributes.CURRENT_THREAD);
        ITmfStateValue value = ss.queryOngoingState(quark);
        int thread = value.isNull() ? -1 : value.unboxInt();
        fCurrentThreadNode = ss.getQuarkRelativeAndAdd(getNodeThreads(ss), String.valueOf(thread));
        return true;
    }


    /**
     * When we want to set a process back to a "running" state, first check
     * its current System_call attribute. If there is a system call active, we
     * put the process back in the syscall state. If not, we put it back in
     * user mode state.
     */
    protected static void setProcessToRunning(ITmfStateSystemBuilder ssb, long ts, int currentThreadNode)
            throws AttributeNotFoundException, TimeRangeException,
            StateValueTypeException {
        int quark;
        ITmfStateValue value;

        quark = ssb.getQuarkRelativeAndAdd(currentThreadNode, Attributes.SYSTEM_CALL);
        if (ssb.queryOngoingState(quark).isNull()) {
            /* We were in user mode before the interruption */
            value = StateValues.PROCESS_STATUS_RUN_USERMODE_VALUE;
        } else {
            /* We were previously in kernel mode */
            value = StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE;
        }
        quark = ssb.getQuarkRelativeAndAdd(currentThreadNode, Attributes.STATUS);
        ssb.modifyAttribute(ts, value, quark);
    }
}
