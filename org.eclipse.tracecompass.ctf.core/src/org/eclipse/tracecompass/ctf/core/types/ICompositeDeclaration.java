/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.types;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Heterogeneous element declaration
 */
public interface ICompositeDeclaration extends IDeclaration{

    /**
     * Query if the struct has a given field
     *
     * @param name
     *            the name of the field, scopeless please
     * @return does the field exist?
     */
    public abstract boolean hasField(String name);

    /**
     * Get the fields of the struct as a map.
     *
     * @return a Map of the fields (key is the name)
     * @since 2.0
     */
    public abstract Map<String, IDeclaration> getFields();

    /**
     * Get the field declaration corresponding to a field name.
     *
     * @param fieldName
     *            The field name
     * @return The declaration of the field, or null if there is no such field.
     * @since 3.1
     */
    @Nullable
    public abstract IDeclaration getField(String fieldName);

    /**
     * Gets the field list. Very important since the map of fields does not
     * retain the order of the fields.
     *
     * @return the field list.
     * @since 3.0
     */
    public abstract Iterable<String> getFieldsList();

}