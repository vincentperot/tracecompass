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
package org.eclipse.tracecompass.ctf.core.trace.writer;

import java.io.IOException;

import org.eclipse.tracecompass.ctf.core.CTFException;

/**
 * CTF trace reader
 */
public class CTFWriterException extends CTFException {

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
    public CTFWriterException(String message) {
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
    public CTFWriterException(String message, Exception e) {
        super(message, e);
    }

    /**
     * Constructor with exception
     *
     * @param e
     *            the thrown exception
     */
    public CTFWriterException(IOException e) {
        super(e);
    }

}
