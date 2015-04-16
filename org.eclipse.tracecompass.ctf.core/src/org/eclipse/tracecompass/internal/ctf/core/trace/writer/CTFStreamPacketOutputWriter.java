package org.eclipse.tracecompass.internal.ctf.core.trace.writer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.StandardOpenOption;

import org.eclipse.tracecompass.ctf.core.trace.ICTFPacketInformation;
import org.eclipse.tracecompass.ctf.core.trace.writer.CTFWriterException;
import org.eclipse.tracecompass.internal.ctf.core.SafeMappedByteBuffer;

/**
 * @since 1.0
 */
public class CTFStreamPacketOutputWriter {

    ICTFStreamCropper fStreamOutputWriter;
    File fFile;

    public CTFStreamPacketOutputWriter(CTFStreamOutputWriter packetWriter) {
        fStreamOutputWriter = packetWriter;
        fFile = packetWriter.getFile();
    }

    public void writePacket(ICTFPacketInformation entry, FileChannel fc) throws CTFWriterException {
        try (FileChannel source = FileChannel.open(fFile.toPath(), StandardOpenOption.READ)) {
            ByteBuffer buffer = SafeMappedByteBuffer.map(source, MapMode.READ_ONLY, entry.getOffsetBytes(), entry.getContentSizeBits() / Byte.SIZE);
            fc.write(buffer);
        } catch (IOException e) {
            throw new CTFWriterException("Can't write packet", e);
        }
    }

}
