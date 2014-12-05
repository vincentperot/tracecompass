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

package org.eclipse.tracecompass.internal.ctf.core.types;

import org.eclipse.tracecompass.ctf.core.types.Declaration;
import org.eclipse.tracecompass.ctf.core.types.ICompoundDeclaration;
import org.eclipse.tracecompass.ctf.core.types.IDeclaration;


/**
 * Parent of sequences and arrays
 *
 * @author Matthew Khouzam
 * @since 3.1
 */
public abstract class CompoundDeclaration extends Declaration implements ICompoundDeclaration {

    /* (non-Javadoc)
     * @see org.eclipse.tracecompass.internal.ctf.core.types.ICompoundDeclaration#getElementType()
     */
    @Override
    public abstract IDeclaration getElementType();

    @Override
    public long getAlignment() {
        return getElementType().getAlignment();
    }

    @Override
    public boolean isString(){
        IDeclaration elementType = getElementType();
        if (elementType instanceof IntegerDeclaration) {
            IntegerDeclaration elemInt = (IntegerDeclaration) elementType;
            return elemInt.isCharacter();
        }
        return false;
    }

}
