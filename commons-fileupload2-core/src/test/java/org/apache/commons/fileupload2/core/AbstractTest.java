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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Abstract test.
 *
 * @param <AFU> The AbstractFileUpload type.
 * @param <R>   The FileUpload request type.
 * @param <I>   The FileItem type.
 * @param <F>   The FileItemFactory type.
 */
public abstract class AbstractTest<AFU extends AbstractFileUpload<R, I, F>, R, I extends FileItem<I>, F extends FileItemFactory<I>> {

    protected abstract AFU newFileUpload();

    protected R newMockHttpServletRequest(final byte[] request, final Long overrideContenLength, final String contentType, final Integer overrideReadLimit) {
        return newMockHttpServletRequest(new ByteArrayInputStream(request), overrideContenLength != null ? overrideContenLength : request.length, contentType,
                overrideReadLimit != null ? overrideReadLimit : -1);
    }

    protected abstract R newMockHttpServletRequest(InputStream requestInputStream, long requestLength, String contentType, int readLimit);

    protected R newMockHttpServletRequest(final String request, final Long overrideContenLength, final Integer overrideReadLimit) {
        return newMockHttpServletRequest(request.getBytes(StandardCharsets.US_ASCII), overrideContenLength, Constants.CONTENT_TYPE, overrideReadLimit);
    }

}
