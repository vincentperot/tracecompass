/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.ctf.core.event.types.composite;

import org.eclipse.tracecompass.ctf.core.event.types.Declaration;
import org.eclipse.tracecompass.ctf.core.event.types.EnumDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.VariantDeclaration;

abstract class AbstactEventHeaderDeclaration extends Declaration {

    /**
     * is an event header declaration
     *
     * @param declaration
     *            the declaration
     * @param eventHeaderDeclarationParams
     *            TODO
     * @return true if the event matches
     */
    protected static boolean isEventHeaderDeclaration(
            StructDeclaration declaration,
            EventHeaderDeclarationParameters eventHeaderDeclarationParams) {
        IDeclaration iDeclaration = declaration.getFields().get(eventHeaderDeclarationParams.getIdName());
        if (!(iDeclaration instanceof EnumDeclaration)) {
            return false;
        }
        EnumDeclaration eId = (EnumDeclaration) iDeclaration;
        if (eId.getContainerType().getLength() != eventHeaderDeclarationParams.getIdSize()) {
            return false;
        }
        iDeclaration = declaration.getFields().get(eventHeaderDeclarationParams.getVariantName());

        if (!(iDeclaration instanceof VariantDeclaration)) {
            return false;
        }
        VariantDeclaration vDec = (VariantDeclaration) iDeclaration;

        if (!vDec.hasField(eventHeaderDeclarationParams.getCompactName()) || !vDec.hasField(eventHeaderDeclarationParams.getExtendedName())) {
            return false;
        }
        if (vDec.getFields().size() != eventHeaderDeclarationParams.getVariantCount()) {
            return false;
        }

        if( vDec.getFields().get(eventHeaderDeclarationParams.getCompactName()).getAlignment() != eventHeaderDeclarationParams.getAlignment()) {
            return false;
        }
        if( vDec.getFields().get(eventHeaderDeclarationParams.getExtendedName()).getAlignment() != eventHeaderDeclarationParams.getAlignment()) {
            return false;
        }

        final int maximumSize = vDec.getMaximumSize();
        final int enumMaxSize = eId.getMaximumSize();
        final int mask = (int) (declaration.getAlignment() - 1);
        if ((maximumSize + (enumMaxSize + mask) & ~mask) != eventHeaderDeclarationParams.getHeaderMaxSize()) {
            return false;
        }
        iDeclaration = vDec.getFields().get(eventHeaderDeclarationParams.getCompactName());
        if (!(iDeclaration instanceof StructDeclaration)) {
            return false;
        }
        StructDeclaration compactDec = (StructDeclaration) iDeclaration;
        if (compactDec.getFields().size() != eventHeaderDeclarationParams.getCompactCount()) {
            return false;
        }
        if (!compactDec.hasField(eventHeaderDeclarationParams.getTimestampName())) {
            return false;
        }
        iDeclaration = compactDec.getFields().get(eventHeaderDeclarationParams.getTimestampName());
        if (!(iDeclaration instanceof IntegerDeclaration)) {
            return false;
        }
        IntegerDeclaration tsDec = (IntegerDeclaration) iDeclaration;
        if (tsDec.getLength() != eventHeaderDeclarationParams.getCompactTimestampSize() || tsDec.isSigned()) {
            return false;
        }
        iDeclaration = vDec.getFields().get(eventHeaderDeclarationParams.getExtendedName());
        if (!(iDeclaration instanceof StructDeclaration)) {
            return false;
        }
        StructDeclaration extendedDec = (StructDeclaration) iDeclaration;
        if (!extendedDec.hasField(eventHeaderDeclarationParams.getTimestampName())) {
            return false;
        }
        if (extendedDec.getFields().size() != eventHeaderDeclarationParams.getExtendedFieldCount()) {
            return false;
        }
        iDeclaration = extendedDec.getFields().get(eventHeaderDeclarationParams.getTimestampName());
        if (!(iDeclaration instanceof IntegerDeclaration)) {
            return false;
        }
        tsDec = (IntegerDeclaration) iDeclaration;
        if (tsDec.getLength() != eventHeaderDeclarationParams.getExtendedTimestampSize() || tsDec.isSigned()) {
            return false;
        }
        iDeclaration = extendedDec.getFields().get(eventHeaderDeclarationParams.getIdName());
        if (!(iDeclaration instanceof IntegerDeclaration)) {
            return false;
        }
        IntegerDeclaration iId = (IntegerDeclaration) iDeclaration;
        if (iId.getLength() != eventHeaderDeclarationParams.getExtendedIdSize() || iId.isSigned()) {
            return false;
        }
        return true;
    }

}