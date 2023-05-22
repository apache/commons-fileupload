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
package org.apache.commons.fileupload2.jakarta;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.fileupload2.Constants;
import org.apache.commons.fileupload2.SizesTest;
import org.apache.commons.fileupload2.disk.DiskFileItemFactory;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Unit test for items with varying sizes.
 */
public class JakartaSizesTest extends SizesTest<JakartaServletFileUpload, HttpServletRequest> {

    @Override
    protected JakartaServletFileUpload newFileUpload() {
        return new JakartaServletFileUpload(new DiskFileItemFactory());
    }

    @Override
    protected JakartaMockHttpServletRequest newMockHttpServletRequest(final String request, final Integer overrideContenLength, final Integer overrideReadLimit) {
        final byte[] requestData = request.getBytes(StandardCharsets.US_ASCII);
        return new JakartaMockHttpServletRequest(new ByteArrayInputStream(requestData), overrideContenLength != null ? overrideContenLength : requestData.length,
                Constants.CONTENT_TYPE, overrideReadLimit != null ? overrideReadLimit : -1);
    }

}
