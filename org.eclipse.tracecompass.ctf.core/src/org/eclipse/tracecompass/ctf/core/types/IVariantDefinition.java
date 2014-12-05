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
 * Variant definition to get specific information
 */
public interface IVariantDefinition {

    /**
     * Get the current field name
     *
     * @return the current field name
     */
    String getCurrentFieldName();

    /**
     * Get the current field
     *
     * @return the current field
     */
    IDefinition getCurrentField();

}