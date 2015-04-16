package org.eclipse.tracecompass.ctf.core.trace.reader;

import java.io.IOException;
import java.nio.ByteOrder;

import org.eclipse.tracecompass.ctf.core.event.EventDefinition;
import org.eclipse.tracecompass.ctf.core.event.IEventDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDefinition;

/**
 * @since 1.0
 *
 */
public interface ICTFStreamInputReader extends AutoCloseable {

    /**
     * Gets the current event in this stream
     *
     * @return the current event in the stream, null if the stream is
     *         finished/empty/malformed
     */
    EventDefinition getCurrentEvent();

    /**
     * Gets the byte order for a trace
     *
     * @return the trace byte order
     */
    ByteOrder getByteOrder();

    /**
     * Gets the name of the stream (it's an id and a number)
     *
     * @return gets the stream name (it's a number)
     */
    int getName();

    /**
     * Gets the CPU of a stream. It's the same as the one in /proc or running
     * the asm CPUID instruction
     *
     * @return The CPU id (a number)
     */
    int getCPU();

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
     * Get the event context of the stream
     *
     * @return the event context declaration of the stream
     */
    StructDeclaration getStreamEventContextDecl();

    /**
     * @return the parent
     */
    ICTFTraceReader getParent();

    @Override
    void close() throws IOException;

    /**
     * get current packet event header
     *
     * @return the packet event header
     */
    StructDefinition getCurrentPacketEventHeader();

}