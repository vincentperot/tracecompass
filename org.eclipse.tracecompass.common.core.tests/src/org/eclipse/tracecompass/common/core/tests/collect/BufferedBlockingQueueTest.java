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

package org.eclipse.tracecompass.common.core.tests.collect;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.common.core.collect.BufferedBlockingQueue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

/**
 * Test suite for the {@link BufferedBlockingQueue}
 */
public class BufferedBlockingQueueTest {

    /** Timeout the tests after 2 minutes */
    @Rule
    public TestRule timeoutRule = new Timeout(120000);

    private static final List<String> TEST_STRING = generateTestVector(40000);

    private BufferedBlockingQueue<String> fStringQueue;

    /**
     * Test setup
     */
    @Before
    public void init() {
        fStringQueue = new BufferedBlockingQueue<>(15, 15);
    }

    private static List<String> generateTestVector(int size) {
        ImmutableList.Builder<String> sb = new ImmutableList.Builder<>();
        for (int i = 0; i < size; i++) {
            sb.add(Integer.toString(i));
        }
        return sb.build();
    }

    /**
     * Test inserting one element and removing it.
     */
    @Test
    public void testSingleInsertion() {
        String element = "x";
        fStringQueue.put(element);
        fStringQueue.flushInputBuffer();

        String out = fStringQueue.take();
        assertEquals(element, out);
    }

    /**
     * Test insertion of elements that fit into the input buffer.
     */
    @Test
    public void testSimpleInsertion() {
        Iterable<String> string = generateTestVector(10);
        for (String elem : string) {
            fStringQueue.put(NonNullUtils.checkNotNull(elem));
        }
        fStringQueue.flushInputBuffer();

        StringBuilder sb = new StringBuilder();
        while (!fStringQueue.isEmpty()) {
            sb.append(fStringQueue.take());
        }
        StringBuilder expected = new StringBuilder();
        for (String element : string) {
            expected.append(element);
        }

        assertEquals(expected.toString(), sb.toString());
    }

    /**
     * Test insertion of elements that will require more than one input buffer.
     */
    @Test
    public void testLargeInsertion() {
        Iterable<String> string = generateTestVector(100);
        StringBuilder expected = new StringBuilder();
        for (String elem : string) {
            fStringQueue.put(NonNullUtils.checkNotNull(elem));
            expected.append(elem);
        }
        fStringQueue.flushInputBuffer();

        StringBuilder sb = new StringBuilder();
        while (!fStringQueue.isEmpty()) {
            sb.append(fStringQueue.take());
        }
        assertEquals(expected.toString(), sb.toString());
    }

    /**
     * Test the state of the {@link BufferedBlockingQueue#isEmpty()} method at
     * various moments.
     */
    @Test
    public void testIsEmpty() {
        BufferedBlockingQueue<String> stringQueue = new BufferedBlockingQueue<>(15, 15);
        assertTrue(stringQueue.isEmpty());

        stringQueue.put("Hello");
        assertFalse(stringQueue.isEmpty());

        stringQueue.flushInputBuffer();
        assertFalse(stringQueue.isEmpty());

        stringQueue.flushInputBuffer();
        assertFalse(stringQueue.isEmpty());

        stringQueue.flushInputBuffer();
        stringQueue.take();
        assertTrue(stringQueue.isEmpty());

        stringQueue.flushInputBuffer();
        assertTrue(stringQueue.isEmpty());
    }

