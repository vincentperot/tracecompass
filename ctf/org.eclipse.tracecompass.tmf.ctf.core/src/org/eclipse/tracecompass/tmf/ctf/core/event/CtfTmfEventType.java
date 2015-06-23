/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.event;

import org.eclipse.tracecompass.tmf.core.event.TmfEventType;

/**
 * The CTF extension of the TMF event type
 *
 * @author Matthew khouzam
 */
public class CtfTmfEventType extends TmfEventType {

    /**
     * Constructor for CtfTmfEventType.
     *
     * @param eventName
     *            The event name
     * @since 2.0
     */
    public CtfTmfEventType(String eventName) {
        super(eventName);
    }

    /**
     * Method toString.
     *
     * @return String
     */
    @Override
    public String toString() {
        return getName();
    }
}
