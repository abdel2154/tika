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

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import org.apache.tika.exception.TikaException;

// Test genere par ChatUniTest (tentative CompilationError_1), corrige a la main pour IFT3913.
// CORRECTION 1 : import inexistant org.apache.tika.exception.BufferUnderrunException supprime
@ExtendWith(MockitoExtension.class)
public class EndianUtils_readUIntBE_5_0_Test {

    @Mock
    private InputStream mockInputStream;

    @Test
    public void testReadUIntBE() throws IOException, TikaException {
        // Arrange
        int ch1 = 0x12;
        int ch2 = 0x34;
        int ch3 = 0x56;
        int ch4 = 0x78;
        when(mockInputStream.read()).thenReturn(ch1, ch2, ch3, ch4, -1);
        // Act
        long result = EndianUtils.readUIntBE(mockInputStream);
        // Assert
        assertEquals(0x12345678L, result);
        verify(mockInputStream, times(4)).read();
        // CORRECTION 2 : InputStream n'a pas de read(int) -> read(byte[])
        verify(mockInputStream, never()).read(any(byte[].class));
    }

    @Test
    public void testReadUIntBEWithBufferUnderrun() throws IOException, TikaException {
        // Arrange
        // CORRECTION 5 : 4 octets valides suffisent : il faut un -1 (fin de flux) pour provoquer l'exception
        when(mockInputStream.read()).thenReturn(0x12, 0x34, 0x56, -1);
        // Act & Assert
        // CORRECTION 3 : BufferUnderrunException est une classe imbriquee -> EndianUtils.BufferUnderrunException
        assertThrows(EndianUtils.BufferUnderrunException.class, () -> EndianUtils.readUIntBE(mockInputStream));
        verify(mockInputStream, times(4)).read();
        // CORRECTION 4 : InputStream n'a pas de read(int) -> read(byte[])
        verify(mockInputStream, never()).read(any(byte[].class));
    }
}
