package org.eclipse.tracecompass.tmf.ui.viewers.events;

/**
 * The events table search/filter keys
 *
 * @version 1.0
 * @author Patrick Tasse
 * @since 1.0
 */
public interface Key {
    /** Search text */
    String SEARCH_TXT = "$srch_txt"; //$NON-NLS-1$

    /** Search object */
    String SEARCH_OBJ = "$srch_obj"; //$NON-NLS-1$

    /** Filter text */
    String FILTER_TXT = "$fltr_txt"; //$NON-NLS-1$

    /** Filter object */
    String FILTER_OBJ = "$fltr_obj"; //$NON-NLS-1$

    /** Timestamp */
    String TIMESTAMP = "$time"; //$NON-NLS-1$

    /** Rank */
    String RANK = "$rank"; //$NON-NLS-1$

    /** Bookmark indicator */
    String BOOKMARK = "$bookmark"; //$NON-NLS-1$

    /** Event aspect represented by this column */
    String ASPECT = "$aspect"; //$NON-NLS-1$

    /**
     * Table item list of style ranges
     *
     * @since 1.0
     */
    String STYLE_RANGES = "$style_ranges"; //$NON-NLS-1$
}