/*******************************************************************************
 * Copyright (c) 2009, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Francois Chouinard - Initial API and implementation, updated as per TMF Event Model 1.0
 *     Alexandre Montplaisir - Made immutable, consolidated constructors
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * A basic implementation of ITmfEvent.
 *
 * @author Francois Chouinard
 *
 * @see ITmfTimestamp
 * @see ITmfEventField
 * @see ITmfTrace
 */
public class TmfEvent extends PlatformObject implements ITmfEvent {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final ITmfTrace fTrace;
    private final long fRank;
    private final @NonNull ITmfTimestamp fTimestamp;
    private final @NonNull String fName;
    private final ITmfEventField fContent;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor. Is required for extension points, but should not be
     * used normally.
     *
     * @deprecated Do not use, extension-point use only. Use
     *             {@link #TmfEvent(ITmfTrace, long, ITmfTimestamp, String, ITmfEventField)}
     *             instead.
     */
    @Deprecated
    public TmfEvent() {
        this(null, ITmfContext.UNKNOWN_RANK, null, "", null); //$NON-NLS-1$
    }

    /**
     * Full constructor
     *
     * @param trace
     *            the parent trace
     * @param rank
     *            the event rank (in the trace). You can use
     *            {@link ITmfContext#UNKNOWN_RANK} as default value
     * @param timestamp
     *            the event timestamp
     * @param name
     *            the event name
     * @param content
     *            the event content (payload)
     * @since 2.0
     */
    public TmfEvent(final ITmfTrace trace,
            final long rank,
            final ITmfTimestamp timestamp,
            final @NonNull String name,
            final ITmfEventField content) {
        fTrace = trace;
        fRank = rank;
        if (timestamp != null) {
            fTimestamp = timestamp;
        } else {
            fTimestamp = TmfTimestamp.ZERO;
        }
        fName = name;
        fContent = content;
    }

    /**
     * Copy constructor
     *
     * @param event the original event
     * @since 2.0
     */
    public TmfEvent(final @NonNull TmfEvent event) {
        fTrace = event.getTrace();
        fRank = event.getRank();
        fTimestamp = event.getTimestamp();
        fName = event.getName();
        fContent = event.getContent();
    }

    // ------------------------------------------------------------------------
    // ITmfEvent
    // ------------------------------------------------------------------------

    @Override
    public ITmfTrace getTrace() {
        ITmfTrace trace = fTrace;
        if (trace == null) {
            throw new IllegalStateException("Null traces are only allowed on special kind of events and getTrace() should not be called on them"); //$NON-NLS-1$
        }
        return trace;
    }

    @Override
    public long getRank() {
        return fRank;
    }

    @Override
    public ITmfTimestamp getTimestamp() {
        return fTimestamp;
    }


    @Override
    public ITmfEventField getContent() {
        return fContent;
    }

    /**
     * @since 1.0
     */
    @Override
    public @NonNull String getName() {
        return fName;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fTrace == null) ? 0 : getTrace().hashCode());
        result = prime * result + (int) (getRank() ^ (getRank() >>> 32));
        result = prime * result + getTimestamp().hashCode();
        result = prime * result + getName().hashCode();
        result = prime * result + ((getContent() == null) ? 0 : getContent().hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        /* Two events must be of the exact same class to be equal */
        if (!(this.getClass().equals(obj.getClass()))) {
            return false;
        }
        final TmfEvent other = (TmfEvent) obj;

        if (fTrace == null) {
            if (other.fTrace != null) {
                return false;
            }
        } else if (!getTrace().equals(other.getTrace())) {
            return false;
        }

        if (getRank() != other.getRank()) {
            return false;
        }
        if (!getTimestamp().equals(other.getTimestamp())) {
            return false;
        }
        if (!NonNullUtils.equalsNullable(getName(), other.getName())) {
            return false;
        }
        if (!NonNullUtils.equalsNullable(getContent(), other.getContent())) {
            return false;
        }
        return true;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return getClass().getSimpleName() + " [fTimestamp=" + getTimestamp()
                + ", fTrace=" + getTrace() + ", fRank=" + getRank()
                +  ", fName=" + getName() + ", fContent=" + getContent()
                + "]";
    }

}
