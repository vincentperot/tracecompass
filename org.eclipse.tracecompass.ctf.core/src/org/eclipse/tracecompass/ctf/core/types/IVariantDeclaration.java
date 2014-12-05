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

/**
 * Variant declaration to get specific information
 */
public interface IVariantDeclaration extends IDeclaration {

    /**
     * Gets the fields of a variant
     *
     * @return the fields of the variant
     */
    Map<String, IDeclaration> getFields();
}
