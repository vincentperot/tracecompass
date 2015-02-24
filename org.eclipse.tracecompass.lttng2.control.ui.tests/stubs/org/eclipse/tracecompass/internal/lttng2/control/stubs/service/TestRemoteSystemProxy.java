/**********************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Markus Schorn - Bug 448058: Use org.eclipse.remote in favor of RSE
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.stubs.service;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.remote.core.IRemoteFileService;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.lttng2.control.stubs.shells.LTTngToolsFileShell;
import org.eclipse.tracecompass.tmf.remote.core.proxy.RemoteSystemProxy;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandShell;

@SuppressWarnings("javadoc")
public class TestRemoteSystemProxy extends RemoteSystemProxy {

    public TestRemoteSystemProxy(IRemoteConnection host) {
        super(NonNullUtils.checkNotNull(host));
    }

    private LTTngToolsFileShell fShell = null;
    private String fTestFile = null;
    private String fScenario = null;

    @Override
    public IRemoteProcessBuilder getProcessBuilder(String... command) {
        return null;
    }

    @Override
    public IRemoteFileService getRemoteFileService() {
        return null;
    }

    @Override
    public void connect(IProgressMonitor monitor) throws ExecutionException {
    }

    @Override
    public void disconnect() {
        fShell = null;
    }

    @Override
    public void dispose() {
    }

    @Override
    public ICommandShell createCommandShell() {
        LTTngToolsFileShell shell = fShell;
        if (shell == null) {
            shell = new LTTngToolsFileShell();
            if ((fTestFile != null) && (fScenario != null)) {
                shell.loadScenarioFile(fTestFile);
                shell.setScenario(fScenario);
            }
            fShell = shell;
        }
        return shell;
    }

    @Override
    public void addConnectionChangeListener(IRemoteConnectionChangeListener listener) {
    }

    @Override
    public void removeConnectionChangeListener(IRemoteConnectionChangeListener listener) {
    }

    public void setTestFile(String testFile) {
        fTestFile = testFile;
    }

    public void setScenario(String scenario) {
        fScenario = scenario;
        if (fShell != null) {
            fShell.setScenario(fScenario);
        }
    }

    @Override
    public boolean isConnected() {
        return true;
    }
}
