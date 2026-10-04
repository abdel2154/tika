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

// Test genere par ChatUniTest (tentative CompilationError_1), corrige a la main pour IFT3913.
// CORRECTION 1 : import inexistant org.apache.tika.exception.BufferUnderrunException
//                -> classe imbriquee EndianUtils.BufferUnderrunException
public class EndianUtils_readUShortBE_3_0_Test {

    private EndianUtils endianUtils;

    @BeforeEach
    public void setUp() {
        endianUtils = new EndianUtils();
    }

    @Test
    // CORRECTION 2 : la methode lance BufferUnderrunException (exception verifiee) -> throws Exception
    public void testReadUShortBE() throws Exception {
        // Test with valid input
        InputStream inputStream = new ByteArrayInputStream(new byte[] { 0x12, 0x34 });
        int result = endianUtils.readUShortBE(inputStream);
        assertEquals(0x1234, result);
        // Test with negative input
        // CORRECTION 3 : l'octet -1 (0xFF) est une donnee valide, pas une fin de flux :
        //                l'IA attendait une exception, le resultat correct est 0xFF00
        InputStream negative = new ByteArrayInputStream(new byte[] { -1, 0 });
        assertEquals(0xFF00, endianUtils.readUShortBE(negative));
        // Test with end of stream
        // CORRECTION 4 : variable reassignee utilisee dans une lambda (non effectivement finale)
        InputStream tooShort = new ByteArrayInputStream(new byte[] { 0x12 });
        assertThrows(EndianUtils.BufferUnderrunException.class,
                () -> endianUtils.readUShortBE(tooShort));
    }
}
