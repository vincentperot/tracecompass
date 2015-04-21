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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

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

    private final BlockingQueue<Queue<T>> fChannel;
    private final int fSegmentSize;

    /**
     * Constructor
     *
     * @param numSegments
     *            number of segments
     * @param segmentSize
     *            the size of a segment
     */
    public BufferedBlockingQueue(int numSegments, int segmentSize) {
        fSegmentSize = segmentSize;
        fChannel = new ArrayBlockingQueue<>(numSegments);
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
    public synchronized void put(T element) throws InterruptedException {
        fTxBuffer.add(element);
        if (fTxBuffer.size() >= fSegmentSize) {
            fChannel.put(fTxBuffer);
            fTxBuffer = new ArrayDeque<>(fSegmentSize);
        }
    }

    /**
     * Get an interval from the queue
     *
     * @return the interval
     * @throws InterruptedException
     *             an interruption
     */
    public T take() throws InterruptedException {
        if (fRxBuffer.isEmpty()) {
            fRxBuffer = NonNullUtils.checkNotNull(fChannel.take());
        }
        return NonNullUtils.checkNotNull(fRxBuffer.poll());
    }

    /**
     * Get the receive buffer
     *
     * @return the receive buffer
     */
    protected Queue<T> getRxBuffer() {
        return fRxBuffer;
    }

    /**
     * Get the transmit buffer
     *
     * @return the transmit buffer
     */
    protected Queue<T> getTxBuffer() {
        return fTxBuffer;
    }

    /**
     * Get the channel buffers
     *
     * @return the channel buffers
     */
    protected Queue<Queue<T>> getChannel() {
        return fChannel;
    }

    /**
     * is the queue empty?
     *
     * @return true if yes, false if no
     */
    public synchronized boolean isEmpty() {
        return fRxBuffer.isEmpty() && fTxBuffer.isEmpty() && fChannel.isEmpty();
    }
}