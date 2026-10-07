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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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

    // ---- Tests ajoutes lors de la seconde passe (apres la generation des methodes restantes) ----

    @Test
    public void testBlankNamePartReturnsNull() {
        // l'extension ".pdf" est reconnue, mais la partie nom n'est qu'un espace : aucun nom utilisable
        assertNull(FilenameUtils.getSanitizedEmbeddedFileName(
                only(TikaCoreProperties.RESOURCE_NAME_KEY, " .pdf"), ".bin", 50));
        assertNull(FilenameUtils.getSanitizedEmbeddedFilePath(
                only(TikaCoreProperties.RESOURCE_NAME_KEY, " .pdf"), ".bin", 50));
    }

    @Test
    public void testOnlyRealDriveLettersAreStripped() {
        // aucune de ces chaines n'est un prefixe de lecteur Windows ("X:" avec X dans A..Z) :
        // rien ne doit etre retire ; ':' devient '/' et le '/' final est supprime
        assertEquals("A.bin", FilenameUtils.getSanitizedEmbeddedFilePath(
                only(TikaCoreProperties.RESOURCE_NAME_KEY, "A"), ".bin", 50));
        assertEquals("AB.bin", FilenameUtils.getSanitizedEmbeddedFilePath(
                only(TikaCoreProperties.RESOURCE_NAME_KEY, "AB"), ".bin", 50));
        // '@' = 'A' - 1 et '[' = 'Z' + 1 : voisins immediats de l'intervalle A..Z
        assertEquals("@.bin", FilenameUtils.getSanitizedEmbeddedFilePath(
                only(TikaCoreProperties.RESOURCE_NAME_KEY, "@:"), ".bin", 50));
        assertEquals("[.bin", FilenameUtils.getSanitizedEmbeddedFilePath(
                only(TikaCoreProperties.RESOURCE_NAME_KEY, "[:"), ".bin", 50));
    }

    @Test
    public void testResolveWithinExistingAndMissingFiles(@TempDir Path dir) throws IOException {
        // fichier reel a l'interieur du dossier : il est accepte et renvoye tel quel
        Files.createFile(dir.resolve("inside.txt"));
        assertEquals(dir.resolve("inside.txt").normalize(),
                FilenameUtils.resolveWithin(dir, "inside.txt"));
        // fichier absent : la verification par toRealPath() ne doit pas etre tentee
        assertEquals(dir.resolve("absent.txt").normalize(),
                FilenameUtils.resolveWithin(dir, "absent.txt"));
    }

    @Test
    public void testResolveWithinRejectsSymlinkEscape(@TempDir Path dir, @TempDir Path outside)
            throws IOException {
        Path link = dir.resolve("lien");
        try {
            Files.createSymbolicLink(link, outside);
        } catch (IOException | UnsupportedOperationException e) {
            // Windows sans privilege : impossible de creer un lien, le test est ignore
            Assumptions.abort("liens symboliques non disponibles : " + e);
        }
        // "lien" est textuellement dans dir, mais pointe hors de dir : il doit etre refuse
        assertThrows(IOException.class, () -> FilenameUtils.resolveWithin(dir, "lien"));
    }

    @Test
    public void testTildeNameKnownDefect() {
        // DEFAUT DE TIKA : commons-io renvoie une longueur de prefixe > longueur du nom pour "~"
        // (2) et "~user" (6), et path.substring(prefixLength) leve une exception. Ce test fige
        // le comportement actuel ; il faudra changer l'oracle quand le defaut sera corrige.
        assertThrows(StringIndexOutOfBoundsException.class,
                () -> FilenameUtils.getSanitizedEmbeddedFileName(
                        only(TikaCoreProperties.RESOURCE_NAME_KEY, "~"), ".bin", 50));
        assertThrows(StringIndexOutOfBoundsException.class,
                () -> FilenameUtils.getSanitizedEmbeddedFilePath(
                        only(TikaCoreProperties.RESOURCE_NAME_KEY, "~user"), ".bin", 50));
    }
}
