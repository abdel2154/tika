package org.apache.tika.io;

import org.apache.tika.io.FilenameUtils;
import org.junit.jupiter.api.function.Executable;
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

public class FilenameUtils_getSuffixFromPath_2_0_Test {

    @Test
    public void testGetSuffixFromPath() throws Exception {
        // Test case 1: Path with valid suffix
        String path1 = "example.txt";
        String expectedSuffix1 = ".txt";
        String actualSuffix1 = invokePrivateMethod(FilenameUtils.class, "getSuffixFromPath", path1);
        assertEquals(expectedSuffix1, actualSuffix1);
        // Test case 2: Path with invalid suffix
        String path2 = "example.docx";
        String expectedSuffix2 = "";
        String actualSuffix2 = invokePrivateMethod(FilenameUtils.class, "getSuffixFromPath", path2);
        assertEquals(expectedSuffix2, actualSuffix2);
        // Test case 3: Path with no suffix
        String path3 = "example";
        String expectedSuffix3 = "";
        String actualSuffix3 = invokePrivateMethod(FilenameUtils.class, "getSuffixFromPath", path3);
        assertEquals(expectedSuffix3, actualSuffix3);
        // Test case 4: Path with multiple dots
        String path4 = "example.file.txt";
        String expectedSuffix4 = ".txt";
        String actualSuffix4 = invokePrivateMethod(FilenameUtils.class, "getSuffixFromPath", path4);
        assertEquals(expectedSuffix4, actualSuffix4);
        // Test case 5: Path with ASCII numeric suffix
        String path5 = ".123";
        String expectedSuffix5 = ".123";
        String actualSuffix5 = invokePrivateMethod(FilenameUtils.class, "getSuffixFromPath", path5);
        assertEquals(expectedSuffix5, actualSuffix5);
        // Test case 6: Path with non-ASCII numeric suffix
        String path6 = ".abc";
        String expectedSuffix6 = "";
        String actualSuffix6 = invokePrivateMethod(FilenameUtils.class, "getSuffixFromPath", path6);
        assertEquals(expectedSuffix6, actualSuffix6);
        // Test case 7: Path with empty string
        String path7 = "";
        String expectedSuffix7 = "";
        String actualSuffix7 = invokePrivateMethod(FilenameUtils.class, "getSuffixFromPath", path7);
        assertEquals(expectedSuffix7, actualSuffix7);
        // Test case 8: Path with null
        String path8 = null;
        String expectedSuffix8 = "";
        String actualSuffix8 = invokePrivateMethod(FilenameUtils.class, "getSuffixFromPath", path8);
        assertEquals(expectedSuffix8, actualSuffix8);
    }

    private String invokePrivateMethod(Class<?> clazz, String methodName, Object... args) throws Exception {
        java.lang.reflect.Method method = clazz.getDeclaredMethod(methodName, String.class);
        method.setAccessible(true);
        return (String) method.invoke(null, args);
    }
}
