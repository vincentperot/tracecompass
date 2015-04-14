/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marc-Andre Laperle - Initial API and implementation. Inspired from TarEntry.
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace;

/**
 * GZip entry
 */
public class GzipEntry {
    private static final String ROOT_DIR = "/"; //$NON-NLS-1$
    private final String fName;
    private final long fMode;
    private final long fTime;
    private final int fType;

    /**
     * Entry type for normal files. This is the only valid type for Gzip
     * entries.
     */
    public static final int FILE = '0';

    /**
     * Entry type for directories. This doesn't really exist in a Gzip but it's
     * useful to represent the root of the archive.
     */
    public static final int DIRECTORY = '5';

    /**
     * Create a new Root GzipEntry
     */
    public GzipEntry() {
        fName = ROOT_DIR;
        fMode = 0644;
        fType = DIRECTORY;
        fTime = System.currentTimeMillis() / 1000;
    }

    /**
     * Create a new GzipEntry for a file of the given name at the given position
     * in the file.
     *
     * @param name
     *            filename
     */
    public GzipEntry(String name) {
        fName = name;
        fMode = 0644;
        fType = FILE;
        fTime = System.currentTimeMillis() / 1000;
    }

    /**
     * Returns the type of this file, can only be FILE for a real Gzip entry.
     * DIRECTORY can be specified to represent a "dummy root" in the archive.
     *
     * @return file type
     */
    public int getFileType() {
        return fType;
    }

    /**
     * Returns the mode of the file in UNIX permissions format.
     *
     * @return file mode
     */
    public long getMode() {
        return fMode;
    }

    /**
     * Returns the name of the file.
     *
     * @return filename
     */
    public String getName() {
        return fName;
    }

    /**
     * Returns the modification time of the file in seconds since January 1st
     * 1970.
     *
     * @return time
     */
    public long getTime() {
        return fTime;
    }
}
