/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.ctf.core.trace;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.trace.event.IEventDeclaration;
import org.eclipse.tracecompass.ctf.core.types.ICompositeDeclaration;
import org.eclipse.tracecompass.ctf.core.types.IDeclaration;
import org.eclipse.tracecompass.internal.ctf.core.trace.event.EventDeclaration;

/**
 * Stream declaration interface. An event stream is an ordered sequence of
 * events, containing a subset of the trace event types
 */
public interface ICTFStream {

    /**
     * Gets the id of a stream
     *
     * @return id the id of a stream
     */
    Long getId();

    /**
     * Is the id of a stream set
     *
     * @return If the ID is set or not
     */
    boolean isIdSet();

    /**
     *
     * @return is the event header set (timestamp and stuff) (see Ctf Spec)
     */
    boolean isEventHeaderSet();

    /**
     *
     * @return is the event context set (pid and stuff) (see Ctf Spec)
     */
    boolean isEventContextSet();

    /**
     *
     * @return Is the packet context set (see Ctf Spec)
     */
    boolean isPacketContextSet();

    /**
     * Gets the event header declaration
     *
     * @return the event header declaration in declaration form
     * @since 3.1
     */
    IDeclaration getEventHeaderDeclaration();

    /**
     *
     * @return the event context declaration in ICompositeDeclaration form
     */
    ICompositeDeclaration getEventContextDecl();

    /**
     *
     * @return the parent trace
     */
    CTFTrace getTrace();

    /**
     * Get all the event declarations in this stream.
     *
     * @return The event declarations for this stream
     * @since 3.2
     */
    @NonNull
    Collection<IEventDeclaration> getEventDeclarations();

    /**
     * Get the event declaration for a given ID.
     *
     * @param eventId
     *            The ID, can be {@link EventDeclaration#UNSET_EVENT_ID}, or any
     *            positive value
     * @return The event declaration with the given ID for this stream, or
     *         'null' if there are no declaration with this ID
     * @throws IllegalArgumentException
     *             If the passed ID is invalid
     * @since 3.2
     */
    @Nullable
    IEventDeclaration getEventDeclaration(int eventId);

}