/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Yuriy Vashchuk (yvashchuk@gmail.com) - Initial API and implementation
 *       based on http://smeric.developpez.com/java/cours/xml/sax/
 *   Patrick Tasse - Refactoring
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.filter.xml;

import java.util.Stack;

import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfEventFieldAspect;
import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterAndNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterAspectNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterCompareNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterCompareNode.Type;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterContainsNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterEqualsNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterMatchesNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterOrNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterRootNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterTraceTypeNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterTreeNode;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The SAX Content Handler
 *
 * @version 1.0
 * @author Yuriy Vashchuk
 * @author Patrick Tasse
 */
public class TmfFilterContentHandler extends DefaultHandler {

    private ITmfFilterTreeNode fRoot = null;
    private Stack<ITmfFilterTreeNode> fFilterTreeStack = null;

    /**
     * The default constructor
     */
    public TmfFilterContentHandler() {
        super();
        fFilterTreeStack = new Stack<>();
    }

    /**
     * Getter of tree
     *
     * @return The builded tree
     */
    public ITmfFilterTreeNode getTree() {
        return fRoot;
    }


    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        ITmfFilterTreeNode node = null;

        if (localName.equalsIgnoreCase(TmfFilterRootNode.NODE_NAME)) {

            node = new TmfFilterRootNode();

        } else if (localName.equals(TmfFilterNode.NODE_NAME)) {

            node = new TmfFilterNode(atts.getValue(TmfFilterNode.NAME_ATTR));

        } else if (localName.equals(TmfFilterTraceTypeNode.NODE_NAME)) {

            node = new TmfFilterTraceTypeNode(null);
            String traceTypeId = atts.getValue(TmfFilterTraceTypeNode.TYPE_ATTR);
            ((TmfFilterTraceTypeNode) node).setTraceTypeId(traceTypeId);
            TraceTypeHelper helper = TmfTraceType.getTraceType(traceTypeId);
            if (helper != null) {
                ((TmfFilterTraceTypeNode) node).setTraceClass(helper.getTraceClass());
            }
            ((TmfFilterTraceTypeNode) node).setName(atts.getValue(TmfFilterTraceTypeNode.NAME_ATTR));

        } else if (localName.equals(TmfFilterAndNode.NODE_NAME)) {

            node = new TmfFilterAndNode(null);
            String value = atts.getValue(TmfFilterAndNode.NOT_ATTR);
            if (value != null && value.equalsIgnoreCase(Boolean.TRUE.toString())) {
                ((TmfFilterAndNode) node).setNot(true);
            }

        } else if (localName.equals(TmfFilterOrNode.NODE_NAME)) {

            node = new TmfFilterOrNode(null);
            String value = atts.getValue(TmfFilterOrNode.NOT_ATTR);
            if (value != null && value.equalsIgnoreCase(Boolean.TRUE.toString())) {
                ((TmfFilterOrNode) node).setNot(true);
            }

        } else if (localName.equals(TmfFilterContainsNode.NODE_NAME)) {

            node = new TmfFilterContainsNode(null);
            String value = atts.getValue(TmfFilterContainsNode.NOT_ATTR);
            if (value != null && value.equalsIgnoreCase(Boolean.TRUE.toString())) {
                ((TmfFilterContainsNode) node).setNot(true);
            }
            createEventAspect((TmfFilterAspectNode) node, atts);
            ((TmfFilterContainsNode) node).setValue(atts.getValue(TmfFilterContainsNode.VALUE_ATTR));
            value = atts.getValue(TmfFilterContainsNode.IGNORECASE_ATTR);
            if (value != null && value.equalsIgnoreCase(Boolean.TRUE.toString())) {
                ((TmfFilterContainsNode) node).setIgnoreCase(true);
            }

        } else if (localName.equals(TmfFilterEqualsNode.NODE_NAME)) {

            node = new TmfFilterEqualsNode(null);
            String value = atts.getValue(TmfFilterEqualsNode.NOT_ATTR);
            if (value != null && value.equalsIgnoreCase(Boolean.TRUE.toString())) {
                ((TmfFilterEqualsNode) node).setNot(true);
            }
            createEventAspect((TmfFilterAspectNode) node, atts);
            ((TmfFilterEqualsNode) node).setValue(atts.getValue(TmfFilterEqualsNode.VALUE_ATTR));
            value = atts.getValue(TmfFilterEqualsNode.IGNORECASE_ATTR);
            if (value != null && value.equalsIgnoreCase(Boolean.TRUE.toString())) {
                ((TmfFilterEqualsNode) node).setIgnoreCase(true);
            }

        } else if (localName.equals(TmfFilterMatchesNode.NODE_NAME)) {

            node = new TmfFilterMatchesNode(null);
            String value = atts.getValue(TmfFilterMatchesNode.NOT_ATTR);
            if (value != null && value.equalsIgnoreCase(Boolean.TRUE.toString())) {
                ((TmfFilterMatchesNode) node).setNot(true);
            }
            createEventAspect((TmfFilterAspectNode) node, atts);
            ((TmfFilterMatchesNode) node).setRegex(atts.getValue(TmfFilterMatchesNode.REGEX_ATTR));

        } else if (localName.equals(TmfFilterCompareNode.NODE_NAME)) {

            node = new TmfFilterCompareNode(null);
            String value = atts.getValue(TmfFilterCompareNode.NOT_ATTR);
            if (value != null && value.equalsIgnoreCase(Boolean.TRUE.toString())) {
                ((TmfFilterCompareNode) node).setNot(true);
            }
            createEventAspect((TmfFilterAspectNode) node, atts);
            value = atts.getValue(TmfFilterCompareNode.TYPE_ATTR);
            if (value != null) {
                ((TmfFilterCompareNode) node).setType(Type.valueOf(value));
            }
            value = atts.getValue(TmfFilterCompareNode.RESULT_ATTR);
            if (value != null) {
                if (value.equals(Integer.toString(-1))) {
                    ((TmfFilterCompareNode) node).setResult(-1);
                } else if (value.equals(Integer.toString(1))) {
                    ((TmfFilterCompareNode) node).setResult(1);
                } else {
                    ((TmfFilterCompareNode) node).setResult(0);
                }
            }
            ((TmfFilterCompareNode) node).setValue(atts.getValue(TmfFilterCompareNode.VALUE_ATTR));

        // Backward compatibility with event type filter node
        } else if (localName.equals("EVENTTYPE")) { //$NON-NLS-1$

            node = new TmfFilterTraceTypeNode(null);
            String name = atts.getValue("name"); //$NON-NLS-1$
            if (name != null) {
                String traceTypeToken[] = name.split(":", 2); //$NON-NLS-1$
                if (traceTypeToken.length == 2) {
                    String traceTypeId = TmfTraceType.getTraceTypeId(traceTypeToken[0], traceTypeToken[1]);
                    TraceTypeHelper helper = TmfTraceType.getTraceType(traceTypeId);
                    if (helper != null) {
                        ((TmfFilterTraceTypeNode) node).setTraceTypeId(helper.getTraceTypeId());
                        ((TmfFilterTraceTypeNode) node).setTraceClass(helper.getTraceClass());
                    }
                }
                ((TmfFilterTraceTypeNode) node).setName(name);
            }

        }

