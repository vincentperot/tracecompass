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
 *   Francois Chouinard - Updated as per TMF Event Model 1.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event;

/**
 * A basic implementation of ITmfEventType.
 *
 * @version 1.0
 * @author Francois Chouinard
 *
 * @see ITmfEvent
 * @see ITmfEventField
 */
public class TmfEventType implements ITmfEventType {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final String fTypeId;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public TmfEventType() {
        this(DEFAULT_TYPE_ID);
    }

    /**
     * Full constructor
     *
     * @param typeId the type name
     * @since 2.0
     */
    public TmfEventType(final String typeId) {
        if (typeId == null) {
            throw new IllegalArgumentException();
        }
        fTypeId = typeId;
    }

    /**
     * Copy constructor
     *
     * @param type the other type
     */
    public TmfEventType(final ITmfEventType type) {
        if (type == null) {
            throw new IllegalArgumentException();
        }
        fTypeId  = type.getName();
    }

    // ------------------------------------------------------------------------
    // ITmfEventType
    // ------------------------------------------------------------------------

    @Override
    public String getName() {
        return fTypeId;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + fTypeId.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TmfEventType)) {
            return false;
        }
        final TmfEventType other = (TmfEventType) obj;
        if (!fTypeId.equals(other.fTypeId)) {
            return false;
        }
        return true;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "TmfEventType [fTypeId=" + fTypeId + "]";
    }

}
