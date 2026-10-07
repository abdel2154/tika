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
public class EndianUtils_getShortLE_12_0_Test {

    @Mock
    private byte[] data;

    @Test
    public void testGetShortLE() {
        // Arrange
        short expected = 1234;
        when(data[0]).thenReturn((byte) 0x34);
        when(data[1]).thenReturn((byte) 0x12);
        // Act
        short result = EndianUtils.getShortLE(data, 0);
        // Assert
        assertEquals(expected, result);
    }
}
