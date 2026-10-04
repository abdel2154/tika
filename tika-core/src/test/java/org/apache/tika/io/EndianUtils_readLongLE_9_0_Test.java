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
// CORRECTION 1 : import inexistant org.apache.tika.io.BufferUnderrunException
//                -> classe imbriquee EndianUtils.BufferUnderrunException
@ExtendWith(MockitoExtension.class)
public class EndianUtils_readLongLE_9_0_Test {

    @Mock
    private InputStream inputStream;

    @Test
    public void testReadLongLE() throws IOException, TikaException {
        // Arrange
        when(inputStream.read()).thenReturn(1, 2, 3, 4, 5, 6, 7, 8);
        // Act
        long result = EndianUtils.readLongLE(inputStream);
        // Assert
        // CORRECTION 2 : en little-endian le premier octet lu est le poids faible :
        //                l'IA attendait 0x0102030405060708L (ordre big-endian)
        assertEquals(0x0807060504030201L, result);
    }

    @Test
    public void testReadLongLE_WithBufferUnderrunException() throws IOException, TikaException {
        // Arrange
        when(inputStream.read()).thenReturn(1, 2, 3, 4, 5, 6, 7, -1);
        // Act & Assert
        assertThrows(EndianUtils.BufferUnderrunException.class,
                () -> EndianUtils.readLongLE(inputStream));
    }
}
