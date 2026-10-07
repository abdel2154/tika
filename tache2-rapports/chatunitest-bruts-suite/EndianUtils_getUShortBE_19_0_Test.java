package org.apache.tika.io;

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
public class EndianUtils_getUShortBE_19_0_Test {

    @Test
    public void testGetUShortBE() {
        byte[] data = { 0x01, 0x02 };
        int offset = 0;
        int expected = 258;
        int result = EndianUtils.getUShortBE(data, offset);
        assertEquals(expected, result);
    }
}
