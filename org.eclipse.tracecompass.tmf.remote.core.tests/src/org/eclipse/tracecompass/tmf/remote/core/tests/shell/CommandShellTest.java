/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.remote.core.tests.shell;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.junit.Assume.assumeTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.tracecompass.internal.tmf.remote.core.shell.CommandShell;
import org.eclipse.tracecompass.tmf.remote.core.proxy.RemoteSystemProxy;
import org.eclipse.tracecompass.tmf.remote.core.proxy.TmfRemoteConnectionFactory;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandInput;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandResult;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandShell;
import org.junit.Test;

/**
 * Test suite for the {@link CommandShell} class
 */
public class CommandShellTest {

    private static final boolean IS_LINUX = System.getProperty("os.name").contains("Linux") ? true : false; //$NON-NLS-1$ //$NON-NLS-2$

    private static final @NonNull String[] CMD_INPUT_LINUX = { "ls", "-l" };
    private static final @NonNull String[] CMD_ERROR_INPUT_LINUX = { "ls", "blablablabla" };
    private static final @NonNull String[] CMD_UNKNOWN_COMMAND_LINUX = { "blablablabla" };

    private static final IRemoteConnection LOCAL_CONNECTION = TmfRemoteConnectionFactory.getLocalConnection();
    private static final RemoteSystemProxy LOCAL_PROXY = new RemoteSystemProxy(checkNotNull(LOCAL_CONNECTION));

    /**
     * Test suite for the {@link CommandShell#executeCommand} method
     * @throws ExecutionException
     *            in case of an error
     * @throws RemoteConnectionException
     *            in case of an error during creation of a shell
     */
    @Test
    public void testExecuteSuccess() throws ExecutionException, RemoteConnectionException {
        assumeTrue(IS_LINUX);
        LOCAL_PROXY.connect(new NullProgressMonitor());
        ICommandShell shell = TmfRemoteConnectionFactory.createCommandShell(LOCAL_PROXY.getRemoteConnection());

        ICommandInput command = shell.createCommand();
        command.addAll(checkNotNull(Arrays.asList(CMD_INPUT_LINUX)));
        ICommandResult result = shell.executeCommand(command, new NullProgressMonitor());
        assertEquals(0, result.getResult());
    }

    /**
     * Test suite for the {@link CommandShell#executeCommand} method (non-null result value)
     * @throws ExecutionException
     *            in case of an error
     * @throws RemoteConnectionException
     *            in case of an error during creation of a shell
     */
    @Test
    public void testExecuteError() throws ExecutionException, RemoteConnectionException {
        assumeTrue(IS_LINUX);

        LOCAL_PROXY.connect(new NullProgressMonitor());
        ICommandShell shell = TmfRemoteConnectionFactory.createCommandShell(LOCAL_PROXY.getRemoteConnection());

        ICommandInput command = shell.createCommand();
        command.addAll(checkNotNull(Arrays.asList(CMD_ERROR_INPUT_LINUX)));
        ICommandResult result = shell.executeCommand(command, new NullProgressMonitor());
        assertTrue(result.getResult() > 0);
    }

    /**
     * Test suite for the {@link CommandShell#executeCommand} method (with exception)
     * @throws ExecutionException
     *            in case of an error
     * @throws RemoteConnectionException
     *            in case of an error during creation of a shell
     */
    @Test (expected=ExecutionException.class)
    public void testExecuteException() throws ExecutionException, RemoteConnectionException {
        if (!IS_LINUX) {
            throw new ExecutionException("");
        }
        LOCAL_PROXY.connect(new NullProgressMonitor());
        ICommandShell shell = TmfRemoteConnectionFactory.createCommandShell(LOCAL_PROXY.getRemoteConnection());

        ICommandInput command = shell.createCommand();
        command.addAll(checkNotNull(Arrays.asList(CMD_UNKNOWN_COMMAND_LINUX)));
        ICommandResult result = shell.executeCommand(command, new NullProgressMonitor());
        assertTrue(result.getResult() > 0);
    }
}