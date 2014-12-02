/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model;

/**
 * Extend ITimeEvent interface
 *
 * @author Patrick Tasse
 * @since 2.1
 */
public interface ITimeEvent2 extends ITimeEvent {

    /**
     * Split an event in two at the specified time and keep the part before the
     * split. If the time is smaller or equal to the event's start, the returned
     * event is null.
     *
     * @param time
     *            the time at which the event is to be split
     * @return The part before the split time
     */
    ITimeEvent splitBefore(long time);

    /**
     * Split an event in two at the specified time and keep the part after the
     * split. If the time is greater or equal to the event's end, the returned
     * event is null.
     *
     * @param time
     *            the time at which the event is to be split
     * @return The part after the split time
     */
    ITimeEvent splitAfter(long time);
}
