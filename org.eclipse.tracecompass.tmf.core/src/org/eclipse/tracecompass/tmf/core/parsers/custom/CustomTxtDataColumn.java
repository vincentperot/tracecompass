/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Matthew Khouzam - Moved to own class
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.core.parsers.custom;

/**
 * Data column for input lines.
 * @since 1.0
 */
public class CustomTxtDataColumn {

    /** Name of this column */
    private String fName;

    /** Action id */
    private int fAction;

    /** Format */
    private String fFormat;

    /**
     * Default (empty) constructor
     */
    public CustomTxtDataColumn() {
    }

    /**
     * Full constructor
     *
     * @param name
     *            Name
     * @param action
     *            Action
     * @param format
     *            Format
     */
    public CustomTxtDataColumn(String name, int action, String format) {
        fName = name;
        fAction = action;
        fFormat = format;
    }

    /**
     * Constructor with default format
     *
     * @param name
     *            Name
     * @param action
     *            Action
     */
    public CustomTxtDataColumn(String name, int action) {
        fName = name;
        fAction = action;
    }


    /**
     * Get the name
     * @return the name
     */
    public String getName() {
        return fName;
    }

    /**
     * Set the name
     * @param name the name
     */
    public void setName(String name) {
        fName = name;
    }

    /**
     * Get the action
     * @return the action
     */
    public int getAction() {
        return fAction;
    }

    /**
     * Set the action
     * @param action the action
     */
    public void setAction(int action) {
        fAction = action;
    }

    /**
     * Get the format
     * @return the format string
     */
    public String getFormat() {
        return fFormat;
    }

    /**
     * Set the format
     * @param format the format string
     */
    public void setFormat(String format) {
        fFormat = format;
    }
}