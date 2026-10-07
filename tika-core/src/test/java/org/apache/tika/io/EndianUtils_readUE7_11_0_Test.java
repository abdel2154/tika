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

// Test genere par ChatUniTest (selon l'outil : "compile and execute successfully" a la ronde 1), corrige a la main pour IFT3913.
public class EndianUtils_readUE7_11_0_Test {

    @Test
    public void testReadUE7() throws IOException {
        // Test case 1: Normal case
        byte[] data1 = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
        InputStream stream1 = new ByteArrayInputStream(data1);
        // CORRECTION 1 : format UE7 : 0x01 n'a pas le bit de continuation (bit 7) -> dernier octet,
        //                la valeur lue est 1 (les octets suivants ne sont pas lus)
        assertEquals(1L, EndianUtils.readUE7(stream1));
        // Test case 2: Continues bit set
        byte[] data2 = { (byte) 0x81, (byte) 0x82, (byte) 0x83, (byte) 0x84, (byte) 0x85, (byte) 0x86, (byte) 0x87, (byte) 0x88 };
        InputStream stream2 = new ByteArrayInputStream(data2);
        // CORRECTION 2 : 7 bits utiles par octet et au plus 6 octets lus (max = 6) :
        //                1*128^5 + 2*128^4 + 3*128^3 + 4*128^2 + 5*128 + 6
        assertEquals(34902966918L, EndianUtils.readUE7(stream2));
        // Test case 3: Continues bit not set
        byte[] data3 = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
        InputStream stream3 = new ByteArrayInputStream(data3);
        // CORRECTION 3 : meme donnee que le cas 1 -> 1
        assertEquals(1L, EndianUtils.readUE7(stream3));
        // Test case 4: Buffer underun
        byte[] data4 = {};
        InputStream stream4 = new ByteArrayInputStream(data4);
        assertThrows(IOException.class, () -> EndianUtils.readUE7(stream4));
    }
}
