/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.segmentstore.core.treemap;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;

/**
 * Implementation of a {@link ISegmentStore} using in-memory {@link TreeMap}'s.
 * This relatively simple implementation holds everything in memory, and as such
 * cannot contain too much data.
 *
 * @param <T>
 *            The time of time range held
 *
 * @author Alexandre Montplaisir
 */
public class TreeMapStore<T extends ISegment> implements ISegmentStore<T> {

    private final TreeMultimap<Long, T> fStartTimesIndex;
    private final TreeMultimap<Long, T> fEndTimesIndex;

    private final Map<Long, T> fPositionMap;
    private long fSize;

    /**
     *Constructor
     */
    public TreeMapStore() {

        fStartTimesIndex = TreeMultimap.create(LONG_COMPARATOR, INTERVAL_START_COMPARATOR);
        fEndTimesIndex = TreeMultimap.create(LONG_COMPARATOR, INTERVAL_END_COMPARATOR);
        fPositionMap = new HashMap<>();
        fSize = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return fStartTimesIndex.values().iterator();
    }

    @Override
    public synchronized void addElement(T val) {
        fStartTimesIndex.put(Long.valueOf(val.getStartTime()), val);
        fEndTimesIndex.put(Long.valueOf(val.getStartTime()), val);
        fPositionMap.put(fSize, val);
        fSize++;
    }

    @Override
    public long getNbElements() {
        return fSize;
    }

    @Override
    public T getElementAtIndex(long index) {
        return fPositionMap.get(Long.valueOf(index));
    }

    @Override
    public Iterable<T> getIntersectingElements(long time) {
        /*
         * The intervals intersecting 't' are those whose 1) start time is
         * *lower* than 't' AND 2) end time is *higher* than 't'.
         */
        Iterable<T> matchStarts = Iterables.concat(fStartTimesIndex.asMap().headMap(time, true).values());
        Iterable<T> matchEnds = Iterables.concat(fEndTimesIndex.asMap().tailMap(time, true).values());

        return Sets.intersection(Sets.newHashSet(matchStarts), Sets.newHashSet(matchEnds));
    }

    @Override
    public void dispose() {
        fStartTimesIndex.clear();
        fEndTimesIndex.clear();
        fPositionMap.clear();
    }

    // ------------------------------------------------------------------------
    // Comparators, used for the tree maps
    // ------------------------------------------------------------------------

    private static final Comparator<Long> LONG_COMPARATOR = new Comparator<Long>() {
        @Override
        public int compare(Long o1, Long o2) {
            return Long.compare(o1, o2);
        }
    };

    private static final Comparator<ISegment> INTERVAL_START_COMPARATOR = new Comparator<ISegment>() {
        @Override
        public int compare(ISegment o1, ISegment o2) {
            return Long.compare(o1.getStartTime(), o2.getStartTime());
        }
    };

    private static final Comparator<ISegment> INTERVAL_END_COMPARATOR = new Comparator<ISegment>() {
        @Override
        public int compare(ISegment o1, ISegment o2) {
            return Long.compare(o1.getEndTime(), o2.getEndTime());
        }
    };

}
