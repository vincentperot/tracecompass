package org.eclipse.tracecompass.tmf.core.parsers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfNanoTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfRegionOfInterest;

/**
 * Parses [time(,time)] name like <code>
 * [2015-01-15 12:18:37.216484041, 2015-01-15 12:18:53.821580313] name
 * [2015-01-15 12:18:37.216484041, 2015-01-15 12:18:53.821580313] name boookmark
 * [ 2015-01-15 12:18:37.216484041] name boookmark
 * [12:18:37.216484041] name boookmark
 * [ 12:18:37.216484041, 12:18:53.821580313] name boookmark
 * [ 2014-12-12 17:29:43.802588035] name with space
 * [ 2014-12-12 17:29:43] irrational title
 * [17:29:43.802588035] rational title
 * [ 17:29:43] test test test
 * [17:29:43,17:29:44] thing
 * [12:18:37.216484041   ]
 * </code>
 *
 */
public class ReportRegionOfInterest implements ITmfRegionOfInterest {

    private static final String lineRegex = "^\\s*\\[([^\\]]*)\\](.*)$"; //$NON-NLS-1$
    private static final Pattern linePattern = Pattern.compile(lineRegex);

    private static final SimpleDateFormat full = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$
    private static final SimpleDateFormat hours = new SimpleDateFormat("HH:mm:ss"); //$NON-NLS-1$
    private static final TmfTimestampFormat dateless = new TmfTimestampFormat("HH:mm:ss.SSSSSSSSS"); //$NON-NLS-1$
    private final @NonNull TmfTimeRange fTimeRange;
    private final @NonNull String fMessage;
    private boolean fWithDate = false;

    /**
     * Region of interest
     *
     * @param timeRange
     *            timerange of the region of interest
     * @param message
     *            the message to show
     * @param withDate
     *            does the timestamp specify the date?
     */
    public ReportRegionOfInterest(@NonNull TmfTimeRange timeRange, @NonNull String message, boolean withDate) {
        fTimeRange = timeRange;
        fMessage = message;
        fWithDate = withDate;
    }

    @Override
    public ITmfTimestamp getStartTime() {
        return fTimeRange.getStartTime();
    }

    @Override
    public ITmfTimestamp getEndTime() {
        return fTimeRange.getEndTime();
    }

    @Override
    public long getDuration() {
        return fTimeRange.getEndTime().getDelta(getStartTime()).getValue();
    }

    @Override
    public String getMessage() {
        return fMessage;
    }

    /**
     * Gets an offsetTimestamp if it fits
     *
     * @param range
     *            the timestamp
     * @return the timerange or null
     */
    public TmfTimeRange getOffsetTimestamp(TmfTimeRange range) {
        if (fWithDate) {
            if (range.getIntersection(fTimeRange) != null) {
                return fTimeRange;
            }
            return null;
        }
        ITmfTimestamp startTime = range.getStartTime();
        TmfNanoTimestamp tmfNanoTimestamp = new TmfNanoTimestamp(startTime);
        long startValue = tmfNanoTimestamp.getValue();
        long parseValue;
        try {
            String tsString = getStartTime().toString(dateless);
            parseValue = TmfTimestampFormat.getDefaulTimeFormat().parseValue(tsString, startValue);
            TmfNanoTimestamp tsStart = new TmfNanoTimestamp(parseValue);
            tsString = getEndTime().toString(dateless);
            parseValue = TmfTimestampFormat.getDefaulTimeFormat().parseValue(tsString, startValue);
            TmfNanoTimestamp tsEnd = new TmfNanoTimestamp(parseValue);
            return range.getIntersection(new TmfTimeRange(tsStart, tsEnd));
        } catch (ParseException e) {
        }
        return null;
    }

    /**
     * Parse a String to markers
     *
     * @param contents
     *            the string
     * @return a collection of markers cannot be null
     */
    public static @NonNull Collection<ITmfRegionOfInterest> parse(String contents) {
        Collection<ITmfRegionOfInterest> markers = new ArrayList<>();
        String[] lines = contents.split("\n"); //$NON-NLS-1$
        for (String line : lines) {
            markers.addAll(parseLine(NonNullUtils.nullToEmptyString(line)));
        }
        return markers;
    }

    private static @NonNull Collection<ITmfRegionOfInterest> parseLine(@NonNull String line) {
        Collection<ITmfRegionOfInterest> markers = new ArrayList<>();
        Matcher matcher = linePattern.matcher(line);
        if (matcher.matches()) {
            String timeRange = matcher.group(1);
            String[] times = timeRange.split(","); //$NON-NLS-1$
            if (times.length == 1) {
                extractMarkers(markers, matcher, times[0].trim(), times[0].trim());
            }
            if (times.length == 2) {
                extractMarkers(markers, matcher, times[0].trim(), times[1].trim());
            }
        }
        return markers;
    }

    private static void extractMarkers(Collection<ITmfRegionOfInterest> markers, Matcher matcher, String timeStart, String timeEnd) {
        // parse time
        ITmfTimestamp timestampStart = extractTimestamp(timeStart);
        ITmfTimestamp timestampEnd = extractTimestamp(timeEnd);
        boolean hasDate = false;
        try {
            full.parse(timeStart);
            hasDate = true;
        } catch (ParseException e) {

        }
        if (timestampStart != null) {
            ReportRegionOfInterest roi = new ReportRegionOfInterest(new TmfTimeRange(timestampStart, timestampEnd), NonNullUtils.nullToEmptyString(matcher.group(2)), hasDate);
            markers.add(roi);
        }
    }

    private static ITmfTimestamp extractTimestamp(String timeString) {
        long decimal = 0;
        String decimalDelimiter = "."; //$NON-NLS-1$
        if (timeString.contains(decimalDelimiter)) {
            decimal = extractDecimal(timeString);
        }

        Date timestamp = extractDate(timeString.split("\\.")[0]); //$NON-NLS-1$
        ITmfTimestamp ts = new TmfNanoTimestamp(timestamp.getTime() * 1000000L + decimal);
        return ts;
    }

    private static Date extractDate(String timeString) {
        Date timestamp = null;

        try {
            timestamp = full.parse(timeString);
        } catch (ParseException e) {

        }
        if (timestamp == null) {
            try {
                timestamp = hours.parse(timeString);
            } catch (ParseException e) {
                timestamp = new Date();
            }
        }
        return timestamp;
    }

    private static long extractDecimal(String timeString) {
        long decimal = 0;
        String decimalDelimiter = "."; //$NON-NLS-1$
        if (timeString.contains(decimalDelimiter)) {
            String decimals = timeString.split("\\.")[1]; //$NON-NLS-1$
            // to avoid numberformatexception
            if (!decimals.equals("000000000")) { //$NON-NLS-1$
                decimal = Long.parseLong(decimals);
            }
        }
        return decimal;
    }

}
