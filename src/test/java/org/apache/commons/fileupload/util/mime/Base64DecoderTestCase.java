/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.fileupload.util.mime;

import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

/**
 * @since 1.3
 */
public final class Base64DecoderTestCase {

    private static final String US_ASCII_CHARSET = "US-ASCII";

    /**
     * Tests RFC 4648 section 10 test vectors.
     * <ul>
     * <li>BASE64("") = ""</li>
     * <li>BASE64("f") = "Zg=="</li>
     * <li>BASE64("fo") = "Zm8="</li>
     * <li>BASE64("foo") = "Zm9v"</li>
     * <li>BASE64("foob") = "Zm9vYg=="</li>
     * <li>BASE64("fooba") = "Zm9vYmE="</li>
     * <li>BASE64("foobar") = "Zm9vYmFy"</li>
     * </ul>
     *
     * @see <a href="http://tools.ietf.org/html/rfc4648">http://tools.ietf.org/html/rfc4648</a>
     */
    @Test
    public void rfc4648Section10Decode() throws Exception {
        assertEncoded("", "");
        assertEncoded("f", "Zg==");
        assertEncoded("fo", "Zm8=");
        assertEncoded("foo", "Zm9v");
        assertEncoded("foob", "Zm9vYg==");
        assertEncoded("fooba", "Zm9vYmE=");
        assertEncoded("foobar", "Zm9vYmFy");
    }

    /**
     * Test our decode with pad character in the middle.
     * Returns data up to pad character.
     *
     *
     * @throws Exception if any error occurs while decoding the input string.
     */
    @Test
    public void decodeWithInnerPad() throws Exception {
        assertEncoded("Hello World", "SGVsbG8gV29ybGQ=SGVsbG8gV29ybGQ=");
    }

    private static void assertEncoded(String clearText, String encoded) throws Exception {
        byte[] expected = clearText.getBytes(US_ASCII_CHARSET);

        ByteArrayOutputStream out = new ByteArrayOutputStream(encoded.length());
        byte[] encodedData = encoded.getBytes(US_ASCII_CHARSET);
        Base64Decoder.decode(encodedData, out);
        byte[] actual = out.toByteArray();

        assertArrayEquals(expected, actual);
    }

    /**
     * Throws IOException for non-BASE64 bytes.
     *
     * @throws Exception
     */
    @Test(expected = IOException.class)
    public void nonBase64Bytes() throws Exception {
        final byte[] x = new byte[]{'n', 'A', (byte) 0x9c};
        Base64Decoder.decode(x, new ByteArrayOutputStream());
    }

    @Test(expected = IOException.class)
    public void truncatedString() throws Exception {
        final byte[] x = new byte[]{'n'};
        Base64Decoder.decode(x, new ByteArrayOutputStream());
    }

}
