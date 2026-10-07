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

import org.junit.jupiter.api.*;
import org.mockito.*;

// Test genere par ChatUniTest (selon l'outil : "compile and execute successfully"), corrige a la main pour IFT3913.
public class EndianUtils_getUShortLE_14_0_Test {

    @Test
    public void testGetUShortLE() throws IOException {
        byte[] data = { 0x01, 0x02 };
        int result = EndianUtils.getUShortLE(data);
        assertEquals(513, result);
    }

    @Test
    public void testGetUShortLEWithOffset() throws IOException {
        byte[] data = { 0x01, 0x02, 0x03, 0x04 };
        int result = EndianUtils.getUShortLE(data, 1);
        // CORRECTION 1 : decalage 1 -> octets 02 03 -> 0x0302 = 770 (513 = 0x0201 est la valeur au decalage 0)
        assertEquals(770, result);
    }
}
