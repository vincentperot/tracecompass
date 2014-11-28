package org.eclipse.tracecompass.tmf.core.filter.model;

import com.google.common.base.Joiner;

public abstract class TmfFilterTreeNode extends AbstractTmfFilterTreeNode {

    private boolean fNot;
    private String fFilterName;
    private String fField;
    private String fValue;
    private String fType;
    private String fName;

    protected TmfFilterTreeNode(ITmfFilterTreeNode parent) {
        super(parent);
        setNot(false);
    }

    /**
     * Get the <em>NOT</em> state
     *
     * @return the <em>NOT</em> state
     */
    public boolean isNot() {
        return fNot;
    }

    /**
     * Set the <em>NOT</em> state
     *
     * @param not
     *            the <em>NOT</em> state
     */
    public void setNot(boolean not) {
        fNot = not;
    }

    /**
     * Get the filter name
     *
     * @return the filer name
     */
    public String getFilterName() {
        return fFilterName;
    }

    /**
     * Set the filter name
     *
     * @param filterName
     *            the filer name
     */
    public void setFilterName(String filterName) {
        fFilterName = filterName;
    }

    /**
     * Get the field name
     *
     * @return the field name
     */
    public String getField() {
        return fField;
    }

    /**
     * Set the field name
     *
     * @param field
     *            the field name
     */
    public void setField(String field) {
        fField = field;
    }

    /**
     * Get the value
     *
     * @return the value
     */
    public String getValue() {
        return fValue;
    }

    /**
     * Set the value
     *
     * @param value
     *            the value
     */
    public void setValue(String value) {
        fValue = value;
    }

    /**
     * Get the event type
     *
     * @return the event type
     */
    public String getEventType() {
        return fType;
    }

    /**
     * Set the event type
     *
     * @param type
     *            the event type
     */
    public void setEventType(String type) {
        fType = type;
    }

    /**
     * Get the category and trace type name
     *
     * @return the category and trace type name
     */
    public String getName() {
        return fName;
    }

    /**
     * Set the category and trace type name
     *
     * @param name
     *            the category and trace type name
     */
    public void setName(String name) {
        fName = name;
    }

    /**
     * Makes a StringBuilder with the operation toString {<not >{ child0 op
     * child1 op .. childN }
     *
     * @param operation
     *            the separation operator
     * @return the StringBuilder
     */
    protected StringBuilder stringifyChildren(String operation) {
        StringBuilder buf = new StringBuilder();
        if (isNot()) {
            buf.append("not "); //$NON-NLS-1$
        }
        if (getParent() != null && !(getParent() instanceof TmfFilterRootNode) && !(getParent() instanceof TmfFilterNode)) {
            buf.append("( "); //$NON-NLS-1$
        }
        Joiner.on(operation).appendTo(buf, getChildren());
        if (getParent() != null && !(getParent() instanceof TmfFilterRootNode) && !(getParent() instanceof TmfFilterNode)) {
            buf.append(" )"); //$NON-NLS-1$
        }
        return buf;
    }

}
