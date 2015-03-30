/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.matchers.AbstractMatcher;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

/**
 * Tells if a particular widget has belongs to a given view
 *
 * @version $Id$
 * @param <T>
 *            The widget type
 */
public class InView<T extends Widget> extends AbstractMatcher<T> {

    private final SWTBotView fView;
    private final Class<T> fType;

    /**
     * Matches a widget that belongs to a particular view
     */
    private InView(SWTBotView view, Class<T> type) {
        fView = view;
        fType = type;
    }

    @Override
    protected boolean doMatch(final Object obj) {
        return UIThreadRunnable.syncExec(new Result<Boolean>() {
            @Override
            public Boolean run() {
                Widget widget = fView.getWidget();
                if (widget instanceof Composite && fType.isInstance(obj)) {
                    return contains((Composite) widget, (Widget) obj);
                }
                return false;
            }

            private boolean contains(Composite parent, Widget target) {
                for (Control child : parent.getChildren()) {
                    if (child.equals(target)) {
                        return true;
                    }
                    if (child instanceof Composite) {
                        return contains((Composite) child, target);
                    }
                }
                return false;
            }

        });
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("from view(").appendText(fView.getTitle()).appendText(")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * Matches a widget residing in a specific view
     *
     * @param view
     *            The view to search
     * @param type
     *            The type of the widget to look up
     *
     * @return a matcher.
     */
    @Factory
    public static <T extends Widget> Matcher<T> inView(SWTBotView view, Class<T> type) {
        return new InView<>(view, type);
    }

}