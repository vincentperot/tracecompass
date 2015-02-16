package org.eclipse.tracecompass.tmf.ui.views.timerange;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.tmf.ui.views.timerange.messages"; //$NON-NLS-1$
    public static String AbstractTimeRangeTable_content;
    public static String AbstractTimeRangeTable_duration;
    public static String AbstractTimeRangeTable_endTime;
    public static String AbstractTimeRangeTable_startTime;
    public static String RegionsOfInterestView_import;
    public static String RegionsOfInterestView_importToolTip;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
