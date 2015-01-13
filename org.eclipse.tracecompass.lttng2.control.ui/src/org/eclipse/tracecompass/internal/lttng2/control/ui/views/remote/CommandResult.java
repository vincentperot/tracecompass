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
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.remote;

import java.util.Arrays;

/**
 * <p>
 * Class containing command result of remote command execution.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class CommandResult implements ICommandResult {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The result of the command. 0 if successful else > 0
     */
    private int fResult;

    /**
     * The output as String array.
     */
    private String[] fOutput = new String[0];
    private String[] fErrorOutput = new String[0];

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param result
     *            The result of the command
     * @param output
     *            The output, as an array of strings
     * @param errorOutput
     *            THe error output as an array of strings
     */
    public CommandResult(int result, String[] output, String[] errorOutput) {
        fResult = result;
        if (output != null) {
            fOutput = Arrays.copyOf(output, output.length);
        }
        if (errorOutput != null) {
            fErrorOutput = Arrays.copyOf(errorOutput, errorOutput.length);
        }
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public int getResult() {
        return fResult;
    }

    @Override
    public void setResult(int result) {
        fResult = result;
    }

    @Override
    public String[] getOutput() {
        return Arrays.copyOf(fOutput, fOutput.length);
    }

    @Override
    public void setOutput(String[] output) {
        fOutput = new String[0];
        if (output != null) {
            fOutput = Arrays.copyOf(output, output.length);
        }
    }

    @Override
    public String[] getErrorOutput() {
        return Arrays.copyOf(fErrorOutput, fErrorOutput.length);
    }

    @Override
    public void setErrorOutput(String[] output) {
        fErrorOutput = new String[0];
        if (output != null) {
            fErrorOutput = Arrays.copyOf(output, output.length);
        }
    }
}