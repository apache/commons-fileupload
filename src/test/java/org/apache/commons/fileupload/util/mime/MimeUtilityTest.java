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

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

/**
 * Use the online <a href="http://dogmamix.com/MimeHeadersDecoder/">MimeHeadersDecoder</a>
 * to validate expected values.
 *
 * @since 1.3
 */
public final class MimeUtilityTest {

    /**
     * Tests no need to decode.
     * @throws java.io.UnsupportedEncodingException if the decoding fails
     * because the encoding is not supported
     */
    @Test
    public void noNeedToDecode() throws UnsupportedEncodingException {
        assertEncoded("abc", "abc");
    }

    /**
     * Tests decoding of quoted UTF-8 printable characters.
     * @throws java.io.UnsupportedEncodingException if the decoding fails
     * because the encoding is not supported
     */
    @Test
    public void decodeUtf8QuotedPrintableEncoded() throws UnsupportedEncodingException {
        assertEncoded(" hé! àèôu !!!", "=?UTF-8?Q?_h=C3=A9!_=C3=A0=C3=A8=C3=B4u_!!!?=");
    }

    /**
     * Tests decoding of base64 encoded UTF-8 characters.
     * @throws java.io.UnsupportedEncodingException if the decoding fails
     * because the encoding is not supported
     */
    @Test
    public void decodeUtf8Base64Encoded() throws UnsupportedEncodingException {
        assertEncoded(" hé! àèôu !!!", "=?UTF-8?B?IGjDqSEgw6DDqMO0dSAhISE=?=");
    }

    /**
     * Tests decoding of base64 encoded ISO-88591 characters.
     * @throws java.io.UnsupportedEncodingException if the decoding fails
     * because the encoding is not supported
     */
    @Test
    public void decodeIso88591Base64Encoded() throws UnsupportedEncodingException {
        assertEncoded("If you can read this you understand the example.",
                "=?ISO-8859-1?B?SWYgeW91IGNhbiByZWFkIHRoaXMgeW8=?= "
                        + "=?ISO-8859-2?B?dSB1bmRlcnN0YW5kIHRoZSBleGFtcGxlLg==?=\"\r\n");
    }

    /**
     * Test decoding of base64 encoded ISO-88591 characters with whitespace.
     * @throws java.io.UnsupportedEncodingException if the decoding fails
     * because the encoding is not supported
     */
    @Test
    public void decodeIso88591Base64EncodedWithWhiteSpace() throws UnsupportedEncodingException {
        assertEncoded("If you can read this you understand the example.",
                "=?ISO-8859-1?B?SWYgeW91IGNhbiByZWFkIHRoaXMgeW8=?=\t  \r\n   "
                        + "=?ISO-8859-2?B?dSB1bmRlcnN0YW5kIHRoZSBleGFtcGxlLg==?=\"\r\n");
    }

    /**
     * Test that an {@code UnsupportedEncodingException} is thrown during
     * decoding of invalid input.
     * @throws UnsupportedEncodingException expected
     */
    @Test(expected = UnsupportedEncodingException.class)
    public void decodeInvalidEncoding() throws UnsupportedEncodingException {
        MimeUtility.decodeText("=?invalid?B?xyz-?=");
    }

    /**
     * Asserts that encoding is the excepted one.
     * @param expected the excepted
     * @param encoded the actual
     * @throws UnsupportedEncodingException if the specified encoding is not
     * supported
     */
    private static void assertEncoded(String expected, String encoded) throws UnsupportedEncodingException {
        assertEquals(expected, MimeUtility.decodeText(encoded));
    }
}
