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

import org.eclipse.tracecompass.ctf.core.trace.event.EventDefinition;
import org.eclipse.tracecompass.ctf.core.trace.event.IEventDeclaration;

/**
 * Trace reader interface
 */
public interface ICTFTraceReader extends AutoCloseable {

    /**
     * Return the start time of this trace (== timestamp of the first event)
     *
     * @return the trace start time
     */
    long getStartTime();

    /**
     * Update the priority queue to make it match the parent trace
     *
     * @throws CTFReaderException
     *             An error occured
     *
     * @since 3.0
     */
    void update() throws CTFReaderException;

    /**
     * Gets an iterable of the stream input readers, useful for foreaches
     *
     * @return the iterable of the stream input readers
     * @since 3.0
     */
    Iterable<IEventDeclaration> getEventDeclarations();

    /**
     * Get the current event, which is the current event of the trace file
     * reader with the lowest timestamp.
     *
     * @return An event definition, or null of the trace reader reached the end
     *         of the trace.
     */
    EventDefinition getCurrentEventDef();

    /**
     * Go to the next event.
     *
     * @return True if an event was read.
     * @throws CTFReaderException
     *             if an error occurs
     */
    boolean advance() throws CTFReaderException;

    /**
     * Does the trace have more events?
     *
     * @return true if yes.
     */
    boolean hasMoreEvents();

    /**
     * Gets the last event timestamp that was read. This is NOT necessarily the
     * last event in a trace, just the last one read so far.
     *
     * @return the last event
     */
    long getEndTime();

    /**
     * Get if the trace is to read live or not
     *
     * @return whether the trace is live or not
     * @since 3.0
     *
     */
    boolean isLive();

    /**
     * Gets the parent trace
     *
     * @return the parent trace
     */
    CTFTrace getTrace();

    @Override
    public void close();

}