/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.ust.ui.analysis.callstack;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import org.eclipse.tracecompass.internal.lttng2.ust.core.trace.callstack.LttngUstCallStackProvider;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.views.callstack.AbstractCallStackAnalysis;

/**
 * Call-stack analysis to populate the TMF CallStack View from UST cyg-profile
 * events.
 *
 * @author Alexandre Montplaisir
 * @since 3.0
 */
public class LttngUstCallStackAnalysis extends AbstractCallStackAnalysis {

    @Override
    public void setTrace(ITmfTrace trace) throws TmfAnalysisException {
        if (!(trace instanceof LttngUstTrace)) {
            throw new IllegalArgumentException("Trace should be of type LttngUstTrace"); //$NON-NLS-1$
        }
        super.setTrace(trace);
    }

    @Override
    protected LttngUstTrace getTrace() {
        return (LttngUstTrace) super.getTrace();
    }

    @Override
    protected ITmfStateProvider createStateProvider() {
        return new LttngUstCallStackProvider(checkNotNull(getTrace()));
    }

}
