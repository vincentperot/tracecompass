/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexis Cabana-Loriaux <alex021994@gmail.com> - Initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers.piecharts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.linuxtools.dataviewers.piechart.PieChart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

/**
 * Creates a viewer containing 2 pie charts, one for showing information about
 * the current selection, and the second one for showing information about the
 * current time-range selection. It follows the MVC pattern, being a view.
 *
 * This class is closely related with the {@link IPieChartViewerState} interface
 * that acts as a state machine for the general layout of the charts.
 *
 * @author Alexis Cabana-Loriaux <alex021994@gmail.com>
 *
 */
public class TmfPieChartViewer extends Composite {

    /**
     * The pie chart containing global information about the trace
     */
    private PieChart fGlobalPC;

    /**
     * The name of the piechart containing the statistics about the global trace
     */
    private String fGlobalPCname;

    /**
     * The pie chart containing information about the current time-range
     * selection
     */
    private PieChart fTimeRangePC;

    /**
     * The name of the piechart containing the statistics about the current
     * selection
     */
    private String fTimeRangePCname;

    /**
     * The listener added to the TimeRange Pie chart every time it is created
     */
    private Listener mouseListenerSelection;

    /**
     * The listener added to the Global Pie chart every time it is created
     */
    private Listener mouseListenerGlobal;

    /**
     * The name of the slice containing the too little slices
     */
    private String fOthersSliceName;

    /**
     * Implementation of the State design pattern to reorder the layout
     * depending on the selection. This variable holds the current state of the
     * layout.
     */
    private IPieChartViewerState fCurrentState;

    /**
     * Represents the minimum percentage a slice of pie must have in order to be
     * shown
     */
    private static final float MIN_PRECENTAGE_TO_SHOW_SLICE = 0.025F;// 2.5%

    /**
     * The data that has to be presented by the pie charts
     */
    private Map<String, Long> fGlobalEventTypes = new HashMap<>();
    private Map<String, Long> fTimeRangeSelectionEventTypes = new HashMap<>();

    /**
     * @param parent
     *            The parent composite that will hold the viewer
     * @param nameOfGLobal
     *            The name to be shown over the Global PieChart
     * @param nameOfSelection
     *            The name to be shown over the Time-range selection PieChart
     * @param nameOfOthers
     *            The name given to the slices piecharts
     */
    public TmfPieChartViewer(Composite parent, String nameOfGLobal, String nameOfSelection, String nameOfOthers) {
        super(parent, SWT.NONE);
        fGlobalPCname = nameOfGLobal;
        fTimeRangePCname = nameOfSelection;
        fOthersSliceName = nameOfOthers;
        initContent();
    }

    // ------------------------------------------------------------------------
    // Class methods
    // ------------------------------------------------------------------------

    /**
     * Called by this class' constructor. Constructs the basic viewer containing
     * the charts, as well as their listeners
     */
    private void initContent() {
        setLayout(new FillLayout());


        fGlobalPC = null;
        fTimeRangePC = null;

        // Setup listeners for the tooltips
        mouseListenerGlobal = new Listener() {
            @Override
            public void handleEvent(org.eclipse.swt.widgets.Event event) {
                PieChart pc = getGlobalPC();
                switch (event.type) {
                case SWT.MouseMove:
                    int sliceIndex = pc.getSliceIndexFromPosition(0, event.x, event.y);
                    if (sliceIndex < 0) {
                        // mouse is outside the chart
                        pc.setToolTipText(null);
                        break;
                    }
                    float percOfSlice = (float) pc.getSlicePercent(0, sliceIndex);
                    String percent = String.format("%.1f", percOfSlice); //$NON-NLS-1$
                    Long nbEvents = Long.valueOf((long) pc.getSeriesSet().getSeries()[sliceIndex].getXSeries()[0]);
                    String text = "Name = " + pc.getSeriesSet().getSeries()[sliceIndex].getId() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
                    text += "Event Count = " + nbEvents.toString() + " (" + percent + "%)"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    pc.setToolTipText(text);
                    return;
                default:
                }
            }
        };

        // Setup listeners for the tooltips
        mouseListenerSelection = new Listener() {
            @Override
            public void handleEvent(org.eclipse.swt.widgets.Event event) {
                PieChart pc = getTimeRangePC();
                switch (event.type) {
                case SWT.MouseMove:
                    int sliceIndex = pc.getSliceIndexFromPosition(0, event.x, event.y);
                    if (sliceIndex < 0) {
                        // mouse is outside the chart
                        pc.setToolTipText(null);
                        break;
                    }
                    float percOfSlice = (float) pc.getSlicePercent(0, sliceIndex);
                    String percent = String.format("%.1f", percOfSlice); //$NON-NLS-1$
                    Long nbEvents = Long.valueOf((long) pc.getSeriesSet().getSeries()[sliceIndex].getXSeries()[0]);
                    String text = "Name = " + pc.getSeriesSet().getSeries()[sliceIndex].getId() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
                    text += "Event Count = " + nbEvents.toString() + " (" + percent + "%)"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    pc.setToolTipText(text);
                    return;
                default:
                }
            }
        };
        // at creation no content is selected
        setCurrentState(new PieChartViewerStateNoContentSelected(this));
    }

