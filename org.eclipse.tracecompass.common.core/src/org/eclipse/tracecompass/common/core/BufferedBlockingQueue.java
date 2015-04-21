/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.common.core;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.jdt.annotation.NonNull;

/**
 * BufferedBlockingQueue is a queue that works in "segments" to allow smaller
 * items being put inside while maintaining performance
 *
 * @param <T>
 *            The datatype of the element contained by the queue
 * @since 1.0
 */
public class BufferedBlockingQueue<T> {

    private Queue<T> fRxBuffer;
    private Queue<T> fTxBuffer;
    private final ReadWriteLock fRwl = new ReentrantReadWriteLock();
    private final Lock fReadLock = NonNullUtils.checkNotNull(fRwl.readLock());
    private final Lock fWriteLock = NonNullUtils.checkNotNull(fRwl.writeLock());
    /** Condition for waiting takes */
    private final Condition fNotEmpty = NonNullUtils.checkNotNull(fWriteLock.newCondition());
    /** Condition for waiting puts */
    private final Condition fNotFull = NonNullUtils.checkNotNull(fWriteLock.newCondition());

    private final Queue<Queue<T>> fChannel;
    private final int fSegmentSize;
    private final int fNumSegments;

    /**
     * Constructor
     *
     * @param numSegments
     *            number of segments
     * @param segmentSize
     *            the size of a segment
     */
    public BufferedBlockingQueue(int numSegments, int segmentSize) {
        fNumSegments = numSegments;
        fSegmentSize = segmentSize;
        fChannel = new ArrayDeque<>(numSegments);
        fTxBuffer = new ArrayDeque<>(segmentSize);
        fRxBuffer = new ArrayDeque<>(0);
    }

    /**
     * Put an interval into the queue
     *
     * @param element
     *            the interval
     * @throws InterruptedException
     *             an interruption
     */
    public void put(T element) throws InterruptedException {
        @NonNull
        T elem = NonNullUtils.checkNotNull(element);
        fWriteLock.lockInterruptibly();
        try {
            fTxBuffer.add(elem);
            if (fTxBuffer.size() >= fSegmentSize) {
                internalEnqueue();
                fTxBuffer = new ArrayDeque<>(fSegmentSize);
            }
        } finally {
            fWriteLock.unlock();
        }
    }

    /**
     * Inefficient way to flush the buffer
     *
     * @throws InterruptedException
     *             interrupted
     */
    public void flush() throws InterruptedException {
        fWriteLock.lockInterruptibly();
        try {
            if (!fTxBuffer.isEmpty()) {
                internalEnqueue();
                fTxBuffer = new ArrayDeque<>(fSegmentSize);
            }
        } finally {
            fWriteLock.unlock();
        }
    }

    private void internalEnqueue() throws InterruptedException {
        while (fChannel.size() >= fNumSegments) {
            fNotFull.await();
        }
        fChannel.add(fTxBuffer);
        fNotEmpty.signal();
    }

    /**
     * Get an interval from the queue
     *
     * @return the interval
     * @throws InterruptedException
     *             an interruption
     */
    public T take() throws InterruptedException {
        fWriteLock.lockInterruptibly();
        try {
            if (fRxBuffer.isEmpty()) {
                internalDeque();
            }
            return NonNullUtils.checkNotNull(fRxBuffer.poll());
        } finally {
            fWriteLock.unlock();
        }

    }

    private void internalDeque() throws InterruptedException {
        while (fChannel.isEmpty()) {
            fNotEmpty.await();
        }
        fRxBuffer = NonNullUtils.checkNotNull(fChannel.poll());
        fNotFull.signal();
    }

    /**
     * Get the data in the order it was placed
     *
     * @return All the data in the queue at this point, it is a shallow copied
     *         collection so the data is the same but the collection will not
     *         change. This will leak resources if not released.
     * @throws InterruptedException
     *             if interrupted
     */
    protected Queue<T> getContent() throws InterruptedException {
        fReadLock.lockInterruptibly();
        try {
            Queue<T> retVal = new ArrayDeque<>();
            retVal.addAll(fRxBuffer);
            for (Queue<T> elements : fChannel) {
                retVal.addAll(elements);
            }
            retVal.addAll(fTxBuffer);
            return retVal;
        } finally {
            fReadLock.unlock();
        }
    }

    /**
     * is the queue empty?
     *
     * @return true if yes, false if no
     * @throws InterruptedException
     *             interrupted
     */
    public boolean isEmpty() throws InterruptedException {
        fReadLock.lockInterruptibly();
        try {
            return fRxBuffer.isEmpty() && fTxBuffer.isEmpty() && fChannel.isEmpty();
        } finally {
            fReadLock.unlock();
        }
    }
}
