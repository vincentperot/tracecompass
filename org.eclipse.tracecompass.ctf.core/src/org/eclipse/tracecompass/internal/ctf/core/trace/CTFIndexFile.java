/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.ctf.core.trace;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;

import org.eclipse.tracecompass.ctf.core.trace.CTFReaderException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

/**
 * Ctf index file reader
 *
 * @author Matthew Khouzam
 */
public class CTFIndexFile {

    private static final int CTF_INDEX_MAGIC = 0xC1F1DCC1;
    private static final int CTF_INDEX_MAJOR = 1;
    private static final int CTF_INDEX_MINOR = 0;
    private static final int PACKET_SIZE = 7 * Integer.SIZE / 8;

    private final ImmutableList<StreamInputPacketIndexEntry> fSipieList;

    /**
     * Ctf index file reader
     *
     * @param indexFile
     *            The {@Link File} input
     * @throws CTFReaderException
     *             an error such as an {@link IOException}
     */
    public CTFIndexFile(File indexFile) throws CTFReaderException {
        try (DataInputStream dataIn = new DataInputStream(new FileInputStream(indexFile))) {
            CtfPacketIndexFileHeader header;

            header = new CtfPacketIndexFileHeader(dataIn);

            // mmm pies
            ImmutableList.Builder<StreamInputPacketIndexEntry> pies = new Builder<>();

            while (dataIn.available() >= PACKET_SIZE) {
                StreamInputPacketIndexEntry element = new StreamInputPacketIndexEntry(dataIn);
                if( header.oldMagic){
                    element.setTarget(indexFile.getName().replaceAll("\\.idx", "")); //$NON-NLS-1$ //$NON-NLS-2$
                }
                pies.add(element); // mmm
            }
            fSipieList = pies.build();
        } catch (IOException e) {
            throw new CTFReaderException(e);
        }
    }

    /**
     * Gets the packet entries of this file
     *
     * @return the packet entries of this file
     */
    public Collection<StreamInputPacketIndexEntry> getStreamInputPacketIndexEntries() {
        return fSipieList;
    }

    /*
     * Header at the beginning of each index file. All integer fields are stored
     * in big endian.
     */
    class CtfPacketIndexFileHeader {
        private final int magic;
        private final int indexMajor;
        private final int indexMinor;
        /* CtfPacketIndexEntry, in bytes */
        private final int packetIndexLen;
        private final boolean oldMagic;

        public CtfPacketIndexFileHeader(DataInputStream dataIn) throws IOException, CTFReaderException {

            magic = dataIn.readInt();
            if (magic != CTF_INDEX_MAGIC) {
                if (magic == 0x43544649) {
                    oldMagic = (dataIn.readShort() == 0x4458);
                } else {
                    oldMagic = false;
                }
                if (!oldMagic) {
                    throw new CTFReaderException("Magic mismatch in index"); //$NON-NLS-1$
                }
            } else {
                oldMagic = false;
            }
            indexMajor = dataIn.readInt();
            if (indexMajor > CTF_INDEX_MAJOR) {
                throw new CTFReaderException("Major version mismatch in index"); //$NON-NLS-1$
            }
            indexMinor = dataIn.readInt();
            if (indexMajor == CTF_INDEX_MAJOR && indexMinor > CTF_INDEX_MINOR) {
                throw new CTFReaderException("Minor version mismatch in index"); //$NON-NLS-1$
            }
            if (!oldMagic) {
                packetIndexLen = dataIn.readInt();
            } else {
                packetIndexLen = PACKET_SIZE;
            }
            if (packetIndexLen != PACKET_SIZE) {
                throw new CTFReaderException("Packet size wrong in index"); //$NON-NLS-1$
            }
        }
    }
}