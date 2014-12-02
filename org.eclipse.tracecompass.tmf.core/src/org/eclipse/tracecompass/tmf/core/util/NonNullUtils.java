/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.util;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility methods to handle {@link org.eclipse.jdt.annotation.NonNull}
 * annotations.
 *
 * @author Alexandre Montplaisir
 */
public class NonNullUtils {

    private NonNullUtils() {}

    /**
     * Convert a potentially null string into an empty one if it is null.
     *
     * @param str
     *            The string to null-check, and convert to an empty string if
     *            null.
     * @return The non-null string
     */
    public static String nullToEmptyString(@Nullable String str) {
        if (str == null) {
            return ""; //$NON-NLS-1$
        }
        return str;
    }
}
