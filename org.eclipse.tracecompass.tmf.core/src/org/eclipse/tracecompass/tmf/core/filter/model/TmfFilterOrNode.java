/*******************************************************************************
 * Copyright (c) 2010, 2012 Ericsson
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

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Filter node for the 'or' operation
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TmfFilterOrNode extends TmfFilterTreeNode {

    /** The node name */
    public static final String NODE_NAME = "OR"; //$NON-NLS-1$
    /** The this node supports <em>NOT</em> */
    public static final String NOT_ATTR = "not"; //$NON-NLS-1$

    /**
     * Constructor
     *
     * @param parent
     *            the parent node
     */
    public TmfFilterOrNode(ITmfFilterTreeNode parent) {
        super(parent);
    }

    @Override
    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public boolean matches(ITmfEvent event) {
        for (ITmfFilterTreeNode node : getChildren()) {
            if (node.matches(event)) {
                return true ^ isNot();
            }
        }
        return false & isNot();
    }

    @Override
    public String toString() {
        return stringifyChildren(" or ").toString(); //$NON-NLS-1$
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (isNot() ? 1231 : 1237);
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
        TmfFilterOrNode other = (TmfFilterOrNode) obj;
        if (isNot() != other.isNot()) {
            return false;
        }
        return true;
    }
}
