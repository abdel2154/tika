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

public class EndianUtils_getShortBE_17_0_Test {

    @Test
    public void testGetShortBE() throws IOException {
        byte[] data = { 0x00, 0x01 };
        int offset = 0;
        short expected = 1;
        short result = EndianUtils.getShortBE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetShortBEWithOffset() throws IOException {
        byte[] data = { 0x01, 0x00, 0x02, 0x00 };
        int offset = 1;
        short expected = 2;
        short result = EndianUtils.getShortBE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetShortBEWithNegativeOffset() throws IOException {
        byte[] data = { 0x00, 0x01 };
        int offset = -1;
        Exception exception = assertThrows(IndexOutOfBoundsException.class, () -> {
            EndianUtils.getShortBE(data, offset);
        });
    }

    @Test
    public void testGetShortBEWithTooLargeOffset() throws IOException {
        byte[] data = { 0x00, 0x01 };
        int offset = 2;
        Exception exception = assertThrows(IndexOutOfBoundsException.class, () -> {
            EndianUtils.getShortBE(data, offset);
        });
    }
}
