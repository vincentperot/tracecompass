/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.views.timerange;

import java.util.Collection;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.core.parsers.ReportRegionOfInterest;
import org.eclipse.tracecompass.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfRegionOfInterest;
import org.eclipse.tracecompass.tmf.ui.viewers.table.AbstractTmfColumnTable;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;

/**
 * Displays the regions of interest
 */
public class RegionsOfInterestView extends TmfView {

    private final class RegionOfInterestTable extends AbstractTmfColumnTable {
        private final class RegionOfInterestSelectionListener implements SelectionListener {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ReportRegionOfInterest roi = (ReportRegionOfInterest) e.item.getData();
                TmfTimeRange offsetRange = roi.getOffsetTimestamp(getActiveTrace().getTimeRange());
                if (offsetRange != null) {
                    if (roi.getDuration() != 0) {
                        updateRange(offsetRange);
                    } else {
                        updateTime(offsetRange.getStartTime());
                    }
                }
            }

            private void updateTime(ITmfTimestamp newTime) {
                TmfTimeSynchSignal signal = new TmfTimeSynchSignal(this, newTime);
                getTimeRangeSyncThrottler().queue(signal);
            }

            private void updateRange(TmfTimeRange offsetRange) {
                TmfRangeSynchSignal signal = new TmfRangeSynchSignal(this, new TmfTimeRange(offsetRange.getStartTime(), offsetRange.getEndTime()));
                getTimeRangeSyncThrottler().queue(signal);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
        }

        private RegionOfInterestTable(Composite parent) {
            super(parent);
            addSelectionListener(new RegionOfInterestSelectionListener());
        }

        @Override
        protected void createColumns() {
            {
                createColumn(Messages.RegionsOfInterestTable_startTime, new ColumnLabelProvider() {
                    @Override
                    public String getText(Object input) {
                        ITmfRegionOfInterest roi = (ITmfRegionOfInterest) input;
                        ITmfTimestamp startTime = roi.getStartTime();
                        return startTime.toString();
                    }
                }, new StartTimeColumnComparator());
                createColumn(Messages.RegionsOfInterestTable_endTime, new ColumnLabelProvider() {
                    @Override
                    public String getText(Object input) {
                        ITmfRegionOfInterest roi = (ITmfRegionOfInterest) input;
                        ITmfTimestamp endTime = roi.getEndTime();
                        return endTime.toString();
                    }
                }, new EndTimeColumnComparator());
                createColumn(Messages.RegionsOfInterestTable_duration, new ColumnLabelProvider() {
                    @Override
                    public String getText(Object input) {
                        ITmfRegionOfInterest roi = (ITmfRegionOfInterest) input;
                        return Long.toString(roi.getDuration());
                    }
                }, new DurationColumnComparator());
                createColumn(Messages.RegionsOfInterestTable_content, new ColumnLabelProvider() {
                    @Override
                    public String getText(Object input) {
                        ITmfRegionOfInterest roi = (ITmfRegionOfInterest) input;
                        return roi.getMessage();
                    }
                }, new MessageColumnComparator());
            }
        }
    }

    /**
     * Default ID string
     */
    public static final String ID = "org.eclipse.tracecompass.tmf.views.regionofinterest"; //$NON-NLS-1$
    /**
     * Import icon
     */
    private static final String IMPORT_ICON_PATH = "icons/etool16/import.gif"; //$NON-NLS-1$
    private final AbstractTmfColumnTable[] fTable = new RegionOfInterestTable[1];
    private Action fImportAction;
    private Action fPasteAction;

    /**
     * Constructor
     */
    public RegionsOfInterestView() {
        super(ID);
    }

    @Override
    public void createPartControl(Composite parent) {
        Composite comp = new Composite(parent, SWT.FILL);
        comp.setLayout(GridLayoutFactory.fillDefaults().create());

        fTable[0] = new RegionOfInterestTable(comp);
        fTable[0].setContentProvider(new ArrayContentProvider());
        makeActions();

    }

    private void makeActions() {
        fPasteAction = getPasteAction();
        fPasteAction.setEnabled(true);
        fPasteAction.setText(Messages.RegionsOfInterestView_Paste);
        final MenuManager tablePopupMenu = new MenuManager();
        tablePopupMenu.add(fPasteAction);
        fPasteAction.setAccelerator(SWT.CTRL + 'v');
        Menu menu = tablePopupMenu.createContextMenu(fTable[0].getControl());
        fTable[0].getControl().setMenu(menu);
        IActionBars bars = getViewSite().getActionBars();
        bars.getToolBarManager().add(getImportAction());
    }

    private Action getPasteAction() {
        return new Action(Messages.RegionsOfInterestView_Paste) {
            @Override
            public void run() {
                final Display display = fTable[0].getControl().getDisplay();
                display.asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        Clipboard cb = new Clipboard(display);
                        TextTransfer tt = TextTransfer.getInstance();
                        String data = (String) cb.getContents(tt);
                        setRois(ReportRegionOfInterest.parse(data));
                        cb.dispose();
                    }
                });

            }
        };
    }

    private Action getImportAction() {
        if (fImportAction != null) {
            return fImportAction;
        }

        fImportAction = new Action() {
            @Override
            public void run() {
                PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                        TimeRangeInputDialog trs = new TimeRangeInputDialog(activeShell);
                        if (trs.open() == Window.OK) {
                            setRois(trs.getRois());
                        }
                    }
                });
            }
        };

        fImportAction.setText(Messages.RegionsOfInterestView_import);
        fImportAction.setToolTipText(Messages.RegionsOfInterestView_importToolTip);
        fImportAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(IMPORT_ICON_PATH));

        return fImportAction;
    }

    @Override
    public void setFocus() {
        // do nothing
    }

    private void setRois(Collection<ITmfRegionOfInterest> rois) {
        fTable[0].setInput(rois);
        fTable[0].refresh();
    }

}
