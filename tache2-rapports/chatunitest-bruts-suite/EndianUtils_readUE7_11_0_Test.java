package org.apache.tika.io;

import org.apache.tika.io.EndianUtils;
import org.junit.jupiter.api.function.Executable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.InputStream;
import org.apache.tika.exception.TikaException;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class EndianUtils_readUE7_11_0_Test {

    @Test
    public void testReadUE7() throws IOException {
        // Test case 1: Normal case
        byte[] data1 = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
        InputStream stream1 = new ByteArrayInputStream(data1);
        assertEquals(0x0102030405060708L, EndianUtils.readUE7(stream1));
        // Test case 2: Continues bit set
        byte[] data2 = { (byte) 0x81, (byte) 0x82, (byte) 0x83, (byte) 0x84, (byte) 0x85, (byte) 0x86, (byte) 0x87, (byte) 0x88 };
        InputStream stream2 = new ByteArrayInputStream(data2);
        assertEquals(0x0102030405060708L, EndianUtils.readUE7(stream2));
        // Test case 3: Continues bit not set
        byte[] data3 = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
        InputStream stream3 = new ByteArrayInputStream(data3);
        assertEquals(0x0102030405060708L, EndianUtils.readUE7(stream3));
        // Test case 4: Buffer underun
        byte[] data4 = {};
        InputStream stream4 = new ByteArrayInputStream(data4);
        assertThrows(IOException.class, () -> EndianUtils.readUE7(stream4));
    }
}
