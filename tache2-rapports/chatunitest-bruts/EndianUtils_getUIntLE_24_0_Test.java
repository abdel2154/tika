package org.apache.tika.io;

import org.apache.tika.io.EndianUtils;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.InputStream;
import org.apache.tika.exception.TikaException;

@ExtendWith(MockitoExtension.class)
public class EndianUtils_getUIntLE_24_0_Test {

    @Test
    public void testGetUIntLE() throws IOException {
        byte[] data = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
        long expected = 0x04030201L;
        long result = EndianUtils.getUIntLE(data);
        assertEquals(expected, result);
    }

    @Test
    public void testGetUIntLEWithOffset() throws IOException {
        byte[] data = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
        long expected = 0x04030201L;
        long result = EndianUtils.getUIntLE(data, 0);
        assertEquals(expected, result);
    }

    @Test
    public void testGetUIntLEWithOffsetAndData() throws IOException {
        byte[] data = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
        long expected = 0x04030201L;
        long result = EndianUtils.getUIntLE(data, 0);
        assertEquals(expected, result);
    }
}
