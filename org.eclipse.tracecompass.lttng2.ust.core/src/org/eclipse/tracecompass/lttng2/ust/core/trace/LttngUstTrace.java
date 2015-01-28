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

import java.nio.BufferOverflowException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tracecompass.internal.lttng2.ust.core.Activator;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.trace.TraceValidationStatus;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;

/**
 * Class to contain LTTng-UST traces
 *
 * @author Matthew Khouzam
 * @since 2.1
 */
public class LttngUstTrace extends CtfTmfTrace {

    private static final int CONFIDENCE = 100;

    /**
     * Default constructor
     */
    public LttngUstTrace() {
        super();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation sets the confidence to 100 if the trace is a valid
     * CTF trace in the "ust" domain.
     */
    @Override
    public IStatus validate(final IProject project, final String path) {
        try (CtfTmfTrace temp = new CtfTmfTrace();) {
            temp.initTrace((IResource) null, path, CtfTmfEvent.class);

            /* Make sure the domain is "ust" in the trace's env vars */
            String dom = temp.getEnvironment().get("domain"); //$NON-NLS-1$
            if (dom != null && dom.equals("\"ust\"")) { //$NON-NLS-1$
                return new TraceValidationStatus(CONFIDENCE, Activator.PLUGIN_ID);
            }
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.LttngUstTrace_DomainError);

        } catch (TmfTraceException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.toString(), e);
        } catch (NullPointerException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.toString(), e);
        } catch (final BufferOverflowException e) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.LttngUstTrace_TraceReadError + ": " + Messages.LttngUstTrace_MalformedTrace); //$NON-NLS-1$
        }
    }
}
