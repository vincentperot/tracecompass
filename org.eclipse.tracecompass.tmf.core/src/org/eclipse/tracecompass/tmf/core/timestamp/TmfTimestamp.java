/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation, refactoring and updates
 *   Thomas Gatterweh    - Updated scaling / synchronization
 *   Geneviève Bastien - Added copy constructor with new value
 *   Alexandre Montplaisir - Removed concept of precision
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.timestamp;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNull;

/**
 * A generic timestamp implementation. The timestamp is represented by the tuple
 * { value, scale, precision }. By default, timestamps are scaled in seconds.
 *
 * @author Francois Chouinard
 */
public class TmfTimestamp implements ITmfTimestamp {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final long scalingFactors[] = new long[] {
            1L,
            10L,
            100L,
            1000L,
            10000L,
            100000L,
            1000000L,
            10000000L,
            100000000L,
            1000000000L,
            10000000000L,
            100000000000L,
            1000000000000L,
            10000000000000L,
            100000000000000L,
            1000000000000000L,
            10000000000000000L,
            100000000000000000L,
            1000000000000000000L,
    };

    /**
     * The beginning of time
     */
    public static final @NonNull ITmfTimestamp BIG_BANG =
            new TmfTimestamp(Long.MIN_VALUE, Integer.MAX_VALUE) {
                @Override
                public ITmfTimestamp normalize(long offset, int scale) {
                    return this;
                }

                @Override
                public int compareTo(ITmfTimestamp ts) {
                    return (ts == this) ? 0 : -1;
                }
            };

    /**
     * The end of time
     */
    public static final @NonNull ITmfTimestamp BIG_CRUNCH =
            new TmfTimestamp(Long.MAX_VALUE, Integer.MAX_VALUE) {
                @Override
                public ITmfTimestamp normalize(long offset, int scale) {
                    return this;
                }

                @Override
                public int compareTo(ITmfTimestamp ts) {
                    return (ts == this) ? 0 : 1;
                }
            };

    /**
     * Zero
     */
    public static final @NonNull ITmfTimestamp ZERO =
            new TmfTimestamp(0, 0);

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The timestamp raw value (mantissa)
     */
    private transient final long fValue;

    private final long fNanoseconds;

    /**
     * The timestamp scale (magnitude)
     */
    private final int fScale;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public TmfTimestamp() {
        this(0, ITmfTimestamp.SECOND_SCALE);
    }

    /**
     * Simple constructor (scale = 0)
     *
     * @param value
     *            the timestamp value
     */
    public TmfTimestamp(final long value) {
        this(value, ITmfTimestamp.SECOND_SCALE);
    }

    /**
     * Full constructor
     *
     * @param value
     *            the timestamp value
     * @param scale
     *            the timestamp scale
     */
    public TmfTimestamp(final long value, final int scale) {
        fValue = value;
        fScale = scale;
        if (fScale == Integer.MAX_VALUE) {
            fNanoseconds = fValue == Long.MIN_VALUE ? -1L : -2L;
        } else if (fScale == NANOSECOND_SCALE) {
            fNanoseconds = fValue;
        } else {
            fNanoseconds = normalize(0, NANOSECOND_SCALE).getValue();
        }
    }

    /**
     * Copy constructor
     *
     * @param timestamp
     *            the timestamp to copy
     */
    public TmfTimestamp(final ITmfTimestamp timestamp) {
        if (timestamp == null) {
            throw new IllegalArgumentException();
        }
        fValue = timestamp.getValue();
        fScale = timestamp.getScale();
        if (fScale == Integer.MAX_VALUE) {
            fNanoseconds = timestamp == BIG_BANG ? -1L : -2L;
        } else {
            fNanoseconds = normalize(0, NANOSECOND_SCALE).getValue();
        }
    }

    /**
     * Copies a timestamp but with a new time value
     *
     * @param timestamp
     *            The timestamp to copy
     * @param newvalue
     *            The value the new timestamp will have
     */
    public TmfTimestamp(ITmfTimestamp timestamp, long newvalue) {
        if (timestamp == null || timestamp == BIG_BANG || timestamp == BIG_CRUNCH) {
            throw new IllegalArgumentException();
        }
        fValue = newvalue;
        fScale = timestamp.getScale();
        fNanoseconds = normalize(0, NANOSECOND_SCALE).getValue();
    }

