package org.apache.tika.io;

import org.apache.tika.io.EndianUtils;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.exception.TikaException;

@ExtendWith(MockitoExtension.class)
public class EndianUtils_getIntBE_23_0_Test {

    @Mock
    private InputStream inputStream;

    @BeforeEach
    public void setUp() {
        // Set up any necessary mock behavior here
    }

    @Test
    public void testGetIntBE() throws IOException {
        // Arrange
        byte[] data = { 0x01, 0x02, 0x03, 0x04 };
        int offset = 0;
        int expected = 0x01020304;
        // Act
        int result = EndianUtils.getIntBE(data, offset);
        // Assert
        assertEquals(expected, result);
    }

    @Test
    public void testGetIntBEWithOffset() throws IOException {
        // Arrange
        byte[] data = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
        int offset = 4;
        int expected = 0x05060708;
        // Act
        int result = EndianUtils.getIntBE(data, offset);
        // Assert
        assertEquals(expected, result);
    }

    @Test
    public void testGetIntBEWithNegativeOffset() {
        // Arrange
        byte[] data = { 0x01, 0x02, 0x03, 0x04 };
        int offset = -1;
        // Act & Assert
        assertThrows(IndexOutOfBoundsException.class, () -> EndianUtils.getIntBE(data, offset));
    }

    @Test
    public void testGetIntBEWithOffsetGreaterThanArrayLength() {
        // Arrange
        byte[] data = { 0x01, 0x02, 0x03, 0x04 };
        int offset = 4;
        // Act & Assert
        assertThrows(IndexOutOfBoundsException.class, () -> EndianUtils.getIntBE(data, offset));
    }
}
