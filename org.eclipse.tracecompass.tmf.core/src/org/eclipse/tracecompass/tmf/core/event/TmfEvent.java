/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
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
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * A basic implementation of ITmfEvent.
 *
 * @version 1.0
 * @author Francois Chouinard
 *
 * @see ITmfTimestamp
 * @see ITmfEventType
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
    private final ITmfEventType fType;
    private final ITmfEventField fContent;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor. Is required for extension points, but should not be
     * used normally.
     *
     * @deprecated Do not use, extension-point use only. Use
     *             {@link #TmfEvent(ITmfTrace, long, ITmfTimestamp, ITmfEventType, ITmfEventField)}
     *             instead.
     */
    @Deprecated
    public TmfEvent() {
        this(null, ITmfContext.UNKNOWN_RANK, null, null, null);
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
     * @param type
     *            the event type
     * @param content
     *            the event content (payload)
     * @since 2.0
     */
    public TmfEvent(final ITmfTrace trace,
            final long rank,
            final ITmfTimestamp timestamp,
            final ITmfEventType type,
            final ITmfEventField content) {
        fTrace = trace;
        fRank = rank;
        if (timestamp != null) {
            fTimestamp = timestamp;
        } else {
            fTimestamp = TmfTimestamp.ZERO;
        }
        fType = type;
        fContent = content;
    }

    /**
     * Copy constructor
     *
     * @param event the original event
     */
    public TmfEvent(final @NonNull ITmfEvent event) {
        fTrace = event.getTrace();
        fRank = event.getRank();
        fTimestamp = event.getTimestamp();
        fType = event.getType();
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

    /**
     * @since 2.0
     */
    @Override
    public ITmfTimestamp getTimestamp() {
        return fTimestamp;
    }

    @Override
    public ITmfEventType getType() {
        return fType;
    }

    @Override
    public ITmfEventField getContent() {
        return fContent;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fTrace == null) ? 0 : fTrace.hashCode());
        result = prime * result + (int) (fRank ^ (fRank >>> 32));
        result = prime * result + fTimestamp.hashCode();
        result = prime * result + ((fType == null) ? 0 : fType.hashCode());
        result = prime * result + ((fContent == null) ? 0 : fContent.hashCode());
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
        if (!(obj instanceof TmfEvent)) {
            return false;
        }
        final TmfEvent other = (TmfEvent) obj;
        if (fTrace == null) {
            if (other.fTrace != null) {
                return false;
            }
        } else if (!fTrace.equals(other.fTrace)) {
            return false;
        }
        if (fRank != other.fRank) {
            return false;
        }
        if (!fTimestamp.equals(other.fTimestamp)) {
            return false;
        }
        if (fType == null) {
            if (other.fType != null) {
                return false;
            }
        } else if (!fType.equals(other.fType)) {
            return false;
        }
        if (fContent == null) {
            if (other.fContent != null) {
                return false;
            }
        } else if (!fContent.equals(other.fContent)) {
            return false;
        }
        return true;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return getClass().getSimpleName() + " [fTimestamp=" + getTimestamp()
                + ", fTrace=" + getTrace() + ", fRank=" + getRank()
                +  ", fType=" + getType() + ", fContent=" + getContent()
                + "]";
    }

}
