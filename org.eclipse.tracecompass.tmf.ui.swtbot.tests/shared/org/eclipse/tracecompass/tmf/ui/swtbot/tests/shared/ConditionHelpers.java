/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Alexandre Montplaisir - Replaced separate Condition objects by anonymous classes
 *   Patrick Tasse - Add projectElementHasChild and isEditorOpened conditions
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared;


import static org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory.withPartName;

import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.IEditorReference;
import org.hamcrest.Matcher;

/**
 * Is a tree node available
 *
 * @author Matthew Khouzam
 */
public final class ConditionHelpers {

    private ConditionHelpers() {}

    /**
     * Provide default implementations for some {@link ICondition} methods.
     */
    private abstract static class SWTBotTestCondition implements ICondition {

        @Override
        public abstract boolean test() throws Exception;

        @Override
        public final void init(SWTBot bot) {
        }

        @Override
        public final String getFailureMessage() {
            return null;
        }
    }

    /**
     * Is a tree node available
     *
     * @param name
     *            the name of the node
     * @param tree
     *            the parent tree
     * @return true or false, it should swallow all exceptions
     */
    public static ICondition IsTreeNodeAvailable(final String name, final SWTBotTree tree) {
        return new SWTBotTestCondition() {
            @Override
            public boolean test() throws Exception {
                try {
                    final SWTBotTreeItem[] treeItems = tree.getAllItems();
                    for (SWTBotTreeItem ti : treeItems) {
                        final String text = ti.getText();
                        if (text.equals(name)) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                }
                return false;
            }
        };
    }

    /**
     * Is a table item available
     *
     * @param name
     *            the name of the item
     * @param table
     *            the parent table
     * @return true or false, it should swallow all exceptions
     */
    public static ICondition isTableItemAvailable(final String name, final SWTBotTable table) {
        return new SWTBotTestCondition() {
            @Override
            public boolean test() throws Exception {
                try {
                    return table.containsItem(name);
                } catch (Exception e) {
                }
                return false;
            }
        };
    }

    /**
     * Is the treeItem's node available
     *
     * @param name
     *            the name of the node
     * @param treeItem
     *            the treeItem
     * @return true or false, it should swallow all exceptions
     */
    public static ICondition IsTreeChildNodeAvailable(final String name, final SWTBotTreeItem treeItem) {
        return new SWTBotTestCondition() {
            @Override
            public boolean test() throws Exception {
                try {
                    return treeItem.getNode(name) != null;
                } catch (Exception e) {
                }
                return false;
            }
        };
    }

    /**
     * Checks if the wizard's shell is null
     *
     * @param wizard
     *            the null
     * @return false if either are null
     */
    public static ICondition isWizardReady(final Wizard wizard) {
        return new SWTBotTestCondition() {
            @Override
            public boolean test() throws Exception {
                if (wizard.getShell() == null) {
                    return false;
                }
                return true;
            }
        };
    }

    /**
     * Is the wizard on the page you want?
     *
     * @param wizard
     *            wizard
     * @param page
     *            the desired page
     * @return true or false
     */
    public static ICondition isWizardOnPage(final Wizard wizard, final IWizardPage page) {
        return new SWTBotTestCondition() {
            @Override
            public boolean test() throws Exception {
                if (wizard == null || page == null) {
                    return false;
                }
                final IWizardContainer container = wizard.getContainer();
                if (container == null) {
                    return false;
                }
                IWizardPage currentPage = container.getCurrentPage();
                return page.equals(currentPage);
            }
        };
    }

    /**
     * Wait for a view to close
     *
     * @param view
     *            bot view for the view
     * @return true if the view is closed, false if it's active.
     */
    public static ICondition ViewIsClosed(final SWTBotView view) {
        return new SWTBotTestCondition() {
            @Override
            public boolean test() throws Exception {
                return (view != null) && (!view.isActive());
            }
        };
    }

    /**
     * Wait till table cell has a given content.
     *
     * @param table
     *            the table bot reference
     * @param content
     *            the content to check
     * @param row
     *            the row of the cell
     * @param column
     *            the column of the cell
     * @return ICondition for verification
     */
    public static ICondition isTableCellFilled(final SWTBotTable table,
            final String content, final int row, final int column) {
        return new SWTBotTestCondition() {
            @Override
            public boolean test() throws Exception {
                try {
                    String cell = table.cell(row, column);
                    if( cell == null ) {
                        return false;
                    }
                    return cell.endsWith(content);
                } catch (Exception e) {
                }
                return false;
            }
        };
    }

    /**
     * Condition to check if a tracing project element has a child with the
     * specified name. A project element label may have a count suffix in the
     * format ' [n]'.
     */
    public static class ProjectElementHasChild extends DefaultCondition {

        private final SWTBotTreeItem fParentItem;
        private final String fName;
        private final String fRegex;
        private SWTBotTreeItem fItem = null;

        /**
         * Constructor.
         *
         * @param parentItem
         *            the parent item
         * @param name
         *            the child name to look for
         */
        public ProjectElementHasChild(final SWTBotTreeItem parentItem, final String name) {
            fParentItem = parentItem;
            fName = name;
            /* Project element labels may have count suffix */
            fRegex = name + "(\\s\\[(\\d)+\\])?";
        }

        @Override
        public boolean test() throws Exception {
            fParentItem.expand();
            for (SWTBotTreeItem item : fParentItem.getItems()) {
                if (item.getText().matches(fRegex)) {
                    fItem = item;
                    return true;
                }
            }
            return false;
        }

        @Override
        public String getFailureMessage() {
            return NLS.bind("No child of {0} found with name {1}", fParentItem.getText(), fName);
        }

        /**
         * Returns the matching child item if the condition returned true.
         *
         * @return the matching item
         */
        public SWTBotTreeItem getItem() {
            return fItem;
        }
    }

    /**
     * Condition to check if an editor with the specified title is opened.
     *
     * @param bot
     *            a workbench bot
     * @param title
     *            the editor title
     * @return ICondition for verification
     */
    public static ICondition isEditorOpened(final SWTWorkbenchBot bot, final String title) {
        return new SWTBotTestCondition() {
            @Override
            public boolean test() throws Exception {
                Matcher<IEditorReference> withPartName = withPartName(title);
                return !bot.editors(withPartName).isEmpty();
            }
        };
    }
}
