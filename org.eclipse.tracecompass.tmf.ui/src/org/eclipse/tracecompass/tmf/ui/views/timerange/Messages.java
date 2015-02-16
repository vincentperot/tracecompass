/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.views.timerange;

import org.eclipse.osgi.util.NLS;

/**
 *  Timerange table strings
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.tmf.ui.views.timerange.messages"; //$NON-NLS-1$
    /** content */
    public static String AbstractTimeRangeTable_content;
    /** duration */
    public static String AbstractTimeRangeTable_duration;
    /** end time */
    public static String AbstractTimeRangeTable_endTime;
    /** start time */
    public static String AbstractTimeRangeTable_startTime;
    /** import */
    public static String RegionsOfInterestView_import;
    /** import tooltip */
    public static String RegionsOfInterestView_importToolTip;
    /** paste */
    public static String RegionsOfInterestView_Paste;
    /** instructions label for dialog */
    public static String TimeRangeInputDialog_label;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
