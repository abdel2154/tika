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
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;

import org.apache.tika.exception.TikaException;

// Test genere par ChatUniTest (tentative CompilationError_1), corrige a la main pour IFT3913.
// CORRECTION 1 : imports inexistants org.apache.tika.io.InputStream et
//                org.apache.tika.io.BufferUnderrunException supprimes
public class EndianUtils_readIntBE_7_0_Test {

    @Test
    public void testReadIntBE() throws IOException, TikaException {
        byte[] data = { (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04 };
        InputStream inputStream = new ByteArrayInputStream(data);
        int result = EndianUtils.readIntBE(inputStream);
        assertEquals(0x01020304, result);
    }

    @Test
    public void testReadIntBEWithBufferUnderrun() throws IOException {
        byte[] data = { (byte) 0x01, (byte) 0x02 };
        InputStream inputStream = new ByteArrayInputStream(data);
        Executable executable = () -> EndianUtils.readIntBE(inputStream);
        // CORRECTION 2 : BufferUnderrunException est une classe imbriquee -> EndianUtils.BufferUnderrunException
        assertThrows(EndianUtils.BufferUnderrunException.class, executable);
    }
}
