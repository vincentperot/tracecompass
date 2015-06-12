/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets;

/**
 * Extension to the time data provider interface.
 *
 * @since 1.0
 */
public interface ITimeDataProvider2 extends ITimeDataProvider {

    /**
     * Updates the time range and optionally notify registered listeners. This
     * should be called if and only if the change is triggered by the widget.
     *
     * @param time0
     *            the time range start time
     * @param time1
     *            the time range end time
     * @param doNotify
     *            true to notify listeners
     */
    void setStartFinishTimeInternal(long time0, long time1, boolean doNotify);

}
