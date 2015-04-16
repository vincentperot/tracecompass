package org.eclipse.tracecompass.ctf.core.trace.reader;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.CTFReaderException;
import org.eclipse.tracecompass.ctf.core.event.EventDefinition;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.types.ICompositeDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.StructDefinition;

/**
 * @since 1.0
 */
public interface ICTFPacketReader extends AutoCloseable{

    /**
     * Get the event context defintiion
     *
     * @param input
     *            the bitbuffer to read from
     * @return an context definition, can be null
     * @throws CTFReaderException
     *             out of bounds exception or such
     */
    StructDefinition getEventContextDefinition(@NonNull BitBuffer input) throws CTFReaderException;

    /**
     * Get the packet context defintiion
     *
     * @param input
     *            the bitbuffer to read from
     * @return an context definition, can be null
     * @throws CTFReaderException
     *             out of bounds exception or such
     */
    StructDefinition getStreamPacketContextDefinition(@NonNull BitBuffer input) throws CTFReaderException;

    /**
     * Get the event header defintiion
     *
     * @param input
     *            the bitbuffer to read from
     * @return an header definition, can be null
     * @throws CTFReaderException
     *             out of bounds exception or such
     */
    StructDefinition getTracePacketHeaderDefinition(@NonNull BitBuffer input) throws CTFReaderException;

    /**
     * Gets the CPU (core) number
     *
     * @return the CPU (core) number
     */
    int getCPU();

    /**
     * Returns whether it is possible to read any more events from this packet.
     *
     * @return True if it is possible to read any more events from this packet.
     */
    boolean hasMoreEvents();

    /**
     * Reads the next event of the packet into the right event definition.
     *
     * @return The event definition containing the event data that was just
     *         read.
     * @throws CTFReaderException
     *             If there was a problem reading the trace
     */
    EventDefinition readNextEvent() throws CTFReaderException;

    /**
     * Get stream event header
     *
     * @return the stream event header
     */
    ICompositeDefinition getStreamEventHeaderDefinition();

    /**
     * Get the current packet event header
     *
     * @return the current packet event header
     */
    StructDefinition getCurrentPacketEventHeader();

    @Override
    void close();

}