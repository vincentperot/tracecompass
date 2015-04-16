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

package org.eclipse.tracecompass.internal.ctf.core.trace.writer;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.trace.CTFStream;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.core.trace.ICTFStreamInput;
import org.eclipse.tracecompass.ctf.core.trace.Metadata;
import org.eclipse.tracecompass.ctf.core.trace.reader.CTFReaderException;
import org.eclipse.tracecompass.ctf.core.trace.writer.CTFWriterException;
import org.eclipse.tracecompass.ctf.core.trace.writer.ICTFTraceCropper;
import org.eclipse.tracecompass.internal.ctf.core.trace.CTFStreamInput;
import org.eclipse.tracecompass.internal.ctf.core.trace.reader.CTFTraceReader;

/**
 * A CTF trace reader. Reads the events of a trace.
 *
 * @version 1.0
 * @author Bernd Hufmann
 * @since 1.0
 */
@NonNullByDefault
public class CTFTraceWriter implements ICTFTraceCropper {

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
    public CTFTraceWriter(CTFTrace trace) throws CTFException {
        fTrace = trace;
        try (CTFTraceReader fTraceReader = (CTFTraceReader)fTrace.createReader()) {
            fTraceReader.populateIndex();
        }
    }

    @Override
    public void copyPackets(long startTime, long endTime, @Nullable String name) throws CTFWriterException {

        long adjustedStart = startTime - fTrace.getClock().getClockOffset();
        long adjustedEnd = endTime - fTrace.getClock().getClockOffset();
        File out = new File(name);
        if (out.exists()) {
            throw new CTFWriterException("Trace seqment cannot be created"); //$NON-NLS-1$
        }

        // create new directory
        boolean isSuccess = out.mkdir();
        if (!isSuccess) {
            throw new CTFWriterException("Trace seqment cannot be created"); //$NON-NLS-1$
        }

        Metadata metadata = new Metadata(fTrace);

        // copy metadata
        try {
            metadata.copyTo(out);
        } catch (IOException e) {
            throw new CTFWriterException("metadata couldn't be copied", e); //$NON-NLS-1$
        }

        for (CTFStream stream : fTrace.getStreams()) {
            Set<ICTFStreamInput> inputs = stream.getStreamInputs();
            for (ICTFStreamInput s : inputs) {
                if (s instanceof CTFStreamInput) {
                    CTFStreamInput streamInput = (CTFStreamInput) s;
                    ICTFStreamCropper streamOutputwriter = new CTFStreamOutputWriter(streamInput, out);
                    streamOutputwriter.copyPackets(adjustedStart, adjustedEnd);
                }
            }
        }
    }

}
