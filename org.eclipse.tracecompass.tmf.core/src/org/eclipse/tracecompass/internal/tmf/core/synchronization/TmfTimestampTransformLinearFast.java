package org.eclipse.tracecompass.internal.tmf.core.synchronization;

import java.math.BigDecimal;
import java.math.MathContext;

import org.eclipse.tracecompass.tmf.core.synchronization.ITmfTimestampTransform;
import org.eclipse.tracecompass.tmf.core.synchronization.TimestampTransformFactory;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * Fast linear timestamp transform.
 *
 * Reduce the use of BigDecimal for an interval of time where the transform can
 * be computed only with integer math. By rearranging the linear equation
 *
 * f(t) = fAlpha * t + fBeta
 *
 * to
 *
 * f(t) = (fAlphaLong * (t - ts)) / m + fBeta + c
 *
 * where fAlphaLong = fAlpha * m, and c is the constant part of the slope
 * product.
 *
 * The slope applies to a relative time reference instead of absolute timestamp
 * from epoch. The constant part of the slope for the interval is added to beta.
 * It reduces the width of slope and timestamp to 32-bit integers, and the
 * result fits a 64-bit value. Using standard integer arithmetic yield speedup
 * compared to BigDecimal, while preserving precision. Depending of rounding,
 * there may be a slight difference of +/- 3ns between the value computed by the
 * fast transform compared to BigDecimal. The timestamps produced are indepotent
 * (transforming the same timestamp will always produce the same result), and
 * the timestamps are monotonic.
 *
 * The number of bits available for the cache range is variable. The variable
 * alphaLong must be a 32-bit value. We reserve 30-bit for the decimal part to
 * reach the nanosecond precision. If the slope is greater than 1.0, the shift
 * is reduced to avoid overflow. It reduces the useful cache range, but the
 * result is correct even for large (1e9) slope.
 *
 * @author Francis Giraldeau <francis.giraldeau@gmail.com>
 *
 */
public class TmfTimestampTransformLinearFast implements ITmfTimestampTransformInvertible {

    private static final long serialVersionUID = 2398540405078949739L;

    private final BigDecimal fAlpha;
    private final BigDecimal fBeta;
    private final long fAlphaLong;
    private final long fBetaLong;
    private final int fDeltaBits;
    private final long fDeltaMax;
    private long fRangeStart;
    private long fOffset;
    private long fScaleMiss;
    private long fScaleHit;
    private final int hc;
    private static final int fIntegerBits = 32;
    private static final int fDecimalBits = 30;
    private static final HashFunction hf = Hashing.goodFastHash(32);

    private static final MathContext fMc = MathContext.DECIMAL128;

    /**
     * Default constructor, equivalent to the identity.
     */
    public TmfTimestampTransformLinearFast() {
        this(BigDecimal.ONE, BigDecimal.ZERO);
    }

    /**
     * Constructor with alpha and beta
     *
     * @param alpha
     *            The slope of the linear transform
     * @param beta
     *            The initial offset of the linear transform
     */
    public TmfTimestampTransformLinearFast(final double alpha, final double beta) {
        this(BigDecimal.valueOf(alpha), BigDecimal.valueOf(beta));
    }

    /**
     * Constructor with alpha and beta as BigDecimal
     *
     * @param alpha
     *            The slope of the linear transform (must be in the range
     *            [1e-9, Integer.MAX_VALUE]
     * @param beta
     *            The initial offset of the linear transform
     */
    public TmfTimestampTransformLinearFast(final BigDecimal alpha, final BigDecimal beta) {
        /*
         * Validate the slope range:
         *
         * - Negative slope means timestamp goes backward wrt another computer,
         *   and this would violate the basic laws of physics.
         *
         * - A slope smaller than 1e-9 means the transform result will always be
         *   truncated to zero nanosecond.
         *
         * - A slope larger than Integer.MAX_VALUE is too large for the
         *   nanosecond scale.
         *
         * Therefore, a valid slope must be in the range [1e-9, Integer.MAX_VALUE]
         */
        if (alpha.compareTo(BigDecimal.valueOf(1e-9)) < 0 ||
                alpha.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) > 0) {
            throw new IllegalArgumentException("The slope alpha must in the range [1e-9, Integer.MAX_VALUE]"); //$NON-NLS-1$
        }
        fAlpha = alpha;
        fBeta = beta;

