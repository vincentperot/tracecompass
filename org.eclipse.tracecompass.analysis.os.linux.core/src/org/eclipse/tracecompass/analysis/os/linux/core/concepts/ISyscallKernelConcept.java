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

import org.eclipse.tracecompass.tmf.core.event.concept.IEventConcept;

/**
 * TODO Just trying to add something in here showed that
 * {@link IEventConcept#getEventNames()} is not enough in the parent interface.
 * Prefixes can be used. I leave it here for now, but the parent interface will
 * have to be updated.
 *
 * @author Geneviève Bastien
 */
public interface ISyscallKernelConcept extends IEventConcept {

}
