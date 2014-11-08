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
 ******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers.events.columns;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.criterion.ITmfEventCriterion;

/**
 * A column in the
 * {@link org.eclipse.tracecompass.tmf.ui.viewers.events.TmfEventsTable}. In
 * addition to ones provided by default, trace types can extend this class to
 * create additional columns specific to their events.
 *
 * Those additional columns can then be passed to the constructor
 * {@link org.eclipse.tracecompass.tmf.ui.viewers.events.TmfEventsTable#TmfEventsTable(org.eclipse.swt.widgets.Composite, int, java.util.Collection)}
 *
 * @author Alexandre Montplaisir
 * @noextend This class should not be extended directly. You should instead
 *           implement an {@link ITmfEventCriterion}.
 * @since 3.1
 */
@NonNullByDefault
public class TmfEventTableColumn {

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    private final ITmfEventCriterion fCriterion;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param criterion
     *            The {@link ITmfEventCriterion} to be used to populate this
     *            column.
     */
    public TmfEventTableColumn(ITmfEventCriterion criterion) {
        fCriterion = criterion;
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * Get this column's header name, a.k.a. title
     *
     * @return The column's title
     */
    public String getHeaderName() {
        return fCriterion.getName();
    }

    /**
     * Get the tooltip text for the column header
     *
     * @return The header's tooltip
     */
    public @Nullable String getHeaderTooltip() {
        return fCriterion.getHelpText();
    }

    /**
     * Get the string that should be displayed in this column's cell for a given
     * trace event. Basically, this defines "what to print in this column for
     * this event".
     * <p>
     * Note to implementers:
     * <p>
     * This method takes an {@link ITmfEvent}, because any type of event could
     * potentially be present in the table at the time. Do not assume that you
     * will only receive events of your trace type. You'd probably want to
     * return an empty string for event that don't match your expected class
     * type here.
     *
     * @param event
     *            The trace event whose element we want to display
     * @return The string to display in the column for this event
     */
    public String getItemString(ITmfEvent event) {
        return fCriterion.resolveCriterion(event);
    }

    /**
     * Return the FILTER_ID used by the filters to search this column.
     *
     * @return The filter ID for this column, or 'null' to not provide a filter
     *         ID (which will mean this column will probably not be
     *         searchable/filterable.)
     */
    public @Nullable String getFilterFieldId() {
        return fCriterion.getFilterId();
    }

    // ------------------------------------------------------------------------
    // hashCode/equals (so that equivalent columns can be merged together)
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + fCriterion.hashCode();
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TmfEventTableColumn)) {
            return false;
        }
        TmfEventTableColumn other = (TmfEventTableColumn) obj;
        if (!fCriterion.equals(other.fCriterion)) {
            /* Criteria can also define how they can be "equal" to one another */
            return false;
        }
        return true;
    }
}
