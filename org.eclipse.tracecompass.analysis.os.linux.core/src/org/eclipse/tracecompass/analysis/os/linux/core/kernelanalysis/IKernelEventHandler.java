package org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis;

import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Kernel event handler interface
 * @since 1.0
 */
public interface IKernelEventHandler {

    boolean handleEvent(ITmfEvent event, ITmfStateSystemBuilder ss) throws AttributeNotFoundException;

}