        /*
         * The result of (fAlphaLong * delta) must be at most 64-bit value.
         * Below, we compute the number of bits usable to represent the delta.
         * Small fAlpha (close to one) have greater range left for delta (at
         * most 30-bit). For large fAlpha, we reduce the delta range. If fAlpha
         * is close to ~1e9, then the delta size will be zero, effectively
         * recomputing the result using the BigDecimal for each transform.
         *
         * Long.numberOfLeadingZeros(fAlpha.longValue()) returns the number of
         * zero bits of the integer part of the slope. Then, fIntegerBits is
         * subtracted, which returns the number of bits usable for delta. This
         * value is bounded in the interval of [0, 30], because the delta can't
         * be negative, and we handle at most nanosecond precision, or 2^30. One
         * bit for each operand is reserved for the sign (Java enforce signed
         * arithmetics), such that
         *
         * bitsof(fDeltaBits) + bitsof(fAlphaLong) = 62 + 2 = 64
         */
        int width = Long.numberOfLeadingZeros(fAlpha.longValue()) - fIntegerBits;
        fDeltaBits = Math.max(Math.min(width, fDecimalBits), 0);
        fDeltaMax = 1 << fDeltaBits;
        fAlphaLong = fAlpha.multiply(BigDecimal.valueOf(fDeltaMax)).longValue();
        fBetaLong = fBeta.longValue();
        fRangeStart = 0L;
        fOffset = 0L;
        fScaleMiss = 0;
        fScaleHit = 0;
        hc = hf.newHasher()
                .putLong(fAlphaLong)
                .putLong(fBetaLong)
                .hash()
                .asInt();
    }

    private long apply(long ts) {
        long delta = ts - fRangeStart;
        if (delta > fDeltaMax || delta < 0) {
            /*
             * Rescale if we exceed the safe range.
             *
             * If the same timestamp is transform with two different fStart
             * reference, they may not produce the same result. To avoid this
             * problem, align fStart on a deterministic boundary.
             */
            fRangeStart = ts - (ts % fDeltaMax);
            fOffset = BigDecimal.valueOf(fRangeStart).multiply(fAlpha, fMc).longValue() + fBetaLong;
            delta = Math.abs(ts - fRangeStart);
            fScaleMiss++;
        } else {
            fScaleHit++;
        }
        long x = (fAlphaLong * delta) >> fDeltaBits;
        return x + fOffset;
    }

    @Override
    public ITmfTimestamp transform(ITmfTimestamp timestamp) {
        return new TmfTimestamp(timestamp, apply(timestamp.getValue()));
    }

    @Override
    public long transform(long timestamp) {
        return apply(timestamp);
    }

    /**
     * A cache miss occurs when the timestamp is out of the range for integer
     * computation, and therefore requires using BigDecimal for re-scaling.
     *
     * @return number of misses
     */
    public long getCacheMisses() {
        return fScaleMiss;
    }

    /**
     * A scale hit occurs if the timestamp is in the range for fast transform.
     *
     * @return number of hits
     */
    public long getCacheHits() {
        return fScaleHit;
    }

    /**
     * Reset scale misses to zero
     */
    public void resetScaleStats() {
        fScaleMiss = 0;
        fScaleHit = 0;
    }

    @Override
    public ITmfTimestampTransform composeWith(ITmfTimestampTransform composeWith) {
        if (composeWith.equals(TmfTimestampTransform.IDENTITY)) {
            /* If composing with identity, just return this */
            return this;
        } else if (composeWith instanceof TmfTimestampTransformLinearFast) {
            /* If composeWith is a linear transform, add the two together */
            TmfTimestampTransformLinearFast ttl = (TmfTimestampTransformLinearFast) composeWith;
            BigDecimal newAlpha = fAlpha.multiply(ttl.getAlpha(), fMc);
            BigDecimal newBeta = fAlpha.multiply(ttl.getBeta(), fMc).add(fBeta);
            return TimestampTransformFactory.createLinearFast(newAlpha, newBeta);
        } else {
            /*
             * We do not know what to do with this kind of transform, just
             * return this
             */
            return this;
        }
    }

    @Override
    public ITmfTimestampTransform inverse() {
        return TimestampTransformFactory.createLinearFast(BigDecimal.ONE.divide(fAlpha, fMc),
                BigDecimal.valueOf(-1).multiply(fBeta).divide(fAlpha, fMc));
    }

    /**
     * @return the slope alpha
     */
    public BigDecimal getAlpha() {
        return fAlpha;
    }

    /**
     * @return the offset beta
     */
    public BigDecimal getBeta() {
        return fBeta;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof TmfTimestampTransformLinearFast) {
            TmfTimestampTransformLinearFast other = (TmfTimestampTransformLinearFast) obj;
            return this.getAlpha().equals(other.getAlpha()) &&
                    this.getBeta().equals(other.getBeta());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hc;
    }

}