/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *     Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.ctf.core.trace.reader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import org.eclipse.tracecompass.ctf.core.CTFReaderException;
import org.eclipse.tracecompass.ctf.core.event.EventDefinition;
import org.eclipse.tracecompass.ctf.core.event.IEventDeclaration;
import org.eclipse.tracecompass.ctf.core.trace.CTFResponse;
import org.eclipse.tracecompass.ctf.core.trace.CTFStream;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.core.trace.ICTFStreamInput;
import org.eclipse.tracecompass.ctf.core.trace.reader.ICTFStreamInputReader;
import org.eclipse.tracecompass.ctf.core.trace.reader.ICTFTraceReader;
import org.eclipse.tracecompass.internal.ctf.core.Activator;
import org.eclipse.tracecompass.internal.ctf.core.trace.StreamInputReaderTimestampComparator;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

/**
 * A CTF trace reader. Reads the events of a trace.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Alexandre Montplaisir
 */
public class CTFTraceReader implements ICTFTraceReader {

    private static final int LINE_LENGTH = 60;

    private static final int MIN_PRIO_SIZE = 16;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The trace to read from.
     */
    private final CTFTrace fTrace;

    /**
     * Vector of all the trace file readers.
     */
    private final List<CTFStreamInputReader> fStreamInputReaders =
            Collections.synchronizedList(new ArrayList<CTFStreamInputReader>());

    /**
     * Priority queue to order the trace file readers by timestamp.
     */
    private PriorityQueue<CTFStreamInputReader> fPrio;

    /**
     * Array to count the number of event per trace file.
     */
    private long[] fEventCountPerTraceFile;

    /**
     * Timestamp of the first event in the trace
     */
    private long fStartTime;

    /**
     * Timestamp of the last event read so far
     */
    private long fEndTime;

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
    public CTFTraceReader(CTFTrace trace) throws CTFReaderException {
        fTrace = trace;
        fStreamInputReaders.clear();

        /**
         * Create the trace file readers.
         */
        createStreamInputReaders();

        /**
         * Populate the timestamp-based priority queue.
         */
        populateStreamInputReaderHeap();

        /**
         * Get the start Time of this trace bear in mind that the trace could be
         * empty.
         */
        fStartTime = 0;
        if (hasMoreEvents()) {
            fStartTime = getTopStream().getCurrentEvent().getTimestamp();
            setEndTime(fStartTime);
        }
    }

    /**
     * @since 1.0
     */
    @Override
    public ICTFTraceReader copyFrom() throws CTFReaderException {
        CTFTraceReader newReader = null;

        newReader = new CTFTraceReader(fTrace);
        newReader.fStartTime = fStartTime;
        newReader.setEndTime(fEndTime);
        return newReader;
    }

