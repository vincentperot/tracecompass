/**********************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.core.model;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Implementation of a null session info to be used instead of null reference.
 *
 * @author Bernd Hufmann
 */
public final class NullSessionInfo implements ISessionInfo {

    /** Null session info instance */
    public static final @NonNull NullSessionInfo INSTANCE = new NullSessionInfo();

    private NullSessionInfo() {}

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setName(String name) {
    }

    @Override
    public TraceSessionState getSessionState() {
        return null;
    }

    @Override
    public void setSessionState(TraceSessionState state) {
    }

    @Override
    public void setSessionState(String stateName) {
    }

    @Override
    public String getSessionPath() {
        return null;
    }

    @Override
    public void setSessionPath(String path) {
    }

    @Override
    public IDomainInfo[] getDomains() {
        return null;
    }

    @Override
    public void setDomains(List<IDomainInfo> domains) {
    }

    @Override
    public void addDomain(IDomainInfo domainInfo) {
    }

    @Override
    public boolean isStreamedTrace() {
        return false;
    }

    @Override
    public void setStreamedTrace(boolean isStreamedTrace) {
    }

    @Override
    public boolean isSnapshotSession() {
        return false;
    }

    @Override
    public void setSnapshot(boolean isSnapshot) {
    }

    @Override
    public ISnapshotInfo getSnapshotInfo() {
        return null;
    }

    @Override
    public void setSnapshotInfo(ISnapshotInfo setSnapshotInfo) {
    }

    @Override
    public boolean isLive() {
        return false;
    }

    @Override
    public void setLive(boolean isLive) {
    }

    @Override
    public long getLiveDelay() {
        return 0;
    }

    @Override
    public void setLiveDelay(long liveDelay) {
    }

    @Override
    public String getNetworkUrl() {
        return null;
    }

    @Override
    public void setNetworkUrl(String networkUrl) {
    }

    @Override
    public String getControlUrl() {
        return null;
    }

    @Override
    public void setControlUrl(String controlUrl) {
    }

    @Override
    public String getDataUrl() {
        return null;
    }

    @Override
    public void setDataUrl(String datalUrl) {
    }

    @Override
    public String getLiveUrl() {
        return null;
    }

    @Override
    public void setLiveUrl(String liveUrl) {
    }

    @Override
    public Integer getLivePort() {
        return null;
    }

    @Override
    public void setLivePort(Integer livePort) {
    }

}
