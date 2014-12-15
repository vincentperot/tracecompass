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

package org.eclipse.tracecompass.common.core;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility methods to handle {@link org.eclipse.jdt.annotation.NonNull}
 * annotations.
 *
 * @author Alexandre Montplaisir
 */
public final class NonNullUtils {

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

    /**
     * Returns a non null String for a potentially null object. This methods
     * calls the {@link Object#toString()} if the object is not null or returns
     * an empty string otherwise.
     *
     * @param obj
     *            A {@link Nullable} object that we want converted as string
     * @return A non-null String
     */
    public static String toString(@Nullable Object obj) {
        if (obj == null) {
            return ""; //$NON-NLS-1$
        }
        return nullToEmptyString(obj.toString());
    }

    /**
     * Convert a non-annotated object reference to a {@link NonNull} one.
     *
     * If the reference is actually null, a {@link NullPointerException} is
     * thrown. This is usually more desirable than letting an unwanted null
     * reference go through and fail much later.
     *
     * @param obj
     *            The object that is supposed to be non-null
     * @return A {@link NonNull} reference to the same object
     * @throws NullPointerException
     *             If the reference was actually null
     */
    public static <T> T checkNotNull(@Nullable T obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        return obj;
    }
}
