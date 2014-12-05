package org.eclipse.tracecompass.ctf.core.types;


public interface ICompoundDeclaration extends IDeclaration{

    /**
     * Get the element type
     *
     * @return the type of element in the array
     */
    public abstract IDeclaration getElementType();

    /**
     * Sometimes, strings are encoded as an array of 1-byte integers (each one
     * being an UTF-8 byte).
     *
     * @return true if this array is in fact an UTF-8 string. false if it's a
     *         "normal" array of generic Definition's.
     */
    public abstract boolean isString();

}