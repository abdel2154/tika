package org.apache.tika.io;

import org.apache.tika.io.EndianUtils;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.exception.TikaException;

public class EndianUtils_getShortBE_16_0_Test {

    @Test
    public void testGetShortBE() {
        byte[] data = { 0x00, 0x01 };
        short expected = 0x0100;
        short result = EndianUtils.getShortBE(data);
        assertEquals(expected, result);
    }

    @Test
    public void testGetShortBEWithOffset() {
        byte[] data = { 0x00, 0x01, 0x02, 0x03 };
        short expected = 0x0100;
        short result = EndianUtils.getShortBE(data, 1);
        assertEquals(expected, result);
    }

    @Test
    public void testGetShortBEWithEmptyArray() {
        byte[] data = {};
        Executable executable = () -> EndianUtils.getShortBE(data);
        assertThrows(IndexOutOfBoundsException.class, executable);
    }

    @Test
    public void testGetShortBEWithOffsetAndEmptyArray() {
        byte[] data = {};
        Executable executable = () -> EndianUtils.getShortBE(data, 1);
        assertThrows(IndexOutOfBoundsException.class, executable);
    }
}
