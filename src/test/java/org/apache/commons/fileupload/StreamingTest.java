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
package org.apache.commons.fileupload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileUploadBase.IOFileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.junit.Test;

/**
 * Unit test for items with varying sizes.
 */
public class StreamingTest {

    private String getFooter() {
        return "-----1234--\r\n";
    }

    private String getHeader(final String fieldName) {
        return "-----1234\r\n"
            + "Content-Disposition: form-data; name=\"" + fieldName + "\"\r\n"
            + "\r\n";

    }

    private InputStream newInputStream(final ByteArrayInputStream bais) {
        return new InputStream() {

            @Override
            public int read() throws IOException {
                return bais.read();
            }

            @Override
            public int read(final byte[] b, final int off, final int len) throws IOException {
                return bais.read(b, off, Math.min(len, 3));
            }

        };
    }

    private byte[] newRequest() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (OutputStreamWriter osw = new OutputStreamWriter(baos, "US-ASCII")) {
            int add = 16;
            int num = 0;
            for (int i = 0; i < 16384; i += add) {
                if (++add == 32) {
                    add = 16;
                }
                osw.write(getHeader("field" + num++));
                osw.flush();
                for (int j = 0; j < i; j++) {
                    baos.write((byte) j);
                }
                osw.write("\r\n");
            }
            osw.write(getFooter());
        }
        return baos.toByteArray();
    }

    private byte[] newShortRequest() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (OutputStreamWriter osw = new OutputStreamWriter(baos, "US-ASCII")) {
            osw.write(getHeader("field"));
            osw.write("123");
            osw.write("\r\n");
            osw.write(getFooter());
        }
        return baos.toByteArray();
    }

    private List<FileItem> parseUpload(final byte[] bytes) throws FileUploadException {
        return parseUpload(new ByteArrayInputStream(bytes), bytes.length);
    }

    private List<FileItem> parseUpload(final InputStream in, final int length) throws FileUploadException {
        final String contentType = "multipart/form-data; boundary=---1234";
        final FileUploadBase upload = new ServletFileUpload();
        upload.setFileItemFactory(new DiskFileItemFactory());
        final HttpServletRequest request = new MockHttpServletRequest(in, length, contentType);
        return upload.parseRequest(new ServletRequestContext(request));
    }

    private FileItemIterator parseUpload(final int length, final InputStream in) throws FileUploadException, IOException {
        final String contentType = "multipart/form-data; boundary=---1234";
        final FileUploadBase upload = new ServletFileUpload();
        upload.setFileItemFactory(new DiskFileItemFactory());
        final HttpServletRequest request = new MockHttpServletRequest(in, length, contentType);
        return upload.getItemIterator(new ServletRequestContext(request));
    }

    /**
     * Tests a file upload with varying file sizes.
     */
    @Test
    public void testFileUpload() throws IOException, FileUploadException {
        final byte[] request = newRequest();
        final List<FileItem> fileItems = parseUpload(request);
        final Iterator<FileItem> fileIter = fileItems.iterator();
        int add = 16;
        int num = 0;
        for (int i = 0; i < 16384; i += add) {
            if (++add == 32) {
                add = 16;
            }
            final FileItem item = fileIter.next();
            assertEquals("field" + num++, item.getFieldName());
            final byte[] bytes = item.get();
            assertEquals(i, bytes.length);
            for (int j = 0; j < i; j++) {
                assertEquals((byte) j, bytes[j]);
            }
        }
        assertTrue(!fileIter.hasNext());
    }

    /**
     * Test for FILEUPLOAD-135.
     */
    @Test
    public void testFILEUPLOAD135() throws IOException, FileUploadException {
        final byte[] request = newShortRequest();
        final ByteArrayInputStream bais = new ByteArrayInputStream(request);
        try (InputStream inputStream = newInputStream(bais)) {
            final List<FileItem> fileItems = parseUpload(inputStream, request.length);
            final Iterator<FileItem> fileIter = fileItems.iterator();
            assertTrue(fileIter.hasNext());
            final FileItem item = fileIter.next();
            assertEquals("field", item.getFieldName());
            final byte[] bytes = item.get();
            assertEquals(3, bytes.length);
            assertEquals((byte) '1', bytes[0]);
            assertEquals((byte) '2', bytes[1]);
            assertEquals((byte) '3', bytes[2]);
            assertTrue(!fileIter.hasNext());
        }
    }

    /**
     * Tests whether an invalid request throws a proper exception.
     */
    @Test
    public void testFileUploadException() throws IOException, FileUploadException {
        final byte[] request = newRequest();
        final byte[] invalidRequest = new byte[request.length - 11];
        System.arraycopy(request, 0, invalidRequest, 0, request.length - 11);
        assertInstanceOf(MultipartStream.MalformedStreamException.class,
                assertThrows(IOFileUploadException.class, () -> parseUpload(invalidRequest)).getCause());
    }

    /**
     *  whether an {@link InvalidFileNameException} is thrown.
     */
    @Test
    public void testInvalidFileNameException() throws Exception {
        final String fileName = "foo.exe\u0000.png";
        final String request =
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
        final byte[] reqBytes = request.getBytes("US-ASCII");

        final FileItemIterator fileItemIter = parseUpload(reqBytes.length, new ByteArrayInputStream(reqBytes));
        final FileItemStream fileItemStream = fileItemIter.next();
        InvalidFileNameException e = assertThrows(InvalidFileNameException.class, fileItemStream::getName);
        assertEquals(fileName, e.getName());
        assertTrue(e.getMessage().indexOf(fileName) == -1);
        assertTrue(e.getMessage().indexOf("foo.exe\\0.png") != -1);

        final List<FileItem> fileItems = parseUpload(reqBytes);
        final FileItem fileItem = fileItems.get(0);
        e = assertThrows(InvalidFileNameException.class, fileItem::getName);
        assertEquals(fileName, e.getName());
        assertTrue(e.getMessage().indexOf(fileName) == -1);
        assertTrue(e.getMessage().indexOf("foo.exe\\0.png") != -1);
    }

    /**
     * Tests, whether an IOException is properly delegated.
     */
    @Test
    public void testIOException() throws IOException {
        final byte[] request = newRequest();
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
                for (int i = 0; i < length; i++) {
                    final int res = read();
                    if (res == -1) {
                        return i == 0 ? -1 : i;
                    }
                    buffer[offset + i] = (byte) res;
                }
                return length;
            }
        };
        final FileUploadException e = assertThrows(FileUploadException.class, () -> parseUpload(stream, request.length));
        assertTrue(e.getCause() instanceof IOException);
        assertEquals("123", e.getCause().getMessage());
    }
}
