/*******************************************************************************
 * Copyright (c) 2009, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Added possibility to pin view
 *   Marc-Andre Laperle - Support for view alignment
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.tmf.ui.views.TimeAlignViewsAction;
import org.eclipse.tracecompass.internal.tmf.ui.views.TmfAlignmentSynchronizer;
import org.eclipse.tracecompass.tmf.core.component.ITmfComponent;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

/**
 * Basic abstract TMF view class implementation.
 *
 * It registers any sub class to the signal manager for receiving and sending
 * TMF signals.
 *
 * @author Francois Chouinard
 */
public abstract class TmfView extends ViewPart implements ITmfComponent {

    private final String fName;
    /** This allows us to keep track of the view sizes */
    private Composite fParentComposite;
    private ControlAdapter fControlListener;
    private static final TmfAlignmentSynchronizer fTimeAlignmentSynchronizer = new TmfAlignmentSynchronizer();

    /**
     * Action class for pinning of TmfView.
     */
    protected PinTmfViewAction fPinAction;
    private static TimeAlignViewsAction fAlignViewsAction;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor. Creates a TMF view and registers to the signal manager.
     *
     * @param viewName
     *            A view name
     */
    public TmfView(String viewName) {
        super();
        fName = viewName;
        TmfSignalManager.register(this);
    }

    /**
     * Disposes this view and de-registers itself from the signal manager
     */
    @Override
    public void dispose() {
        TmfSignalManager.deregister(this);
        super.dispose();
    }

    // ------------------------------------------------------------------------
    // ITmfComponent
    // ------------------------------------------------------------------------

    @Override
    public String getName() {
        return fName;
    }

    @Override
    public void broadcast(TmfSignal signal) {
        TmfSignalManager.dispatchSignal(signal);
    }

    @Override
    public void broadcastAsync(TmfSignal signal) {
        TmfSignalManager.dispatchSignalAsync(signal);
    }

    // ------------------------------------------------------------------------
    // View pinning support
    // ------------------------------------------------------------------------

    /**
     * Returns whether the pin flag is set.
     * For example, this flag can be used to ignore time synchronization signals from other TmfViews.
     *
     * @return pin flag
     */
    public boolean isPinned() {
        return ((fPinAction != null) && (fPinAction.isChecked()));
    }

    /**
     * Method adds a pin action to the TmfView. The pin action allows to toggle the <code>fIsPinned</code> flag.
     * For example, this flag can be used to ignore time synchronization signals from other TmfViews.
     */
    protected void contributePinActionToToolBar() {
        if (fPinAction == null) {
            fPinAction = new PinTmfViewAction();

            IToolBarManager toolBarManager = getViewSite().getActionBars()
                    .getToolBarManager();
            toolBarManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
            toolBarManager.add(fPinAction);
        }
    }

    @Override
    public void createPartControl(final Composite parent) {
        fParentComposite = parent;
        if (this instanceof ITmfTimeAligned) {
            contributeAlignViewsActionToToolbar();

            // Wait until the view is first painted before aligning it. That
            // way, we know its controls will be fully created.
            fParentComposite.addPaintListener(new PaintListener() {
                @Override
                public void paintControl(PaintEvent e) {
                    fParentComposite.removePaintListener(this);
                    fTimeAlignmentSynchronizer.handleViewCreated(TmfView.this);

                    fControlListener = new ControlAdapter() {
                        @Override
                        public void controlResized(ControlEvent event) {
                            fTimeAlignmentSynchronizer.handleViewResized(TmfView.this);
                        }
                    };
                    fParentComposite.addControlListener(fControlListener);
                }
            });

            getSite().getPage().addPartListener(new IPartListener() {
                @Override
                public void partOpened(IWorkbenchPart part) {
                }

                @Override
                public void partDeactivated(IWorkbenchPart part) {
                }

                @Override
                public void partClosed(IWorkbenchPart part) {
                    if (part == TmfView.this && fControlListener != null && !fParentComposite.isDisposed()) {
                        fParentComposite.removeControlListener(fControlListener);
                        fControlListener = null;
                    }
                }

                @Override
                public void partBroughtToTop(IWorkbenchPart part) {
                }

                @Override
                public void partActivated(IWorkbenchPart part) {
                }
            });
        }
    }

    private void contributeAlignViewsActionToToolbar() {
        if (fAlignViewsAction == null) {
            fAlignViewsAction = new TimeAlignViewsAction();
        }

        IToolBarManager toolBarManager = getViewSite().getActionBars()
                .getToolBarManager();
        toolBarManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        toolBarManager.add(fAlignViewsAction);
    }

    /**
     * Returns the parent control of the view
     *
     * @return the parent control
     *
     * @since 1.0
     */
    public Composite getParentComposite() {
        return fParentComposite;
    }
}
