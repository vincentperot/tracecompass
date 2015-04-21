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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Random;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.common.core.collect.BufferedBlockingQueue;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Predicate;

/**
 * BufferedBlockingQueue test
 */
public class BufferedBlockingQueueTest {
    BufferedBlockingQueue<String> fixtureString;
    BufferedBlockingQueue<Object> fixtureObject;

    /**
     * Initialize Buffered Blocking Queue
     */
    @Before
    public void init() {
        fixtureString = new BufferedBlockingQueue<>(15, 15);
        fixtureObject = new BufferedBlockingQueue<>(15, 15);
    }

    /**
     * Null test
     *
     */
    @Test(expected = NullPointerException.class)
    public void testInsertNull() {
        fixtureObject.put(NonNullUtils.checkNotNull(null));
    }

    /**
     * Test inserting X and removing it.
     */
    @Test
    public void testSingleInsertion() {
        String reference = "x";
        for (int i = 0; i < reference.length(); i++) {
            fixtureString.put(NonNullUtils.checkNotNull(String.valueOf(reference.charAt(i))));
        }
        fixtureString.flushInputBuffer();
        StringBuilder sb = new StringBuilder();
        sb.append(fixtureString.take());
        assertEquals(reference, sb.toString());
    }

    /**
     * Test simple insertion of hello world
     */
    @Test
    public void testSimpleInsertion() {
        String reference = "Hello world!";
        for (int i = 0; i < reference.length(); i++) {
            fixtureString.put(NonNullUtils.checkNotNull(String.valueOf(reference.charAt(i))));
        }
        fixtureString.flushInputBuffer();
        StringBuilder sb = new StringBuilder();
        while (!fixtureString.isEmpty()) {
            sb.append(fixtureString.take());
        }
        assertEquals(reference, sb.toString());
    }

    /**
     * Test a large insertion
     */
    @Test
    public void testLargeInsertion() {
        String reference = testString.substring(0, 222);
        for (int i = 0; i < reference.length(); i++) {
            fixtureString.put(NonNullUtils.checkNotNull(String.valueOf(reference.charAt(i))));
        }
        fixtureString.flushInputBuffer();
        StringBuilder sb = new StringBuilder();
        while (!fixtureString.isEmpty()) {
            sb.append(fixtureString.take());
        }
        assertEquals(reference, sb.toString());
    }

