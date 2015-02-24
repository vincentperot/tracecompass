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
package org.eclipse.tracecompass.tmf.remote.core.shell;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Interface for a command shell implementation
 *
 * @author Bernd Hufmann
 */
public interface ICommandShell {

    /**
     * Method to disconnect the command shell.
     */
    void dispose();

    /**
     * Method to execute a command on the command shell.
     *
     * @param command
     *            The command to executed
     * @param monitor
     *            A progress monitor
     * @return the @link{ICommandResult} instance
     * @throws ExecutionException
     *             If the command fails
     */
    ICommandResult executeCommand(ICommandInput command, @Nullable IProgressMonitor monitor) throws ExecutionException;

    /**
     * Creates a command input instance
     *
     * @return {@link ICommandInput} instance
     *
     */
    ICommandInput createCommand();
}
