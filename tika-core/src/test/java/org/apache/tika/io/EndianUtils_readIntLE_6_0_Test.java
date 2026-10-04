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

// Test genere par ChatUniTest (tentative CompilationError_1), corrige a la main pour IFT3913.
// CORRECTION 1 : import inexistant org.apache.tika.io.InputStream supprime (java.io.InputStream)
@ExtendWith(MockitoExtension.class)
public class EndianUtils_readIntLE_6_0_Test {

    @Mock
    private InputStream inputStream;

    @Test
    // CORRECTION 2 : BufferUnderrunException est une exception verifiee -> throws Exception
    public void testReadIntLE() throws Exception {
        when(inputStream.read()).thenReturn(1, 2, 3, 4);
        int result = EndianUtils.readIntLE(inputStream);
        assertEquals(0x04030201, result);
    }

    @Test
    public void testReadIntLEBufferUnderrunException() throws IOException {
        // CORRECTION 3 : Mockito repete la derniere valeur (3) indefiniment : il faut -1 pour
        //                simuler la fin du flux
        when(inputStream.read()).thenReturn(1, 2, 3, -1);
        // CORRECTION 4 : java.nio.BufferUnderflowException -> EndianUtils.BufferUnderrunException
        assertThrows(EndianUtils.BufferUnderrunException.class,
                () -> EndianUtils.readIntLE(inputStream));
    }

    @Test
    public void testReadIntLEIOException() throws IOException {
        when(inputStream.read()).thenThrow(new IOException());
        assertThrows(IOException.class, () -> EndianUtils.readIntLE(inputStream));
    }
}
