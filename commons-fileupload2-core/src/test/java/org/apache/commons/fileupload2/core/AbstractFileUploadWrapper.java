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
package org.apache.commons.fileupload2.core;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Common tests for implementations of {@link AbstractFileUpload}. This is a parameterized test. Tests must be valid and common to all implementations of FileUpload
 * added as parameter in this class.
 *
 * @param <T> The type for {@link AbstractFileUpload}.
 */
public abstract class AbstractFileUploadWrapper<T extends AbstractFileUpload<?>> {

    protected final T upload;

    protected AbstractFileUploadWrapper(final T fileUpload) {
        this.upload = fileUpload;
    }

    public List<FileItem> parseUpload(final T upload, final byte[] bytes) throws FileUploadException {
        return parseUpload(upload, bytes, Constants.CONTENT_TYPE);
    }

    public abstract List<FileItem> parseUpload(final T upload, final byte[] bytes, final String contentType) throws FileUploadException;

    public List<FileItem> parseUpload(final T upload, final String content) throws FileUploadException {
        final byte[] bytes = content.getBytes(StandardCharsets.US_ASCII);
        return parseUpload(upload, bytes, Constants.CONTENT_TYPE);
    }

}
