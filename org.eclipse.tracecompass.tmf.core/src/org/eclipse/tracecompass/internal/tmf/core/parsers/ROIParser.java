package org.eclipse.tracecompass.internal.tmf.core.parsers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.NonNullUtils;

/**
 * Parses [time(,time)] name like
 * <code>
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
public class ROIParser {

    private static final String END = " end";
    private static final String START = " start";
    private static final String lineRegex = "^\\[(.*)\\](.*)$"; //$NON-NLS-1$
    private static final Pattern linePattern = Pattern.compile(lineRegex);
    private static IResource res;

    private static final SimpleDateFormat full = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); //$NON-NLS-1$
    private static final SimpleDateFormat hours = new SimpleDateFormat("hh:mm:ss"); //$NON-NLS-1$

    /**
     * Parse a String to markers
     * @param contents the string
     * @return a collection of markers cannot be null
     */
    public static @NonNull Collection<IMarker> parse(String contents) {
        Collection<IMarker> markers = new TreeSet<>();
        String[] lines = contents.split("\n"); //$NON-NLS-1$
        /**

         */
        for (String line : lines) {
            Collection<IMarker> marker = parseLine(line);
            if (marker != null) {
                markers.addAll(marker);
            }

        }
        return markers;
    }

    private static Collection<IMarker> parseLine(String line) {
        Collection<IMarker> markers = new ArrayList<>();
        Matcher matcher = linePattern.matcher(line);
        if (matcher.matches()) {
            String timeRange = matcher.group(0);
            String[] times = timeRange.split(","); //$NON-NLS-1$
            for (String time : times) {
                String[] timeParts = time.split("\\."); //$NON-NLS-1$
                if (timeParts.length == 1) {
                    extractMarkers(markers, matcher, timeParts[0].trim(), NonNullUtils.nullToEmptyString(null));
                }
                if (timeParts.length == 2) {
                    extractMarkers(markers, matcher, timeParts[0].trim(), START);
                    extractMarkers(markers, matcher, timeParts[1].trim(), END);
                }
            }
        }
        return markers;
    }

    private static void extractMarkers(Collection<IMarker> markers, Matcher matcher, String timeString, String suffix) {
        try {
            // parse time
            Date timestamp = extractTimestamp(timeString);
            if (timestamp != null) {
                IMarker marker = res.createMarker(matcher.group(1) + suffix);
                marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
                markers.add(marker);
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    private static Date extractTimestamp(String timeString) {
        Date timestamp = null;

        try {
            timestamp = full.parse(timeString);
        } catch (ParseException e) {

        }
        if (timestamp == null) {
            try {
                timestamp = hours.parse(timeString);
            } catch (ParseException e) {

            }
        }
        return timestamp;
    }
}
