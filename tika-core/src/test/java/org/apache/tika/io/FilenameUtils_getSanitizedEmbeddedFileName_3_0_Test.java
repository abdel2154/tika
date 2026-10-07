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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;

// Test genere par ChatUniTest (tentative ExecutionError_1), corrige a la main pour IFT3913.
@ExtendWith(MockitoExtension.class)
public class FilenameUtils_getSanitizedEmbeddedFileName_3_0_Test {

    // CORRECTION 1 : mock strict -> PotentialStubbingProblem des que calculateExtension lit
    //                Metadata.CONTENT_TYPE (non stubbe) : on rend le mock indulgent (lenient)
    @Mock(strictness = Mock.Strictness.LENIENT)
    private Metadata metadata;

    @BeforeEach
    public void setUp() {
        when(metadata.get(TikaCoreProperties.RESOURCE_NAME_KEY)).thenReturn("example.txt");
        when(metadata.get(TikaCoreProperties.INTERNAL_PATH)).thenReturn(null);
        when(metadata.get(TikaCoreProperties.EMBEDDED_RELATIONSHIP_ID)).thenReturn(null);
        when(metadata.get(TikaCoreProperties.EMBEDDED_RESOURCE_PATH)).thenReturn(null);
        when(metadata.get(TikaCoreProperties.ORIGINAL_RESOURCE_NAME)).thenReturn(null);
    }

    @Test
    public void testGetSanitizedEmbeddedFileName_withNoExtensionInput() throws IOException {
        FilenameUtils filenameUtils = new FilenameUtils();
        when(metadata.get(TikaCoreProperties.RESOURCE_NAME_KEY)).thenReturn("example");
        // CORRECTION 2 : l'extension par defaut est ajoutee telle quelle (sans ajout de point) :
        //                comme pour calculateExtension, elle doit inclure le point -> ".txt"
        String result = filenameUtils.getSanitizedEmbeddedFileName(metadata, ".txt", 10);
        assertEquals("example.txt", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFileName_withNullExtensionInput() throws IOException {
        FilenameUtils filenameUtils = new FilenameUtils();
        when(metadata.get(TikaCoreProperties.RESOURCE_NAME_KEY)).thenReturn("example");
        String result = filenameUtils.getSanitizedEmbeddedFileName(metadata, null, 10);
        // CORRECTION 3 : une extension par defaut null n'est pas remplacee : elle est concatenee
        //                (namePart + extension) -> "examplenull" (comportement reel du code)
        assertEquals("examplenull", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFileName_withEmptyExtensionInput() throws IOException {
        FilenameUtils filenameUtils = new FilenameUtils();
        when(metadata.get(TikaCoreProperties.RESOURCE_NAME_KEY)).thenReturn("example");
        String result = filenameUtils.getSanitizedEmbeddedFileName(metadata, "", 10);
        // CORRECTION 4 : extension par defaut vide -> aucun suffixe n'est ajoute
        assertEquals("example", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFileName_withWhitespaceInput() throws IOException {
        FilenameUtils filenameUtils = new FilenameUtils();
        when(metadata.get(TikaCoreProperties.RESOURCE_NAME_KEY)).thenReturn(" example ");
        // CORRECTION 5 : extension par defaut avec le point initial (voir CORRECTION 2)
        String result = filenameUtils.getSanitizedEmbeddedFileName(metadata, ".txt", 10);
        assertEquals("example.txt", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFileName_withLongInput() throws IOException {
        FilenameUtils filenameUtils = new FilenameUtils();
        when(metadata.get(TikaCoreProperties.RESOURCE_NAME_KEY)).thenReturn("a".repeat(100));
        String result = filenameUtils.getSanitizedEmbeddedFileName(metadata, "txt", 10);
        // CORRECTION 6 : troncature = substring(0, maxLength - extension.length() - 3) = 10 - 3 - 3 = 4
        assertEquals("a".repeat(4) + "..." + "txt", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFileName_withReservedCharactersInput() throws IOException {
        FilenameUtils filenameUtils = new FilenameUtils();
        when(metadata.get(TikaCoreProperties.RESOURCE_NAME_KEY)).thenReturn("example?*<>|\":'");
        String result = filenameUtils.getSanitizedEmbeddedFileName(metadata, "txt", 10);
        // CORRECTION 7 : ':' est un separateur de chemin (noms Mac) : seul le dernier segment "'" est garde,
        //                normalize le remplace par %27, puis l'extension par defaut est ajoutee
        assertEquals("%27txt", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFileName_withNullMetadataInput() throws IOException {
        FilenameUtils filenameUtils = new FilenameUtils();
        // CORRECTION 8 : metadata null n'est pas gere (NullPointerException), la methode ne renvoie pas null
        assertThrows(NullPointerException.class,
                () -> filenameUtils.getSanitizedEmbeddedFileName(null, "txt", 10));
    }

    @Test
    public void testGetSanitizedEmbeddedFileName_withBlankMetadataInput() throws IOException {
        FilenameUtils filenameUtils = new FilenameUtils();
        when(metadata.get(TikaCoreProperties.RESOURCE_NAME_KEY)).thenReturn("");
        String result = filenameUtils.getSanitizedEmbeddedFileName(metadata, "txt", 10);
        assertNull(result);
    }

    @Test
    public void testGetSanitizedEmbeddedFileName_withNullDefaultExtensionInput() throws IOException {
        FilenameUtils filenameUtils = new FilenameUtils();
        when(metadata.get(TikaCoreProperties.RESOURCE_NAME_KEY)).thenReturn("example");
        String result = filenameUtils.getSanitizedEmbeddedFileName(metadata, null, 10);
        // CORRECTION 9 : meme cas que CORRECTION 3 (test en double)
        assertEquals("examplenull", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFileName_withValidInput() throws IOException {
        FilenameUtils filenameUtils = new FilenameUtils();
        String result = filenameUtils.getSanitizedEmbeddedFileName(metadata, "txt", 10);
        assertEquals("example.txt", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFileName_withNullInput() throws IOException {
        FilenameUtils filenameUtils = new FilenameUtils();
        when(metadata.get(TikaCoreProperties.RESOURCE_NAME_KEY)).thenReturn(null);
        String result = filenameUtils.getSanitizedEmbeddedFileName(metadata, "txt", 10);
        assertEquals(null, result);
    }

    @Test
    public void testGetSanitizedEmbeddedFileName_withEmptyInput() throws IOException {
        FilenameUtils filenameUtils = new FilenameUtils();
        when(metadata.get(TikaCoreProperties.RESOURCE_NAME_KEY)).thenReturn("");
        String result = filenameUtils.getSanitizedEmbeddedFileName(metadata, "txt", 10);
        assertEquals(null, result);
    }

    @Test
    public void testGetSanitizedEmbeddedFileName_withProtocolInput() throws IOException {
        FilenameUtils filenameUtils = new FilenameUtils();
        when(metadata.get(TikaCoreProperties.RESOURCE_NAME_KEY)).thenReturn("http://example.com/example.txt");
        String result = filenameUtils.getSanitizedEmbeddedFileName(metadata, "txt", 10);
        assertEquals("example.txt", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFileName_withEmptyMaxLengthInput() throws IOException {
    }
}
