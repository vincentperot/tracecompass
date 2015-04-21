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
package org.eclipse.tracecompass.internal.statesystem.core.backend.historytree;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.BufferedBlockingQueue;
import org.eclipse.tracecompass.internal.statesystem.core.Activator;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;

/**
 * BufferedBlockingQueue is a queue that works in "segments" to allow smaller
 * items being put inside while maintaining performance
 */
@NonNullByDefault
class BufferedIntervalBlockingQueue extends BufferedBlockingQueue<ITmfStateInterval> {

    /**
     * Constructor
     *
     * @param numSegments
     *            number of segments
     * @param segmentSize
     *            the size of a segment
     */
    public BufferedIntervalBlockingQueue(int numSegments, int segmentSize) {
        super(numSegments, segmentSize);
    }

    /**
     * Does the queue contain an interval that has the timestamp and attribute
     * quark as defined below
     *
     * @param timestamp
     *            the timestamp to match
     * @param attributeQuark
     *            the attribute quark to match
     * @return the interval or null if not found
     */
    public synchronized @Nullable ITmfStateInterval contains(long timestamp, int attributeQuark) {
        try {
            for (ITmfStateInterval interval : getContent()) {
                if (matches(timestamp, attributeQuark, interval)) {
                    return interval;
                }
            }
        } catch (InterruptedException e) {
            Activator.getDefault().logError(e.getMessage(), e);
        }
        return null;
    }

    private static boolean matches(long timestamp, int attributeQuark, @Nullable ITmfStateInterval interval) {
        return interval != null && interval.getAttribute() == attributeQuark && interval.intersects(timestamp);
    }

}
