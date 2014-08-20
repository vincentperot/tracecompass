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

package org.eclipse.tracecompass.internal.ctf.core.event.types.composite;

class EventHeaderDeclarationParameters {
    private final String fIdName;
    private final int fIdSize;
    private final String fVariantName;
    private final int fVariantCount;
    private final int fHeaderMaxSize;
    private final String fTimestampName;
    private final String fCompactName;
    private final int fCompactCount;
    private final int fCompactTimestampSize;
    private final String fExtendedName;
    private final int fExtendedFieldCount;
    private final int fExtendedIdSize;
    private final int fExtendedTimestampSize;
    private final int fAlignment;

    public EventHeaderDeclarationParameters(String idName, int idSize, String variantName, int variantCount, int headerMaxSize, String timestampName, String compactName, int compactCount, int compactTimestampSize, String extendedName,
            int extendedFieldCount, int extendedIdSize, int extendedTimestampSize, int alignment) {
        fIdName = idName;
        fIdSize = idSize;
        fVariantName = variantName;
        fVariantCount = variantCount;
        fHeaderMaxSize = headerMaxSize;
        fTimestampName = timestampName;
        fCompactName = compactName;
        fCompactCount = compactCount;
        fCompactTimestampSize = compactTimestampSize;
        fExtendedName = extendedName;
        fExtendedFieldCount = extendedFieldCount;
        fExtendedIdSize = extendedIdSize;
        fExtendedTimestampSize = extendedTimestampSize;
        fAlignment = alignment;
    }

    public String getIdName() {
        return fIdName;
    }

    public int getIdSize() {
        return fIdSize;
    }

    public String getVariantName() {
        return fVariantName;
    }

    public int getVariantCount() {
        return fVariantCount;
    }

    public int getHeaderMaxSize() {
        return fHeaderMaxSize;
    }

    public String getTimestampName() {
        return fTimestampName;
    }

    public String getCompactName() {
        return fCompactName;
    }

    public int getCompactCount() {
        return fCompactCount;
    }

    public int getCompactTimestampSize() {
        return fCompactTimestampSize;
    }

    public String getExtendedName() {
        return fExtendedName;
    }

    public int getExtendedFieldCount() {
        return fExtendedFieldCount;
    }

    public int getExtendedIdSize() {
        return fExtendedIdSize;
    }

    public int getExtendedTimestampSize() {
        return fExtendedTimestampSize;
    }

    public int getAlignment() {
        return fAlignment;
    }

}