        fFilterTreeStack.push(node);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        ITmfFilterTreeNode node = fFilterTreeStack.pop();

        if (fFilterTreeStack.isEmpty()) {
            fRoot = node;
        } else if (fFilterTreeStack.lastElement() instanceof TmfFilterTreeNode &&
                node instanceof TmfFilterTreeNode) {
            fFilterTreeStack.lastElement().addChild(node);
        }

    }

    private static void createEventAspect(TmfFilterAspectNode node, Attributes atts) {
        String traceTypeId = atts.getValue(TmfFilterAspectNode.TRACE_TYPE_ID_ATTR);
        String name = atts.getValue(TmfFilterAspectNode.EVENT_ASPECT_ATTR);
        if (TmfFilterAspectNode.BASE_ASPECT_ID.equals(traceTypeId)) {
            for (ITmfEventAspect eventAspect : ITmfEventAspect.BASE_ASPECTS) {
                if (eventAspect.getName().equals(name)) {
                    node.setEventAspect(eventAspect);
                    node.setTraceTypeId(traceTypeId);
                    break;
                }
            }
        } else if (TmfFilterAspectNode.EVENT_FIELD_ASPECT_ID.equals(traceTypeId) && name != null) {
            ITmfEventAspect eventAspect = new TmfEventFieldAspect(name, name);
            node.setEventAspect(eventAspect);
            node.setTraceTypeId(traceTypeId);
        } else if (traceTypeId != null && name != null) {
            TraceTypeHelper helper = TmfTraceType.getTraceType(traceTypeId);
            if (helper != null) {
                for (ITmfEventAspect eventAspect : helper.getTrace().getEventAspects()) {
                    if (eventAspect.getName().equals(name)) {
                        node.setEventAspect(eventAspect);
                        node.setTraceTypeId(traceTypeId);
                        break;
                    }
                }
            }
        } else {
            // Backward compatibility with field-based filters
            String field = atts.getValue("field"); //$NON-NLS-1$
            if (field != null) {
                ITmfEventAspect eventAspect = new TmfEventFieldAspect(field, field);
                node.setEventAspect(eventAspect);
                node.setTraceTypeId(TmfFilterAspectNode.EVENT_FIELD_ASPECT_ID);
            }
        }
    }
}
