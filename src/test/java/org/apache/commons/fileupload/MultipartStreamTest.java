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

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.MultipartStream.MalformedStreamException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link org.apache.commons.fileupload.MultipartStream}.
 */
public class MultipartStreamTest {

    static private final String BOUNDARY_TEXT = "myboundary";

    @Test
    public void testMalformedUploadTruncatedHeaders()
            throws IOException, FileUploadException {
        final String request =
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
            "This is the content of the file\n";

        final ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(-1);
        upload.setSizeMax(-1);

        final MockHttpServletRequest req = new MockHttpServletRequest(
                request.getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        assertThrows(FileUploadBase.IOFileUploadException.class, () -> upload.parseRequest(req));
    }

    @Test
    public void testMalformedUploadTruncatedHeadersOnBoundary() throws IOException {
        final StringBuilder request = new StringBuilder(
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"file1\"; filename=\"foo1.tab\"\r\n" +
            "Content-Type: text/whatever\r\n" +
            "Content-Length: 10\r\n" +
            "X-Padding: ");
        final int paddingLength = MultipartStream.DEFAULT_BUFSIZE - request.length();
        for (int i = 0; i < paddingLength; i++) {
            request.append('x');
        }

        final ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(-1);
        upload.setSizeMax(-1);
        upload.setPartHeaderSizeMax(-1);

        final MockHttpServletRequest req = new MockHttpServletRequest(
                request.toString().getBytes("US-ASCII"), Constants.CONTENT_TYPE);
        final FileUploadException e = assertThrows(FileUploadException.class, () -> upload.parseRequest(req));
        assertInstanceOf(MalformedStreamException.class, e.getCause());
    }

    @Test
    public void testSmallBuffer() throws Exception {
        final String strData = "foobar";
        final byte[] contents = strData.getBytes();
        final InputStream input = new ByteArrayInputStream(contents);
        final byte[] boundary = BOUNDARY_TEXT.getBytes();
        final int iBufSize = 1;
        assertThrows(IllegalArgumentException.class, () -> new MultipartStream(input, boundary, iBufSize, new MultipartStream.ProgressNotifier(null, contents.length)));
    }

    @Test
    public void testThreeParamConstructor() throws Exception {
        final String strData = "foobar";
        final byte[] contents = strData.getBytes();
        final InputStream input = new ByteArrayInputStream(contents);
        final byte[] boundary = BOUNDARY_TEXT.getBytes();
        final int iBufSize = boundary.length + MultipartStream.BOUNDARY_PREFIX.length + 1;
        final MultipartStream ms = new MultipartStream(input, boundary, iBufSize, new MultipartStream.ProgressNotifier(null, contents.length));
        assertNotNull(ms);
    }

    @Test
    public void testTwoParamConstructor() throws Exception {
        final String strData = "foobar";
        final byte[] contents = strData.getBytes();
        final InputStream input = new ByteArrayInputStream(contents);
        final byte[] boundary = BOUNDARY_TEXT.getBytes();
        final MultipartStream ms = new MultipartStream(input, boundary, new MultipartStream.ProgressNotifier(null, contents.length));
        assertNotNull(ms);
    }
}
