/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.ctf.core.trace.reader;

import java.io.IOException;

import org.eclipse.tracecompass.ctf.core.CTFException;

/**
 * CTF trace reader
 */
public class CTFReaderException extends CTFException {

    /**
     * Serial id
     */
    private static final long serialVersionUID = 1950355491665639826L;

    /**
     * Constructor with an attached message.
     *
     * @param message
     *            The message attached to this exception
     */
    public CTFReaderException(String message) {
        super(message);
    }

    /**
     * Constructor with an attached message.
     *
     * @param message
     *            The message attached to this exception
     * @param e
     *            The thrown exception
     */
    public CTFReaderException(String message, Exception e) {
        super(message, e);
    }

    /**
     * Constructor with exception
     *
     * @param e
     *            the thrown exception
     */
    public CTFReaderException(IOException e) {
        super(e);
    }

}
