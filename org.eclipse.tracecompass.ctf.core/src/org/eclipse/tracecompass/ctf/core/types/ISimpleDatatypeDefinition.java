package org.eclipse.tracecompass.ctf.core.types;

/**
 * A datatype that actually contains data (a leaf datatype)
 *
 */
public interface ISimpleDatatypeDefinition {

    /**
     * Gets the value in integer form
     *
     * @return the integer in a long
     */
    public abstract long getIntegerValue();

    /**
     * Gets the value in string form
     *
     * @return the integer in a String, can be null
     */
    public abstract String getStringValue();

    /**
     * Gets the value of a definition
     *
     * @return the value
     */
    public abstract Object getValue();

    /**
     * Gets the double value of a definition
     *
     * @return the value
     */
    public abstract double getDoubleValue();

}