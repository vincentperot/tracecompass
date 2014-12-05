package org.eclipse.tracecompass.ctf.core.types;

/**
 * Homogeneous children declaration
 *
 */
public interface ICompoundDeclaration extends IDeclaration{

    /**
     * Get the element type
     *
     * @return the type of element in the array
     */
    IDeclaration getElementType();

    /**
     * Sometimes, strings are encoded as an array of 1-byte integers (each one
     * being an UTF-8 byte).
     *
     * @return true if this array is in fact an UTF-8 string. false if it's a
     *         "normal" array of generic Definition's.
     */
    boolean isString();

    /**
     * @return is this an array of integers?
     */
    boolean isInteger();

}