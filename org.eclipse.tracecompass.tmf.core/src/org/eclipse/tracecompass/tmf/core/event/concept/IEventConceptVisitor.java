/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event.concept;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * A generic event concept visitor. This interface should be sub-typed by the
 * plugins whenever new concepts are added so that a {@code visit} method may
 * exist for each concept.
 *
 * @author Geneviève Bastien
 *
 */
public interface IEventConceptVisitor {

    /**
     * Visit a concept
     *
     * @param concept
     *            The concept to visit
     * @param event
     *            The event with which to visit the concept
     */
    void visit(IEventConcept concept, ITmfEvent event);

}