    /**
     * Dispose the CTFTraceReader
     */
    @Override
    public void close() {
        synchronized (fStreamInputReaders) {
            for (CTFStreamInputReader reader : fStreamInputReaders) {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Activator.logError(e.getMessage(), e);
                    }
                }
            }
            fStreamInputReaders.clear();
        }
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    @Override
    public long getStartTime() {
        return fStartTime;
    }

    /**
     * Set the trace's end time
     *
     * @param endTime
     *            The end time to use
     */
    protected final void setEndTime(long endTime) {
        fEndTime = endTime;
    }

    /**
     * Get the priority queue of this trace reader.
     *
     * @return The priority queue of input readers
     */
    protected PriorityQueue<CTFStreamInputReader> getPrio() {
        return fPrio;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Creates one trace file reader per trace file contained in the trace.
     *
     * @throws CTFReaderException
     *             if an error occurs
     */
    private void createStreamInputReaders() throws CTFReaderException {
        /*
         * For each stream.
         */
        for (CTFStream stream : fTrace.getStreams()) {
            Set<ICTFStreamInput> streamInputs = stream.getStreamInputs();

            /*
             * For each trace file of the stream.
             */
            for (ICTFStreamInput streamInput : streamInputs) {

                /*
                 * Create a reader and add it to the group.
                 */
                fStreamInputReaders.add(new CTFStreamInputReader(streamInput));
            }
        }

        /*
         * Create the array to count the number of event per trace file.
         */
        fEventCountPerTraceFile = new long[fStreamInputReaders.size()];
    }

    @Override
    public void update() throws CTFReaderException {
        Set<CTFStreamInputReader> readers = new HashSet<>();
        for (CTFStream stream : fTrace.getStreams()) {
            Set<ICTFStreamInput> streamInputs = stream.getStreamInputs();
            for (ICTFStreamInput streamInput : streamInputs) {
                /*
                 * Create a reader.
                 */
                CTFStreamInputReader streamInputReader = new CTFStreamInputReader(
                        streamInput);

                /*
                 * Add it to the group.
                 */
                if (!fStreamInputReaders.contains(streamInputReader)) {
                    streamInputReader.readNextEvent();
                    fStreamInputReaders.add(streamInputReader);
                    readers.add(streamInputReader);
                }
            }
        }
        long[] temp = fEventCountPerTraceFile;
        fEventCountPerTraceFile = new long[readers.size() + temp.length];
        for (CTFStreamInputReader reader : readers) {
            fPrio.add(reader);
        }
        for (int i = 0; i < temp.length; i++) {
            fEventCountPerTraceFile[i] = temp[i];
        }
    }

    @Override
    public Iterable<IEventDeclaration> getEventDeclarations() {
        ImmutableSet.Builder<IEventDeclaration> builder = new Builder<>();
        for (ICTFStreamInputReader sir : fStreamInputReaders) {
            builder.addAll(sir.getEventDeclarations());
        }
        return builder.build();
    }

    /**
     * Initializes the priority queue used to choose the trace file with the
     * lower next event timestamp.
     *
     * @throws CTFReaderException
     *             if an error occurs
     */
    private void populateStreamInputReaderHeap() throws CTFReaderException {
        if (fStreamInputReaders.isEmpty()) {
            fPrio = new PriorityQueue<>(MIN_PRIO_SIZE,
                    new StreamInputReaderTimestampComparator());
            return;
        }

        /*
         * Create the priority queue with a size twice as bigger as the number
         * of reader in order to avoid constant resizing.
         */
        fPrio = new PriorityQueue<>(
                Math.max(fStreamInputReaders.size() * 2, MIN_PRIO_SIZE),
                new StreamInputReaderTimestampComparator());

        int pos = 0;

        for (CTFStreamInputReader reader : fStreamInputReaders) {
            /*
             * Add each trace file reader in the priority queue, if we are able
             * to read an event from it.
             */
            reader.setParent(this);
            CTFResponse readNextEvent = reader.readNextEvent();
            if (readNextEvent == CTFResponse.OK || readNextEvent == CTFResponse.WAIT) {
                fPrio.add(reader);

                fEventCountPerTraceFile[pos] = 0;
                reader.setName(pos);

                pos++;
            }
        }
    }

    @Override
    public EventDefinition getCurrentEventDef() {
        ICTFStreamInputReader top = getTopStream();
        return (top != null) ? top.getCurrentEvent() : null;
    }

    @Override
    public boolean advance() throws CTFReaderException {
        /*
         * Remove the reader from the top of the priority queue.
         */
        CTFStreamInputReader top = fPrio.poll();

        /*
         * If the queue was empty.
         */
        if (top == null) {
            return false;
        }
        /*
         * Read the next event of this reader.
         */
        switch (top.readNextEvent()) {
        case OK: {
            /*
             * Add it back in the queue.
             */
            fPrio.add(top);
            final long topEnd = fTrace.timestampCyclesToNanos(top.getCurrentEvent().getTimestamp());
            setEndTime(Math.max(topEnd, getEndTime()));
            fEventCountPerTraceFile[top.getName()]++;

            if (top.getCurrentEvent() != null) {
                fEndTime = Math.max(top.getCurrentEvent().getTimestamp(),
                        fEndTime);
            }
            break;
        }
        case WAIT: {
            fPrio.add(top);
            break;
        }
        case FINISH:
            break;
        case ERROR:
        default:
            // something bad happend
        }
        /*
         * If there is no reader in the queue, it means the trace reader reached
         * the end of the trace.
         */
        return hasMoreEvents();
    }

    /* (non-Javadoc)
     * @see org.eclipse.tracecompass.ctf.core.trace.ICTFTraceReader#goToLastEvent()
     */
    @Override
    public void goToLastEvent() throws CTFReaderException {
        seek(getEndTime());
        while (fPrio.size() > 1) {
            advance();
        }
    }

    @Override
    public boolean seek(long timestamp) throws CTFReaderException {
        /*
         * Remove all the trace readers from the priority queue
         */
        fPrio.clear();
        for (CTFStreamInputReader streamInputReader : fStreamInputReaders) {
            /*
             * Seek the trace reader.
             */
            streamInputReader.seek(timestamp);

            /*
             * Add it to the priority queue if there is a current event.
             */
            if (streamInputReader.getCurrentEvent() != null) {
                fPrio.add(streamInputReader);
            }
        }
        return hasMoreEvents();
    }

    /**
     * Gets the stream with the oldest event
     *
     * @return the stream with the oldest event
     */
    public CTFStreamInputReader getTopStream() {
        return fPrio.peek();
    }

    @Override
    public final boolean hasMoreEvents() {
        return fPrio.size() > 0;
    }

    /**
     * Prints the event count stats.
     */
    public void printStats() {
        printStats(LINE_LENGTH);
    }

    /**
     * Prints the event count stats.
     *
     * @param width
     *            Width of the display.
     */
    public void printStats(int width) {
        int numEvents = 0;
        if (width == 0) {
            return;
        }

        for (long i : fEventCountPerTraceFile) {
            numEvents += i;
        }

        for (int j = 0; j < fEventCountPerTraceFile.length; j++) {
            ICTFStreamInputReader se = fStreamInputReaders.get(j);

            long len = (width * fEventCountPerTraceFile[se.getName()])
                    / numEvents;

            StringBuilder sb = new StringBuilder(se.getFilename());
            sb.append("\t["); //$NON-NLS-1$

            for (int i = 0; i < len; i++) {
                sb.append('+');
            }

            for (long i = len; i < width; i++) {
                sb.append(' ');
            }

            sb.append("]\t" + fEventCountPerTraceFile[se.getName()] + " Events"); //$NON-NLS-1$//$NON-NLS-2$
            Activator.log(sb.toString());
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.tracecompass.ctf.core.trace.ICTFTraceReader#getEndTime()
     */
    @Override
    public long getEndTime() {
        return fEndTime;
    }

    /* (non-Javadoc)
     * @see org.eclipse.tracecompass.ctf.core.trace.ICTFTraceReader#setLive(boolean)
     */
    @Override
    public void setLive(boolean live) {
        for (CTFStreamInputReader s : fPrio) {
            s.setLive(live);
        }
    }

    @Override
    public boolean isLive() {
        return getTopStream().isLive();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (int) (fStartTime ^ (fStartTime >>> 32));
        result = (prime * result) + fStreamInputReaders.hashCode();
        result = (prime * result) + ((fTrace == null) ? 0 : fTrace.hashCode());
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
        if (!(obj instanceof CTFTraceReader)) {
            return false;
        }
        CTFTraceReader other = (CTFTraceReader) obj;
        if (!fStreamInputReaders.equals(other.fStreamInputReaders)) {
            return false;
        }
        if (fTrace == null) {
            if (other.fTrace != null) {
                return false;
            }
        } else if (!fTrace.equals(other.fTrace)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        /* Only for debugging, shouldn't be externalized */
        return "CTFTraceReader [trace=" + fTrace + ']'; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.tracecompass.ctf.core.trace.ICTFTraceReader#getTrace()
     */
    @Override
    public CTFTrace getTrace() {
        return fTrace;
    }
}
