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
 * Tells if a particular widget has value for the given key.
 *
 * @version $Id$
 * @param <T>
 *            The widget type
 */
public class FromView<T extends Widget> extends AbstractMatcher<T> {

    private final SWTBotView fView;

    /**
     * Matches a widget that has the specified Key/Value pair set as data into
     * it.
     *
     * @see Widget#setData(String, Object)
     * @param key
     *            the key
     * @param value
     *            the value
     */
    FromView(SWTBotView view) {
        fView = view;
    }

    @Override
    protected boolean doMatch(final Object obj) {
        return UIThreadRunnable.syncExec(new Result<Boolean>() {
            @Override
            public Boolean run() {
                Widget widget = fView.getWidget();
                if (widget instanceof Composite && obj instanceof Widget) {
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
     *
     * @return a matcher.
     */
    @Factory
    public static <T extends Widget> Matcher<T> fromView(SWTBotView view) {
        return new FromView<>(view);
    }

}