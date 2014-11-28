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
 * Filter node for the event match operation
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TmfFilterNode extends TmfFilterTreeNode {

    /** The node name */
    public static final String NODE_NAME = "FILTER"; //$NON-NLS-1$
    /** This node supports names */
    public static final String NAME_ATTR = "name"; //$NON-NLS-1$

    /**
     * Constructor
     *
     * @param filterName
     *            the filter name
     */
    public TmfFilterNode(String filterName) {
        super(null);
        setFilterName(filterName);
    }

    /**
     * Constructor with a name
     *
     * @param parent
     *            the parent node
     * @param filterName
     *            the filter name
     */
    public TmfFilterNode(ITmfFilterTreeNode parent, String filterName) {
        super(parent);
        setFilterName(filterName);
    }

    @Override
    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public boolean matches(ITmfEvent event) {
        // There should be at most one child
        for (ITmfFilterTreeNode node : getChildren()) {
            if (node.matches(event)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> getValidChildren() {
        if (getChildrenCount() == 0) {
            return super.getValidChildren();
        }
        return new ArrayList<>(0); // only one child allowed
    }

    @Override
    public String toString() {
        return stringifyChildren(" and ").toString(); //$NON-NLS-1$
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((getFilterName() == null) ? 0 : getFilterName().hashCode());
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
        TmfFilterNode other = (TmfFilterNode) obj;
        if (getFilterName() == null) {
            if (other.getFilterName() != null) {
                return false;
            }
        } else if (!getFilterName().equals(other.getFilterName())) {
            return false;
        }
        return true;
    }
}
