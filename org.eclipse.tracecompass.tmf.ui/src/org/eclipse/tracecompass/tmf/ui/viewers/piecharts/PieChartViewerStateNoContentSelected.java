package org.eclipse.tracecompass.tmf.ui.viewers.piecharts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

/**
 * Implementation of the IPieChartViewerState interface to represent the state
 * of the layout when there is no content currently selected.
 *
 * @author ealcaba
 *
 */
public class PieChartViewerStateNoContentSelected implements IPieChartViewerState {

    /**
     * Default constructor
     */
    public PieChartViewerStateNoContentSelected() {
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
                    // set the layout for the Time-range PieChart
                    context.getTimeRangePC().setParent(context);
                    context.getTimeRangePC().getLegend().setPosition(SWT.BOTTOM);

                    // set the layout for the Global Pichart
                    context.getGlobalPC().getLegend().setPosition(SWT.BOTTOM);
                    context.layout();

                    context.setCurrentState(new PieChartViewerStateContentSelected());
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
                if (context.isDisposed()) {
                    return;
                }
                if (context.getTimeRangePC() != null && !context.getTimeRangePC().isDisposed()) {
                    context.getTimeRangePC().dispose();
                    context.setTimeRangePC(null);
                }
                context.layout();
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
                    if (context.getTimeRangePC() != null && context.getTimeRangePC().isDisposed()) {
                        context.getTimeRangePC().dispose();
                        context.setTimeRangePC(null);
                    }
                    context.updateGlobalPieChart();
                    context.getGlobalPC().getLegend().setPosition(SWT.RIGHT);
                    context.getGlobalPC().redraw();
                }
            }
        });
    }
}
