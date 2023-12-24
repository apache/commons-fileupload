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
package org.apache.commons.fileupload2.jakarta.servlet6;

import java.io.InputStream;

import org.apache.commons.fileupload2.core.AbstractSizesTest;
import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Unit test for items with varying sizes.
 */
public class JakartaSizesTest
        extends AbstractSizesTest<JakartaServletFileUpload<DiskFileItem, DiskFileItemFactory>, HttpServletRequest, DiskFileItem, DiskFileItemFactory> {

    @Override
    protected JakartaServletFileUpload<DiskFileItem, DiskFileItemFactory> newFileUpload() {
        return new JakartaServletFileUpload<>(DiskFileItemFactory.builder().get());
    }

    @Override
    protected JakartaMockHttpServletRequest newMockHttpServletRequest(final InputStream request, final long requestLength, final String contentType,
            final int readLimit) {
        return new JakartaMockHttpServletRequest(request, requestLength, contentType, readLimit);
    }

}
