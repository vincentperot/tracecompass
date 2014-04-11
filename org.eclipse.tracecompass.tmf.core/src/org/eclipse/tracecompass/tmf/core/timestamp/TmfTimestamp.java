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

/**
 * A generic timestamp implementation. The timestamp is represented by the
 * tuple { value, scale, precision }. By default, timestamps are scaled in
 * seconds.
 *
 * @author Francois Chouinard
 * @since 2.0
 */
public class TmfTimestamp implements ITmfTimestamp {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The beginning of time
     */
    public static final ITmfTimestamp BIG_BANG = new TmfTimestamp(Long.MIN_VALUE, Integer.MAX_VALUE) {
        @Override
        public int compareTo(ITmfTimestamp ts) {
            return (ts == this) ? 0 : -1;
        }

        @Override
        public boolean equals(Object other) {
            return other == this;
        }

        @Override
        public ITmfTimestamp normalize(long offset, int scale) {
            return this;
        }
    };

    /**
     * The end of time
     */
    public static final ITmfTimestamp BIG_CRUNCH = new TmfTimestamp(Long.MAX_VALUE, Integer.MAX_VALUE) {
        @Override
        public int compareTo(ITmfTimestamp ts) {
            return (ts == this) ? 0 : 1;
        }

        @Override
        public boolean equals(Object other) {
            return other == this;
        }

        @Override
        public ITmfTimestamp normalize(long offset, int scale) {
            return this;
        }
    };

