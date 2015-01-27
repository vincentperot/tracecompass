/*******************************************************************************
 * Copyright (c) 2015 Ericsson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.editors;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;

/**
 * Constants for the TMF UI components
 *
 * @noimplement
 */
public interface ITmfUIConstants {
    /**
     * The editor input type persistent property of a trace resource. Use to
     * differentiate between traces and experiments.
     */
    QualifiedName EDITOR_INPUT_TYPE = TmfCommonConstants.TRACETYPE;

    /**
     * The trace editor input type.
     */
    String TRACE_EDITOR_INPUT_TYPE = "editorInputType.trace"; //$NON-NLS-1$

    /**
     * The trace editor input type.
     */
    String EXPERIMENT_EDITOR_INPUT_TYPE = "editorInputType.experiment"; //$NON-NLS-1$
}