    // ------------------------------------------------------------------------
    // ITmfTimestamp
    // ------------------------------------------------------------------------

    /**
     * Construct the timestamp from the ByteBuffer.
     *
     * @param bufferIn
     *            the buffer to read from
     */
    public TmfTimestamp(ByteBuffer bufferIn) {
        this(bufferIn.getLong(), bufferIn.getInt());
    }

    @Override
    public long getValue() {
        return fValue;
    }

    @Override
    public int getScale() {
        return fScale;
    }

    @Override
    public ITmfTimestamp normalize(final long offset, final int scale) {

        long value = fValue;

        // Handle the trivial case
        if (fScale == scale && offset == 0) {
            return this;
        }

        // In case of big bang and big crunch just return this (no need to
        // normalize)
        if (this.equals(BIG_BANG) || this.equals(BIG_CRUNCH)) {
            return this;
        }

        // First, scale the timestamp
        if (fScale != scale) {
            final int scaleDiff = Math.abs(fScale - scale);
            if (scaleDiff >= scalingFactors.length) {
                throw new ArithmeticException("Scaling exception"); //$NON-NLS-1$
            }

            final long scalingFactor = scalingFactors[scaleDiff];
            if (scale < fScale) {
                value *= scalingFactor;
            } else {
                value /= scalingFactor;
            }
        }

        // Then, apply the offset
        if (offset < 0) {
            value = (value < Long.MIN_VALUE - offset) ? Long.MIN_VALUE : value + offset;
        } else {
            value = (value > Long.MAX_VALUE - offset) ? Long.MAX_VALUE : value + offset;
        }

        return new TmfTimestamp(value, scale);
    }

    @Override
    public ITmfTimestamp getDelta(final ITmfTimestamp ts) {
        final ITmfTimestamp nts = ts.normalize(0, fScale);
        final long value = fValue - nts.getValue();
        return new TmfTimestampDelta(value, fScale);
    }

    @Override
    public boolean intersects(TmfTimeRange range) {
        if (this.compareTo(range.getStartTime()) >= 0 &&
                this.compareTo(range.getEndTime()) <= 0) {
            return true;
        }
        return false;
    }

    // ------------------------------------------------------------------------
    // Comparable
    // ------------------------------------------------------------------------

    @Override
    public int compareTo(final ITmfTimestamp ts) {
        // Check the corner cases (we can't use equals() because it uses
        // compareTo()...)
        if (ts == null) {
            return 1;
        }
        if (this == ts || (fValue == ts.getValue() && fScale == ts.getScale())) {
            return 0;
        }
        if (ts == BIG_CRUNCH) {
            return -1;
        }
        if (ts == BIG_BANG) {
            return 1;
        }
        return Long.compare(fNanoseconds, ts.getNanoseconds());
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (fNanoseconds ^ (fNanoseconds >>> 32));
        return result;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof ITmfTimestamp)) {
            return false;
        }
        /* We allow comparing with other types of *I*TmfTimestamp though */
        final ITmfTimestamp ts = (ITmfTimestamp) other;
        return ts.getNanoseconds() == fNanoseconds;
    }

    @Override
    public String toString() {
        return toString(TmfTimestampFormat.getDefaulTimeFormat());
    }

    @Override
    public String toString(final TmfTimestampFormat format) {
        try {
            ITmfTimestamp ts = normalize(0, ITmfTimestamp.NANOSECOND_SCALE);
            return format.format(ts.getValue());
        } catch (ArithmeticException e) {
            return format.format(0);
        }
    }

    /**
     * Write the time stamp to the ByteBuffer so that it can be saved to disk.
     *
     * @param bufferOut
     *            the buffer to write to
     */
    public void serialize(ByteBuffer bufferOut) {
        bufferOut.putLong(fValue);
        bufferOut.putInt(fScale);
    }

    @Override
    public long getNanoseconds() {
        return fNanoseconds;
    }
}
