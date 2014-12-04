/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mohamad Gebai - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.model.qemukvm;

/**
 * Lttng specific strings used by this plugin
 *
 * @author Mohamad Gebai
 */
@SuppressWarnings({"javadoc","nls"})
public interface QemuKvmStrings {

    /* vmsync events */
    static final String VMSYNC_GH_HOST = "vmsync_gh_host";
    static final String VMSYNC_HG_HOST = "vmsync_hg_host";
    static final String VMSYNC_GH_GUEST = "vmsync_gh_guest";
    static final String VMSYNC_HG_GUEST = "vmsync_hg_guest";
    static final String COUNTER_PAYLOAD = "cnt";
    static final String VM_UID_PAYLOAD = "vm_uid";

    /* kvm entry/exit events */
    static final String KVM_ENTRY = "kvm_entry";
    static final String KVM_EXIT = "kvm_exit";
    static final String VCPU_ID = "vcpu_id";

}
