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
package org.apache.commons.fileupload2.javax;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

import org.apache.commons.fileupload2.core.AbstractFileUpload;

final class JavaxHttpServletRequestFactory {

    private static final String BOUNDARY = "----------------------------1234567890";
    private static final String LINE_FEED = "\r\n";

    public static HttpServletRequest createHttpServletRequestWithNullContentType() {
        final var requestData = "foobar".getBytes();
        return new JavaxMockHttpServletRequest(requestData, null);
    }

    public static HttpServletRequest createInvalidHttpServletRequest() {
        final var requestData = "foobar".getBytes();
        return new JavaxMockHttpServletRequest(requestData, AbstractFileUpload.MULTIPART_FORM_DATA);
    }

    public static HttpServletRequest createValidHttpServletRequest(final String[] strFileNames) {
        final var sbRequestData = new StringBuilder();

        // Start building multipart form-data content
        for (int i = 0; i < strFileNames.length; i++) {
            // Add boundary
            sbRequestData.append("--").append(BOUNDARY).append(LINE_FEED);

            // Add content disposition header
            sbRequestData.append("Content-Disposition: form-data; name=\"file")
                    .append(i + 1)
                    .append("\"; filename=\"")
                    .append(strFileNames[i])
                    .append("\"")
                    .append(LINE_FEED);

            // Add content type header
            sbRequestData.append("Content-Type: application/octet-stream")
                    .append(LINE_FEED);

            // Add content transfer encoding header
            sbRequestData.append("Content-Transfer-Encoding: binary")
                    .append(LINE_FEED);

            // Empty line before content
            sbRequestData.append(LINE_FEED);

            // Add some dummy content for the file
            sbRequestData.append("Sample content for file: ")
                    .append(strFileNames[i])
                    .append(LINE_FEED);
        }

        // Add final boundary
        sbRequestData.append("--").append(BOUNDARY).append("--").append(LINE_FEED);

        final var requestData = sbRequestData.toString().getBytes(StandardCharsets.UTF_8);
        final var contentType = AbstractFileUpload.MULTIPART_FORM_DATA + "; boundary=" + BOUNDARY;

        return new JavaxMockHttpServletRequest(requestData, contentType);
    }
}