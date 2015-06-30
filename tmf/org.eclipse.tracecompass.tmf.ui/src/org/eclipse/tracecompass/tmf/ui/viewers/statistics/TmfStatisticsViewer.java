/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Initial API and implementation
 *   Alexandre Montplaisir - Port to ITmfStatistics provider
 *   Patrick Tasse - Support selection range
 *   Bernd Hufmann - Fix range selection updates
 *   Alexis Cabana-Loriaux <alex021994@gmail.com> - Extraction of the model to TmfStatisticsModel
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers.statistics;

import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.tracecompass.tmf.core.component.TmfComponent;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.ui.TmfUiRefreshHandler;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.piecharts.TmfPieChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.statistics.model.TmfBaseColumnData;
import org.eclipse.tracecompass.tmf.ui.viewers.statistics.model.TmfBaseColumnDataProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.statistics.model.TmfStatisticsFormatter;
import org.eclipse.tracecompass.tmf.ui.viewers.statistics.model.TmfStatisticsTreeManager;
import org.eclipse.tracecompass.tmf.ui.viewers.statistics.model.TmfStatisticsTreeNode;
import org.eclipse.tracecompass.tmf.ui.viewers.statistics.model.TmfTreeContentProvider;
import org.eclipse.tracecompass.tmf.ui.views.statistics.TmfStatisticsModel;

/**
 * A basic viewer to display statistics in the statistics view.
 *
 * @author Mathieu Denis
 */
public class TmfStatisticsViewer extends TmfViewer {

    private final class EraseListener implements Listener {
        private final List<TmfBaseColumnData> fColumnDataList;

        private EraseListener(List<TmfBaseColumnData> columnDataList) {
            fColumnDataList = columnDataList;
        }

