/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.trace.text;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.eclipse.core.resources.IResource;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.internal.tmf.core.parsers.custom.CustomEventAspects;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfEventParser;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.indexer.ITmfPersistentlyIndexable;
import org.eclipse.tracecompass.tmf.core.trace.indexer.ITmfTraceIndexer;
import org.eclipse.tracecompass.tmf.core.trace.indexer.TmfBTreeTraceIndexer;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.TmfCheckpoint;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;
import org.eclipse.tracecompass.tmf.core.trace.location.TmfLongLocation;

/**
 * Extension of TmfTrace for handling of line-based text traces parsed, be they
 * XML, text or other. Each line that matches the first line pattern indicates
 * the start of a new event. The subsequent lines can contain additional
 * information that is added to the current event.
 *
 * @author Matthew Khouzam
 * @since 3.1
 */
public abstract class AbstractCustomTrace extends TmfTrace implements ITmfEventParser, ITmfPersistentlyIndexable {

    /**
     * NULL LOCATION
     */
    static public final TmfLongLocation NULL_LOCATION = new TmfLongLocation(-1L);

    /**
     * Number of events per checkpoint
     */
    protected int fCheckpointSize;

    /**
     * Get the file
     *
     * @return the fFile
     */
    protected abstract BufferedRandomAccessFile getFile();

    /**
     * Set the file
     *
     * @param file
     *            the file
     */
    protected abstract void setFile(BufferedRandomAccessFile file);

    /**
     * Default constructor
     */
    public AbstractCustomTrace() {
        super();
        fCheckpointSize = -1;
    }

    /**
     * Custom Trace Type
     *
     * @param resource
     *            The resource associated to the trace
     * @param type
     *            The type of events that will be read from this trace
     * @param path
     *            The path to the trace on the filesystem
     * @param cacheSize
     *            The trace cache size. Pass '-1' to use the default specified
     *            in {@link ITmfTrace#DEFAULT_TRACE_CACHE_SIZE}
     * @param interval
     *            The trace streaming interval. You can use '0' for post-mortem
     *            traces.
     * @param checkpointSize
     *            The amount of events per checkpoint
     * @param parser
     *            The trace event parser. Use 'null' if (and only if) the trace
     *            object itself is also the ITmfEventParser to be used.
     * @throws TmfTraceException
     *             If something failed during the opening
     */
    public AbstractCustomTrace(IResource resource, Class<? extends ITmfEvent> type, String path, int cacheSize, long interval, int checkpointSize, ITmfEventParser parser) throws TmfTraceException {
        super(resource, type, path, cacheSize, interval, parser);
        fCheckpointSize = checkpointSize;
    }

    /**
     * Copy constructors
     *
     * @param trace
     *            the original trace
     * @param checkpointSize
     *            The amount of events per checkpoint
     * @throws TmfTraceException
     *             Should not happen usually
     */
    public AbstractCustomTrace(TmfTrace trace, int checkpointSize) throws TmfTraceException {
        super(trace);
        fCheckpointSize = checkpointSize;
    }

    @Override
    public void initTrace(final IResource resource, final String path, final Class<? extends ITmfEvent> eventType) throws TmfTraceException {
        super.initTrace(resource, path, eventType);
        try {
            setFile(new BufferedRandomAccessFile(getPath(), "r")); //$NON-NLS-1$
        } catch (IOException e) {
            throw new TmfTraceException(e.getMessage(), e);
        }
    }

    @Override
    public synchronized void dispose() {
        super.dispose();
        if (getFile() != null) {
            try {
                getFile().close();
            } catch (IOException e) {
            } finally {
                setFile(null);
            }
        }
    }


    /**
     * Get the trace definition.
     *
     * @return The trace definition
     */
    public abstract CustomTraceDefinition getDefinition();

    @Override
    public Iterable<ITmfEventAspect> getEventAspects() {
        return CustomEventAspects.generateAspects(getDefinition());
    }

    @Override
    public ITmfTraceIndexer getIndexer() {
        return super.getIndexer();
    }

    @Override
    public synchronized TmfContext seekEvent(final ITmfLocation location) {
        TmfContext context = getNullContext();
        if (NULL_LOCATION.equals(location) || getFile() == null) {
            return context;
        }
        try {
            if (location == null) {
                getFile().seek(0);
            } else if (location.getLocationInfo() instanceof Long) {
                getFile().seek((Long) location.getLocationInfo());
            }
            long rawPos = getFile().getFilePointer();
            String line = getFile().getNextLine();
            while (line != null) {
                context = match(context, rawPos, line);
                if (context != null) {
                    return context;
                }
                rawPos = getFile().getFilePointer();
                line = getFile().getNextLine();
            }
            return context;
        } catch (final FileNotFoundException e) {
            Activator.logError("Error seeking event. File not found: " + getPath(), e); //$NON-NLS-1$
            return context;
        } catch (final IOException e) {
            Activator.logError("Error seeking event. File: " + getPath(), e); //$NON-NLS-1$
            return context;
        }

    }

    /**
     * Gets a null context
     *
     * @return a null context
     */
    protected abstract TmfContext getNullContext();

    /**
     * Match a context to a position in a file
     *
     * @param context
     *            The context to validate
     * @param rawPos
     *            the offset in a file
     * @param line
     *            the line number
     * @return the context that is updated, or null if invalid
     * @throws IOException
     *             end of file or such
     */
    protected abstract TmfContext match(final TmfContext context, long rawPos, String line) throws IOException;

    @Override
    public synchronized TmfContext seekEvent(final double ratio) {
        if (getFile() == null) {
            return getNullContext();
        }
        try {
            long pos = Math.round(ratio * getFile().length());
            while (pos > 0) {
                getFile().seek(pos - 1);
                if (getFile().read() == '\n') {
                    break;
                }
                pos--;
            }
            final ITmfLocation location = new TmfLongLocation(pos);
            final TmfContext context = seekEvent(location);
            context.setRank(ITmfContext.UNKNOWN_RANK);
            return context;
        } catch (final IOException e) {
            Activator.logError("Error seeking event. File: " + getPath(), e); //$NON-NLS-1$
            return getNullContext();
        }
    }

    @Override
    public synchronized double getLocationRatio(final ITmfLocation location) {
        if (getFile() == null) {
            return 0;
        }
        try {
            if (location.getLocationInfo() instanceof Long) {
                return ((Long) location.getLocationInfo()).doubleValue() / getFile().length();
            }
        } catch (final IOException e) {
            Activator.logError("Error seeking event. File: " + getPath(), e); //$NON-NLS-1$
        }
        return 0;
    }

    @Override
    public synchronized int getCheckpointSize() {
        if (fCheckpointSize == -1) {
            TmfCheckpoint c = new TmfCheckpoint(TmfTimestamp.ZERO, new TmfLongLocation(0L), 0);
            ByteBuffer b = ByteBuffer.allocate(ITmfCheckpoint.MAX_SERIALIZE_SIZE);
            b.clear();
            c.serialize(b);
            fCheckpointSize = b.position();
        }

        return fCheckpointSize;
    }

    @Override
    public ITmfLocation getCurrentLocation() {
        return null;
    }

    @Override
    public ITmfLocation restoreLocation(ByteBuffer bufferIn) {
        return new TmfLongLocation(bufferIn);
    }

    @Override
    protected ITmfTraceIndexer createIndexer(int interval) {
        return new TmfBTreeTraceIndexer(this, interval);
    }

}