    /**
     * Write random data in and read it, several times.
     */
    @Test
    public void testOddInsertions() {
        BufferedBlockingQueue<Object> objectQueue = new BufferedBlockingQueue<>(15, 15);
        LinkedList<Object> expectedValues = new LinkedList<>();
        Random rnd = new Random();
        rnd.setSeed(123);

        for (int i = 0; i < 10; i++) {
            /*
             * The queue's total size is 225 (15x15). We must make sure to not
             * fill it up here!
             */
            for (int j = 0; j < 50; j++) {
                Integer testInt = NonNullUtils.checkNotNull(rnd.nextInt());
                Long testLong = NonNullUtils.checkNotNull(rnd.nextLong());
                Double testDouble = NonNullUtils.checkNotNull(rnd.nextDouble());
                Double testGaussian = NonNullUtils.checkNotNull(rnd.nextGaussian());

                expectedValues.add(testInt);
                expectedValues.add(testLong);
                expectedValues.add(testDouble);
                expectedValues.add(testGaussian);
                objectQueue.put(testInt);
                objectQueue.put(testLong);
                objectQueue.put(testDouble);
                objectQueue.put(testGaussian);
            }
            objectQueue.flushInputBuffer();

            while (!expectedValues.isEmpty()) {
                Object expected = expectedValues.removeFirst();
                Object actual = objectQueue.take();
                assertEquals(expected, actual);
            }
        }
    }

    /**
     * Read with a producer and a consumer
     *
     * @throws InterruptedException
     *             The test was interrupted
     */
    @Test
    public void testMultiThread() throws InterruptedException {
        /* A character not found in the test string */
        final String lastElement = "%";

        Thread producer = new Thread() {
            @Override
            public void run() {
                for (String c : TEST_STRING) {
                    fStringQueue.put(NonNullUtils.checkNotNull(c));
                }
                fStringQueue.put(lastElement);
                fStringQueue.flushInputBuffer();
            }
        };
        producer.start();

        Thread consumer = new Thread() {
            @Override
            public void run() {
                String s = fStringQueue.take();
                while (!s.equals(lastElement)) {
                    s = fStringQueue.take();
                }
            }
        };
        consumer.start();

        consumer.join();
        producer.join();
    }

    /**

     * Test the contents returned by {@link BufferedBlockingQueue#iterator()}.
     *
     * The test is sequential, because the iterator has no guarantee wrt to its
     * contents when run concurrently.
     */
    @Test
    public void testIteratorContents() {
        Deque<String> expected = new LinkedList<>();

        /* Iterator should be empty initially */
        assertFalse(fStringQueue.iterator().hasNext());

        /* Insert the first 50 elements */
        for (int i = 0; i < 50; i++) {
            String element = TEST_STRING.get(i);
            fStringQueue.put(NonNullUtils.checkNotNull(element));
            expected.addFirst(element);
        }
        LinkedList<String> actual = new LinkedList<>();
        Iterators.addAll(actual, fStringQueue.iterator());
        assertSameElements(expected, actual);

        /*
         * Insert more elements, flush the input buffer (should not affect the
         * iteration).
         */
        for (int i = 50; i < 60; i++) {
            String element = TEST_STRING.get(i);
            fStringQueue.put(NonNullUtils.nullToEmptyString(element));
            fStringQueue.flushInputBuffer();
            expected.add(element);
        }
        actual = new LinkedList<>();
        Iterators.addAll(actual, fStringQueue.iterator());
        assertSameElements(expected, actual);

        /* Consume the 30 last elements from the queue */
        for (int i = 0; i < 30; i++) {
            fStringQueue.take();
            expected.removeLast();
        }
        actual = new LinkedList<>();
        Iterators.addAll(actual, fStringQueue.iterator());
        assertSameElements(expected, actual);

        /* Now empty the queue */
        while (!fStringQueue.isEmpty()) {
            fStringQueue.take();
            expected.removeLast();
        }
        assertFalse(fStringQueue.iterator().hasNext());
    }


    /**
     * Read with a 2 producers and a consumer
     *
     * @throws InterruptedException
     *             The test was interrupted
     */
    @Test
    public void testMultiThread2Producers() throws InterruptedException {
        /* A character not found in the test string */
        final List<String> testString = generateTestVector(1000);
        testNProducers(2, testString);
    }

    /**
     * Read with a many producers and a consumer
     *
     * @throws InterruptedException
     *             The test was interrupted
     */
    @Test
    public void testMultiThreadManyProducers() throws InterruptedException {
        final int NUM_PRODUCERS = 9;
        /* A character not found in the test string */
        final List<String> testString = generateTestVector(150);
        testNProducers(NUM_PRODUCERS, testString);
    }

