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

package org.eclipse.tracecompass.tmf.ui.viewers.table;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalThrottler;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;

/**
 * Generic {@link TableViewer} wrapper with most standard features enabled
 *
 * <pre>
 * It provides the following features:
 *   - Sortable columns
 *   - Movable columns
 *   - Resizable columns
 * </pre>
 *
 * The person extending this class must implement the {@link #createColumns()},
 * they must also supply a content provider to
 * {@link #setContentProvider(IContentProvider)} along with an input to
 * {@link #setInput(Object)} and they can add selection listeners with
 * {@link #addSelectionListener(SelectionListener)}
 */
public abstract class AbstractTmfColumnTable extends TmfViewer {

    private final class columnSorter extends SelectionAdapter {
        private final TableColumn fColumn;

        private columnSorter(TableColumn column) {
            fColumn = column;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {

            Table table = fTableViewer.getTable();
            table.setSortDirection(getSortDirection());
            TableColumn prevSortcolumn = table.getSortColumn();
            if (prevSortcolumn == fColumn) {
                flipSortDirection();
            }
            table.setSortColumn(fColumn);
            ViewerComparator comparator = fComparators.get(fColumn.getText());
            if (fDirection == SWT.DOWN) {
                fTableViewer.setComparator(comparator);
            } else {
                fTableViewer.setComparator(new InvertSorter(comparator));
            }
            fTableViewer.refresh();
        }
    }

    private class InvertSorter extends ViewerComparator {
        private final ViewerComparator fVc;

        public InvertSorter(ViewerComparator vc) {
            fVc = vc;
        }

        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            return -fVc.compare(viewer, e1, e2);
        }

    }

    private static final int DEFAULT_COL_WIDTH = 200;
    private final TableViewer fTableViewer;
    private final TmfSignalThrottler fTimeRangeSyncThrottler = new TmfSignalThrottler(this, 200);
    private final Map<String, ViewerComparator> fComparators = new HashMap<>();

    private int fDirection;

    /**
     * Constructor that initializes the parent of the viewer
     *
     * @param parent
     *            The parent composite that holds this viewer
     */
    public AbstractTmfColumnTable(Composite parent) {
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

        fDirection = SWT.DOWN;

        fTableViewer.setUseHashlookup(true);
        refresh();
    }

    /**
     * Adds the listener to the collection of listeners who will be notified
     * when the user changes the receiver's selection, by sending it one of the
     * messages defined in the <code>SelectionListener</code> interface.
     * <p>
     * When <code>widgetSelected</code> is called, the item field of the event
     * object is valid. If the receiver has the <code>SWT.CHECK</code> style and
     * the check selection changes, the event object detail field contains the
     * value <code>SWT.CHECK</code>. <code>widgetDefaultSelected</code> is
     * typically called when an item is double-clicked. The item field of the
     * event object is valid for default selection, but the detail field is not
     * used.
     * </p>
     *
     * @param listener
     *            the listener which should be notified when the user changes
     *            the receiver's selection
     *
     * @exception IllegalArgumentException
     *                <ul>
     *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     *                </ul>
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
     *                disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
     *                thread that created the receiver</li>
     *                </ul>
     *
     * @see SelectionListener
     * @see SelectionEvent
     */
    protected final void addSelectionListener(SelectionListener listener) {
        fTableViewer.getTable().addSelectionListener(listener);
    }

    /**
     * Create a column for the table
     *
     * @param name
     *            the name of the column (must be unique)
     * @param provider
     *            the provider of the column
     * @param viewerComparator
     *            the comparator associated with clicking on the column
     */

    protected final void createColumn(String name, ColumnLabelProvider provider, ViewerComparator viewerComparator) {
        if (fComparators.containsKey(name)) {
            throw new IllegalArgumentException("Cannot have two columns with the same name"); //$NON-NLS-1$
        }
        TableViewerColumn col = new TableViewerColumn(fTableViewer, SWT.NONE);
        col.setLabelProvider(provider);
        final TableColumn column = col.getColumn();
        column.setWidth(DEFAULT_COL_WIDTH);
        column.setText(name);
        column.setResizable(true);
        column.setMoveable(true);
        column.addSelectionListener(new columnSorter(column));
        fComparators.put(name, viewerComparator);
    }

    /**
     * Column initializer, called in the constructor
     */
    protected abstract void createColumns();

    private void flipSortDirection() {
        if (fDirection == SWT.DOWN) {
            fDirection = SWT.UP;
        } else {
            fDirection = SWT.DOWN;
        }

    }

    private int getSortDirection() {
        return fDirection;
    }

    @Override
    public final Control getControl() {
        return fTableViewer.getControl();
    }

    /**
     * Gets the signal throttler
     *
     * @return the signal throttler
     */
    protected final TmfSignalThrottler getTimeRangeSyncThrottler() {
        return fTimeRangeSyncThrottler;
    }

    @Override
    public final void refresh() {
        fTableViewer.refresh();
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
    public final void setContentProvider(IContentProvider contentProvider) {
        fTableViewer.setContentProvider(contentProvider);
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
    public final void setInput(Object input) {
        fTableViewer.setInput(input);
    }

}
