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

package org.eclipse.tracecompass.internal.ctf.core.trace.reader;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.EventDefinition;
import org.eclipse.tracecompass.ctf.core.event.IEventDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDefinition;
import org.eclipse.tracecompass.ctf.core.trace.CTFResponse;
import org.eclipse.tracecompass.ctf.core.trace.ICTFPacketInformation;
import org.eclipse.tracecompass.ctf.core.trace.ICTFStreamInput;
import org.eclipse.tracecompass.ctf.core.trace.reader.ICTFPacketReader;
import org.eclipse.tracecompass.ctf.core.trace.reader.ICTFStreamInputReader;
import org.eclipse.tracecompass.ctf.core.trace.reader.ICTFTraceReader;
import org.eclipse.tracecompass.internal.ctf.core.Activator;
import org.eclipse.tracecompass.internal.ctf.core.trace.CTFStreamInput;

import com.google.common.collect.ImmutableList;

/**
 * A CTF trace event reader. Reads the events of a trace file.
 *
 * @author Matthew Khouzam
 * @author Simon Marchi
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
     * @throws CTFException
     *             If the file cannot be opened
     */
    public CTFStreamInputReader(ICTFStreamInput streamInput) throws CTFException {
        if (!(streamInput instanceof CTFStreamInput)) {
            throw new IllegalArgumentException("stream must be a ctf stream input"); //$NON-NLS-1$
        }
        fStreamInput = (CTFStreamInput) streamInput;
        fFile = fStreamInput.getFile();
        try {
            fFileChannel = FileChannel.open(fFile.toPath(), StandardOpenOption.READ);
        } catch (IOException e) {
            throw new CTFException(e);
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

    /**
     * Dispose the StreamInputReader, closes the file channel and its packet
     * reader
     *
     * @throws IOException
     *             If an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        fFileChannel.close();
        fPacketReader.close();
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.tracecompass.ctf.core.trace.ICTFStreamInputReader#getCurrentEvent
     * ()
     */
    @Override
    public EventDefinition getCurrentEvent() {
        return fCurrentEvent;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.tracecompass.ctf.core.trace.ICTFStreamInputReader#getByteOrder
     * ()
     */
    @Override
    public ByteOrder getByteOrder() {
        return fStreamInput.getStream().getTrace().getByteOrder();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.tracecompass.ctf.core.trace.ICTFStreamInputReader#getName()
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

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.tracecompass.ctf.core.trace.ICTFStreamInputReader#getCPU()
     */
    @Override
    public int getCPU() {
        return fPacketReader.getCPU();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.tracecompass.ctf.core.trace.ICTFStreamInputReader#getFilename
     * ()
     */
    @Override
    public String getFilename() {
        return fStreamInput.getFilename();
    }

    /*
     * for internal use only
     */
    ICTFStreamInput getStreamInput() {
        return fStreamInput;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.tracecompass.ctf.core.trace.ICTFStreamInputReader#
     * getEventDeclarations()
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

    /**
     * Get if the trace is to read live or not
     *
     * @return whether the trace is live or not
     */
    public boolean isLive() {
        return fLive;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.tracecompass.ctf.core.trace.ICTFStreamInputReader#
     * getStreamEventContextDecl()
     */
    @Override
    public StructDeclaration getStreamEventContextDecl() {
        return getStreamInput().getStream().getEventContextDecl();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Reads the next event in the current event variable.
     *
     * @return If an event has been successfully read.
     * @throws CTFException
     *             if an error occurs
     */
    public CTFResponse readNextEvent() throws CTFException {

        /*
         * Change packet if needed
         */
        if (!fPacketReader.hasMoreEvents()) {
            final ICTFPacketInformation prevPacket = fPacketReader
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
     * @throws CTFException
     *             if an error occurs
     */
    private void goToNextPacket() throws CTFException {
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
        return fStreamInput.getIndex().size();
    }

    /**
     * Changes the location of the trace file reader so that the current event
     * is the first event with a timestamp greater or equal the given timestamp.
     *
     * @param timestamp
     *            The timestamp to seek to.
     * @return The offset compared to the current position
     * @throws CTFException
     *             if an error occurs
     */
    public long seek(long timestamp) throws CTFException {
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
            } catch (CTFException e) {
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
     * @throws CTFException
     *             if an error occurs
     */
    private void gotoPacket(long timestamp) throws CTFException {
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
     * @throws CTFException
     *             if an error occurs
     */
    public void goToLastEvent() throws CTFException {
        /*
         * Search in the index for the packet to search in.
         */
        final int len = fStreamInput.getIndex().size();

        /*
         * Go to beginning of trace.
         */
        seek(0);
        /*
         * if the trace is empty.
         */
        if ((fStreamInput.getIndex().isEmpty()) || (!fPacketReader.hasMoreEvents())) {
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

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.tracecompass.ctf.core.trace.ICTFStreamInputReader#getParent()
     */
    @Override
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

    private ICTFPacketInformation getPacket() {
        return fStreamInput.getIndex().getElement(getPacketIndex());
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
     * @since 1.0
     */
    public ICTFPacketReader getPacketReader() {
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

    @Override
    public StructDefinition getCurrentPacketEventHeader() {
        return getPacketReader().getCurrentPacketEventHeader();
    }

}