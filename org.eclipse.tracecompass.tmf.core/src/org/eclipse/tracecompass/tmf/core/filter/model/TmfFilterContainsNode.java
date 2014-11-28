/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.filter.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Filter node for the 'contains' operation
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TmfFilterContainsNode extends TmfFilterTreeNode {

    /** The node name */
    public static final String NODE_NAME = "CONTAINS"; //$NON-NLS-1$
    /** This node supports <em>NOT</em> */
    public static final String NOT_ATTR = "not"; //$NON-NLS-1$
    /** This node supports fields */
    public static final String FIELD_ATTR = "field"; //$NON-NLS-1$
    /** This node supports field values */
    public static final String VALUE_ATTR = "value"; //$NON-NLS-1$
    /** This node supports comparisons where the case is ignored */
    public static final String IGNORECASE_ATTR = "ignorecase"; //$NON-NLS-1$

    private transient String fValueUpperCase;

    /**
     * Constructor
     *
     * @param parent
     *            the parent node
     */
    public TmfFilterContainsNode(ITmfFilterTreeNode parent) {
        super(parent);
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        if (value != null) {
            fValueUpperCase = value.toUpperCase();
        }
    }

    @Override
    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public boolean matches(ITmfEvent event) {
        Object value = getFieldValue(event, getField());
        if (value == null) {
            return false ^ isNot();
        }
        String valueString = value.toString();
        if (isIgnoreCase()) {
            return valueString.toUpperCase().contains(fValueUpperCase) ^ isNot();
        }
        return valueString.contains(getValue()) ^ isNot();
    }

    @Override
    public List<String> getValidChildren() {
        return new ArrayList<>(0);
    }

    @Override
    public String toString() {
        return getField() + (isNot() ? " not" : "") + " contains \"" + getValue() + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    @Override
    public ITmfFilterTreeNode clone() {
        TmfFilterContainsNode clone = (TmfFilterContainsNode) super.clone();
        clone.setField(getField());
        clone.setValue(getValue());
        return clone;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((getField() == null) ? 0 : getField().hashCode());
        result = prime * result + (isIgnoreCase() ? 1231 : 1237);
        result = prime * result + (isNot() ? 1231 : 1237);
        result = prime * result + ((getValue() == null) ? 0 : getValue().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TmfFilterContainsNode other = (TmfFilterContainsNode) obj;
        if (getField() == null) {
            if (other.getField() != null) {
                return false;
            }
        } else if (!getField().equals(other.getField())) {
            return false;
        }
        if (isIgnoreCase() != other.isIgnoreCase()) {
            return false;
        }
        if (isNot() != other.isNot()) {
            return false;
        }
        if (getValue() == null) {
            if (other.getValue() != null) {
                return false;
            }
        } else if (!getValue().equals(other.getValue())) {
            return false;
        }
        return true;
    }
}
