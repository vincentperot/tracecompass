/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Matthew Khouzam - Initial API and implementation
 *  Alexandre Montplaisir - Initial API and implementation, extend TmfEventField
 *  Bernd Hufmann - Add Enum field handling
 *  Geneviève Bastien - Add Struct and Variant field handling
 *  Jean-Christian Kouame - Correct handling of unsigned integer fields
 *  François Doray - Add generic array field type
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.tracecompass.ctf.core.types.ICompositeDefinition;
import org.eclipse.tracecompass.ctf.core.types.ICompoundDeclaration;
import org.eclipse.tracecompass.ctf.core.types.ICompoundDefinition;
import org.eclipse.tracecompass.ctf.core.types.IDeclaration;
import org.eclipse.tracecompass.ctf.core.types.IDefinition;
import org.eclipse.tracecompass.ctf.core.types.ISimpleDatatypeDefinition;
import org.eclipse.tracecompass.ctf.core.types.IVariantDefinition;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.ctf.core.CtfEnumPair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

/**
 * The CTF implementation of the TMF event field model
 *
 * @version 2.0
 * @author Matthew Khouzam
 * @author Alexandre Montplaisir
 */
public abstract class CtfTmfEventField extends TmfEventField {

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Standard constructor. Only to be used internally, call parseField() to
     * generate a new field object.
     *
     * @param name
     *            The name of this field
     * @param value
     *            The value of this field. Its type should match the field type.
     * @param fields
     *            The children fields. Useful for composite fields
     * @since 2.0
     */
    protected CtfTmfEventField(String name, Object value, ITmfEventField[] fields) {
        super(/* Strip the underscore from the field name if there is one */
                name.startsWith("_") ? name.substring(1) : name, //$NON-NLS-1$
                value,
                fields);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Factory method to instantiate CtfTmfEventField objects.
     *
     * @param fieldDef
     *            The CTF Definition of this event field
     * @param fieldName
     *            String The name to assign to this field
     * @return The resulting CtfTmfEventField object
     * @since 3.1
     */
    public static CtfTmfEventField parseField(IDefinition fieldDef,
            String fieldName) {
        CtfTmfEventField field = null;

        /* Determine the Definition type */
        if (fieldDef instanceof ISimpleDatatypeDefinition) {
            ISimpleDatatypeDefinition simpleDef = (ISimpleDatatypeDefinition) fieldDef;
            field = new CTFSimpleField(fieldName, simpleDef);

        } else if (fieldDef instanceof ICompoundDefinition) {
            ICompoundDefinition compoundDef = (ICompoundDefinition) fieldDef;
            IDeclaration decl = compoundDef.getDeclaration();
            if (!(decl instanceof ICompoundDeclaration)) {
                throw new IllegalArgumentException("Array definitions should only come from sequence or array declarations"); //$NON-NLS-1$
            }
            ICompoundDeclaration compoundDeclaration = (ICompoundDeclaration) decl;
            List<IDefinition> definitions = compoundDef.getDefinitions();
            if (compoundDeclaration.isInteger()) {
                /* it's a CTFIntegerArrayField */
                Builder<Long> values = ImmutableList.<Long> builder();
                Builder<String> valueNames = ImmutableList.<String> builder();
                for (int i = 0; i < definitions.size(); i++) {
                    IDefinition elem = definitions.get(i);
                    if (elem == null) {
                        break;
                    }
                    values.add(((Long) ((ISimpleDatatypeDefinition) elem).getValue()).longValue());
                    valueNames.add(((ISimpleDatatypeDefinition) elem).toString());

                }
                field = new CTFIntegerArrayField(fieldName, values.build(),
                        valueNames.build());

            } else {
                /* Arrays of elements of any other type */
                CtfTmfEventField[] elements = new CtfTmfEventField[definitions.size()];

                /* Parse the elements of the array. */
                int i = 0;
                for (IDefinition definition : definitions) {
                    CtfTmfEventField curField = CtfTmfEventField.parseField(
                            definition, fieldName + '[' + i + ']');
                    elements[i] = curField;
                    i++;
                }
                field = new CTFArrayField(fieldName, elements);
            }
        } else if (fieldDef instanceof ICompositeDefinition) {
            ICompositeDefinition strDef = (ICompositeDefinition) fieldDef;

            List<ITmfEventField> list = new ArrayList<>();
            /* Recursively parse the fields */
            for (String curFieldName : strDef.getFieldNames()) {
                list.add(CtfTmfEventField.parseField(strDef.getDefinition(curFieldName), curFieldName));
            }
            field = new CTFStructField(fieldName, list.toArray(new CtfTmfEventField[list.size()]));

        } else if (fieldDef instanceof IVariantDefinition) {
            IVariantDefinition varDef = (IVariantDefinition) fieldDef;

            String curFieldName = varDef.getCurrentFieldName();
            IDefinition curFieldDef = varDef.getCurrentField();
            if (curFieldDef != null) {
                CtfTmfEventField subField = CtfTmfEventField.parseField(curFieldDef, curFieldName);
                field = new CTFVariantField(fieldName, subField);
            } else {
                /* A safe-guard, but curFieldDef should never be null */
                field = new CTFStringField(curFieldName, ""); //$NON-NLS-1$
            }

        } else {
            /*
             * Safe-guard, to avoid null exceptions later, field is expected not
             * to be null
             */
            field = new CTFStringField(fieldName, Messages.CtfTmfEventField_UnsupportedType + fieldDef.getClass().toString());
        }
        return field;
    }

    @Override
    public String toString() {
        return getName() + '=' + getFormattedValue();
    }

}

/**
 * The CTF field implementation for integer fields.
 *
 * @author alexmont
 */
final class CTFSimpleField extends CtfTmfEventField {

