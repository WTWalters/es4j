/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.postgresql;

import com.eventsourcing.*;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.layout.LayoutConstructor;
import com.eventsourcing.repository.JournalTest;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.*;

import static com.eventsourcing.postgresql.PostgreSQLTest.createDataSource;
import static org.apache.commons.lang3.ArrayUtils.toObject;
import static org.testng.Assert.*;

@Test
public class PostgreSQLJournalTest extends JournalTest<PostgreSQLJournal> {


    @SneakyThrows
    static PostgreSQLJournal createJournal() {
        return new PostgreSQLJournal(createDataSource());
    }

    public PostgreSQLJournalTest() {
        super(createJournal());
    }

    @BeforeClass @Override public void setUpEnv() throws Exception {
        super.setUpEnv();
        repository.addCommandSetProvider(new PackageCommandSetProvider(new Package[]{PostgreSQLJournalTest.class.getPackage()
        }));
        repository.addEventSetProvider(new PackageEventSetProvider(new Package[]{PostgreSQLJournalTest.class.getPackage()
        }));
    }

    @Accessors(fluent = true)
    public static class SomeValue {
        @Getter
        private final String value;

        @Builder
        public SomeValue(String value) {this.value = value;}
    }

    @Accessors(fluent = true)
    public static class SomeValue1 {
        @Getter
        private final List<SomeValue2> value;

        @Builder
        public SomeValue1(List<SomeValue2> value) {this.value = value;}
    }

    @Accessors(fluent = true)
    public static class SomeValue2 {
        @Getter
        private final SomeValue value;

        @Builder
        public SomeValue2(SomeValue value) {this.value = value;}
    }

    @Accessors(fluent = true)
    public static class TestClass {

        @Getter
        private final byte pByte;
        @Getter
        private final Byte oByte;

        @Getter
        private final byte[] pByteArr;
        @Getter
        private final Byte[] oByteArr;

        @Getter
        private final short pShort;
        @Getter
        private final Short oShort;

        @Getter
        private final int pInt;
        @Getter
        private final Integer oInt;

        @Getter
        private final long pLong;
        @Getter
        private final Long oLong;

        @Getter
        private final float pFloat;
        @Getter
        private final Float oFloat;

        @Getter
        private final double pDouble;
        @Getter
        private final Double oDouble;

        @Getter
        private final boolean pBoolean;
        @Getter
        private final Boolean oBoolean;

        @Getter
        private final String str;

        @Getter
        private final UUID uuid;

        public enum E {A, B}

        @Getter
        private final E e;

        @Getter
        private final SomeValue value;

        @Getter
        private final SomeValue1 value1;

        @Getter
        private final List<List<String>> list;

        @Getter
        private final Map<String, List<String>> map;

        @Getter
        private final Optional<String> optional;

        @Getter
        private final BigDecimal bigDecimal;

        @Getter
        private final Date date;

        @Builder
        public TestClass(byte pByte, Byte oByte, byte[] pByteArr, Byte[] oByteArr, short pShort, Short oShort, int pInt,
                         Integer oInt, long pLong, Long oLong, float pFloat, Float oFloat, double pDouble,
                         Double oDouble, boolean pBoolean, Boolean oBoolean, String str, UUID uuid,
                         E e, SomeValue value, SomeValue1 value1, List<List<String>> list,
                         Map<String, List<String>> map, Optional<String> optional,
                         BigDecimal bigDecimal, Date date) {
            this.pByte = pByte;
            this.oByte = oByte;
            this.pByteArr = pByteArr;
            this.oByteArr = oByteArr;
            this.pShort = pShort;
            this.oShort = oShort;
            this.pInt = pInt;
            this.oInt = oInt;
            this.pLong = pLong;
            this.oLong = oLong;
            this.pFloat = pFloat;
            this.oFloat = oFloat;
            this.pDouble = pDouble;
            this.oDouble = oDouble;
            this.pBoolean = pBoolean;
            this.oBoolean = oBoolean;
            this.str = str;
            this.uuid = uuid;
            this.e = e;
            this.value = value;
            this.value1 = value1;
            this.list = list;
            this.map = map;
            this.optional = optional;
            this.bigDecimal = bigDecimal;
            this.date = date;
        }
    }


    public static class SerializationEvent extends StandardEvent {
        @Getter
        final TestClass test;

