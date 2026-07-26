/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.fileupload2.javax;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.core.FileItemInput;
import org.apache.commons.fileupload2.core.FileItemInputIterator;
import org.apache.commons.fileupload2.core.FileUploadByteCountLimitException;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.fileupload2.core.FileUploadFileCountLimitException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for {@link JakartaServletFileUpload#getItemIterator(HttpServletRequest)}.
 * <p>
 * Covers normal use-cases as well as edge cases such as size limits, file count limits, empty payloads, and non-multipart requests.
 * </p>
 */
class JavaxServletFileUploadGetItemIteratorTest {

    /** Boundary value used throughout these tests. */
    private static final String BOUNDARY = "---1234";
    /** Content-type header value that matches {@link #BOUNDARY}. */
    private static final String CONTENT_TYPE = "multipart/form-data; boundary=" + BOUNDARY;
    /** Content-type header value for multipart/related requests that matches {@link #BOUNDARY}. */
    private static final String CONTENT_TYPE_RELATED = "multipart/related; boundary=" + BOUNDARY;
    /** Boundary value used for nested multipart/mixed parts. */
    private static final String MIXED_BOUNDARY = "---9876";

    /**
     * Builds a complete multipart body that contains a single form-data part holding a nested multipart/mixed part with {@code fileCount} identical files.
     *
     * @param fileCount number of nested files to include
     * @return raw multipart bytes encoded in US-ASCII
     */
    private static byte[] buildMixedFileParts(final int fileCount) {
        final var sb = new StringBuilder();
        sb.append("--").append(BOUNDARY).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"files\"\r\n");
        sb.append("Content-Type: multipart/mixed; boundary=").append(MIXED_BOUNDARY).append("\r\n");
        sb.append("\r\n");
        for (int i = 1; i <= fileCount; i++) {
            sb.append("--").append(MIXED_BOUNDARY).append("\r\n");
            sb.append("Content-Disposition: attachment; filename=\"file").append(i).append(".txt\"\r\n");
            sb.append("Content-Type: text/plain\r\n");
            sb.append("\r\n");
            sb.append("Content of file ").append(i).append("\r\n");
        }
        sb.append("--").append(MIXED_BOUNDARY).append("--\r\n");
        sb.append("--").append(BOUNDARY).append("--\r\n");
        return sb.toString().getBytes(StandardCharsets.US_ASCII);
    }

    /**
     * Builds a complete multipart body that contains {@code fileCount} identical file parts.
     *
     * @param fileCount number of parts to include
     * @return raw multipart bytes encoded in US-ASCII
     */
    private static byte[] buildMultiFileParts(final int fileCount) {
        final var sb = new StringBuilder();
        for (int i = 1; i <= fileCount; i++) {
            sb.append("--").append(BOUNDARY).append("\r\n");
            sb.append("Content-Disposition: form-data; name=\"file").append(i).append("\"; filename=\"file").append(i).append(".txt\"\r\n");
            sb.append("Content-Type: text/plain\r\n");
            sb.append("\r\n");
            sb.append("Content of file ").append(i).append("\r\n");
        }
        sb.append("--").append(BOUNDARY).append("--\r\n");
        return sb.toString().getBytes(StandardCharsets.US_ASCII);
    }

    /**
     * Builds a complete multipart/related body that contains {@code partCount} identical parts.
     *
     * @param partCount number of parts to include
     * @return raw multipart bytes encoded in US-ASCII
     */
    private static byte[] buildMultiRelatedParts(final int partCount) {
        final var sb = new StringBuilder();
        for (int i = 1; i <= partCount; i++) {
            sb.append("--").append(BOUNDARY).append("\r\n");
            sb.append("Content-Type: text/plain\r\n");
            sb.append("Content-ID: <part").append(i).append("@example.org>\r\n");
            sb.append("\r\n");
            sb.append("Content of part ").append(i).append("\r\n");
        }
        sb.append("--").append(BOUNDARY).append("--\r\n");
        return sb.toString().getBytes(StandardCharsets.US_ASCII);
    }

    /**
     * Builds a complete multipart body that contains exactly one file part.
     *
     * @param fieldName   form-field name attribute
     * @param fileName    original file name
     * @param fileContent body content for the part
     * @return raw multipart bytes encoded in US-ASCII
     */
    private static byte[] buildSingleFilePart(final String fieldName, final String fileName, final String fileContent) {
        // @formatter:off
        final var body = "--" + BOUNDARY + "\r\n"
                + "Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"\r\n"
                + "Content-Type: text/plain\r\n"
                + "\r\n"
                + fileContent + "\r\n"
                + "--" + BOUNDARY + "--\r\n";
        // @formatter:on
        return body.getBytes(StandardCharsets.US_ASCII);
    }

    /**
     * Builds a complete multipart body that contains exactly one form-field part (no filename).
     *
     * @param fieldName  form-field name attribute
     * @param fieldValue value of the form field
     * @return raw multipart bytes encoded in US-ASCII
     */
    private static byte[] buildSingleFormField(final String fieldName, final String fieldValue) {
        // @formatter:off
        final var body = "--" + BOUNDARY + "\r\n"
                + "Content-Disposition: form-data; name=\"" + fieldName + "\"\r\n"
                + "\r\n"
                + fieldValue + "\r\n"
                + "--" + BOUNDARY + "--\r\n";
        // @formatter:on
        return body.getBytes(StandardCharsets.US_ASCII);
    }

    /**
     * Creates a new {@link JakartaServletFileUpload} instance with the default {@link DiskFileItemFactory}.
     */
    private static JavaxServletFileUpload<?, ?> newUpload() {
        return new JavaxServletFileUpload<>(DiskFileItemFactory.builder().get());
    }

    /**
     * A multipart body with no parts (just the closing boundary): the iterator must have no items.
     */
    @Test
    void testEmptyMultipartBody() throws Exception {
        final var body = ("--" + BOUNDARY + "--\r\n").getBytes(StandardCharsets.US_ASCII);
        final HttpServletRequest request = new JavaxMockHttpServletRequest(body, CONTENT_TYPE);
        final var upload = newUpload();
        final FileItemInputIterator iter = upload.getItemIterator(request);
        assertFalse(iter.hasNext(), "Expected no items in an empty multipart body");
    }

    /**
     * When the number of uploaded files exceeds {@code maxFileCount}, iterating must throw a {@link FileUploadFileCountLimitException} with the expected
     * permitted count. Tested for maxFileCount values of 1, 2, 4, and 8.
     */
    @ParameterizedTest
    @ValueSource(longs = { 1, 2, 4, 8 })
    void testFileCountLimitExceededThrowsException(final long maxFileCount) throws Exception {
        final var body = buildMultiFileParts((int) maxFileCount + 1);
        final HttpServletRequest request = new JavaxMockHttpServletRequest(body, CONTENT_TYPE);
        final var upload = newUpload();
        upload.setMaxFileCount(maxFileCount);
        final FileItemInputIterator iter = upload.getItemIterator(request);
        // Consume allowed items first
        for (int i = 0; i < maxFileCount; i++) {
            assertTrue(iter.hasNext());
            iter.next();
        }
        // The next hasNext() call triggers the limit check
        final var ex = assertThrows(FileUploadFileCountLimitException.class, iter::hasNext);
        assertEquals(maxFileCount, ex.getPermitted());
    }

    /**
     * A file part: the content read from the item's InputStream must match the uploaded body.
     */
    @Test
    void testFilePartContentIsReadable() throws Exception {
        final var expectedContent = "Hello, World!";
        final var body = buildSingleFilePart("file", "test.txt", expectedContent);
        final HttpServletRequest request = new JavaxMockHttpServletRequest(body, CONTENT_TYPE);
        final var upload = newUpload();
        final FileItemInputIterator iter = upload.getItemIterator(request);
        assertTrue(iter.hasNext());
        final FileItemInput item = iter.next();
        try (InputStream is = item.getInputStream()) {
            final var content = new String(is.readAllBytes(), StandardCharsets.US_ASCII);
            assertEquals(expectedContent, content);
        }
    }

    /**
     * A part with a non-ASCII file name encoded in the content-disposition header is returned with the original name value as parsed from the header.
     */
    @Test
    void testFilePartWithSpecialCharactersInFieldName() throws Exception {
        // @formatter:off
        final var bodyStr = "--" + BOUNDARY + "\r\n"
                + "Content-Disposition: form-data; name=\"field-with-dashes\"; filename=\"résumé.txt\"\r\n"
                + "Content-Type: text/plain\r\n"
                + "\r\n"
                + "content\r\n"
                + "--" + BOUNDARY + "--\r\n";
        // @formatter:on
        final var body = bodyStr.getBytes(StandardCharsets.UTF_8);
        final HttpServletRequest request = new JavaxMockHttpServletRequest(body, CONTENT_TYPE);
        final var upload = newUpload();
        final FileItemInputIterator iter = upload.getItemIterator(request);
        assertTrue(iter.hasNext());
        final FileItemInput item = iter.next();
        assertEquals("field-with-dashes", item.getFieldName());
        assertFalse(iter.hasNext());
    }

    /**
     * When a single file part exceeds {@code maxFileSize}, reading its content must throw a {@link FileUploadByteCountLimitException}.
     */
    @Test
    void testFileSizeLimitExceededThrowsException() throws Exception {
        final var largeContent = "A".repeat(1024); // 1 KiB
        final var body = buildSingleFilePart("bigFile", "large.txt", largeContent);
        final HttpServletRequest request = new JavaxMockHttpServletRequest(body, CONTENT_TYPE);
        final var upload = newUpload();
        upload.setMaxFileSize(10); // allow only 10 bytes
        final FileItemInputIterator iter = upload.getItemIterator(request);
        assertTrue(iter.hasNext());
        final FileItemInput item = iter.next();
        assertThrows(FileUploadByteCountLimitException.class, () -> {
            try (InputStream is = item.getInputStream()) {
                // Must read enough bytes to trigger the limit
                is.readAllBytes();
            }
        });
    }

    /**
     * {@link FileItemInputIterator#forEachRemaining(org.apache.commons.io.function.IOConsumer)} visits every item exactly once.
     */
    @Test
    void testForEachRemainingVisitsAllItems() throws Exception {
        final int fileCount = 3;
        final var body = buildMultiFileParts(fileCount);
        final HttpServletRequest request = new JavaxMockHttpServletRequest(body, CONTENT_TYPE);
        final var upload = newUpload();
        final var count = new int[1];
        upload.getItemIterator(request).forEachRemaining(item -> count[0]++);
        assertEquals(fileCount, count[0]);
    }

    /**
     * A form-field part: the content read from the item's InputStream must match the submitted value.
     */
    @Test
    void testFormFieldContentIsReadable() throws Exception {
        final var body = buildSingleFormField("answer", "42");
        final HttpServletRequest request = new JavaxMockHttpServletRequest(body, CONTENT_TYPE);
        final var upload = newUpload();
        final FileItemInputIterator iter = upload.getItemIterator(request);
        assertTrue(iter.hasNext());
        final FileItemInput item = iter.next();
        try (InputStream is = item.getInputStream()) {
            final var content = new String(is.readAllBytes(), StandardCharsets.US_ASCII);
            assertEquals("42", content);
        }
    }

    /**
     * Calling {@link JavaxServletFileUpload#getItemIterator(HttpServletRequest)} twice on different requests must each return an independent iterator.
     */
    @Test
    void testGetItemIteratorIsReentrant() throws Exception {
        final var body1 = buildSingleFilePart("first", "first.txt", "first");
        final var body2 = buildMultiFileParts(2);
        final HttpServletRequest request1 = new JavaxMockHttpServletRequest(body1, CONTENT_TYPE);
        final HttpServletRequest request2 = new JavaxMockHttpServletRequest(body2, CONTENT_TYPE);
        final var upload = newUpload();
        final FileItemInputIterator iter1 = upload.getItemIterator(request1);
        final FileItemInputIterator iter2 = upload.getItemIterator(request2);
        // iter1 should yield one item
        assertTrue(iter1.hasNext());
        assertEquals("first", iter1.next().getFieldName());
        assertFalse(iter1.hasNext());
        // iter2 should independently yield two items
        final var count = new int[1];
        iter2.forEachRemaining(item -> count[0]++);
        assertEquals(2, count[0]);
    }

    /**
     * The returned iterator must be non-null for a well-formed multipart request.
     */
    @Test
    void testGetItemIteratorReturnsNonNull() throws Exception {
        final var body = buildSingleFilePart("f", "a.txt", "data");
        final HttpServletRequest request = new JavaxMockHttpServletRequest(body, CONTENT_TYPE);
        final var upload = newUpload();
        assertNotNull(upload.getItemIterator(request));
    }

    /**
     * {@link FileItemInputIterator#getFileSizeMax()} and {@link FileItemInputIterator#getSizeMax()} must reflect the values configured on the upload instance
     * when no override has been applied.
     */
    @Test
    void testIteratorInheritsUploadSizeLimits() throws Exception {
        final var body = buildSingleFilePart("f", "a.txt", "data");
        final HttpServletRequest request = new JavaxMockHttpServletRequest(body, CONTENT_TYPE);
        final var upload = newUpload();
        upload.setMaxFileSize(100_000L);
        upload.setMaxSize(500_000L);
        final FileItemInputIterator iter = upload.getItemIterator(request);
        assertEquals(100_000L, iter.getFileSizeMax());
        assertEquals(500_000L, iter.getSizeMax());
    }

    /**
     * {@link FileItemInputIterator#setFileSizeMax(long)} and {@link FileItemInputIterator#setSizeMax(long)} must override the values from the upload instance.
     */
    @Test
    void testIteratorSizeLimitOverrides() throws Exception {
        final var body = buildSingleFilePart("f", "a.txt", "data");
        final HttpServletRequest request = new JavaxMockHttpServletRequest(body, CONTENT_TYPE);
        final var upload = newUpload();
        upload.setMaxFileSize(100_000L);
        upload.setMaxSize(500_000L);
        final FileItemInputIterator iter = upload.getItemIterator(request);
        iter.setFileSizeMax(200L);
        iter.setSizeMax(800L);
        assertEquals(200L, iter.getFileSizeMax());
        assertEquals(800L, iter.getSizeMax());
    }

    /**
     * Mixed parts: the iterator correctly reports form-field vs. file parts.
     */
    @Test
    void testMixedFormFieldAndFileParts() throws Exception {
        // @formatter:off
        final var bodyStr = "--" + BOUNDARY + "\r\n"
                + "Content-Disposition: form-data; name=\"field1\"\r\n"
                + "\r\n"
                + "value1\r\n"
                + "--" + BOUNDARY + "\r\n"
                + "Content-Disposition: form-data; name=\"file1\"; filename=\"upload.txt\"\r\n"
                + "Content-Type: text/plain\r\n"
                + "\r\n"
                + "file content\r\n"
                + "--" + BOUNDARY + "\r\n"
                + "Content-Disposition: form-data; name=\"field2\"\r\n"
                + "\r\n"
                + "value2\r\n"
                + "--" + BOUNDARY + "--\r\n";
        // @formatter:on
        final var body = bodyStr.getBytes(StandardCharsets.US_ASCII);
        final HttpServletRequest request = new JavaxMockHttpServletRequest(body, CONTENT_TYPE);
        final var upload = newUpload();
        final FileItemInputIterator iter = upload.getItemIterator(request);
        // Part 1: form field
        assertTrue(iter.hasNext());
        final FileItemInput part1 = iter.next();
        assertEquals("field1", part1.getFieldName());
        assertTrue(part1.isFormField());
        // Part 2: file
        assertTrue(iter.hasNext());
        final FileItemInput part2 = iter.next();
        assertEquals("file1", part2.getFieldName());
        assertFalse(part2.isFormField());
        // Part 3: form field
        assertTrue(iter.hasNext());
        final FileItemInput part3 = iter.next();
        assertEquals("field2", part3.getFieldName());
        assertTrue(part3.isFormField());
        assertFalse(iter.hasNext());
    }

    /**
     * When the number of files in a nested multipart/mixed part exceeds {@code maxFileCount}, iterating must throw a {@link FileUploadFileCountLimitException}
     * with the expected permitted count. Tested for maxFileCount values of 1, 2, 4, and 8.
     */
    @ParameterizedTest
    @ValueSource(longs = { 1, 2, 4, 8 })
    void testMultipartMixedFileCountLimitExceededThrowsException(final long maxFileCount) throws Exception {
        final var body = buildMixedFileParts((int) maxFileCount + 1);
        final HttpServletRequest request = new JavaxMockHttpServletRequest(body, CONTENT_TYPE);
        final var upload = newUpload();
        upload.setMaxFileCount(maxFileCount);
        final FileItemInputIterator iter = upload.getItemIterator(request);
        // Consume allowed items first
        for (int i = 0; i < maxFileCount; i++) {
            assertTrue(iter.hasNext());
            iter.next();
        }
        // The next hasNext() call triggers the limit check
        final var ex = assertThrows(FileUploadFileCountLimitException.class, iter::hasNext);
        assertEquals(maxFileCount, ex.getPermitted());
    }

    /**
     * When the number of parts in a multipart/related request exceeds {@code maxFileCount}, iterating must throw a {@link FileUploadFileCountLimitException}
     * with the expected permitted count. Tested for maxFileCount values of 1, 2, 4, and 8.
     */
    @ParameterizedTest
    @ValueSource(longs = { 1, 2, 4, 8 })
    void testMultipartRelatedFileCountLimitExceededThrowsException(final long maxFileCount) throws Exception {
        final var body = buildMultiRelatedParts((int) maxFileCount + 1);
        final HttpServletRequest request = new JavaxMockHttpServletRequest(body, CONTENT_TYPE_RELATED);
        final var upload = newUpload();
        upload.setMaxFileCount(maxFileCount);
        final FileItemInputIterator iter = upload.getItemIterator(request);
        // Consume allowed items first
        for (int i = 0; i < maxFileCount; i++) {
            assertTrue(iter.hasNext());
            iter.next();
        }
        // The next hasNext() call triggers the limit check
        final var ex = assertThrows(FileUploadFileCountLimitException.class, iter::hasNext);
        assertEquals(maxFileCount, ex.getPermitted());
    }

    /**
     * Multiple parts: the iterator must return every part in transmission order.
     */
    @Test
    void testMultipleFileParts() throws Exception {
        final int fileCount = 4;
        final var body = buildMultiFileParts(fileCount);
        final HttpServletRequest request = new JavaxMockHttpServletRequest(body, CONTENT_TYPE);
        final var upload = newUpload();
        final FileItemInputIterator iter = upload.getItemIterator(request);
        for (int i = 1; i <= fileCount; i++) {
            assertTrue(iter.hasNext(), "Expected item " + i);
            final FileItemInput item = iter.next();
            assertEquals("file" + i, item.getFieldName());
            assertEquals("file" + i + ".txt", item.getName());
        }
        assertFalse(iter.hasNext(), "Expected no more items after " + fileCount);
    }

    /**
     * A request whose content-type is not {@code multipart/*} must cause a {@link FileUploadException}.
     */
    @Test
    void testNonMultipartContentTypeThrowsException() {
        final var body = "hello=world".getBytes(StandardCharsets.US_ASCII);
        final HttpServletRequest request = new JavaxMockHttpServletRequest(body, "application/x-www-form-urlencoded");
        final var upload = newUpload();
        assertThrows(FileUploadException.class, () -> upload.getItemIterator(request));
    }

    /**
     * A part with an explicit but empty {@code filename=""} attribute must be treated as a file part (not a form field) with an empty name string.
     */
    @Test
    void testPartWithEmptyFileName() throws Exception {
        // @formatter:off
        final var bodyStr = "--" + BOUNDARY + "\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"\"\r\n"
                + "Content-Type: application/octet-stream\r\n"
                + "\r\n"
                + "\r\n"
                + "--" + BOUNDARY + "--\r\n";
        // @formatter:on
        final var body = bodyStr.getBytes(StandardCharsets.US_ASCII);
        final HttpServletRequest request = new JavaxMockHttpServletRequest(body, CONTENT_TYPE);
        final var upload = newUpload();
        final FileItemInputIterator iter = upload.getItemIterator(request);
        assertTrue(iter.hasNext());
        final FileItemInput item = iter.next();
        assertEquals("file", item.getFieldName());
        assertEquals("", item.getName());
        assertFalse(item.isFormField());
        assertFalse(iter.hasNext());
    }

    /**
     * A single-file part: the iterator must yield exactly one item whose properties match the part headers.
     */
    @Test
    void testSingleFilePart() throws Exception {
        final var body = buildSingleFilePart("upload", "hello.txt", "Hello, World!");
        final HttpServletRequest request = new JavaxMockHttpServletRequest(body, CONTENT_TYPE);
        final var upload = newUpload();
        final FileItemInputIterator iter = upload.getItemIterator(request);
        assertTrue(iter.hasNext(), "Expected at least one item");
        final FileItemInput item = iter.next();
        assertEquals("upload", item.getFieldName());
        assertEquals("hello.txt", item.getName());
        assertEquals("text/plain", item.getContentType());
        assertFalse(item.isFormField(), "Part with a filename should not be a form field");
        assertFalse(iter.hasNext(), "Expected no more items");
    }

    /**
     * A single form-field part (no filename): the iterator must yield one item marked as a form field.
     */
    @Test
    void testSingleFormField() throws Exception {
        final var body = buildSingleFormField("myField", "myValue");
        final HttpServletRequest request = new JavaxMockHttpServletRequest(body, CONTENT_TYPE);
        final var upload = newUpload();
        final FileItemInputIterator iter = upload.getItemIterator(request);
        assertTrue(iter.hasNext(), "Expected at least one item");
        final FileItemInput item = iter.next();
        assertEquals("myField", item.getFieldName());
        assertNull(item.getName(), "Form field should have no file name");
        assertTrue(item.isFormField(), "Part without a filename should be a form field");
        assertFalse(iter.hasNext(), "Expected no more items");
    }
}
