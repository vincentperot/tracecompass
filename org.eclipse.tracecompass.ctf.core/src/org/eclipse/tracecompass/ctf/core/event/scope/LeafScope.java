/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event.scope;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A lttng specific speedup node (an element under a speedup node) of a lexical
 * scope
 *
 * @author Matthew Khouzam
 * @since 1.0
 */
@NonNullByDefault
public class LeafScope extends LexicalScope {
    /**
     * The scope constructor
     *
     * @param parent
     *            The parent node, can be null, but shouldn't
     * @param name
     *            the name of the field
     */
    public LeafScope(LexicalScope parent, String name) {
        super(parent, name);
    }

}