    /**
     * Write random data in and read it, several times
     */
    @Test
    public void testOddInsertions() {
        ArrayList<Object> o = new ArrayList<>();
        Random rnd = new Random();

        rnd.setSeed(123);
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 50; j++) {
                @NonNull
                Integer tmpI = NonNullUtils.checkNotNull(rnd.nextInt());
                @NonNull
                Long tmpL = NonNullUtils.checkNotNull(rnd.nextLong());
                @NonNull
                Double tmpD = NonNullUtils.checkNotNull(rnd.nextDouble());
                @NonNull
                Double tmpG = NonNullUtils.checkNotNull(rnd.nextGaussian());
                o.add(tmpI);
                o.add(tmpL);
                o.add(tmpD);
                o.add(tmpG);
                fixtureObject.put(tmpI);
                fixtureObject.put(tmpL);
                fixtureObject.put(tmpD);
                fixtureObject.put(tmpG);
            }
            fixtureObject.flushInputBuffer();
            while (!o.isEmpty()) {
                Object expected = o.remove(0);
                Object actual = fixtureObject.take();
                assertEquals(expected, actual);
            }
        }
    }

    /**
     * Read with a producer and a consumer
     *
     * @throws InterruptedException
     *             the test was interrupted
     */
    @Test
    public void testMultiThread() throws InterruptedException {
        final String lastElement = "That's all folks!";
        Thread producer = new Thread() {
            @SuppressWarnings("null")
            @Override
            public void run() {
                for (int i = 0; i < testString.length(); i++) {
                    fixtureString.put(String.valueOf(testString.charAt(i)));
                }
                fixtureString.put(lastElement);
                fixtureString.flushInputBuffer();

            }
        };
        producer.start();
        final String[] message = new String[1];
        Thread consumer = new Thread() {
            @Override
            public void run() {
                StringBuilder sb = new StringBuilder();
                String s = null;
                s = fixtureString.take();
                while (!s.equals(lastElement)) {
                    sb.append(s);
                    s = fixtureString.take();
                }
                message[0] = sb.toString();
            }
        };
        consumer.start();
        consumer.join();
        producer.join();
        assertEquals(testString, message[0]);
    }

    /**
     * Test like Multithreaded with a producer and consumer but now with an
     * inquisitor checking up on the queue. A buffered blocking queue smoke test
     *
     * @throws InterruptedException
     *             the test was interrupted
     */
    @Test
    public void testMultiThreadWithInterruptions() throws InterruptedException {
        final BufferedBlockingQueue<String> isq = new BufferedBlockingQueue<>(15, 15);
        final String[] message = new String[1];
        final String poisonPill = "That's all folks!";
        final String lastElement = "END";
        final boolean[] fail = new boolean[1];
        fail[0] = false;

        Thread producer = new Thread() {
            @SuppressWarnings("null")
            @Override
            public void run() {
                for (int i = 0; i < testString.length(); i++) {
                    isq.put(String.valueOf(testString.charAt(i)));
                }
                isq.put(poisonPill);
                isq.flushInputBuffer();
            }
        };

        Thread consumer = new Thread() {
            @Override
            public void run() {
                StringBuilder sb = new StringBuilder();
                String s = null;
                s = isq.take();
                while (!s.equals(poisonPill)) {
                    sb.append(s);
                    s = isq.take();
                }
                message[0] = sb.toString();
            }
        };

        Thread inquisitor = new Thread() {
            @Override
            public void run() {
                isq.lookForMatchingElement(new Predicate<String>() {

                    @Override
                    public boolean apply(String input) {
                        boolean b = input != poisonPill;
                        if(!b) {
                            fixtureString.put(input);
                        }
                        return b;
                    }

                });
                fixtureString.put(lastElement);
                fixtureString.flushInputBuffer();
            }
        };
        Thread auditor = new Thread() {
            @Override
            public void run() {
                String val;
                val = fixtureString.take();
                while (!val.equals(lastElement) && !fail[0]) {
                    if (testString.indexOf(val) == -1) {
                        fail[0] = true;
                    }
                    val = fixtureString.take();
                }
            }
        };

        producer.setName("Producer");
        inquisitor.setName("Inquisitor");
        auditor.setName("Auditor");
        consumer.setName("Consumer");

        producer.start();
        inquisitor.start();
        consumer.start();
        auditor.start();

        producer.join();
        consumer.join();
        inquisitor.join();
        auditor.join();

        assertEquals(testString, message[0]);
        assertFalse(fail[0]);
    }

    /**
     * The EPL text is long and I think covered by epl
     */
    private static final String testString = "Eclipse Public License - v 1.0\n"
            +
            "\n"
            +
            "THE ACCOMPANYING PROGRAM IS PROVIDED UNDER THE TERMS OF THIS ECLIPSE PUBLIC LICENSE (\"AGREEMENT\"). ANY USE, REPRODUCTION OR DISTRIBUTION OF THE PROGRAM CONSTITUTES RECIPIENT\'S ACCEPTANCE OF THIS AGREEMENT.\n"
            +
            "\n"
            +
            "1. DEFINITIONS\n"
            +
            "\n"
            +
            "\"Contribution\" means:\n"
            +
            "\n"
            +
            "a) in the case of the initial Contributor, the initial code and documentation distributed under this Agreement, and\n"
            +
            "\n"
            +
            "b) in the case of each subsequent Contributor:\n"
            +
            "\n"
            +
            "i) changes to the Program, and\n"
            +
            "\n"
            +
            "ii) additions to the Program;\n"
            +
            "\n"
            +
            "where such changes and/or additions to the Program originate from and are distributed by that particular Contributor. A Contribution \'originates\' from a Contributor if it was added to the Program by such Contributor itself or anyone acting on such Contributor\'s behalf. Contributions do not include additions to the Program which: (i) are separate modules of software distributed in conjunction with the Program under their own license agreement, and (ii) are not derivative works of the Program.\n"
            +
            "\n"
            +
            "\"Contributor\" means any person or entity that distributes the Program.\n"
            +
            "\n"
            +
            "\"Licensed Patents\" mean patent claims licensable by a Contributor which are necessarily infringed by the use or sale of its Contribution alone or when combined with the Program.\n"
            +
            "\n"
            +
            "\"Program\" means the Contributions distributed in accordance with this Agreement.\n"
            +
            "\n"
            +
            "\"Recipient\" means anyone who receives the Program under this Agreement, including all Contributors.\n"
            +
            "\n"
            +
            "2. GRANT OF RIGHTS\n"
            +
            "\n"
            +
            "a) Subject to the terms of this Agreement, each Contributor hereby grants Recipient a non-exclusive, worldwide, royalty-free copyright license to reproduce, prepare derivative works of, publicly display, publicly perform, distribute and sublicense the Contribution of such Contributor, if any, and such derivative works, in source code and object code form.\n"
            +
            "\n"
            +
            "b) Subject to the terms of this Agreement, each Contributor hereby grants Recipient a non-exclusive, worldwide, royalty-free patent license under Licensed Patents to make, use, sell, offer to sell, import and otherwise transfer the Contribution of such Contributor, if any, in source code and object code form. This patent license shall apply to the combination of the Contribution and the Program if, at the time the Contribution is added by the Contributor, such addition of the Contribution causes such combination to be covered by the Licensed Patents. The patent license shall not apply to any other combinations which include the Contribution. No hardware per se is licensed hereunder.\n"
            +
            "\n"
            +
            "c) Recipient understands that although each Contributor grants the licenses to its Contributions set forth herein, no assurances are provided by any Contributor that the Program does not infringe the patent or other intellectual property rights of any other entity. Each Contributor disclaims any liability to Recipient for claims brought by any other entity based on infringement of intellectual property rights or otherwise. As a condition to exercising the rights and licenses granted hereunder, each Recipient hereby assumes sole responsibility to secure any other intellectual property rights needed, if any. For example, if a third party patent license is required to allow Recipient to distribute the Program, it is Recipient\'s responsibility to acquire that license before distributing the Program.\n"
            +
            "\n"
            +
            "d) Each Contributor represents that to its knowledge it has sufficient copyright rights in its Contribution, if any, to grant the copyright license set forth in this Agreement.\n"
            +
            "\n"
            +
            "3. REQUIREMENTS\n"
            +
            "\n"
            +
            "A Contributor may choose to distribute the Program in object code form under its own license agreement, provided that:\n"
            +
            "\n"
            +
            "a) it complies with the terms and conditions of this Agreement; and\n"
            +
            "\n"
            +
            "b) its license agreement:\n"
            +
            "\n"
            +
            "i) effectively disclaims on behalf of all Contributors all warranties and conditions, express and implied, including warranties or conditions of title and non-infringement, and implied warranties or conditions of merchantability and fitness for a particular purpose;\n"
            +
            "\n"
            +
            "ii) effectively excludes on behalf of all Contributors all liability for damages, including direct, indirect, special, incidental and consequential damages, such as lost profits;\n"
            +
            "\n"
            +
            "iii) states that any provisions which differ from this Agreement are offered by that Contributor alone and not by any other party; and\n"
            +
            "\n"
            +
            "iv) states that source code for the Program is available from such Contributor, and informs licensees how to obtain it in a reasonable manner on or through a medium customarily used for software exchange.\n"
            +
            "\n"
            +
            "When the Program is made available in source code form:\n"
            +
            "\n"
            +
            "a) it must be made available under this Agreement; and\n"
            +
            "\n"
            +
            "b) a copy of this Agreement must be included with each copy of the Program.\n"
            +
            "\n"
            +
            "Contributors may not remove or alter any copyright notices contained within the Program.\n"
            +
            "\n"
            +
            "Each Contributor must identify itself as the originator of its Contribution, if any, in a manner that reasonably allows subsequent Recipients to identify the originator of the Contribution.\n"
            +
            "\n"
            +
            "4. COMMERCIAL DISTRIBUTION\n"
            +
            "\n"
            +
            "Commercial distributors of software may accept certain responsibilities with respect to end users, business partners and the like. While this license is intended to facilitate the commercial use of the Program, the Contributor who includes the Program in a commercial product offering should do so in a manner which does not create potential liability for other Contributors. Therefore, if a Contributor includes the Program in a commercial product offering, such Contributor (\"Commercial Contributor\") hereby agrees to defend and indemnify every other Contributor (\"Indemnified Contributor\") against any losses, damages and costs (collectively \"Losses\") arising from claims, lawsuits and other legal actions brought by a third party against the Indemnified Contributor to the extent caused by the acts or omissions of such Commercial Contributor in connection with its distribution of the Program in a commercial product offering. The obligations in this section do not apply to any claims or Losses relating to any actual or alleged intellectual property infringement. In order to qualify, an Indemnified Contributor must: a) promptly notify the Commercial Contributor in writing of such claim, and b) allow the Commercial Contributor to control, and cooperate with the Commercial Contributor in, the defense and any related settlement negotiations. The Indemnified Contributor may participate in any such claim at its own expense.\n"
            +
            "\n"
            +
            "For example, a Contributor might include the Program in a commercial product offering, Product X. That Contributor is then a Commercial Contributor. If that Commercial Contributor then makes performance claims, or offers warranties related to Product X, those performance claims and warranties are such Commercial Contributor\'s responsibility alone. Under this section, the Commercial Contributor would have to defend claims against the other Contributors related to those performance claims and warranties, and if a court requires any other Contributor to pay any damages as a result, the Commercial Contributor must pay those damages.\n"
            +
            "\n"
            +
            "5. NO WARRANTY\n"
            +
            "\n"
            +
            "EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, THE PROGRAM IS PROVIDED ON AN \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER EXPRESS OR IMPLIED INCLUDING, WITHOUT LIMITATION, ANY WARRANTIES OR CONDITIONS OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Each Recipient is solely responsible for determining the appropriateness of using and distributing the Program and assumes all risks associated with its exercise of rights under this Agreement , including but not limited to the risks and costs of program errors, compliance with applicable laws, damage to or loss of data, programs or equipment, and unavailability or interruption of operations.\n"
            +
            "\n"
            +
            "6. DISCLAIMER OF LIABILITY\n"
            +
            "\n"
            +
            "EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, NEITHER RECIPIENT NOR ANY CONTRIBUTORS SHALL HAVE ANY LIABILITY FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING WITHOUT LIMITATION LOST PROFITS), HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THE PROGRAM OR THE EXERCISE OF ANY RIGHTS GRANTED HEREUNDER, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.\n"
            +
            "\n"
            +
            "7. GENERAL\n"
            +
            "\n"
            +
            "If any provision of this Agreement is invalid or unenforceable under applicable law, it shall not affect the validity or enforceability of the remainder of the terms of this Agreement, and without further action by the parties hereto, such provision shall be reformed to the minimum extent necessary to make such provision valid and enforceable.\n"
            +
            "\n"
            +
            "If Recipient institutes patent litigation against any entity (including a cross-claim or counterclaim in a lawsuit) alleging that the Program itself (excluding combinations of the Program with other software or hardware) infringes such Recipient\'s patent(s), then such Recipient\'s rights granted under Section 2(b) shall terminate as of the date such litigation is filed.\n"
            +
            "\n"
            +
            "All Recipient\'s rights under this Agreement shall terminate if it fails to comply with any of the material terms or conditions of this Agreement and does not cure such failure in a reasonable period of time after becoming aware of such noncompliance. If all Recipient\'s rights under this Agreement terminate, Recipient agrees to cease use and distribution of the Program as soon as reasonably practicable. However, Recipient\'s obligations under this Agreement and any licenses granted by Recipient relating to the Program shall continue and survive.\n"
            +
            "\n"
            +
            "Everyone is permitted to copy and distribute copies of this Agreement, but in order to avoid inconsistency the Agreement is copyrighted and may only be modified in the following manner. The Agreement Steward reserves the right to publish new versions (including revisions) of this Agreement from time to time. No one other than the Agreement Steward has the right to modify this Agreement. The Eclipse Foundation is the initial Agreement Steward. The Eclipse Foundation may assign the responsibility to serve as the Agreement Steward to a suitable separate entity. Each new version of the Agreement will be given a distinguishing version number. The Program (including Contributions) may always be distributed subject to the version of the Agreement under which it was received. In addition, after a new version of the Agreement is published, Contributor may elect to distribute the Program (including its Contributions) under the new version. Except as expressly stated in Sections 2(a) and 2(b) above, Recipient receives no rights or licenses to the intellectual property of any Contributor under this Agreement, whether expressly, by implication, estoppel or otherwise. All rights in the Program not expressly granted under this Agreement are reserved.\n"
            +
            "\n"
            +
            "This Agreement is governed by the laws of the State of New York and the intellectual property laws of the United States of America. No party to this Agreement will bring a legal action under this Agreement more than one year after the cause of action arose. Each party waives its rights to a jury trial in any resulting litigation.";

}
