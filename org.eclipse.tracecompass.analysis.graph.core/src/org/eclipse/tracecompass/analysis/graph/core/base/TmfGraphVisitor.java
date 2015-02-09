/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien and Francis Giraldeau - Initial implementation and API
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.graph.core.base;

/**
 * An empty implementation of the graph visitor
 *
 * @author Geneviève Bastien
 * @author Francis Giraldeau
 */
public class TmfGraphVisitor implements ITmfGraphVisitor {

    @Override
    public void visitHead(TmfVertex node) {

    }

    @Override
    public void visit(TmfVertex node) {

    }

    @Override
    public void visit(TmfEdge edge, boolean horizontal) {

    }

}