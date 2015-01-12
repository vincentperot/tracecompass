/*******************************************************************************
 * Copyright (c) 2011-2014 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.trace.CTFReaderException;
import org.eclipse.tracecompass.ctf.core.trace.CTFStream;
import org.eclipse.tracecompass.ctf.core.trace.CTFStreamInputReader;

/**
 * Representation of one type of event. A bit like "int" or "long" but for trace
 * events.
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public interface IEventDeclaration {

    /**
     * Creates an instance of EventDefinition corresponding to this declaration.
     *
     * @param streamInputReader
     *            The StreamInputReader for which this definition is created.
     * @param input
     *            the bitbuffer input source
     * @param timestamp
     *            The timestamp when the event was taken
     * @return A new EventDefinition.
     * @throws CTFReaderException
     *             As a bitbuffer is used to read, it could have wrapped
     *             IOExceptions.
     * @since 3.0
     */
    EventDefinition createDefinition(CTFStreamInputReader streamInputReader, @NonNull BitBuffer input, long timestamp) throws CTFReaderException;

    /**
     * Gets the name of an event declaration
     *
     * @return the name
     */
    String getName();

    /**
     * Gets the fields of an event declaration
     *
     * @return fields the fields in {@link StructDeclaration} format
     */
    StructDeclaration getFields();

    /**
     * Gets the context of an event declaration
     *
     * @return context the fields in {@link StructDeclaration} format
     */
    StructDeclaration getContext();

    /**
     * Gets the id of an event declaration
     *
     * @return The EventDeclaration ID
     */
    Long getId();

    /**
     * Gets the {@link CTFStream} of an event declaration
     *
     * @return the stream
     * @since 3.0
     */
    CTFStream getStream();

    /**
     * What is the log level of this event?
     *
     * @return the log level.
     * @since 2.0
     */
    long getLogLevel();

    /**
     * Get the {@link Set} of names of the custom CTF attributes.
     *
     * @return The set of custom attributes
     * @since 2.0
     */
    Set<String> getCustomAttributes();

    /**
     * Get the value of a given CTF attribute.
     *
     * @param key
     *            The CTF attribute name
     * @return the CTF attribute
     * @since 2.0
     */
    String getCustomAttribute(String key);

}
