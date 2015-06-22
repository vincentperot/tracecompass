/*******************************************************************************
 * Copyright (c) 2015 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam, Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.core;

/**
 * Strings related to UST traces and convenience libraries.
 *
 * @author Matthew Khouzam
 * @author Geneviève Bastien
 * @noimplement This interface only contains static definitions.
 */
@SuppressWarnings("nls")
public interface LttngUstStrings {

    /** Memory state system attribute name */
    String UST_MEMORY_MEMORY_ATTRIBUTE = "Memory";

    /** Procname state system attribute name */
    String UST_MEMORY_PROCNAME_ATTRIBUTE = "Procname";

    /** Name of the attribute to store memory usage of events with no context */
    String OTHERS = "Others";
}
