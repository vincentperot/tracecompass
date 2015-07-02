/*******************************************************************************
 * Copyright (c) 2010, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.parsers.custom;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventType;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition.OutputColumn;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfNanoTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Base event for custom text parsers.
 *
 * @author Patrick Tassé
 */
public class CustomEvent extends TmfEvent {

    /** Input format key */
    protected static final String TIMESTAMP_INPUT_FORMAT_KEY = "CE_TS_I_F"; //$NON-NLS-1$

    /** Empty message */
    protected static final String NO_MESSAGE = ""; //$NON-NLS-1$

    /** Replacement for the super-class' timestamp field */
    private @NonNull ITmfTimestamp customEventTimestamp;

    /** Replacement for the super-class' content field */
    private ITmfEventField customEventContent;

    /** Replacement for the super-class' type field */
    private ITmfEventType customEventType;

    /** The trace to which this event belongs */
    protected CustomTraceDefinition fDefinition;

    /** The payload data of this event, <field name, value> */
    protected Map<String, String> fData;

    private TmfEventField[] fColumnData;

    /**
     * Basic constructor.
     *
     * @param definition
     *            The trace definition to which this event belongs
     */
    public CustomEvent(CustomTraceDefinition definition) {
        super(null, ITmfContext.UNKNOWN_RANK, null, null, null);
        fDefinition = definition;
        fData = new HashMap<>();
        customEventTimestamp = TmfTimestamp.ZERO;
    }

    /**
     * Build a new CustomEvent from an existing TmfEvent.
     *
     * @param definition
     *            The trace definition to which this event belongs
     * @param other
     *            The TmfEvent to copy
     */
    public CustomEvent(CustomTraceDefinition definition, @NonNull TmfEvent other) {
        super(other);
        fDefinition = definition;
        fData = new HashMap<>();

        /* Set our overridden fields */
        customEventTimestamp = other.getTimestamp();
        customEventContent = other.getContent();
        customEventType = other.getType();
    }

    /**
     * Full constructor
     *
     * @param definition
     *            Trace definition of this event
     * @param parentTrace
     *            Parent trace object
     * @param timestamp
     *            Timestamp of this event
     * @param type
     *            Event type
     */
    public CustomEvent(CustomTraceDefinition definition, ITmfTrace parentTrace,
            ITmfTimestamp timestamp, TmfEventType type) {
        /* Do not use upstream's fields for stuff we override */
        super(parentTrace, ITmfContext.UNKNOWN_RANK, null, null, null);
        fDefinition = definition;
        fData = new HashMap<>();

        /* Set our overridden fields */
        if (timestamp == null) {
            customEventTimestamp = TmfTimestamp.ZERO;
        } else {
            customEventTimestamp = timestamp;
        }
        customEventContent = null;
        customEventType = type;
    }

    // ------------------------------------------------------------------------
    // Overridden getters
    // ------------------------------------------------------------------------

    @Override
    public ITmfTimestamp getTimestamp() {
        if (fData != null) {
            processData();
        }
        return customEventTimestamp;
    }

    @Override
    public ITmfEventField getContent() {
        return customEventContent;
    }

    @Override
    public ITmfEventType getType() {
        return customEventType;
    }

    // ------------------------------------------------------------------------
    // Setters
    // ------------------------------------------------------------------------

    /**
     * Set this event's timestamp
     *
     * @param timestamp
     *            The new timestamp
     */
    protected void setTimestamp(@NonNull ITmfTimestamp timestamp) {
        customEventTimestamp = timestamp;
    }

    /**
     * Set this event's content
     *
     * @param content
     *            The new content
     */
    protected void setContent(ITmfEventField content) {
        customEventContent = content;
    }

    /**
     * Set this event's type
     *
     * @param type
     *            The new type
     */
    protected void setType(ITmfEventType type) {
        customEventType = type;
    }

    // ------------------------------------------------------------------------
    // Other operations
    // ------------------------------------------------------------------------

    /**
     * Get the contents of an event table cell for this event's row.
     *
     * @param index
     *            The ID/index of the field to display. This corresponds to the
     *            index in the event content.
     * @return The String to display in the cell
     */
    public String getEventString(int index) {
        if (fData != null) {
            processData();
        }
        if (index < 0 || index >= fColumnData.length) {
            return ""; //$NON-NLS-1$
        }

        return fColumnData[index].getValue().toString();
    }

    private void processData() {
        String timestampString = fData.get(CustomTraceDefinition.TAG_TIMESTAMP);
        String timestampInputFormat = fData.get(TIMESTAMP_INPUT_FORMAT_KEY);
        TmfTimestamp timestamp = null;
        if (timestampInputFormat != null && timestampString != null) {
            TmfTimestampFormat timestampFormat = new TmfTimestampFormat(timestampInputFormat);
            try {
                long time = timestampFormat.parseValue(timestampString);
                timestamp = new TmfNanoTimestamp(getTrace().getTimestampTransform().transform(time));
                setTimestamp(timestamp);
            } catch (ParseException e) {
                setTimestamp(TmfTimestamp.ZERO);
            }
        } else {
            setTimestamp(TmfTimestamp.ZERO);
        }

        int i = 0;
        fColumnData = new TmfEventField[fDefinition.outputs.size()];
        for (OutputColumn outputColumn : fDefinition.outputs) {
            String value = fData.get(outputColumn.name);
            String columnName = nullToEmptyString(outputColumn.name);
            if (columnName.equals(CustomTraceDefinition.TAG_TIMESTAMP) && timestamp != null) {
                TmfTimestampFormat timestampFormat = new TmfTimestampFormat(fDefinition.timeStampOutputFormat);
                fColumnData[i++] = new TmfEventField(columnName, timestampFormat.format(timestamp.getValue()), null);
            } else {
                fColumnData[i++] = new TmfEventField(columnName, (value != null ? value : ""), null); //$NON-NLS-1$
            }
        }
        CustomEventContent curContent = (CustomEventContent) getContent();
        setContent(new CustomEventContent(curContent.getName(), curContent.getValue(), fColumnData));
        fData = null;
    }
}
