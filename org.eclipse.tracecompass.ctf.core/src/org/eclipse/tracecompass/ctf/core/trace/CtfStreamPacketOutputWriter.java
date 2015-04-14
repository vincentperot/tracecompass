package org.eclipse.tracecompass.ctf.core.trace;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.StandardOpenOption;

import org.eclipse.tracecompass.ctf.core.CTFReaderException;
import org.eclipse.tracecompass.internal.ctf.core.SafeMappedByteBuffer;
import org.eclipse.tracecompass.internal.ctf.core.trace.StreamInputPacketIndexEntry;

/**
 * @since 1.0
 */
public class CtfStreamPacketOutputWriter {

    CTFStreamOutputWriter fStreamOutputWriter;
    File fFile;

    public CtfStreamPacketOutputWriter(CTFStreamOutputWriter packetWriter) {
        fStreamOutputWriter = packetWriter;
        fFile = packetWriter.getFile();
    }

    public void writePacket(StreamInputPacketIndexEntry entry, FileChannel fc) throws CTFReaderException {
        try (FileChannel source = FileChannel.open(fFile.toPath(), StandardOpenOption.READ)) {
            ByteBuffer buffer = SafeMappedByteBuffer.map(source, MapMode.READ_ONLY, entry.getOffsetBytes(), entry.getContentSizeBits() / Byte.SIZE);
            fc.write(buffer);
        } catch (IOException e) {
            new CTFReaderException("Can't write packet", e);
        }
    }

}
