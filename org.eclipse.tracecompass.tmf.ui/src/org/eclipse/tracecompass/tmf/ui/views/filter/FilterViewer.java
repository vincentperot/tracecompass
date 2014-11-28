/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Xavier Raynaud - add cut/copy/paste/dnd support
 *   Vincent Perot - Add subfield filtering
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.filter;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;
import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterAndNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterCompareNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterCompareNode.Type;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterContainsNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterEqualsNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterEventTypeNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterMatchesNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterOrNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterRootNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterTreeNode;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition.OutputColumn;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtEvent;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTxtTraceDefinition;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlEvent;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;

class FilterViewer extends Composite {

    private static final String SEP = " : "; //$NON-NLS-1$

    private TreeViewer fViewer;

    private Composite fComposite;
    private MenuManager fMenuManager;

    public FilterViewer(Composite parent, int style) {
        super(parent, style);

        setLayout(new FillLayout());
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        setLayoutData(gd);

        final SashForm sash = new SashForm(this, SWT.HORIZONTAL);

        // Create the tree viewer to display the filter tree
        fViewer = new TreeViewer(sash, SWT.NONE);
        fViewer.setContentProvider(new FilterTreeContentProvider());
        fViewer.setLabelProvider(new FilterTreeLabelProvider());
        fViewer.setInput(new TmfFilterRootNode());

        // Create the empty filter node properties panel
        fComposite = new Composite(sash, SWT.NONE);
        GridLayout gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        fComposite.setLayout(gl);

        createContextMenu();

        fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if (!(event.getSelection().isEmpty()) && event.getSelection() instanceof IStructuredSelection) {
                    // Update the filter node properties panel to the selection
                    IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                    ITmfFilterTreeNode node = (ITmfFilterTreeNode) selection.getFirstElement();
                    updateFilterNodeComposite(node);
                    // Highlight the selection's children
                    highlightTreeItems(fViewer.getTree().getSelection()[0].getItems());
                } else {
                    updateFilterNodeComposite(null);
                }
            }
        });

        fViewer.getTree().addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                TmfFilterTreeNode root = (TmfFilterTreeNode) fViewer.getInput();
                if (root == null || root.getChildrenCount() == 0) {
                    e.gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
                    e.gc.drawText(Messages.FilterViewer_EmptyTreeHintText, 5, 0);
                }
            }
        });

        int operations = DND.DROP_MOVE | DND.DROP_COPY;
        DragSource dragSource = new org.eclipse.swt.dnd.DragSource(fViewer.getTree(), operations);
        dragSource.setTransfer(new Transfer[] { LocalSelectionTransfer.getTransfer() });
        dragSource.addDragListener(new FilterDragSourceAdapter(this));
        DropTarget dropTarget = new DropTarget(fViewer.getTree(), operations);
        dropTarget.setTransfer(new Transfer[] { LocalSelectionTransfer.getTransfer() });
        dropTarget.addDropListener(new FilterDropTargetAdapter(this));
    }

    /**
     * Create the context menu for the tree viewer
     */
    private void createContextMenu() {
        // Adds root context menu
        fMenuManager = new MenuManager();
        fMenuManager.setRemoveAllWhenShown(true);
        fMenuManager.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                fillContextMenu(manager);
            }
        });

        // Context
        Menu contextMenu = fMenuManager.createContextMenu(fViewer.getTree());

        // Publish it
        fViewer.getTree().setMenu(contextMenu);
    }

    public MenuManager getMenuManager() {
        return fMenuManager;
    }

    /**
     * Fill the context menu for the tree viewer.
     *
     * @param manager
     *            The menu manager
     */
    protected void fillContextMenu(IMenuManager manager) {
        final ISelection selection = fViewer.getSelection();
        ITmfFilterTreeNode filterTreeNode = null;
        if (selection instanceof StructuredSelection) {
            Object element = ((StructuredSelection) selection).getFirstElement();
            if (element instanceof ITmfFilterTreeNode) {
                filterTreeNode = (ITmfFilterTreeNode) element;
            }
        }

        if (filterTreeNode != null) {
            fillContextMenuForNode(filterTreeNode, manager);
        }
        manager.add(new Separator("delete")); //$NON-NLS-1$
        manager.add(new Separator("edit")); //$NON-NLS-1$

        if (fViewer.getInput() instanceof TmfFilterRootNode || filterTreeNode == null) {
            manager.add(new Separator());
            ITmfFilterTreeNode root = (ITmfFilterTreeNode) fViewer.getInput();
            fillContextMenuForNode(root, manager);
        }
    }

    /**
     * Fill the context menu with the valid children of the provided node
     *
     * @param node
     *            The target node
     * @param manager
     *            The menu manager
     */
    protected void fillContextMenuForNode(final ITmfFilterTreeNode node, IMenuManager manager) {
        for (final String child : node.getValidChildren()) {
            final Action action = new Action() {
                @Override
                public void run() {
                    ITmfFilterTreeNode newNode = null;
                    if (TmfFilterNode.NODE_NAME.equals(child)) {
                        newNode = new TmfFilterNode(node, ""); //$NON-NLS-1$
                    } else if (TmfFilterEventTypeNode.NODE_NAME.equals(child)) {
                        newNode = new TmfFilterEventTypeNode(node);
                    } else if (TmfFilterAndNode.NODE_NAME.equals(child)) {
                        newNode = new TmfFilterAndNode(node);
                    } else if (TmfFilterOrNode.NODE_NAME.equals(child)) {
                        newNode = new TmfFilterOrNode(node);
                    } else if (TmfFilterContainsNode.NODE_NAME.equals(child)) {
                        newNode = new TmfFilterContainsNode(node);
                    } else if (TmfFilterEqualsNode.NODE_NAME.equals(child)) {
                        newNode = new TmfFilterEqualsNode(node);
                    } else if (TmfFilterMatchesNode.NODE_NAME.equals(child)) {
                        newNode = new TmfFilterMatchesNode(node);
                    } else if (TmfFilterCompareNode.NODE_NAME.equals(child)) {
                        newNode = new TmfFilterCompareNode(node);
                    }
                    if (newNode != null) {
                        fViewer.refresh();
                        fViewer.setSelection(new StructuredSelection(newNode), true);
                    }
                }
            };
            if (TmfFilterNode.NODE_NAME.equals(child)) {
                action.setText(Messages.FilterViewer_NewPrefix + " " + child); //$NON-NLS-1$
            } else {
                action.setText(child);
            }
            manager.add(action);
        }
    }

    /**
     * Create the appropriate filter node properties composite
     */
    private void updateFilterNodeComposite(ITmfFilterTreeNode node) {
        for (Control control : fComposite.getChildren()) {
            control.dispose();
        }

        if (node instanceof TmfFilterNode) {
            new FilterNodeComposite(fComposite, (TmfFilterNode) node);
        } else if (node instanceof TmfFilterEventTypeNode) {
            new FilterEventTypeNodeComposite(fComposite, (TmfFilterEventTypeNode) node);
        } else if (node instanceof TmfFilterAndNode) {
            new FilterAndNodeComposite(fComposite, (TmfFilterAndNode) node);
        } else if (node instanceof TmfFilterOrNode) {
            new FilterOrNodeComposite(fComposite, (TmfFilterOrNode) node);
        } else if (node instanceof TmfFilterContainsNode) {
            new FilterContainsNodeComposite(fComposite, (TmfFilterContainsNode) node);
        } else if (node instanceof TmfFilterEqualsNode) {
            new FilterEqualsNodeComposite(fComposite, (TmfFilterEqualsNode) node);
        } else if (node instanceof TmfFilterMatchesNode) {
            new FilterMatchesNodeComposite(fComposite, (TmfFilterMatchesNode) node);
        } else if (node instanceof TmfFilterCompareNode) {
            new FilterCompareNodeComposite(fComposite, (TmfFilterCompareNode) node);
        } else {
            new FilterBaseNodeComposite(fComposite);
        }
        fComposite.layout();
    }

    /**
     * Highlight the provided tree items
     */
    private void highlightTreeItems(TreeItem[] items) {
        resetTreeItems(fViewer.getTree().getItems());
        for (TreeItem item : items) {
            item.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
        }

    }

    /**
     * Reset the provided tree items (remove highlight)
     */
    private void resetTreeItems(TreeItem[] items) {
        for (TreeItem item : items) {
            item.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
            resetTreeItems(item.getItems());
        }
    }

    public void setInput(ITmfFilterTreeNode root) {
        fViewer.setInput(root);
        fViewer.expandAll();

        updateFilterNodeComposite(null);
    }

    public ITmfFilterTreeNode getInput() {
        return (ITmfFilterTreeNode) fViewer.getInput();
    }

    public void refresh() {
        fViewer.refresh();
    }

    public void setSelection(ITmfFilterTreeNode node, boolean reveal) {
        fViewer.setSelection(new StructuredSelection(node), reveal);
    }

    public void setSelection(ITmfFilterTreeNode node) {
        fViewer.setSelection(new StructuredSelection(node));
    }

    public ITmfFilterTreeNode getSelection() {
        final ISelection selection = fViewer.getSelection();
        ITmfFilterTreeNode filterTreeNode = null;
        if (selection instanceof StructuredSelection) {
            Object element = ((StructuredSelection) selection).getFirstElement();
            if (element instanceof ITmfFilterTreeNode) {
                filterTreeNode = (ITmfFilterTreeNode) element;
            }
        }

        final ITmfFilterTreeNode selectedNode = filterTreeNode;
        return selectedNode;
    }

    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        fViewer.addSelectionChangedListener(listener);
    }

    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        fViewer.removeSelectionChangedListener(listener);
    }

    /**
     * Gets the TreeViewer displaying filters
     *
     * @return a {@link TreeViewer}
     */
    TreeViewer getTreeViewer() {
        return fViewer;
    }

    private class FilterBaseNodeComposite extends Composite {
        private TmfFilterTreeNode fNode;
        private Button fNotButton;
        private Combo fFieldCombo;
        private Text fValueText;
        private Button fIgnoreCaseButton;

        TmfFilterTreeNode getNode() {
            return fNode;
        }

        void setNode(TmfFilterTreeNode node) {
            fNode = node;
        }

        FilterBaseNodeComposite(Composite parent) {
            super(parent, SWT.NONE);
            setLayout(new GridLayout(2, false));
            setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        }

        protected Map<String, Object> getEventsTypeMap() {
            Map<String, Object> eventsTypeMap = new TreeMap<>();
            for (IConfigurationElement ce : TmfTraceType.getTypeElements()) {
                String categoryPrefix = ""; //$NON-NLS-1$
                String categoryId = ce.getAttribute(TmfTraceType.CATEGORY_ATTR);
                if (categoryId != null) {
                    categoryPrefix = TmfTraceType.getCategoryName(categoryId) + SEP;
                }
                String text = categoryPrefix + ce.getAttribute(TmfTraceType.NAME_ATTR);
                eventsTypeMap.put(text, ce);
            }
            for (CustomTxtTraceDefinition def : CustomTxtTraceDefinition.loadAll()) {
                String text = def.categoryName + SEP + def.definitionName;
                eventsTypeMap.put(text, def);
            }
            for (CustomXmlTraceDefinition def : CustomXmlTraceDefinition.loadAll()) {
                String text = def.categoryName + SEP + def.definitionName;
                eventsTypeMap.put(text, def);
            }
            return eventsTypeMap;
        }

        protected String[] getFieldsList(ITmfFilterTreeNode node) {
            ArrayList<String> fieldsList = new ArrayList<>();
            ITmfFilterTreeNode curNode = node;
            while (curNode != null) {
                if (curNode instanceof TmfFilterEventTypeNode) {
                    TmfFilterEventTypeNode eventTypeNode = (TmfFilterEventTypeNode) curNode;
                    for (IConfigurationElement ce : TmfTraceType.getTypeElements()) {
                        if (ce.getAttribute(TmfTraceType.EVENT_TYPE_ATTR).equals(eventTypeNode.getEventType())) {
                            try {
                                ITmfEvent event = (ITmfEvent) ce.createExecutableExtension(TmfTraceType.EVENT_TYPE_ATTR);
                                ITmfEventType eventType = event.getType();
                                if (eventType != null) {
                                    for (String field : eventType.getRootField().getFieldNames()) {
                                        fieldsList.add(field);
                                    }
                                }
                            } catch (CoreException e) {
                            }
                            if (fieldsList.size() == 0) {
                                fieldsList.add(ITmfEvent.EVENT_FIELD_TIMESTAMP);
                                fieldsList.add(ITmfEvent.EVENT_FIELD_SOURCE);
                                fieldsList.add(ITmfEvent.EVENT_FIELD_TYPE);
                                fieldsList.add(ITmfEvent.EVENT_FIELD_REFERENCE);
                                fieldsList.add(ITmfEvent.EVENT_FIELD_CONTENT);
                            }
                            return fieldsList.toArray(new String[0]);
                        }
                    }
                    if (eventTypeNode.getEventType() != null && eventTypeNode.getEventType().startsWith(CustomTxtEvent.class.getCanonicalName())) {
                        for (CustomTxtTraceDefinition def : CustomTxtTraceDefinition.loadAll()) {
                            if (eventTypeNode.getEventType().equals(CustomTxtEvent.class.getCanonicalName() + ':' + def.categoryName + ':' + def.definitionName)) {
                                for (OutputColumn output : def.outputs) {
                                    fieldsList.add(output.name);
                                }
                                return fieldsList.toArray(new String[0]);
                            }
                        }
                    }
                    if (eventTypeNode.getEventType() != null && eventTypeNode.getEventType().startsWith(CustomXmlEvent.class.getCanonicalName())) {
                        for (CustomXmlTraceDefinition def : CustomXmlTraceDefinition.loadAll()) {
                            if (eventTypeNode.getEventType().equals(CustomXmlEvent.class.getCanonicalName() + ':' + def.categoryName + ':' + def.definitionName)) {
                                for (OutputColumn output : def.outputs) {
                                    fieldsList.add(output.name);
                                }
                                return fieldsList.toArray(new String[0]);
                            }
                        }
                    }
                }
                curNode = curNode.getParent();
            }

            fieldsList.add(Messages.FilterViewer_CommonCategory);
            fieldsList.add(ITmfEvent.EVENT_FIELD_TIMESTAMP);
            fieldsList.add(ITmfEvent.EVENT_FIELD_SOURCE);
            fieldsList.add(ITmfEvent.EVENT_FIELD_TYPE);
            fieldsList.add(ITmfEvent.EVENT_FIELD_REFERENCE);
            fieldsList.add(ITmfEvent.EVENT_FIELD_CONTENT);
            fieldsList.add(""); //$NON-NLS-1$

            for (Entry<String, Object> eventTypeEntry : getEventsTypeMap().entrySet()) {
                Object value = eventTypeEntry.getValue();
                if (value instanceof IConfigurationElement) {
                    IConfigurationElement ce = (IConfigurationElement) value;
                    try {
                        ITmfEvent event = (ITmfEvent) ce.createExecutableExtension(TmfTraceType.EVENT_TYPE_ATTR);
                        ITmfEventType eventType = event.getType();
                        if (eventType != null && eventType.getFieldNames().size() > 0) {
                            String categoryId = ce.getAttribute(TmfTraceType.CATEGORY_ATTR);
                            if (categoryId != null) {
                                fieldsList.add('[' + TmfTraceType.getCategoryName(categoryId) + SEP
                                        + ce.getAttribute(TmfTraceType.NAME_ATTR) + ']');
                            } else {
                                fieldsList.add('[' + ce.getAttribute(TmfTraceType.NAME_ATTR) + ']');
                            }
                            for (String field : eventType.getFieldNames()) {
                                fieldsList.add(field);
                            }
                            fieldsList.add(""); //$NON-NLS-1$
                        }
                    } catch (CoreException e) {
                    }
                } else if (value instanceof CustomTraceDefinition) {
                    CustomTraceDefinition def = (CustomTraceDefinition) value;
                    if (def.outputs.size() > 0) {
                        fieldsList.add('[' + def.categoryName + SEP + def.definitionName + ']');
                        for (OutputColumn output : def.outputs) {
                            fieldsList.add(output.name);
                        }
                        fieldsList.add(""); //$NON-NLS-1$
                    }
                }
            }
            return fieldsList.toArray(new String[0]);
        }

        void createLabel(String text) {
            Label label = new Label(this, SWT.NONE);
            label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            label.setText(text);
        }

        void createNotButton() {
            createLabel(Messages.FilterViewer_NotLabel);

            fNotButton = new Button(this, SWT.CHECK);
            fNotButton.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            fNotButton.setSelection(getNode().isNot());
            fNotButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    getNode().setNot(fNotButton.getSelection());
                    fViewer.refresh(getNode());
                }
            });
        }

        void createFieldCombo() {
            createLabel(Messages.FilterViewer_FieldLabel);

            fFieldCombo = new Combo(this, SWT.DROP_DOWN);
            fFieldCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            fFieldCombo.setItems(getFieldsList(getNode()));
            fFieldCombo.setToolTipText(Messages.FilterViewer_Subfilter_ToolTip);
            if (getNode().getField() != null) {
                fFieldCombo.setText(getNode().getField());
            }
            fFieldCombo.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    getNode().setField(fFieldCombo.getText());
                    fViewer.refresh(getNode());
                }
            });
        }

        void createValueText(TmfFilterTreeNode node) {
            createLabel(Messages.FilterViewer_ValueLabel);

            fValueText = new Text(this, SWT.BORDER);
            fValueText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            if (node.getValue() != null && node.getValue().length() > 0) {
                fValueText.setText(node.getValue());
            } else {
                fValueText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
                fValueText.setText(Messages.FilterViewer_ValueHint);
            }
            fValueText.addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    if (getNode().getValue() == null || getNode().getValue().length() == 0) {
                        fValueText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
                        fValueText.setText(Messages.FilterViewer_ValueHint);
                    }
                }

                @Override
                public void focusGained(FocusEvent e) {
                    if (fValueText.getForeground().equals(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY))) {
                        fValueText.setText(""); //$NON-NLS-1$
                    }
                    fValueText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
                }
            });
            fValueText.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    if (!fValueText.getForeground().equals(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY))) {
                        getNode().setValue(fValueText.getText());
                        fViewer.refresh(getNode());
                    }
                }
            });
        }

        Button getIgnoreCaseButton() {
            return fIgnoreCaseButton;
        }

        void setIgnoreCaseButton(Button ignoreCaseButton) {
            fIgnoreCaseButton = ignoreCaseButton;
        }
    }

    private class FilterNodeComposite extends FilterBaseNodeComposite {
        Text fNameText;

        FilterNodeComposite(Composite parent, TmfFilterNode node) {
            super(parent);
            setNode(node);

            Label label = new Label(this, SWT.NONE);
            label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            label.setText(Messages.FilterViewer_NameLabel);

            fNameText = new Text(this, SWT.BORDER);
            fNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            if (node.getFilterName() != null && node.getFilterName().length() > 0) {
                fNameText.setText(node.getFilterName());
            } else {
                fNameText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
                fNameText.setText(Messages.FilterViewer_FilterNameHint);
            }
            fNameText.addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    if (getNode().getFilterName() == null || getNode().getFilterName().length() == 0) {
                        fNameText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
                        fNameText.setText(Messages.FilterViewer_FilterNameHint);
                    }
                }

                @Override
                public void focusGained(FocusEvent e) {
                    if (fNameText.getForeground().equals(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY))) {
                        fNameText.setText(""); //$NON-NLS-1$
                    }
                    fNameText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
                }
            });
            fNameText.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    if (!fNameText.getForeground().equals(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY))) {
                        getNode().setFilterName(fNameText.getText());
                        fViewer.refresh(getNode());
                    }
                }
            });
        }

    }

    private class FilterEventTypeNodeComposite extends FilterBaseNodeComposite {
        Combo fTypeCombo;
        Map<String, Object> fEventsTypeMap;

        FilterEventTypeNodeComposite(Composite parent, TmfFilterEventTypeNode node) {
            super(parent);
            setNode(node);
            fEventsTypeMap = getEventsTypeMap();

            Label label = new Label(this, SWT.NONE);
            label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            label.setText(Messages.FilterViewer_TypeLabel);

            fTypeCombo = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
            fTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            fTypeCombo.setItems(fEventsTypeMap.keySet().toArray(new String[0]));
            if (getNode().getEventType() != null) {
                fTypeCombo.setText(getNode().getName());
            }
            fTypeCombo.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    for (Entry<String, Object> eventTypeEntry : fEventsTypeMap.entrySet()) {
                        if (eventTypeEntry.getKey().equals(fTypeCombo.getText())) {
                            Object value = eventTypeEntry.getValue();
                            if (value instanceof IConfigurationElement) {
                                IConfigurationElement ce = (IConfigurationElement) value;
                                getNode().setEventType(ce.getAttribute(TmfTraceType.EVENT_TYPE_ATTR));
                                String categoryId = ce.getAttribute(TmfTraceType.CATEGORY_ATTR);
                                if (categoryId != null) {
                                    getNode().setName(TmfTraceType.getCategoryName(categoryId) + SEP
                                            + ce.getAttribute(TmfTraceType.NAME_ATTR));
                                } else {
                                    getNode().setName(ce.getAttribute(TmfTraceType.NAME_ATTR));
                                }
                            } else if (value instanceof CustomTxtTraceDefinition) {
                                CustomTxtTraceDefinition def = (CustomTxtTraceDefinition) value;
                                getNode().setEventType(CustomTxtEvent.class.getCanonicalName() + ':' + def.categoryName + ':' + def.definitionName);
                                getNode().setName(def.categoryName + SEP + def.definitionName);
                            } else if (value instanceof CustomXmlTraceDefinition) {
                                CustomXmlTraceDefinition def = (CustomXmlTraceDefinition) value;
                                getNode().setEventType(CustomXmlEvent.class.getCanonicalName() + ':' + def.categoryName + ':' + def.definitionName);
                                getNode().setName(def.categoryName + SEP + def.definitionName);
                            }
                            fViewer.refresh(getNode());
                            break;
                        }
                    }
                }
            });
        }
    }

    private class FilterAndNodeComposite extends FilterBaseNodeComposite {

        FilterAndNodeComposite(Composite parent, TmfFilterAndNode node) {
            super(parent);
            setNode(node);
            createNotButton();
        }
    }

    private class FilterOrNodeComposite extends FilterBaseNodeComposite {

        FilterOrNodeComposite(Composite parent, TmfFilterOrNode node) {
            super(parent);
            setNode(node);
            createNotButton();
        }

    }

    private class FilterContainsNodeComposite extends FilterBaseNodeComposite {

        FilterContainsNodeComposite(Composite parent, TmfFilterContainsNode node) {
            super(parent);
            setNode(node);

            createNotButton();

            createFieldCombo();

            createValueText(node);

            Label label = new Label(this, SWT.NONE);
            label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

            setIgnoreCaseButton(new Button(this, SWT.CHECK));
            getIgnoreCaseButton().setSelection(getNode().isIgnoreCase());
            getIgnoreCaseButton().setText(Messages.FilterViewer_IgnoreCaseButtonText);
            getIgnoreCaseButton().setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            getIgnoreCaseButton().addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    getNode().setIgnoreCase(getIgnoreCaseButton().getSelection());
                    fViewer.refresh(getNode());
                }
            });
        }

        @Override
        TmfFilterContainsNode getNode() {
            return (TmfFilterContainsNode) super.getNode();
        }

    }

    private class FilterEqualsNodeComposite extends FilterBaseNodeComposite {

        FilterEqualsNodeComposite(Composite parent, TmfFilterEqualsNode node) {
            super(parent);
            setNode(node);

            createNotButton();

            createFieldCombo();

            createValueText(node);

            Label label = new Label(this, SWT.NONE);
            label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

            setIgnoreCaseButton(new Button(this, SWT.CHECK));
            getIgnoreCaseButton().setSelection(getNode().isIgnoreCase());
            getIgnoreCaseButton().setText(Messages.FilterViewer_IgnoreCaseButtonText);
            getIgnoreCaseButton().setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            getIgnoreCaseButton().addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    getNode().setIgnoreCase(getIgnoreCaseButton().getSelection());
                    fViewer.refresh(getNode());
                }
            });
        }

        @Override
        TmfFilterEqualsNode getNode() {
            return (TmfFilterEqualsNode) super.getNode();
        }
    }

    private class FilterMatchesNodeComposite extends FilterBaseNodeComposite {
        Text fRegexText;

        FilterMatchesNodeComposite(Composite parent, TmfFilterMatchesNode node) {
            super(parent);
            setNode(node);

            createNotButton();

            createFieldCombo();

            Label label = new Label(this, SWT.NONE);
            label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            label.setText(Messages.FilterViewer_RegexLabel);

            fRegexText = new Text(this, SWT.BORDER);
            fRegexText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            if (node.getRegex() != null && node.getRegex().length() > 0) {
                fRegexText.setText(node.getRegex());
            } else {
                fRegexText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
                fRegexText.setText(Messages.FilterViewer_RegexHint);
            }
            fRegexText.addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    if (getNode().getRegex() == null || getNode().getRegex().length() == 0) {
                        fRegexText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
                        fRegexText.setText(Messages.FilterViewer_RegexHint);
                    }
                }

                @Override
                public void focusGained(FocusEvent e) {
                    if (fRegexText.getForeground().equals(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY))) {
                        fRegexText.setText(""); //$NON-NLS-1$
                    }
                    fRegexText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
                }
            });
            fRegexText.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    if (!fRegexText.getForeground().equals(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY))) {
                        getNode().setRegex(fRegexText.getText());
                        fViewer.refresh(getNode());
                    }
                }
            });
        }

        @Override
        TmfFilterMatchesNode getNode() {
            return (TmfFilterMatchesNode) super.getNode();
        }
    }

    private class FilterCompareNodeComposite extends FilterBaseNodeComposite {

        Button fLTButton;
        Button fEQButton;
        Button fGTButton;
        Button fNumButton;
        Button fAlphaButton;
        Button fTimestampButton;

        FilterCompareNodeComposite(Composite parent, TmfFilterCompareNode node) {
            super(parent);
            setNode(node);

            Label label = new Label(this, SWT.NONE);
            label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            label.setText(Messages.FilterViewer_NotLabel);

            createNotButton();

            createFieldCombo();

            label = new Label(this, SWT.NONE);
            label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            label.setText(Messages.FilterViewer_ResultLabel);

            Composite resultGroup = new Composite(this, SWT.NONE);
            GridLayout rggl = new GridLayout(3, true);
            rggl.marginHeight = 0;
            rggl.marginWidth = 0;
            resultGroup.setLayout(rggl);
            resultGroup.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

            fLTButton = new Button(resultGroup, SWT.RADIO);
            fLTButton.setSelection(getNode().getResult() < 0);
            fLTButton.setText("<"); //$NON-NLS-1$
            fLTButton.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            fLTButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (fLTButton.getSelection()) {
                        getNode().setResult(-1);
                    }
                    fViewer.refresh(getNode());
                }
            });

            fEQButton = new Button(resultGroup, SWT.RADIO);
            fEQButton.setSelection(getNode().getResult() == 0);
            fEQButton.setText("="); //$NON-NLS-1$
            fEQButton.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            fEQButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (fEQButton.getSelection()) {
                        getNode().setResult(0);
                    }
                    fViewer.refresh(getNode());
                }
            });

            fGTButton = new Button(resultGroup, SWT.RADIO);
            fGTButton.setSelection(getNode().getResult() > 0);
            fGTButton.setText(">"); //$NON-NLS-1$
            fGTButton.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            fGTButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (fGTButton.getSelection()) {
                        getNode().setResult(1);
                    }
                    fViewer.refresh(getNode());
                }
            });

            label = new Label(this, SWT.NONE);
            label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            label.setText(Messages.FilterViewer_TypeLabel);

            Composite typeGroup = new Composite(this, SWT.NONE);
            GridLayout tggl = new GridLayout(3, false);
            tggl.marginHeight = 0;
            tggl.marginWidth = 0;
            typeGroup.setLayout(tggl);
            typeGroup.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

            fNumButton = new Button(typeGroup, SWT.RADIO);
            fNumButton.setSelection(getNode().getType() == Type.NUM);
            fNumButton.setText(Messages.FilterViewer_NumButtonText);
            fNumButton.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            fNumButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (fNumButton.getSelection()) {
                        getNode().setType(Type.NUM);
                    }
                    fViewer.refresh(getNode());
                }
            });

            fAlphaButton = new Button(typeGroup, SWT.RADIO);
            fAlphaButton.setSelection(getNode().getType() == Type.ALPHA);
            fAlphaButton.setText(Messages.FilterViewer_AlphaButtonText);
            fAlphaButton.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            fAlphaButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (fAlphaButton.getSelection()) {
                        getNode().setType(Type.ALPHA);
                    }
                    fViewer.refresh(getNode());
                }
            });

            fTimestampButton = new Button(typeGroup, SWT.RADIO);
            fTimestampButton.setSelection(getNode().getType() == Type.TIMESTAMP);
            fTimestampButton.setText(Messages.FilterViewer_TimestampButtonText);
            fTimestampButton.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            fTimestampButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (fTimestampButton.getSelection()) {
                        getNode().setType(Type.TIMESTAMP);
                    }
                    fViewer.refresh(getNode());
                }
            });

            createValueText(node);
        }

        @Override
        TmfFilterCompareNode getNode() {
            return (TmfFilterCompareNode) super.getNode();
        }
    }

}
