package org.eclipse.tracecompass.tmf.core.event.concept;

import java.util.Collection;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * An event concept describes a concept in a trace that an event represents. The
 * concepts may be used in analysis, when many trace types are susceptible to
 * implement it.
 *
 * @author Geneviève Bastien
 */
public interface IEventConcept {

    /**
     * @return The collection of event names that represent this concept
     */
    Collection<String> getEventNames();

    /**
     * Accept a visitor for a concept.
     *
     * Note to implementers: Since there is no fixed list of concept, the
     * {@link IEventConceptVisitor} will likely have sub-interfaces that will
     * have a knowledge of the concepts it visits and concepts will know which
     * visitors belongs to them so an implementation of this method may look
     * like this:
     *
     * <pre>
     * &#064;Override
     * public void accept(IEventConceptVisitor visitor, ITmfEvent event) {
     *     if (visitor instanceof ISchedKernelConceptVisitor) {
     *         ((ISchedKernelConceptVisitor) visitor).visit(this, event);
     *     } else {
     *         visitor.visit(this, event);
     *     }
     * }
     * </pre>
     *
     * @param visitor
     * @param event
     */
    void accept(IEventConceptVisitor visitor, ITmfEvent event);

}
