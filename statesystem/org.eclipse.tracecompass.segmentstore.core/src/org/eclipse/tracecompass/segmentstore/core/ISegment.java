/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.segmentstore.core;

import java.io.Serializable;

/**
 * Generic interface for any segment (like a time range) that can be used in the
 * segment store.
 *
 * @author Alexandre Montplaisir
 */
public interface ISegment extends Serializable {

    /**
     * The start time of the time range
     *
     * @return The start time
     */
    long getStartTime();

    /**
     * The end time of the time range
     *
     * @return The end time
     */
    long getEndTime();

    /**
     * The duration of the time range. Normally ({@link #getEndTime()} -
     * {@link #getStartTime()}).
     *
     * @return The duration
     */
    long getDuration();
}