    @Override
    public void dispose() {
        if (fGlobalPC != null) {
            fGlobalPC.dispose();
        }
        if (fTimeRangePC != null) {
            fTimeRangePC.dispose();
        }
        super.dispose();
    }

    /**
     * Updates the data contained in the Global PieChart by using a Map.
     * Normally, this method is only called by the state machine.
     */
    public void updateGlobalPieChart() {
        if (getGlobalPC() == null) {
            fGlobalPC = new PieChart(this, SWT.NONE);
            getGlobalPC().getTitle().setText(fGlobalPCname);
            getGlobalPC().getAxisSet().getXAxis(0).getTitle().setText(""); //Hide the title over the legend //$NON-NLS-1$
            getGlobalPC().getLegend().setVisible(true);
            getGlobalPC().getLegend().setPosition(SWT.RIGHT);
            getGlobalPC().addListener(SWT.MouseMove, mouseListenerGlobal);
        }
        else if (getGlobalPC().isDisposed()) {
            return;
        }

        PieChartUpdater(fGlobalPC, fGlobalEventTypes, MIN_PRECENTAGE_TO_SHOW_SLICE, fOthersSliceName);
    }

    /**
     * Updates the data contained in the Time-Range PieChart by using a Map.
     * Normally, this method is only called by the state machine.
     */
    public void updateTimeRangeSelectionPieChart() {
        if (getTimeRangePC() == null) {
            fTimeRangePC = new PieChart(this, SWT.NONE);
            getTimeRangePC().getTitle().setText(fTimeRangePCname);
            getTimeRangePC().getAxisSet().getXAxis(0).getTitle().setText(""); //Hide the title over the legend //$NON-NLS-1$
            getTimeRangePC().getLegend().setPosition(SWT.BOTTOM);
            getTimeRangePC().getLegend().setVisible(true);
            getTimeRangePC().addListener(SWT.MouseMove, mouseListenerSelection);
        }
        else if (getTimeRangePC().isDisposed()) {
            return;
        }

        PieChartUpdater(fTimeRangePC, fTimeRangeSelectionEventTypes, MIN_PRECENTAGE_TO_SHOW_SLICE, fOthersSliceName);
    }

    /**
     * Reinitializes the charts to their initial state, without any data
     */
    public void reinitializeCharts() {
        if (getGlobalPC() != null) {
            getGlobalPC().dispose();
        }
        fGlobalPC = new PieChart(this, SWT.NONE);
        getGlobalPC().getTitle().setText(fGlobalPCname);
        getGlobalPC().getAxisSet().getXAxis(0).getTitle().setText(""); //Hide the title over the legend //$NON-NLS-1$
        if (getTimeRangePC() != null) {
            getTimeRangePC().dispose();
            fTimeRangePC = null;
        }
        layout();
        setCurrentState(new PieChartViewerStateNoContentSelected(this));
    }

