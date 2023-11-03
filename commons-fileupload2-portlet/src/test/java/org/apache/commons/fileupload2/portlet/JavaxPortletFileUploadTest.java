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
package org.apache.commons.fileupload2.portlet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.portlet.ActionRequest;

import org.apache.commons.fileupload2.core.AbstractFileUploadTest;
import org.apache.commons.fileupload2.core.Constants;
import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link JavaxPortletFileUpload}.
 *
 * @see AbstractFileUploadTest
 */
public class JavaxPortletFileUploadTest
        extends AbstractFileUploadTest<JavaxPortletFileUpload<DiskFileItem, DiskFileItemFactory>, ActionRequest, DiskFileItem, DiskFileItemFactory> {

    public JavaxPortletFileUploadTest() {
        super(new JavaxPortletFileUpload<>(DiskFileItemFactory.builder().get()));
    }

    @Override
    public List<DiskFileItem> parseUpload(final JavaxPortletFileUpload<DiskFileItem, DiskFileItemFactory> upload, final byte[] bytes, final String contentType)
            throws FileUploadException {
        final ActionRequest request = new JavaxPortletMockActionRequest(bytes, contentType);
        return upload.parseRequest(new JavaxPortletRequestContext(request));
    }

    @Test
    public void testParseParameterMap() throws Exception {
        // @formatter:off
        final var text = "-----1234\r\n" +
                      "Content-Disposition: form-data; name=\"file\"; filename=\"foo.tab\"\r\n" +
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
        final var bytes = text.getBytes(StandardCharsets.US_ASCII);
        final ActionRequest request = new JavaxPortletMockActionRequest(bytes, Constants.CONTENT_TYPE);

        final var mappedParameters = upload.parseParameterMap(request);
        assertTrue(mappedParameters.containsKey("file"));
        assertEquals(1, mappedParameters.get("file").size());

        assertTrue(mappedParameters.containsKey("field"));
        assertEquals(1, mappedParameters.get("field").size());

        assertTrue(mappedParameters.containsKey("multi"));
        assertEquals(2, mappedParameters.get("multi").size());
    }

}
