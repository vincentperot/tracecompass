/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.remote;

/**
 * <p>
 * Interface for providing command execution result.
 * </p>
 *
 * @author Bernd Hufmann
 */
public interface ICommandResult {
    /**
     * The result of the command.
     *
     * @return 0 if successful else >0
     */
    int getResult();

    /**
     * Sets the command result value.
     *
     * @param result
     *            The integer result to set
     */
    void setResult(int result);

    /**
     * @return returns the command output.
     */
    String[] getOutput();

    /**
     * Sets the command output.
     *
     * @param output
     *            The output (as an array of Strings) to assign
     */
    void setOutput(String[] output);

    /**
     * The error output of the command.
     *
     * @return returns the command error output.
     */
    String[] getErrorOutput();

    /**
     * Sets the command output.
     *
     * @param output
     *            The output (as an array of Strings) to assign
     */
    void setErrorOutput(String[] output);
}