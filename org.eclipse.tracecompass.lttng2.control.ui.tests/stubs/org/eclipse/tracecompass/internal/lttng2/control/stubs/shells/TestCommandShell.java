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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.tracecompass.internal.tmf.remote.core.shell.CommandInput;
import org.eclipse.tracecompass.internal.tmf.remote.core.shell.CommandResult;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandInput;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandResult;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandShell;

/**
 * Command shell stub
 */
public class TestCommandShell implements ICommandShell {

    /** If the shell is connected */
    protected boolean fIsConnected = false;

    @Override
    public void dispose() {
        fIsConnected = false;
    }

    @Override
    public ICommandResult executeCommand(ICommandInput command, IProgressMonitor monitor) throws ExecutionException {
        if (fIsConnected) {
            return new CommandResult(0, new String[0], new String[0]);
        }
        return new CommandResult(1, new String[0], new String[0]);
    }

    @Override
    public ICommandInput createCommand() {
        return new CommandInput();
    }
}
