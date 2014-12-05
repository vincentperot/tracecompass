/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.ctf.core.trace;

import java.io.IOException;

import org.eclipse.tracecompass.ctf.core.trace.event.EventDefinition;
import org.eclipse.tracecompass.ctf.core.trace.event.IEventDeclaration;
import org.eclipse.tracecompass.ctf.core.types.ICompositeDeclaration;
/**
 * A reader for an ICTFStreamInput
 */
public interface ICTFStreamInputReader extends AutoCloseable {

    /**
     * Dispose the StreamInputReader, closes the file channel and its packet
     * reader
     *
     * @throws IOException
     *             If an I/O error occurs
     */
    @Override
    void close() throws IOException;

    /**
     * Gets the current event in this stream
     *
     * @return the current event in the stream, null if the stream is
     *         finished/empty/malformed
     */
    EventDefinition getCurrentEvent();

    /**
     * Gets the name of the stream (it's an id and a number)
     *
     * @return gets the stream name (it's a number)
     */
    int getName();

    /**
     * Gets the filename of the stream being read
     *
     * @return The filename of the stream being read
     */
    String getFilename();

    /**
     * Gets the event definition set for this StreamInput
     *
     * @return Unmodifiable set with the event definitions
     */
    Iterable<IEventDeclaration> getEventDeclarations();

    /**
     * Get if the trace is to read live or not
     *
     * @return whether the trace is live or not
     */
    boolean isLive();

    /**
     * Get the event context of the stream
     *
     * @return the event context declaration of the stream
     */
    ICompositeDeclaration getStreamEventContextDecl();

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Reads the next event in the current event variable.
     *
     * @return If an event has been successfully read.
     * @throws CTFReaderException
     *             if an error occurs
     */
    CTFResponse readNextEvent() throws CTFReaderException;

    /**
     * Changes the location of the trace file reader so that the current event
     * is the first event with a timestamp greater or equal the given timestamp.
     *
     * @param timestamp
     *            The timestamp to seek to.
     * @return The offset compared to the current position
     * @throws CTFReaderException
     *             if an error occurs
     */
    long seek(long timestamp) throws CTFReaderException;

}