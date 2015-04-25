/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.ctf.core.trace;

import java.io.Serializable;
import java.util.Comparator;

import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.ctf.core.trace.CTFStreamInputReader;
import org.eclipse.tracecompass.ctf.core.trace.Utils;

/**
 * <b><u>StreamInputReaderTimestampComparator</u></b>
 * <p>
 * Compares two StreamInputReader by their timestamp (smaller comes before).
 */
public class StreamInputReaderTimestampComparator implements
        Comparator<CTFStreamInputReader>, Serializable {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final long serialVersionUID = 1066434959451875045L;

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * @throws NullPointerException
     *             If any {@link CTFStreamInputReader} parameter is null, of if any
     *             of them does not contain a current event.
     */
    @Override
    public int compare(CTFStreamInputReader a, CTFStreamInputReader b) {
        IEventDefinition event_a = a.getCurrentEvent();
        IEventDefinition event_b = b.getCurrentEvent();

        long ta = event_a.getTimestamp();
        long tb = event_b.getTimestamp();
        return Utils.unsignedCompare(ta, tb);
    }

}
