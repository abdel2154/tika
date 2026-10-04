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

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;

// Test genere par ChatUniTest (tentative CompilationError_1), corrige a la main pour IFT3913.
// CORRECTION 1 : import inexistant org.apache.tika.io.InputStreams supprime
public class EndianUtils_readLongBE_10_0_Test {

    private EndianUtils endianUtils;

    @BeforeEach
    public void setUp() {
        endianUtils = new EndianUtils();
    }

    @Test
    // CORRECTION 2 : BufferUnderrunException est une exception verifiee -> throws Exception
    public void testReadLongBE() throws Exception {
        byte[] data = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
        InputStream inputStream = new ByteArrayInputStream(data);
        long result = endianUtils.readLongBE(inputStream);
        assertEquals(0x0102030405060708L, result);
    }

    @Test
    public void testReadLongBEWithBufferUnderrun() {
        byte[] data = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };
        InputStream inputStream = new ByteArrayInputStream(data);
        Executable executable = () -> endianUtils.readLongBE(inputStream);
        // CORRECTION 3 : BufferUnderrunException -> EndianUtils.BufferUnderrunException
        assertThrows(EndianUtils.BufferUnderrunException.class, executable);
    }

    @Test
    public void testReadLongBEWithNegativeByte() throws Exception {
        byte[] data = new byte[] { -1, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
        InputStream inputStream = new ByteArrayInputStream(data);
        // CORRECTION 4 : l'octet -1 (0xFF) est une donnee valide : l'IA attendait une exception,
        //                le resultat correct est 0xFF02030405060708L
        assertEquals(0xFF02030405060708L, endianUtils.readLongBE(inputStream));
    }
}
