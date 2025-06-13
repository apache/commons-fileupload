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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.io.File;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link DiskFileUpload}. Remove when deprecated class is removed.
 *
 * @since 1.4
 */
@SuppressWarnings({"deprecation"}) // unit tests for deprecated class
public class DiskFileUploadTest {

    private DiskFileUpload upload;

    @Before
    public void setUp() {
        upload = new DiskFileUpload();
    }

    /** Proposed test for FILEUPLOAD-293. As of yet, doesn't reproduce the problem.
     */
    @Test
    public void testMoveFile() throws Exception {
        final DiskFileUpload myUpload = new DiskFileUpload();
        myUpload.setSizeThreshold(0);
        final String content =
                "-----1234\r\n" +
                "Content-Disposition: form-data; name=\"file\";" +
                "filename=\"foo.tab\"\r\n" +
                "Content-Type: text/whatever\r\n" +
                "\r\n" +
                "This is the content of the file\n" +
                "\r\n" +
                "-----1234--\r\n";
    	final byte[] contentBytes = content.getBytes("US-ASCII");
        final HttpServletRequest request = new MockHttpServletRequest(contentBytes, Constants.CONTENT_TYPE);
        final List<FileItem> items = myUpload.parseRequest(request);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        final DiskFileItem dfi = (DiskFileItem) items.get(0);
        final File out = File.createTempFile("install", ".tmp");
        dfi.write(out);
    }

    @Test
    public void testWithInvalidRequest() {
        final HttpServletRequest req = HttpServletRequestFactory.createInvalidHttpServletRequest();
        assertThrows(FileUploadException.class, () -> upload.parseRequest(req));
    }

    @Test
    public void testWithNullContentType() {
        final HttpServletRequest req = HttpServletRequestFactory.createHttpServletRequestWithNullContentType();
        assertThrowsExactly(DiskFileUpload.InvalidContentTypeException.class, () -> upload.parseRequest(req));
    }
}
