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
import org.mockito.*;

public class FilenameUtils_normalize_0_0_Test {

    @Test
    public void testNormalizeWithNullInput() {
        assertThrows(IllegalArgumentException.class, () -> FilenameUtils.normalize(null));
    }

    @Test
    public void testNormalizeWithEmptyString() {
        assertEquals("", FilenameUtils.normalize(""));
    }

    @Test
    public void testNormalizeWithNoReservedCharacters() {
        assertEquals("example.txt", FilenameUtils.normalize("example.txt"));
    }

    @Test
    public void testNormalizeWithReservedCharacters() {
        assertEquals("example txt", FilenameUtils.normalize("example txt"));
        assertEquals("example/txt", FilenameUtils.normalize("example/txt"));
        assertEquals("example%3Ftxt", FilenameUtils.normalize("example?txt"));
        assertEquals("example%3Atxt", FilenameUtils.normalize("example:txt"));
        assertEquals("example%2Atxt", FilenameUtils.normalize("example*txt"));
        assertEquals("example%3Ctxt", FilenameUtils.normalize("example<txt"));
        assertEquals("example%3Etxt", FilenameUtils.normalize("example>txt"));
        assertEquals("example%7Ctxt", FilenameUtils.normalize("example|txt"));
        assertEquals("example%22txt", FilenameUtils.normalize("example\"txt"));
        assertEquals("example%27txt", FilenameUtils.normalize("example'txt"));
    }

    @Test
    public void testNormalizeWithLeadingDot() {
        assertEquals(".example", FilenameUtils.normalize(".example"));
        assertEquals(".example txt", FilenameUtils.normalize(".example txt"));
        assertEquals(".example/txt", FilenameUtils.normalize(".example/txt"));
        assertEquals(".example%3Ftxt", FilenameUtils.normalize(".example?txt"));
        assertEquals(".example%3Atxt", FilenameUtils.normalize(".example:txt"));
        assertEquals(".example%2Atxt", FilenameUtils.normalize(".example*txt"));
        assertEquals(".example%3Ctxt", FilenameUtils.normalize(".example<txt"));
        assertEquals(".example%3Etxt", FilenameUtils.normalize(".example>txt"));
        assertEquals(".example%7Ctxt", FilenameUtils.normalize(".example|txt"));
        assertEquals(".example%22txt", FilenameUtils.normalize(".example\"txt"));
        assertEquals(".example%27txt", FilenameUtils.normalize(".example'txt"));
    }

    @Test
    public void testNormalizeWithTrailingDot() {
        assertEquals("example.", FilenameUtils.normalize("example."));
        assertEquals("example. txt", FilenameUtils.normalize("example. txt"));
        assertEquals("example./txt", FilenameUtils.normalize("example./txt"));
        assertEquals("example./%3Ftxt", FilenameUtils.normalize("example./?txt"));
        assertEquals("example./%3Atxt", FilenameUtils.normalize("example./:txt"));
        assertEquals("example./%2Atxt", FilenameUtils.normalize("example./*txt"));
        assertEquals("example./%3Ctxt", FilenameUtils.normalize("example./<txt"));
        assertEquals("example./%3Etxt", FilenameUtils.normalize("example./>txt"));
        assertEquals("example./%7Ctxt", FilenameUtils.normalize("example./|txt"));
        assertEquals("example./%22txt", FilenameUtils.normalize("example./\"txt"));
        assertEquals("example./%27txt", FilenameUtils.normalize("example./'txt"));
    }

    @Test
    public void testNormalizeWithLeadingAndTrailingDot() {
        assertEquals(".example.", FilenameUtils.normalize(".example."));
        assertEquals(".example. txt", FilenameUtils.normalize(".example. txt"));
        assertEquals(".example./txt", FilenameUtils.normalize(".example./txt"));
        assertEquals(".example./%3Ftxt", FilenameUtils.normalize(".example./?txt"));
        assertEquals(".example./%3Atxt", FilenameUtils.normalize(".example./:txt"));
        assertEquals(".example./%2Atxt", FilenameUtils.normalize(".example./*txt"));
        assertEquals(".example./%3Ctxt", FilenameUtils.normalize(".example./<txt"));
        assertEquals(".example./%3Etxt", FilenameUtils.normalize(".example./>txt"));
        assertEquals(".example./%7Ctxt", FilenameUtils.normalize(".example./|txt"));
        assertEquals(".example./%22txt", FilenameUtils.normalize(".example./\"txt"));
        assertEquals(".example./%27txt", FilenameUtils.normalize(".example./'txt"));
    }
}
