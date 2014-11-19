/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Alexandre Montplaisir - Update to new Event Table API
 *******************************************************************************/

package org.eclipse.tracecompass.btf.ui;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.btf.core.event.BtfEvent;
import org.eclipse.tracecompass.btf.core.trace.BtfColumnNames;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.ui.viewers.events.columns.ITmfEventTableColumns;
import org.eclipse.tracecompass.tmf.ui.viewers.events.columns.TmfEventTableColumn;
import org.eclipse.tracecompass.tmf.ui.viewers.events.columns.TmfEventTableFieldColumn;

import com.google.common.collect.ImmutableList;

/**
 * Columns to use in the BTF event table
 *
 * @author Alexandre Montplaisir
 */
public class BtfEventTableColumns implements ITmfEventTableColumns {

    // ------------------------------------------------------------------------
    // Column definition
    // ------------------------------------------------------------------------

    @SuppressWarnings("null")
    private static final @NonNull Collection<TmfEventTableColumn> BTF_COLUMNS = ImmutableList.of(
            TmfEventTableColumn.BaseColumns.TIMESTAMP,
            new BtfSourceColumn(),
            new BtfSourceInstanceColumn(),
            TmfEventTableColumn.BaseColumns.EVENT_TYPE,
            new BtfTargetColumn(),
            new BtfTargetInstanceColumn(),
            new BtfEventColumn(),
            new BtfNotesColumn()
            );

    /**
     * The "source" column, whose value comes from {@link ITmfEvent#getSource()}
     */
    private static class BtfSourceColumn extends TmfEventTableColumn {

        public BtfSourceColumn() {
            super(BtfColumnNames.SOURCE.toString(), null);
        }

        @Override
        public String getItemString(ITmfEvent event) {
            if (!(event instanceof BtfEvent)) {
                return EMPTY_STRING;
            }
            String ret = ((BtfEvent) event).getSource();
            return (ret == null ? EMPTY_STRING : ret);
        }

        @Override
        public String getFilterFieldId() {
            return ITmfEvent.EVENT_FIELD_SOURCE;
        }
    }

    /**
     * The "source instance" column, whose value comes from the field of the
     * same name.
     */
    private static class BtfSourceInstanceColumn extends TmfEventTableFieldColumn {
        public BtfSourceInstanceColumn() {
            super(BtfColumnNames.SOURCE_INSTANCE.toString());
        }
    }

    /**
     * The "target" column, taking its value from
     * {@link ITmfEvent#getReference()}.
     */
    private static class BtfTargetColumn extends TmfEventTableColumn {

        public BtfTargetColumn() {
            super(BtfColumnNames.TARGET.toString());
        }

        @Override
        public String getItemString(ITmfEvent event) {
            if (!(event instanceof BtfEvent)) {
                return EMPTY_STRING;
            }
            String ret = ((BtfEvent) event).getReference();
            return (ret == null ? EMPTY_STRING : ret);
        }

        @Override
        public String getFilterFieldId() {
            return ITmfEvent.EVENT_FIELD_REFERENCE;
        }
    }

    /**
     * The "target instance" column, whose value comes from the field of the
     * same name.
     */
    private static class BtfTargetInstanceColumn extends TmfEventTableFieldColumn {
        public BtfTargetInstanceColumn() {
            super(BtfColumnNames.TARGET_INSTANCE.toString());
        }
    }

    /**
     * The "event" column, whose value comes from the field of the same name.
     */
    private static class BtfEventColumn extends TmfEventTableFieldColumn {
        public BtfEventColumn() {
            super(BtfColumnNames.EVENT.toString());
        }
    }

    /**
     * The "notes" column, whose value comes from the field of the same name, if
     * present.
     */
    private static class BtfNotesColumn extends TmfEventTableFieldColumn {
        public BtfNotesColumn() {
            super(BtfColumnNames.NOTES.toString());
        }
    }

    // ------------------------------------------------------------------------
    // ITmfEventTableColumns
    // ------------------------------------------------------------------------

    @Override
    public Collection<? extends TmfEventTableColumn> getEventTableColumns() {
        return BTF_COLUMNS;
    }
}