/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   France Lapointe Nguyen - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.segmentstore.core.tests.stubs;

import org.eclipse.tracecompass.segmentstore.core.ISegment;

/**
 * ISegment interval used in test vectors for latency analysis testing
 *
 * @author France Lapointe Nguyen
 * @since 1.0
 */
public class TestInterval implements ISegment {

    /**
     * id
     */
    public static final long serialVersionUID = 720219435203234220L;
    private final long fStart;
    private final long fEnd;

    /**
     * @param start
     *            Start time of the interval
     * @param end
     *            End time of the interval
     */
    public TestInterval(long start, long end) {
        fStart = start;
        fEnd = end;

    }

    @Override
    public long getStart() {
        return fStart;
    }

    @Override
    public long getEnd() {
        return fEnd;
    }

    @Override
    public long getLength() {
        return fEnd - fStart;
    }

}