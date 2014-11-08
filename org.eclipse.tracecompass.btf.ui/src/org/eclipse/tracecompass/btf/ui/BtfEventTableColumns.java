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
import org.eclipse.tracecompass.tmf.core.event.criterion.ITmfEventCriterion;
import org.eclipse.tracecompass.tmf.core.event.criterion.TmfEventFieldCriterion;
import org.eclipse.tracecompass.tmf.ui.viewers.events.columns.ITmfEventTableColumns;
import org.eclipse.tracecompass.tmf.ui.viewers.events.columns.TmfEventTableColumn;

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
            new TmfEventTableColumn(ITmfEventCriterion.BaseCriteria.TIMESTAMP),
            new TmfEventTableColumn(new BtfSourceCriterion()),
            new TmfEventTableColumn(new BtfSourceInstanceCriterion()),
            new TmfEventTableColumn(ITmfEventCriterion.BaseCriteria.EVENT_TYPE),
            new TmfEventTableColumn(new BtfTargetCriterion()),
            new TmfEventTableColumn(new BtfTargetInstanceCriterion()),
            new TmfEventTableColumn(new BtfEventCriterion()),
            new TmfEventTableColumn(new BtfNotesCriterion())
            );

    /**
     * The "source" column, whose value comes from {@link ITmfEvent#getSource()}
     */
    private static class BtfSourceCriterion implements ITmfEventCriterion {

        @Override
        public String getName() {
            return BtfColumnNames.SOURCE.toString();
        }

        @Override
        public String resolveCriterion(ITmfEvent event) {
            if (!(event instanceof BtfEvent)) {
                return EMPTY_STRING;
            }
            String ret = ((BtfEvent) event).getSource();
            return (ret == null ? EMPTY_STRING : ret);
        }

        @Override
        public String getHelpText() {
            return EMPTY_STRING;
        }

        @Override
        public String getFilterId() {
            return ITmfEvent.EVENT_FIELD_SOURCE;
        }
    }

    /**
     * The "source instance" column, whose value comes from the field of the
     * same name.
     */
    private static class BtfSourceInstanceCriterion extends TmfEventFieldCriterion {
        public BtfSourceInstanceCriterion() {
            super(BtfColumnNames.SOURCE_INSTANCE.toString(),
                    BtfColumnNames.SOURCE_INSTANCE.toString());
        }
    }

    /**
     * The "target" column, taking its value from
     * {@link ITmfEvent#getReference()}.
     */
    private static class BtfTargetCriterion implements ITmfEventCriterion {

        @Override
        public String getName() {
             return BtfColumnNames.TARGET.toString();
        }

        @Override
        public String resolveCriterion(ITmfEvent event) {
            if (!(event instanceof BtfEvent)) {
                return EMPTY_STRING;
            }
            String ret = ((BtfEvent) event).getReference();
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

    /**
     * The "target instance" column, whose value comes from the field of the
     * same name.
     */
    private static class BtfTargetInstanceCriterion extends TmfEventFieldCriterion {
        public BtfTargetInstanceCriterion() {
            super(BtfColumnNames.TARGET_INSTANCE.toString(),
                    BtfColumnNames.TARGET_INSTANCE.toString());
        }
    }

    /**
     * The "event" column, whose value comes from the field of the same name.
     */
    private static class BtfEventCriterion extends TmfEventFieldCriterion {
        public BtfEventCriterion() {
            super(BtfColumnNames.EVENT.toString(),
                    BtfColumnNames.EVENT.toString());
        }
    }

    /**
     * The "notes" column, whose value comes from the field of the same name, if
     * present.
     */
    private static class BtfNotesCriterion extends TmfEventFieldCriterion {
        public BtfNotesCriterion() {
            super(BtfColumnNames.NOTES.toString(),
                    BtfColumnNames.NOTES.toString());
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