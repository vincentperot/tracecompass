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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Filter node for the regex match
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TmfFilterMatchesNode extends TmfFilterTreeNode {

    /** The node name */
    public static final String NODE_NAME = "MATCHES"; //$NON-NLS-1$
    /** This node supports <em>NOT</em> */
    public static final String NOT_ATTR = "not"; //$NON-NLS-1$
    /** This node supports fields */
    public static final String FIELD_ATTR = "field"; //$NON-NLS-1$
    /** This node supports regular expressions */
    public static final String REGEX_ATTR = "regex"; //$NON-NLS-1$

    private boolean fNot = false;
    private String fRegex;
    private transient Pattern fPattern;

    /**
     * Constructor
     *
     * @param parent
     *            the parent node
     */
    public TmfFilterMatchesNode(ITmfFilterTreeNode parent) {
        super(parent);
    }

    /**
     * Get the regular expression
     *
     * @return the regular expression
     */
    public String getRegex() {
        return fRegex;
    }

    /**
     * Sets the regular expression
     *
     * @param regex
     *            the regular expression
     */
    public void setRegex(String regex) {
        this.fRegex = regex;
        if (regex != null) {
            try {
                this.fPattern = Pattern.compile(regex, Pattern.DOTALL);
            } catch (PatternSyntaxException e) {
                this.fPattern = null;
            }
        }
    }

    @Override
    public String getEventType() {
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
    public String getValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEventType(String type) {
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
    public void setValue(String value) {
        throw new UnsupportedOperationException();
    }


    @Override
    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public boolean matches(ITmfEvent event) {
        if (fPattern == null) {
            return false ^ fNot;
        }

        Object value = getFieldValue(event, getField());
        if (value == null) {
            return false ^ fNot;
        }
        String valueString = value.toString();

        return fPattern.matcher(valueString).matches() ^ fNot;
    }

    @Override
    public List<String> getValidChildren() {
        return new ArrayList<>(0);
    }

    @Override
    public String toString() {
        return getField() + (fNot ? " not" : "") + " matches \"" + fRegex + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    @Override
    public ITmfFilterTreeNode clone() {
        TmfFilterMatchesNode clone = (TmfFilterMatchesNode) super.clone();
        clone.setField(getField());
        clone.setRegex(fRegex);
        return clone;
    }

    /**
     * <p>
     * Sanitize a regular expression
     * </p>
     * if the pattern does not contain one of the expressions .* !^ (at the
     * beginning) $ (at the end), then a .* is added at the beginning and at the
     * end of the pattern
     *
     * @param pattern
     *            the rough regex pattern
     * @return the compliant regex
     */
    public static String regexFix(String pattern) {
        String ret = pattern;
        // if the pattern does not contain one of the expressions .* !^
        // (at the beginning) $ (at the end), then a .* is added at the
        // beginning and at the end of the pattern
        if (!(ret.indexOf(".*") >= 0 || ret.charAt(0) == '^' || ret.charAt(ret.length() - 1) == '$')) { //$NON-NLS-1$
            ret = ".*" + ret + ".*"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return ret;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((getField() == null) ? 0 : getField().hashCode());
        result = prime * result + (fNot ? 1231 : 1237);
        result = prime * result + ((fRegex == null) ? 0 : fRegex.hashCode());
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
        TmfFilterMatchesNode other = (TmfFilterMatchesNode) obj;
        if (getField() == null) {
            if (other.getField() != null) {
                return false;
            }
        } else if (!getField().equals(other.getField())) {
            return false;
        }
        if (fNot != other.fNot) {
            return false;
        }
        if (fRegex == null) {
            if (other.fRegex != null) {
                return false;
            }
        } else if (!fRegex.equals(other.fRegex)) {
            return false;
        }
        return true;
    }
}
