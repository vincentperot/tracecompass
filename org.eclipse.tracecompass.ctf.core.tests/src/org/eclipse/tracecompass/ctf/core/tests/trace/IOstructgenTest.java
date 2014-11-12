/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *     Simon Delisle - Generate dummy trace
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tracecompass.ctf.core.event.IEventDeclaration;
import org.eclipse.tracecompass.ctf.core.tests.CtfCoreTestPlugin;
import org.eclipse.tracecompass.ctf.core.trace.CTFReaderException;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.internal.ctf.core.event.EventDeclaration;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.IOStructGen;
import org.junit.Test;

/**
 * Unit tests for {@link IOStructGen}
 *
 * @author Matthew Khouzam
 */
public class IOstructgenTest {

    private static final String metadataDecs = "typealias integer { size = 8; align = 8; signed = false; } := uint8_t;\n"
            + "typealias integer { size = 16; align = 8; signed = false; } := uint16_t;\n"
            + "typealias integer { size = 32; align = 8; signed = false; } := uint32_t;\n"
            + "typealias integer { size = 64; align = 8; signed = false; } := uint64_t;\n"
            + "typealias integer { size = 64; align = 8; signed = false; } := unsigned long;\n"
            + "typealias integer { size = 5; align = 1; signed = false; } := uint5_t;\n"
            + "typealias integer { size = 27; align = 1; signed = false; } := uint27_t;\n"
            + "typealias integer { size = 32; align = 1; signed = true; base = decimal; } := int32_t;\n"
            + "typealias integer { size = 31; align = 1; signed = true; base = dec; } := int31_t;\n"
            + "typealias integer { size = 30; align = 1; signed = true; base = d; } := int30_t;\n"
            + "typealias integer { size = 29; align = 1; signed = true; base = i; } := int29_t;\n"
            + "typealias integer { size = 28; align = 1; signed = true; base = u; } := int28_t;\n"
            + "typealias integer { size = 27; align = 1; signed = true; base = hexadecimal; } := int27_t;\n"
            + "typealias integer { size = 26; align = 1; signed = true; base = hex; } := int26_t;\n"
            + "typealias integer { size = 25; align = 1; signed = true; base = x; } := int25_t;\n"
            + "typealias integer { size = 24; align = 1; signed = true; base = X; } := int24_t;\n"
            + "typealias integer { size = 23; align = 1; signed = true; base = p; } := int23_t;\n"
            + "typealias integer { size = 22; align = 1; signed = true; base = 16; } := int22_t;\n"
            + "typealias integer { size = 21; align = 1; signed = true; base = oct; } := int21_t;\n"
            + "typealias integer { size = 20; align = 1; signed = true; base = b; } := int20_t;\n"
            + "typealias integer { size = 19; align = 1; signed = true; base = octal; } := int19_t;\n"
            + "typealias integer { size = 18; align = 1; signed = true; base = o; } := int18_t;\n"
            + "typealias integer { size = 17; align = 1; signed = true; base = binary; } := int17_t;\n"
            + "\n"
            + "trace {\n"
            + "    major = 1;\n"
            + "    minor = 8;\n"
            + "    uuid = \"b04d391b-e736-44c1-8d89-4bb438857f8d\";\n"
            + "    byte_order = le;\n"
            + "    packet.header := struct {\n"
            + "        uint32_t magic;\n"
            + "        uint8_t  uuid[16];\n"
            + "        uint32_t stream_id;\n" + "    };\n" + "};\n" + "\n";
    private static final String environmentMD = "env {\n"
            + "    hostname = \"DemoSystem\";\n"
            + "    vpid = 1337;\n"
            + "    procname = \"demo\";\n"
            + "    domain = \"autogenerated\";\n"
            + "    tracer_name = \"tmf\";\n"
            + "    tracer_major = 2;\n"
            + "    tracer_minor = 0x01;\n"
            + "    tracer_patchlevel = 0;\n"
            + "};\n" + "\n";
    private static final String clockMD = "clock {\n" + "    name = monotonic;\n"
            + "    uuid = \"cbf9f42e-9be7-4798-a96f-11db556e2ebb\";\n"
            + "    description = \"Monotonic Clock\";\n"
            + "    freq = 1000000000; /* Frequency, in Hz */\n"
            + "    /* clock value offset from Epoch is: offset * (1/freq) */\n"
            + "    offset = 1350310657466295832;\n" + "};\n"
            + "\n";

