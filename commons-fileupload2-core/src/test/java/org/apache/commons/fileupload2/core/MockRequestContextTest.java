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

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.function.Function;
import java.util.function.LongSupplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AbstractRequestContext}
 */
public class MockRequestContextTest {
    private static final class MockRequestContext extends AbstractRequestContext<Object> {
        private final String characterEncoding;
        private final String contentType;
        private final InputStream inputStream;

        private MockRequestContext(final Function<String, String> contentLengthString,
                                   final LongSupplier contentLengthDefault,
                                   final Object request,
                                   final String characterEncoding,
                                   final String contentType,
                                   final InputStream inputStream) {
            super(contentLengthString, contentLengthDefault, request);
            this.characterEncoding = characterEncoding;
            this.contentType = contentType;
            this.inputStream = inputStream;
        }

        /**
         * Gets the character encoding for the request.
         *
         * @return The character encoding for the request.
         */
        @Override
        public String getCharacterEncoding() {
            return characterEncoding;
        }

        /**
         * Gets the content type of the request.
         *
         * @return The content type of the request.
         */
        @Override
        public String getContentType() {
            return contentType;
        }

        /**
         * Gets the input stream for the request.
         *
         * @return The input stream for the request.
         */
        @Override
        public InputStream getInputStream() {
            return inputStream;
        }
    }

    /**
     * Test if the given {@code character-encoding} is a valid CharEncoding
     */
    @Test
    public void getCharset() {
        final RequestContext request = new MockRequestContext(
                x -> "1234",
                () -> 5678L,
                "Request",
                "US-ASCII",
                "text/plain",
                null);
        assertEquals(StandardCharsets.US_ASCII, request.getCharset());
    }

    /**
     * Test if the {@code content-length} Value is numeric.
     */
    @Test
    public void getContentLengthByParsing() {
        final RequestContext request = new MockRequestContext(
                x -> "1234",
                () -> 5678L,
                "Request",
                "US-ASCII",
                "text/plain",
                null);
        assertEquals(1234L, request.getContentLength());
    }

    /**
     * Test if the {@code content-length} Value is not numeric
     * and the Default will be taken.
     */
    @Test
    public void getContentLengthDefaultBecauseOfInvalidNumber() {
        final RequestContext request = new MockRequestContext(
                x -> "not-a-number",
                () -> 5678L,
                "Request",
                "US-ASCII",
                "text/plain",
                null);
        assertEquals(5678L, request.getContentLength());
    }

    /**
     * Test if the given {@code character-encoding} is an invalid CharEncoding
     * and leads to {@link UnsupportedCharsetException}
     */
    @Test
    public void getInvalidCharset() {
        final RequestContext request = new MockRequestContext(
                x -> "1234",
                () -> 5678L,
                "Request",
                "invalid-charset",
                "text/plain",
                null);
        assertThrows(UnsupportedCharsetException.class, request::getCharset);
    }

    /**
     * Test if the {@code content-type} is {@code multipart/related}
     */
    @Test
    public void testIsMultipartRelated() {
        final RequestContext request = new MockRequestContext(
                x -> "1234",
                () -> 5678L,
                "Request",
                "US-ASCII",
                "multipart/related; boundary=---1234; type=\"application/xop+xml\"; start-info=\"application/soap+xml\"",
                null);
        assertTrue(request.isMultipartRelated());
    }

    /**
     * Test if the {@code content-type} is not {@code multipart/related}
     */
    @Test
    public void testIsNotMultipartRelated() {
        final RequestContext request = new MockRequestContext(
                x -> "1234",
                () -> 5678L,
                "Request",
                "US-ASCII",
                "text/plain",
                null);
        assertFalse(request.isMultipartRelated());
    }

    /**
     * Test the {@code toString()} Output
     */
    @Test
    public void testToString() {
        final RequestContext request = new MockRequestContext(
                x -> "1234",
                () -> 5678L,
                "Request",
                "US-ASCII",
                "text/plain",
                null);
        assertEquals("MockRequestContext [ContentLength=1234, ContentType=text/plain]", request.toString());
    }
}
