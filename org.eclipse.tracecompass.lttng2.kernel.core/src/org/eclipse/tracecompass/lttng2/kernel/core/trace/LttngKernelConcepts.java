/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.trace;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.concepts.ISchedKernelConceptVisitor;
import org.eclipse.tracecompass.analysis.os.linux.core.concepts.ISchedKernelConcepts.ISchedSwitchConcept;
import org.eclipse.tracecompass.analysis.os.linux.core.concepts.ISchedKernelConcepts.ISchedWakeupConcept;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.concept.IEventConceptVisitor;

import com.google.common.collect.ImmutableList;

public interface LttngKernelConcepts {

    class SchedConceptUtils {
        private static ITmfEventField getField(ITmfEvent event, String name) {
            ITmfEventField content = event.getContent();
            ITmfEventField field = content.getField(name);
            return field;
        }
    }

    public class LttngSchedSwitchConcept implements ISchedSwitchConcept {

        private static final String SCHED_SWITCH = "sched_switch"; //$NON-NLS-1$
        private static final String PREV_TID = "prev_tid"; //$NON-NLS-1$
        private static final String PREV_STATE = "prev_state"; //$NON-NLS-1$
        private static final String NEXT_COMM = "next_comm"; //$NON-NLS-1$
        private static final String NEXT_TID = "next_tid"; //$NON-NLS-1$

        @Override
        public Collection<String> getEventNames() {
            return NonNullUtils.checkNotNull(Collections.singleton(SCHED_SWITCH));
        }

        @Override
        public void accept(IEventConceptVisitor visitor, ITmfEvent event) {
            if (visitor instanceof ISchedKernelConceptVisitor) {
                ((ISchedKernelConceptVisitor) visitor).visit(this, event);
            } else {
                visitor.visit(this, event);
            }
        }

        @Override
        public Integer getPrevTid(ITmfEvent event) {
            return NonNullUtils.checkNotNull(((Long) SchedConceptUtils.getField(event, PREV_TID).getValue()).intValue());
        }

        @Override
        public Long getPrevState(ITmfEvent event) {
            return NonNullUtils.checkNotNull((Long) SchedConceptUtils.getField(event, PREV_STATE).getValue());
        }

        @Override
        public String getNextProcName(ITmfEvent event) {
            return NonNullUtils.checkNotNull((String) SchedConceptUtils.getField(event, NEXT_COMM).getValue());
        }

        @Override
        public Integer getNextTid(ITmfEvent event) {
            return NonNullUtils.checkNotNull(((Long) SchedConceptUtils.getField(event, NEXT_TID).getValue()).intValue());
        }
    }

    public class LttngSchedWakeupConcept implements ISchedWakeupConcept {

        private static final @NonNull Collection<String> SCHED_WAKEUP_EVENTS =
                checkNotNull(ImmutableList.of("sched_wakeup", "sched_wakeup_new")); //$NON-NLS-1$ //$NON-NLS-2$
        private static final String TID = "tid"; //$NON-NLS-1$

        @Override
        public Collection<String> getEventNames() {
            return SCHED_WAKEUP_EVENTS;
        }

        @Override
        public void accept(IEventConceptVisitor visitor, ITmfEvent event) {
            if (visitor instanceof ISchedKernelConceptVisitor) {
                ((ISchedKernelConceptVisitor) visitor).visit(this, event);
            } else {
                visitor.visit(this, event);
            }
        }

        @Override
        public Integer getPrevTid(ITmfEvent event) {
            return NonNullUtils.checkNotNull(((Long) SchedConceptUtils.getField(event, TID).getValue()).intValue());
        }

    }

}