    /**
     * Zero
     */
    public static final ITmfTimestamp ZERO = new TmfTimestamp(0, 0) {
        @Override
        public int compareTo(ITmfTimestamp ts) {
            return (ts == this) ? 0 : super.compareTo(ts);
        }

        @Override
        public boolean equals(Object other) {
            return other == this;
        }

        @Override
        public ITmfTimestamp normalize(long offset, int scale) {
            return this;
        }
    };

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The timestamp raw value (mantissa)
     */
    private final long fValue;

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
    }

    /**
     * Copies a timestamp but with a new time value
     *
     * @param timestamp
     *            The timestamp to copy
     * @param newvalue
     *            The value the new timestamp will have
     * @since 3.0
     */
    public TmfTimestamp(ITmfTimestamp timestamp, long newvalue) {
        if (timestamp == null) {
            throw new IllegalArgumentException();
        }
        fValue = newvalue;
        fScale = timestamp.getScale();
    }

    // ------------------------------------------------------------------------
    // ITmfTimestamp
    // ------------------------------------------------------------------------

    /**
     * Construct the timestamp from the ByteBuffer.
     *
     * @param bufferIn
     *            the buffer to read from
     *
     * @since 3.0
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

    private static final long SCALING_FACTORS[] = new long[] {
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

    private static final long OVERFLOW_CHECK[] = new long[] {
            Long.MAX_VALUE / SCALING_FACTORS[0],
            Long.MAX_VALUE / SCALING_FACTORS[1],
            Long.MAX_VALUE / SCALING_FACTORS[2],
            Long.MAX_VALUE / SCALING_FACTORS[3],
            Long.MAX_VALUE / SCALING_FACTORS[4],
            Long.MAX_VALUE / SCALING_FACTORS[5],
            Long.MAX_VALUE / SCALING_FACTORS[6],
            Long.MAX_VALUE / SCALING_FACTORS[7],
            Long.MAX_VALUE / SCALING_FACTORS[8],
            Long.MAX_VALUE / SCALING_FACTORS[9],
            Long.MAX_VALUE / SCALING_FACTORS[10],
            Long.MAX_VALUE / SCALING_FACTORS[11],
            Long.MAX_VALUE / SCALING_FACTORS[12],
            Long.MAX_VALUE / SCALING_FACTORS[13],
            Long.MAX_VALUE / SCALING_FACTORS[14],
            Long.MAX_VALUE / SCALING_FACTORS[15],
            Long.MAX_VALUE / SCALING_FACTORS[16],
            Long.MAX_VALUE / SCALING_FACTORS[17],
            Long.MAX_VALUE / SCALING_FACTORS[18]
    };

    private static final long underflowCheck[] = new long[] {
            Long.MIN_VALUE / SCALING_FACTORS[0],
            Long.MIN_VALUE / SCALING_FACTORS[1],
            Long.MIN_VALUE / SCALING_FACTORS[2],
            Long.MIN_VALUE / SCALING_FACTORS[3],
            Long.MIN_VALUE / SCALING_FACTORS[4],
            Long.MIN_VALUE / SCALING_FACTORS[5],
            Long.MIN_VALUE / SCALING_FACTORS[6],
            Long.MIN_VALUE / SCALING_FACTORS[7],
            Long.MIN_VALUE / SCALING_FACTORS[8],
            Long.MIN_VALUE / SCALING_FACTORS[9],
            Long.MIN_VALUE / SCALING_FACTORS[10],
            Long.MIN_VALUE / SCALING_FACTORS[11],
            Long.MIN_VALUE / SCALING_FACTORS[12],
            Long.MIN_VALUE / SCALING_FACTORS[13],
            Long.MIN_VALUE / SCALING_FACTORS[14],
            Long.MIN_VALUE / SCALING_FACTORS[15],
            Long.MIN_VALUE / SCALING_FACTORS[16],
            Long.MIN_VALUE / SCALING_FACTORS[17],
            Long.MIN_VALUE / SCALING_FACTORS[18]
    };

    @Override
    public ITmfTimestamp normalize(final long offset, final int scale) {

        long value = fValue;

        // Handle the trivial case
        if (fScale == scale && offset == 0) {
            return this;
        }

        // First, scale the timestamp
        if (fScale != scale) {
            final int scaleDiff = Math.abs(fScale - scale);
            if (scaleDiff >= SCALING_FACTORS.length) {
                throw new ArithmeticException("Scaling exception"); //$NON-NLS-1$
            }

            final long scalingFactor = SCALING_FACTORS[scaleDiff];
            if (scale < fScale) {
                if (value > OVERFLOW_CHECK[scaleDiff] || value < underflowCheck[scaleDiff]) {
                    throw new ArithmeticException("overflow exception"); //$NON-NLS-1$
                }
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
        // Check the corner cases (we can't use equals() because it uses compareTo()...)
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

        try {
            final ITmfTimestamp nts = ts.normalize(0, fScale);
            long delta = fValue - nts.getValue();
            if (delta == Long.MIN_VALUE) {
                delta = (fValue >> 1) - (nts.getValue() >> 1);
            }
            if (delta > 0) {
                return 1;
            } else if (delta < 0) {
                return -1;
            } else {
                return 0;
            }
        } catch (final ArithmeticException e) {
            // Scaling error. We can figure it out nonetheless.

            // First, look at the sign of the mantissa
            final long value = ts.getValue();
            if (fValue == 0 && value == 0) {
                return 0;
            }
            if (fValue < 0 && value >= 0) {
                return -1;
            }
            if (fValue >= 0 && value < 0) {
                return 1;
            }

            // Otherwise, just compare the scales
            final int scale = ts.getScale();
            return (fScale > scale) ? (fValue >= 0) ? 1 : -1 : (fValue >= 0) ? -1 : 1;
        }
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (fValue ^ (fValue >>> 32));
        result = prime * result + fScale;
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
        return (compareTo(ts) == 0);
    }

    @Override
    public String toString() {
        return toString(TmfTimestampFormat.getDefaulTimeFormat());
    }

    /**
     * @since 2.0
     */
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
     * @param bufferOut the buffer to write to
     *
     * @since 3.0
     */
    public void serialize(ByteBuffer bufferOut) {
        bufferOut.putLong(fValue);
        bufferOut.putInt(fScale);
    }
}
