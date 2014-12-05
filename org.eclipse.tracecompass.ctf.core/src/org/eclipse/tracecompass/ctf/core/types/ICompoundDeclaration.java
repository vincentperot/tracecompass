/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.ctf.core.types;

/**
 * Interface for data definitions containing homogenous definitions
 * (elements in an array)
 *
 * @author Matthew Khouzam
 * @since 3.1
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