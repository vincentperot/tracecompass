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

package org.eclipse.tracecompass.ctf.core.event.types;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.tracecompass.ctf.core.event.scope.ILexicalScope;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

/**
 * A CTF structure definition (similar to a C structure).
 *
 * A structure is similar to a C structure, it is a compound data type that
 * contains other datatypes in fields. they are stored in an hashmap and indexed
 * by names which are strings.
 *
 * TODO: move me to internal
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public final class StructDefinition extends ScopedDefinition implements ICompositeDefinition {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final ImmutableList<String> fFieldNames;
    private final Definition[] fDefinitions;
    private Map<String, Definition> fDefinitionsMap = null;

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
     * @param structFieldName
     *            the field name
     * @param definitions
     *            the definitions
     * @since 1.0
     */
    public StructDefinition(@NonNull StructDeclaration declaration,
            IDefinitionScope definitionScope,
            @NonNull String structFieldName,
            Definition[] definitions) {
        super(declaration, definitionScope, structFieldName);
        fFieldNames = ImmutableList.copyOf(declaration.getFieldsList());
        fDefinitions = definitions;
        if (fFieldNames.isEmpty()) {
            fDefinitionsMap = Collections.EMPTY_MAP;
        }
    }

    /**
     * Constructor This one takes the scope and thus speeds up definition
     * creation
     *
     * @param declaration
     *            the parent declaration
     * @param definitionScope
     *            the parent scope
     * @param scope
     *            the scope of this variable
     * @param structFieldName
     *            the field name
     * @param fieldNames
     *            the list of fields
     * @param definitions
     *            the definitions
     * @since 1.0
     */
    public StructDefinition(@NonNull StructDeclaration declaration,
            IDefinitionScope definitionScope, @NonNull ILexicalScope scope,
            @NonNull String structFieldName, @NonNull Iterable<String> fieldNames, Definition[] definitions) {
        super(declaration, definitionScope, structFieldName, scope);
        fFieldNames = ImmutableList.copyOf(fieldNames);
        fDefinitions = definitions;
        if (fFieldNames.isEmpty()) {
            fDefinitionsMap = Collections.EMPTY_MAP;
        }
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    @Override
    public Definition getDefinition(String fieldName) {
        if (fDefinitionsMap == null) {
            /* Build the definitions map */
            Builder<String, Definition> mapBuilder = new ImmutableMap.Builder<>();
            for (int i = 0; i < fFieldNames.size(); i++) {
                if (fDefinitions[i] != null) {
                    mapBuilder.put(fFieldNames.get(i), fDefinitions[i]);
                }
            }
            fDefinitionsMap = mapBuilder.build();
        }
        return fDefinitionsMap.get(fieldName);
    }

    @Override
    public List<String> getFieldNames() {
        return fFieldNames;
    }

    @Override
    public StructDeclaration getDeclaration() {
        return (StructDeclaration) super.getDeclaration();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public Definition lookupDefinition(String lookupPath) {
        /*
         * The fields are created in order of appearance, so if a variant or
         * sequence refers to a field that is after it, the field's definition
         * will not be there yet in the hashmap.
         */
        int val = fFieldNames.indexOf(lookupPath);
        if (val != -1) {
            return fDefinitions[val];
        }
        String lookupUnderscored = "_" + lookupPath; //$NON-NLS-1$
        val = fFieldNames.indexOf(lookupUnderscored);
        if (val != -1) {
            return fDefinitions[val];
        }
        return (Definition) getDefinitionScope().lookupDefinition(lookupPath);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("{ "); //$NON-NLS-1$

        if (fFieldNames != null) {
            List<String> fields = new LinkedList<>();
            for (String field : fFieldNames) {
                String appendee = field + " = " + lookupDefinition(field).toString(); //$NON-NLS-1$
                fields.add(appendee);
            }
            Joiner joiner = Joiner.on(", ").skipNulls(); //$NON-NLS-1$
            builder.append(joiner.join(fields));
        }

        builder.append(" }"); //$NON-NLS-1$

        return builder.toString();
    }

}
