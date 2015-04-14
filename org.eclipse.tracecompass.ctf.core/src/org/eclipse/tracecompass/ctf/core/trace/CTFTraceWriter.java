/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.trace;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.ctf.core.CTFReaderException;
import org.eclipse.tracecompass.internal.ctf.core.Activator;

/**
 * A CTF trace reader. Reads the events of a trace.
 *
 * @version 1.0
 * @author Bernd Hufmann
 * @since 1.0
 */
@NonNullByDefault
public class CTFTraceWriter {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The trace to read from.
     */
    private final CTFTrace fTrace;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a TraceReader to read a trace.
     *
     * @param trace
     *            The trace to read from.
     * @throws CTFReaderException
     *             if an error occurs
     */
    public CTFTraceWriter(CTFTrace trace) throws CTFReaderException {
        fTrace = trace;
        try (CTFTraceReader fTraceReader = new CTFTraceReader(fTrace)) {
            fTraceReader.populateIndex();
        }
    }

    public void write(long startTime, long endTime, String name) throws CTFReaderException {

        long adjustedStart = startTime - fTrace.getClock().getClockOffset();
        long adjustedEnd = endTime - fTrace.getClock().getClockOffset();
        File out = new File(name);
        if (out.exists()) {
            throw new CTFReaderException("Trace seqment cannot be created");
        }

        // create new directory
        boolean isSuccess = out.mkdir();
        if (!isSuccess) {
            throw new CTFReaderException("Trace seqment cannot be created");
        }

        Metadata metadata = new Metadata(fTrace);

        // copy metadata
        try {
            metadata.copyTo(out);
        } catch (IOException e) {
            throw new CTFReaderException("metadata couldn't be copied", e);
        }

        for (CTFStream stream : fTrace.getStreams()) {
            Set<CTFStreamInput> inputs = stream.getStreamInputs();
            for (CTFStreamInput s : inputs) {
                try (CTFStreamOutputWriter streamOutputwriter = new CTFStreamOutputWriter(checkNotNull(s), out)) {
                    streamOutputwriter.write(adjustedStart, adjustedEnd);
                } catch (IOException e) {
                    Activator.logError(e.getMessage(), e);
                }
            }
        }
    }

}
