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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.trace.CTFReaderException;
import org.eclipse.tracecompass.ctf.core.types.Declaration;
import org.eclipse.tracecompass.ctf.core.types.Definition;
import org.eclipse.tracecompass.ctf.core.types.ICompositeDeclaration;
import org.eclipse.tracecompass.ctf.core.types.IDeclaration;
import org.eclipse.tracecompass.internal.ctf.core.io.BitBuffer;
import org.eclipse.tracecompass.internal.ctf.core.trace.event.scope.IDefinitionScope;
import org.eclipse.tracecompass.internal.ctf.core.trace.event.scope.LexicalScope;

/**
 * A CTF structure declaration.
 *
 * A structure is similar to a C structure, it is a compound data type that
 * contains other datatypes in fields. they are stored in an hashmap and indexed
 * by names which are strings.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public class StructDeclaration extends Declaration implements ICompositeDeclaration {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /** linked list of field names. So fieldName->fieldValue */
    private final @NonNull Map<String, IDeclaration> fFieldMap = new LinkedHashMap<>();

    /** maximum bit alignment */
    private long fMaxAlign;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * The struct declaration, add fields later
     *
     * @param align
     *            the minimum alignment of the struct. (if a struct is 8bit
     *            aligned and has a 32 bit aligned field, the struct becomes 32
     *            bit aligned.
     */
    public StructDeclaration(long align) {
        fMaxAlign = Math.max(align, 1);
    }

    /**
     * Struct declaration constructor
     *
     * @param names
     *            the names of all the fields
     * @param declarations
     *            all the fields
     * @since 3.0
     */
    public StructDeclaration(String[] names, Declaration[] declarations) {
        fMaxAlign = 1;

        for (int i = 0; i < names.length; i++) {
            addField(names[i], declarations[i]);
        }
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Get current alignment
     *
     * @return the alignment of the struct and all its fields
     */
    public long getMaxAlign() {
        return fMaxAlign;
    }

    /* (non-Javadoc)
     * @see org.eclipse.tracecompass.internal.ctf.core.types.ICompositeDeclaration#hasField(java.lang.String)
     */
    @Override
    public boolean hasField(String name) {
        return fFieldMap.containsKey(name);
    }

    /* (non-Javadoc)
     * @see org.eclipse.tracecompass.internal.ctf.core.types.ICompositeDeclaration#getFields()
     */
    @Override
    public Map<String, IDeclaration> getFields() {
        return fFieldMap;
    }

    /* (non-Javadoc)
     * @see org.eclipse.tracecompass.internal.ctf.core.types.ICompositeDeclaration#getField(java.lang.String)
     */
    @Override
    public IDeclaration getField(String fieldName) {
        return fFieldMap.get(fieldName);
    }

    /* (non-Javadoc)
     * @see org.eclipse.tracecompass.internal.ctf.core.types.ICompositeDeclaration#getFieldsList()
     */
    @Override
    public Iterable<String> getFieldsList() {
        return fFieldMap.keySet();
    }

    @Override
    public long getAlignment() {
        return this.fMaxAlign;
    }

    /**
     * @since 3.0
     */
    @Override
    public int getMaximumSize() {
        int maxSize = 0;
        for (IDeclaration field : fFieldMap.values()) {
            maxSize += field.getMaximumSize();
        }
        return Math.min(maxSize, Integer.MAX_VALUE);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * @since 3.0
     */
    @Override
    public StructDefinition createDefinition(IDefinitionScope definitionScope,
            String fieldName, BitBuffer input) throws CTFReaderException {
        alignRead(input);
        final Definition[] myFields = new Definition[fFieldMap.size()];
        StructDefinition structDefinition = new StructDefinition(this, definitionScope, fieldName, fFieldMap.keySet(), myFields);
        fillStruct(input, myFields, structDefinition);
        return structDefinition;
    }

    /**
     * Create a definition from this declaration. This is a faster constructor
     * as it has a lexical scope and this does not need to look it up.
     *
     * @param definitionScope
     *            the definition scope, the parent where the definition will be
     *            placed
     * @param fieldScope
     *            the scope of the definition
     * @param input
     *            a bitbuffer to read from
     * @return a reference to the definition
     * @throws CTFReaderException
     *             error in reading
     * @since 3.1
     */
    public StructDefinition createDefinition(IDefinitionScope definitionScope,
            LexicalScope fieldScope, @NonNull BitBuffer input) throws CTFReaderException {
        alignRead(input);
        final Definition[] myFields = new Definition[fFieldMap.size()];
        /*
         * Key set is NOT null
         */
        @SuppressWarnings("null")
        StructDefinition structDefinition = new StructDefinition(this, definitionScope, fieldScope, fieldScope.getName(), fFieldMap.keySet(), myFields);
        fillStruct(input, myFields, structDefinition);
        return structDefinition;
    }

    /**
     * Add a field to the struct
     *
     * @param name
     *            the name of the field, scopeless
     * @param declaration
     *            the declaration of the field
     */
    public void addField(String name, IDeclaration declaration) {
        fFieldMap.put(name, declaration);
        fMaxAlign = Math.max(fMaxAlign, declaration.getAlignment());
    }

    @SuppressWarnings("null")
    private void fillStruct(@NonNull BitBuffer input, final Definition[] myFields, StructDefinition structDefinition) throws CTFReaderException {
        Iterator<Map.Entry<String, IDeclaration>> iter = fFieldMap.entrySet().iterator();
        for (int i = 0; i < fFieldMap.size(); i++) {
            Map.Entry<String, IDeclaration> entry = iter.next();
            myFields[i] = entry.getValue().createDefinition(structDefinition, entry.getKey(), input);
        }
    }

    @Override
    public String toString() {
        /* Only used for debugging */
        StringBuilder sb = new StringBuilder();
        sb.append("[declaration] struct["); //$NON-NLS-1$
        for (Entry<String, IDeclaration> field : fFieldMap.entrySet()) {
            sb.append(field.getKey()).append(':').append(field.getValue());
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        for (Entry<String, IDeclaration> field : fFieldMap.entrySet()) {
            result = prime * result + field.getKey().hashCode();
            result = prime * result + field.getValue().hashCode();
        }
        result = (prime * result) + (int) (fMaxAlign ^ (fMaxAlign >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof StructDeclaration)) {
            return false;
        }
        StructDeclaration other = (StructDeclaration) obj;
        if (fFieldMap.size() != other.fFieldMap.size()) {
            return false;
        }

        List<String> localFieldNames = new ArrayList<>();
        localFieldNames.addAll(fFieldMap.keySet());

        List<IDeclaration> localDecs = new ArrayList<>();
        localDecs.addAll(fFieldMap.values());

        List<String> otherFieldNames = new ArrayList<>();
        otherFieldNames.addAll(other.fFieldMap.keySet());

        List<IDeclaration> otherDecs = new ArrayList<>();
        otherDecs.addAll(other.fFieldMap.values());

        //check fields in order
        for (int i = 0; i < fFieldMap.size(); i++) {
            if ((!localFieldNames.get(i).equals(otherFieldNames.get(i))) ||
                    (!otherDecs.get(i).equals(localDecs.get(i)))) {
                return false;
            }
        }

        if (fMaxAlign != other.fMaxAlign) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isBinaryEquivalent(IDeclaration obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof StructDeclaration)) {
            return false;
        }
        StructDeclaration other = (StructDeclaration) obj;
        if (fFieldMap.size() != other.fFieldMap.size()) {
            return false;
        }
        List<IDeclaration> localDecs = new ArrayList<>();
        localDecs.addAll(fFieldMap.values());
        List<IDeclaration> otherDecs = new ArrayList<>();
        otherDecs.addAll(other.fFieldMap.values());
        for (int i = 0; i < fFieldMap.size(); i++) {
            if (!otherDecs.get(i).isBinaryEquivalent(localDecs.get(i))) {
                return false;
            }
        }

        if (fMaxAlign != other.fMaxAlign) {
            return false;
        }
        return true;
    }

}
