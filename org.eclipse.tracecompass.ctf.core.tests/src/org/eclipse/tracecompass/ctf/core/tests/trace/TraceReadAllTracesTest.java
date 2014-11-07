/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.trace;

import static org.junit.Assert.*;

import org.eclipse.tracecompass.ctf.core.CTFStrings;
import org.eclipse.tracecompass.ctf.core.event.EventDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDefinition;
import org.eclipse.tracecompass.ctf.core.tests.shared.CtfTestTrace;
import org.eclipse.tracecompass.ctf.core.trace.CTFReaderException;
import org.eclipse.tracecompass.ctf.core.trace.CTFTraceReader;
import org.junit.Test;

/**
 * Read all the traces and make sure they make sense
 *
 * @author Matthew Khouzam
 */
public class TraceReadAllTracesTest {

    /**
     * Reads all the traces
     */
    @Test
    public void readTraces() {
        for (CtfTestTrace traceEnum : CtfTestTrace.values()) {
            if (traceEnum.getNbEvents() != -1) {
                try {

                    try (CTFTraceReader reader = new CTFTraceReader(traceEnum.getTrace())) {
                        EventDefinition currentEventDef = reader.getCurrentEventDef();
                        double start = currentEventDef.getTimestamp();
                        long count = 0;
                        double end = start;
                        while (reader.hasMoreEvents()) {
                            reader.advance();
                            count++;
                            currentEventDef = reader.getCurrentEventDef();
                            if (currentEventDef != null) {
                                end = currentEventDef.getTimestamp();
                                if (currentEventDef.getDeclaration().getName().equals(CTFStrings.LOST_EVENT_NAME)) {
                                    count += ((IntegerDefinition) currentEventDef.getFields().getDefinition(CTFStrings.LOST_EVENTS_FIELD)).getValue() - 1;
                                }
                            }
                        }
                        assertEquals("Event count", traceEnum.getNbEvents(), count);
                        assertEquals("Trace duration", traceEnum.getDuration(), (end - start) / 1000000000.0, 1.0);
                    }
                } catch (CTFReaderException e) {
                    fail(traceEnum.getPath() + " " + e.getMessage());
                }
            }
        }
    }
}
