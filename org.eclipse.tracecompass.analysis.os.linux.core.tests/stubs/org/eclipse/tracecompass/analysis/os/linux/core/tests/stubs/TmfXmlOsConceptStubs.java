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

package org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.concepts.ISchedKernelConceptVisitor;
import org.eclipse.tracecompass.analysis.os.linux.core.concepts.ISchedKernelConcepts;
import org.eclipse.tracecompass.analysis.os.linux.core.concepts.ISchedKernelConcepts.ISchedWakeupConcept;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.concept.IEventConceptVisitor;

import com.google.common.collect.ImmutableList;

/**
 * Default implementation of concepts for an OS XML stub trace
 *
 * @author Geneviève Bastien
 */
public interface TmfXmlOsConceptStubs {

    class StubOsConceptUtils {
        private static ITmfEventField getField(ITmfEvent event, String name) {
            ITmfEventField content = event.getContent();
            ITmfEventField field = content.getField(name);
            return field;
        }
    }

    public class SchedSwitchConcept implements ISchedKernelConcepts.ISchedSwitchConcept {

        @Override
        public Collection<String> getEventNames() {
            return NonNullUtils.checkNotNull(Collections.singleton("sched_switch"));
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
            return NonNullUtils.checkNotNull(((Long) StubOsConceptUtils.getField(event, "prev_tid").getValue()).intValue());
        }

        @Override
        public Long getPrevState(ITmfEvent event) {
            return NonNullUtils.checkNotNull((Long) StubOsConceptUtils.getField(event, "prev_state").getValue());
        }

        @Override
        public String getNextProcName(ITmfEvent event) {
            return NonNullUtils.checkNotNull((String) StubOsConceptUtils.getField(event, "next_comm").getValue());
        }

        @Override
        public Integer getNextTid(ITmfEvent event) {
            return NonNullUtils.checkNotNull(((Long) StubOsConceptUtils.getField(event, "next_tid").getValue()).intValue());
        }

    }

    public class SchedWakeupConcept implements ISchedWakeupConcept {

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
            return NonNullUtils.checkNotNull(((Long) StubOsConceptUtils.getField(event, TID).getValue()).intValue());
        }

    }

}
