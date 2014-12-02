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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility methods to handle {@link org.eclipse.jdt.annotation.NonNull}
 * annotations.
 *
 * @author Alexandre Montplaisir
 */
@NonNullByDefault
public class NonNullUtils {

    private NonNullUtils() {
    }

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
     * Create an immutable empty list that is guaranteed to be non null
     *
     * @return the list
     */
    public static <T> List<T> nonNullEmptyList() {
        @SuppressWarnings("null")
        @NonNull
        List<T> emptyList = Collections.<T> emptyList();
        return emptyList;
    }

    /**
     * Create an immutable empty list that is guaranteed to be non null
     *
     * @return the set
     */
    public static <T> Set<T> nonNullEmptySet() {
        @SuppressWarnings("null")
        @NonNull
        Set<T> emptySet = Collections.<T> emptySet();
        return emptySet;
    }

    /**
     * Create an immutable empty list that is guaranteed to be non null
     *
     * @param <V>
     *            key type
     * @param <K>
     *            value type
     * @return the map
     */
    public static <V, K> Map<K, V> nonNullEmptyMap() {
        @SuppressWarnings("null")
        @NonNull
        Map<K, V> emptyMap = Collections.<K, V> emptyMap();
        return emptyMap;
    }

}