    private static final String ctfStart =
            "typealias integer {\n"
                    + "    size = 27; align = 1; signed = false;\n"
                    + "    map = clock.monotonic.value;\n"
                    + "} := uint27_clock_monotonic_t;\n"
                    + "\n"
                    + "typealias integer {\n"
                    + "    size = 32; align = 8; signed = false;\n"
                    + "    map = clock.monotonic.value;\n"
                    + "} := uint32_clock_monotonic_t;\n"
                    + "\n"
                    + "typealias integer {\n"
                    + "    size = 64; align = 8; signed = false;\n"
                    + "    map = clock.monotonic.value;\n"
                    + "} := uint64_clock_monotonic_t;\n"
                    + "\n";

    private static final String ctfHeaders =
            "struct packet_context {\n"
                    + "    uint64_clock_monotonic_t timestamp_begin;\n"
                    + "    uint64_clock_monotonic_t timestamp_end;\n"
                    + "    uint64_t content_size;\n"
                    + "    uint64_t packet_size;\n"
                    + "    unsigned long events_discarded;\n"
                    + "    uint32_t cpu_id;\n"
                    + "};\n"
                    + "\n"
                    + "struct event_header_compact {\n"
                    + "    enum : uint5_t { compact = 0 ... 30, extended = 31 } id;\n"
                    + "    variant <id> {\n"
                    + "        struct {\n"
                    + "            uint27_clock_monotonic_t timestamp;\n"
                    + "        } compact;\n"
                    + "        struct {\n"
                    + "            uint32_t id;\n"
                    + "            uint64_clock_monotonic_t timestamp;\n"
                    + "        } extended;\n"
                    + "    } v;\n"
                    + "} align(8);\n"
                    + "\n"
                    + "struct event_header_large {\n"
                    + "    enum : uint16_t { compact = 0 ... 65534, extended = 65535 } id;\n"
                    + "    variant <id> {\n" + "        struct {\n"
                    + "            uint32_clock_monotonic_t timestamp;\n"
                    + "        } compact;\n" + "        struct {\n"
                    + "            uint32_t id;\n"
                    + "            uint64_clock_monotonic_t timestamp;\n"
                    + "        } extended;\n" + "    } v;\n" + "} align(8);\n" + "\n";

