/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.ctf.core.types;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.ctf.core.types.ICompositeDeclaration;

import com.google.common.collect.ImmutableList;

/**
 * Event header declaration abstract class
 *
 * @author Matthew Khouzam
 * @since 3.1
 */
@NonNullByDefault
public interface IEventHeaderDeclaration extends ICompositeDeclaration {
    /** The id of an event */
    String ID = "id"; //$NON-NLS-1$

    /** The name of a timestamp field */
    String TIMESTAMP = "timestamp"; //$NON-NLS-1$

    /** Extended header */
    String EXTENDED = "extended"; //$NON-NLS-1$

    /** Compact header (not to be confused with compact vs large) */
    String COMPACT = "compact"; //$NON-NLS-1$

    /** Name of the variant according to the spec */
    String VARIANT_NAME = "v"; //$NON-NLS-1$

    /** List of fields */
    List<String> FIELD_LIST = NonNullUtils.checkNotNull(ImmutableList.<String>of(ID, TIMESTAMP, VARIANT_NAME));
}
