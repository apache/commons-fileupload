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
package org.apache.commons.fileupload.portlet;

import java.io.UnsupportedEncodingException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;

import org.apache.commons.fileupload.Constants;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.FileUploadTest;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import static org.apache.commons.fileupload.util.EncodingConstants.US_ASCII_CHARSET;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link PortletFileUpload}.
 *
 * @see FileUploadTest
 * @since 1.4
 */
public class PortletFileUploadTest {

    /**
     * The upload used for the test.
     */
    private PortletFileUpload upload;

    /**
     * Re-create upload before each test method.
     */
    @Before
    public void setUp() {
        upload = new PortletFileUpload(new DiskFileItemFactory());
    }

    /**
     * Tests parsing of parameter map.
     * @throws UnsupportedEncodingException if {@code US-ASCII} is not supported
     * @throws FileUploadException if a file upload exception occurs
     */
    @Test
    public void parseParameterMap() throws UnsupportedEncodingException, FileUploadException {
        String text = "-----1234\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"foo.tab\"\r\n"
                + "Content-Type: text/whatever\r\n"
                + "\r\n"
                + "This is the content of the file\n"
                + "\r\n"
                + "-----1234\r\n"
                + "Content-Disposition: form-data; name=\"field\"\r\n"
                + "\r\n"
                + "fieldValue\r\n"
                + "-----1234\r\n"
                + "Content-Disposition: form-data; name=\"multi\"\r\n"
                + "\r\n"
                + "value1\r\n"
                + "-----1234\r\n"
                + "Content-Disposition: form-data; name=\"multi\"\r\n"
                + "\r\n"
                + "value2\r\n"
                + "-----1234--\r\n";
        byte[] bytes = text.getBytes(US_ASCII_CHARSET);
        ActionRequest request = new PortletActionRequestMock(bytes, Constants.CONTENT_TYPE);

        Map<String, List<FileItem>> mappedParameters = upload.parseParameterMap(request);
        assertTrue(mappedParameters.containsKey("file"));
        assertEquals(1, mappedParameters.get("file").size());

        assertTrue(mappedParameters.containsKey("field"));
        assertEquals(1, mappedParameters.get("field").size());

        assertTrue(mappedParameters.containsKey("multi"));
        assertEquals(2, mappedParameters.get("multi").size());
    }

}
