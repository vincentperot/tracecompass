/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event.scope;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.base.Joiner;

/**
 * A node of a lexical scope
 *
 * @author Matthew Khouzam
 */
@NonNullByDefault
public class LexicalScope implements Comparable<LexicalScope>, ILexicalScope {
    private int hash = 0;
    private final String fName;
    private final String fPath;
    private final Map<String, ILexicalScope> fChildren = new ConcurrentHashMap<>();

    /**
     * The scope constructor
     *
     * @param parent
     *            The parent node, can be null, but shouldn't
     * @param name
     *            the name of the field
     * @since 1.0
     */
    public LexicalScope(@Nullable ILexicalScope parent, String name) {
        fName = name;
        if (parent != null) {
            @NonNull
            String pathString = checkNotNull(Joiner.on('.').skipNulls().join(parent.getPath(), parent.getName()));
            /*
             * if joiner return null, we get an NPE... so we won't assign fPath
             * to null
             */
            if (pathString.startsWith(".")) { //$NON-NLS-1$
                /*
                 * substring throws an exception or returns a string, it won't
                 * return null
                 */
                pathString = checkNotNull(pathString.substring(1));
            }
            fPath = pathString;
            parent.addChild(fName, this);
        } else {
            fPath = ""; //$NON-NLS-1$
        }
    }

    /**
     * @since 1.0
     */
    @Override
    public void addChild(String name, ILexicalScope child) {
        fChildren.put(name, child);
    }

    @Override
    public String getName() {
        return fName;
    }

    /**
     * @since 1.0
     */
    @Override
    public @Nullable ILexicalScope getChild(String name) {
        return fChildren.get(name);
    }

    /**
     * @since 1.0
     */
    @Override
    public String getPath() {
        // TODO Auto-generated method stub
        return fPath;
    }

    @Override
    public String toString() {
        return (fPath.isEmpty() ? fName : fPath + '.' + fName);
    }

    @Override
    public int compareTo(@Nullable LexicalScope other) {
        if (other == null) {
            throw new IllegalArgumentException();
        }
        int comp = fPath.compareTo(other.fPath);
        if (comp == 0) {
            return fName.compareTo(other.fName);
        }
        return comp;
    }

    @Override
    public synchronized int hashCode() {
        if (hash == 0) {
            final int prime = 31;
            hash = prime * (prime + fName.hashCode()) + fPath.hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LexicalScope other = (LexicalScope) obj;
        if (!fName.equals(other.fName)) {
            return false;
        }
        return fPath.equals(other.fPath);
    }

}
