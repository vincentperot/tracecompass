/**********************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Alexandre Montplaisir - Add UST callstack state system
 *   Marc-Andre Laperle - Handle BufferOverflowException (Bug 420203)
 **********************************************************************/

package org.eclipse.tracecompass.lttng2.ust.core.trace;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.lttng2.ust.core.Activator;
import org.eclipse.tracecompass.internal.lttng2.ust.core.trace.layout.LttngUst20EventLayout;
import org.eclipse.tracecompass.internal.lttng2.ust.core.trace.layout.LttngUst27EventLayout;
import org.eclipse.tracecompass.lttng2.ust.core.trace.layout.ILttngUstEventLayout;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.trace.TraceValidationStatus;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTraceValidationStatus;

/**
 * Class to contain LTTng-UST traces
 *
 * @author Matthew Khouzam
 */
public class LttngUstTrace extends CtfTmfTrace {

    private static final int CONFIDENCE = 100;

    private @Nullable ILttngUstEventLayout fLayout = null;

    /**
     * Default constructor
     */
    public LttngUstTrace() {
        super();
    }

    /**
     * Get the event layout to use with this trace. This normally depends on the
     * tracer's version.
     *
     * @return The event layout
     * @since 1.1
     */
    public @NonNull ILttngUstEventLayout getEventLayout() {
        ILttngUstEventLayout layout = fLayout;
        if (layout == null) {
            throw new IllegalStateException("Cannot get the layout of a non-initialized trace!"); //$NON-NLS-1$
        }
        return layout;
    }

    @Override
    public void initTrace(IResource resource, String path,
            Class<? extends ITmfEvent> eventType) throws TmfTraceException {
        super.initTrace(resource, path, eventType);

        /* Determine the event layout to use from the tracer's version */
        fLayout = getLayoutFromEnv(this.getEnvironment());
    }

    private static @NonNull ILttngUstEventLayout getLayoutFromEnv(Map<String, String> traceEnv) {
        String tracerName = traceEnv.get("tracer_name"); //$NON-NLS-1$
        String tracerMajor = traceEnv.get("tracer_major"); //$NON-NLS-1$
        String tracerMinor = traceEnv.get("tracer_minor"); //$NON-NLS-1$

        if ("\"lttng-ust\"".equals(tracerName) && tracerMajor != null && tracerMinor != null) { //$NON-NLS-1$
            if (Integer.valueOf(tracerMajor) >= 2) {
                if (Integer.valueOf(tracerMinor) >= 7) {
                    return LttngUst27EventLayout.getInstance();
                }
                return LttngUst20EventLayout.getInstance();
            }
        }

        /* Fallback to the UST 2.0 layout and hope for the best */
        return LttngUst20EventLayout.getInstance();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation sets the confidence to 100 if the trace is a valid
     * CTF trace in the "ust" domain.
     */
    @Override
    public IStatus validate(final IProject project, final String path) {
        IStatus status = super.validate(project, path);
        if (status instanceof CtfTraceValidationStatus) {
            Map<String, String> environment = ((CtfTraceValidationStatus) status).getEnvironment();
            /* Make sure the domain is "ust" in the trace's env vars */
            String domain = environment.get("domain"); //$NON-NLS-1$
            if (domain == null || !domain.equals("\"ust\"")) { //$NON-NLS-1$
                return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.LttngUstTrace_DomainError);
            }
            return new TraceValidationStatus(CONFIDENCE, Activator.PLUGIN_ID);
        }
        return status;
    }
}
