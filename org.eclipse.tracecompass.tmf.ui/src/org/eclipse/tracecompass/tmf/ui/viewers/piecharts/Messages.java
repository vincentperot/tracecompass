/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexis Cabana-Loriaux <alex021994@gmail.com> - Initial API and Implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers.piecharts;

import org.eclipse.osgi.util.NLS;

/**
 * Messages file for statistics view strings.
 *
 * @author Alexis Cabana-Loriaux <alex021994@gmail.com>
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.tmf.ui.viewers.piecharts.messages"; //$NON-NLS-1$

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
     * String for the top of the global selection piechart
     * @since 1.0
     */
    public static String TmfStatisticsView_GlobalSelectionPieChartName;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
