/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexis Cabana-Loriaux - Initial API and Implementation
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.contextswitch;

import org.eclipse.osgi.util.NLS;

/**
 * Messages file for statistics view strings.
 *
 * @author Alexis Cabana-Loriaux
 * @since 1.0
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.analysis.os.linux.core.contextswitch.messages"; //$NON-NLS-1$

    /**
     * String for the default model name
     */
    public static String TmfContextSwitchView_DefaultModelName;

    /**
     * Default name of the jobs querying the Statesystem
     */
    public static String TmfContextSwitchView_DefaultModelUpdateJobName;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
