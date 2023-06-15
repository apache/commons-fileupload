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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import org.apache.commons.io.Charsets;

/**
 * Abstracts access to the request information needed for file uploads.
 * <p>
 * This interface should be implemented for each type of request that may be handled by FileUpload, such as servlets and portlets.
 * </p>
 */
public interface RequestContext {

    /**
     * Gets the character encoding for the request.
     *
     * @return The character encoding for the request.
     */
    String getCharacterEncoding();

    /**
     * Gets the character encoding for the request or null if unspecified.
     *
     * @return The character encoding for the request or null.
     * @throws UnsupportedCharsetException If the named Charset is unavailable.
     */
    default Charset getCharset() throws UnsupportedCharsetException {
        return Charsets.toCharset(getCharacterEncoding(), null);
    }

    /**
     * Gets the content length of the request.
     *
     * @return The content length of the request.
     */
    long getContentLength();

    /**
     * Gets the content type of the request.
     *
     * @return The content type of the request.
     */
    String getContentType();

    /**
     * Gets the input stream for the request.
     *
     * @return The input stream for the request.
     * @throws IOException if a problem occurs.
     */
    InputStream getInputStream() throws IOException;

}
