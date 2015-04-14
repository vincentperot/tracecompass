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

package org.eclipse.tracecompass.ctf.core.trace;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.ctf.core.CTFReaderException;
import org.eclipse.tracecompass.internal.ctf.core.trace.StreamInputPacketIndex;
import org.eclipse.tracecompass.internal.ctf.core.trace.StreamInputPacketIndexEntry;

/**
 * <b><u>StreamInput</u></b>
 * <p>
 * Represents a trace file that belongs to a certain stream.
 * @since 1.0
 */
@NonNullByDefault
public class CTFStreamOutputWriter implements AutoCloseable {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private CTFStreamInputReader fStreamInputReader;
    private CTFStreamInputPacketReader fStreamInputPacketReader;
    private CtfStreamPacketOutputWriter fStreamPacketOutputWriter;

    private File fFile;

    private FileChannel fFileChannel;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a StreamInput.
     *
     * @param stream
     *            The stream to which this StreamInput belongs to.
     * @param file
     *            Information about the trace file (for debugging purposes).
     * @throws CTFReaderException
     */
    public CTFStreamOutputWriter(CTFStreamInput streamInput, File file) throws CTFReaderException {
        fStreamInputReader = new CTFStreamInputReader(streamInput);
        String inFileName = streamInput.getFile().getName();
        Path outFilePath = FileSystems.getDefault().getPath(file.getAbsolutePath(), inFileName);

        try {
            fFile = Files.createFile(outFilePath).toFile();
            fFileChannel = checkNotNull(FileChannel.open(fFile.toPath(), StandardOpenOption.WRITE));
        } catch (IOException e) {
            throw new CTFReaderException("Output file channel can't be opened");
        }

        fStreamInputPacketReader = new CTFStreamInputPacketReader(fStreamInputReader);
        fStreamPacketOutputWriter = new CtfStreamPacketOutputWriter(this);
    }

    public FileChannel getFc() {
        return fFileChannel;
    }

    public CTFStreamInputPacketReader getStreamInputReader() {
        return fStreamInputPacketReader;
    }

    /**
     * @throws CTFReaderException
     * @since 1.0
     */
    public void write(long startTime, long endTime) throws CTFReaderException {
        CTFStreamInput streamInput = fStreamInputReader.getStreamInput();
        StreamInputPacketIndex index = streamInput.getIndex();
        for(int i = 0; i < index.size(); i++) {
            StreamInputPacketIndexEntry entry = index.getElement(i);
            if ((startTime <= entry.getTimestampBegin() && (endTime >= entry.getTimestampBegin()))) {
                fStreamPacketOutputWriter.writePacket(entry);
            }
        }
    }

    @Override
    public void close() throws IOException {
        fStreamInputReader.close();
        fFileChannel.close();
    }

}