        @Builder
        public SerializationEvent(HybridTimestamp timestamp, TestClass test) {
            super(timestamp);
            this.test = test;
        }
    }
    public static class SerializationCommand extends StandardCommand<SerializationEvent, UUID> {

        private TestClass t;

        @LayoutConstructor
        public SerializationCommand(HybridTimestamp timestamp) {
            super(timestamp);
        }

        @Builder
        public SerializationCommand(TestClass t) {
            super(null);
            this.t = t;
        }

        @Override public EventStream<SerializationEvent> events() throws Exception {
            SerializationEvent.SerializationEventBuilder builder = SerializationEvent.builder();
            if (t != null) {
                builder.test(t);
            }
            SerializationEvent serializationEvent = builder.build();
            return EventStream.ofWithState(serializationEvent, serializationEvent);
        }

        @Override public UUID result(SerializationEvent state) {
            return state.uuid();
        }
    }

    @Test @SneakyThrows
    public void serializationNull() {
        HybridTimestamp timestamp = new HybridTimestamp(timeProvider);
        timestamp.update();

        Journal.Transaction tx = journal.beginTransaction();
        SerializationEvent event = SerializationEvent.builder().test(TestClass.builder().build()).build();
        event = (SerializationEvent) journal.journal(tx, event);
        tx.rollback();

        TestClass test = event.getTest();

        assertEquals(test.pByte, 0);
        assertEquals(test.oByte, Byte.valueOf((byte) 0));

        assertEquals(test.pByteArr.length, 0);
        assertEquals(test.oByteArr.length, 0);

        assertEquals(test.pShort, 0);
        assertEquals(test.oShort, Short.valueOf((short) 0));

        assertEquals(test.pInt, 0);
        assertEquals(test.oInt, Integer.valueOf(0));

        assertEquals(test.pLong, 0);
        assertEquals(test.oLong, Long.valueOf(0));

        assertTrue(test.pFloat == 0.0);
        assertEquals(test.oFloat, Float.valueOf((float) 0.0));

        assertEquals(test.pDouble, 0.0);
        assertEquals(test.oDouble, Double.valueOf(0.0));


        assertEquals(test.pBoolean, false);
        assertEquals(test.oBoolean, Boolean.FALSE);

        assertEquals(test.str, "");

        assertEquals(test.uuid, new UUID(0,0));

        assertEquals(test.e, TestClass.E.A);

        assertNotNull(test.value);
        assertEquals(test.value.value, "");

        assertNotNull(test.value1);
        assertTrue(test.value1.value().isEmpty());

        assertNotNull(test.list);
        assertEquals(test.list.size(), 0);

        assertNotNull(test.map);
        assertEquals(test.map.size(), 0);

        assertNotNull(test.optional);
        assertFalse(test.optional.isPresent());

        assertNotNull(test.bigDecimal);
        assertEquals(test.bigDecimal, BigDecimal.ZERO);

        assertNotNull(test.date);
        assertEquals(test.date, new Date(0));

    }

