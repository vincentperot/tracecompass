/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
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

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;

/**
 * Filter node for the comparison operation
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TmfFilterCompareNode extends TmfFilterTreeNode {

    /** The node name */
    public static final String NODE_NAME = "COMPARE"; //$NON-NLS-1$
    /** This node supports <em>NOT</em> */
    public static final String NOT_ATTR = "not"; //$NON-NLS-1$
    /** This node supports fields */
    public static final String FIELD_ATTR = "field"; //$NON-NLS-1$
    /** This node supports results */
    public static final String RESULT_ATTR = "result"; //$NON-NLS-1$
    /** This node supports event types */
    public static final String TYPE_ATTR = "type"; //$NON-NLS-1$
    /** This node supports values */
    public static final String VALUE_ATTR = "value"; //$NON-NLS-1$

    /**
     * Supported comparison types
     */
    public static enum Type {
        /** Number */
        NUM,
        /** String */
        ALPHA,
        /** Timestamp */
        TIMESTAMP
    }

    private int fResult;
    private Type fType = Type.NUM;
    private String fValue;
    private transient Number fValueNumber;
    private transient TmfTimestamp fValueTimestamp;

    /**
     * @param parent
     *            the parent node
     */
    public TmfFilterCompareNode(ITmfFilterTreeNode parent) {
        super(parent);
    }

    /**
     * @return the compare result
     */
    public int getResult() {
        return fResult;
    }

    /**
     * @param result
     *            the compare result
     */
    public void setResult(int result) {
        this.fResult = result;
    }

    /**
     * @return the comparison type
     */
    public Type getType() {
        return fType;
    }

    /**
     * @param type
     *            the comparison type
     */
    public void setType(Type type) {
        this.fType = type;
        setValue(fValue);
    }

    /**
     * @param value
     *            the comparison value
     */
    @Override
    public void setValue(String value) {
        super.setValue(value);
        fValueNumber = null;
        fValueTimestamp = null;
        if (value == null) {
            return;
        }
        if (fType == Type.NUM) {
            try {
                fValueNumber = NumberFormat.getInstance().parse(value).doubleValue();
            } catch (ParseException e) {
            }
        } else if (fType == Type.TIMESTAMP) {
            try {
                fValueTimestamp = new TmfTimestamp((long) (1E9 * NumberFormat.getInstance().parse(value.toString()).doubleValue()));
            } catch (ParseException e) {
            }
        }
    }

    @Override
    public String getNodeName() {
        return NODE_NAME;
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
    public boolean matches(ITmfEvent event) {
        Object value = getFieldValue(event, getField());
        if (value == null) {
            return false ^ isNot();
        }
        if (fType == Type.NUM) {
            if (fValueNumber != null) {
                if (value instanceof Number) {
                    double valueDouble = ((Number) value).doubleValue();
                    return (Double.compare(valueDouble, fValueNumber.doubleValue()) == fResult) ^ isNot();
                }
                try {
                    double valueDouble = NumberFormat.getInstance().parse(value.toString()).doubleValue();
                    return (Double.compare(valueDouble, fValueNumber.doubleValue()) == fResult) ^ isNot();
                } catch (ParseException e) {
                }
            }
        } else if (fType == Type.ALPHA) {
            String valueString = value.toString();
            int comp = valueString.compareTo(fValue.toString());
            if (comp < -1) {
                comp = -1;
            } else if (comp > 1) {
                comp = 1;
            }
            return (comp == fResult) ^ isNot();
        } else if (fType == Type.TIMESTAMP) {
            if (fValueTimestamp != null) {
                if (value instanceof TmfTimestamp) {
                    TmfTimestamp valueTimestamp = (TmfTimestamp) value;
                    return (valueTimestamp.compareTo(fValueTimestamp) == fResult) ^ isNot();
                }
                try {
                    TmfTimestamp valueTimestamp = new TmfTimestamp((long) (1E9 * NumberFormat
                            .getInstance().parse(value.toString()).doubleValue()));
                    return (valueTimestamp.compareTo(fValueTimestamp) == fResult) ^ isNot();
                } catch (ParseException e) {
                }
            }
        }
        return false ^ isNot();
    }

    @Override
    public List<String> getValidChildren() {
        return new ArrayList<>(0);
    }

    @Override
    public String toString() {
        String result = (fResult == 0 ? "= " : fResult < 0 ? "< " : "> "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String open = (fType == Type.NUM ? "" : fType == Type.ALPHA ? "\"" : "["); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String close = (fType == Type.NUM ? "" : fType == Type.ALPHA ? "\"" : "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return getField() + (isNot() ? " not " : " ") + result + open + fValue + close; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public ITmfFilterTreeNode clone() {
        TmfFilterCompareNode clone = (TmfFilterCompareNode) super.clone();
        clone.setField(getField());
        clone.setValue(getValue());
        return clone;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((getField() == null) ? 0 : getField().hashCode());
        result = prime * result + (isNot() ? 1231 : 1237);
        result = prime * result + fResult;
        result = prime * result + ((fType == null) ? 0 : fType.hashCode());
        result = prime * result + ((fValue == null) ? 0 : fValue.hashCode());
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
        TmfFilterCompareNode other = (TmfFilterCompareNode) obj;
        if (getField() == null) {
            if (other.getField() != null) {
                return false;
            }
        } else if (!getField().equals(other.getField())) {
            return false;
        }
        if (isNot() != other.isNot()) {
            return false;
        }
        if (fResult != other.fResult) {
            return false;
        }
        if (fType != other.fType) {
            return false;
        }
        if (fValue == null) {
            if (other.fValue != null) {
                return false;
            }
        } else if (!fValue.equals(other.fValue)) {
            return false;
        }
        return true;
    }
}
