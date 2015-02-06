/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Patrick Tasse - Support selection range
 *   Xavier Raynaud - Support filters tracking
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.trace;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.filter.ITmfFilter;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;

/**
 * Context of a trace, which is the representation of the "view" the user
 * currently has on this trace (window time range, selected time or time range).
 *
 * TODO could be extended to support the notion of current location too.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
@NonNullByDefault
final class TmfTraceContext {

    static final TmfTraceContext NULL_CONTEXT =
            new TmfTraceContext(TmfTimestamp.BIG_CRUNCH, TmfTimestamp.BIG_CRUNCH, TmfTimeRange.NULL_RANGE, null);

    private final TmfTimeRange fSelection;
    private final TmfTimeRange fWindowRange;
    private final @Nullable IFile fEditorFile;
    private final @Nullable ITmfFilter fFilter;

    public TmfTraceContext(ITmfTimestamp beginTs, ITmfTimestamp endTs, TmfTimeRange tr, @Nullable IFile editorFile) {
        fSelection = new TmfTimeRange(beginTs, endTs);
        fWindowRange = tr;
        fEditorFile = editorFile;
        fFilter = null;
    }

    public TmfTraceContext(TmfTraceContext prevCtx, ITmfTimestamp beginTs, ITmfTimestamp endTs) {
        fSelection = new TmfTimeRange(beginTs, endTs);
        fWindowRange = prevCtx.fWindowRange;
        fEditorFile = prevCtx.fEditorFile;
        fFilter = prevCtx.fFilter;
    }

    public TmfTraceContext(TmfTraceContext prevCtx, TmfTimeRange tr) {
        fSelection = prevCtx.fSelection;
        fWindowRange = tr;
        fEditorFile = prevCtx.fEditorFile;
        fFilter = prevCtx.fFilter;
    }

    /**
     * @param prevCtx
     *              The previous context
     * @param filter
     *              The applied filter
     * @since 2.2
     */
    public TmfTraceContext(TmfTraceContext prevCtx, @Nullable ITmfFilter filter) {
        fSelection = prevCtx.fSelection;
        fWindowRange = prevCtx.fWindowRange;
        fEditorFile = prevCtx.fEditorFile;
        fFilter = filter;
    }

    public ITmfTimestamp getSelectionBegin() {
        return fSelection.getStartTime();
    }

    public ITmfTimestamp getSelectionEnd() {
        return fSelection.getEndTime();
    }

    public TmfTimeRange getWindowRange() {
        return fWindowRange;
    }

    public @Nullable IFile getEditorFile() {
        return fEditorFile;
    }

    /**
     * @return the current filter applied to the trace
     * @since 2.2
     */
    public @Nullable ITmfFilter getFilter() {
        return fFilter;
    }

    public boolean isValid() {
        if (fSelection.getStartTime().compareTo(TmfTimestamp.ZERO) <= 0 ||
                fSelection.getEndTime().compareTo(TmfTimestamp.ZERO) <= 0 ||
                fWindowRange.getEndTime().compareTo(fWindowRange.getStartTime()) <= 0) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[fSelection=" + fSelection + //$NON-NLS-1$
                ", fWindowRange=" + fWindowRange + ']'; //$NON-NLS-1$
    }
}
