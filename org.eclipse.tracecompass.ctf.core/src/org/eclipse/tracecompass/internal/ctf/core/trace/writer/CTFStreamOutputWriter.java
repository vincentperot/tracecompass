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

package org.eclipse.tracecompass.internal.ctf.core.trace.writer;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.ctf.core.trace.ICTFPacketInformation;
import org.eclipse.tracecompass.ctf.core.trace.writer.CTFWriterException;
import org.eclipse.tracecompass.internal.ctf.core.trace.CTFStreamInput;
import org.eclipse.tracecompass.internal.ctf.core.trace.StreamInputPacketIndex;

/**
 * <b><u>StreamInput</u></b>
 * <p>
 * Represents a trace file that belongs to a certain stream.
 *
 * @since 1.0
 */
@NonNullByDefault
public class CTFStreamOutputWriter implements ICTFStreamCropper {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private final CTFStreamInput fStreamInput;
    private final CTFStreamPacketOutputWriter fStreamPacketOutputWriter;

    private final File fFile;


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
     * @throws CTFWriterException
     */
    public CTFStreamOutputWriter(CTFStreamInput ctfStreamInput, File file) throws CTFWriterException {
        fStreamInput = ctfStreamInput;
        String inFileName = ctfStreamInput.getFile().getName();
        Path outFilePath = FileSystems.getDefault().getPath(file.getAbsolutePath(), inFileName);
        try {
            fFile = Files.createFile(outFilePath).toFile();
        } catch (IOException e) {
            throw new CTFWriterException("Output file can't be created", e); //$NON-NLS-1$
        }

        fStreamPacketOutputWriter = new CTFStreamPacketOutputWriter(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.tracecompass.internal.ctf.core.trace.writer.ICTFStreamCropper#copyPackets(long, long)
     */
    @Override
    public void copyPackets(long startTime, long endTime) throws CTFWriterException {
        try (FileChannel fc = checkNotNull(FileChannel.open(fFile.toPath(), StandardOpenOption.WRITE));){
            StreamInputPacketIndex index = fStreamInput.getIndex();
            for (int i = 0; i < index.size(); i++) {
                 ICTFPacketInformation entry = index.getElement(i);
                if ((startTime <= entry.getTimestampBegin() && (endTime >= entry.getTimestampBegin()))) {
                    fStreamPacketOutputWriter.writePacket(entry, fc);
                }
            }
        } catch (IOException e) {
            throw new CTFWriterException("Output file channel can't be opened", e); //$NON-NLS-1$
        }
    }

    public File getFile() {
        return fFile;
    }
}
