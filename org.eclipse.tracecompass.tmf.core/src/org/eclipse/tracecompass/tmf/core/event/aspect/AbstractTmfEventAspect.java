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

package org.eclipse.tracecompass.tmf.core.event.aspect;

/**
 * TmfEventAspect where one does not need to implement {@link #getHelpText()}
 *
 */
public abstract class AbstractTmfEventAspect implements ITmfEventAspect {

    @Override
    public String getHelpText() {
        return EMPTY_STRING;
    }
}
