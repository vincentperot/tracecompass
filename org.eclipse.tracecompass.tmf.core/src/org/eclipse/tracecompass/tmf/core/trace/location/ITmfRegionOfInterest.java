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
package org.eclipse.tracecompass.tmf.core.trace.location;

import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;

/**
 * A region of interest, basically an {@link TmfTimeRange} and a message
 */
public interface ITmfRegionOfInterest {

    /**
     * Get the start time of the region of interest
     *
     * @return the start time
     */
    ITmfTimestamp getStartTime();

    /**
     * Get the end time of the region of interest
     *
     * @return the end time
     */
    ITmfTimestamp getEndTime();

    /**
     * Get the duration of the region of interest
     *
     * @return the duration
     */
    long getDuration();

    /**
     * Get the message of the region of interest
     *
     * @return a string message
     */
    String getMessage();
}
