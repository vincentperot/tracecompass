/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.trace;

import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.tmf.core.trace.TraceValidationStatus;

/**
 * Trace validation status that contains a CTF trace.
 *
 * @since 1.0
 */
public class CtfTraceValidationStatus extends TraceValidationStatus {

    private final CTFTrace fTrace;

    /**
     * Constructor
     *
     * @param confidence
     *            the confidence level, 0 is lowest
     * @param pluginId
     *            the unique identifier of the relevant plug-in
     * @param trace
     *            the CTF trace
     */
    public CtfTraceValidationStatus(int confidence, String pluginId, CTFTrace trace) {
        super(confidence, pluginId);
        fTrace = trace;
    }

    /**
     * Get the CTF trace
     *
     * @return the CTF trace
     */
    public CTFTrace getTrace() {
        return fTrace;
    }

}
