package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.syscall;

import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.Attributes;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.StateValues;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.AbstractKernelEventHandlerWithCpuAndThread;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

public class SysEntry extends AbstractKernelEventHandlerWithCpuAndThread {

    public SysEntry(IKernelAnalysisEventLayout layout) {
        super(layout);
    }

    @Override
    public boolean handleEvent(ITmfEvent event, ITmfStateSystemBuilder ss) throws AttributeNotFoundException {
        if (!super.handleEvent(event, ss)) {
            return false;
        }
        /* Assign the new system call to the process */
        int quark = ss.getQuarkRelativeAndAdd(getCurrentThreadNode(), Attributes.SYSTEM_CALL);
        ITmfStateValue value = TmfStateValue.newValueString(event.getName());
        ss.modifyAttribute(getTimestamp(), value, quark);

        /* Put the process in system call mode */
        quark = ss.getQuarkRelativeAndAdd(getCurrentThreadNode(), Attributes.STATUS);
        value = StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE;
        ss.modifyAttribute(getTimestamp(), value, quark);

        /* Put the CPU in system call (kernel) mode */
        quark = ss.getQuarkRelativeAndAdd(getCurrentCPUNode(), Attributes.STATUS);
        value = StateValues.CPU_STATUS_RUN_SYSCALL_VALUE;
        ss.modifyAttribute(getTimestamp(), value, quark);
        return true;
    }

}
