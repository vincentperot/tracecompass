/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Markus Schorn - Bug 448058: Use org.eclipse.remote in favor of RSE
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.property;

import org.eclipse.tracecompass.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TargetNodeComponent;
import org.eclipse.tracecompass.tmf.ui.properties.ReadOnlyTextPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

/**
 * <p>
 * Property source implementation for the target node component.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class TargetNodePropertySource extends BasePropertySource {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The node name property ID.
     */
    public static final String TARGET_NODE_NAME_PROPERTY_ID = "target.node.name"; //$NON-NLS-1$
    /**
     * The node address property ID.
     */
    public static final String TARGET_NODE_ADDRESS_PROPERTY_ID = "target.node.address"; //$NON-NLS-1$
    /**
     * The state property ID.
     */
    public static final String TARGET_NODE_STATE_PROPERTY_ID = "target.node.state"; //$NON-NLS-1$
    /**
     * The node version property ID.
     */
    public static final String TARGET_NODE_VERSION_PROPERTY_ID = "target.node.version"; //$NON-NLS-1$

    /**
     *  The node name property name.
     */
    public static final String TARGET_NODE_NAME_PROPERTY_NAME = Messages.TraceControl_HostNamePropertyName;
    /**
     * The node address property name.
     */
    public static final String TARGET_NODE_ADDRESS_PROPERTY_NAME = Messages.TraceControl_HostAddressPropertyName;
    /**
     * The state address property name.
     */
    public static final String TARGET_NODE_STATE_PROPERTY_NAME = Messages.TraceControl_StatePropertyName;
    /**
     * The node version property name.
     */
    public static final String TARGET_NODE_VERSION_PROPERTY_NAME = Messages.TraceControl_VersionPropertyName;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The node component which this property source is for.
     */
    private final TargetNodeComponent fTargetNode;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param component - the node component
     */
    public TargetNodePropertySource(TargetNodeComponent component) {
        fTargetNode = component;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return new IPropertyDescriptor[] {
                new ReadOnlyTextPropertyDescriptor(TARGET_NODE_NAME_PROPERTY_ID, TARGET_NODE_NAME_PROPERTY_NAME),
                new ReadOnlyTextPropertyDescriptor(TARGET_NODE_ADDRESS_PROPERTY_ID, TARGET_NODE_ADDRESS_PROPERTY_NAME),
                new ReadOnlyTextPropertyDescriptor(TARGET_NODE_STATE_PROPERTY_ID, TARGET_NODE_STATE_PROPERTY_NAME),
                new ReadOnlyTextPropertyDescriptor(TARGET_NODE_VERSION_PROPERTY_ID, TARGET_NODE_VERSION_PROPERTY_NAME)};
    }

    @Override
    public Object getPropertyValue(Object id) {
        if(TARGET_NODE_NAME_PROPERTY_ID.equals(id)) {
            return fTargetNode.getName();
        }
        if (TARGET_NODE_ADDRESS_PROPERTY_ID.equals(id)) {
            return fTargetNode.getRemoteConnection().getAddress();
        }
        if (TARGET_NODE_STATE_PROPERTY_ID.equals(id)) {
            return fTargetNode.getTargetNodeState().name();
        }
        if (TARGET_NODE_VERSION_PROPERTY_ID.equals(id)) {
            return fTargetNode.getNodeVersion();
        }
        return null;
    }
}
