package org.apache.tika.io;

import org.apache.tika.io.FilenameUtils;
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
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

public class FilenameUtils_getName_1_0_Test {

    @Test
    public void testGetNameWithNullPath() throws Exception {
        String result = FilenameUtils.getName(null);
        assertEquals("", result);
    }

    @Test
    public void testGetNameWithEmptyPath() throws Exception {
        String result = FilenameUtils.getName("");
        assertEquals("", result);
    }

    @Test
    public void testGetNameWithUnixPath() throws Exception {
        String result = FilenameUtils.getName("/home/user/documents/file.txt");
        assertEquals("file.txt", result);
    }

    @Test
    public void testGetNameWithWindowsPath() throws Exception {
        String result = FilenameUtils.getName("C:\\Users\\user\\Documents\\file.txt");
        assertEquals("file.txt", result);
    }

    @Test
    public void testGetNameWithColonPath() throws Exception {
        String result = FilenameUtils.getName("C:somefilename");
        assertEquals("somefilename", result);
    }

    @Test
    public void testGetNameWithDotPath() throws Exception {
        String result = FilenameUtils.getName(".file.txt");
        assertEquals("", result);
    }

    @Test
    public void testGetNameWithDoubleDotPath() throws Exception {
        String result = FilenameUtils.getName("..file.txt");
        assertEquals("", result);
    }

    @Test
    public void testGetNameWithSingleDotPath() throws Exception {
        String result = FilenameUtils.getName(".file.txt");
        assertEquals("", result);
    }

    @Test
    public void testGetNameWithReservedCharactersPath() throws Exception {
        String result = FilenameUtils.getName("file?name*<name>:name|name\"name\'name");
        assertEquals("filename", result);
    }
}
