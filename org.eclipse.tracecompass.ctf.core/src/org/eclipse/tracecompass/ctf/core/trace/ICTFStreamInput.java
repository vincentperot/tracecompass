package org.eclipse.tracecompass.ctf.core.trace;

import java.io.File;

import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;

/**
 * @since 1.0
 */
public interface ICTFStreamInput extends IDefinitionScope {

    /**
     * Gets the stream the streamInput wrapper is wrapping
     *
     * @return the stream the streamInput wrapper is wrapping
     */
    CTFStream getStream();

    /**
     * Gets the filename of the streamInput file.
     *
     * @return the filename of the streaminput file.
     */
    String getFilename();

    /**
     * Gets the start time of a stream, this is not necessarily the timestamp of
     * the first event
     *
     * @return the start time
     */
    long getTimestampBegin();

    /**
     * Gets the last read timestamp of a stream. (this is not necessarily the
     * last time in the stream.)
     *
     * @return the last read timestamp
     */
    long getTimestampEnd();

    /**
     * Get the file
     *
     * @return the file
     * @since 1.0
     */
    File getFile();

}