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
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.views.timerange;

import java.util.Collection;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.tmf.core.parsers.ReportRegionOfInterest;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfRegionOfInterest;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;
import org.eclipse.ui.IActionBars;

/**
 * Displays the regions of interest
 */
public class RegionsOfInterestView extends TmfView {

    private final class RegionOfInterestTable extends AbstractTimeRangeTable {
        private RegionOfInterestTable(Composite parent) {
            super(parent);
        }
    }

    /**
     * Default ID string
     */
    public static final String ID = "org.eclipse.tracecompass.tmf.views.regionofinterest"; //$NON-NLS-1$
    private final AbstractTimeRangeTable[] fTable = new RegionOfInterestTable[1];
    private Action fImportAction;

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

        makeActions();

        fTable[0] = new RegionOfInterestTable(comp);
        fTable[0].setContentProvider(new ArrayContentProvider());

    }

    private void makeActions() {
        IActionBars bars = getViewSite().getActionBars();
        bars.getToolBarManager().add(getImportAction());
    }

    private Action getImportAction() {
        if (fImportAction != null) {
            return fImportAction;
        }

        fImportAction = new Action() {
            @Override
            public void addPropertyChangeListener(IPropertyChangeListener listener) {
                Collection<ITmfRegionOfInterest> rois = ReportRegionOfInterest.parse("[12:12:12.000000000 - 13:00:00.000000000 ] Hi mom!"); //$NON-NLS-1$
                fTable[0].setInput(rois);
                fTable[0].refresh();

            }
        };

        fImportAction.setText("Import");
        fImportAction.setToolTipText(Messages.RegionsOfInterestView_importToolTip);

        return fImportAction;
    }

    @Override
    public void setFocus() {

    }

}
