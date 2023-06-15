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

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload2.core.AbstractRequestContext;

/**
 * Provides access to the request information needed for a request made to an HTTP servlet.
 */
public class JavaxServletRequestContext extends AbstractRequestContext<HttpServletRequest> {

    /**
     * Constructs a context for this request.
     *
     * @param request The request to which this context applies.
     */
    public JavaxServletRequestContext(final HttpServletRequest request) {
        super(request::getHeader, request::getContentLength, request);
    }

    /**
     * Gets the character encoding for the request.
     *
     * @return The character encoding for the request.
     */
    @Override
    public String getCharacterEncoding() {
        return getRequest().getCharacterEncoding();
    }

    /**
     * Gets the content type of the request.
     *
     * @return The content type of the request.
     */
    @Override
    public String getContentType() {
        return getRequest().getContentType();
    }

    /**
     * Gets the input stream for the request.
     *
     * @return The input stream for the request.
     * @throws IOException if a problem occurs.
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return getRequest().getInputStream();
    }

}