    private static final String ctfBody = "stream {\n"
            + "    id = 0;\n"
            + "    event.header := struct event_header_compact;\n"
            + "    packet.context := struct packet_context;\n"
            + "    event.context := struct {\n"
            + "        integer { size = 64; align = 8; signed = 0; encoding = none; base = 16; } _ip;\n"
            + "    };\n"
            + "};\n"
            + "\n"
            + "event {\n"
            + "    name = \"ust_tests_demo3:done\";\n"
            + "    id = 0;\n"
            + "    stream_id = 0;\n"
            + "    loglevel = 4;\n"
            + "    fields := struct {\n"
            + "        integer { size = 32; align = 8; signed = 1; encoding = none; base = 10; } _value;\n"
            + "    };\n"
            + "};\n"
            + "\n"
            + "event {\n"
            + "    name = \"ust_tests_demo:starting\";\n"
            + "    id = 1;\n"
            + "    stream_id = 0;\n"
            + "    loglevel = 2;\n"
            + "    model.emf.uri = \"http://example.com/path_to_model?q=ust_tests_demo:starting\";\n"
            + "    fields := struct {\n"
            + "        integer { size = 32; align = 8; signed = 1; encoding = none; base = 10; } _value;\n"
            + "    };\n"
            + "};\n"
            + "\n"
            + "event {\n"
            + "    name = \"ust_tests_demo:done\";\n"
            + "    id = 2;\n"
            + "    stream_id = 0;\n"
            + "    loglevel = 2;\n"
            + "    model.emf.uri = \"http://example.com/path_to_model?q=ust_tests_demo:done\";\n"
            + "    fields := struct {\n"
            + "        integer { size = 32; align = 8; signed = 1; encoding = none; base = 10; } _value;\n"
            + "    };\n"
            + "};\n"
            + "\n"
            + "event {\n"
            + "    name = \"ust_tests_demo2:loop\";\n"
            + "    id = 3;\n"
            + "    stream_id = 0;\n"
            + "    loglevel = 4;\n"
            + "    fields := struct {\n"
            + "        integer { size = 32; align = 8; signed = 1; encoding = none; base = 10; } _intfield;\n"
            + "        integer { size = 32; align = 8; signed = 1; encoding = none; base = 16; } _intfield2;\n"
            + "        integer { size = 64; align = 8; signed = 1; encoding = none; base = 10; } _longfield;\n"
            + "        integer { size = 32; align = 8; signed = 1; encoding = none; base = 10; byte_order = be; } _netintfield;\n"
            + "        integer { size = 32; align = 8; signed = 1; encoding = none; base = 16; byte_order = be; } _netintfieldhex;\n"
            + "        integer { size = 64; align = 8; signed = 1; encoding = none; base = 10; } _arrfield1[3];\n"
            + "        integer { size = 8; align = 8; signed = 1; encoding = UTF8; base = 10; } _arrfield2[10];\n"
            + "        integer { size = 64; align = 8; signed = 0; encoding = none; base = 10; } __seqfield1_length;\n"
            + "        integer { size = 8; align = 8; signed = 1; encoding = none; base = 10; } _seqfield1[ __seqfield1_length ];\n"
            + "        integer { size = 64; align = 8; signed = 0; encoding = none; base = 10; } __seqfield2_length;\n"
            + "        integer { size = 8; align = 8; signed = 1; encoding = UTF8; base = 10; } _seqfield2[ __seqfield2_length ];\n"
            + "        string _stringfield;\n"
            + "        floating_point { exp_dig = 8; mant_dig = 24; align = 8; } _floatfield;\n"
            + "        floating_point { exp_dig = 11; mant_dig = 53; align = 8; } _doublefield;\n"
            + "    };\n"
            + "};\n"
            + "\n";

    private static final String enumMd =
            "typealias integer { size = 32; align = 8; signed = false; } := int;\n"
                    + "typealias enum { ONE = 0, a,b,c=10, d} := useless_enum;\n"
                    + "struct useless{ \n"
                    + "    enum : uint8_t { A=0, \"B\",} enum3;\n"
                    + "    useless_enum enum2;"
                    + "    enum { C, D, E } enum4;\n"
                    + "    uint16_t val;\n"
                    + "} ;\n"
                    + "\n"
                    + "event {\n"
                    + "   name = \"enumEvent\";\n"
                    + "   id = 6;\n"
                    + "   stream_id = 0;\n"
                    + "   loglevel = 5;\n"
                    + "   fields := struct{\n"
                    + "       uint16_t _some_field;\n"
                    // + "       useless junk;\n"
                    // + "       bad_enum a;\n"
                    + "       enum {A, B, C = 3 , } _other_enum;\n"
                    + "   };\n"
                    + "};\n"
                    + "\n";

    private final static String contextMD =
            "event {\n" +
                    "   name = \"someOtherEvent\";\n" +
                    "   id = 5;\n" +
                    "   stream_id = 0;\n" +
                    "   loglevel = 5;\n" +
                    "   context := struct{\n" +
                    "       uint16_t _someContext;\n" +
                    "   };\n" +
                    "   fields := struct{\n" +
                    "       uint16_t _somefield;\n" +
                    "   };\n" +
                    "};\n " +
                    "\n";

