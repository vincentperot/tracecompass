/*******************************************************************************
 * Copyright (c) 2009, 2010, 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Updated as per TMF Trace Model 1.0
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace;

/**
 * <b><u>ITmfContext</u></b>
 * <p>
 * The basic trace context structure in TMF. The purpose of the context is to
 * associate a trace location to an event of a specific rank (order).
 * <p>
 * The context should be sufficient to allow the trace to position itself so
 * that performing a trace read operation will yield the corresponding event.
 */
public interface ITmfContext extends Cloneable {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The initial context event rank, before anything is read from the trace
     */
    public long INITIAL_RANK = -1L;

    /**
     * The unknown event rank
     */
    public long UNKNOWN_RANK = -2L;

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @return the rank of the event referred to by the context
     */
    public long getRank();

    /**
     * @return the location of the event referred to by the context
     */
    public ITmfLocation<? extends Comparable<?>> getLocation();

    /**
     * @return indicates if the context rank is valid (!= UNKNOWN_RANK)
     */
    public boolean hasValidRank();

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * @param location the new location
     */
    public void setLocation(ITmfLocation<? extends Comparable<?>> location);

    /**
     * @param rank the new rank
     */
    public void setRank(long rank);

    /**
     * Increment the context rank
     */
    public void increaseRank();

    /**
     * Cleanup hook
     */
    public void dispose();

    // ------------------------------------------------------------------------
    // Cloneable
    // ------------------------------------------------------------------------

    /**
     * @return a clone of the context
     */
    public ITmfContext clone();

}
