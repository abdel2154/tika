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

import java.lang.reflect.Method;

import org.junit.jupiter.api.*;
import org.mockito.*;

// Test genere par ChatUniTest (selon l'outil : "generated successfully"), corrige a la main pour IFT3913.
public class EndianUtils_getUIntBE_27_0_Test {

    @Test
    public void testGetUIntBE() throws Exception {
        EndianUtils endianUtils = new EndianUtils();
        Method method = EndianUtils.class.getDeclaredMethod("getUIntBE", byte[].class, int.class);
        method.setAccessible(true);
        byte[] data = { 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00 };
        // CORRECTION 1 : le 0x01 est a l'indice 3 : a partir de l'indice 4 on lit 00 00 00 00 (= 0) ;
        //                on lit donc a partir de 0 pour obtenir 0x00000001
        int offset = 0;
        long result = (long) method.invoke(endianUtils, data, offset);
        assertEquals(1L, result);
    }
}
