/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.ctf.core.types;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.types.Definition;
import org.eclipse.tracecompass.ctf.core.types.IDeclaration;
import org.eclipse.tracecompass.ctf.core.types.IDefinitionScope;
import org.eclipse.tracecompass.ctf.core.types.ISimpleDatatypeDefinition;

/**
 * Simple Datatype definition is a datatype that allows the addition of
 * getIntegerValue and getStringValue to a class.
 *
 * @author Matthew Khouzam
 * @since 1.2
 */
public abstract class SimpleDatatypeDefinition extends Definition implements ISimpleDatatypeDefinition {

    /**
     * Create a new SimpleDatatypeDefinition
     *
     * @param declaration
     *            definition's declaration
     * @param definitionScope
     *            The scope of this definition
     * @param fieldName
     *            The name of the field matching this definition in the parent
     *            scope
     * @since 3.0
     */
    public SimpleDatatypeDefinition(@NonNull IDeclaration declaration, IDefinitionScope definitionScope,
            @NonNull String fieldName) {
        super(declaration, definitionScope, fieldName);
    }

    @Override
    public long getIntegerValue() {
        return Long.MIN_VALUE;
    }

    @Override
    public String getStringValue() {
        return null;
    }

    @Override
    public abstract Object getValue();

    @Override
    public double getDoubleValue() {
        return Double.NaN;
    }

}
