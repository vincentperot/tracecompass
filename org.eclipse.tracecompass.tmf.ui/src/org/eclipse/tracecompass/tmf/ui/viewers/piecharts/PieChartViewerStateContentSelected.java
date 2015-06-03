package org.eclipse.tracecompass.tmf.ui.viewers.piecharts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

/**
 * Implementation of the IPieChartViewerState interface to represent the state
 * of the layout when there is content currently selected.
 *
 * @author ealcaba
 *
 */
public class PieChartViewerStateContentSelected implements IPieChartViewerState {

    /**
     * Default constructor
     */
    public PieChartViewerStateContentSelected() {
    }

    @Override
    public void newSelection(final TmfPieChartViewer context) {
        if (context.isDisposed()) {
            return;
        }

        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (!context.isDisposed()) {
                    context.updateTimeRangeSelectionPieChart();
                    context.getTimeRangePC().redraw();

                    context.layout();
                }
            }
        });
    }

    @Override
    public void newEmptySelection(final TmfPieChartViewer context) {

        if (context.isDisposed()) {
            return;
        }

        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (!context.isDisposed()) {
                    // Have to get rid of the time-range PieChart
                    if (context.getTimeRangePC() != null) {
                        context.getTimeRangePC().dispose();
                        context.setTimeRangePC(null);
                    }

                    // update the global chart so it takes all the place
                    context.getGlobalPC().getLegend().setPosition(SWT.RIGHT);

                    context.layout();
                    context.setCurrentState(new PieChartViewerStateNoContentSelected());
                }
            }
        });
    }

    @Override
    public void newGlobalEntries(final TmfPieChartViewer context) {

        if (context.isDisposed()) {
            return;
        }

        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (!context.isDisposed()) {
                    // Have to get rid of the time-range PieChart
                    if (context.getTimeRangePC() != null) {
                        context.getTimeRangePC().dispose();
                        context.setTimeRangePC(null);
                    }

                    // update the global chart so it takes all the place
                    context.updateGlobalPieChart();
                    context.getGlobalPC().getLegend().setPosition(SWT.RIGHT);

                    context.layout();
                }
            }
        });
        context.setCurrentState(new PieChartViewerStateNoContentSelected());
    }
}
