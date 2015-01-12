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
package org.eclipse.tracecompass.internal.lttng2.control.stubs.shells;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.remote.CommandResult;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.remote.ICommandResult;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.remote.ICommandShell;

/**
 * Command shell stub
 */
public class TestCommandShell implements ICommandShell {

    /** If the shell is connected */
    protected boolean fIsConnected = false;

    @Override
    public void connect() throws ExecutionException {
        fIsConnected = true;
    }

    @Override
    public void disconnect() {
        fIsConnected = false;
    }

    @Override
    public ICommandResult executeCommand(List<String> command, IProgressMonitor monitor) throws ExecutionException {
        if (fIsConnected) {
            return new CommandResult(0, new String[0], new String[0]);
        }
        return new CommandResult(1, new String[0], new String[0]);
    }
}