    @Test @SneakyThrows
    public void serializationValue() {
        assertEquals(serializationResult(TestClass.builder().pByte(Byte.MIN_VALUE).build()).pByte(), Byte.MIN_VALUE);
        assertEquals(serializationResult(TestClass.builder().pByte(Byte.MAX_VALUE).build()).pByte(), Byte.MAX_VALUE);

        assertEquals((byte)serializationResult(TestClass.builder().oByte(Byte.MIN_VALUE).build()).oByte(), Byte.MIN_VALUE);
        assertEquals((byte)serializationResult(TestClass.builder().oByte(Byte.MAX_VALUE).build()).oByte(), Byte.MAX_VALUE);

        assertEquals(serializationResult(TestClass.builder().pByteArr("Hello, world".getBytes()).build()).pByteArr(),
                     "Hello, world".getBytes());
        assertEquals(serializationResult(TestClass.builder().oByteArr(toObject(("Hello, world").getBytes())).build()).oByteArr(),
                     "Hello, world".getBytes());

        assertEquals(serializationResult(TestClass.builder().pShort(Short.MIN_VALUE).build()).pShort(), Short.MIN_VALUE);
        assertEquals((short)serializationResult(TestClass.builder().oShort(Short.MAX_VALUE).build()).oShort(), Short.MAX_VALUE);

        assertEquals(serializationResult(TestClass.builder().pInt(Integer.MIN_VALUE).build()).pInt(), Integer.MIN_VALUE);
        assertEquals((int)serializationResult(TestClass.builder().oInt(Integer.MAX_VALUE).build()).oInt(), Integer.MAX_VALUE);

        assertEquals(serializationResult(TestClass.builder().pLong(Long.MIN_VALUE).build()).pLong(), Long.MIN_VALUE);
        assertEquals((long)serializationResult(TestClass.builder().oLong(Long.MAX_VALUE).build()).oLong(), Long.MAX_VALUE);

        assertEquals(serializationResult(TestClass.builder().pFloat(Float.MIN_VALUE).build()).pFloat(), Float.MIN_VALUE);
        assertEquals(serializationResult(TestClass.builder().oFloat(Float.MAX_VALUE).build()).oFloat(), Float.MAX_VALUE);

        assertEquals(serializationResult(TestClass.builder().pDouble(Double.MIN_VALUE).build()).pDouble(), Double.MIN_VALUE);
        assertEquals(serializationResult(TestClass.builder().oDouble(Double.MAX_VALUE).build()).oDouble(), Double.MAX_VALUE);

        assertEquals(serializationResult(TestClass.builder().pBoolean(true).build()).pBoolean(), true);
        assertEquals(serializationResult(TestClass.builder().pBoolean(false).build()).pBoolean(), false);

        assertEquals((boolean)serializationResult(TestClass.builder().oBoolean(true).build()).oBoolean(), true);
        assertEquals((boolean)serializationResult(TestClass.builder().oBoolean(false).build()).oBoolean(), false);

        assertEquals(serializationResult(TestClass.builder().str("Hello, world").build()).str(), "Hello, world");

        UUID uuid = UUID.randomUUID();
        assertEquals(serializationResult(TestClass.builder().uuid(uuid).build()).uuid(), uuid);

        assertEquals(serializationResult(TestClass.builder().e(TestClass.E.B).build()).e(), TestClass.E.B);

        assertEquals(serializationResult(TestClass.builder().value(new SomeValue("test")).build())
                             .value().value(), "test");

        assertEquals(serializationResult(
                TestClass.builder()
                         .value1(new SomeValue1(Collections.singletonList(new SomeValue2(new SomeValue("test")))))
                         .build()).value1().value().get(0).value().value(), "test");

        ArrayList<List<String>> l = new ArrayList<>();
        ArrayList<String> l1 = new ArrayList<>();
        l1.add("test");
        l.add(l1);
        assertEquals(serializationResult(TestClass.builder().list(l).build()).list().get(0).get(0), "test");

        Map<String, List<String>> map = new HashMap<>();
        LinkedList<String> list = new LinkedList<>(Arrays.asList("Hello"));
        map.put("test", list);
        map.put("anothertest", list);
        assertEquals(serializationResult(TestClass.builder().map(map).build()).map().get("test").get(0), "Hello");
        assertEquals(serializationResult(TestClass.builder().map(map).build()).map().get("anothertest").get(0),
                     "Hello");

        assertFalse(serializationResult(TestClass.builder().optional(Optional.empty()).build()).optional().isPresent());
        assertTrue(serializationResult(TestClass.builder().optional(Optional.of("test")).build()).optional().isPresent());
        assertEquals(serializationResult(TestClass.builder().optional(Optional.of("test")).build()).optional().get(), "test");

        BigDecimal bigDecimal = new BigDecimal("0.00000000000000000000000000001");
        assertEquals(serializationResult(TestClass.builder().bigDecimal(bigDecimal).build()).bigDecimal(), bigDecimal);

        Date date = new Date();
        assertEquals(serializationResult(TestClass.builder().date(date).build()).date(), date);
    }

    @SneakyThrows
    private TestClass serializationResult(TestClass t) {
        HybridTimestamp timestamp = new HybridTimestamp(timeProvider);
        timestamp.update();

        Journal.Transaction tx = journal.beginTransaction();
        SerializationEvent event = SerializationEvent.builder().test(t).build();
        journal.journal(tx, event);
        tx.commit();

        event = (SerializationEvent) journal.get(event.uuid()).get();

        return event.getTest();
    }
}
