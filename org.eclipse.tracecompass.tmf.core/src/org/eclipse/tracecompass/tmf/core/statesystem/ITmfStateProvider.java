/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.statesystem;

import org.eclipse.tracecompass.statesystem.core.IStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * This is the interface used to define the "state change input", which is the
 * main type of input that goes in the state system.
 *
 * Usually a state change input, also called "state provider" is the piece of
 * the pipeline which converts trace events to state changes.
 *
 * @author Alexandre Montplaisir
 */
public interface ITmfStateProvider extends IStateProvider {


    @Override
    ITmfStateProvider getNewInstance();

    /**
     * Get the trace with which this state input plugin is associated.
     *
     * @return The associated trace
     */
    @Override
    ITmfTrace getTrace();
}
