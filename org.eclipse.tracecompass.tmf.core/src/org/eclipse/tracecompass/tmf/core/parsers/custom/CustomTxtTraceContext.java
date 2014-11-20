/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.parsers.custom;

import static org.eclipse.tracecompass.common.core.NonNullUtils.equalsNullable;

import java.util.regex.Matcher;

import org.eclipse.tracecompass.tmf.core.trace.TmfContext;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;

/**
 * Trace context for custom text traces.
 *
 * @author Patrick Tassé
 */
public class CustomTxtTraceContext extends TmfContext {

    /** Regex matcher for the first line of the trace */
    public Matcher firstLineMatcher;

    /** First line of the text file */
    public String firstLine;

    /** Position in the file where the 'current' next line is */
    public long nextLineLocation;

    /** InputLine object for the currently read line */
    public CustomTxtInputLine inputLine;

    /**
     * Constructor.
     *
     * @param location
     *            Location in the trace
     * @param rank
     *            Rank of the event at this location
     */
    public CustomTxtTraceContext(ITmfLocation location, long rank) {
        super(location, rank);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((firstLine == null) ? 0 : firstLine.hashCode());
        result = prime * result + ((firstLineMatcher == null) ? 0 : firstLineMatcher.hashCode());
        result = prime * result + ((inputLine == null) ? 0 : inputLine.hashCode());
        result = prime * result + (int) (nextLineLocation ^ (nextLineLocation >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof CustomTxtTraceContext)) {
            return false;
        }
        CustomTxtTraceContext other = (CustomTxtTraceContext) obj;
        if (!equalsNullable(firstLine, other.firstLine)) {
            return false;
        }
        if (!equalsNullable(firstLineMatcher, other.firstLineMatcher)) {
            return false;
        }
        if (!equalsNullable(inputLine, other.inputLine)) {
            return false;
        }
        if (nextLineLocation != other.nextLineLocation) {
            return false;
        }
        return true;
    }

}