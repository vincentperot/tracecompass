/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.timerange;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.tracecompass.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalThrottler;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfRegionOfInterest;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfTimeViewer;

/**
 * Display time ranges
 */
public abstract class AbstractTimeRangeTable extends TmfTimeViewer {


    private final class RangeUpdater implements SelectionListener {
        @Override
        public void widgetSelected(SelectionEvent e) {
            ITmfRegionOfInterest roi =  (ITmfRegionOfInterest) e.item.getData();
            TmfRangeSynchSignal signal = new TmfRangeSynchSignal(this, new TmfTimeRange(roi.getStartTime(), roi.getEndTime()));
            fTimeRangeSyncThrottler.queue(signal);
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
        }
    }

    private static final int DEFAULT_COL_WIDTH = 200;

    private final TableViewer fTableViewer;
    private final TmfSignalThrottler fTimeRangeSyncThrottler = new TmfSignalThrottler(this, 200);
    /**
     * Constructor that initializes the parent of the viewer
     *
     * @param parent
     *            The parent composite that holds this viewer
     */
    public AbstractTimeRangeTable(Composite parent) {
        super(parent);
        Composite comp = new Composite(parent, SWT.NONE);
        comp.setLayout(GridLayoutFactory.fillDefaults().create());
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        comp.setLayoutData(layoutData);

        fTableViewer = new TableViewer(comp, SWT.VIRTUAL);
        fTableViewer.getControl().setLayoutData(layoutData);
        createColumns();

        final Table table = fTableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.addSelectionListener(new RangeUpdater());

        fTableViewer.setUseHashlookup(true);
        refresh();
    }

    /**
     * The <code>ContentViewer</code> implementation of this <code>Viewer</code>
     * method invokes <code>inputChanged</code> on the content provider and then
     * the <code>inputChanged</code> hook method. This method fails if this
     * viewer does not have a content provider. Subclassers are advised to
     * override <code>inputChanged</code> rather than this method, but may
     * extend this method if required.
     *
     * @param input
     *            The input
     */
    public void setInput(Object input) {
        fTableViewer.setInput(input);
    }

    /**
     * Sets the content provider used by this viewer.
     * <p>
     * The <code>ContentViewer</code> implementation of this method records the
     * content provider in an internal state variable. Overriding this method is
     * generally not required; however, if overriding in a subclass,
     * <code>super.setContentProvider</code> must be invoked.
     * </p>
     *
     * @param contentProvider
     *            the content provider
     */
    public void setContentProvider(IContentProvider contentProvider) {
        fTableViewer.setContentProvider(contentProvider);
    }

    /**
     * Column initializer, can be overwritten
     */
    protected void createColumns() {
        createColumn(Messages.AbstractTimeRangeTable_startTime, new ColumnLabelProvider() {
            @Override
            public String getText(Object input) {
                ITmfRegionOfInterest roi = (ITmfRegionOfInterest) input;
                ITmfTimestamp startTime = roi.getStartTime();
                return startTime.toString();
            }
        });
        createColumn(Messages.AbstractTimeRangeTable_endTime, new ColumnLabelProvider() {
            @Override
            public String getText(Object input) {
                ITmfRegionOfInterest roi = (ITmfRegionOfInterest) input;
                ITmfTimestamp endTime = roi.getEndTime();
                return endTime.toString();
            }
        });
        createColumn(Messages.AbstractTimeRangeTable_duration, new ColumnLabelProvider() {
            @Override
            public String getText(Object input) {
                ITmfRegionOfInterest roi = (ITmfRegionOfInterest) input;
                return Long.toString(roi.getDuration());
            }
        });
        createColumn(Messages.AbstractTimeRangeTable_content, new ColumnLabelProvider() {
            @Override
            public String getText(Object input) {
                ITmfRegionOfInterest roi = (ITmfRegionOfInterest) input;
                return roi.getMessage();
            }
        });
    }

    /**
     * Create a column for the table
     *
     * @param name
     *            the name of the column
     * @param provider
     *            the provider of the column
     */
    protected void createColumn(String name, ColumnLabelProvider provider) {
        TableViewerColumn col = new TableViewerColumn(fTableViewer, SWT.NONE);
        col.getColumn().setWidth(DEFAULT_COL_WIDTH);
        col.getColumn().setText(name);
        col.setLabelProvider(provider);
    }

    @Override
    public Control getControl() {
        return fTableViewer.getControl();
    }

    @Override
    public void refresh() {
        fTableViewer.refresh();
    }

    /**
     * Set the item count of the receiver.
     *
     * @param count
     *            the new table size.
     */
    protected void setItemCount(int count) {
        fTableViewer.setItemCount(count);

    }

}
