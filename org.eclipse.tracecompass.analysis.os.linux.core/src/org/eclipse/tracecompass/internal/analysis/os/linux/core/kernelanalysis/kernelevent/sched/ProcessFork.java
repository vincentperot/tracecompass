package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.sched;

import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.Attributes;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.StateValues;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.AbstractKernelEventHandler;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;

public class ProcessFork extends AbstractKernelEventHandler {

    public ProcessFork(IKernelAnalysisEventLayout layout) {
        super(layout);
    }

    @Override
    public boolean handleEvent(ITmfEvent event, ITmfStateSystemBuilder ss) throws AttributeNotFoundException {
        if(!super.handleEvent(event, ss)){
            return false;
        }
        ITmfEventField content = event.getContent();
        // String parentProcessName = (String) event.getFieldValue("parent_comm");
        String childProcessName = (String) content.getField(getLayout().fieldChildComm()).getValue();
        // assert ( parentProcessName.equals(childProcessName) );

        Integer parentTid = ((Long) content.getField(getLayout().fieldParentTid()).getValue()).intValue();
        Integer childTid = ((Long) content.getField(getLayout().fieldChildTid()).getValue()).intValue();

        Integer parentTidNode = ss.getQuarkRelativeAndAdd(getNodeThreads(ss), parentTid.toString());
        Integer childTidNode = ss.getQuarkRelativeAndAdd(getNodeThreads(ss), childTid.toString());

        /* Assign the PPID to the new process */
        int quark = ss.getQuarkRelativeAndAdd(childTidNode, Attributes.PPID);
        ITmfStateValue value = TmfStateValue.newValueInt(parentTid);
        ss.modifyAttribute(getTimestamp(), value, quark);

        /* Set the new process' exec_name */
        quark = ss.getQuarkRelativeAndAdd(childTidNode, Attributes.EXEC_NAME);
        value = TmfStateValue.newValueString(childProcessName);
        ss.modifyAttribute(getTimestamp(), value, quark);

        /* Set the new process' status */
        quark = ss.getQuarkRelativeAndAdd(childTidNode, Attributes.STATUS);
        value = StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE;
        ss.modifyAttribute(getTimestamp(), value, quark);

        /* Set the process' syscall name, to be the same as the parent's */
        quark = ss.getQuarkRelativeAndAdd(parentTidNode, Attributes.SYSTEM_CALL);
        value = ss.queryOngoingState(quark);
        if (value.isNull()) {
            /*
             * Maybe we were missing info about the parent? At least we
             * will set the child right. Let's suppose "sys_clone".
             */
            value = TmfStateValue.newValueString(getLayout().eventSyscallEntryPrefix() + IKernelAnalysisEventLayout.INITIAL_SYSCALL_NAME);
        }
        quark = ss.getQuarkRelativeAndAdd(childTidNode, Attributes.SYSTEM_CALL);
        ss.modifyAttribute(getTimestamp(), value, quark);
        return true;
    }
}
