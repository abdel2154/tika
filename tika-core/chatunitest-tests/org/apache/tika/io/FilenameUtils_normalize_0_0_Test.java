package org.apache.tika.io;

import org.apache.tika.io.FilenameUtils;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tika.extractor.EmbeddedDocumentUtil;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.utils.StringUtils;

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
        assertEquals("example%20txt", FilenameUtils.normalize("example txt"));
        assertEquals("example%2Ftxt", FilenameUtils.normalize("example/txt"));
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
        assertEquals(".example%20txt", FilenameUtils.normalize(".example txt"));
        assertEquals(".example%2Ftxt", FilenameUtils.normalize(".example/txt"));
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
        assertEquals("example.%20txt", FilenameUtils.normalize("example. txt"));
        assertEquals("example.%2Ftxt", FilenameUtils.normalize("example./txt"));
        assertEquals("example.%3Ftxt", FilenameUtils.normalize("example./?txt"));
        assertEquals("example.%3Atxt", FilenameUtils.normalize("example./:txt"));
        assertEquals("example.%2Atxt", FilenameUtils.normalize("example./*txt"));
        assertEquals("example.%3Ctxt", FilenameUtils.normalize("example./<txt"));
        assertEquals("example.%3Etxt", FilenameUtils.normalize("example./>txt"));
        assertEquals("example.%7Ctxt", FilenameUtils.normalize("example./|txt"));
        assertEquals("example.%22txt", FilenameUtils.normalize("example./\"txt"));
        assertEquals("example.%27txt", FilenameUtils.normalize("example./'txt"));
    }

    @Test
    public void testNormalizeWithLeadingAndTrailingDot() {
        assertEquals(".example.", FilenameUtils.normalize(".example."));
        assertEquals(".example.%20txt", FilenameUtils.normalize(".example. txt"));
        assertEquals(".example.%2Ftxt", FilenameUtils.normalize(".example./txt"));
        assertEquals(".example.%3Ftxt", FilenameUtils.normalize(".example./?txt"));
        assertEquals(".example.%3Atxt", FilenameUtils.normalize(".example./:txt"));
        assertEquals(".example.%2Atxt", FilenameUtils.normalize(".example./*txt"));
        assertEquals(".example.%3Ctxt", FilenameUtils.normalize(".example./<txt"));
        assertEquals(".example.%3Etxt", FilenameUtils.normalize(".example./>txt"));
        assertEquals(".example.%7Ctxt", FilenameUtils.normalize(".example./|txt"));
        assertEquals(".example.%22txt", FilenameUtils.normalize(".example./\"txt"));
        assertEquals(".example.%27txt", FilenameUtils.normalize(".example./'txt"));
    }
}
