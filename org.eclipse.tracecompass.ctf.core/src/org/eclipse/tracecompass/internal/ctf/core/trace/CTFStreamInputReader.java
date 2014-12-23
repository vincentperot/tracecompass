/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.ctf.core.trace;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.trace.CTFReaderException;
import org.eclipse.tracecompass.ctf.core.trace.CTFResponse;
import org.eclipse.tracecompass.ctf.core.trace.ICTFStreamInputReader;
import org.eclipse.tracecompass.ctf.core.trace.ICTFTraceReader;
import org.eclipse.tracecompass.ctf.core.trace.event.EventDefinition;
import org.eclipse.tracecompass.ctf.core.trace.event.IEventDeclaration;
import org.eclipse.tracecompass.ctf.core.types.ICompositeDeclaration;
import org.eclipse.tracecompass.internal.ctf.core.Activator;
import org.eclipse.tracecompass.internal.ctf.core.trace.stream.StreamInputPacketIndexEntry;

import com.google.common.collect.ImmutableList;

/**
 * A CTF trace event reader. Reads the events of a trace file.
 *
 * @author Matthew Khouzam
 * @author Simon Marchi
 * @since 3.0
 */
public class CTFStreamInputReader implements ICTFStreamInputReader {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The StreamInput we are reading.
     */
    private final @NonNull File fFile;

    private final @NonNull CTFStreamInput fStreamInput;

    private final FileChannel fFileChannel;

    /**
     * The packet reader used to read packets from this trace file.
     */
    private final CTFStreamInputPacketReader fPacketReader;

    /**
     * Iterator on the packet index
     */
    private int fPacketIndex;

    /**
     * Reference to the current event of this trace file (iow, the last on that
     * was read, the next one to be returned)
     */
    private EventDefinition fCurrentEvent = null;

    private int fId;

    private ICTFTraceReader fParent;

    /**
     * Live trace reading
     */
    private boolean fLive = false;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructs a StreamInputReader that reads a StreamInput.
     *
     * @param streamInput
     *            The StreamInput to read.
     * @throws CTFReaderException
     *             If the file cannot be opened
     */
    public CTFStreamInputReader(CTFStreamInput streamInput) throws CTFReaderException {
        if (streamInput == null) {
            throw new IllegalArgumentException("stream cannot be null"); //$NON-NLS-1$
        }
        fStreamInput = streamInput;
        fFile = fStreamInput.getFile();
        try {
            fFileChannel = FileChannel.open(fFile.toPath(), StandardOpenOption.READ);
        } catch (IOException e) {
            throw new CTFReaderException(e);
        }
        fPacketReader = new CTFStreamInputPacketReader(this);
        /*
         * Get the iterator on the packet index.
         */
        fPacketIndex = 0;
        /*
         * Make first packet the current one.
         */
        goToNextPacket();
    }

