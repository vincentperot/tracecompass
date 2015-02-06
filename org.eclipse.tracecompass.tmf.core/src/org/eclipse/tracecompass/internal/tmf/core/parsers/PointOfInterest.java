package org.eclipse.tracecompass.internal.tmf.core.parsers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfNanoTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;

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
public class PointOfInterest implements Comparable<PointOfInterest> {

    private static final String END = " end";
    private static final String START = " start";
    private static final String lineRegex = "^\\s*\\[(.*)\\](.*)$"; //$NON-NLS-1$
    private static final Pattern linePattern = Pattern.compile(lineRegex);

    private static final SimpleDateFormat full = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$
    private static final SimpleDateFormat hours = new SimpleDateFormat("HH:mm:ss"); //$NON-NLS-1$

    private final @NonNull ITmfTimestamp fTs;
    private final @NonNull String fTsString;
    private final @NonNull String fMessage;
    private boolean fWithDate = false;

    /**
     * Region of interest
     *
     * @param timestamp
     *            timestamp when this occurred
     * @param message
     *            the message to show
     * @param withDate
     *            does the timestamp specify the date?
     */
    public PointOfInterest(@NonNull String timestampString, @NonNull ITmfTimestamp timestamp, @NonNull String message, boolean withDate) {
        fTs = timestamp;
        fTsString = timestampString;
        fMessage = message;
        fWithDate = withDate;
    }

    /**
     * Get the timestamp of the poi
     *
     * @return the timestamp
     */
    public ITmfTimestamp getTimestamp() {
        return fTs;
    }

    /**
     * Get the poi's message
     *
     * @return the message
     */
    public String getMessage() {
        return fMessage;
    }

    /**
     * Gets an offsetTimestamp if it fits
     *
     * @param range
     *            the timestamp
     * @return the timestamp or null
     */
    public ITmfTimestamp getOffsetTimestamp(TmfTimeRange range) {
        if (fWithDate) {
            if (range.contains(getTimestamp())) {
                return getTimestamp();
            }
            return null;
        }
        ITmfTimestamp startTime = range.getStartTime();
        TmfNanoTimestamp tmfNanoTimestamp = new TmfNanoTimestamp(startTime);
        long startValue = tmfNanoTimestamp.getValue() ;
        long parseValue;
        try {
            parseValue = TmfTimestampFormat.getDefaulTimeFormat().parseValue(fTsString, startValue);
            TmfNanoTimestamp ts = new TmfNanoTimestamp(parseValue);
            if(range.contains(ts)){
                return ts;
            }
        } catch (ParseException e) {
        }
        return null;
    }

    @Override
    public String toString() {
        return "PointOfInterest [fTs=" + fTs + ", fMessage=" + fMessage + ", fWithDate=" + fWithDate + "]";
    }

    @Override
    public int compareTo(PointOfInterest o) {
        int tsCompare = getTimestamp().compareTo(o.getTimestamp());
        if (tsCompare == 0) {
            return getMessage().compareTo(getMessage());
        }
        return tsCompare;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + fMessage.hashCode();
        result = prime * result + fTs.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PointOfInterest other = (PointOfInterest) obj;
        if (!fMessage.equals(other.fMessage)) {
            return false;
        }
        if (!fTs.equals(other.fTs)) {
            return false;
        }
        return true;
    }

    /**
     * Parse a String to markers
     *
     * @param contents
     *            the string
     * @return a collection of markers cannot be null
     */
    public static @NonNull Collection<PointOfInterest> parse(String contents) {
        Collection<PointOfInterest> markers = new TreeSet<>();
        String[] lines = contents.split("\n"); //$NON-NLS-1$
        for (String line : lines) {
            markers.addAll(parseLine(NonNullUtils.nullToEmptyString(line)));
        }
        return markers;
    }

    private static @NonNull Collection<PointOfInterest> parseLine(@NonNull String line) {
        Collection<PointOfInterest> markers = new ArrayList<>();
        Matcher matcher = linePattern.matcher(line);
        if (matcher.matches()) {
            String timeRange = matcher.group(1);
            String[] times = timeRange.split(","); //$NON-NLS-1$
            if (times.length == 1) {
                extractMarkers(markers, matcher, times[0].trim(), NonNullUtils.nullToEmptyString(null));
            }
            if (times.length == 2) {
                extractMarkers(markers, matcher, times[0].trim(), START);
                extractMarkers(markers, matcher, times[1].trim(), END);
            }
        }
        return markers;
    }

    private static void extractMarkers(Collection<PointOfInterest> markers, Matcher matcher, String timeString, String suffix) {
        // parse time
        ITmfTimestamp timestamp = extractTimestamp(timeString);
        boolean hasDate = false;
        try {
            full.parse(timeString);
            hasDate = true;
        } catch (ParseException e) {

        }
        if (timestamp != null) {
            PointOfInterest marker = new PointOfInterest(timeString, timestamp, matcher.group(2) + suffix, hasDate);
            markers.add(marker);
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
            decimal = Long.parseLong(timeString.split("\\.")[1]);
        }
        return decimal;
    }
}
