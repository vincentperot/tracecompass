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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfRegionOfInterest;

/**
 * {@link ITmfTimestamp} column comparator
 *
 */
public class StartTimeColumnComparator extends ViewerComparator {

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        ITmfTimestamp ts1 = ((ITmfRegionOfInterest) e1).getEndTime();
        ITmfTimestamp ts2 = ((ITmfRegionOfInterest) e2).getEndTime();
        return ts1.compareTo(ts2);
    }
}
