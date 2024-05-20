package org.apache.commons.fileupload2.core;

import org.junit.jupiter.api.Assertions;
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
    /**
     * Test if the <code>content-length</code> Value is numeric.
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
     * Test if the <code>content-length</code> Value is not numeric
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
     * Test if the given <code>character-encoding</code> is a valid CharEncoding
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
     * Test if the given <code>character-encoding</code> is an invalid CharEncoding
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
     * Test the <code>toString()</code> Output
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

    /**
     * Test if the <code>content-type</code> is <code>multipart/related</code>
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
     * Test if the <code>content-type</code> is not <code>multipart/related</code>
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

    private static final class MockRequestContext extends AbstractRequestContext<Object> {
        private final String characterEncoding;
        private final String contentType;
        private final InputStream inputStream;

        private MockRequestContext(Function<String, String> contentLengthString,
                                   LongSupplier contentLengthDefault,
                                   Object request,
                                   String characterEncoding,
                                   String contentType,
                                   InputStream inputStream) {
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
}
