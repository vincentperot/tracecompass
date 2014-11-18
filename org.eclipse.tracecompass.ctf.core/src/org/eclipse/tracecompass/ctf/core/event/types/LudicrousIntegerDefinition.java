/*******************************************************************************
 * Copyright (c) 2014 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event.types;

import java.math.BigInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;

/**
 * For really really big ints
 *
 * @author ematkho
 *
 */
@NonNullByDefault
public class LudicrousIntegerDefinition extends IntegerDefinition {

    private BigInteger fBigInt;

    /**
     * For really really really big ints
     *
     * @param declaration
     *            the parent declaration
     * @param definitionScope
     *            the parent scope
     * @param fieldName
     *            the field name
     * @param bigInteger
     *            really really really big integer value
     */
    public LudicrousIntegerDefinition(IntegerDeclaration declaration, @Nullable IDefinitionScope definitionScope, String fieldName, BigInteger bigInteger) {
        super(declaration, definitionScope, fieldName, Long.MAX_VALUE);
        fBigInt = bigInteger;
    }

    @SuppressWarnings("null")
    @Override
    public String getStringValue() {
        return fBigInt.toString(getDeclaration().getBase());
    }

}
