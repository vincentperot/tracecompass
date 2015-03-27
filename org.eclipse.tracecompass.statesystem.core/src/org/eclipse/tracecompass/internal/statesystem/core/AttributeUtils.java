/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.statesystem.core;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Utility methods for attributes
 *
 * @author Patrick Tasse
 */
public class AttributeUtils {

    /**
     * Convert a full path array to a slash-separated path string. '/' and '\'
     * in attribute names are escaped by a preceding '\' in the returned string.
     *
     * @param path
     *            The full path array
     * @return The slash-separated escaped path string
     * @since 1.0
     * @see #pathStringToArray(String)
     */
    public static @NonNull String pathArrayToString(@NonNull String... path) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < path.length; i++) {
            if (i > 0) {
                builder.append('/');
            }
            if (path[i] != null) {
                /* Escape '/' and '\' in attribute name */
                String attribute = path[i].replace("\\", "\\\\").replace("/", "\\/"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                builder.append(attribute);
            }
        }
        return checkNotNull(builder.toString());
    }

    /**
     * Convert a slash-separated path string to a full path array. '/' and '\'
     * in the input string can be escaped by a preceding '\'. The attribute
     * names in the returned path array are unescaped.
     *
     * @param string
     *            The slash-separated escaped path string
     * @return The full path array
     * @since 1.0
     * @see #pathArrayToString(String...)
     */
    public static @NonNull String[] pathStringToArray(@NonNull String string) {
        List<String> attributes = new ArrayList<>();
        StringBuilder attribute = new StringBuilder();
        int i = 0;
        while (i < string.length()) {
            Character c = string.charAt(i++);
            if (c == '/') {
                attributes.add(attribute.toString());
                attribute.setLength(0);
            } else {
                if (c == '\\' && i < string.length()) {
                    c = string.charAt(i++);
                    if (c != '\\' && c != '/') {
                        /* allow '\' before unescaped character */
                        attribute.append('\\');
                    }
                }
                attribute.append(c);
            }
        }
        attributes.add(attribute.toString());
        return checkNotNull(attributes.toArray(new String[0]));
    }

}
