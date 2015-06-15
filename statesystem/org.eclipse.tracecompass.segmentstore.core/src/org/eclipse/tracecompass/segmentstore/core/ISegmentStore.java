/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.segmentstore.core;

/**
 * Interface for the segment-storing backend. This allows multiple
 * implementations to be used, depending on the backend.
 *
 * @param <T>
 *            The type of {@link ISegment} element that will be stored in this
 *            database.
 *
 * @author Alexandre Montplaisir
 */
public interface ISegmentStore<T extends ISegment> extends Iterable<T> {

    /**
     * Add an element to the database.
     *
     * @param elem The element to add.
     */
    void addElement(T elem);

    /**
     * Get the number of element currently existing in the database.
     *
     * @return The number of elements.
     */
    long getNbElements();

    /**
     * To seek rapidly among all elements, the elements should be indexed by
     * their ascending order of start times.
     *
     * This method returns an individual element, given a position in this index.
     *
     * @param index
     *            Retrieve the element at this location
     * @return The element at this index
     */
    T getElementAtIndex(long index);

    /**
     * Retrieve all the intervals that cross the given timestamp.
     *
     * @param time
     *            The target timestamp
     * @return The intervals that cross this timestamp.
     */
    Iterable<T> getIntersectingElements(long time);

    /**
     * Dispose the data structure.
     */
    void dispose();
}