    private void testNProducers(final int NUM_PRODUCERS, final List<String> testString) throws InterruptedException {
        final String lastElement = "!";
        Thread.currentThread().setName("Unit test");
        List<Thread> producers = new ArrayList<>();
        for (int i = 0; i < NUM_PRODUCERS; i++) {
            Thread producer = new Thread() {
                @Override
                public void run() {
                    for (String c : testString) {
                        fStringQueue.put(NonNullUtils.checkNotNull(c));
                    }
                }
            };
            producer.setName("Producer " + i);
            producer.start();
            producers.add(producer);
        }

        final List<String> actual = new ArrayList<>();
        Thread consumer = new Thread() {
            @Override
            public void run() {
                String take = fStringQueue.take();
                while (!take.equals(lastElement)) {
                    actual.add(take);
                    take = fStringQueue.take();
                }
            }
        };
        consumer.setName("Consumer");
        consumer.start();
        for (Thread producer : producers) {
            producer.join();
        }
        fStringQueue.put(lastElement);
        fStringQueue.flushInputBuffer();
        consumer.join();
        assertTrue(fStringQueue.isEmpty());
        assertEquals(new HashSet<>(testString), new HashSet<>(actual));
        assertEquals(testString.size() * NUM_PRODUCERS, actual.size());
        Collection<String> multipliedList = new ArrayList<>();
        for (int i = 0; i < NUM_PRODUCERS; i++) {
            multipliedList.addAll(testString);
        }
        assertSameElements(multipliedList, actual);
    }

    /**
     * Utility method to verify that two collections contain the exact same
     * elements, not necessarily in the same iteration order.
     *
     * {@link Collection#equals} requires the iteration order to be the same,
     * which we do not want here.
     *
     * Using a {@link Set} or {@link Collection#containsAll} is not sufficient
     * either, because those will throw away duplicate elements.
     */
    private static <T> void assertSameElements(Collection<T> expected, Collection<T> actual) {
        assertEquals(HashMultiset.create(expected), HashMultiset.create(actual));
    }

    /**
     * Test iterating on the queue while a producer and a consumer threads are
     * using it. The iteration should not affect the elements taken by the
     * consumer.
     *
     * @throws InterruptedException
     *             The test was interrupted
     * @throws ExecutionException
     *             If one of the sub-threads throws an exception, which should
     *             not happen
     */
    @Test
    public void testConcurrentIteration() throws InterruptedException, ExecutionException {
        final BufferedBlockingQueue<String> queue = new BufferedBlockingQueue<>(15, 15);

        ExecutorService pool = Executors.newFixedThreadPool(3);

        final String poisonPill = "That's all folks!";

        Runnable producer = new Runnable() {
            @Override
            public void run() {
                for (String element : TEST_STRING) {
                    queue.put(nullToEmptyString(element));
                }
                queue.put(poisonPill);
                queue.flushInputBuffer();
            }
        };

        Callable<String> consumer = new Callable<String>() {
            @Override
            public String call() {
                StringBuilder sb = new StringBuilder();
                String s = queue.take();
                while (!s.equals(poisonPill)) {
                    sb.append(s);
                    s = queue.take();
                }
                return sb.toString();
            }
        };

        Runnable inquisitor = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    final Set<String> results = new HashSet<>();
                    /*
                     * The interest of this test is here: we are iterating on
                     * the queue while it is being used.
                     */
                    for (String input : queue) {
                        results.add(input);
                    }
                }
            }
        };

        pool.submit(producer);
        pool.submit(inquisitor);
        Future<String> message = pool.submit(consumer);

        pool.shutdown();
        pool.awaitTermination(2, TimeUnit.MINUTES);
        StringBuilder expected = new StringBuilder();
        for (String element : TEST_STRING) {
            expected.append(element);
        }
        assertEquals(expected.toString(), message.get());
    }
}