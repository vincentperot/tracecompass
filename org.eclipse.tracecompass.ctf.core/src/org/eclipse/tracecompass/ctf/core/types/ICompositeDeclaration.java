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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.trace.CTFReaderException;
import org.eclipse.tracecompass.internal.ctf.core.io.BitBuffer;
import org.eclipse.tracecompass.internal.ctf.core.trace.event.scope.LexicalScope;

/**
 * Interface for data definitions containing heterogenous definitions (elements
 * in an array)
 *
 * @author Matthew Khouzam
 * @since 3.1
 */
public interface ICompositeDeclaration extends IDeclaration {

    /**
     * Gets the field list. This retains the order of the fields when
     * applicable.
     *
     * @return the field list.
     * @since 3.0
     */
    @NonNull public Iterable<String> getFieldsList();

    /**
     * Gets the fields of the composite
     *
     * @return the fields of the composite
     */
    @NonNull public Map<String, IDeclaration> getFields();

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
    @NonNull public ICompositeDefinition createDefinition(IDefinitionScope definitionScope, @NonNull LexicalScope fieldScope, @NonNull BitBuffer input) throws CTFReaderException;

}