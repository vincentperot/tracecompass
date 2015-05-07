/*******************************************************************************
 * Copyright (c) 2015 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Sebastien Lorrain - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout;

/**
 * This file defines all the known event and field names for LTTng kernel
 * traces, for versions of lttng-modules 2.7 and above.
 *
 * @author Sebastien Lorrain
 */
@SuppressWarnings("javadoc")
public class Lttng27EventLayout extends Lttng26EventLayout {

    private Lttng27EventLayout() {}

    public static final Lttng27EventLayout INSTANCE = new Lttng27EventLayout();

    // ------------------------------------------------------------------------
    // New event names in these versions
    // ------------------------------------------------------------------------

    public String fieldParentNSInum() {
        return "parent_ns_inum"; //$NON-NLS-1$
    }

    public String fieldChildNSInum() {
        return "child_ns_inum"; //$NON-NLS-1$
    }

    public String fieldChildVTids() {
        return "vtids"; //$NON-NLS-1$
    }

    public String fieldNSInum() {
        return "ns_inum"; //$NON-NLS-1$
    }

    public String fieldVTid() {
        return "vtid"; //$NON-NLS-1$
    }

    public String fieldPPid() {
        return "ppid"; //$NON-NLS-1$
    }

    public String fieldNSLevel() {
        return "ns_level"; //$NON-NLS-1$
    }

    public String fieldStatus() {
        return "status"; //$NON-NLS-1$
    }
}
