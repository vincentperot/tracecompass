/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.gdbtrace.ui.views.events;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.gdbtrace.core.event.GdbTraceEvent;
import org.eclipse.tracecompass.internal.gdbtrace.core.event.GdbTraceEventContent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.criterion.ITmfEventCriterion;
import org.eclipse.tracecompass.tmf.core.event.criterion.TmfEventFieldCriterion;
import org.eclipse.tracecompass.tmf.ui.viewers.events.columns.ITmfEventTableColumns;
import org.eclipse.tracecompass.tmf.ui.viewers.events.columns.TmfEventTableColumn;

import com.google.common.collect.ImmutableList;

/**
 * Event table column definition for GDB traces.
 *
 * @author Alexandre Montplaisir
 */
public class GdbEventTableColumns implements ITmfEventTableColumns {

    // ------------------------------------------------------------------------
    // Column definition
    // ------------------------------------------------------------------------

    @SuppressWarnings("null")
    static final @NonNull Collection<TmfEventTableColumn> GDB_COLUMNS = ImmutableList.of(
            new TmfEventTableColumn(new GdbTraceFrameCriterion()),
            new TmfEventTableColumn(new GdbTracepointCriterion()),
            new TmfEventTableColumn(new GdbFileCriterion())
            );

    private static class GdbTraceFrameCriterion extends TmfEventFieldCriterion {
        public GdbTraceFrameCriterion() {
            super(GdbTraceEventContent.TRACE_FRAME,
                    GdbTraceEventContent.TRACE_FRAME);
        }
    }

    private static class GdbTracepointCriterion extends TmfEventFieldCriterion {
        public GdbTracepointCriterion() {
            super(GdbTraceEventContent.TRACEPOINT,
                    GdbTraceEventContent.TRACEPOINT);
        }
    }

    private static class GdbFileCriterion implements ITmfEventCriterion {

        @Override
        public String getName() {
            return "File"; //$NON-NLS-1$
        }

        @Override
        public String resolveCriterion(ITmfEvent event) {
            if (!(event instanceof GdbTraceEvent)) {
                return EMPTY_STRING;
            }
            String ret = ((GdbTraceEvent) event).getReference();
            return (ret == null ? EMPTY_STRING : ret);
        }

        @Override
        public String getHelpText() {
            return EMPTY_STRING;
        }

        @Override
        public String getFilterId() {
            return ITmfEvent.EVENT_FIELD_REFERENCE;
        }
    }

    // ------------------------------------------------------------------------
    // ITmfEventTableColumns
    // ------------------------------------------------------------------------

    @Override
    public Collection<? extends TmfEventTableColumn> getEventTableColumns() {
        return GDB_COLUMNS;
    }
}
