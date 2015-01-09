/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs;

import org.eclipse.tracecompass.tmf.core.event.concept.TmfEventConceptManager;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.xml.TmfXmlTraceStub;

/**
 * Stub trace for XML traces that represent OS traces with a default
 * implementation of os concepts
 *
 * @author Geneviève Bastien
 */
public class TmfXmlOsTraceStub extends TmfXmlTraceStub {

    static {
        TmfEventConceptManager.registerConcept(TmfXmlOsTraceStub.class, new TmfXmlOsConceptStubs.SchedSwitchConcept());
        TmfEventConceptManager.registerConcept(TmfXmlOsTraceStub.class, new TmfXmlOsConceptStubs.SchedWakeupConcept());
    }

}
