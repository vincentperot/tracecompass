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

public class Wakeup extends AbstractKernelEventHandler {

    public Wakeup(IKernelAnalysisEventLayout layout) {
        super(layout);
    }

    @Override
    public boolean handleEvent(ITmfEvent event, ITmfStateSystemBuilder ss) throws AttributeNotFoundException {
        if (!super.handleEvent(event, ss)) {
            return false;
        }
        final int tid = ((Long) event.getContent().getField(getLayout().fieldTid()).getValue()).intValue();
        final int prio = ((Long) event.getContent().getField(getLayout().fieldPrio()).getValue()).intValue();
        final int threadNode = ss.getQuarkRelativeAndAdd(getNodeThreads(ss), String.valueOf(tid));

        /*
         * The process indicated in the event's payload is now ready to run.
         * Assign it to the "wait for cpu" state, but only if it was not already
         * running.
         */
        int quark = ss.getQuarkRelativeAndAdd(threadNode, Attributes.STATUS);
        int status = ss.queryOngoingState(quark).unboxInt();
        ITmfStateValue value = null;
        if (status != StateValues.PROCESS_STATUS_RUN_SYSCALL &&
                status != StateValues.PROCESS_STATUS_RUN_USERMODE) {
            value = StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE;
            ss.modifyAttribute(getTimestamp(), value, quark);
        }

        /*
         * When a user changes a threads prio (e.g. with pthread_setschedparam),
         * it shows in ftrace with a sched_wakeup.
         */
        quark = ss.getQuarkRelativeAndAdd(threadNode, Attributes.PRIO);
        value = TmfStateValue.newValueInt(prio);
        ss.modifyAttribute(getTimestamp(), value, quark);
        return true;
    }
}
