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
public class EndianUtils_getUIntBE_26_0_Test {

    @Mock
    private InputStream inputStream;

    @Test
    public void testGetUIntBEWithOffset() throws IOException, TikaException {
        // Arrange
        byte[] data = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01 };
        int offset = 4;
        long expected = 1L;
        // Act
        long result = EndianUtils.getUIntBE(data, offset);
        // Assert
        assertEquals(expected, result);
    }
}
