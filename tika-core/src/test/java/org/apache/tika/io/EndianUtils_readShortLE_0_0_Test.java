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
// CORRECTION 1 : imports inexistants org.apache.tika.io.InputStream et
//                org.apache.tika.io.BufferUnderrunException supprimes
@ExtendWith(MockitoExtension.class)
public class EndianUtils_readShortLE_0_0_Test {

    @Mock
    private InputStream inputStream;

    @Test
    public void testReadShortLE() throws IOException, TikaException {
        when(inputStream.read()).thenReturn(0x12, 0x34);
        short result = EndianUtils.readShortLE(inputStream);
        assertEquals(0x3412, result);
        verify(inputStream, times(2)).read();
    }

    @Test
    public void testReadShortLE_WithBufferUnderrunException() throws IOException {
        when(inputStream.read()).thenReturn(0x12, -1);
        // CORRECTION 2 : BufferUnderrunException est une classe imbriquee -> EndianUtils.BufferUnderrunException
        assertThrows(EndianUtils.BufferUnderrunException.class, () -> EndianUtils.readShortLE(inputStream));
        verify(inputStream, times(2)).read();
    }
}
