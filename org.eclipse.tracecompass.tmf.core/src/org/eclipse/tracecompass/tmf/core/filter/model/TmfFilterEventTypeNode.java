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
 * Filter node for an event
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TmfFilterEventTypeNode extends TmfFilterTreeNode {

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getEventType() == null) ? 0 : getEventType().hashCode());
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
        TmfFilterEventTypeNode other = (TmfFilterEventTypeNode) obj;
        if (getName() == null) {
            if (other.getName() != null) {
                return false;
            }
        } else if (!getName().equals(other.getName())) {
            return false;
        }
        if (getEventType() == null) {
            if (other.getEventType() != null) {
                return false;
            }
        } else if (!getEventType().equals(other.getEventType())) {
            return false;
        }
        return true;
    }

    public static final String NODE_NAME = "EVENTTYPE"; //$NON-NLS-1$
    public static final String TYPE_ATTR = "type"; //$NON-NLS-1$
    public static final String NAME_ATTR = "name"; //$NON-NLS-1$

    /**
     * @param parent the parent node
     */
    public TmfFilterEventTypeNode(ITmfFilterTreeNode parent) {
        super(parent);
    }

    @Override
    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public boolean matches(ITmfEvent event) {
        boolean match = false;
        if (getEventType().contains(":")) { //$NON-NLS-1$
            // special case for custom parsers
            if (getEventType().startsWith(event.getClass().getCanonicalName())) {
                if (getEventType().endsWith(event.getType().getName())) {
                    match = true;
                }
            }
        } else {
            if (event.getClass().getCanonicalName().equals(getEventType())) {
                match = true;
            }
        }
        if (match) {
            // There should be at most one child
            for (ITmfFilterTreeNode node : getChildren()) {
                if (! node.matches(event)) {
                    return false;
                }
            }
            return true;
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
        StringBuffer buf = new StringBuffer();
        buf.append("EventType is " + getName()); //$NON-NLS-1$
        if (getChildrenCount() > 0) {
            buf.append(" and "); //$NON-NLS-1$
        }
        buf.append(stringifyChildren(" and ")); //$NON-NLS-1$
        return buf.toString();
    }
}
