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
package org.apache.commons.fileupload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileUploadBase.FileUploadIOException;
import org.apache.commons.fileupload.FileUploadBase.SizeException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.junit.Test;
import static org.apache.commons.fileupload.util.EncodingConstants.US_ASCII_CHARSET;

/**
 * Unit test for items with varying sizes.
 */
public class SizesTest {

    /**
     * The initial max file size 1.
     */
    private static final long MAX_FILE_SIZE_1 = 40;

    /**
     * Runs a test with varying file sizes.
     * @throws java.io.IOException if an I/O error occurs
     * @throws FileUploadException if a fileupload
     * exception occurs
     */
    @Test
    public void testFileUpload()
            throws IOException, FileUploadException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final int addValue = 16;
        final int iterationLimit = 16384;
        final int equalityCheck = 32;
        int add = addValue;
        int num = 0;
        for (int i = 0;  i < iterationLimit;  i += add) {
            if (++add == equalityCheck) {
                add = addValue;
            }
            String header = "-----1234\r\n"
                + "Content-Disposition: form-data; name=\"field" + (num++) + "\"\r\n"
                + "\r\n";
            baos.write(header.getBytes(US_ASCII_CHARSET));
            for (int j = 0;  j < i;  j++) {
                baos.write((byte) j);
            }
            baos.write("\r\n".getBytes(US_ASCII_CHARSET));
        }
        baos.write("-----1234--\r\n".getBytes(US_ASCII_CHARSET));

