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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload2.Constants;
import org.apache.commons.fileupload2.SizesTest;
import org.apache.commons.fileupload2.disk.DiskFileItemFactory;

/**
 * Unit test for items with varying sizes.
 */
public class JavaxSizesTest extends SizesTest<ServletFileUpload, HttpServletRequest> {

    @Override
    protected ServletFileUpload newFileUpload() {
        return new ServletFileUpload(new DiskFileItemFactory());
    }

    @Override
    protected JavaxMockHttpServletRequest newMockHttpServletRequest(final String request, final Integer overrideContenLength, final Integer overrideReadLimit) {
        final byte[] requestData = request.getBytes(StandardCharsets.US_ASCII);
        return new JavaxMockHttpServletRequest(new ByteArrayInputStream(requestData), overrideContenLength != null ? overrideContenLength : requestData.length,
                Constants.CONTENT_TYPE, overrideReadLimit != null ? overrideReadLimit : -1);
    }

}
