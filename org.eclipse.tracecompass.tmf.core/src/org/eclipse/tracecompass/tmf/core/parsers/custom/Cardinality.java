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
 *   Matthew Khouzam - Moved to own class
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.core.parsers.custom;

/**
 * Input line cardinality
 * @since 1.0
 */
public class Cardinality {

    /** Representation of infinity */
    public final static int INF = Integer.MAX_VALUE;

    /** Preset for [1, 1] */
    public final static Cardinality ONE = new Cardinality(1, 1);

    /** Preset for [1, inf] */
    public final static Cardinality ONE_OR_MORE = new Cardinality(1, INF);

    /** Preset for [0, 1] */
    public final static Cardinality ZERO_OR_ONE = new Cardinality(0, 1);

    /** Preset for [0, inf] */
    public final static Cardinality ZERO_OR_MORE = new Cardinality(0, INF);

    private final int min;
    private final int max;

    /**
     * Get the minimum cardinality
     *
     * @return the minimum cardinality
     * @since 1.0
     */
    public int getMin() {
        return min;
    }

    /**
     * Get the maximum cardinality
     *
     * @return the maximum cardinality
     * @since 1.0
     */
    public int getMax() {
        return max;
    }

    /**
     * Constructor.
     *
     * @param min
     *            Minimum
     * @param max
     *            Maximum
     */
    public Cardinality(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public String toString() {
        return "(" + (min >= 0 ? min : "?") + ',' + (max == INF ? "\u221E" : (max >= 0 ? max : "?")) + ')'; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + max;
        result = prime * result + min;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Cardinality)) {
            return false;
        }
        Cardinality other = (Cardinality) obj;
        return (this.min == other.min && this.max == other.max);
    }
}