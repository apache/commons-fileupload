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
package org.apache.commons.fileupload2.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.UnsupportedEncodingException;

import org.junit.jupiter.api.Test;

/**
 * Use the online <a href="https://dogmamix.com/MimeHeadersDecoder/">MimeHeadersDecoder</a> to validate expected values.
 */
public final class MimeUtilityTestCase {

    private static void assertEncoded(final String expected, final String encoded) throws Exception {
        assertEquals(expected, MimeUtils.decodeText(encoded));
    }

    @Test
    void testDecodeInvalidEncoding() {
        assertThrows(UnsupportedEncodingException.class, () -> MimeUtils.decodeText("=?invalid?B?xyz-?="));
    }

    @Test
    void testDecodeIso88591Base64Encoded() throws Exception {
        assertEncoded("If you can read this you understand the example.",
                "=?ISO-8859-1?B?SWYgeW91IGNhbiByZWFkIHRoaXMgeW8=?= " + "=?ISO-8859-2?B?dSB1bmRlcnN0YW5kIHRoZSBleGFtcGxlLg==?=\"\r\n");
    }

    @Test
    void testDecodeIso88591Base64EncodedWithWhiteSpace() throws Exception {
        assertEncoded("If you can read this you understand the example.",
                "=?ISO-8859-1?B?SWYgeW91IGNhbiByZWFkIHRoaXMgeW8=?=\t  \r\n   =?ISO-8859-" + "2?B?dSB1bmRlcnN0YW5kIHRoZSBleGFtcGxlLg==?=\"\r\n");
    }

    @Test
    void testDecodeUtf8Base64Encoded() throws Exception {
        assertEncoded(" h\u00e9! \u00e0\u00e8\u00f4u !!!", "=?UTF-8?B?IGjDqSEgw6DDqMO0dSAhISE=?=");
    }

    @Test
    void testDecodeUtf8QuotedPrintableEncoded() throws Exception {
        assertEncoded(" h\u00e9! \u00e0\u00e8\u00f4u !!!", "=?UTF-8?Q?_h=C3=A9!_=C3=A0=C3=A8=C3=B4u_!!!?=");
    }

    @Test
    void testNoNeedToDecode() throws Exception {
        assertEncoded("abc", "abc");
    }
}
