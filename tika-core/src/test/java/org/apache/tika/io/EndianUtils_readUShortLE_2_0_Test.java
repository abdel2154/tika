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
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.*;
import org.mockito.*;

import org.apache.tika.exception.TikaException;

// Test genere par ChatUniTest (tentative CompilationError_1), corrige a la main pour IFT3913.
// CORRECTION 1 : imports inexistants org.apache.tika.io.InputStreamWithPosition et
//                org.apache.tika.io.BufferUnderrunException supprimes
public class EndianUtils_readUShortLE_2_0_Test {

    private EndianUtils endianUtils;

    @BeforeEach
    public void setUp() {
        endianUtils = new EndianUtils();
    }

    @Test
    public void testReadUShortLE() throws IOException, TikaException {
        byte[] data = { 0x12, 0x34 };
        InputStream inputStream = new ByteArrayInputStream(data);
        int result = endianUtils.readUShortLE(inputStream);
        assertEquals(0x3412, result);
    }

    @Test
    public void testReadUShortLEWithBufferUnderrun() throws IOException, TikaException {
        byte[] data = { 0x12 };
        InputStream inputStream = new ByteArrayInputStream(data);
        // CORRECTION 2 : BufferUnderrunException est une classe imbriquee -> EndianUtils.BufferUnderrunException
        assertThrows(EndianUtils.BufferUnderrunException.class, () -> endianUtils.readUShortLE(inputStream));
    }

    @Test
    public void testReadUShortLEWithNegativeValues() throws IOException, TikaException {
        byte[] data = { (byte) 0xFF, (byte) 0xFF };
        InputStream inputStream = new ByteArrayInputStream(data);
        int result = endianUtils.readUShortLE(inputStream);
        assertEquals(0xFFFF, result);
    }

    @Test
    public void testReadUShortLEWithZeroValues() throws IOException, TikaException {
        byte[] data = { 0x00, 0x00 };
        InputStream inputStream = new ByteArrayInputStream(data);
        int result = endianUtils.readUShortLE(inputStream);
        assertEquals(0x0000, result);
    }

    @Test
    public void testReadUShortLEWithLargeValues() throws IOException, TikaException {
        byte[] data = { (byte) 0x7F, (byte) 0xFF };
        InputStream inputStream = new ByteArrayInputStream(data);
        int result = endianUtils.readUShortLE(inputStream);
        assertEquals(0xFF7F, result);
    }

    @Test
    public void testReadUShortLEWithMixedValues() throws IOException, TikaException {
        byte[] data = { (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78 };
        InputStream inputStream = new ByteArrayInputStream(data);
        int result = endianUtils.readUShortLE(inputStream);
        assertEquals(0x3412, result);
    }

    @Test
    public void testReadUShortLEWithEmptyStream() throws IOException, TikaException {
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        // CORRECTION 3 : BufferUnderrunException est une classe imbriquee -> EndianUtils.BufferUnderrunException
        assertThrows(EndianUtils.BufferUnderrunException.class, () -> endianUtils.readUShortLE(inputStream));
    }

    @Test
    public void testReadUShortLEWithNullStream() throws IOException, TikaException {
        InputStream inputStream = null;
        assertThrows(NullPointerException.class, () -> endianUtils.readUShortLE(inputStream));
    }
}
