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

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;

public class EndianUtils_getShortBE_16_0_Test {

    @Test
    public void testGetShortBE() {
        byte[] data = { 0x00, 0x01 };
        short expected = 0x0001;
        short result = EndianUtils.getShortBE(data);
        assertEquals(expected, result);
    }

    @Test
    public void testGetShortBEWithOffset() {
        byte[] data = { 0x00, 0x01, 0x02, 0x03 };
        short expected = 0x0102;
        short result = EndianUtils.getShortBE(data, 1);
        assertEquals(expected, result);
    }

    @Test
    public void testGetShortBEWithEmptyArray() {
        byte[] data = {};
        Executable executable = () -> EndianUtils.getShortBE(data);
        assertThrows(IndexOutOfBoundsException.class, executable);
    }

    @Test
    public void testGetShortBEWithOffsetAndEmptyArray() {
        byte[] data = {};
        Executable executable = () -> EndianUtils.getShortBE(data, 1);
        assertThrows(IndexOutOfBoundsException.class, executable);
    }
}
