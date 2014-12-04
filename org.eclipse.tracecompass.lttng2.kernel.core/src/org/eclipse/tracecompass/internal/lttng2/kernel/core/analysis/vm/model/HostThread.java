/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.vm.model;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * This class represents a thread from a specific host. Many machine in an
 * experiment can have the same thread IDs. This class differentiates the thread
 * by adding the hostID it belongs to.
 *
 * @author Geneviève Bastien
 */
public class HostThread {

    @SuppressWarnings("null")
    private static HashFunction hf = Hashing.goodFastHash(32);

    private final String fHost;
    private final Integer fTid;

    /**
     * Constructor
     *
     * @param host
     *            The host this thread belongs to
     * @param tid
     *            The thread ID of this thread
     */
    public HostThread(String host, Integer tid) {
        fHost = host;
        fTid = tid;
    }

    @Override
    public int hashCode() {
        return hf.newHasher(32)
                .putUnencodedChars(fHost)
                .putInt(fTid).hash().asInt();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o instanceof HostThread) {
            HostThread hostTid = (HostThread) o;
            if (fTid.equals(hostTid.fTid) &&
                    fHost.equals(hostTid.fHost)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "HostTid: " + fHost + "," + fTid; //$NON-NLS-1$//$NON-NLS-2$
    }

    /**
     * @return The thread ID of this thread
     */
    public Integer getTid() {
        return fTid;
    }

    /**
     * @return The host ID this thread belongs to
     */
    public String getHost() {
        return fHost;
    }

}