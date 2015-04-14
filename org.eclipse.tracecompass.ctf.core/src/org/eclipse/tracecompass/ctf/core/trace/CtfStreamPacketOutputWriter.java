package org.eclipse.tracecompass.ctf.core.trace;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.eclipse.tracecompass.ctf.core.CTFReaderException;
import org.eclipse.tracecompass.internal.ctf.core.trace.StreamInputPacketIndexEntry;

/**
 * @since 1.0
 */
public class CtfStreamPacketOutputWriter {

    CTFStreamOutputWriter fStreamOutputWriter;
    CTFStreamInputPacketReader fStreamInputPacketReader;

    public CtfStreamPacketOutputWriter(CTFStreamOutputWriter packetWriter) {
        fStreamOutputWriter = packetWriter;
        fStreamInputPacketReader = packetWriter.getStreamInputReader();
    }


    public void writePacket(StreamInputPacketIndexEntry entry) throws CTFReaderException {
        fStreamInputPacketReader.setCurrentPacket(entry);
        ByteBuffer buffer = fStreamInputPacketReader.getByteBuffer();
        try {
            fStreamOutputWriter.getFc().write(buffer);
        } catch (IOException e) {
            new CTFReaderException("Can't write packet", e);
        }
    }



}
