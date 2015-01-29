/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Philippe Sawicki (INF4990.A2010@gmail.com)   - Initial API and implementation
 *   Mathieu Denis    (mathieu.denis55@gmail.com) - Refactored code
 *******************************************************************************/
package org.eclipse.tracecompass.internal.lttng2.kernel.core.latency.analyzer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.tracecompass.internal.lttng2.kernel.core.Activator;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;

/**
 * <b><u>StackWrapper</u></b>
 * <p>
 * Stack pile.
 *
 * TODO Change the types of the HashMaps from <String,String> to <Integer,Integer>, in order to take advantage of the
 * compilation-time String.hashCode() speedup over execution-time String hash computation.
 *
 * @author Philippe Sawicki
 * @since 3.2
 */
public class StackWrapper {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Hash map of event stacks.
     */
    private Map<String, Stack<CtfTmfEvent>> fStacks = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     */
    public StackWrapper() {
        fStacks = new HashMap<>();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Adds an event to the list of events of the same type.
     * @param event
     *            The event to add to the list.
     */
    public void put(CtfTmfEvent event) {
//        String key = event.getMarkerName();
        String key = event.getType().getName();

        if (fStacks.containsKey(key)) {
            fStacks.get(key).add(event);
        } else {
            Stack<CtfTmfEvent> newStack = new Stack<>();
            newStack.add(event);
            fStacks.put(key, newStack);
        }
    }

    /**
     * Checks if the stack contains a list of events of the given type.
     * @param key
     *            The type of events to check for.
     * @return "true" if the stack contains events of the given type, "false" otherwise.
     */
    public boolean containsKey(String key) {
        return fStacks.containsKey(key);
    }

    /**
     * Returns the list of events of the given type.
     * @param key
     *            The type of events to return.
     * @return The list of events of the given type, or null.
     */
    public Stack<CtfTmfEvent> getStackOf(String key) {
        return fStacks.get(key);
    }

    /**
     * Removes the given event from the given stack list.
     * @param key
     *            The given stack type.
     * @param event
     *            The event to remove from the given stack type.
     * @return "true" if the event was removed, "false" otherwise.
     */
    public boolean removeEvent(String key, CtfTmfEvent event) {
        Stack<CtfTmfEvent> stack = fStacks.get(key);

        boolean removed = false;

        try {
            /**
             * TODO Correct this... Here, no matter what CPU or other content field, we always remove the last event
             * added to the stack. Should be something like : return stack.remove(event);
             */
            stack.pop();
            removed = true;
        } catch (Exception e) {
            Activator.getDefault().logError("Error removing Event", e); //$NON-NLS-1$
        }

        // Remove the stack from the stack list if it is empty
        if (stack.isEmpty()) {
            fStacks.remove(key);
        }

        return removed;
    }

    /**
     * Clears the stack content.
     */
    public void clear() {
        fStacks.clear();
    }

    /**
     * Prints the content of the stack to the console.
     */
    @SuppressWarnings("nls")
    public void printContent() {
        Collection<Stack<CtfTmfEvent>> values = fStacks.values();
        Iterator<Stack<CtfTmfEvent>> valueIt = values.iterator();

        Set<String> keys = fStacks.keySet();
        Iterator<String> keyIt = keys.iterator();

        while (valueIt.hasNext() && keyIt.hasNext()) {
            Stack<CtfTmfEvent> stack = valueIt.next();

            System.out.println("   " + keyIt.next() + " [" + stack.size() + "] : " + stack);
        }
    }
}