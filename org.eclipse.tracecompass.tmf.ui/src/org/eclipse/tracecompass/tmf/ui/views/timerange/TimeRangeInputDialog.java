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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.tmf.core.parsers.ReportRegionOfInterest;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfRegionOfInterest;

/**
 * Regions of interest import dialog
 */
public class TimeRangeInputDialog extends Dialog {

    private static final RGB fStartTsRGB = new RGB(130, 170, 170);
    private static final RGB fEndTsRGB = new RGB(100, 170, 130);
    private static final RGB fMessageRBG = new RGB(170, 128, 170);
    private static final RGB fInvalidRBG = new RGB(255, 128, 128);

    private final Color fStartTimeColor;
    private final Color fEndTimeColor;
    private final Color fMessageColor;
    private final Color fInvalidColor;

    private StyledText fTextBox;
    private final Color fBlackColor;
    private Collection<ITmfRegionOfInterest> fRois;

    /**
     * Creates a dialog instance. Note that the window will have no visual
     * representation (no widgets) until it is told to open. By default,
     * <code>open</code> blocks for dialogs.
     *
     * @param parentShell
     *            the parent shell, or <code>null</code> to create a top-level
     *            shell
     */
    public TimeRangeInputDialog(Shell parentShell) {
        super(parentShell);
        fBlackColor = new Color(parentShell.getDisplay(), new RGB(0, 0, 0));
        fStartTimeColor = new Color(parentShell.getDisplay(), fStartTsRGB);
        fEndTimeColor = new Color(parentShell.getDisplay(), fEndTsRGB);
        fMessageColor = new Color(parentShell.getDisplay(), fMessageRBG);
        fInvalidColor = new Color(parentShell.getDisplay(), fInvalidRBG);
        fRois = Collections.EMPTY_LIST;
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    @Override
    protected void okPressed() {
        super.okPressed();
        dispose();
    }

    @Override
    protected void cancelPressed() {
        super.cancelPressed();
        dispose();
    }

    private void dispose() {
        fStartTimeColor.dispose();
        fEndTimeColor.dispose();
        fMessageColor.dispose();
        fInvalidColor.dispose();
        fBlackColor.dispose();
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = 550;
        gd.heightHint = 400;
        container.setLayoutData(gd);

        createLabel(container);

        fTextBox = new StyledText(container, SWT.V_SCROLL | SWT.H_SCROLL);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        layoutData.grabExcessVerticalSpace = true;
        fTextBox.setLayoutData(layoutData);
        fTextBox.setVisible(true);
        fTextBox.addExtendedModifyListener(new ExtendedModifyListener() {
            @Override
            public void modifyText(ExtendedModifyEvent event) {
                update();
            }
        });
        return container;
    }

    private static void createLabel(Composite container) {
        GridData gd;
        // Instructions label
        Label label = new Label(container, SWT.NONE);
        label.setText(Messages.TimeRangeInputDialog_label);
        gd = new GridData(GridData.BEGINNING);
        label.setLayoutData(gd);
    }

    private void update() {
        String data = fTextBox.getText();
        String[] lines = data.split("\n"); //$NON-NLS-1$
        List<StyleRange> srs = new ArrayList<>();
        fRois = ReportRegionOfInterest.parse(data);
        getButton(IDialogConstants.OK_ID).setEnabled(!fRois.isEmpty());
        int offset = 0;
        for (String line : lines) {
            parseLine(srs, offset, line);
            offset += line.length() + 1;
        }
        StyleRange[] styleRanges = srs.toArray(new StyleRange[0]);
        fTextBox.setStyleRanges(styleRanges);
    }

    private void parseLine(List<StyleRange> srs, int offset, String line) {
        int openB = line.indexOf('[') + offset;
        int firstC = line.indexOf(',', openB - offset);
        int closeB = line.indexOf(']', openB - offset);
        if (openB != -1 && closeB != -1) {
            if (openB > closeB + offset) {
                srs.add(new StyleRange(offset, line.length() - 1, fBlackColor, fInvalidColor));
            }
            else {
                parseValidLine(srs, offset, line, openB, firstC, closeB);
            }
        }
    }

    private void parseValidLine(List<StyleRange> srs, int offset, String line, int openB, int firstC, int closeB) {
        int start = openB + 1;
        if (firstC == -1) {
            int lengthTs = closeB - openB - 1 + offset;
            srs.add(new StyleRange(start, lengthTs, fBlackColor, fStartTimeColor));
        }
        else {
            int lengthTs1 = firstC - openB - 1 + offset;
            srs.add(new StyleRange(start, lengthTs1, fBlackColor, fStartTimeColor));
            int startTs2 = firstC + 1 + offset;
            int lengthTs2 = closeB - firstC - 1;
            srs.add(new StyleRange(startTs2, lengthTs2, fBlackColor, fEndTimeColor));
        }
        int startMsg = closeB + 1 + offset;
        srs.add(new StyleRange(startMsg, line.length() - closeB - 1, fBlackColor, fMessageColor));
    }

    /**
     * Get regions of interest, can be empty
     *
     * @return the regions of interest
     */
    public Collection<ITmfRegionOfInterest> getRois() {
        return fRois;
    }
}
