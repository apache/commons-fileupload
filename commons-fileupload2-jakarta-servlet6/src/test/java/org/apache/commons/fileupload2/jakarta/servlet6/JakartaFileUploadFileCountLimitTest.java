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

package org.apache.commons.fileupload2.jakarta.servlet6;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;

import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.core.FileUploadFileCountLimitException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Tests that {@link JakartaServletFileUpload#parseRequest(org.apache.commons.fileupload2.core.RequestContext)} throws a
 * {@link FileUploadFileCountLimitException} when {@code maxFileCount} is exceeded.
 */
class JakartaFileUploadFileCountLimitTest {

    /**
     * Builds a multipart/form-data request body containing {@code fileCount} file parts, using the boundary {@code ---1234}.
     *
     * @param fileCount the number of file parts to include
     * @return the multipart body as a UTF-8 encoded byte array
     */
    private static byte[] buildMultipartBody(final int fileCount) {
        final var boundary = "---1234";
        final var sb = new StringBuilder();
        for (int i = 1; i <= fileCount; i++) {
            sb.append("--").append(boundary).append("\r\n");
            sb.append("Content-Disposition: form-data; name=\"file").append(i).append("\"; filename=\"file").append(i).append(".txt\"\r\n");
            sb.append("Content-Type: text/plain\r\n");
            sb.append("\r\n");
            sb.append("Content of file ").append(i).append("\r\n");
        }
        sb.append("--").append(boundary).append("--\r\n");
        return sb.toString().getBytes(StandardCharsets.US_ASCII);
    }

    /**
     * Tests that {@code parseRequest(RequestContext)} throws {@link FileUploadFileCountLimitException} when the number of uploaded files exceeds the configured
     * {@code maxFileCount}. The request contains {@code maxFileCount + 1} files so that the limit is always exceeded.
     *
     * @param maxFileCount the maximum number of files allowed, set to a non-default value
     * @throws Exception Thrown if the test fails unexpectedly
     */
    @ParameterizedTest
    @ValueSource(longs = { 1, 2, 4, 8 })
    void testParseRequestThrowsFileUploadFileCountLimitException(final long maxFileCount) throws Exception {
        // Build a request with one more file than the limit allows
        final int fileCount = (int) maxFileCount + 1;
        final var contentType = "multipart/form-data; boundary=---1234";
        final var body = buildMultipartBody(fileCount);
        final HttpServletRequest request = new JakartaMockHttpServletRequest(body, contentType);
        final var requestContext = new JakartaServletRequestContext(request);
        final var upload = new JakartaServletFileUpload<>(DiskFileItemFactory.builder().get());
        upload.setMaxFileCount(maxFileCount);
        final var exception = assertThrows(FileUploadFileCountLimitException.class, () -> upload.parseRequest(requestContext));
        assertEquals(maxFileCount, exception.getPermitted());
        assertEquals(maxFileCount, exception.getActualSize());
    }
}
