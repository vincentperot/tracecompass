/*******************************************************************************
 * Copyright (c) 2014 Ericsson
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtTraceDefinition.Cardinality;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtTraceDefinition.InputData;

/**
 * Wrapper to store a line of the log file
 */
public class CustomTxtInputLine {

    /** Data columns of this line */
    private List<InputData> columns;

    /** Cardinality of this line (see {@link Cardinality}) */
    private Cardinality cardinality;

    /** Parent line */
    private CustomTxtInputLine parentInput;

    /** Level of this line */
    private int level;

    /** Next input line in the file */
    private CustomTxtInputLine nextInput;

    /** Children of this line (if one "event" spans many lines) */
    private List<CustomTxtInputLine> childrenInputs;

    String regex;
    private Pattern pattern;

    /**
     * Default (empty) constructor.
     */
    public CustomTxtInputLine() {
    }

    /**
     * Constructor.
     *
     * @param cardinality
     *            Cardinality of this line.
     * @param regex
     *            Regex
     * @param columns
     *            Columns to use
     */
    public CustomTxtInputLine(Cardinality cardinality, String regex, List<InputData> columns) {
        this.cardinality = cardinality;
        this.regex = regex;
        this.columns = columns;
    }

    /**
     * Set the regex of this input line
     *
     * @param regex
     *            Regex to set
     */
    public void setRegex(String regex) {
        this.regex = regex;
        this.pattern = null;
    }

    /**
     * Get the current regex
     *
     * @return The current regex
     */
    public String getRegex() {
        return regex;
    }

    /**
     * Get the Pattern object of this line's regex
     *
     * @return The Pattern
     * @throws PatternSyntaxException
     *             If the regex does not parse correctly
     */
    public Pattern getPattern() throws PatternSyntaxException {
        if (pattern == null) {
            pattern = Pattern.compile(regex);
        }
        return pattern;
    }

    /**
     * Add a child line to this line.
     *
     * @param input
     *            The child input line
     */
    public void addChild(CustomTxtInputLine input) {
        if (childrenInputs == null) {
            childrenInputs = new ArrayList<>(1);
        } else if (childrenInputs.size() > 0) {
            CustomTxtInputLine last = childrenInputs.get(childrenInputs.size() - 1);
            last.nextInput = input;
        }
        childrenInputs.add(input);
        input.parentInput = this;
        input.level = this.level + 1;
    }

    /**
     * Set the next input line.
     *
     * @param input
     *            The next input line
     */
    public void addNext(CustomTxtInputLine input) {
        if (parentInput != null) {
            int index = parentInput.childrenInputs.indexOf(this);
            parentInput.childrenInputs.add(index + 1, input);
            CustomTxtInputLine next = nextInput;
            nextInput = input;
            input.nextInput = next;
        }
        input.parentInput = this.parentInput;
        input.level = this.level;
    }

    /**
     * Move this line up in its parent's children.
     */
    public void moveUp() {
        if (parentInput != null) {
            int index = parentInput.childrenInputs.indexOf(this);
            if (index > 0) {
                parentInput.childrenInputs.add(index - 1, parentInput.childrenInputs.remove(index));
                parentInput.childrenInputs.get(index).nextInput = nextInput;
                nextInput = parentInput.childrenInputs.get(index);
            }
        }
    }

    /**
     * Move this line down in its parent's children.
     */
    public void moveDown() {
        if (parentInput != null) {
            int index = parentInput.childrenInputs.indexOf(this);
            if (index < parentInput.childrenInputs.size() - 1) {
                parentInput.childrenInputs.add(index + 1, parentInput.childrenInputs.remove(index));
                nextInput = parentInput.childrenInputs.get(index).nextInput;
                parentInput.childrenInputs.get(index).nextInput = this;
            }
        }
    }

    /**
     * Add a data column to this line
     *
     * @param column
     *            The column to add
     */
    public void addColumn(InputData column) {
        if (columns == null) {
            columns = new ArrayList<>(1);
        }
        columns.add(column);
    }

    /**
     * Get the next input lines.
     *
     * @param countMap
     *            The map of line "sets".
     * @return The next list of lines.
     */
    public List<CustomTxtInputLine> getNextInputs(Map<CustomTxtInputLine, Integer> countMap) {
        List<CustomTxtInputLine> nextInputs = new ArrayList<>();
        CustomTxtInputLine next = nextInput;
        while (next != null) {
            nextInputs.add(next);
            if (next.cardinality.getMin() > 0) {
                return nextInputs;
            }
            next = next.nextInput;
        }
        if (parentInput != null && parentInput.level > 0) {
            int parentCount = countMap.get(parentInput);
            if (parentCount < parentInput.getMaxCount()) {
                nextInputs.add(parentInput);
            }
            if (parentCount < parentInput.getMinCount()) {
                return nextInputs;
            }
            nextInputs.addAll(parentInput.getNextInputs(countMap));
        }
        return nextInputs;
    }

    /**
     * Get the minimum possible amount of entries.
     *
     * @return The minimum
     */
    public int getMinCount() {
        return cardinality.getMin();
    }

    /**
     * Get the maximum possible amount of entries.
     *
     * @return The maximum
     */
    public int getMaxCount() {
        return cardinality.getMax();
    }

    // ------------------------------------------------------------------------
    // Getters and setters
    // ------------------------------------------------------------------------

    /**
     * Get the columns
     *
     * @return the columns
     */
    public List<InputData> getColumns() {
        return columns;
    }

    /**
     * Set the columns
     *
     * @param size
     *            the number of columns
     */
    public void createColumns(int size) {
        columns = new ArrayList<>(size);
    }

    /**
     * Get the cardinality
     *
     * @return the cardinality
     */
    public Cardinality getCardinality() {
        return cardinality;
    }

    /**
     * Set the cardinality
     *
     * @param cardinality
     *            the cardinality to set
     */
    public void setCardinality(Cardinality cardinality) {
        this.cardinality = cardinality;
    }

    /**
     * Get the parent input
     *
     * @return the parentInput
     */
    public CustomTxtInputLine getParentInput() {
        return parentInput;
    }

    /**
     * Does the input line have a parent
     *
     * @return whether the input line has a parent or not
     */
    public boolean hasParent() {
        return parentInput == null;
    }

    /**
     * Get the level
     *
     * @return the level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Get the next input
     *
     * @return the nextInput
     */
    public CustomTxtInputLine getNextInput() {
        return nextInput;
    }

    /**
     * Get the children inputs
     *
     * @return the childrenInputs
     */
    public List<CustomTxtInputLine> getChildrenInputs() {
        return childrenInputs;
    }

    /**
     * Does the input have children
     *
     * @return whether the input has children or not
     */
    public boolean hasChildren() {
        return childrenInputs != null && !childrenInputs.isEmpty();
    }

    @Override
    public String toString() {
        return regex + " " + cardinality; //$NON-NLS-1$
    }
}