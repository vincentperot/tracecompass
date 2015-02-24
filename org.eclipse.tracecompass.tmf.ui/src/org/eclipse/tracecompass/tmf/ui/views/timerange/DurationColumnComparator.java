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

import org.eclipse.tracecompass.tmf.core.trace.location.ITmfRegionOfInterest;
import org.eclipse.tracecompass.tmf.ui.viewers.table.ViewerCompoundComparator;

/**
 * Long (numerical) column comparator
 */
public class DurationColumnComparator extends ViewerCompoundComparator {

    @Override
    public int compare(Object e1, Object e2) {
        long l1 = ((ITmfRegionOfInterest) e1).getDuration();
        long l2 = ((ITmfRegionOfInterest) e2).getDuration();
        return Long.compare(l1, l2);
    }
}