    private final ISimpleDatatypeDefinition fDefinition;

    /**
     * A CTF "IntegerDefinition" can be an integer of any byte size, so in the
     * Java parser this is interpreted as a long.
     *
     * @param name
     *            The name of this field
     */
    CTFSimpleField(String name, ISimpleDatatypeDefinition def) {
        super(name, def.getValue(), null);
        fDefinition = def;
    }

    @Override
    public String getFormattedValue() {
        return fDefinition.toString();
    }

}

final class CTFStringField extends CtfTmfEventField {

    /**
     * A string field with a name and a value
     *
     * @param name
     *            the name
     * @param value
     *            the value
     */
    protected CTFStringField(String name, String value) {
        super(name, value, null);
    }

}

/**
 * CTF field implementation for arrays of integers.
 *
 * @author alexmont
 */
final class CTFIntegerArrayField extends CtfTmfEventField {

    private String fFormattedValue = null;
    private final List<String> fValueNames;
    private final List<Long> fValues;

    /**
     * Constructor for CTFIntegerArrayField.
     *
     * @param name
     *            The name of this field
     * @param longValues
     *            The array of integers (as longs) that compose this field's
     *            value
     * @param signed
     *            Are the values in the array signed or not
     */
    CTFIntegerArrayField(String name, List<Long> longValues, List<String> valueNames) {
        super(name, longValues, null);
        fValueNames = valueNames;
        fValues = longValues;

    }

    @Override
    public List<Long> getValue() {
        return fValues;
    }

    @Override
    public synchronized String getFormattedValue() {
        if (fFormattedValue == null) {
            fFormattedValue = fValueNames.toString();
        }
        return fFormattedValue;
    }

}

/**
 * CTF field implementation for arrays of arbitrary types.
 *
 * @author fdoray
 */
final class CTFArrayField extends CtfTmfEventField {

    private String fFormattedValue = null;

    /**
     * Constructor for CTFArrayField.
     *
     * @param name
     *            The name of this field
     * @param compoundDef
     *            The array elements of this field
     */
    CTFArrayField(String name, CtfTmfEventField[] compoundDef) {
        super(name, compoundDef, compoundDef);
    }

    @Override
    public CtfTmfEventField[] getValue() {
        return (CtfTmfEventField[]) super.getValue();
    }

    @Override
    public synchronized String getFormattedValue() {
        if (fFormattedValue == null) {
            List<String> strings = new ArrayList<>();
            for (CtfTmfEventField element : getValue()) {
                strings.add(element.getFormattedValue());
            }
            fFormattedValue = strings.toString();
        }
        return fFormattedValue;
    }
}

/**
 * CTF field implementation for floats.
 *
 * @author emathko
 */
final class CTFFloatField extends CtfTmfEventField {

    /**
     * Constructor for CTFFloatField.
     *
     * @param value
     *            The float value (actually a double) of this field
     * @param name
     *            The name of this field
     */
    protected CTFFloatField(String name, double value) {
        super(name, value, null);
    }

    @Override
    public Double getValue() {
        return (Double) super.getValue();
    }
}

/**
 * The CTF field implementation for Enum fields
 *
 * @author Bernd Hufmann
 */
final class CTFEnumField extends CtfTmfEventField {

    /**
     * Constructor for CTFEnumField.
     *
     * @param enumValue
     *            The Enum value consisting of a pair of Enum value name and its
     *            long value
     * @param name
     *            The name of this field
     */
    CTFEnumField(String name, CtfEnumPair enumValue) {
        super(name, new CtfEnumPair(enumValue.getFirst(),
                enumValue.getSecond()), null);
    }

    @Override
    public CtfEnumPair getValue() {
        return (CtfEnumPair) super.getValue();
    }
}

/**
 * The CTF field implementation for struct fields with sub-fields
 *
 * @author gbastien
 */
final class CTFStructField extends CtfTmfEventField {

    /**
     * Constructor for CTFStructField.
     *
     * @param fields
     *            The children of this field
     * @param name
     *            The name of this field
     */
    CTFStructField(String name, CtfTmfEventField[] fields) {
        super(name, fields, fields);
    }

    @Override
    public CtfTmfEventField[] getValue() {
        return (CtfTmfEventField[]) super.getValue();
    }

    @Override
    public String getFormattedValue() {
        return Arrays.toString(getValue());
    }

}

/**
 * The CTF field implementation for variant fields its child
 *
 * @author gbastien
 */
final class CTFVariantField extends CtfTmfEventField {

    /**
     * Constructor for CTFVariantField.
     *
     * @param field
     *            The field selected for this variant
     * @param name
     *            The name of this field
     */
    CTFVariantField(String name, CtfTmfEventField field) {
        super(name, field, new CtfTmfEventField[] { field });
    }

    @Override
    public CtfTmfEventField getValue() {
        return (CtfTmfEventField) super.getValue();
    }

}

/* Implement other possible fields types here... */
