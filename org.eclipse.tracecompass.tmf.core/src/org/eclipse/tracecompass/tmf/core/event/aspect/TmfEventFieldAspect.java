/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Patrick Tasse - Support subfield array
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event.aspect;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;

/**
 * Event aspect that resolves to a root event field, or one of its subfields.
 *
 * When used, the subfield pattern is slash-prefixed and slash-separated, and
 * the backslash character is used to escape an uninterpreted slash.
 *
 * @author Alexandre Montplaisir
 */
public class TmfEventFieldAspect implements ITmfEventAspect {

    private static final char SLASH = '/';
    private static final char BACKSLASH = '\\';

    private final String fAspectName;
    private final IRootField fRootField;
    private final @Nullable String fField;
    private final @Nullable String[] fFieldArray;

    /**
     * Interface for the root field resolver
     */
    public interface IRootField {
        /**
         * Returns the root event field for this aspect. Implementations must
         * override to provide a specific event member but should not assume the
         * event is of any specific type.
         *
         * @param event
         *            The event to process
         * @return the root event field
         */
        public @Nullable ITmfEventField getRootField(ITmfEvent event);
    }

    /**
     * Constructor
     *
     * @param aspectName
     *            The name of the aspect. Should be localized.
     * @param fieldName
     *            The field name or subfield pattern to resolve the event, or
     *            null to use the root field. Should *not* be localized!
     * @param rootField
     *            The root field resolver object
     */
    public TmfEventFieldAspect(String aspectName, @Nullable String fieldName, IRootField rootField) {
        fAspectName = aspectName;
        fField = fieldName;
        fFieldArray = getFieldArray(fieldName);
        fRootField = rootField;
    }

    /**
     * Get the field name or subfield pattern to resolve the event, or null if
     * the root field is used.
     *
     * @return the field name, subfield pattern, or null
     */
    public @Nullable String getField() {
        return fField;
    }

    /**
     * Create a new event aspect with the specified field name or subfield
     * pattern, relative to the root field. The new aspect is final, meaning it
     * can no longer be narrowed further to another subfield.
     *
     * @param aspectName
     *            The name of the aspect. Should be localized.
     * @param fieldName
     *            The field name or subfield pattern to resolve the event.
     * @return a new aspect instance
     */
    public ITmfEventAspect createAspect(final String aspectName, final String fieldName) {
        return new ITmfEventAspect() {

            private final @Nullable String[] fieldArray = getFieldArray(fieldName);

            @Override
            public String getName() {
                return aspectName;
            }

            @Override
            public String getHelpText() {
                return EMPTY_STRING;
            }

            @Override
            public @Nullable Object resolve(ITmfEvent event) {
                ITmfEventField root = fRootField.getRootField(event);
                if (root == null) {
                    return null;
                }
                ITmfEventField field;
                if (fieldArray == null) {
                    field = root.getField(fieldName);
                } else {
                    field = root.getSubField(fieldArray);
                }
                if (field == null) {
                    return null;
                }
                return field.getValue();
            }
        };
    }

    /**
     * Create a new instance of the aspect for the specified field name or
     * subfield pattern, relative to the root field, or null for the root
     * field.
     *
     * @param fieldName
     *            The field name or subfield pattern to resolve the event, or
     *            null.
     * @return a new aspect instance
     */
    public TmfEventFieldAspect forField(@Nullable String fieldName) {
        return new TmfEventFieldAspect(fAspectName, fieldName, fRootField);
    }

    @Override
    public String getName() {
        return fAspectName;
    }

    @Override
    public String getHelpText() {
        return EMPTY_STRING;
    }

    @Override
    public @Nullable Object resolve(ITmfEvent event) {
        ITmfEventField root = fRootField.getRootField(event);
        if (root == null) {
            return null;
        }
        ITmfEventField field;
        if (fField == null) {
            return root;
        } else if (fFieldArray == null) {
            field = root.getField(fField);
        } else {
            field = root.getSubField(fFieldArray);
        }
        if (field == null) {
            return null;
        }
        return field.getValue();
    }

    // ------------------------------------------------------------------------
    // hashCode/equals
    // Typically we want identical field aspects to be merged together.
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + fAspectName.hashCode();
        String field = fField;
        result = prime * result + (field == null ? 0 : field.hashCode());
        return result;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!this.getClass().equals(obj.getClass())) {
            return false;
        }
        TmfEventFieldAspect other = (TmfEventFieldAspect) obj;
        if (!fAspectName.equals(other.fAspectName)) {
            return false;
        }
        String field = fField;
        if (field == null) {
            if (other.fField != null) {
                return false;
            }
        } else if (!field.equals(other.fField)) {
            return false;
        }
        return true;
    }

    private static @Nullable String[] getFieldArray(@Nullable String field) {

        if (field == null || field.isEmpty() || field.charAt(0) != SLASH) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        List<String> list = new ArrayList<>();

        // We start at 1 since the first character is a slash that we want to
        // ignore.
        for (int i = 1; i < field.length(); i++) {
            char charAt = field.charAt(i);
            if (charAt == SLASH) {
                // char is slash. Cut here.
                list.add(sb.toString());
                sb = new StringBuilder();
            } else if ((charAt == BACKSLASH) && (i < field.length() - 1) && (field.charAt(i + 1) == SLASH)) {
                // Uninterpreted slash. Add it.
                sb.append(SLASH);
                i++;
            } else {
                // Any other character. Add.
                sb.append(charAt);
            }
        }

        // Last block. Add it to list.
        list.add(sb.toString());

        // Transform to array
        String[] array = new String[list.size()];
        list.toArray(array);

        return array;
    }
}
