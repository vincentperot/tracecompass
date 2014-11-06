/*******************************************************************************
 * Copyright (c) 2014 Ecole Polytechnique
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Florian Wininger - Initial implementation
 ******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This file defines all name in the XML Structure for the State Provider
 *
 * @author Florian Wininger
 * @noimplement This interface only contains static defines
 */
@SuppressWarnings({ "javadoc", "nls" })
@NonNullByDefault
public interface TmfXmlStrings {

    /* XML generic Element attribute names */
    static final String VALUE = "value";
    static final String NAME = "name";
    static final String VERSION = "version";
    static final String TYPE = "type";

    /* XML header element */
    static final String HEAD = "head";
    static final String TRACETYPE = "traceType";
    static final String ID = "id";
    static final String LABEL = "label";
    static final String ANALYSIS = "analysis";

    /* XML String */
    static final String NULL = "";
    static final String WILDCARD = "*";
    static final String VARIABLE_PREFIX = "$";
    static final String COLOR = "color";
    static final String COLOR_PREFIX = "#";

    /* XML Element Name */
    static final String STATE_PROVIDER = "stateProvider";
    static final String DEFINED_VALUE = "definedValue";
    static final String LOCATION = "location";
    static final String EVENT_HANDLER = "eventHandler";
    static final String STATE_ATTRIBUTE = "stateAttribute";
    static final String STATE_VALUE = "stateValue";
    static final String STATE_CHANGE = "stateChange";
    static final String ELEMENT_FIELD = "field";

    /* XML Condition strings */
    static final String IF = "if";
    static final String CONDITION = "condition";
    static final String THEN = "then";
    static final String ELSE = "else";

    /* XML event handler strings */
    static final String HANDLER_EVENT_NAME = "eventName";

    /* XML constant for Type of Attribute and Value */
    static final String TYPE_NULL = "null";
    static final String TYPE_CONSTANT = "constant";
    static final String EVENT_FIELD = "eventField";
    static final String TYPE_LOCATION = "location";
    static final String TYPE_QUERY = "query";
    static final String TYPE_SELF = "self";
    static final String TYPE_INT = "int";
    static final String TYPE_LONG = "long";
    static final String TYPE_STRING = "string";
    static final String TYPE_EVENT_NAME = "eventName";
    static final String TYPE_DELETE = "delete";
    static final String INCREMENT = "increment";
    static final String FORCED_TYPE = "forcedType";
    static final String ATTRIBUTE_STACK = "stack";
    static final String STACK_POP = "pop";
    static final String STACK_PUSH = "push";
    static final String STACK_PEEK = "peek";
    static final String CPU = "cpu";

    /**
     * @since 1.2
     */
    static final String TIMESTAMP = "timestamp";

    /* Operator type */
    static final String NOT = "not";
    static final String AND = "and";
    static final String OR = "or";

    static final String OPERATOR = "operator";

    /* Comparison/Condition operator types */
    static final String EQ = "eq";
    static final String NE = "ne";
    static final String GE = "ge";
    static final String GT = "gt";
    static final String LE = "le";
    static final String LT = "lt";

}