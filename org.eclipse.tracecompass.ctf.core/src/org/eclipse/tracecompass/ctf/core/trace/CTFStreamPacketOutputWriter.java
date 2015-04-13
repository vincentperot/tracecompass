/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.ctf.core.trace;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.StandardOpenOption;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.internal.ctf.core.SafeMappedByteBuffer;

/**
 * CTF trace packet writer.
 *
 * @author Bernd Hufmann
 * @since 1.0
 */
public class CTFStreamPacketOutputWriter {

    @Nullable private File fInFile;

    /**
     * Constructor
     *
     * @param packetWriter
     *            a stream output writer
     */
    public CTFStreamPacketOutputWriter(@NonNull CTFStreamOutputWriter packetWriter) {
        fInFile = packetWriter.getInFile();
    }

    /**
     * Writes a stream packet to the output file channel based on the packet
     * descriptor information.
     *
     * @param packetDescriptor
     *            a packet descriptor
     * @param fc
     *            a file channel
     * @throws CTFException
     *            if a reading or writing error occurs
     */
    public void writePacket(ICTFPacketDescriptor packetDescriptor, FileChannel fc) throws CTFException {
        File inFile = fInFile;
        if (inFile == null) {
            throw new CTFIOException("Stream input file is null. Can't copy packets"); //$NON-NLS-1$
        }
        try (FileChannel source = FileChannel.open(inFile.toPath(), StandardOpenOption.READ)) {
            ByteBuffer buffer = SafeMappedByteBuffer.map(source, MapMode.READ_ONLY, packetDescriptor.getOffsetBytes(), packetDescriptor.getPacketSizeBits() / Byte.SIZE);
            fc.write(buffer);
        } catch (IOException e) {
            throw new CTFIOException("Can't CTF write packet to file: " + e.toString(), e); //$NON-NLS-1$
        }
    }

}
