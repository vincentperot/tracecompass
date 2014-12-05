package org.eclipse.tracecompass.ctf.core.types;

import java.util.List;

/**
 * Compound definition is a definition with a collection of elements
 */
public interface ICompoundDefinition extends IDefinition {

    /**
     * Get the defintions, an array is a collection of definitions
     *
     * @return the definitions
     */
    public abstract List<IDefinition> getDefinitions();

}