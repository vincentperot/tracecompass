/*******************************************************************************
 * Copyright (c) 2014 Ericsson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.event;

import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for tmf.ctf.core.event
 *
 * @author Matthew Khouzam
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.tmf.ctf.core.event.messages"; //$NON-NLS-1$

    /** Unsupported field type */
    public static String CtfTmfEventField_UnsupportedType;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
