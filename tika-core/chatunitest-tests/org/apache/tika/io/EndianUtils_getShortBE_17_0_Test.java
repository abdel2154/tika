package org.apache.tika.io;

import org.apache.tika.io.EndianUtils;
import java.io.IOException;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.InputStream;
import org.apache.tika.exception.TikaException;

public class EndianUtils_getShortBE_17_0_Test {

    @Test
    public void testGetShortBE() throws IOException {
        byte[] data = { 0x00, 0x01 };
        int offset = 0;
        short expected = 257;
        short result = EndianUtils.getShortBE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetShortBEWithOffset() throws IOException {
        byte[] data = { 0x01, 0x00, 0x02, 0x00 };
        int offset = 1;
        short expected = 512;
        short result = EndianUtils.getShortBE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetShortBEWithNegativeOffset() throws IOException {
        byte[] data = { 0x00, 0x01 };
        int offset = -1;
        Exception exception = assertThrows(IndexOutOfBoundsException.class, () -> {
            EndianUtils.getShortBE(data, offset);
        });
    }

    @Test
    public void testGetShortBEWithTooLargeOffset() throws IOException {
        byte[] data = { 0x00, 0x01 };
        int offset = 2;
        Exception exception = assertThrows(IndexOutOfBoundsException.class, () -> {
            EndianUtils.getShortBE(data, offset);
        });
    }
}
