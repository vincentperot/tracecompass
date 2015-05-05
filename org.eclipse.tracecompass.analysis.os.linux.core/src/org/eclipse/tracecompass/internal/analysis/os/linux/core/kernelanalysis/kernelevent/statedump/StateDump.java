package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.statedump;

import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.Attributes;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.StateValues;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.LinuxValues;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.AbstractKernelEventHandler;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;

public class StateDump extends AbstractKernelEventHandler {
    public StateDump(IKernelAnalysisEventLayout layout) {
        super(layout);
    }

    @Override
    public boolean handleEvent(ITmfEvent event, ITmfStateSystemBuilder ss) throws AttributeNotFoundException {
        if (!super.handleEvent(event, ss)) {
            return false;
        }
        ITmfEventField content = event.getContent();
        int tid = ((Long) content.getField("tid").getValue()).intValue(); //$NON-NLS-1$
        int pid = ((Long) content.getField("pid").getValue()).intValue(); //$NON-NLS-1$
        int ppid = ((Long) content.getField("ppid").getValue()).intValue(); //$NON-NLS-1$
        int status = ((Long) content.getField("status").getValue()).intValue(); //$NON-NLS-1$
        String name = (String) content.getField("name").getValue(); //$NON-NLS-1$
        /*
         * "mode" could be interesting too, but it doesn't seem to be populated
         * with anything relevant for now.
         */

        int curThreadNode = ss.getQuarkRelativeAndAdd(getNodeThreads(ss), String.valueOf(tid));
        ITmfStateValue value;
        /* Set the process' name */
        int quark = ss.getQuarkRelativeAndAdd(curThreadNode, Attributes.EXEC_NAME);
        if (ss.queryOngoingState(quark).isNull()) {
            /* If the value didn't exist previously, set it */
            value = TmfStateValue.newValueString(name);
            ss.modifyAttribute(getTimestamp(), value, quark);
        }

        /* Set the process' PPID */
        quark = ss.getQuarkRelativeAndAdd(curThreadNode, Attributes.PPID);
        if (ss.queryOngoingState(quark).isNull()) {
            if (pid == tid) {
                /* We have a process. Use the 'PPID' field. */
                value = TmfStateValue.newValueInt(ppid);
            } else {
                /* We have a thread, use the 'PID' field for the parent. */
                value = TmfStateValue.newValueInt(pid);
            }
            ss.modifyAttribute(getTimestamp(), value, quark);
        }

        /* Set the process' status */
        quark = ss.getQuarkRelativeAndAdd(curThreadNode, Attributes.STATUS);
        if (ss.queryOngoingState(quark).isNull()) {
            switch (status) {
            case LinuxValues.STATEDUMP_PROCESS_STATUS_WAIT_CPU:
                value = StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE;
                break;
            case LinuxValues.STATEDUMP_PROCESS_STATUS_WAIT:
                /*
                 * We have no information on what the process is waiting on
                 * (unlike a sched_switch for example), so we will use the
                 * WAIT_UNKNOWN state instead of the "normal" WAIT_BLOCKED
                 * state.
                 */
                value = StateValues.PROCESS_STATUS_WAIT_UNKNOWN_VALUE;
                break;
            default:
                value = StateValues.PROCESS_STATUS_UNKNOWN_VALUE;
            }
            ss.modifyAttribute(getTimestamp(), value, quark);
        }
        return true;
    }
}