    private static final String callsiteMD =
            "callsite {\n"
                    + "    name = \"ust_tests_demo2:loop\";\n"
                    + "    func = \"main\";\n" + "    ip = 0x400a29;\n"
                    + "    file = \"demo.c\";\n" + "    line = 59;\n" + "};\n" + "\n"
                    + "callsite {\n" + "    name = \"ust_tests_demo3:done\";\n"
                    + "    func = \"main\";\n" + "    ip = 0x400a6c;\n"
                    + "    file = \"demo.c\";\n" + "    line = 62;\n" + "};\n" + "\n"
                    + "callsite {\n" + "    name = \"ust_tests_demo:done\";\n"
                    + "    func = \"main\";\n" + "    ip = 0x400aaf;\n"
                    + "    file = \"demo.c\";\n" + "    line = 61;\n" + "};\n" + "\n"
                    + "callsite {\n" + "    name = \"ust_tests_demo:starting\";\n"
                    + "    func = \"main\";\n" + "    ip = 0x400af2;\n"
                    + "    file = \"demo.c\";\n" + "    line = 55;\n" + "};\n";

    private static final String simpleTSDL = metadataDecs + ctfStart + ctfHeaders
            + ctfBody;
    private static final String enumTSDL = metadataDecs + ctfStart + ctfHeaders
            + ctfBody + enumMd;
    private static final String clockTSDL = metadataDecs + clockMD + ctfStart
            + ctfHeaders + ctfBody;
    private static final String envTSDL = metadataDecs + environmentMD + ctfStart
            + ctfHeaders + ctfBody;
    private static final String contextTSDL = metadataDecs + environmentMD + ctfStart
            + ctfHeaders + ctfBody + contextMD;
    private static final String callsiteTSDL = metadataDecs + ctfStart + ctfHeaders
            + ctfBody + callsiteMD;
    private static final String allDressedTSDL = metadataDecs + environmentMD + clockMD
            + ctfStart + ctfHeaders + ctfBody + enumMd + callsiteMD;

    static final String tempTraceDir = CtfCoreTestPlugin.getTemporaryDirPath()
            + File.separator + "tempTrace";

    private static final int DATA_SIZE = 4096;

    private static final int HEADER_SIZE = 68;

    private static final int PACKET_SIZE = DATA_SIZE + HEADER_SIZE + 512;

    private CTFTrace trace;

    private static class Event {
        private static final int EVENT_SIZE = 16;
        private int eventId;
        private int eventTimestamp;
        private int eventContent;

        public Event(int id, int content) {
            eventId = id;
            eventTimestamp = 0;
            eventContent = content;
        }

        public void setEventTimestamp(int eventTimestamp) {
            this.eventTimestamp = eventTimestamp;
        }

        public void setEventContent(int eventContent) {
            this.eventContent = eventContent;
        }

        public void writeEvent(ByteBuffer data) {
            // Id and Timestamp
            int timeId = eventTimestamp << 5;
            timeId |= eventId & 0x1f;
            data.putInt(timeId);

            // Context
            long ip = 0x0000facedecafe00L + ((data.position() /
                    getSize()) & 0x0F);
            data.putLong(ip);

            // Content
            data.putInt(eventContent);

        }

        public int getSize() {
            return EVENT_SIZE;
        }

    }

    private static void deltree(File f) {
        for (File elem : f.listFiles()) {
            if (elem.isDirectory()) {
                deltree(elem);
            }
            elem.delete();
        }
        f.delete();
    }

