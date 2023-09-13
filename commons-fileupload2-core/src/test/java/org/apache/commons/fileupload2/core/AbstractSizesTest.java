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
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

/**
 * Unit test for items with varying sizes.
 *
 * @param <AFU> The FileUpload type.
 * @param <R>   The FileUpload request type.
 * @param <I>   The FileItem type.
 * @param <F>   The FileItemFactory type.
 */
public abstract class AbstractSizesTest<AFU extends AbstractFileUpload<R, I, F>, R, I extends FileItem<I>, F extends FileItemFactory<I>>
        extends AbstractTest<AFU, R, I, F> {

    /**
     * Checks, whether limiting the file size works.
     *
     * @throws IOException Test failure.
     */
    @Test
    public void testFileSizeLimit() throws IOException {
        final var content = "This is the content of the file\n";
        final var contentSize = content.getBytes().length;

        // @formatter:off
        final var request =
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"file\"; filename=\"foo.tab\"\r\n" +
            "Content-Type: text/whatever\r\n" +
            "\r\n" +
            content +
            "\r\n" +
            "-----1234--\r\n";
        // @formatter:on

        var upload = newFileUpload();
        upload.setFileSizeMax(-1);
        var req = newMockHttpServletRequest(request, null, null);
        var fileItems = upload.parseRequest(req);
        assertEquals(1, fileItems.size());
        var item = fileItems.get(0);
        assertEquals(content, new String(item.get()));

        upload = newFileUpload();
        upload.setFileSizeMax(40);
        req = newMockHttpServletRequest(request, null, null);
        fileItems = upload.parseRequest(req);
        assertEquals(1, fileItems.size());
        item = fileItems.get(0);
        assertEquals(content, new String(item.get()));

        upload = newFileUpload();
        upload.setFileSizeMax(contentSize);
        req = newMockHttpServletRequest(request, null, null);
        fileItems = upload.parseRequest(req);
        assertEquals(1, fileItems.size());
        item = fileItems.get(0);
        assertEquals(content, new String(item.get()));

        upload = newFileUpload();
        upload.setFileSizeMax(contentSize - 1);
        req = newMockHttpServletRequest(request, null, null);
        try {
            upload.parseRequest(req);
            fail("Expected exception.");
        } catch (final FileUploadByteCountLimitException e) {
            assertEquals(contentSize - 1, e.getPermitted());
        }

        upload = newFileUpload();
        upload.setFileSizeMax(30);
        req = newMockHttpServletRequest(request, null, null);
        try {
            upload.parseRequest(req);
            fail("Expected exception.");
        } catch (final FileUploadByteCountLimitException e) {
            assertEquals(30, e.getPermitted());
        }
    }

    /**
     * Checks, whether a faked Content-Length header is detected.
     *
     * @throws IOException Test failure.
     */
    @Test
    public void testFileSizeLimitWithFakedContentLength() throws IOException {
        // @formatter:off
        final var request =
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"file\"; filename=\"foo.tab\"\r\n" +
            "Content-Type: text/whatever\r\n" +
            "Content-Length: 10\r\n" +
            "\r\n" +
            "This is the content of the file\n" +
            "\r\n" +
            "-----1234--\r\n";
        // @formatter:on

        var upload = newFileUpload();
        upload.setFileSizeMax(-1);
        var req = newMockHttpServletRequest(request, null, null);
        var fileItems = upload.parseRequest(req);
        assertEquals(1, fileItems.size());
        var item = fileItems.get(0);
        assertEquals("This is the content of the file\n", new String(item.get()));

        upload = newFileUpload();
        upload.setFileSizeMax(40);
        req = newMockHttpServletRequest(request, null, null);
        fileItems = upload.parseRequest(req);
        assertEquals(1, fileItems.size());
        item = fileItems.get(0);
        assertEquals("This is the content of the file\n", new String(item.get()));

        // provided Content-Length is larger than the FileSizeMax -> handled by ctor
        upload = newFileUpload();
        upload.setFileSizeMax(5);
        req = newMockHttpServletRequest(request, null, null);
        try {
            upload.parseRequest(req);
            fail("Expected exception.");
        } catch (final FileUploadByteCountLimitException e) {
            assertEquals(5, e.getPermitted());
        }

        // provided Content-Length is wrong, actual content is larger -> handled by LimitedInputStream
        upload = newFileUpload();
        upload.setFileSizeMax(15);
        req = newMockHttpServletRequest(request, null, null);
        try {
            upload.parseRequest(req);
            fail("Expected exception.");
        } catch (final FileUploadByteCountLimitException e) {
            assertEquals(15, e.getPermitted());
        }
    }

    /**
     * Checks whether maxSize works.
     *
     * @throws IOException Test failure.
     */
    @Test
    public void testMaxSizeLimit() throws IOException {
        // @formatter:off
        final var request =
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"file1\"; filename=\"foo1.tab\"\r\n" +
            "Content-Type: text/whatever\r\n" +
            "Content-Length: 10\r\n" +
            "\r\n" +
            "This is the content of the file\n" +
            "\r\n" +
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"file2\"; filename=\"foo2.tab\"\r\n" +
            "Content-Type: text/whatever\r\n" +
            "\r\n" +
            "This is the content of the file\n" +
            "\r\n" +
            "-----1234--\r\n";
        // @formatter:on

        final var upload = newFileUpload();
        upload.setFileSizeMax(-1);
        upload.setSizeMax(200);

        final var req = newMockHttpServletRequest(request, null, null);
        try {
            upload.parseRequest(req);
            fail("Expected exception.");
        } catch (final FileUploadSizeException e) {
            assertEquals(200, e.getPermitted());
        }
    }

    @Test
    public void testMaxSizeLimitUnknownContentLength() throws IOException {
        // @formatter:off
        final var request =
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"file1\"; filename=\"foo1.tab\"\r\n" +
            "Content-Type: text/whatever\r\n" +
            "Content-Length: 10\r\n" +
            "\r\n" +
            "This is the content of the file\n" +
            "\r\n" +
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"file2\"; filename=\"foo2.tab\"\r\n" +
            "Content-Type: text/whatever\r\n" +
            "\r\n" +
            "This is the content of the file\n" +
            "\r\n" +
            "-----1234--\r\n";
        // @formatter:on

        final var upload = newFileUpload();
        upload.setFileSizeMax(-1);
        upload.setSizeMax(300);

        // the first item should be within the max size limit
        // set the read limit to 10 to simulate a "real" stream
        // otherwise the buffer would be immediately filled

        final var req = newMockHttpServletRequest(request, -1L, 10);

        final var it = upload.getItemIterator(req);
        assertTrue(it.hasNext());

        final var item = it.next();
        assertFalse(item.isFormField());
        assertEquals("file1", item.getFieldName());
        assertEquals("foo1.tab", item.getName());

        {
            try (final var baos = new ByteArrayOutputStream();
                    final var stream = item.getInputStream()) {
                IOUtils.copy(stream, baos);
            }

        }

        // the second item is over the size max, thus we expect an error
        // the header is still within size max -> this shall still succeed
        assertTrue(it.hasNext());

        assertThrows(FileUploadException.class, () -> {
            final var item2 = it.next();
            try (final var baos = new ByteArrayOutputStream();
                    final var stream = item2.getInputStream()) {
                IOUtils.copy(stream, baos);
            }
        });
    }
}
