/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *   Guilliano Molaire - Provide the requirements of the analysis
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.ust.core.analysis.memory;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Collections;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.memory.UstMemoryStateProvider;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAnalysisRequirement;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * This analysis build a state system from the libc memory instrumentation on a
 * UST trace
 *
 * @author Geneviève Bastien
 */
public class UstMemoryAnalysisModule extends TmfStateSystemAnalysisModule {

    /**
     * Analysis ID, it should match that in the plugin.xml file
     */
    public static final @NonNull String ID = "org.eclipse.linuxtools.lttng2.ust.analysis.memory"; //$NON-NLS-1$

    @Override
    protected ITmfStateProvider createStateProvider() {
        return new UstMemoryStateProvider(checkNotNull(getTrace()));
    }

    /**
     * @since 1.0
     */
    @Override
    public boolean setTrace(ITmfTrace trace) throws TmfAnalysisException {
        if (!(trace instanceof LttngUstTrace)) {
            return false;
        }
        return super.setTrace(trace);
    }

    @Override
    protected LttngUstTrace getTrace() {
        return (LttngUstTrace) super.getTrace();
    }

    @Override
    public Iterable<TmfAnalysisRequirement> getAnalysisRequirements() {
        return checkNotNull(Collections.EMPTY_SET);

        // FIXME Analysis requirements are currently only checked before the
        // trace is initialized.
        // Since the check here depends on the tracer version, we cannot know
        // the event names in advance.
        // Below is an example implementation of what should be done once the
        // requirements are checked after initialization.

//        LttngUstTrace trace = this.getTrace();
//        if (trace == null) {
//            Activator.getDefault().logError("UST Memory Analysis : Cannot get requirements of uninitialized trace."); //$NON-NLS-1$
//            return checkNotNull(Collections.EMPTY_SET);
//        }
//        ILttngUstEventLayout layout = trace.getEventLayout();
//        return generateRequirements(layout);
    }
//
//    private static @NonNull Set<TmfAnalysisRequirement> generateRequirements(ILttngUstEventLayout layout) {
//        /* Get the required event names from the layout */
//        final ImmutableSet<String> requiredEvents = ImmutableSet.of(
//                layout.eventLibcMalloc(),
//                layout.eventLibcFree(),
//                layout.eventLibcCalloc(),
//                layout.eventLibcRealloc(),
//                layout.eventLibcMemalign(),
//                layout.eventLibcPosixMemalign()
//                );
//
//        /* Initialize the requirements for the analysis: domain and events */
//        TmfAnalysisRequirement eventsReq = new TmfAnalysisRequirement(SessionConfigStrings.CONFIG_ELEMENT_EVENT, requiredEvents, ValuePriorityLevel.MANDATORY);
//        /*
//         * In order to have these events, the libc wrapper with probes should be
//         * loaded
//         */
//        eventsReq.addInformation(Messages.UstMemoryAnalysisModule_EventsLoadingInformation);
//        eventsReq.addInformation(Messages.UstMemoryAnalysisModule_EventsLoadingExampleInformation);
//
//        /* The domain type of the analysis */
//        TmfAnalysisRequirement domainReq = new TmfAnalysisRequirement(SessionConfigStrings.CONFIG_ELEMENT_DOMAIN);
//        domainReq.addValue(SessionConfigStrings.CONFIG_DOMAIN_TYPE_UST, ValuePriorityLevel.MANDATORY);
//
//        return checkNotNull(ImmutableSet.of(domainReq, eventsReq));
//    }
}
