/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ctf.core;

import java.lang.reflect.Field;

import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.eclipse.tracecompass.tmf.ctf.core.trace.iterator.CtfIterator;

/**
 * Utility class to work around the problem that {@link CtfIterator} needs to
 * access a {@link CtfTmfTrace}'s {@link CTFTrace}, while we do not want to
 * have {@link CTFTrace}'s (which are part of another plugin) in the API.
 *
 * @author Alexandre Montplaisir
 */
public final class CtfTraceUtils {

    private static final Field CTF_TRACE_FIELD;

    static {
        try {
            CTF_TRACE_FIELD = CtfTmfTrace.class.getDeclaredField("fTrace"); //$NON-NLS-1$
        } catch (NoSuchFieldException | SecurityException e) {
            /* If we ever rename this field, we need to know right away! */
            throw new IllegalStateException(e);
        }
        CTF_TRACE_FIELD.setAccessible(true);
    }

    private CtfTraceUtils() {}

    /**
     * Retrieve the {@link CTFTrace} of a {@link CtfTmfTrace}.
     *
     * @param trace
     *            The CtfTmfTrace
     * @return The CTFTrace contained within
     */
    public static CTFTrace getCtfTrace(CtfTmfTrace trace) {
        try {
            return (CTFTrace) CTF_TRACE_FIELD.get(trace);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