        @Override
        public void handleEvent(Event event) {
            if (fColumnDataList.get(event.index).getPercentageProvider() != null) {

                TmfStatisticsTreeNode node = (TmfStatisticsTreeNode) event.item.getData();

                // If node is hidden, exit immediately.
                if (TmfBaseColumnDataProvider.HIDDEN_FOLDER_LEVELS.contains(node.getName())) {
                    return;
                }

                // Otherwise, get percentage and draw bar and text if
                // applicable.
                double percentage = fColumnDataList.get(event.index).getPercentageProvider().getPercentage(node);

                // The item is selected.
                if ((event.detail & SWT.SELECTED) > 0) {
                    // Draws our own background to avoid overwriting the
                    // bar.
                    event.gc.fillRectangle(event.x, event.y, event.width, event.height);
                    event.detail &= ~SWT.SELECTED;
                }

                /*
                 * Drawing the percentage text if events are present in top
                 * node and the current node is not the top node and if is
                 * total or partial events column. If not, exit the method.
                 */
                if (!((event.index == TmfBaseColumnDataProvider.StatsColumn.TOTAL.getIndex() || event.index == TmfBaseColumnDataProvider.StatsColumn.PARTIAL.getIndex())
                && node != node.getTop())) {
                    return;
                }

                long eventValue = event.index == TmfBaseColumnDataProvider.StatsColumn.TOTAL.getIndex() ?
                        node.getTop().getValues().getTotal() : node.getTop().getValues().getPartial();

                if (eventValue != 0) {

                    int oldAlpha = event.gc.getAlpha();
                    Color oldForeground = event.gc.getForeground();
                    Color oldBackground = event.gc.getBackground();

                    // Bar to draw
                    if (percentage != 0) {
                        /*
                         * Draws a transparent gradient rectangle from the
                         * color of foreground and background.
                         */
                        int barWidth = (int) ((fTreeViewer.getTree().getColumn(event.index).getWidth() - 8) * percentage);
                        event.gc.setAlpha(64);
                        event.gc.setForeground(event.item.getDisplay().getSystemColor(SWT.COLOR_BLUE));
                        event.gc.setBackground(event.item.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
                        event.gc.fillGradientRectangle(event.x, event.y, barWidth, event.height, true);
                        event.gc.drawRectangle(event.x, event.y, barWidth, event.height);

                        // Restore old values
                        event.gc.setBackground(oldBackground);
                        event.gc.setAlpha(oldAlpha);
                        event.detail &= ~SWT.BACKGROUND;

                    }

                    String percentageText = TmfStatisticsFormatter.toPercentageText(percentage);
                    String absoluteNumberText = TmfStatisticsFormatter.toColumnData(node, TmfBaseColumnDataProvider.StatsColumn.getColumn(event.index));

                    if (event.width > event.gc.stringExtent(percentageText).x + event.gc.stringExtent(absoluteNumberText).x) {
                        int textHeight = event.gc.stringExtent(percentageText).y;
                        event.gc.setForeground(event.item.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
                        event.gc.drawText(percentageText, event.x, event.y + (event.height - textHeight) / 2, true);
                    }

                    // Restores old values
                    event.gc.setForeground(oldForeground);

                }
            }
        }
    }

    /**
     * The actual tree viewer to display
     */
    private TreeViewer fTreeViewer;

    /**
     * The viewer that builds the piecharts to show statistics
     *
     * @since 1.0
     */
    private TmfPieChartViewer fPieChartViewer;

    private SashForm fSash;

    /**
     * Create a basic statistics viewer. To be used in conjunction with
     * {@link TmfStatisticsViewer#initContent(Composite)}
     *
     * @param parent
     *            The parent composite that will hold the viewer
     * @param viewerName
     *            The name that will be assigned to this viewer
     * @see TmfComponent
     * @since 1.0
     */
    public TmfStatisticsViewer(
            Composite parent,
            String viewerName) {
        super.init(parent, viewerName);
        initContent(parent);
    }

    @Override
    public void dispose() {
        super.dispose();
        // Clean the model for this viewer
        fTreeViewer.getTree().dispose();
        fPieChartViewer.dispose();
    }

    // ------------------------------------------------------------------------
    // Class methods
    // ------------------------------------------------------------------------

    /**
     * Returns the primary control associated with this viewer.
     *
     * @return the SWT control which displays this viewer's content
     */
    @Override
    public Control getControl() {
        return fSash;
    }

    /**
     * Get the input of the viewer.
     *
     * @return an object representing the input of the statistics viewer.
     */
    public Object getInput() {
        return fTreeViewer.getInput();
    }

    /**
     * Returns a unique ID based on name to be associated with the statistics
     * tree for this viewer. For a same name, it will always return the same ID.
     *
     * @return a unique statistics tree ID.
     */
    public String getTreeID() {
        return getName();
    }

    @Override
    public void refresh() {
        final Control viewerControl = getControl();
        // Ignore update if disposed
        if (viewerControl.isDisposed()) {
            return;
        }

        TmfUiRefreshHandler.getInstance().queueUpdate(this, new Runnable() {
            @Override
            public void run() {
                if (!viewerControl.isDisposed()) {
                    fTreeViewer.refresh();
                    fPieChartViewer.redraw();
                }
            }
        });
    }

    /**
     * Focus on the statistics tree of the viewer
     */
    public void setFocus() {
        fTreeViewer.getTree().setFocus();
    }

    /**
     * Cancels the request if it is not already completed
     *
     * @param request
     *            The request to be canceled
     */
    protected void cancelOngoingRequest(ITmfEventRequest request) {
        if (request != null && !request.isCompleted()) {
            request.cancel();
        }
    }

    /**
     * This method can be overridden to change the representation of the data in
     * the columns.
     *
     * @return An object of type {@link TmfBaseColumnDataProvider}.
     */
    protected TmfBaseColumnDataProvider getColumnDataProvider() {
        return new TmfBaseColumnDataProvider();
    }

    /**
     * Initialize the content that will be drawn in this viewer
     *
     * @param parent
     *            The parent of the control to create
     */
    protected void initContent(Composite parent) {

        final List<TmfBaseColumnData> columnDataList = getColumnDataProvider().getColumnData();

        fSash = new SashForm(parent, SWT.BORDER | SWT.HORIZONTAL);
        GridData gd = new GridData(SWT.FILL,SWT.FILL,true,true);
        fSash.setLayoutData(gd);

        fTreeViewer = new TreeViewer(fSash, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        fPieChartViewer = new TmfPieChartViewer(fSash);

        fTreeViewer.setContentProvider(new TmfTreeContentProvider());
        fTreeViewer.getTree().setHeaderVisible(true);
        fTreeViewer.setUseHashlookup(true);
        fSash.setWeights(new int[] { 100, 100 });
        // Creates the columns defined by the column data provider
        for (final TmfBaseColumnData columnData : columnDataList) {
            setUpViewerColumns(columnDataList, columnData);
        }

        // Handler that will draw the percentages and the bar charts.
        fTreeViewer.getTree().addListener(SWT.EraseItem, new EraseListener(columnDataList));

        // Initializes the comparator parameters
        fTreeViewer.setComparator(columnDataList.get(0).getComparator());
        fTreeViewer.getTree().setSortColumn(fTreeViewer.getTree().getColumn(0));
        fTreeViewer.getTree().setSortDirection(SWT.DOWN);
    }

    /**
     * @param columnDataList
     * @param columnData
     */
    private void setUpViewerColumns(final List<TmfBaseColumnData> columnDataList, final TmfBaseColumnData columnData) {
        final TreeViewerColumn treeColumn = new TreeViewerColumn(fTreeViewer, columnData.getAlignment());
        treeColumn.getColumn().setText(columnData.getHeader());
        treeColumn.getColumn().setWidth(columnData.getWidth());
        treeColumn.getColumn().setToolTipText(columnData.getTooltip());

        // If is dummy column
        if (columnData == columnDataList.get(TmfBaseColumnDataProvider.StatsColumn.DUMMY.getIndex())) {
            treeColumn.getColumn().setResizable(false);
        }

        // A comparator is defined.
        if (columnData.getComparator() != null) {
            // Adds a listener on the columns header for sorting purpose.
            treeColumn.getColumn().addSelectionListener(new SelectionAdapter() {

                private ViewerComparator reverseComparator;

                @Override
                public void widgetSelected(SelectionEvent e) {
                    // Initializes the reverse comparator once.
                    if (reverseComparator == null) {
                        reverseComparator = new ViewerComparator() {
                            @Override
                            public int compare(Viewer viewer, Object e1, Object e2) {
                                return -1 * columnData.getComparator().compare(viewer, e1, e2);
                            }
                        };
                    }

                    if (fTreeViewer.getTree().getSortDirection() == SWT.UP
                            || fTreeViewer.getTree().getSortColumn() != treeColumn.getColumn()) {
                        /*
                         * Puts the descendant order if the old order was up
                         * or if the selected column has changed.
                         */
                        fTreeViewer.setComparator(columnData.getComparator());
                        fTreeViewer.getTree().setSortDirection(SWT.DOWN);
                    } else {
                        /*
                         * Puts the ascendant ordering if the selected
                         * column hasn't changed.
                         */
                        fTreeViewer.setComparator(reverseComparator);
                        fTreeViewer.getTree().setSortDirection(SWT.UP);
                    }
                    fTreeViewer.getTree().setSortColumn(treeColumn.getColumn());
                }
            });
        }
        treeColumn.setLabelProvider(columnData.getLabelProvider());
    }

    /**
     * Sets and show the data contained within a model. This affects both
     * viewers.
     *
     * @param model
     *            The input from which the tree will be populated
     * @since 1.0
     */
    public void setInput(final TmfStatisticsModel model) {

        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                fTreeViewer.setInput(model.getStatisticData().getRootNode());

                getPieChartViewer().setGlobalPieChartEntries(model.getPieChartGlobalModel());
                getPieChartViewer().setTimeRangePieChartEntries(model.getPieChartSelectionModel());
            }
        });
        refresh();
    }

    /**
     * @return The viewer representing the piechart.
     * @since 1.0
     */
    public TmfPieChartViewer getPieChartViewer() {
        return fPieChartViewer;
    }

    /**
     * Resets the number of events within the time range
     */
    protected void resetTimeRangeValue() {
        TmfStatisticsTreeNode treeModelRoot = TmfStatisticsTreeManager.getStatTreeRoot(getTreeID());
        if (treeModelRoot != null && treeModelRoot.hasChildren()) {
            treeModelRoot.resetTimeRangeValue();
        }
    }

    /**
     * @return The Tree viewer represented in this viewer
     * @since 1.0
     */
    public TreeViewer getTreeViewer() {
        return fTreeViewer;
    }
}
