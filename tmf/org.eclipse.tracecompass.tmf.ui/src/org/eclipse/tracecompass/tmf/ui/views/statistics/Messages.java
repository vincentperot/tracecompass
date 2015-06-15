/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Initial API and Implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.statistics;

import org.eclipse.osgi.util.NLS;

/**
 * Messages file for statistics view strings.
 *
 * @author Mathieu Denis
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.tmf.ui.views.statistics.messages"; //$NON-NLS-1$

    /**
     * String for the global tab name and on top of the global selection
     * piechart
     */
    public static String TmfStatisticsView_GlobalTabName;
    /**
     * String shown on top of the time-range selection piechart
     *
     * @since 1.0
     */
    public static String TmfStatisticsView_TimeRangeSelectionPieChartName;
    /**
     * String given to the slice in the piechart containing the too little
     * slices
     * @since 1.0
     */
    public static String TmfStatisticsView_PieChartOthersSliceName;

    /**
     * Name given to the jobs scheduled to update the view
     * @since 1.0
     */
    public static String TmfStatisticsView_ViewUpdateJobName;

    /**
     * Name given to the jobs scheduled to update the Statistics model
     * @since 1.0
     */
    public static String TmfStatisticsView_StatisticsUpdateJobName;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
