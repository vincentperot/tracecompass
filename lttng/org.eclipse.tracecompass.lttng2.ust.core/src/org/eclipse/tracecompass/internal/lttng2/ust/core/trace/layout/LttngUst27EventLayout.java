/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.core.trace.layout;

/**
 * Updated event definitions for LTTng-UST 2.7.
 *
 * @author Alexandre Montplaisir
 */
@SuppressWarnings({"javadoc", "nls"})
public class LttngUst27EventLayout extends LttngUst20EventLayout {

    /**
     * Constructor
     */
    protected LttngUst27EventLayout() {}

    private static final LttngUst27EventLayout INSTANCE = new LttngUst27EventLayout();

    /**
     * Get a singleton instance.
     *
     * @return The instance
     */
    public static LttngUst27EventLayout getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------
    // UST Statedump events
    // ------------------------------------------------------------------------

    public String eventStatedumpStart() {
        return "lttng_ust_statedump:start";
    }

    public String eventStatedumpEnd() {
        return "lttng_ust_statedump:end";
    }

    public String eventStatedumpSoInfo() {
        return "lttng_ust_statedump:soinfo";
    }

    // ------------------------------------------------------------------------
    // Event names used in liblttng-ust-libc-wrapper
    // They are now prefixed with "lttng_*"
    // ------------------------------------------------------------------------

    @Override
    public String eventLibcMalloc() {
        return "lttng_ust_libc:malloc";
    }

    @Override
    public String eventLibcCalloc() {
        return "lttng_ust_libc:calloc";
    }

    @Override
    public String eventLibcRealloc() {
        return "lttng_ust_libc:realloc";
    }

    @Override
    public String eventLibcFree() {
        return "lttng_ust_libc:free";
    }

    @Override
    public String eventLibcMemalign() {
        return "lttng_ust_libc:memalign";
    }

    @Override
    public String eventLibcPosixMemalign() {
        return "lttng_ust_libc:posix_memalign";
    }

    // ------------------------------------------------------------------------
    // Field names used by statedump and liblttng-ust-dl events
    // (only supported for 2.7+ traces)
    // ------------------------------------------------------------------------

    public String fieldBaddr() {
        return "baddr";
    }

    public String fieldSopath() {
        return "sopath";
    }

    public String fieldMemsz() {
        return "memsz";
    }

    public String fieldBuildId() {
        return "build_id";
    }
}
