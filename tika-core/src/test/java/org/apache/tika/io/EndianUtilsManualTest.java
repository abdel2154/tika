/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

/**
 * Tests ecrits a la main (IFT3913, tache 2) pour tuer les mutants PIT
 * qui survivent aux tests originaux et aux tests generes par ChatUniTest.
 * Chaque test est documente dans le README a la racine du depot.
 */
public class EndianUtilsManualTest {

    /** Lecture d'un nombre a partir d'un flux, utilisee pour parcourir toutes les methodes read*. */
    private interface StreamReader {
        long read(InputStream stream) throws Exception;
    }

    private static final StreamReader[] READERS_2 = {
            EndianUtils::readUShortLE, EndianUtils::readUShortBE};
    private static final StreamReader[] READERS_4 = {
            EndianUtils::readUIntLE, EndianUtils::readUIntBE, EndianUtils::readIntLE,
            EndianUtils::readIntBE, EndianUtils::readIntME};
    private static final StreamReader[] READERS_8 = {
            EndianUtils::readLongLE, EndianUtils::readLongBE};

    /** Flux simule qui renvoie exactement les valeurs donnees (-1 = fin de flux), puis -1. */
    private static InputStream streamOf(int... values) {
        return new InputStream() {
            private int pos = 0;

            @Override
            public int read() {
                return pos < values.length ? values[pos++] : -1;
            }
        };
    }

    private static InputStream bytes(int... values) {
        byte[] b = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            b[i] = (byte) values[i];
        }
        return new ByteArrayInputStream(b);
    }

    @Test
    public void testReadAllZeroBytesReturnsZero() throws Exception {
        for (StreamReader r : READERS_2) {
            assertEquals(0L, r.read(bytes(0, 0)));
        }
        for (StreamReader r : READERS_4) {
            assertEquals(0L, r.read(bytes(0, 0, 0, 0)));
        }
        for (StreamReader r : READERS_8) {
            assertEquals(0L, r.read(bytes(0, 0, 0, 0, 0, 0, 0, 0)));
        }
    }

    @Test
    public void testReadDetectsMissingByteAtEveryPosition() {
        checkMissingByteAtEveryPosition(READERS_2, 2);
        checkMissingByteAtEveryPosition(READERS_4, 4);
        checkMissingByteAtEveryPosition(READERS_8, 8);
    }

    private static void checkMissingByteAtEveryPosition(StreamReader[] readers, int size) {
        for (StreamReader r : readers) {
            for (int missing = 0; missing < size; missing++) {
                int[] values = new int[size];
                for (int i = 0; i < size; i++) {
                    values[i] = (i == missing) ? -1 : 0x11;
                }
                assertThrows(EndianUtils.BufferUnderrunException.class,
                        () -> r.read(streamOf(values)),
                        "octet manquant a la position " + missing);
            }
        }
    }

    @Test
    public void testReadUntestedStreamMethods() throws Exception {
        assertEquals(0x1234, EndianUtils.readUShortLE(bytes(0x34, 0x12)));
        assertEquals(0xFEFF, EndianUtils.readUShortLE(bytes(0xFF, 0xFE)));
        assertEquals(0x01020304, EndianUtils.readIntBE(bytes(0x01, 0x02, 0x03, 0x04)));
        assertEquals(0x80000001, EndianUtils.readIntBE(bytes(0x80, 0x00, 0x00, 0x01)));
        assertEquals((short) -2, EndianUtils.readShortLE(bytes(0xFE, 0xFF)));
        assertEquals((short) -2, EndianUtils.readShortBE(bytes(0xFF, 0xFE)));
    }

    @Test
    public void testArrayMethodsWithAndWithoutOffset() {
        byte[] le = {(byte) 0xFE, (byte) 0xFF};
        assertEquals((short) -2, EndianUtils.getShortLE(le));
        assertEquals((short) -2, EndianUtils.getShortLE(le, 0));
        assertEquals(0xFFFE, EndianUtils.getUShortLE(le));
        assertEquals(0x1234, EndianUtils.getUShortLE(new byte[]{0x00, 0x34, 0x12}, 1));
        assertEquals(0x1234, EndianUtils.getUShortBE(new byte[]{0x12, 0x34}));
        assertEquals(0x04030201, EndianUtils.getIntLE(new byte[]{1, 2, 3, 4}));
        assertEquals(0x01020304, EndianUtils.getIntBE(new byte[]{1, 2, 3, 4}));
        byte[] big = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xF0};
        assertEquals(4294967280L, EndianUtils.getUIntBE(big));
        assertEquals(4294967280L, EndianUtils.getUIntBE(new byte[]{0, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xF0}, 1));
    }

    @Test
    public void testReadUE7ZeroByteAfterContinuation() throws Exception {
        // 0x81 = "continue, valeur 1", puis 0x00 = "dernier octet, valeur 0" : 1 << 7 = 128
        assertEquals(128L, EndianUtils.readUE7(bytes(0x81, 0x00)));
    }

    @Test
    public void testReadUE7StopsAfterSixBytes() throws Exception {
        // 7 octets "continue, valeur 1" puis 0x01 : la methode s'arrete apres max = 6 octets.
        // Valeur attendue = 1 + 128 + 128^2 + ... + 128^5 = (2^42 - 1) / 127
        InputStream in = bytes(0x81, 0x81, 0x81, 0x81, 0x81, 0x81, 0x81, 0x01);
        assertEquals(34630287489L, EndianUtils.readUE7(in));
    }
}