        List<FileItem> fileItems =
                Util.parseUpload(new ServletFileUpload(new DiskFileItemFactory()), baos.toByteArray());
        Iterator<FileItem> fileIter = fileItems.iterator();
        add = addValue;
        num = 0;
        for (int i = 0;  i < iterationLimit;  i += add) {
            if (++add == equalityCheck) {
                add = addValue;
            }
            FileItem item = fileIter.next();
            assertEquals("field" + (num++), item.getFieldName());
            byte[] bytes = item.get();
            assertEquals(i, bytes.length);
            for (int j = 0;  j < i;  j++) {
                assertEquals((byte) j, bytes[j]);
            }
        }
        assertTrue(!fileIter.hasNext());
    }

    /**
     * Checks whether limiting the file size works.
     * @throws java.io.IOException if an I/O error occurs
     * @throws FileUploadException if a fileupload
     * exception occurs
     */
    @Test
    public void testFileSizeLimit()
            throws IOException, FileUploadException {
        final String request =
            "-----1234\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"foo.tab\"\r\n"
                + "Content-Type: text/whatever\r\n"
                + "\r\n"
                + "This is the content of the file\n"
                + "\r\n"
                + "-----1234--\r\n";

        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(-1);
        HttpServletRequest req = new HttpServletRequestMock(
                request.getBytes(US_ASCII_CHARSET), Constants.CONTENT_TYPE);
        List<FileItem> fileItems = upload.parseRequest(req);
        assertEquals(1, fileItems.size());
        FileItem item = fileItems.get(0);
        assertEquals("This is the content of the file\n", new String(item.get()));

        upload = new ServletFileUpload(new DiskFileItemFactory());
        final int fileSizeMax = 40;
        upload.setFileSizeMax(fileSizeMax);
        req = new HttpServletRequestMock(request.getBytes(US_ASCII_CHARSET), Constants.CONTENT_TYPE);
        fileItems = upload.parseRequest(req);
        assertEquals(1, fileItems.size());
        item = fileItems.get(0);
        assertEquals("This is the content of the file\n", new String(item.get()));

        upload = new ServletFileUpload(new DiskFileItemFactory());
        final int fileSizeMax2 = 30;
        upload.setFileSizeMax(fileSizeMax2);
        req = new HttpServletRequestMock(request.getBytes(US_ASCII_CHARSET), Constants.CONTENT_TYPE);
        try {
            upload.parseRequest(req);
            fail("Expected exception.");
        } catch (FileUploadBase.FileSizeLimitExceededException e) {
            assertEquals(fileSizeMax2, e.getPermittedSize());
        }
    }

    /**
     * Checks whether a faked Content-Length header is detected.
     * @throws java.io.IOException if an I/O error occurs
     * @throws FileUploadException if a fileupload
     * exception occurs
     */
    @Test
    public void testFileSizeLimitWithFakedContentLength()
            throws IOException, FileUploadException {
        final String request =
            "-----1234\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"foo.tab\"\r\n"
                + "Content-Type: text/whatever\r\n"
                + "Content-Length: 10\r\n"
                + "\r\n"
                + "This is the content of the file\n"
                + "\r\n"
                + "-----1234--\r\n";

        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(-1);
        HttpServletRequest req = new HttpServletRequestMock(
                request.getBytes(US_ASCII_CHARSET), Constants.CONTENT_TYPE);
        List<FileItem> fileItems = upload.parseRequest(req);
        assertEquals(1, fileItems.size());
        FileItem item = fileItems.get(0);
        assertEquals("This is the content of the file\n", new String(item.get()));

        upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(MAX_FILE_SIZE_1);
        req = new HttpServletRequestMock(request.getBytes(US_ASCII_CHARSET), Constants.CONTENT_TYPE);
        fileItems = upload.parseRequest(req);
        assertEquals(1, fileItems.size());
        item = fileItems.get(0);
        assertEquals("This is the content of the file\n", new String(item.get()));

        // provided Content-Length is larger than the FileSizeMax -> handled by ctor
        upload = new ServletFileUpload(new DiskFileItemFactory());
        final int fileSizeMax1 = 5;
        upload.setFileSizeMax(fileSizeMax1);
        req = new HttpServletRequestMock(request.getBytes(US_ASCII_CHARSET), Constants.CONTENT_TYPE);
        try {
            upload.parseRequest(req);
            fail("Expected exception.");
        } catch (FileUploadBase.FileSizeLimitExceededException e) {
            assertEquals(fileSizeMax1, e.getPermittedSize());
        }

        // provided Content-Length is wrong, actual content is larger -> handled by LimitedInputStream
        upload = new ServletFileUpload(new DiskFileItemFactory());
        final int fileSizeMax2 = 15;
        upload.setFileSizeMax(fileSizeMax2);
        req = new HttpServletRequestMock(request.getBytes(US_ASCII_CHARSET), Constants.CONTENT_TYPE);
        try {
            upload.parseRequest(req);
            fail("Expected exception.");
        } catch (FileUploadBase.FileSizeLimitExceededException e) {
            assertEquals(fileSizeMax2, e.getPermittedSize());
        }
    }

    /**
     * Checks whether the maxSize works.
     * @throws java.io.IOException if an I/O error occurs
     * @throws FileUploadException if a fileupload
     * exception occurs
     */
    @Test
    public void testMaxSizeLimit()
            throws IOException, FileUploadException {
        final String request =
            "-----1234\r\n"
                + "Content-Disposition: form-data; name=\"file1\"; filename=\"foo1.tab\"\r\n"
                + "Content-Type: text/whatever\r\n"
                + "Content-Length: 10\r\n"
                + "\r\n"
                + "This is the content of the file\n"
                + "\r\n"
                + "-----1234\r\n"
                + "Content-Disposition: form-data; name=\"file2\"; filename=\"foo2.tab\"\r\n"
                + "Content-Type: text/whatever\r\n"
                + "\r\n"
                + "This is the content of the file\n"
                + "\r\n"
                + "-----1234--\r\n";

        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(-1);
        final int sizeMax = 200;
        upload.setSizeMax(sizeMax);

        HttpServletRequestMock req = new HttpServletRequestMock(
                request.getBytes(US_ASCII_CHARSET), Constants.CONTENT_TYPE);
        try {
            upload.parseRequest(req);
            fail("Expected exception.");
        } catch (FileUploadBase.SizeLimitExceededException e) {
            assertEquals(sizeMax, e.getPermittedSize());
        }
    }

    /**
     * Tests that the maximum size is not exceed if content lenght is unknown.
     * @throws java.io.IOException if an I/O error occurs
     * @throws FileUploadException if a fileupload
     * exception occurs
     */
    @Test
    public void testMaxSizeLimitUnknownContentLength()
            throws IOException, FileUploadException {
        final String request =
            "-----1234\r\n"
                + "Content-Disposition: form-data; name=\"file1\"; filename=\"foo1.tab\"\r\n"
                + "Content-Type: text/whatever\r\n"
                + "Content-Length: 10\r\n"
                + "\r\n"
                + "This is the content of the file\n"
                + "\r\n"
                + "-----1234\r\n"
                + "Content-Disposition: form-data; name=\"file2\"; filename=\"foo2.tab\"\r\n"
                + "Content-Type: text/whatever\r\n"
                + "\r\n"
                + "This is the content of the file\n"
                + "\r\n"
                + "-----1234--\r\n";

        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(-1);
        final int sizeMax = 300;
        upload.setSizeMax(sizeMax);

        // the first item should be within the max size limit
        // set the read limit to 10 to simulate a "real" stream
        // otherwise the buffer would be immediately filled

        HttpServletRequestMock req = new HttpServletRequestMock(
                request.getBytes(US_ASCII_CHARSET), Constants.CONTENT_TYPE);
        req.setContentLength(-1);
        final int readLimit = 10;
        req.setReadLimit(readLimit);

        FileItemIterator it = upload.getItemIterator(req);
        assertTrue(it.hasNext());

        FileItemStream item = it.next();
        assertFalse(item.isFormField());
        assertEquals("file1", item.getFieldName());
        assertEquals("foo1.tab", item.getName());

        InputStream stream = item.openStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Streams.copy(stream, baos, true);

        // the second item is over the size max, thus we expect an error
        try {
            // the header is still within size max -> this shall still succeed
            assertTrue(it.hasNext());
        } catch (SizeException e) {
            fail();
        }

        item = it.next();

        try {
            stream = item.openStream();
            baos = new ByteArrayOutputStream();
            Streams.copy(stream, baos, true);
            fail();
        } catch (FileUploadIOException e) {
            // expected
        }
    }

}
