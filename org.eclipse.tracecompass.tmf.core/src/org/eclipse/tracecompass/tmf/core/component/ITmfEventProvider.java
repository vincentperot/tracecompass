/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.component;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;

/**
 * This is the interface of the data providers in TMF. Data providers have the
 * capability of handling data requests.
 *
 * @author Francois Chouinard
 *
 * @see TmfEventProvider
 * @since 3.0
 */
public interface ITmfEventProvider extends ITmfComponent {

    /**
     * Queue the request for processing.
     *
     * @param request The request to process
     */
    void sendRequest(ITmfEventRequest request);

    /**
     * Increments/decrements the pending requests counters and fires the request
     * if necessary (counter == 0). Used for coalescing requests across multiple
     * TmfDataProvider's.
     *
     * @param isIncrement
     *            Should we increment (true) or decrement (false) the pending
     *            counter
     */
    void notifyPendingRequest(boolean isIncrement);

    /**
     * Return the next event based on the context supplied. The context
     * will be updated for the subsequent read.
     *
     * @param context the trace read context (updated)
     * @return the event referred to by context
     */
    ITmfEvent getNext(ITmfContext context);

    /**
     * Gets the parent component.
     *
     * @return the parent component.
     */
    @Nullable
    ITmfEventProvider getParent();

    /**
     * Sets the parent component.
     *
     * @param parent
     *            the parent to set.
     */
    void setParent(@Nullable ITmfEventProvider parent);

    /**
     * Adds a child component.
     *
     * @param child
     *            child to add.
     */
    void addChild(@NonNull ITmfEventProvider child);

    /**
     * Gets the children components.
     *
     * @return the children components
     */
    @NonNull
    List<ITmfEventProvider> getChildren();

    /**
     * Returns the child component with given name.
     *
     * @param name
     *            name of child to find.
     * @return child component or null.
     */
    @Nullable
    ITmfEventProvider getChild(String name);

    /**
     * Returns the child component for a given index
     *
     * @param index index of child to get
     * @return child component
     */
    @NonNull
    ITmfEventProvider getChild(int index);

    /**
     * Gets children for given class type.
     *
     * @param clazz
     *            a class type to get
     * @return list of trace control components matching given class type.
     */
    @NonNull
    <T extends ITmfEventProvider> List<T> getChildren(Class<T> clazz);

    /**
     * Gets the number of children
     *
     * @return number of children
     */
    int getNbChildren();

    /**
     * Returns true if an event was provided by this event provider or one of
     * its children event providers else false.
     *
     * @param event
     *            the event to check
     * @return <code>true</code> if event was provided by this provider or one
     *         of its children else <code>false</code>
     */
    boolean isEventProvidedBy(ITmfEvent event);
}
