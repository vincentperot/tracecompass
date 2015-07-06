/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexis Cabana-Loriaux <alex021994@gmail.com> - Initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers.piecharts;

/**
 * Interface used to take control of a {@link TmfPieChartViewer} as part of the
 * State design pattern. Thus it is closely related with the TmfPieChartViewer
 *
 * @author Alexis Cabana-Loriaux <alex021994@gmail.com>
 * @since 2.0
 *
 */
public interface IPieChartViewerState {

    /**
     * Mutex blocking two UIThread Jobs from modifying the layout in the same
     * time
     */
    public static final Object mutexLayoutChange = new Object();

    /**
     * To be called when the current selection has changed
     *
     * @param context
     *            The context in which to apply the changes
     */
    public void newSelection(final TmfPieChartViewer context);

    /**
     * To be called when the current selection changes to "empty"
     *
     * @param context
     *            The context in which to apply the changes
     */
    public void newEmptySelection(final TmfPieChartViewer context);

    /**
     * To be called when there are new global entries to show
     *
     * @param context
     *            The context in which to apply the changes
     */
    public void newGlobalEntries(final TmfPieChartViewer context);
}