    /* (non-Javadoc)
     * @see org.eclipse.tracecompass.ctf.core.trace.ICTFStreamInputReader#close()
     */
    @Override
    public void close() throws IOException {
        fFileChannel.close();
        fPacketReader.close();
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.tracecompass.ctf.core.trace.ICTFStreamInputReader#getCurrentEvent()
     */
    @Override
    public EventDefinition getCurrentEvent() {
        return fCurrentEvent;
    }

    /**
     * Gets the byte order for a trace
     *
     * @return the trace byte order
     */
    public ByteOrder getByteOrder() {
        return fStreamInput.getStream().getTrace().getByteOrder();
    }

    /* (non-Javadoc)
     * @see org.eclipse.tracecompass.ctf.core.trace.ICTFStreamInputReader#getName()
     */
    @Override
    public int getName() {
        return fId;
    }

    /**
     * Sets the name of the stream
     *
     * @param name
     *            the name of the stream, (it's a number)
     */
    public void setName(int name) {
        fId = name;
    }

    /**
     * Gets the CPU of a stream. It's the same as the one in /proc or running
     * the asm CPUID instruction
     *
     * @return The CPU id (a number)
     */
    public int getCPU() {
        return fPacketReader.getCPU();
    }

    /* (non-Javadoc)
     * @see org.eclipse.tracecompass.ctf.core.trace.ICTFStreamInputReader#getFilename()
     */
    @Override
    public String getFilename() {
        return fStreamInput.getFilename();
    }

    /*
     * for internal use only
     */
    CTFStreamInput getStreamInput() {
        return fStreamInput;
    }

    /* (non-Javadoc)
     * @see org.eclipse.tracecompass.ctf.core.trace.ICTFStreamInputReader#getEventDeclarations()
     */
    @Override
    public Iterable<IEventDeclaration> getEventDeclarations() {
        return ImmutableList.copyOf(fStreamInput.getStream().getEventDeclarations());
    }

    /**
     * Set the trace to live mode
     *
     * @param live
     *            whether the trace is read live or not
     */
    public void setLive(boolean live) {
        fLive = live;
    }

    /* (non-Javadoc)
     * @see org.eclipse.tracecompass.ctf.core.trace.ICTFStreamInputReader#isLive()
     */
    @Override
    public boolean isLive() {
        return fLive;
    }

    /* (non-Javadoc)
     * @see org.eclipse.tracecompass.ctf.core.trace.ICTFStreamInputReader#getStreamEventContextDecl()
     */
    @Override
    public ICompositeDeclaration getStreamEventContextDecl() {
        return getStreamInput().getStream().getEventContextDecl();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.eclipse.tracecompass.ctf.core.trace.ICTFStreamInputReader#readNextEvent()
     */
    @Override
    public CTFResponse readNextEvent() throws CTFReaderException {

        /*
         * Change packet if needed
         */
        if (!fPacketReader.hasMoreEvents()) {
            final StreamInputPacketIndexEntry prevPacket = fPacketReader
                    .getCurrentPacket();
            if (prevPacket != null || fLive) {
                goToNextPacket();
            }

        }

        /*
         * If an event is available, read it.
         */
        if (fPacketReader.hasMoreEvents()) {
            setCurrentEvent(fPacketReader.readNextEvent());
            return CTFResponse.OK;
        }
        this.setCurrentEvent(null);
        return fLive ? CTFResponse.WAIT : CTFResponse.FINISH;
    }

    /**
     * Change the current packet of the packet reader to the next one.
     *
     * @throws CTFReaderException
     *             if an error occurs
     */
    private void goToNextPacket() throws CTFReaderException {
        fPacketIndex++;
        // did we already index the packet?
        if (getPacketSize() >= (fPacketIndex + 1)) {
            fPacketReader.setCurrentPacket(getPacket());
        } else {
            // go to the next packet if there is one, index it at the same time
            if (fStreamInput.addPacketHeaderIndex()) {
                fPacketIndex = getPacketSize() - 1;
                fPacketReader.setCurrentPacket(getPacket());
            } else {
                // out of packets
                fPacketReader.setCurrentPacket(null);
            }
        }
    }

    /**
     * @return
     */
    private int getPacketSize() {
        return fStreamInput.getIndex().getEntries().size();
    }

    /* (non-Javadoc)
     * @see org.eclipse.tracecompass.ctf.core.trace.ICTFStreamInputReader#seek(long)
     */
    @Override
    public long seek(long timestamp) throws CTFReaderException {
        long offset = 0;

        gotoPacket(timestamp);

        /*
         * index up to the desired timestamp.
         */
        while ((fPacketReader.getCurrentPacket() != null)
                && (fPacketReader.getCurrentPacket().getTimestampEnd() < timestamp)) {
            try {
                fStreamInput.addPacketHeaderIndex();
                goToNextPacket();
            } catch (CTFReaderException e) {
                // do nothing here
                Activator.log(e.getMessage());
            }
        }
        if (fPacketReader.getCurrentPacket() == null) {
            gotoPacket(timestamp);
        }

        /*
         * Advance until either of these conditions are met:
         *
         * - reached the end of the trace file (the given timestamp is after the
         * last event)
         *
         * - found the first event with a timestamp greater or equal the given
         * timestamp.
         */
        readNextEvent();
        boolean done = (this.getCurrentEvent() == null);
        while (!done && (this.getCurrentEvent().getTimestamp() < timestamp)) {
            readNextEvent();
            done = (this.getCurrentEvent() == null);
            offset++;
        }
        return offset;
    }

    /**
     * @param timestamp
     *            the time to seek
     * @throws CTFReaderException
     *             if an error occurs
     */
    private void gotoPacket(long timestamp) throws CTFReaderException {
        fPacketIndex = fStreamInput.getIndex().search(timestamp)
                .previousIndex();
        /*
         * Switch to this packet.
         */
        goToNextPacket();
    }

    /**
     * Seeks the last event of a stream and returns it.
     *
     * @throws CTFReaderException
     *             if an error occurs
     */
    public void goToLastEvent() throws CTFReaderException {
        /*
         * Search in the index for the packet to search in.
         */
        final int len = fStreamInput.getIndex().getEntries().size();

        /*
         * Go to beginning of trace.
         */
        seek(0);
        /*
         * if the trace is empty.
         */
        if ((len == 0) || (!fPacketReader.hasMoreEvents())) {
            /*
             * This means the trace is empty. abort.
             */
            return;
        }
        /*
         * Go to the last packet that contains events.
         */
        for (int pos = len - 1; pos > 0; pos--) {
            fPacketIndex = pos;
            fPacketReader.setCurrentPacket(getPacket());
            if (fPacketReader.hasMoreEvents()) {
                break;
            }
        }

        /*
         * Go until the end of that packet
         */
        EventDefinition prevEvent = null;
        while (fCurrentEvent != null) {
            prevEvent = fCurrentEvent;
            this.readNextEvent();
        }
        /*
         * Go back to the previous event
         */
        this.setCurrentEvent(prevEvent);
    }

    /**
     * @return the parent
     */
    public ICTFTraceReader getParent() {
        return fParent;
    }

    /**
     * @param parent
     *            the parent to set
     */
    public void setParent(ICTFTraceReader parent) {
        fParent = parent;
    }

    /**
     * Sets the current event in a stream input reader
     *
     * @param currentEvent
     *            the event to set
     */
    public void setCurrentEvent(EventDefinition currentEvent) {
        fCurrentEvent = currentEvent;
    }

    /**
     * @return the packetIndexIt
     */
    private int getPacketIndex() {
        return fPacketIndex;
    }

    private StreamInputPacketIndexEntry getPacket() {
        return fStreamInput.getIndex().getEntries().get(getPacketIndex());
    }

    /**
     * Get the file channel wrapped by this reader
     *
     * @return the file channel
     */
    FileChannel getFc() {
        return fFileChannel;
    }

    /**
     * @return the packetReader
     */
    public CTFStreamInputPacketReader getPacketReader() {
        return fPacketReader;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + fId;
        result = (prime * result)
                + fFile.hashCode();
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
        if (!(obj instanceof CTFStreamInputReader)) {
            return false;
        }
        CTFStreamInputReader other = (CTFStreamInputReader) obj;
        if (fId != other.fId) {
            return false;
        }
        return fFile.equals(other.fFile);
    }

    @Override
    public String toString() {
        // this helps debugging
        return fId + ' ' + fCurrentEvent.toString();
    }

}
