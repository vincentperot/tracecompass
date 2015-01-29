/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.ui.views.latency.model;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.views.distribution.model.IBaseDistributionModel;
import org.eclipse.tracecompass.tmf.ui.views.histogram.IHistogramDataModel;

/**
 * <b><u>LatencyController</u></b>
 * <p>
 */
public class LatencyController {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private static LatencyController fInstance = null;

    private LatencyEventRequest fEventRequest;

    private ITmfTrace fProvider;

    private final ListenerList fModels;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    private LatencyController() {
        fModels = new ListenerList();
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    public static LatencyController getInstance() {
        if (fInstance == null) {
            fInstance = new LatencyController();
        }
        return fInstance;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Refresh all registered models
     *
     * @param provider - TmfEventProvider to request data from
     * @param timeRange - time range of request
     */
    public void refreshModels(ITmfTrace provider, TmfTimeRange timeRange) {
        // save provider
        fProvider = provider;
        if (fProvider != null) {
            if (fEventRequest != null && !fEventRequest.isCompleted()) {
                fEventRequest.cancel();
            }
            clear();
            fEventRequest = new LatencyEventRequest(this, timeRange, 0, ITmfEventRequest.ALL_DATA, ITmfEventRequest.ExecutionType.FOREGROUND);
            fProvider.sendRequest(fEventRequest);
        }
    }

    /**
     * Refreshes registered models by re-sending previous request to saved provider
     */
    public void refreshModels() {
        if (fProvider != null && fEventRequest != null) {
            refreshModels(fProvider, fEventRequest.getRange());
        }
    }

    /**
     * Clear all models
     */
    public void clear() {
        Object models[] = fModels.getListeners();

        for (int i = 0; i < models.length; i++) {
            ((IBaseDistributionModel)models[i]).clear();
        }
    }

    /**
     * Dispose of controller
     */
    public void dispose() {
        if (fEventRequest != null && !fEventRequest.isCompleted()) {
            fEventRequest.cancel();
        }
        fProvider = null;
    }

    /**
     * Register given model.
     * @param model - model to register
     */
    public void registerModel(IBaseDistributionModel model) {
        fModels.add(model);
    }

    /**
     * Deregister given model.
     *
     * @param model - model to deregister
     */
    public void deregisterModel(IBaseDistributionModel model) {
        fModels.remove(model);
    }

    /**
     * Handle data of event request and pass it information to the registered models
     *
     * @param eventCount - event count
     * @param timestamp - start timestamp of latency calculation
     * @param latency - latency value (startTimestamp - endTimestamp)
     */
    public void handleData(int eventCount, long timestamp, long latency, ITmfTrace trace) {
        Object[] models = fModels.getListeners();
        for (int i = 0; i < models.length; i++) {
            IBaseDistributionModel model = (IBaseDistributionModel)models[i];
            if (model instanceof IHistogramDataModel) {
                ((IHistogramDataModel)model).countEvent(eventCount, latency, trace);
            } else if (model instanceof IGraphDataModel) {
                ((IGraphDataModel)model).countEvent(eventCount, timestamp, latency);
            }
        }
    }

    /**
     * Handle complete indication from request.
     */
    public void handleCompleted() {
        Object[] models = fModels.getListeners();
        for (int i = 0; i < models.length; i++) {
            ((IBaseDistributionModel)models[i]).complete();
        }
    }

    /**
     * Handle cancel indication from request.
     */
    public void handleCancel() {
        clear();
    }

    /**
     * Set event provider for refresh.
     *
     * @param provider
     */
    public void setEventProvider(ITmfTrace provider) {
        fProvider = provider;
    }

    /**
     * Set current event time in model(s).
     *
     * @param timestamp
     */
    public void setCurrentEventTime(long timestamp) {
        Object[] models = fModels.getListeners();
        for (int i = 0; i < models.length; i++) {
            IBaseDistributionModel model = (IBaseDistributionModel)models[i];
            if (model instanceof LatencyGraphModel) {
                ((LatencyGraphModel)model).setCurrentEventNotifyListeners(timestamp);
            }
        }
    }
}
