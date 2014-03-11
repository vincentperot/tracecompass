/**********************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.component;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * <p>
 * Interface for TMF composite components that have parent-child-relationship.
 * </p>
 *
 * @author Bernd Hufmann
 * @since 3.0
 */
public interface ITmfCompositeComponent extends ITmfComponent {

    /**
     * Gets the parent component.
     *
     * @return the parent component.
     */
    @Nullable
    ITmfCompositeComponent getParent();

    /**
     * Sets the parent component.
     *
     * @param parent
     *            the parent to set.
     */
    void setParent(@Nullable ITmfCompositeComponent parent);

    /**
     * Adds a child component.
     *
     * @param child
     *            child to add.
     */
    void addChild(@NonNull ITmfCompositeComponent child);

    /**
     * Gets the children components.
     *
     * @return the children components
     */
    @NonNull
    List<ITmfCompositeComponent> getChildren();

    /**
     * Returns the child component with given name.
     *
     * @param name
     *            name of child to find.
     * @return child component or null.
     */
    @Nullable
    ITmfCompositeComponent getChild(String name);

    /**
     * Returns the child component for a given index
     *
     * @param index index of child to get
     * @return child component
     */
    @NonNull
    ITmfCompositeComponent getChild(int index);

    /**
     * Gets children for given class type.
     *
     * @param clazz
     *            a class type to get
     * @return list of trace control components matching given class type.
     */
    @NonNull
    <T extends ITmfCompositeComponent> List<T> getChildren(Class<T> clazz);

    /**
     * Gets the number of children
     *
     * @return number of children
     */
    int getNbChildren();
}
