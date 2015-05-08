/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.irq.IrqEntry;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.irq.IrqExit;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.sched.PiSetprio;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.sched.ProcessExit;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.sched.ProcessFork;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.sched.ProcessFree;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.sched.Switch;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.sched.Wakeup;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.softirq.SoftIrqEntry;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.softirq.SoftIrqExit;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.softirq.SoftIrqRaise;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.statedump.StateDump;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.syscall.SysEntry;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelanalysis.kernelevent.syscall.SysExit;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.ImmutableMap;

/**
 * This is the state change input plugin for TMF's state system which handles
 * the LTTng 2.0 kernel traces in CTF format.
 *
 * It uses the reference handler defined in CTFKernelHandler.java.
 *
 * @author alexmont
 *
 */
public class KernelStateProvider extends AbstractTmfStateProvider {

    // ------------------------------------------------------------------------
    // Static fields
    // ------------------------------------------------------------------------

    /**
     * Version number of this state provider. Please bump this if you modify the
     * contents of the generated state history in some way.
     */
    private static final int VERSION = 7;

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    private final Map<String, IKernelEventHandler> fEventNames;
    private final IKernelAnalysisEventLayout fLayout;
    private final IKernelEventHandler fSysEntry;
    private final IKernelEventHandler fSysExit;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Instantiate a new state provider plugin.
     *
     * @param trace
     *            The LTTng 2.0 kernel trace directory
     * @param layout
     *            The event layout to use for this state provider. Usually
     *            depending on the tracer implementation.
     */
    public KernelStateProvider(ITmfTrace trace, IKernelAnalysisEventLayout layout) {
        super(trace, "Kernel"); //$NON-NLS-1$
        fLayout = layout;
        fEventNames = buildEventNames(layout);
        fSysEntry = new SysEntry(fLayout);
        fSysExit = new SysExit(fLayout);
    }

    // ------------------------------------------------------------------------
    // Event names management
    // ------------------------------------------------------------------------

    private static Map<String, IKernelEventHandler> buildEventNames(IKernelAnalysisEventLayout layout) {
        ImmutableMap.Builder<String, IKernelEventHandler> builder = ImmutableMap.builder();

        builder.put(layout.eventIrqHandlerEntry(), new IrqEntry(layout));
        builder.put(layout.eventIrqHandlerExit(), new IrqExit(layout));
        builder.put(layout.eventSoftIrqEntry(), new SoftIrqEntry(layout));
        builder.put(layout.eventSoftIrqExit(), new SoftIrqExit(layout));
        builder.put(layout.eventSoftIrqRaise(), new SoftIrqRaise(layout));
        builder.put(layout.eventSchedSwitch(), new Switch(layout));
        builder.put(layout.eventSchedPiSetprio(), new PiSetprio(layout));
        builder.put(layout.eventSchedProcessFork(), new ProcessFork(layout));
        builder.put(layout.eventSchedProcessExit(), new ProcessExit(layout));
        builder.put(layout.eventSchedProcessFree(), new ProcessFree(layout));

        final String eventStatedumpProcessState = layout.eventStatedumpProcessState();
        if (eventStatedumpProcessState != null) {
            builder.put(eventStatedumpProcessState, new StateDump(layout));
        }

        for (String eventSchedWakeup : layout.eventsSchedWakeup()) {
            builder.put(eventSchedWakeup, new Wakeup(layout));
        }

        return checkNotNull(builder.build());
    }

    // ------------------------------------------------------------------------
    // IStateChangeInput
    // ------------------------------------------------------------------------

    @Override
    public int getVersion() {
        return VERSION;
    }

    @Override
    public void assignTargetStateSystem(ITmfStateSystemBuilder ssb) {
        /* We can only set up the locations once the state system is assigned */
        super.assignTargetStateSystem(ssb);
    }

    @Override
    public KernelStateProvider getNewInstance() {
        return new KernelStateProvider(this.getTrace(), fLayout);
    }

    @Override
    protected void eventHandle(@Nullable ITmfEvent event) {
        if (event == null) {
            return;
        }

        final String eventName = event.getName();

        try {
            final ITmfStateSystemBuilder ss = checkNotNull(getStateSystemBuilder());

            /*
             * Feed event to the history system if it's known to cause a state
             * transition.
             */
            IKernelEventHandler handler = fEventNames.get(eventName);
            if (handler == null) {
                if (eventName.startsWith(fLayout.eventSyscallExitPrefix())) {
                    handler = fSysExit;
                }
                else if (eventName.startsWith(fLayout.eventSyscallEntryPrefix())
                        || eventName.startsWith(fLayout.eventCompatSyscallEntryPrefix())) {
                    handler = fSysEntry;
                }
            }
            if (handler != null) {
                handler.handleEvent(event, ss);
            }

        } catch (AttributeNotFoundException ae) {
            /*
             * This would indicate a problem with the logic of the manager here,
             * so it shouldn't happen.
             */
            ae.printStackTrace();

        } catch (TimeRangeException tre) {
            /*
             * This would happen if the events in the trace aren't ordered
             * chronologically, which should never be the case ...
             */
            System.err.println("TimeRangeExcpetion caught in the state system's event manager."); //$NON-NLS-1$
            System.err.println("Are the events in the trace correctly ordered?"); //$NON-NLS-1$
            tre.printStackTrace();

        } catch (StateValueTypeException sve) {
            /*
             * This would happen if we were trying to push/pop attributes not of
             * type integer. Which, once again, should never happen.
             */
            sve.printStackTrace();
        }
    }

}
