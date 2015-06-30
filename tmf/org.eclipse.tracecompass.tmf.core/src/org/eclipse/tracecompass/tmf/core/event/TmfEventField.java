/*******************************************************************************
 * Copyright (c) 2009, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Updated as per TMF Event Model 1.0
 *   Alexandre Montplaisir - Removed Cloneable, made immutable
 *   Patrick Tasse - Remove getSubField
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

/**
 * A basic implementation of ITmfEventField.
 * <p>
 * Non-value fields are structural (i.e. used to represent the event structure
 * including optional fields) while the valued fields are actual event fields.
 *
 * @version 1.0
 * @author Francois Chouinard
 *
 * @see ITmfEvent
 * @see ITmfEventType
 */
public class TmfEventField implements ITmfEventField {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final @NonNull String fName;
    private final @Nullable Object fValue;
    private final @NonNull Map<String, ITmfEventField> fFields;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Full constructor
     *
     * @param name
     *            the event field id
     * @param value
     *            the event field value
     * @param fields
     *            the list of subfields
     * @throws IllegalArgumentException
     *             If 'name' is null, or if 'fields' has duplicate field names.
     */
    public TmfEventField(@NonNull String name, @Nullable Object value, @Nullable ITmfEventField[] fields) {
        fName = name;
        fValue = value;

        if (fields == null) {
            fFields = checkNotNull(ImmutableMap.<String, ITmfEventField> of());
        } else {
            /* Java 8 streams will make this even more simple! */
            ImmutableMap.Builder<String, ITmfEventField> mapBuilder = new ImmutableMap.Builder<>();
            for (ITmfEventField field : fields) {
                final String curName = field.getName();
                mapBuilder.put(curName, field);
            }
            fFields = checkNotNull(mapBuilder.build());
        }
    }

    /**
     * Copy constructor
     *
     * @param field the other event field
     */
    public TmfEventField(final TmfEventField field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        fName = field.fName;
        fValue = field.fValue;
        fFields = field.fFields;
    }

    // ------------------------------------------------------------------------
    // ITmfEventField
    // ------------------------------------------------------------------------

    @Override
    public String getName() {
        return fName;
    }

    @Override
    public Object getValue() {
        return fValue;
    }

    @Override
    public final Collection<String> getFieldNames() {
        return checkNotNull(fFields.keySet());
    }

    @Override
    public final Collection<ITmfEventField> getFields() {
        return checkNotNull(fFields.values());
    }

    @Override
    public ITmfEventField getField(final String... path) {
        if (path.length == 1) {
            return fFields.get(path[0]);
        }
        ITmfEventField field = this;
        for (String name : path) {
            field = field.getField(name);
            if (field == null) {
                return null;
            }
        }
        return field;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Create a root field from a list of labels.
     *
     * @param labels the list of labels
     * @return the (flat) root list
     */
    public static final ITmfEventField makeRoot(final String[] labels) {
        final ITmfEventField[] fields = new ITmfEventField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            String label = nullToEmptyString(labels[i]);
            fields[i] = new TmfEventField(label, null, null);
        }
        // Return a new root field;
        return new TmfEventField(ITmfEventField.ROOT_FIELD_ID, null, fields);
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        Object value = getValue();

        final int prime = 31;
        int result = 1;
        result = prime * result + getName().hashCode();
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        result = prime * result + fFields.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TmfEventField)) {
            return false;
        }

        final TmfEventField other = (TmfEventField) obj;

        /* Check that the field names are the same */
        if (!NonNullUtils.equalsNullable(getName(), other.getName())) {
            return false;
        }

        /* Check that the field values are the same */
        if (!valueEquals(this.getValue(), other.getValue())) {
            return false;
        }

        /* Check that sub-fields are the same */
        if (!fFields.equals(other.fFields)) {
            return false;
        }

        return true;
    }

    /**
     * Check if two Object values are equal. We have to handle the special cases
     * where values can be null, or [] arrays.
     */
    private static boolean valueEquals(@Nullable Object value1, @Nullable Object value2) {
        if ((value1 == null) && (value2 == null)) {
            return true;
        }
        if ((value1 == null) || (value2 == null)) {
            return false;
        }
        /* From here on we are sure both values are not null */

        /*
         * First we need to check if the field type is an [] array, in which
         * case equals() won't match them.
         */
        if (value1.getClass().isArray() && value2.getClass().isArray()) {
            return Arrays.equals(getArray(value1), getArray(value2));
        }
        return (value1.equals(value2));
    }

    /*
     * Construct a Object[] from any unknown-typed array.
     * Java 9 (or 10?) will save us from this mess.
     *
     * From http://stackoverflow.com/a/25309047/4227853 .
     */
    private static final Class<?>[] ARRAY_PRIMITIVE_TYPES = {
            int[].class, float[].class, double[].class, boolean[].class,
            byte[].class, short[].class, long[].class, char[].class };

    private static Object[] getArray(Object val) {
        Class<?> valKlass = val.getClass();
        Object[] outputArray = null;

        for (Class<?> arrKlass : ARRAY_PRIMITIVE_TYPES) {
            if (valKlass.isAssignableFrom(arrKlass)) {
                int arrlength = Array.getLength(val);
                outputArray = new Object[arrlength];
                for (int i = 0; i < arrlength; ++i) {
                    outputArray[i] = Array.get(val, i);
                }
                break;
            }
        }
        if (outputArray == null) {
            /* Not a primitive type array */
            outputArray = (Object[]) val;
        }

        return outputArray;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        if (fName.equals(ITmfEventField.ROOT_FIELD_ID)) {
            /*
             * If this field is a top-level "field container", we will print its
             * sub-fields directly.
             */
            appendSubFields(ret);

        } else {
            /* The field has its own values */
            ret.append(fName);
            ret.append('=');
            ret.append(fValue);

            if (!fFields.isEmpty()) {
                /*
                 * In addition to its own name/value, this field also has
                 * sub-fields.
                 */
                ret.append(" ["); //$NON-NLS-1$
                appendSubFields(ret);
                ret.append(']');
            }
        }
        return ret.toString();
    }

    private void appendSubFields(StringBuilder sb) {
        Joiner joiner = Joiner.on(", ").skipNulls(); //$NON-NLS-1$
        sb.append(joiner.join(getFields()));
    }

    @Override
    public String getFormattedValue() {
        return getValue().toString();
    }

}
