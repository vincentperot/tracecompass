package org.eclipse.tracecompass.internal.tmf.core.synchronization;

import java.math.BigDecimal;
import java.math.MathContext;

import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * Fast linear timestamp transform.
 *
 * Reduce the use of BigDecimal for an interval of time where the transform can
 * be computed only with integer math. By rearranging the linear equation to:
 *
 * alphaLong = alpha * m f(t) = (alphaLong * (t - ts)) / m + beta + c
 *
 * The slope applies to a relative time reference instead of absolute timestamp
 * from epoch. The constant part of the slope for the interval is added to beta.
 * It reduces the width of slope and timestamp to 32-bit integers, and the
 * result fits a 64-bit value. Using standard integer arithmetic yield speedup
 * compared to BigDecimal, while preserving precision. Because of rounding,
 * there may be a slight difference of +/- 3ns between the value computed by the
 * fast transform compared to BigDecimal. The timestamps produced are indepotent
 * (transforming the same timestamp will always produce the same result), and
 * the timestamps are monotonic.
 *
 * The number of bits available for the cache range is variable. The variable
 * alphaLong must be a 32-bit value. We reserve 30-bit for the decimal part to
 * reach the nanosecond precision. If the slope is greater than 1.0, the shift
 * is reduced to avoid overflow. It reduces the useful cache range, but the
 * result is correct even for large (10e9) slope.
 *
 * @author Francis Giraldeau <francis.giraldeau@gmail.com>
 *
 */
public class TmfTimestampTransformLinearFast extends TmfTimestampTransformLinear {

    private static final long serialVersionUID = 2398540405078949739L;
    private long scaleOffset;
    private long start;
    private long fAlphaLong;
    private long fBetaLong;
    private long fScaleMiss;
    private long fScaleHit;
    private int hc;
    private int tsBitWidth;
    private static final HashFunction hf = Hashing.goodFastHash(32);

    private static final MathContext fMc = MathContext.DECIMAL128;

    /**
     * @param xform construct a fast transform for the linear transform
     */
    public TmfTimestampTransformLinearFast(TmfTimestampTransformLinear xform) {
        super(xform.getAlpha(), xform.getBeta());
        int width = Long.numberOfLeadingZeros(xform.getAlpha().longValue()) - 32;
        tsBitWidth = Math.max(Math.min(width, 30), 0);
        fAlphaLong = xform.getAlpha().multiply(BigDecimal.valueOf(1 << tsBitWidth)).longValue() ;
        fBetaLong = xform.getBeta().longValue();
        start = 0L;
        scaleOffset = 0L;
        fScaleMiss = 0;
        fScaleHit = 0;
        hc = hf.newHasher()
                .putLong(getAlpha().longValue())
                .putLong(getBeta().longValue())
                .hash()
                .asInt();
    }

    private long apply(long ts) {
        // rescale if we exceed the safe range
        long delta = ts - start;
        if (delta > (1 << tsBitWidth) || delta < 0) {
            start = ts - (ts % (1 << tsBitWidth));
            scaleOffset = BigDecimal.valueOf(start).multiply(getAlpha(), fMc).longValue() + fBetaLong;
            delta = Math.abs(ts - start);
            fScaleMiss++;
        } else {
            fScaleHit++;
        }
        long x = (fAlphaLong * delta) >>> tsBitWidth;
        return x + scaleOffset;
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
    public boolean equals(Object other) {
        if (other instanceof TmfTimestampTransformLinearFast) {
            TmfTimestampTransformLinearFast that = (TmfTimestampTransformLinearFast) other;
            return this.getAlpha().equals(that.getAlpha()) &&
                    this.getBeta().equals(that.getBeta());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hc;
    }


}