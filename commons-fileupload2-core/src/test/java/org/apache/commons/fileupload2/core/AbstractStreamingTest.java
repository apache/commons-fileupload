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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Unit test for items with varying sizes.
 *
 * @param <AFU> The subclass of FileUpload.
 * @param <R>   The type of FileUpload request.
 * @param <C>   The request context type.
 * @param <I>   The FileItem type.
 * @param <F>   The FileItemFactory type.
 */
public abstract class AbstractStreamingTest<AFU extends AbstractFileUpload<R, I, F>, R, C extends AbstractRequestContext<?>, I extends FileItem<I>, F extends FileItemFactory<I>>
        extends AbstractTest<AFU, R, I, F> {

    protected String getFooter() {
        return "-----1234--\r\n";
    }

    protected String getHeader(final String value) {
        // @formatter:off
        return "-----1234\r\n"
            + "Content-Disposition: form-data; name=\"" + value + "\"\r\n"
            + "\r\n";
        // @formatter:on
    }

    protected abstract F newDiskFileItemFactory();

    protected byte[] newRequest() throws IOException {
        final var baos = new ByteArrayOutputStream();
        try (final var osw = new OutputStreamWriter(baos, StandardCharsets.US_ASCII)) {
            var add = 16;
            var num = 0;
            for (var i = 0; i < 16384; i += add) {
                if (++add == 32) {
                    add = 16;
                }
                osw.write(getHeader("field" + num++));
                osw.flush();
                for (var j = 0; j < i; j++) {
                    baos.write((byte) j);
                }
                osw.write("\r\n");
            }
            osw.write(getFooter());
        }
        return baos.toByteArray();
    }

    protected abstract C newServletRequestContext(final R request);

    protected byte[] newShortRequest() throws IOException {
        final var baos = new ByteArrayOutputStream();
        try (final var osw = new OutputStreamWriter(baos, StandardCharsets.US_ASCII)) {
            osw.write(getHeader("field"));
            osw.write("123");
            osw.write("\r\n");
            osw.write(getFooter());
        }
        return baos.toByteArray();
    }

    protected List<I> parseUpload(final byte[] bytes) throws FileUploadException {
        return parseUpload(new ByteArrayInputStream(bytes), bytes.length);
    }

    protected List<I> parseUpload(final InputStream inputStream, final int length) throws FileUploadException {
        final var contentType = "multipart/form-data; boundary=---1234";

        final var upload = newFileUpload();
        upload.setFileItemFactory(newDiskFileItemFactory());
        final var request = newMockHttpServletRequest(inputStream, length, contentType, -1);

        return upload.parseRequest(newServletRequestContext(request));
    }

    protected FileItemInputIterator parseUpload(final int length, final InputStream inputStream) throws FileUploadException, IOException {
        final var contentType = "multipart/form-data; boundary=---1234";

        final var upload = newFileUpload();
        upload.setFileItemFactory(newDiskFileItemFactory());
        final var request = newMockHttpServletRequest(inputStream, length, contentType, -1);

        return upload.getItemIterator(newServletRequestContext(request));
    }

    /**
     * Tests a file upload with varying file sizes.
     *
     * @throws IOException Test failure.
     */
    @Test
    public void testFileUpload() throws IOException {
        final var request = newRequest();
        final var fileItems = parseUpload(request);
        final var fileIter = fileItems.iterator();
        var add = 16;
        var num = 0;
        for (var i = 0; i < 16384; i += add) {
            if (++add == 32) {
                add = 16;
            }
            final var item = fileIter.next();
            assertEquals("field" + num++, item.getFieldName());
            final var bytes = item.get();
            assertEquals(i, bytes.length);
            for (var j = 0; j < i; j++) {
                assertEquals((byte) j, bytes[j]);
            }
        }
        assertTrue(!fileIter.hasNext());
    }

    /**
     * Test for FILEUPLOAD-135
     *
     * @throws IOException Test failure.
     */
    @Test
    public void testFILEUPLOAD135() throws IOException {
        final var request = newShortRequest();
        final var bais = new ByteArrayInputStream(request);
        final var fileItems = parseUpload(new InputStream() {
            @Override
            public int read() throws IOException {
                return bais.read();
            }

            @Override
            public int read(final byte[] b, final int off, final int len) throws IOException {
                return bais.read(b, off, Math.min(len, 3));
            }

        }, request.length);
        final var fileIter = fileItems.iterator();
        assertTrue(fileIter.hasNext());
        final var item = fileIter.next();
        assertEquals("field", item.getFieldName());
        final var bytes = item.get();
        assertEquals(3, bytes.length);
        assertEquals((byte) '1', bytes[0]);
        assertEquals((byte) '2', bytes[1]);
        assertEquals((byte) '3', bytes[2]);
        assertTrue(!fileIter.hasNext());
    }

    /**
     * Tests, whether an invalid request throws a proper exception.
     *
     * @throws IOException Test failure.
     */
    @Test
    public void testFileUploadException() throws IOException {
        final var request = newRequest();
        final var invalidRequest = new byte[request.length - 11];
        System.arraycopy(request, 0, invalidRequest, 0, request.length - 11);
        try {
            parseUpload(invalidRequest);
            fail("Expected EndOfStreamException");
        } catch (final FileUploadException e) {
            assertTrue(e.getSuppressed()[0] instanceof MultipartInput.MalformedStreamException, e.toString());
        }
    }

    /**
     * Tests, whether an {@link InvalidPathException} is thrown.
     *
     * @throws IOException Test failure.
     */
    @Test
    public void testInvalidFileNameException() throws IOException {
        final var fileName = "foo.exe\u0000.png";
        // @formatter:off
        final var request =
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n" +
            "Content-Type: text/whatever\r\n" +
            "\r\n" +
            "This is the content of the file\n" +
            "\r\n" +
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"field\"\r\n" +
            "\r\n" +
            "fieldValue\r\n" +
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"multi\"\r\n" +
            "\r\n" +
            "value1\r\n" +
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"multi\"\r\n" +
            "\r\n" +
            "value2\r\n" +
            "-----1234--\r\n";
        // @formatter:on
        final var reqBytes = request.getBytes(StandardCharsets.US_ASCII);

        final var fileItemIter = parseUpload(reqBytes.length, new ByteArrayInputStream(reqBytes));
        final var fileItemInput = fileItemIter.next();
        try {
            fileItemInput.getName();
            fail("Expected exception");
        } catch (final InvalidPathException e) {
            assertEquals(fileName, e.getInput());
            assertEquals(26, e.getMessage().indexOf(fileName));
            assertEquals(7, e.getIndex());
            assertTrue(e.getMessage().contains("foo.exe\\0.png"));
        }

        try {
            parseUpload(reqBytes);
            fail("Expected exception");
        } catch (final InvalidPathException e) {
            assertEquals(fileName, e.getInput());
            assertEquals(26, e.getMessage().indexOf(fileName));
            assertEquals(7, e.getIndex());
            assertTrue(e.getMessage().contains("foo.exe\\0.png"));
        }
    }

    /**
     * Tests, whether an IOException is properly delegated.
     *
     * @throws IOException Test failure.
     */
    @Test
    public void testIOException() throws IOException {
        final var request = newRequest();
        final InputStream stream = new FilterInputStream(new ByteArrayInputStream(request)) {
            private int num;

            @Override
            public int read() throws IOException {
                if (++num > 123) {
                    throw new IOException("123");
                }
                return super.read();
            }

            @Override
            public int read(final byte[] buffer, final int offset, final int length) throws IOException {
                for (var i = 0; i < length; i++) {
                    final var res = read();
                    if (res == -1) {
                        return i == 0 ? -1 : i;
                    }
                    buffer[offset + i] = (byte) res;
                }
                return length;
            }
        };
        try {
            parseUpload(stream, request.length);
            fail("Expected IOException");
        } catch (final FileUploadException e) {
            assertTrue(e.getCause() instanceof IOException);
            assertEquals("123", e.getCause().getMessage());
        }
    }

}
