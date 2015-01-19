/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mohamad Gebai - Initial API and implementation
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.ui.views.vm.vcpuview;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.StateValues;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.VcpuStateValues;
import org.eclipse.tracecompass.internal.lttng2.kernel.ui.views.vm.vcpuview.VirtualMachineCommon.Type;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;

/**
 * Presentation provider for the Virtual Machine view, based on the generic TMF
 * presentation provider.
 *
 * @author Mohamad Gebai
 */
public class VirtualMachinePresentationProvider extends TimeGraphPresentationProvider {

    /*
     * TODO: Some of it is copy-pasted from the control flow presentation
     * provider because it actually is the same data as from the control flow
     * view. Ideally, we should reuse what is there instead of rewriting it here
     */
    private enum State {
        UNKNOWN(new RGB(100, 100, 100)),
        IDLE(new RGB(200, 200, 200)),
        USERMODE(new RGB(0, 200, 0)),
        WAIT_VMM(new RGB(200, 0, 0)),
        VCPU_PREEMPTED(new RGB(120, 40, 90)),
        THREAD_UNKNOWN(new RGB(100, 100, 100)),
        THREAD_WAIT_BLOCKED(new RGB(200, 200, 0)),
        THREAD_WAIT_FOR_CPU(new RGB(200, 100, 0)),
        THREAD_USERMODE(new RGB(0, 200, 0)),
        THREAD_SYSCALL(new RGB(0, 0, 200)),
        THREAD_INTERRUPTED(new RGB(200, 0, 100));

        public final RGB rgb;

        private State(RGB rgb) {
            this.rgb = rgb;
        }
    }

    /**
     * Default constructor
     */
    public VirtualMachinePresentationProvider() {
        super();
    }

    private static State[] getStateValues() {
        return State.values();
    }

    private static @Nullable State getEventState(TimeEvent event) {
        if (event.hasValue()) {
            VirtualMachineViewEntry entry = (VirtualMachineViewEntry) event.getEntry();
            int value = event.getValue();

            if (entry.getType() == Type.VCPU) {
                if ((value & VcpuStateValues.VCPU_PREEMPT) > 0) {
                    return State.VCPU_PREEMPTED;
                } else if ((value & VcpuStateValues.VCPU_VMM) > 0) {
                    return State.WAIT_VMM;
                } else if (value == 2) {
                    return State.USERMODE;
                } else if (value == 1) {
                    return State.IDLE;
                } else {
                    return State.UNKNOWN;
                }
            } else if (entry.getType() == Type.THREAD) {
                if (value == VcpuStateValues.VCPU_PREEMPT) {
                    return null;
                }
                switch (value) {
                case StateValues.PROCESS_STATUS_RUN_USERMODE:
                    return State.THREAD_USERMODE;
                case StateValues.PROCESS_STATUS_RUN_SYSCALL:
                    return State.THREAD_SYSCALL;
                case StateValues.PROCESS_STATUS_WAIT_FOR_CPU:
                    return State.THREAD_WAIT_FOR_CPU;
                case StateValues.PROCESS_STATUS_WAIT_BLOCKED:
                    return State.THREAD_WAIT_BLOCKED;
                case StateValues.PROCESS_STATUS_INTERRUPTED:
                    return State.THREAD_INTERRUPTED;
                case StateValues.PROCESS_STATUS_UNKNOWN:
                    return State.THREAD_UNKNOWN;
                default:
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    public int getStateTableIndex(@Nullable ITimeEvent event) {
        if (event == null) {
            return TRANSPARENT;
        }
        State state = getEventState((TimeEvent) event);
        if (state != null) {
            return state.ordinal();
        }
        if (event instanceof NullTimeEvent) {
            return INVISIBLE;
        }
        return TRANSPARENT;
    }

    @Override
    public StateItem[] getStateTable() {
        State[] states = getStateValues();
        StateItem[] stateTable = new StateItem[states.length];
        for (int i = 0; i < stateTable.length; i++) {
            State state = states[i];
            stateTable[i] = new StateItem(state.rgb, state.toString());
        }
        return stateTable;
    }

    @Override
    public @Nullable String getEventName(@Nullable ITimeEvent event) {
        if (event == null) {
            return null;
        }
        State state = getEventState((TimeEvent) event);
        if (state != null) {
            return state.toString();
        }
        if (event instanceof NullTimeEvent) {
            return null;
        }
        return Messages.VmView_multipleStates;
    }

    @Override
    public Map<String, String> getEventHoverToolTipInfo(@Nullable ITimeEvent event, long hoverTime) {

        Map<String, String> retMap = new LinkedHashMap<>();

        /*
         * TODO: implement me to add the data for the VCPU overlay status.
         * Events can actually overlap so how will this method be called, what
         * event will be in parameter?
         */

        return retMap;
    }

    @Override
    public void postDrawEvent(@Nullable ITimeEvent event, @Nullable Rectangle bounds, @Nullable GC gc) {
        if (bounds == null || gc == null) {
            return;
        }
        boolean visible = bounds.width == 0 ? false : true;
        if (!visible) {
            return;
        }
        if (!(event instanceof TimeEvent)) {
            return;
        }
        TimeEvent ev = (TimeEvent) event;
        /*
         * FIXME: There seems to be a bug when multiple events should be drawn
         * under a alpha event. Why? Works well for "quiet" traces though.
         */
        if (ev.hasValue()) {
            VirtualMachineViewEntry entry = (VirtualMachineViewEntry) event.getEntry();

            if (entry.getType() == Type.THREAD) {
                int value = ev.getValue();
                if ((value & VcpuStateValues.VCPU_PREEMPT) != 0) {
                    /*
                     * If the status was preempted at this time, draw an alpha
                     * over this state
                     */
                    Color white = Display.getDefault().getSystemColor(SWT.COLOR_RED);

                    int alpha = gc.getAlpha();
                    Color background = gc.getBackground();
                    // fill all rect area
                    gc.setBackground(white);
                    gc.setAlpha(70);
                    gc.fillRectangle(bounds);

                    gc.setBackground(background);
                    gc.setAlpha(alpha);
                }
            }
        }
    }

    @Override
    public void postDrawEntry(@Nullable ITimeGraphEntry entry, @Nullable Rectangle bounds, @Nullable GC gc) {

    }
}