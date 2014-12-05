/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.ctf.core.types;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.types.Definition;
import org.eclipse.tracecompass.ctf.core.types.IDefinition;
import org.eclipse.tracecompass.ctf.core.types.IDefinitionScope;
import org.eclipse.tracecompass.ctf.core.types.IVariantDefinition;

/**
 * A CTF variant definition (similar to a C union).
 *
 * A variant is similar to a C union, only taking the minimum size of the types,
 * it is a compound data type that contains other datatypes in fields. they are
 * stored in an hashmap and indexed by names which are strings.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public final class VariantDefinition extends ScopedDefinition implements IVariantDefinition {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final IDefinition fDefinition;
    private final String fCurrentField;
    private final String fFieldName;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param declaration
     *            the parent declaration
     * @param definitionScope
     *            the parent scope
     * @param selectedField
     *            the selected field
     * @param fieldName
     *            the field name
     * @param fieldValue
     *            the field value
     * @since 3.0
     */
    public VariantDefinition(@NonNull VariantDeclaration declaration,
            IDefinitionScope definitionScope, String selectedField, @NonNull String fieldName, IDefinition fieldValue) {
        super(declaration, definitionScope, fieldName);

        fFieldName = fieldName;
        fCurrentField = selectedField;
        fDefinition = fieldValue;

    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    @Override
    public VariantDeclaration getDeclaration() {
        return (VariantDeclaration) super.getDeclaration();
    }

    /* (non-Javadoc)
     * @see org.eclipse.tracecompass.internal.ctf.core.types.IVariantDefintion#getCurrentFieldName()
     */
    @Override
    public String getCurrentFieldName() {
        return fCurrentField;
    }

    /* (non-Javadoc)
     * @see org.eclipse.tracecompass.internal.ctf.core.types.IVariantDefintion#getCurrentField()
     */
    @Override
    public IDefinition getCurrentField() {
        return fDefinition;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public Definition lookupDefinition(String lookupPath) {
        if (lookupPath == null) {
            return null;
        }
        if (lookupPath.equals(fFieldName)) {
            return (Definition)fDefinition;
        }
        return getDefinitionScope().lookupDefinition(lookupPath);
    }

    @Override
    public String toString() {
        return "{ " + getCurrentFieldName() + //$NON-NLS-1$
                " = " + getCurrentField() + //$NON-NLS-1$
                " }"; //$NON-NLS-1$
    }
}
