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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Property;
import org.apache.tika.metadata.TikaCoreProperties;

/**
 * Tests ecrits a la main (IFT3913, tache 2) pour tuer les mutants PIT
 * qui survivent aux tests originaux et aux tests generes par ChatUniTest.
 * Chaque test est documente dans le README a la racine du depot.
 */
public class FilenameUtilsManualTest {

    private static Metadata only(Property key, String value) {
        Metadata m = new Metadata();
        m.set(key, value);
        return m;
    }

    private static Metadata only(String key, String value) {
        Metadata m = new Metadata();
        m.set(key, value);
        return m;
    }

    @Test
    public void testSuffixLengthBoundary() {
        // point + 4 caracteres = 5 : accepte ; point + 5 caracteres = 6 : refuse
        assertEquals(".abcd", FilenameUtils.getSuffixFromPath("file.abcd"));
        assertEquals("", FilenameUtils.getSuffixFromPath("file.abcde"));
    }

    @Test
    public void testEmbeddedNameFallbackOrder() {
        assertEquals("report.pdf", FilenameUtils.getSanitizedEmbeddedFileName(
                only(TikaCoreProperties.ORIGINAL_RESOURCE_NAME, "report.pdf"), ".bin", 50));
        assertEquals("internal.doc", FilenameUtils.getSanitizedEmbeddedFileName(
                only(TikaCoreProperties.INTERNAL_PATH, "/a/b/internal.doc"), ".bin", 50));
        assertEquals("rId5.png", FilenameUtils.getSanitizedEmbeddedFileName(
                only(TikaCoreProperties.EMBEDDED_RELATIONSHIP_ID, "rId5.png"), ".bin", 50));
        assertEquals("z.txt", FilenameUtils.getSanitizedEmbeddedFileName(
                only(TikaCoreProperties.EMBEDDED_RESOURCE_PATH, "/x/y/z.txt"), ".bin", 50));
    }

    @Test
    public void testEmbeddedPathFallbackOrder() {
        assertEquals("report.pdf", FilenameUtils.getSanitizedEmbeddedFilePath(
                only(TikaCoreProperties.ORIGINAL_RESOURCE_NAME, "report.pdf"), ".bin", 50));
        assertEquals("dir/sub/file.txt", FilenameUtils.getSanitizedEmbeddedFilePath(
                only(TikaCoreProperties.INTERNAL_PATH, "dir/sub/file.txt"), ".bin", 50));
        assertEquals("a/b.txt", FilenameUtils.getSanitizedEmbeddedFilePath(
                only(TikaCoreProperties.RESOURCE_NAME_KEY, "a/b.txt"), ".bin", 50));
        assertEquals("rId1.xml", FilenameUtils.getSanitizedEmbeddedFilePath(
                only(TikaCoreProperties.EMBEDDED_RELATIONSHIP_ID, "rId1.xml"), ".bin", 50));
    }

    @Test
    public void testCalculateExtension() {
        assertEquals(".png", FilenameUtils.calculateExtension(
                only(Metadata.CONTENT_TYPE, "image/png"), ".bin"));
        assertEquals(".dflt", FilenameUtils.calculateExtension(new Metadata(), ".dflt"));
        assertEquals(".bin", FilenameUtils.calculateExtension(
                only(Metadata.CONTENT_TYPE, "application/x-ift3913-inconnu"), ".dflt"));
    }

    @Test
    public void testDegenerateNamesReturnNull() {
        // le nom n'est qu'une extension : pas de partie nom utilisable
        assertNull(FilenameUtils.getSanitizedEmbeddedFileName(
                only(TikaCoreProperties.RESOURCE_NAME_KEY, ".pdf"), ".bin", 50));
        // le caractere nul est remplace par un espace : le chemin devient vide
        assertNull(FilenameUtils.getSanitizedEmbeddedFilePath(
                only(TikaCoreProperties.RESOURCE_NAME_KEY, "\u0000"), ".bin", 50));
        // le dernier segment est "." : aucun nom de fichier
        assertNull(FilenameUtils.getSanitizedEmbeddedFilePath(
                only(TikaCoreProperties.RESOURCE_NAME_KEY, "a/."), ".bin", 50));
    }

    @Test
    public void testNameExactlyMaxLength() {
        // la partie nom "abcde" fait exactement maxLength = 5 : elle ne doit pas etre tronquee
        assertEquals("abcde.txt", FilenameUtils.getSanitizedEmbeddedFileName(
                only(TikaCoreProperties.RESOURCE_NAME_KEY, "abcde.txt"), ".bin", 5));
    }

    @Test
    public void testPathExactlyMaxLength() {
        // le chemin complet "dir/file.txt" fait exactement 12 caracteres : il est conserve
        assertEquals("dir/file.txt", FilenameUtils.getSanitizedEmbeddedFilePath(
                only(TikaCoreProperties.RESOURCE_NAME_KEY, "dir/file.txt"), ".bin", 12));
        // chemin trop long, mais la partie nom "abcdefgh" fait exactement 8 : nom seul, non tronque
        assertEquals("abcdefgh.txt", FilenameUtils.getSanitizedEmbeddedFilePath(
                only(TikaCoreProperties.RESOURCE_NAME_KEY, "dir/abcdefgh.txt"), ".bin", 8));
    }
}
