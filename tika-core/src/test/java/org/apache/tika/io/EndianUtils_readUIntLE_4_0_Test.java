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

// Test genere par ChatUniTest (tentative CompilationError_1), corrige a la main pour IFT3913.
// CORRECTION 1 : import inexistant org.apache.tika.io.InputStreams supprime
public class EndianUtils_readUIntLE_4_0_Test {

    @Test
    // CORRECTION 2 : readUIntLE leve aussi BufferUnderrunException (verifiee) -> throws Exception
    public void testReadUIntLE() throws Exception {
        // Arrange
        InputStream inputStream = new ByteArrayInputStream(new byte[] { 0x01, 0x02, 0x03, 0x04 });
        EndianUtils endianUtils = new EndianUtils();
        // Act
        long result = endianUtils.readUIntLE(inputStream);
        // Assert
        assertEquals(0x04030201L, result);
    }

    @Test
    public void testReadUIntLE_WithBufferUnderrunException() throws IOException {
        // Arrange
        InputStream inputStream = new ByteArrayInputStream(new byte[] { 0x01, 0x02, 0x03 });
        EndianUtils endianUtils = new EndianUtils();
        // Act & Assert
        // CORRECTION 3 : BufferUnderrunException est une classe imbriquee -> EndianUtils.BufferUnderrunException
        assertThrows(EndianUtils.BufferUnderrunException.class, () -> endianUtils.readUIntLE(inputStream));
    }

    @Test
    // CORRECTION 5 (suite) : l'appel direct exige throws Exception (BufferUnderrunException verifiee)
    public void testReadUIntLE_WithNegativeByte() throws Exception {
        // Arrange
        InputStream inputStream = new ByteArrayInputStream(new byte[] { -1, -2, -3, -4 });
        EndianUtils endianUtils = new EndianUtils();
        // Act & Assert
        // CORRECTION 4 : (compilation) BufferUnderrunException -> EndianUtils.BufferUnderrunException
        // CORRECTION 5 : les octets -1..-4 (0xFF..0xFC) sont des donnees valides : read() renvoie 255, 254...
        //                et non -1. Valeur attendue en little-endian non signe : 0xFCFDFEFF
        assertEquals(0xFCFDFEFFL, endianUtils.readUIntLE(inputStream));
    }
}
