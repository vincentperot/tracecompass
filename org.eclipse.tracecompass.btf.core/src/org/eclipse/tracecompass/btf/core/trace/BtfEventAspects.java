/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Alexandre Montplaisir - Update to new Event Table API
 *******************************************************************************/

package org.eclipse.tracecompass.btf.core.trace;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.btf.core.event.BtfEvent;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;

import com.google.common.collect.ImmutableList;

/**
 * Columns to use in the BTF event table
 *
 * @author Alexandre Montplaisir
 */
public final class BtfEventAspects {

    private BtfEventAspects() {}

    private static final @NonNull Iterable<ITmfEventAspect> BTF_ASPECTS =
            NonNullUtils.checkNotNull(ImmutableList.of(
                    ITmfEventAspect.BaseAspects.TIMESTAMP,
                    new BtfSourceAspect(),
                    ITmfEventAspect.BaseAspects.CONTENTS.createAspect(BtfColumnNames.SOURCE_INSTANCE.toString(), BtfColumnNames.SOURCE_INSTANCE.toString()),
                    ITmfEventAspect.BaseAspects.EVENT_TYPE,
                    new BtfTargetAspect(),
                    ITmfEventAspect.BaseAspects.CONTENTS.createAspect(BtfColumnNames.TARGET_INSTANCE.toString(), BtfColumnNames.TARGET_INSTANCE.toString()),
                    ITmfEventAspect.BaseAspects.CONTENTS.createAspect(BtfColumnNames.EVENT.toString(), BtfColumnNames.EVENT.toString()),
                    ITmfEventAspect.BaseAspects.CONTENTS.createAspect(BtfColumnNames.NOTES.toString(), BtfColumnNames.NOTES.toString())
                    ));

    /**
     * The "source" aspect, whose value comes from {@link ITmfEvent#getSource()}
     */
    private static class BtfSourceAspect implements ITmfEventAspect {

        @Override
        public String getName() {
            return BtfColumnNames.SOURCE.toString();
        }

        @Override
        public String getHelpText() {
            return EMPTY_STRING;
        }

        @Override
        public String resolve(ITmfEvent event) {
            if (!(event instanceof BtfEvent)) {
                return EMPTY_STRING;
            }
            String ret = ((BtfEvent) event).getSource();
            return (ret == null ? EMPTY_STRING : ret);
        }
    }

    /**
     * The "target" aspect, taking its value from
     * {@link ITmfEvent#getReference()}.
     */
    private static class BtfTargetAspect implements ITmfEventAspect {

        @Override
        public String getName() {
             return BtfColumnNames.TARGET.toString();
        }

        @Override
        public String getHelpText() {
            return EMPTY_STRING;
        }

        @Override
        public String resolve(ITmfEvent event) {
            if (!(event instanceof BtfEvent)) {
                return EMPTY_STRING;
            }
            String ret = ((BtfEvent) event).getReference();
            return (ret == null ? EMPTY_STRING : ret);
        }
    }

    /**
     * Return the event aspects defined for BTF traces.
     *
     * @return The aspects
     */
    public static @NonNull Iterable<ITmfEventAspect> getAspects() {
        return BTF_ASPECTS;
    }
}