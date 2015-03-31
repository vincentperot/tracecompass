/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout;

/**
 * This file defines all the known event and field names for LTTng kernel
 * traces, for versions of lttng-modules 2.8 and above.
 *
 * @author Sebastien Lorrain
 */
public class Lttng28EventLayout extends LttngEventLayout {

    private Lttng28EventLayout() {}

    private static final Lttng28EventLayout INSTANCE = new Lttng28EventLayout();

    private static final String PARENT_NS_INUM = "parent_ns_inum";
    private static final String CHILD_VTIDS = "vtids";
    private static final String CHILD_NS_INUM = "child_ns_inum";
    private static final String NS_INUM = "ns_inum";
    private static final String VTID = "vtid";
    private static final String PPID = "ppid";
    private static final String NS_LEVEL = "ns_level";
    private static final String STATUS = "status";


    public static Lttng28EventLayout getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------
    // New event names in these versions
    // ------------------------------------------------------------------------

    @Override
    public String eventSyscallEntryPrefix() {
        return "syscall_entry_"; //$NON-NLS-1$
    }

    @Override
    public String eventCompatSyscallEntryPrefix() {
        return "compat_syscall_entry_"; //$NON-NLS-1$
    }

    @Override
    public String eventSyscallExitPrefix() {
        return "syscall_exit_"; //$NON-NLS-1$
    }

    public String fieldParentNSInum() {
        return PARENT_NS_INUM;
    }

    public String fieldChildNSInum() {
        return CHILD_NS_INUM;
    }

    public String fieldChildVTids() {
        return CHILD_VTIDS;
    }

    public String fieldNSInum() {
        return NS_INUM;
    }

    public String fieldVTid() {
        return VTID;
    }

    public String fieldPPid() {
        return PPID;
    }

    public String fieldNSLevel() {
        return NS_LEVEL;
    }

    public String fieldStatus() {
        return STATUS;
    }
}
