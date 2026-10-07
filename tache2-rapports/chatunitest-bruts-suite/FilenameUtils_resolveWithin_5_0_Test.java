package org.apache.tika.io;

import org.apache.tika.io.FilenameUtils;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tika.extractor.EmbeddedDocumentUtil;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.utils.StringUtils;

@ExtendWith(MockitoExtension.class)
public class FilenameUtils_resolveWithin_5_0_Test {

    @Mock
    private Path mockPath;

    @BeforeEach
    public void setUp() {
        when(mockPath.normalize()).thenReturn(mockPath);
    }

    @Test
    public void testResolveWithin_OutsideOfDirectory() throws IOException {
        Path outsidePath = Paths.get("/outside/directory");
        when(mockPath.resolve("test.txt")).thenReturn(outsidePath);
        assertThrows(IOException.class, () -> FilenameUtils.resolveWithin(mockPath, "test.txt"));
    }
}
