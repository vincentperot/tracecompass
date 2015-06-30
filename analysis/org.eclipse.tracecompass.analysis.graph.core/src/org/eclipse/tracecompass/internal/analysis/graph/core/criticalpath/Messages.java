/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.graph.core.criticalpath;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

/**
 * Externalized string for this package
 *
 * @author Geneviève Bastien
 */
@SuppressWarnings("javadoc")
public class Messages {
	private static final String BUNDLE_NAME = "org.eclipse.tracecompass.analysis.graph.core.criticalpath.messages"; //$NON-NLS-1$

    public static @Nullable String CriticalPathModule_waitingForGraph;
    public static @Nullable String CriticalPathModule_fullHelpText;
    public static @Nullable String CriticalPathModule_cantExecute;

	static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }

}
