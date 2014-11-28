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

import java.util.Arrays;
import java.util.List;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * The Filter tree root node
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TmfFilterRootNode extends TmfFilterTreeNode {

    /** The node name */
    public static final String NODE_NAME = "ROOT"; //$NON-NLS-1$

    private static final String[] VALID_CHILDREN = {
            TmfFilterNode.NODE_NAME
    };

    /**
     * Default constructor
     */
    public TmfFilterRootNode() {
        super(null);
    }

    @Override
    public String getEventType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getField() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getFilterName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isIgnoreCase() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isNot() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEventType(String type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setField(String field) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFilterName(String filterName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setIgnoreCase(boolean ignoreCase) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNot(boolean not) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public boolean matches(ITmfEvent event) {
        for (ITmfFilterTreeNode node : getChildren()) {
            if (!node.matches(event)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<String> getValidChildren() {
        return Arrays.asList(VALID_CHILDREN);
    }
}
