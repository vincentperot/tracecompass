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

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
            return new CommandResultStub(0, new String[0], new String[0]);
        }
        return new CommandResultStub(1, new String[0], new String[0]);
    }

    @Override
    public ICommandInput createCommand() {
        return new CommandInputStub();
    }

    /**
     * Command Result Stub
     */
    @NonNullByDefault
    protected class CommandResultStub implements ICommandResult {

        private final int fResult;
        private final List<String> fOutput;
        private final List<String> fErrorOutput;
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
        public CommandResultStub(int result, String[] output, String[] errorOutput) {
            fResult = result;
            fOutput = checkNotNull(Arrays.asList(output));
            fErrorOutput = checkNotNull(Arrays.asList(errorOutput));
        }

        @Override
        public int getResult() {
            return fResult;
        }

        @Override
        public List<String> getOutput() {
            return fOutput;
        }

        @Override
        public List<String> getErrorOutput() {
            return fErrorOutput;
        }

        @Override
        public String toString() {
            StringBuffer ret = new StringBuffer();
            ret.append("Error Output:\n"); //$NON-NLS-1$
            for (String string : fErrorOutput) {
                ret.append(string).append("\n"); //$NON-NLS-1$
            }
            ret.append("Return Value: "); //$NON-NLS-1$
            ret.append(fResult);
            ret.append("\n"); //$NON-NLS-1$
            for (String string : fOutput) {
                ret.append(string).append("\n"); //$NON-NLS-1$
            }
            return nullToEmptyString(ret.toString());
        }
    }

    /**
     * Command Input Stub
     */
    public class CommandInputStub implements ICommandInput {
        private final List<String> fInput = new ArrayList<>();

        @Override
        @NonNull public List<String> getInput() {
            return checkNotNull(fInput);
        }

        @Override
        public void add(@Nullable String segment) {
            if (segment != null) {
                fInput.add(segment);
            }
        }

        @Override
        public void addAll(@Nullable List<String> segments) {
            if (segments != null) {
                fInput.addAll(segments);
            }
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (String segment : fInput) {
                builder.append(segment).append(' ');
            }
            return nullToEmptyString(builder.toString().trim());
        }
    }
}