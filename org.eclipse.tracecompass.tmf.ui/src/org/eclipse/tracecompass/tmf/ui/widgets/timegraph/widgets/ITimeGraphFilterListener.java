/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets;

import java.util.List;

/**
 * Interface to implement to listen to a time graph entry filter change.
 *
 * @since 3.2
 */
public interface ITimeGraphFilterListener {

    /**
     * The list of filtered items have been modified. The filtered items are the
     * ones meant to be displayed.
     *
     * @param fFilteredObjects
     *            The list of filtered items. Of <code>null</code> if all items
     *            should be displayed.
     */
    void filterChanged(List<Object> fFilteredObjects);

}