    /**
     * Function used to update or create the slices of a PieChart to match the
     * content of a Map passed in parameter. It also provides a facade to use
     * the PieChart API
     */
    private static void PieChartUpdater(
            final PieChart chart,
            final Map<String, Long> Slices,
            final float minimumSizeOfSlice,
            final String nameOfOthers) {

        List<EventOccurrenceObject> chartValues = new ArrayList<>();
        Long eventTotal = 0L;
        for (Entry<String, Long> entry : Slices.entrySet()) {
            eventTotal += entry.getValue();
            chartValues.add(new EventOccurrenceObject(entry.getKey(), entry.getValue()));
        }

        // No events in the selection
        if (eventTotal == 0) {
            // clear the chart and show "NO DATA"

            return;
        }

        /*
         * filter out the event types taking too little space in the chart and
         * label the whole group together. The remaining slices will be showing
         */
        List<EventOccurrenceObject> filteredChartValues = new ArrayList<>();
        Long OthersEntryCount = 0L;
        for (EventOccurrenceObject entry : chartValues) {
            if (entry.getNbOccurence() / eventTotal.floatValue() > minimumSizeOfSlice) {
                filteredChartValues.add(entry);
            } else {
                OthersEntryCount += entry.getNbOccurence();
            }
        }

        Collections.sort(filteredChartValues);

        // Add the "Others" slice in the pie if its not empty
        if (OthersEntryCount != 0) {
            filteredChartValues.add(new EventOccurrenceObject(nameOfOthers, OthersEntryCount));
        }

        // put the entries in the chart and add their percentage
        double[][] tempValues = new double[filteredChartValues.size()][1];
        String[] tempNames = new String[filteredChartValues.size()];
        int index = 0;
        for (EventOccurrenceObject entry : filteredChartValues) {
            tempValues[index][0] = entry.getNbOccurence();
            tempNames[index] = entry.getName();
            index++;
        }

        chart.addPieChartSeries(tempNames, tempValues);
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @return the global piechart
     */
    public PieChart getGlobalPC() {
        return fGlobalPC;
    }

    /**
     * @return the time-range selection piechart
     */
    public PieChart getTimeRangePC() {
        return fTimeRangePC;
    }

    /**
     * @return the current state of the viewer
     */
    public synchronized IPieChartViewerState getCurrentState() {
        return fCurrentState;
    }

    // ------------------------------------------------------------------------
    // Setters
    // ------------------------------------------------------------------------

    /**
     * A call to this method modifies the PieChart viewer
     *
     * @param entries
     *            The new entries to show in the Global PieChart.
     */
    public void setGlobalPieChartEntries(Map<String, Long> entries) {
        fGlobalEventTypes = entries;
        getCurrentState().newGlobalEntries(this);
    }

    /**
     * A call to this method modifies the PieChart viewer
     *
     * @param entries
     *            The new entries to be shown in the Time-range selection
     *            PieChart.
     */
    public void setTimeRangePieChartEntries(Map<String, Long> entries) {
        fTimeRangeSelectionEventTypes = entries;

        // Check if the selection is empty
        long Tot = 0;
        for (Entry<String, Long> entry : fTimeRangeSelectionEventTypes.entrySet()) {
            Tot += entry.getValue();
        }

        // Check if the selection is empty
        if (Tot == 0) {
            getCurrentState().newEmptySelection(this);
        } else {
            getCurrentState().newSelection(this);
        }
    }

    /**
     * Normally, this method is only called by the state machine
     *
     * @param newChart
     *            the new PieChart
     */
    public synchronized void setTimeRangePC(PieChart newChart) {
        fTimeRangePC = newChart;
    }

    /**
     * Setter method for the state.
     *
     * @param newState
     *            The new state of the viewer Normally only called by classes
     *            implementing the IPieChartViewerState interface.
     */
    public synchronized void setCurrentState(final IPieChartViewerState newState) {
        fCurrentState = newState;
    }

    /**
     * Nested class used to handle and sort more easily the pair (Name, Number
     * of occurences)
     *
     * @author Alexis Cabana-Loriaux <alexis.cabana-loriaux@poltmtl.ca>
     */
    private static class EventOccurrenceObject implements Comparable<EventOccurrenceObject> {

        private String fName;

        private Long fNbOccurrences;

        EventOccurrenceObject(String name, Long nbOccurences) {
            this.fName = name;
            this.fNbOccurrences = nbOccurences;
        }

        @Override
        public int compareTo(EventOccurrenceObject other) {
            // descending order
            return -Long.compare(this.getNbOccurence(), other.getNbOccurence());
        }

        public String getName() {
            return fName;
        }

        public Long getNbOccurence() {
            return fNbOccurrences;
        }
    }
}
