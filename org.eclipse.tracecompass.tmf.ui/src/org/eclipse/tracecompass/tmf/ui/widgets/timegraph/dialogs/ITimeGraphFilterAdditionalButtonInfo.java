/*******************************************************************************
 * Copyright (c) 2015 Keba AG
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Christian Mansky - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs;

/**
 * Interface containing information for an additional view specific button to be
 * used in TimeGraphFilterDialog.
 *
 * @since 1.0
 */
public interface ITimeGraphFilterAdditionalButtonInfo {

    /**
     * @return Name of the button label.
     */
    public String getButtonLabel();

    /**
     * @return Tooltip of the button.
     */
    public String getToolTip();

    /**
     * @param element
     *            An Element in the TimeGraphFilterDialog to check against
     *            selecting/ticking
     * @return Returns true if this element has to be selected. Do nothing when
     *         false.
     */
    public boolean checkSelectionStatus(Object element);

    /**
     * @param element
     *            An Element in the TimeGraphFilterDialog to check against
     *            unselecting/unticking
     * @return Returns true if this element has to be unselected. Do nothing
     *         when false.
     */
    public boolean checkUnselectionStatus(Object element);
}
