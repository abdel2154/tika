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

import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;

// Test genere par ChatUniTest (tentative CompilationError_1), corrige a la main pour IFT3913.
public class FilenameUtils_getSanitizedEmbeddedFilePath_4_0_Test {

    private FilenameUtils filenameUtils;

    @BeforeEach
    public void setUp() {
        filenameUtils = new FilenameUtils();
    }

    @Test
    public void testGetSanitizedEmbeddedFilePath_withValidPath() throws IOException {
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.EMBEDDED_RESOURCE_PATH, "http://example.com/path/to/file.txt");
        String defaultExtension = "txt";
        int maxLength = 100;
        String result = filenameUtils.getSanitizedEmbeddedFilePath(metadata, defaultExtension, maxLength);
        // CORRECTION 2 : seul le protocole "http://" est retire (removeProtocol) ; l'hote est conserve
        //                comme premier segment du chemin relatif
        assertEquals("example.com/path/to/file.txt", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFilePath_withNullPath() throws IOException {
        Metadata metadata = new Metadata();
        // CORRECTION 1 : appel ambigu set(Property, Date) / set(Property, Calendar) -> (String) null
        metadata.set(TikaCoreProperties.EMBEDDED_RESOURCE_PATH, (String) null);
        String defaultExtension = "txt";
        int maxLength = 100;
        String result = filenameUtils.getSanitizedEmbeddedFilePath(metadata, defaultExtension, maxLength);
        assertNull(result);
    }

    @Test
    public void testGetSanitizedEmbeddedFilePath_withBlankPath() throws IOException {
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.EMBEDDED_RESOURCE_PATH, "   ");
        String defaultExtension = "txt";
        int maxLength = 100;
        String result = filenameUtils.getSanitizedEmbeddedFilePath(metadata, defaultExtension, maxLength);
        assertNull(result);
    }

    @Test
    public void testGetSanitizedEmbeddedFilePath_withProtocol() throws IOException {
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.EMBEDDED_RESOURCE_PATH, "http://example.com/path/to/file.txt");
        String defaultExtension = "txt";
        int maxLength = 100;
        String result = filenameUtils.getSanitizedEmbeddedFilePath(metadata, defaultExtension, maxLength);
        // CORRECTION 3 : idem CORRECTION 2
        assertEquals("example.com/path/to/file.txt", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFilePath_withInvalidProtocol() throws IOException {
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.EMBEDDED_RESOURCE_PATH, "ftp://example.com/path/to/file.txt");
        String defaultExtension = "txt";
        int maxLength = 100;
        String result = filenameUtils.getSanitizedEmbeddedFilePath(metadata, defaultExtension, maxLength);
        // CORRECTION 4 : "ftp://" est aussi un protocole retire (motif [A-Za-z0-9]{1,10}://) ; l'hote reste
        assertEquals("example.com/path/to/file.txt", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFilePath_withEmptyExtension() throws IOException {
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.EMBEDDED_RESOURCE_PATH, "http://example.com/path/to/file");
        String defaultExtension = "";
        int maxLength = 100;
        String result = filenameUtils.getSanitizedEmbeddedFilePath(metadata, defaultExtension, maxLength);
        // CORRECTION 5 : comportement reel du code : sans extension, namePart reprend TOUT le chemin
        //                (namePart = path et non fName), dont les '/' deviennent '_', puis le chemin relatif
        //                est ajoute devant. C'est vraisemblablement un defaut de Tika (voir README).
        //                L'extension par defaut "" n'ajoute rien (et "txt" n'est pas devine).
        assertEquals("example.com/path/to/example.com_path_to_file", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFilePath_withLongPath() throws IOException {
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.EMBEDDED_RESOURCE_PATH, "http://example.com/path/to/very/long/path/to/file.txt");
        String defaultExtension = "txt";
        int maxLength = 10;
        String result = filenameUtils.getSanitizedEmbeddedFilePath(metadata, defaultExtension, maxLength);
        // CORRECTION 6 : le chemin depasse maxLength -> seule la partie nom est renvoyee (Javadoc du code)
        assertEquals("file.txt", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFilePath_withLongFileName() throws IOException {
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.EMBEDDED_RESOURCE_PATH, "http://example.com/path/to/very/long/filename.txt");
        String defaultExtension = "txt";
        int maxLength = 10;
        String result = filenameUtils.getSanitizedEmbeddedFilePath(metadata, defaultExtension, maxLength);
        // CORRECTION 7 : idem : nom seul ; "filename" (8) <= maxLength (10) donc non tronque
        assertEquals("filename.txt", result);
    }

    @Test
    public void testGetSanitizedEmbeddedFilePath_withLongExtension() throws IOException {
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.EMBEDDED_RESOURCE_PATH, "http://example.com/path/to/file.txt");
    }
}
