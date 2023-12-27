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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.UnsupportedEncodingException;

import org.junit.jupiter.api.Test;

/**
 * The expected characters are encoded in UTF16, while the actual characters may be encoded in UTF-8/ISO-8859-1
 *
 * RFC 5987 recommends to support both UTF-8 and ISO 8859-1. Test values are taken from https://tools.ietf.org/html/rfc5987#section-3.2.2
 */
public final class RFC2231UtilityTestCase {

    private static void assertEncoded(final String expected, final String encoded) throws Exception {
        assertEquals(expected, RFC2231Utils.decodeText(encoded));
    }

    @Test
    public void testDecodeInvalidEncoding() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> RFC2231Utils.decodeText("abc'en'hello"));
    }

    @Test
    public void testDecodeIso88591() throws Exception {
        assertEncoded("\u00A3 rate", "iso-8859-1'en'%A3%20rate"); // "£ rate"
    }

    @Test
    public void testDecodeUtf8() throws Exception {
        assertEncoded("\u00a3 \u0061\u006e\u0064 \u20ac \u0072\u0061\u0074\u0065\u0073", "UTF-8''%c2%a3%20and%20%e2%82%ac%20rates"); // "£ and € rates"
    }

    @Test
    public void testHasEncodedValue() {
        final var nameWithAsteriskAtEnd = "paramname*";
        assertTrue(RFC2231Utils.hasEncodedValue(nameWithAsteriskAtEnd));

        final var nameWithAsteriskNotAtEnd = "param*name";
        assertFalse(RFC2231Utils.hasEncodedValue(nameWithAsteriskNotAtEnd));

        final var nameWithoutAsterisk = "paramname";
        assertFalse(RFC2231Utils.hasEncodedValue(nameWithoutAsterisk));
    }

    @Test
    public void testNoNeedToDecode() throws Exception {
        assertEncoded("abc", "abc");
    }

    @Test
    public void testStripDelimiter() {
        final var nameWithAsteriskAtEnd = "paramname*";
        assertEquals("paramname", RFC2231Utils.stripDelimiter(nameWithAsteriskAtEnd));

        final var nameWithAsteriskNotAtEnd = "param*name";
        assertEquals("param*name", RFC2231Utils.stripDelimiter(nameWithAsteriskNotAtEnd));

        final var nameWithTwoAsterisks = "param*name*";
        assertEquals("param*name", RFC2231Utils.stripDelimiter(nameWithTwoAsterisks));

        final var nameWithoutAsterisk = "paramname";
        assertEquals("paramname", RFC2231Utils.stripDelimiter(nameWithoutAsterisk));
    }
}
