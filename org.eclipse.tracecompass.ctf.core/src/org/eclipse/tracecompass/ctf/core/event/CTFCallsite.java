/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Matthew Khouzam - Initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event;

/**
 * Callsite information to help with cdt integration
 *
 * @author Matthew Khouzam
 *
 * @since 1.2
 */
public class CTFCallsite implements Comparable<CTFCallsite> {

    // Integer size
    private static final int INT_BITS = Integer.SIZE;

    // a bitmask for the size of an integer
    private static final long INT_MASK = (1L << INT_BITS) - 1;

    /**
     * The event name
     */
    private final String fEventName;

    /**
     * the file name of the callsite
     */
    private final String fFileName;

    /**
     * the instruction pointer
     */
    private final long fIp;

    /**
     * the function name
     */
    private final String fFunctionName;

    /**
     * the line number of the callsite
     */
    private final long fLineNumber;

    /**
     * The callsite constructor
     *
     * @param en
     *            The event name
     * @param func
     *            the function name
     * @param ip
     *            the instruction pointer of the callsite
     * @param fn
     *            the file name of the callsite
     * @param line
     *            the line number of the callsite
     */
    public CTFCallsite(String en, String func, long ip, String fn, long line) {
        fEventName = en;
        fFileName = fn;
        fFunctionName = func;
        fIp = ip;
        fLineNumber = line;
    }

    /**
     * @return the eventName
     */
    public String getEventName() {
        return fEventName;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fFileName;
    }

    /**
     * @return the ip
     */
    public long getIp() {
        return fIp;
    }

    /**
     * @return the functionName
     */
    public String getFunctionName() {
        return fFunctionName;
    }

    /**
     * @return the lineNumber
     */
    public long getLineNumber() {
        return fLineNumber;
    }

    /*
     * The callsites will be sorted by calling addresses. To do this we take IPs
     * (instruction pointers) and compare them. Java only supports signed
     * operation and since memory addresses are unsigned, we will convert the
     * longs into integers that contain the high and low bytes and compare them.
     */
    @Override
    public int compareTo(CTFCallsite o) {
        /*
         * mask32 is 32 zeros followed by 32 ones, when we bitwise and this it
         * will return the lower 32 bits
         */

        long other = o.fIp;
        /*
         * To get a high int: we downshift by 32 and bitwise and with the mask
         * to get rid of the sign
         *
         * To get the low int: we bitwise and with the mask.
         */
        long otherHigh = (other >> INT_BITS) & INT_MASK;
        long otherLow = other & INT_MASK;
        long ownHigh = (fIp >> INT_BITS) & INT_MASK;
        long ownLow = fIp & INT_MASK;
        /* are the high values different, if so ignore the lower values */
        if (ownHigh > otherHigh) {
            return 1;
        }
        if (ownHigh < otherHigh) {
            return -1;
        }
        /* the high values are the same, compare the lower values */
        if (ownLow > otherLow) {
            return 1;
        }
        if (ownLow < otherLow) {
            return -1;
        }
        /* the values are identical */
        return 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fEventName == null) ? 0 : fEventName.hashCode());
        result = prime * result + ((fFileName == null) ? 0 : fFileName.hashCode());
        result = prime * result + ((fFunctionName == null) ? 0 : fFunctionName.hashCode());
        result = prime * result + (int) (fIp ^ (fIp >>> 32));
        result = prime * result + (int) (fLineNumber ^ (fLineNumber >>> 32));
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
        CTFCallsite other = (CTFCallsite) obj;
        if (fEventName == null) {
            if (other.fEventName != null) {
                return false;
            }
        } else if (!fEventName.equals(other.fEventName)) {
            return false;
        }
        if (fFileName == null) {
            if (other.fFileName != null) {
                return false;
            }
        } else if (!fFileName.equals(other.fFileName)) {
            return false;
        }
        if (fFunctionName == null) {
            if (other.fFunctionName != null) {
                return false;
            }
        } else if (!fFunctionName.equals(other.fFunctionName)) {
            return false;
        }
        if (fIp != other.fIp) {
            return false;
        }
        if (fLineNumber != other.fLineNumber) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return fFileName + "/" + fFunctionName + ":" + fLineNumber; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
