package org.eclipse.tracecompass.ctf.core.types;

/**
 * Gets the datatypes that can be many types depending on the a determinator
 *
 */
public interface IIntersectsDefinition extends IDefinition{

    /**
     * Get the current field name
     *
     * @return the current field name
     */
    public abstract String getCurrentFieldName();

    /**
     * Get the current field
     *
     * @return the current field
     */
    public abstract Definition getCurrentField();

}