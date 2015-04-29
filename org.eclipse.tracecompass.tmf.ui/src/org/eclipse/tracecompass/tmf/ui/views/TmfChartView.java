/**********************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.tmf.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.signal.TmfTimeViewAlignmentInfo;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;

/**
 * Base class to be used with a chart viewer {@link TmfXYChartViewer}.
 * It is responsible to instantiate the viewer class and load the trace
 * into the viewer when the view is created.
 *
 * @author Bernd Hufmann
 */
public abstract class TmfChartView extends TmfView implements ITmfTimeAligned {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /** The TMF XY Chart reference */
    private TmfXYChartViewer fChartViewer;
    /** The Trace reference */
    private ITmfTrace fTrace;
    private Composite fChartContainer;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Standard Constructor
     *
     * @param viewName
     *            The view name
     */
    public TmfChartView(String viewName) {
        super(viewName);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * Returns the TMF XY chart viewer implementation.
     *
     * @return the TMF XY chart viewer {@link TmfXYChartViewer}
     */
    protected TmfXYChartViewer getChartViewer() {
        return fChartViewer;
    }

    /**
     * Create the TMF XY chart viewer implementation
     *
     * @param parent
     *            the parent control
     *
     * @return The TMF XY chart viewer {@link TmfXYChartViewer}
     * @since 1.0
     */
    abstract protected TmfXYChartViewer createChartViewer(Composite parent);

    /**
     * Returns the ITmfTrace implementation
     *
     * @return the ITmfTrace implementation {@link ITmfTrace}
     */
    protected ITmfTrace getTrace() {
        return fTrace;
    }

    /**
     * Sets the ITmfTrace implementation
     *
     * @param trace
     *            The ITmfTrace implementation {@link ITmfTrace}
     */
    protected void setTrace(ITmfTrace trace) {
        fTrace = trace;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        fChartContainer = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        fChartContainer.setLayout(layout);
        fChartViewer = createChartViewer(fChartContainer);
        fChartViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            setTrace(trace);
            loadTrace();
        }
    }

    @Override
    public void dispose() {
        if (fChartViewer != null) {
            fChartViewer.dispose();
        }
    }

    /**
     * Load the trace into view.
     */
    protected void loadTrace() {
        if (fChartViewer != null) {
            fChartViewer.loadTrace(fTrace);
        }
    }

    /**
     * @since 1.0
     */
    @Override
    public TmfTimeViewAlignmentInfo getTimeViewAlignmentInfo() {
        if (fChartViewer == null) {
            return null;
        }

        // There is no movable sash in this view so set the timeAxisOffset so
        // that it is not considered to be used as a reference for other views
        // to align on.
        return new TmfTimeViewAlignmentInfo(fChartViewer.getControl().getShell(), fChartViewer.getControl().toControl(0, 0), 0);
    }

    /**
     * @since 1.0
     */
    @Override
    public int getAvailableWidth(int requestedOffset) {
        if (fChartViewer == null) {
            return 0;
        }

        int pointAreaWidth = fChartViewer.getPointAreaWidth();
        int endOffset = fChartViewer.getPointAreaOffset() + pointAreaWidth;
        // TODO this is just an approximation that assumes that the end will be at the same position but that can change for a different data range/scaling
        int availableWidth = endOffset - requestedOffset;
        return availableWidth;
    }

    /**
     * @since 1.0
     */
    @Override
    public void performAlign(int offset, int width) {
        GridLayout layout = (GridLayout) fChartContainer.getLayout();
        int axisOffset = offset - fChartViewer.getPointAreaOffset();
        layout.marginLeft = Math.min(fChartContainer.getBounds().width, Math.max(0, axisOffset));
        fChartContainer.layout();
    }
}
