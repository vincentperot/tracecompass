/*******************************************************************************
 * Copyright (c) 2015 Ericsson, EfficiOS Inc., and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.utils;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.statesystem.core.Activator;

import com.google.common.base.Predicate;

/**
 * A BufferedBlockingQueue is a wrapper around a {@link ArrayBlockingQueue},
 * which provides input and output "buffers", so that chunks of elements are
 * inserted into the queue, rather than individual elements.
 *
 * The API provides usual put() and take() methods which work on single
 * elements. This class abstracts the concept of chunking, as well as the
 * required locking, from the users
 *
 * The main use case is for when different threads are doing insertion and
 * removal operations. The added buffering reduces the contention between those
 * two threads.
 *
 * This class is currently optimized for a single reader and a single writer
 * thread.
 *
 * @param <T>
 *            The data type of the elements contained by the queue
 * @since 1.0
 */
public class BufferedBlockingQueue<T> {

    private final BlockingQueue<Deque<T>> fInnerQueue;
    private final ReadWriteLock fInputLock = new ReentrantReadWriteLock();
    private final ReadWriteLock fOutputLock = new ReentrantReadWriteLock();
    private final int fChunkSize;

    private Deque<T> fInputBuffer;
    private Deque<T> fOutputBuffer;

    /**
     * Constructor
     *
     * @param queueSize
     *            The size of the actual blocking queue. This is the number of
     *            *chunks* that will go in the queue.
     * @param chunkSize
     *            The size of an individual chunk.
     */
    public BufferedBlockingQueue(int queueSize, int chunkSize) {
        fInnerQueue = new ArrayBlockingQueue<>(queueSize);
        fChunkSize = chunkSize;

        fInputBuffer = new ArrayDeque<>(fChunkSize);
        /*
         * Set fOutputBuffer to something to avoid a null reference, even though
         * this particular array will never be used.
         */
        fOutputBuffer = new ArrayDeque<>(0);
    }

    /**
     * Put an element into the queue.
     *
     * This method will block the caller if the inner queue is full, waiting for
     * space to become available.
     *
     * @param element
     *            The element to insert
     */
    public void put(T element) {
        fInputLock.writeLock().lock();
        try {
            fInputBuffer.addFirst(element);
            if (fInputBuffer.size() >= fChunkSize) {
                this.flushInputBuffer();
            }
        } finally {
            fInputLock.writeLock().unlock();
        }
    }

    /**
     * Flush the current input buffer, disregarding the expected buffer size
     * limit.
     *
     * This will guarantee that an element that was inserted via the
     * {@link #put} method becomes visible to the {@link #take} method.
     *
     * This method will block if the inner queue is currently full, waiting for
     * space to become available.
     */
    public void flushInputBuffer() {
        fInputLock.writeLock().lock();
        try {
            /*
             * This call blocks if fInputBuffer is full, effectively
             * blocking the caller until elements are removed via the take()
             * method.
             */
            fInnerQueue.put(fInputBuffer);
            fInputBuffer = new ArrayDeque<>();

        } catch (InterruptedException e) {
            Activator.getDefault().logError("Buffered queue interrupted", e); //$NON-NLS-1$
        } finally {
            fInputLock.writeLock().unlock();
        }
    }

    /**
     * Retrieve an element from the queue.
     *
     * If the queue is empty, this call will block until an element is inserted.
     *
     * @return The retrieved element. It will be removed from the queue.
     */
    public T take() {
        fOutputLock.writeLock().lock();
        try {
            if (fOutputBuffer.isEmpty()) {
                /*
                 * Our read buffer is empty, take the next buffer in the queue.
                 * This call will block if the inner queue is empty.
                 */
                fOutputBuffer = checkNotNull(fInnerQueue.take());
            }
            return checkNotNull(fOutputBuffer.removeLast());
        } catch (InterruptedException e) {
            Activator.getDefault().logError("Buffered queue interrupted", e); //$NON-NLS-1$
            throw new IllegalStateException();
        } finally {
            fOutputLock.writeLock().unlock();
        }
    }

    /**
     * Check if the buffered queue currently contains a specific element. This
     * includes the inner queue as well as the input and output buffer.
     *
     * A Predicate passed in parameters is called on every element of the queue.
     * If a match is found, the element that matched the predicate is returned.
     *
     * If concurrent insertions happen while this method is executing, it is
     * possible for an element that was actually in the queue when the call was
     * made to have been removed by the {@link #take} method in the meantime.
     * However, this method guarantees that the element is either inside the
     * queue OR was removed by the {@link #take} method. No element should
     * "fall in the cracks".
     *
     * @param predicate
     *            The predicate to determine if each element matches
     * @return The element that matched the predicate, or 'null' if no such
     *         element was found.
     */
    public @Nullable T lookForMatchingElement(Predicate<T> predicate) {
        /*
         * To ensure that we do not "miss" elements, we have to look in the
         * input buffer first, then the actual blocking queue, then the output
         * buffer, in this order.
         */

        /* Look in the input buffer */
        fInputLock.readLock().lock();
        try {
            for (T elem : fInputBuffer) {
                if (predicate.apply(elem)) {
                    return elem;
                }
            }
        } finally {
            fInputLock.readLock().unlock();
        }

        /*
         * Look in the blocking queue itself. Note that ArrayBlockingQueue's
         * iterator() is thread-safe, so no need to lock the queue.
         */
        for (Collection<T> chunk : fInnerQueue) {
            for (T elem : chunk) {
                if (predicate.apply(elem)) {
                    return elem;
                }
            }
        }

        /* Look in the output buffer */
        fOutputLock.readLock().lock();
        try {
            for (T elem : fOutputBuffer) {
                if (predicate.apply(elem)) {
                    return elem;
                }
            }
        } finally {
            fOutputLock.readLock().unlock();
        }

        /* No matching element was found */
        return null;
    }
}