    private static void createDummyTrace(String metadata) {
        File dir = new File(tempTraceDir);
        if (dir.exists()) {
            deltree(dir);
        }
        dir.mkdirs();

        File metadataFile = new File(tempTraceDir + "/metadata");
        try (FileWriter fw = new FileWriter(metadataFile);) {
            fw.write(metadata);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte magicLE[] = { (byte) 0xC1, (byte) 0x1F, (byte) 0xFC,
                (byte) 0xC1 };
        byte uuid[] = { (byte) 0xb0, 0x4d, 0x39, 0x1b, (byte) 0xe7,
                0x36, 0x44, (byte) 0xc1, (byte) 0x8d, (byte) 0x89, 0x4b,
                (byte) 0xb4, 0x38, (byte) 0x85, 0x7f, (byte) 0x8d };

        Event ev = new Event(2, 2);

        final int nbEvents = (DATA_SIZE / ev.getSize()) - 1;
        final int contentSize = (nbEvents * ev.getSize() +
                HEADER_SIZE) * 8;

        ByteBuffer data = ByteBuffer.allocate(PACKET_SIZE);
        data.order(ByteOrder.LITTLE_ENDIAN);
        data.clear();

        // packet header
        // magic number 4
        data.put(magicLE);
        // uuid 16
        data.put(uuid);
        // stream ID 4
        data.putInt(0);

        // packet context
        // timestamp_begin 8
        data.putLong(0xa500);

        // timestamp_end 8
        data.putLong(nbEvents * 0x10000 + 0xa5a6);

        // content_size 8
        data.putLong(contentSize);

        // packet_size 8
        data.putLong(PACKET_SIZE * 8);

        // events_discarded 8
        data.putLong(0);

        // cpu_id 4
        data.putInt(0);

        // fill me
        for (int i = 0; i < nbEvents; i++) {
            ev.setEventTimestamp(i * 0x10000 + 0xa5a5);
            ev.setEventContent(i);
            ev.writeEvent(data);
        }

        // The byteBuffer needs to be flipped in file writing mode
        data.flip();

        File dummyFile = new File(tempTraceDir + "/dummyChan");
        try (FileOutputStream fos = new FileOutputStream(dummyFile);) {
            fos.getChannel().write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Simple test (only the minimum)
     *
     * @throws CTFReaderException
     *             something wrong happened
     */
    @Test
    public void TSDLSimpleTest() throws CTFReaderException {
        createDummyTrace(simpleTSDL);
        trace = new CTFTrace(tempTraceDir);
        assertNotNull(trace);
    }

    /**
     * Test with environment variables
     *
     * @throws CTFReaderException
     *             something wrong happened
     */
    @Test
    public void TSDLEnvironmentTest() throws CTFReaderException {
        createDummyTrace(envTSDL);
        trace = new CTFTrace(tempTraceDir);
        assertNotNull(trace);
    }

    /**
     * Test with Clocks
     *
     * @throws CTFReaderException
     *             something wrong happened
     */
    @Test
    public void TSDLEnumTest() throws CTFReaderException {
        createDummyTrace(enumTSDL);
        trace = new CTFTrace(tempTraceDir);
        assertNotNull(trace);
    }

    /**
     * Test with Clocks
     *
     * @throws CTFReaderException
     *             something wrong happened
     */
    @Test
    public void TSDLClockTest() throws CTFReaderException {
        createDummyTrace(clockTSDL);
        trace = new CTFTrace(tempTraceDir);
        assertNotNull(trace);
    }

    /**
     * Test with Contexts
     *
     * @throws CTFReaderException
     *             something wrong happened
     */
    @Test
    public void TSDLContextTest() throws CTFReaderException {
        createDummyTrace(contextTSDL);
        trace = new CTFTrace(tempTraceDir);
        assertNotNull(trace);
    }

    /**
     * Test with Callsites
     *
     * @throws CTFReaderException
     *             something wrong happened
     */
    @Test
    public void TSDLCallsiteTest() throws CTFReaderException {
        createDummyTrace(callsiteTSDL);
        trace = new CTFTrace(tempTraceDir);
        assertNotNull(trace);
    }

    /**
     * Test everything
     *
     * @throws CTFReaderException
     *             something wrong happened
     */
    @Test
    public void TSDLAllTest() throws CTFReaderException {
        createDummyTrace(allDressedTSDL);
        trace = new CTFTrace(tempTraceDir);
        assertNotNull(trace);

        final List<IEventDeclaration> eventDeclarations = new ArrayList<>(trace.getEventDeclarations(0L));
        final EventDeclaration eventDeclaration = (EventDeclaration) eventDeclarations.get(2);
        assertEquals("http://example.com/path_to_model?q=ust_tests_demo:done",
                eventDeclaration.getCustomAttribute("model.emf.uri"));
    }

}
