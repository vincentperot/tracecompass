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

package org.eclipse.tracecompass.analysis.os.linux.core.concepts;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.concept.IEventConceptVisitor;

/**
 * Visitor interface for the kernel scheduler concepts.
 *
 * TODO When java 8 arrives, have default implementations of nothing for those
 * methods to avoid having to implement them all in classes that need only some
 * of them.
 *
 * @author Geneviève Bastien
 */
public interface ISchedKernelConceptVisitor extends IEventConceptVisitor {

    /**
     * Visit a thread switch concept
     *
     * @param concept
     * @param event
     */
    void visit(ISchedKernelConcepts.ISchedSwitchConcept concept, ITmfEvent event);

    /**
     * Visit a scheduler wakeup concept
     *
     * @param concept
     * @param event
     */
    void visit(ISchedKernelConcepts.ISchedWakeupConcept concept, ITmfEvent event);

}
