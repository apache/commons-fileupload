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
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.apache.commons.fileupload.util.EncodingConstants;
import static org.apache.commons.fileupload.util.EncodingConstants.US_ASCII_CHARSET;

import org.junit.Test;

/**
 * @since 1.3
 */
public final class QuotedPrintableDecoderTest {

    /**
     * Tests decoding of empty string.
     * @throws java.io.IOException if an I/O exception occurs
     */
    @Test
    public void emptyDecode() throws IOException {
        assertEncoded("", "");
    }

    /**
     * Tests decoding of plain text.
     * @throws java.io.IOException if an I/O exception occurs
     */
    @Test
    public void plainDecode() throws IOException {
        // spaces are allowed in encoded data
        // There are special rules for trailing spaces; these are not currently implemented.
        assertEncoded("The quick brown fox jumps over the lazy dog.", "The quick brown fox jumps over the lazy dog.");
    }

    /**
     * Tests decoding of basic encoding.
     * @throws java.io.IOException if an I/O exception occurs
     */
    @Test
    public void basicEncodeDecode() throws IOException {
        assertEncoded("= Hello there =\r\n", "=3D Hello there =3D=0D=0A");
    }

    /**
     * Tests decoding of invalid quoted printable encoding.
     * @throws UnsupportedEncodingException if
     * {@link EncodingConstants#US_ASCII_CHARSET} is not supported
     */
    @Test
    public void invalidQuotedPrintableEncoding() throws UnsupportedEncodingException {
        assertIOException("truncated escape sequence", "YWJjMTIzXy0uKn4hQCMkJV4mKCkre31cIlxcOzpgLC9bXQ==");
    }

    /**
     * Tests unsafe decoding.
     * @throws java.io.IOException if an I/O exception occurs
     */
    @Test
    public void unsafeDecode() throws IOException {
        assertEncoded("=\r\n", "=3D=0D=0A");
    }

    /**
     * Tests decoding of lower case.
     * @throws java.io.IOException if an I/O exception occurs
     */
    @Test
    public void unsafeDecodeLowerCase() throws IOException {
        assertEncoded("=\r\n", "=3d=0d=0a");
    }

    /**
     * Tests decoding of invalid chars.
     * @throws java.io.IOException if an I/O exception occurs
     */
    @Test(expected = IOException.class)
    public void invalidCharDecode() throws IOException {
        assertEncoded("=\r\n", "=3D=XD=XA");
    }

    /**
     * This is NOT supported by Commons-Codec, see CODEC-121.
     *
     * @throws java.io.IOException if an I/O exception occurs
     * @see <a href="https://issues.apache.org/jira/browse/CODEC-121">CODEC-121</a>
     */
    @Test
    public void softLineBreakDecode() throws IOException {
        assertEncoded("If you believe that truth=beauty, then surely "
                + "mathematics is the most beautiful branch of philosophy.",
                "If you believe that truth=3Dbeauty, then "
                        + "surely=20=\r\nmathematics is the most beautiful "
                        + "branch of philosophy.");
    }

    /**
     * Tests invalid soft break at position 1.
     * @throws UnsupportedEncodingException if
     * {@link EncodingConstants#US_ASCII_CHARSET} is not supported
     */
    @Test
    public void invalidSoftBreak1() throws UnsupportedEncodingException {
        assertIOException("CR must be followed by LF", "=\r\r");
    }

    /**
     * Tests invalid soft break at position 2.
     * @throws UnsupportedEncodingException if
     * {@link EncodingConstants#US_ASCII_CHARSET} is not supported
     */
    @Test
    public void invalidSoftBreak2() throws UnsupportedEncodingException {
        assertIOException("CR must be followed by LF", "=\rn");
    }

    /**
     * Tests truncated escape.
     * @throws UnsupportedEncodingException if
     * {@link EncodingConstants#US_ASCII_CHARSET} is not supported
     */
    @Test
    public void truncatedEscape() throws UnsupportedEncodingException {
        assertIOException("truncated", "=1");
    }

    /**
     * Decodes encoded text and asserts that it's equals to the expected text.
     * @param clearText the expected text
     * @param encoded the encoded text
     * @throws UnsupportedEncodingException if
     * {@link EncodingConstants#US_ASCII_CHARSET} is not supported
     */
    private static void assertEncoded(String clearText, String encoded) throws UnsupportedEncodingException,
            IOException {
        byte[] expected = clearText.getBytes(US_ASCII_CHARSET);

        ByteArrayOutputStream out = new ByteArrayOutputStream(encoded.length());
        byte[] encodedData = encoded.getBytes(US_ASCII_CHARSET);
        QuotedPrintableDecoder.decode(encodedData, out);
        byte[] actual = out.toByteArray();

        assertArrayEquals(expected, actual);
    }

    /**
     * Asserts that an {@code IOException} is thrown with the right cause
     * message when invalid encoded text is decoded.
     * @param messageText the expected message
     * @param encoded the invalid encoded text
     * @throws UnsupportedEncodingException if
     * {@link EncodingConstants#US_ASCII_CHARSET} is not supported
     */
    private static void assertIOException(String messageText, String encoded) throws UnsupportedEncodingException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(encoded.length());
        byte[] encodedData = encoded.getBytes(US_ASCII_CHARSET);
        try {
            QuotedPrintableDecoder.decode(encodedData, out);
            fail("Expected IOException");
        } catch (IOException e) {
            String em = e.getMessage();
            assertTrue("Expected to find " + messageText + " in '" + em + "'",
                    em.contains(messageText));
        }
    }

}
