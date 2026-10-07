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

// Test genere par ChatUniTest (tentative ExecutionError_1), corrige a la main pour IFT3913.
@ExtendWith(MockitoExtension.class)
public class FilenameUtils_calculateExtension_10_0_Test {

    // CORRECTION 1 : mocks MimeTypes/MimeType supprimes : ils ne sont jamais injectes (calculateExtension
    //                utilise le registre statique MimeTypes.getDefaultMimeTypes()) et Mockito strict
    //                signale des stubs inutiles (UnnecessaryStubbingException)

    @Test
    public void testCalculateExtensionWithMimeType() throws IOException {
        Metadata metadata = new Metadata();
        metadata.set(Metadata.CONTENT_TYPE, "image/png");
        String defaultValue = "default.ext";
        String result = FilenameUtils.calculateExtension(metadata, defaultValue);
        // CORRECTION 2 : la Javadoc precise que l'extension inclut le point initial
        assertEquals(".png", result);
    }

    @Test
    public void testCalculateExtensionWithEmptyMimeType() throws IOException {
        Metadata metadata = new Metadata();
        metadata.set(Metadata.CONTENT_TYPE, "");
        String defaultValue = "default.ext";
        String result = FilenameUtils.calculateExtension(metadata, defaultValue);
        // CORRECTION 3 : la valeur par defaut n'est renvoyee que si Content-Type est absent (null) ;
        //                un type present mais inutilisable donne ".bin" (code de calculateExtension)
        assertEquals(".bin", result);
    }

    @Test
    public void testCalculateExtensionWithNullMetadata() throws IOException {
        String defaultValue = "default.ext";
        // CORRECTION 4 : metadata null n'est pas gere par la methode (metadata.get(...) -> NullPointerException)
        assertThrows(NullPointerException.class, () -> FilenameUtils.calculateExtension(null, defaultValue));
    }

    @Test
    public void testCalculateExtensionWithWhitespaceDefaultValue() throws IOException {
        Metadata metadata = new Metadata();
        metadata.set(Metadata.CONTENT_TYPE, "image/png");
        String result = FilenameUtils.calculateExtension(metadata, " ");
        // CORRECTION 5 : extension avec le point initial
        assertEquals(".png", result);
    }

    @Test
    public void testCalculateExtensionWithNullDefaultValue() throws IOException {
        Metadata metadata = new Metadata();
        metadata.set(Metadata.CONTENT_TYPE, "image/png");
        String result = FilenameUtils.calculateExtension(metadata, null);
        // CORRECTION 6 : extension avec le point initial
        assertEquals(".png", result);
    }

    @Test
    public void testCalculateExtensionWithWhitespaceMimeType() throws IOException {
        Metadata metadata = new Metadata();
        metadata.set(Metadata.CONTENT_TYPE, " image/png ");
        String defaultValue = "default.ext";
        String result = FilenameUtils.calculateExtension(metadata, defaultValue);
        // CORRECTION 7 : extension avec le point initial
        assertEquals(".png", result);
    }

    @Test
    public void testCalculateExtensionWithInvalidMimeType() throws IOException {
        Metadata metadata = new Metadata();
        metadata.set(Metadata.CONTENT_TYPE, "invalid/mime");
        String defaultValue = "default.ext";
        String result = FilenameUtils.calculateExtension(metadata, defaultValue);
        // CORRECTION 8 : type inconnu (sans extension enregistree) -> ".bin", pas la valeur par defaut
        assertEquals(".bin", result);
    }

    @Test
    public void testCalculateExtensionWithEmptyDefaultValue() throws IOException {
        Metadata metadata = new Metadata();
        metadata.set(Metadata.CONTENT_TYPE, "image/png");
        String result = FilenameUtils.calculateExtension(metadata, "");
        // CORRECTION 9 : extension avec le point initial
        assertEquals(".png", result);
    }

    @Test
    public void testCalculateExtensionWithNullMimeType() throws IOException {
        Metadata metadata = new Metadata();
        String defaultValue = "default.ext";
        String result = FilenameUtils.calculateExtension(metadata, defaultValue);
        assertEquals(defaultValue, result);
    }

    @Test
    public void testCalculateExtensionWithNoExtension() throws IOException {
        Metadata metadata = new Metadata();
        metadata.set(Metadata.CONTENT_TYPE, "application/octet-stream");
        String defaultValue = "default.ext";
        String result = FilenameUtils.calculateExtension(metadata, defaultValue);
        assertEquals(".bin", result);
    }
}
